import { DOCUMENT } from '@angular/common';
import { computed, DestroyRef, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring, MessageAt, StepFlushResult } from '../../../../core/game/authoring/authoring.service';
import { carriedOf, CarriedForm, StepForm, StepTransport } from '../../../../core/game/authoring/send-queue';
import { DraftStep } from '../../../../core/game/authoring/authoring-state';
import { childInsertOp } from '../../../../core/game/authoring/step-ops';
import { FormKind, withValue, WorkStep } from '../../../../core/game/authoring/step-model';
import { Notifier } from '../../../../core/notifier/notifier';
import { askAuthor } from '../widgets/author-confirm';
import { isBlock, kidsOf, locate, numberOf, stepsOf } from '../../../../core/game/authoring/steps';
import { SessionStore } from '../../../../core/platform/session-store';
import { TYPING_SEND_MS } from '../widgets/author-send.directive';

/** Where the form's unsent parts are kept while the page goes to a fresh game (the next page's form takes them). */
export const STEP_CARRY_KEY = 'gemp-author-step-unsent';

/** A field with the focus, and where its caret was. */
interface FieldFocus {
  field: string;
  start: number;
  end: number;
}

/** Where the form's step is, for what the tab shows around it. */
export interface FormPlace {
  index: number;
  /** In its block, from 0; -1: none. */
  sub: number;
  kind: FormKind;
  /** "34" or "34.2". */
  number: string;
}

/**
 * The Step tab's form, for the components that show it (provided by the tab; the legacy scenarioAuthor.js `form`):
 * the step selected, as a working copy (StepForm) that only the author's typing changes, and what comes back of what
 * was sent where the author has left it alone; when it is sent (at once, or a moment after typing pauses), and the
 * panel's say in it (Authoring.attachStepTab: send it before going elsewhere, whether the author is typing, start
 * afresh, keep it across a fresh game). Fields name what they edit by its path in the working copy ("when",
 * "messages.0.text"); `data-field` on each says which it is.
 */
@Injectable()
export class StepEditor {
  readonly authoring = inject(Authoring);
  private readonly store = inject(SessionStore);
  private readonly document = inject(DOCUMENT);
  private readonly notifier = inject(Notifier);

  /** The form of the step selected, or null (none selected). */
  readonly form = signal<StepForm | null>(null);
  readonly work = computed<WorkStep | null>(() => this.form()?.work() ?? null);
  /** Where the form's step is now. */
  readonly place = computed<FormPlace | null>(() => {
    const form = this.form();
    const draft = this.authoring.draft();
    const found = form && draft ? locate(draft, form.uid) : null;
    if (!found) return null;
    const kind: FormKind = found.sub > -1 ? 'child' : isBlock(found.step) ? 'block' : 'step';
    return { index: found.index, sub: found.sub, kind, number: numberOf(found.index, found.sub) };
  });
  /** The step of the script the form is in: the step itself, or the block it is one of the steps of. */
  readonly top = computed<DraftStep | null>(() => {
    const place = this.place();
    const draft = this.authoring.draft();
    return place && draft ? (stepsOf(draft)[place.index] ?? null) : null;
  });
  /** "Wrong clicks, giving up" is opened by what puts something in it (Pass only once…). */
  readonly wrongClicksOpen = signal(false);
  /** A field to give the focus once the form is on the page (where it was on the page before a fresh game). */
  readonly focusDue = signal<FieldFocus | null>(null);
  /** The tab's element: where fields with the focus are looked for. */
  element: HTMLElement | null = null;

  private timer: ReturnType<typeof setTimeout> | null = null;
  /** Bumped to make the form afresh (Authoring's reset). */
  private readonly fresh = signal(0);
  /** What a page that went to a fresh game left for this one's form: its step, what was unsent, the focus. */
  private carried: { uid: string; form: CarriedForm | null; focus: FieldFocus | null } | null = null;

