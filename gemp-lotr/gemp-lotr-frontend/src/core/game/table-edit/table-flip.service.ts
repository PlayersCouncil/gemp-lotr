import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { DecisionGate, DecisionGates } from '../decision/decision-gate';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { GameUndo } from '../undo/game-undo.service';
import { EditSeat } from './edit-ops';
import { answersHere, flipTitle, isFlipped, lookedFrom, lookingText, OtherSeat, waitingElsewhere } from './table-seat';
import { NOT_DONE, PROBLEM_MS } from './table-edit.service';

/** The flip's call (POST scenarios/sandbox/flip; an author's table takes it too). */
export const FLIP_PATH = '/scenarios/sandbox/flip';
/** Who plays the other seat (POST scenarios/sandbox/otherSeat). */
export const OTHER_SEAT_PATH = '/scenarios/sandbox/otherSeat';

/**
 * Flip table, and who plays the other seat, for one game page (ef-notes Part 2). What they are comes from the
 * scenario's info event (sent again whenever the seat looked from, the other seat's player or the seat waited on
 * changes); this asks the server to change them.
 *
 * - **The flip** (`flip`): the server gives this page up and makes it the other seat's player's page; the page hears
 *   it through the game's notices (a resync with reason `flip`, GameUndo) and reads the game again from nothing, as
 *   after a take-back, and from the game's first event finds itself that player. Nothing more is done here.
 * - **The other seat** (`setOtherSeat`): the script, Gemp's bot, or the person. The game's events are asked for at
 *   once after it (a decision may have been answered by the new player) and undo's list (it is a restore point).
 *
 * It is one of the page's decision gates: flipped onto a seat that plays itself nothing is answered (the page does
 * not show the decision either: decisionView), as the server would refuse it. Page-scoped.
 */
@Injectable()
export class TableFlip implements DecisionGate {
  private readonly guide = inject(ScenarioGuide);
  private readonly session = inject(GameSession);
  private readonly undo = inject(GameUndo);
  private readonly lifetime = inject(PageLifetime);
  private readonly http = inject(GempHttp);

  private readonly info = computed(() => (this.guide.playing() ? this.guide.info() : null));
  private readonly over = computed(() => this.guide.end() !== null || this.session.model().ended);

  /** The seat the page shows the table from. */
  readonly seat = computed<EditSeat>(() => lookedFrom(this.info()));
  /** The page looks from the seat the person does not sit in. */
  readonly flipped = computed(() => isFlipped(this.info()));
  /** The table can be flipped (a sandbox, or an author at the table). */
  readonly canFlip = computed(() => this.info()?.canFlip === true);
  /** Who plays the other seat now (null: not a sandbox). */
  readonly otherSeat = computed<OtherSeat | null>(() => this.info()?.otherSeat ?? null);
  /** The choices on offer: only a sandbox's, and only while its game goes on. */
  readonly otherSeats = computed<readonly OtherSeat[]>(() =>
    this.info()?.level === 'sandbox' && !this.over() ? (this.info()?.otherSeats ?? []) : [],
  );
  /** What the page is asked is the person's to answer. */
  readonly answers = computed(() => answersHere(this.info()));
  /** The seat the game waits on that this page does not show, while the person plays it (null: none). */
  readonly elsewhere = computed(() => (this.over() ? null : waitingElsewhere(this.info())));
  /** The seat the person sits in. */
  readonly humanSeat = computed<EditSeat>(() => this.info()?.humanSeat ?? 'freeps');
  /** The seat looked from in words, while it is the other one ("As Shadow"); null unflipped. */
  readonly looking = computed(() => lookingText(this.info()));
  /** Where Flip turns the table to ("Look from Shadow's side"). */
  readonly flipTitle = computed(() => flipTitle(this.info()));
  /** A call is on its way. */
  readonly busy = signal(false);
  /** The last refusal, in the server's words. */
  readonly problem = signal<string | null>(null);

  constructor() {
    inject(DecisionGates).add(this);
  }

  allows(): boolean {
    return this.answers();
  }

  /** Shows the table from the other seat (or back); resolves to whether the server did it. */
  flip(): Promise<boolean> {
    if (!this.canFlip()) return Promise.resolve(false);
    return this.call(FLIP_PATH, {}, () => undefined);
  }

  /** Gives the other seat to the script, the bot or the person. */
  setOtherSeat(to: OtherSeat): Promise<boolean> {
    if (to === this.otherSeat() || !this.otherSeats().includes(to)) return Promise.resolve(false);
    return this.call(OTHER_SEAT_PATH, { to }, () => {
      // (a decision of the other seat's may have been answered at once: the game's news, and the new restore point)
      this.session.refresh();
      this.undo.refresh();
    });
  }

  private call(path: string, form: Record<string, string>, done: () => void): Promise<boolean> {
    const gameId = this.session.openGame;
    if (!gameId || this.busy()) return Promise.resolve(false);
    this.busy.set(true);
    this.problem.set(null);
    const asked: Observable<unknown> = this.http.postJson<unknown>(path, { gameId, ...form });
    return new Promise<boolean>((resolve) => {
      this.lifetime.write(asked, {
        next: () => {
          this.busy.set(false);
          done();
          resolve(true);
        },
        error: (error: unknown) => {
          this.busy.set(false);
          const text = ApiError.from(error).said ?? NOT_DONE;
          this.problem.set(text);
          this.lifetime.later(() => {
            if (this.problem() === text) this.problem.set(null);
          }, PROBLEM_MS);
          resolve(false);
        },
      });
    });
  }
}
