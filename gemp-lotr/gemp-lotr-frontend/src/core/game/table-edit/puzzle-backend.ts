import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, defer, map, Observable, of, shareReplay, throwError } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { EditBoard, readEditBoard } from './edit-board';
import { EditOp } from './edit-ops';
import { emptySlots, PuzzleSlot } from './puzzle-slots';
import { TableEditBackend } from './table-edit-backend';

/** A card in a slot: its blueprint and its name. */
export interface SlotCard {
  blueprintId: string;
  name: string;
}

/** A slot as the puzzle page shows it: the slot, and the card in it now (null: empty). */
export interface FilledSlot {
  slot: PuzzleSlot;
  card: SlotCard | null;
}

/** The server's word on a card for a slot (`POST scenarios/picks`): whether it fits, why not, and its name. */
export interface SlotVerdict {
  fits: boolean;
  why: string | null;
  name: string | null;
}

/** Said when a puzzle is asked for something only a sandbox has. */
export const NOT_A_SANDBOX = 'A puzzle is changed only by filling its slots.';
/** Said when the server's answer about a pick is unreadable. */
export const NO_VERDICT = 'The server did not say whether that card fits.';

type Json = Record<string, unknown>;
const object = (value: unknown): Json =>
  typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as Json) : {};

/** The verdict on one slot in an answer of `scenarios/picks`. */
export function readVerdict(answer: unknown, slot: string): SlotVerdict | null {
  const one = object(object(object(answer)['picks'])[slot]);
  if (typeof one['fits'] !== 'boolean') return null;
  return {
    fits: one['fits'],
    why: typeof one['why'] === 'string' ? one['why'] : null,
    name: typeof one['name'] === 'string' ? one['name'] : null,
  };
}

/** A refusal in the server's words (TableEdit shows an ApiError's `said`). */
const refusal = (said: string) => new ApiError(400, said, { said });

/**
 * A puzzle's slots before Submit (ep-notes.md P3), the backend of TableEdit's `puzzle` level: the held game is never
 * changed. A card put in a slot (`{op: 'pick'}`) is checked with the server as it is made (`POST scenarios/picks`,
 * with the other slots' cards, for uniqueness) and held on the page; refused, it is not held, and the server's words
 * are the refusal. Submit (`POST scenarios/start` with the picks) starts a fresh game built with them, which the page
 * goes to (TableEdit.submit). The candidates the slot's chooser lists are checked the same way (`verdict`), each once.
 * A level without Stop, Start or Reset: those are refused. Page-scoped.
 */
@Injectable()
export class PuzzleBackend extends TableEditBackend {
  readonly level = 'puzzle' as const;
  private readonly http = inject(GempHttp);
  private readonly guide = inject(ScenarioGuide);

  /** The cards put in the slots on this page, by slot id, until Submit. */
  readonly picks = signal<Readonly<Record<string, SlotCard>>>({});
  /** The puzzle's slots, as its info says. */
  readonly slots = computed<readonly PuzzleSlot[]>(() => this.guide.info()?.slots ?? []);
  /** Each slot with its card now: the one put in it here while held, else the one the game was built with. */
  readonly filled = computed<readonly FilledSlot[]>(() => {
    const held = this.guide.info()?.mode === 'edit';
    const picks = this.picks();
    return this.slots().map((slot) => ({
      slot,
      card: held
        ? (picks[slot.id] ?? null)
        : slot.pick === null
          ? null
          : { blueprintId: slot.pick, name: slot.pickName ?? slot.pick },
    }));
  });
  /** The slots still empty, by what the page calls them. */
  readonly missing = computed(() => emptySlots(this.slots(), this.picks()));

  private readonly verdicts = new Map<string, Observable<SlotVerdict>>();

  state(): Observable<EditBoard> {
    return of(this.board());
  }

  apply(_gameId: string, ops: readonly EditOp[]): Observable<EditBoard> {
    const [op, ...more] = ops;
    if (op?.op !== 'pick' || more.length > 0) return throwError(() => refusal(NOT_A_SANDBOX));
    const { slot, blueprint } = op;
    if (blueprint === null) {
      this.picks.update((held) => Object.fromEntries(Object.entries(held).filter(([id]) => id !== slot)));
      return of(this.board());
    }
    const picks = { ...this.blueprints(), [slot]: blueprint };
    return this.ask(picks).pipe(
      map((answer) => {
        const verdict = readVerdict(answer, slot);
        if (!verdict) throw refusal(NO_VERDICT);
        if (!verdict.fits) throw refusal(verdict.why ?? NO_VERDICT);
        this.picks.update((held) => ({ ...held, [slot]: { blueprintId: blueprint, name: verdict.name ?? blueprint } }));
        return this.board();
      }),
    );
  }

  stop(): Observable<EditBoard> {
    return throwError(() => refusal(NOT_A_SANDBOX));
  }

  start(): Observable<EditBoard> {
    return throwError(() => refusal(NOT_A_SANDBOX));
  }

  reset(): Observable<string> {
    return throwError(() => refusal(NOT_A_SANDBOX));
  }

  /** Submit: a fresh game of the puzzle built with the picks (every slot filled); its id. */
  override submit(): Observable<string> {
    const id = this.guide.info()?.id;
    if (!id) return throwError(() => refusal(NOT_A_SANDBOX));
    return this.http
      .postJson<{ gameId?: unknown }>('/scenarios/start', { id, picks: JSON.stringify(this.blueprints()) })
      .pipe(map((answer) => String(answer.gameId)));
  }

  /**
   * The server's word on a card for a slot, beside the other slots' cards (uniqueness counts them): asked once for
   * each card, slot and set of other cards.
   */
  verdict(slot: string, blueprint: string): Observable<SlotVerdict> {
    const others = Object.fromEntries(Object.entries(this.blueprints()).filter(([id]) => id !== slot));
    const picks = { ...others, [slot]: blueprint };
    const key = JSON.stringify([slot, blueprint, others]);
    let known = this.verdicts.get(key);
    if (!known) {
      known = defer(() => this.ask(picks)).pipe(
        map((answer) => readVerdict(answer, slot) ?? { fits: false, why: NO_VERDICT, name: null }),
        catchError((error: unknown) => {
          // (not known after all: asked again next time)
          this.verdicts.delete(key);
          return of({ fits: false, why: ApiError.from(error).said ?? NO_VERDICT, name: null });
        }),
        shareReplay(1),
      );
      this.verdicts.set(key, known);
    }
    return known;
  }

  /** The picks as the server takes them: slot id → blueprint. */
  private blueprints(): Record<string, string> {
    return Object.fromEntries(Object.entries(this.picks()).map(([slot, card]) => [slot, card.blueprintId]));
  }

  private ask(picks: Readonly<Record<string, string>>): Observable<unknown> {
    const id = this.guide.info()?.id ?? '';
    return this.http.postJson<unknown>('/scenarios/picks', { id, picks: JSON.stringify(picks) });
  }

  /** The table as this level knows it: nothing but its mode (the slots are this backend's own). */
  private board(): EditBoard {
    return readEditBoard({ mode: this.guide.info()?.mode ?? 'play', level: 'puzzle' });
  }
}