  private readonly transport: StepTransport = {
    merge: async (uid, set, unset) => {
      // (the page is on its way to a fresh game: what is unsent goes from there, carried)
      if (this.authoring.gone()) return new Promise<never>(() => undefined);
      return (await this.authoring.edit({ steps: [{ op: 'merge', uid, set, unset }] }))?.state ?? null;
    },
    problem: () => this.authoring.problem(),
    sayProblem: (text) => this.authoring.problem.set(text),
    focused: (field) => this.focused() === field,
    current: (form) => this.form() === form,
  };

  constructor() {
    const saved = this.store.read(STEP_CARRY_KEY) as {
      uid?: unknown;
      form?: unknown;
      focus?: FieldFocus | null;
    } | null;
    this.store.remove(STEP_CARRY_KEY);
    if (typeof saved?.uid === 'string') {
      this.carried = { uid: saved.uid, form: carriedOf(saved.form), focus: saved.focus ?? null };
    }
    const detach = this.authoring.attachStepTab({
      flush: () => this.flush(),
      engaged: () => this.typing(),
      reset: () => this.reset(),
      carry: () => this.carry(),
    });
    // The step selected, as the state now has it: the same step's form takes what changed elsewhere; another's is new
    effect(() => {
      const draft = this.authoring.draft();
      const { selected, sub } = this.authoring.ui();
      this.fresh();
      untracked(() => this.follow(draft, selected, sub));
    });
    inject(DestroyRef).onDestroy(() => {
      detach();
      this.stop();
      // (the tab is left: what has not been sent goes)
      void this.form()?.send();
    });
  }

  private follow(draft: DraftState | null, selected: number | null, sub: string | null): void {
    const steps = draft ? stepsOf(draft) : [];
    const top = selected === null ? undefined : steps[selected - 1];
    const form = this.form();
    if (!draft || selected === null || !top) {
      if (form) this.away(form);
      return;
    }
    const index = selected - 1;
    const kids = kidsOf(draft, index);
    const at = isBlock(top) && sub !== null ? kids.indexOf(sub) : -1;
    const uid = (at > -1 ? kids[at] : draft.uids[index]) ?? '';
    const step = at > -1 ? (top.anyOrder?.[at] ?? top) : top;
    if (form?.uid === uid) {
      form.update(step, draft.texts[uid]);
      return;
    }
    if (form) this.away(form);
    const next = new StepForm(uid, step, draft.texts[uid], { index, sub: at }, this.transport);
    this.wrongClicksOpen.set(false);
    this.form.set(next);
    const carried = this.carried;
    if (carried?.uid === uid) {
      this.carried = null;
      if (carried.form && next.restore(carried.form)) this.later();
      this.focusDue.set(carried.focus);
    }
  }

  /** Turned away from a form: what has not been sent of it still goes. */
  private away(form: StepForm): void {
    this.stop();
    this.form.set(null);
    void form.send();
  }

  /** Changes the value at a path of the working copy (it is sent as the field says: now, later, or with the next). */
  put(path: string, value: unknown): void {
    this.form()?.work.update((work) => withValue(work, path, value));
  }

  /** Sends what has changed a moment after the last change (typing in a message: its bubble shows it soon after). */
  later(): void {
    this.schedule(TYPING_SEND_MS);
  }

  /** Sends what has changed at once (a field left, a tick, a choice). */
  now(): void {
    this.schedule(0);
  }

  /** Sends now, and says whether it went (or there was nothing to send). */
  send(): Promise<boolean> {
    this.stop();
    return this.form()?.send() ?? Promise.resolve(true);
  }

  private schedule(ms: number): void {
    this.stop();
    if (!this.form()) return;
    this.timer = setTimeout(() => {
      this.timer = null;
      void this.form()?.send();
    }, ms);
  }

