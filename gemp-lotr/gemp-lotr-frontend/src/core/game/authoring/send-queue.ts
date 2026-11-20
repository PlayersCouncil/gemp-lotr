/**
 * How the Step tab's form goes to the server (the legacy scenarioAuthor.js sendStep and asFiled): only the parts the
 * author has changed are sent (the rest is the server's, which may know more about the step: the answer the game has
 * just written into it), one send at a time, what is changed meanwhile going once the one on its way is back. A send
 * the server refuses is not sent again as it is; one of several parts is tried part by part, so that one that is
 * wrong does not hold the others back. What comes back as the server writes it (a part written out in full) is taken
 * into the form where the author has left it as it was sent and is not in that field; what was changed elsewhere
 * (the game writing in an answer) is taken where the author has not changed it since. Pure: the page gives it a way
 * to send (StepTransport) and says which field has the focus.
 */
import { signal, WritableSignal } from '@angular/core';
import { DraftState, DraftStep, StepTexts } from './authoring-state';
import {
  canon,
  conditionsAsBefore,
  Diff,
  diff,
  fieldsOf,
  rebase,
  TYPED,
  valueAt,
  without,
  withValue,
  WorkStep,
  working,
  Written,
  written,
} from './step-model';
import { locate, own } from './steps';

/** What the form needs of the page. */
export interface StepTransport {
  /** Sends a merge of the step's parts; resolves to the state it came back with, or null if it was refused (said). */
  merge(uid: string, set: Readonly<Record<string, unknown>>, unset: readonly string[]): Promise<DraftState | null>;
  /** What is said to have gone wrong, and saying it. */
  problem(): string | null;
  sayProblem(text: string | null): void;
  /** Whether a field of the form ("when", "allow.passWhen") has the focus. */
  focused(field: string): boolean;
  /** Whether this form is still the one the page shows. */
  current(form: StepForm): boolean;
}

/** The fields whose parts the server writes back its own way, and the part each is in. */
const FILED: readonly (readonly [field: string, part: string])[] = [
  ...TYPED.map((name) => [name, name] as const),
  ['whenRequire', 'when'],
  ['allow.passWhen', 'allow'],
];

/** Those fields, with the ones a step has as many of as it has messages (each message's condition), and its refusals. */
function filedFields(theirs: WorkStep): readonly (readonly [field: string, part: string])[] {
  return [
    ...FILED,
    ['refuses', 'refuses'],
    ...theirs.messages.map((_, i) => [`messages.${i}.require`, 'messages'] as const),
  ];
}

/** The text a step's state is known by: when it changes, the step was changed (by the form, or elsewhere). */
export function baseOf(step: DraftStep): string {
  return canon(own(step));
}

/** The form for one step (by its name): its working copy, what was last sent of it, and its sends. */
export class StepForm {
  /** The working copy: what the fields show. */
  readonly work: WritableSignal<WorkStep>;
  /** The step as it was last sent, or first found (its file's shape). */
  origin: Written;
  /** The server's step the form was last brought up to (baseOf). */
  base: string;
  /** Where the step is: in the script (from 0) and in its block (from 0, or -1). */
  index: number;
  sub: number;

  private sending = false;
  private again = false;
  private waiting: ((went: boolean) => void)[] = [];
  /** What the server last refused, which is not sent again as it is. */
  private refused: string | null = null;
  /** What is on its way: the step, and the working copy, as they were sent. */
  private inFlight: { step: Written; work: WorkStep } | null = null;

  constructor(
    readonly uid: string,
    step: DraftStep,
    texts: StepTexts | undefined,
    place: { index: number; sub: number },
    private readonly transport: StepTransport,
  ) {
    const work = working(step, texts);
    this.work = signal(work);
    this.origin = written(work);
    this.base = baseOf(step);
    this.index = place.index;
    this.sub = place.sub;
  }

  /** Whether a send is on its way. */
  get busy(): boolean {
    return this.sending;
  }

  /** The parts that are not as they were last sent. */
  dirty(): Diff {
    return diff(written(this.work()), this.origin);
  }