  private stop(): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
  }

  /** Sends the form before the panel goes elsewhere (StepTabHooks.flush). */
  private async flush(): Promise<StepFlushResult> {
    const form = this.form();
    if (!form) return { went: true };
    const number = this.place()?.number ?? String(form.index + 1);
    if (await this.send()) return { went: true };
    return {
      went: false,
      note: `Step ${number}'s last change is not in the file: the server would not take it (see the error), and it is still in the Step tab to be put right.`,
    };
  }

  /** The form is made afresh (StepTabHooks.reset): a change to the steps has been sent. */
  private reset(): void {
    this.stop();
    this.form.set(null);
    this.fresh.update((count) => count + 1);
  }

  /**
   * The page goes to a fresh game: what was typed since the send on its way, and where the focus is, are kept for
   * the next page's form.
   */
  private carry(): void {
    const form = this.form();
    if (!form) return;
    const active = this.activeField();
    const focus =
      active === null
        ? null
        : { field: active.dataset['field'] ?? '', start: active.selectionStart ?? 0, end: active.selectionEnd ?? 0 };
    this.store.write(STEP_CARRY_KEY, { uid: form.uid, form: form.carry(), focus });
  }

  /** The field of the form that has the focus, if one has. */
  private activeField(): HTMLInputElement | HTMLTextAreaElement | null {
    const active = this.document.activeElement;
    if (!(active instanceof HTMLInputElement || active instanceof HTMLTextAreaElement)) return null;
    return active.dataset['field'] !== undefined && this.element?.contains(active) ? active : null;
  }

  /** The path of the field that has the focus, or null. */
  focused(): string | null {
    return this.activeField()?.dataset['field'] ?? null;
  }

  /** Whether the author is typing in the tab (a text field there has the focus). */
  typing(): boolean {
    const active = this.document.activeElement;
    if (!active || !this.element?.contains(active)) return false;
    if (active instanceof HTMLTextAreaElement) return true;
    return active instanceof HTMLInputElement && ['text', 'number', 'search', ''].includes(active.type);
  }

  /** A message of the form's step has the focus: it is put on the screen (and again whenever its step is). */
  focusMessage(index: number): void {
    const at = this.messageAt(index);
    if (at) this.authoring.focusOn(at);
  }

  /** The message `index` of the form's step, as the guide knows it. */
  messageAt(index: number): MessageAt | null {
    const place = this.place();
    return place ? { step: place.index + 1, sub: place.sub > -1 ? place.sub + 1 : 0, index } : null;
  }

  /** Turns to a step of the script (from 1), as ◀ ▶ do. */
  select(number: number): void {
    const draft = this.authoring.draft();
    if (!draft || number < 1 || number > stepsOf(draft).length) return;
    this.authoring.remember({ selected: number, selectedUid: draft.uids[number - 1] ?? null, sub: null });
  }

  /** Opens one of the sections of a block's page: the block's own (-1), or one of its steps (from 0). */
  openPanel(sub: number): void {
    const place = this.place();
    const draft = this.authoring.draft();
    if (!place || !draft) return;
    this.authoring.remember({ sub: sub > -1 ? (kidsOf(draft, place.index)[sub] ?? null) : null });
  }

  /** The block's step whose section was last brought into sight (it is brought into sight once, when opened). */
  panelShown: string | null = null;

  /**
   * Puts a step into the block at `index` (from 0), before its step at `at` (from 0; past the last: last), and turns
   * to it. `awaiting`: it takes its answer from what is done when the game comes to it.
   */
  async insertInBlock(index: number, at: number, step: DraftStep, awaiting = false): Promise<void> {
    if (!(await this.send())) return;
    const draft = this.authoring.draft();
    if (!draft) return;
    this.authoring.showTab('step');
    await this.authoring.changeBlock([childInsertOp(draft, index, at, step, awaiting)], index, at);
  }

  /** Asks the author, in the page's own dialog. */
  ask(question: string, ok: string): Promise<boolean> {
    return askAuthor(this.notifier, question, ok);
  }
}