  /**
   * The parts changed since what is on its way (or, with nothing on its way, since the last send): what a page that
   * goes away now would lose. Their names.
   */
  unsent(): string[] {
    const now = diff(written(this.work()), this.inFlight?.step ?? this.origin);
    return [...Object.keys(now.set), ...now.unset];
  }

  /**
   * Takes the server's step (another state of it, changed elsewhere) into the form where the author has not changed it
   * since it was last sent. Nothing while a send is on its way: its answer brings the step.
   */
  update(step: DraftStep, texts: StepTexts | undefined): boolean {
    const base = baseOf(step);
    if (this.sending || base === this.base) return false;
    this.base = base;
    return this.take(working(step, texts));
  }

  private take(theirs: WorkStep, skip: readonly string[] = []): boolean {
    const next = rebase(this.work(), this.origin, theirs, skip);
    this.origin = next.origin;
    if (next.changed) this.work.set(next.work);
    return next.changed;
  }

  /**
   * Sends the parts changed since the last send. Resolves true when they went (or there was nothing to send), false
   * when the server refused them: they stay in the form, unsent, to be put right.
   */
  send(): Promise<boolean> {
    if (this.sending) {
      // One at a time: what has changed since goes when this one is back
      this.again = true;
      return new Promise((resolve) => this.waiting.push(resolve));
    }
    const changes = this.dirty();
    if (!changes.any) return Promise.resolve(true);
    // (what was refused is refused again as it is: it is not sent again, and stays unsent)
    if (canon(changes) === this.refused) return Promise.resolve(false);
    return this.go(changes);
  }

  private async go(changes: Diff): Promise<boolean> {
    const sentWork = this.work();
    const sent = written(sentWork);
    this.sending = true;
    this.inFlight = { step: sent, work: sentWork };
    const state = await this.transport.merge(this.uid, changes.set, changes.unset);
    let went: boolean;
    if (state !== null) {
      this.took(changes, sent);
      this.refused = null;
      if (this.transport.current(this)) this.cameBack(state, changes, sentWork);
      went = true;
    } else {
      went = await this.partByPart(changes, sent);
    }
    return this.finished(went);
  }

  private took(changes: Diff, sent: Written): void {
    const origin: Record<string, unknown> = without(this.origin, ...changes.unset);
    for (const name of Object.keys(changes.set)) origin[name] = sent[name];
    this.origin = origin;
  }

  /** The step as it came back: where it is now, the server's way of writing what was sent, what changed elsewhere. */
  private cameBack(state: DraftState, changes: Diff, sentWork: WorkStep): void {
    const found = locate(state, this.uid);
    if (!found) return;
    this.index = found.index;
    this.sub = found.sub;
    this.base = baseOf(found.step);
    const theirs = working(found.step, state.texts[this.uid]);
    this.asFiled(theirs, changes, sentWork);
    // Anything else about the step that is not as the form has it was changed somewhere else
    this.take(theirs, [...Object.keys(changes.set), ...changes.unset]);
  }

  /**
   * What was typed in full ({ ... }) has been read by the server, and comes back written the way a file has it. Where
   * the form still has what was sent, and the author is not in that field, it takes the file's way of writing it: left
   * as it was typed, it would look from then on as if somebody else had changed the step.
   */
  private asFiled(theirs: WorkStep, changes: Diff, sentWork: WorkStep): void {
    let work = this.work();
    const parts = new Set<string>();
    for (const [field, part] of filedFields(theirs)) {
      if (!(part in changes.set)) continue;
      const mine = valueAt(work, field);
      const filed = valueAt(theirs, field);
      if (typeof filed !== 'string' || mine === filed || mine !== valueAt(sentWork, field)) continue;
      if (this.transport.focused(field)) continue;
      work = withValue(work, field, filed);
      parts.add(part);
    }
    if (parts.size === 0) return;
    this.work.set(work);
    // (the part is now as the server has it, where the form took all of it back)
    const now = written(work);
    const theirsFiled = written(theirs);
    const origin: Record<string, unknown> = { ...this.origin };
    for (const part of parts) if (canon(now[part]) === canon(theirsFiled[part])) origin[part] = theirsFiled[part];
    this.origin = origin;
  }

  /**
   * Refused: it stays unsent (and is not sent again as it is). If several parts went together, each is tried by
   * itself, so that one that is wrong does not hold the others back; what is said is the last refusal.
   */
  private async partByPart(changes: Diff, sent: Written): Promise<boolean> {
    const parts = [
      ...Object.keys(changes.set).map((name) => ({ set: name })),
      ...changes.unset.map((unset) => ({ unset })),
    ];
    if (parts.length < 2) {
      const only = Object.keys(changes.set)[0];
      if (only !== undefined && (await this.salvage(only, changes.set[only]))) {
        this.refused = canon(diff(sent, this.origin));
        return false;
      }
      this.refused = canon(changes);
      return false;
    }
    let said = this.transport.problem();
    for (const part of parts) {
      const one: Diff =
        'set' in part
          ? { set: { [part.set]: changes.set[part.set] }, unset: [], any: true }
          : { set: {}, unset: [part.unset], any: true };
      if ((await this.transport.merge(this.uid, one.set, one.unset)) !== null) this.took(one, sent);
      else {
        said = this.transport.problem();
        if ('set' in part) await this.salvage(part.set, changes.set[part.set]);
      }
    }
    this.refused = canon(this.dirty());
    if (this.transport.problem() === null) this.transport.sayProblem(said);
    return false;
  }

  /**
   * A step's messages (or its end) refused: if a message's condition was changed with them, they are tried with the
   * conditions as they were last sent, so that what else was typed in them goes (a condition with a mistake in it
   * stays in the form, unsent, with the server's words said). Whether that went.
   */
  private async salvage(part: string, value: unknown): Promise<boolean> {
    const kept = conditionsAsBefore(part, value, this.origin[part]);
    if (kept === null) return false;
    const said = this.transport.problem();
    if ((await this.transport.merge(this.uid, { [part]: kept }, [])) === null) return false;
    this.origin = { ...this.origin, [part]: kept };
    this.transport.sayProblem(said);
    return true;
  }

  private finished(went: boolean): boolean {
    this.sending = false;
    this.inFlight = null;
    const waiting = this.waiting;
    this.waiting = [];
    if (this.again) {
      // (what was changed while this was on its way goes now, even when another step has been turned to since)
      this.again = false;
      void this.send().then((did) => {
        for (const resolve of waiting) resolve(did);
      });
      return went;
    }
    for (const resolve of waiting) resolve(went);
    return went;
  }

  /**
   * What the form has that a page going away would lose (the parts changed since the send on its way), and the
   * working copy they are in: for the next page's form to take back (`restore`). Null when there is none.
   */
  carry(): CarriedForm | null {
    const parts = this.unsent();
    return parts.length === 0 ? null : { uid: this.uid, parts, work: this.work() };
  }

  /** Takes into the form the parts a page that went away carried (see carry): they are unsent here. */
  restore(carried: CarriedForm): boolean {
    if (carried.uid !== this.uid) return false;
    let work = this.work();
    for (const part of carried.parts) {
      for (const field of fieldsOf(part)) {
        work = withValue(work, field, (carried.work as Readonly<Record<string, unknown>>)[field]);
      }
    }
    this.work.set(work);
    return this.dirty().any;
  }
}

/** What a form carries to the next page (StepForm.carry). */
export interface CarriedForm {
  uid: string;
  parts: readonly string[];
  work: WorkStep;
}

/** A carried form as it was stored (anything else: none). */
export function carriedOf(value: unknown): CarriedForm | null {
  if (value === null || typeof value !== 'object') return null;
  const { uid, parts, work } = value as Record<string, unknown>;
  if (typeof uid !== 'string' || !Array.isArray(parts) || work === null || typeof work !== 'object') return null;
  const names = (parts as unknown[]).filter((part): part is string => typeof part === 'string');
  const step = work as Partial<WorkStep>;
  if (!Array.isArray(step.messages) || typeof step.allow !== 'object' || typeof step.end !== 'object') return null;
  return { uid, parts: names, work: work as WorkStep };
}
