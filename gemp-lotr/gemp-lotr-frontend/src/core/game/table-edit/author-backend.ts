import { computed, inject, Injectable } from '@angular/core';
import { firstValueFrom, from, Observable, of, throwError } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { AuthoringApi } from '../authoring/authoring-api.service';
import { FoundCard } from '../authoring/authoring-state';
import { Authoring } from '../authoring/authoring.service';
import { BoardChanges, WHERE_REFUSED } from '../authoring/board-changes.service';
import { boardAliases, suggestAlias } from '../authoring/board-queue';
import { addSlotPatch } from '../authoring/puzzle-slots';
import { GameSession } from '../session/game-session.service';
import { AUTHOR_TOOLS, authorBoard, setupCount } from './author-board';
import { AuthorStep, authorSteps } from './author-ops';
import { EditBoard } from './edit-board';
import { EditOp, EditPlace } from './edit-ops';
import { EditSeating } from './edit-seating.service';
import { FilledSlot } from './puzzle-backend';
import { readPuzzleSlots } from './puzzle-slots';
import { BackendClaim, TableEditBackend } from './table-edit-backend';
import { NOT_DONE } from './table-edit.service';

/** Said for what an author's table has no such thing as. */
export const NOT_AN_AUTHORS = 'The author’s table has no Stop, Start or Reset: the panel says how the game goes.';
/** The places a slot can be opened at from the table, as a slot's zone says them (a deck's top is its 1st place). */
const SLOT_ZONES: Readonly<Partial<Record<EditPlace['zone'], string>>> = {
  characters: 'characters',
  support: 'support',
  minions: 'minions',
  hand: 'hand',
  'deck.top': 'deck',
  discard: 'discard',
  deadPile: 'deadPile',
};

const refusal = (said: string) => new ApiError(400, said, { said });

/**
 * The author's table (level `author`): while a draft is written at the table, the on-table edit controls change the
 * board the way the authoring panel's Board tab does, through the same service (BoardChanges): each change is said as
 * the tab says it (author-ops.ts), gathered and sent at once, to the board the tab's "Changes are made" says (as it
 * stands: the step's `do`; or the one the scenario starts with: the setup, and a fresh game the page goes to). The
 * controls are on while the panel's Edit table is (`editing`, remembered across fresh games); off, the game is played.
 * The table is the authoring state's board (author-board.ts). Changes gathered in the tab and not sent are the tab's:
 * nothing is sent from the table while there are any. Page-scoped.
 */
@Injectable()
export class AuthorBackend extends TableEditBackend {
  readonly level = 'author' as const;
  private readonly authoring = inject(Authoring);
  private readonly changes = inject(BoardChanges);
  private readonly api = inject(AuthoringApi);
  private readonly session = inject(GameSession);
  private readonly seating = inject(EditSeating);

  /** A draft is being written at the table on this page, the panel docked beside it. */
  readonly writing = computed(() => this.authoring.writing() && this.authoring.view() === 'author');
  /** The table's controls are on (Edit table). */
  readonly editing = computed(() => this.writing() && this.authoring.ui().editTable === true);
  /** Changes go to the board the scenario starts with (the Board tab's choice, shared). */
  readonly inSetup = this.changes.inSetup;

  override readonly claim = computed<BackendClaim | null>(() =>
    this.writing() ? { mode: this.editing() ? 'edit' : 'play', tools: AUTHOR_TOOLS } : null,
  );

  override readonly liveBoard = computed<EditBoard | null>(() => {
    const state = this.authoring.draft();
    if (!state || !this.writing()) return null;
    const model = this.session.model();
    return authorBoard({
      state,
      inSetup: this.inSetup(),
      mode: this.editing() ? 'edit' : 'play',
      tools: AUTHOR_TOOLS,
      cards: model.cards,
      zones: model.stats?.playerZones ?? {},
      seats: this.seating.seats(),
    });
  });

  /** A puzzle's draft: a slot can be opened on the table. */
  readonly slotted = computed(() => this.writing() && this.authoring.draft()?.draft.kind === 'puzzle');

  /** Edit table: the controls on, or off (the game played). */
  toggle(on = !this.editing()): void {
    this.authoring.remember({ editTable: on });
  }

  /**
   * Where changes are made (the Board tab's choice: one state, its radio and the table's chip): refused while changes
   * gathered there wait (the tab says so).
   */
  setWhere(where: 'now' | 'setup'): boolean {
    return this.changes.setWhere(where);
  }

  state(): Observable<EditBoard> {
    const board = this.liveBoard();
    return board ? of(board) : throwError(() => refusal(NOT_DONE));
  }

  apply(_gameId: string, ops: readonly EditOp[]): Observable<EditBoard> {
    return from(this.make(ops));
  }

  stop(): Observable<EditBoard> {
    return throwError(() => refusal(NOT_AN_AUTHORS));
  }

  start(): Observable<EditBoard> {
    return throwError(() => refusal(NOT_AN_AUTHORS));
  }

  reset(): Observable<string> {
    return throwError(() => refusal(NOT_AN_AUTHORS));
  }

  /**
   * Makes the edits as the Board tab would: said as its changes, gathered, and submitted. Resolves to the board as it
   * now is (a change that brought a fresh game: as this page last knew it, the page being on its way to that game); a
   * refusal (the tab's words, or the server's) throws.
   */
  private async make(ops: readonly EditOp[]): Promise<EditBoard> {
    const state = this.authoring.draft();
    const board = this.liveBoard();
    if (!state || !board) throw refusal(NOT_DONE);
    if (this.changes.count() > 0) throw refusal(WHERE_REFUSED);
    const made = authorSteps(
      {
        board,
        inSetup: this.inSetup(),
        realOpening: this.changes.opening() === 'game',
        sites: state.board?.sites ?? { freeps: [], shadow: [] },
        human: state.draft.human ?? 'freeps',
        markersTogether: this.changes.start()?.markersTogether === true,
        setupTokens: (alias) => setupCount(state, 'tokens', alias),
      },
      ops,
    );
    if ('refused' in made) throw refusal(made.refused);
    this.authoring.dismissProblem();
    for (const step of made.steps) {
      if (step.kind === 'site') return this.site(step, board);
      if (!(await this.gather(step))) return this.refused();
    }
    if (this.changes.count() === 0) return board;
    await this.changes.submit();
    return this.authoring.problem() !== null ? this.refused() : (this.liveBoard() ?? board);
  }

  /** Gathers one step as the tab would; false when the tab would not. */
  private async gather(step: Exclude<AuthorStep, { kind: 'site' }>): Promise<boolean> {
    switch (step.kind) {
      case 'queue':
        this.changes.queue(step.name, step.args);
        return true;
      case 'drop':
        this.changes.drop({ alias: step.alias, added: false });
        return true;
      case 'path':
        this.changes.setPathSite(step.number, step.alias);
        return true;
      case 'add': {
        const found = await this.found(step);
        const alias = suggestAlias(found.name, this.taken());
        if (!(await this.changes.add(found, alias, step.seat, step.zone, step.host))) return false;
        if (step.then !== null) this.changes.queue('move', [alias, step.then]);
        return true;
      }
    }
  }

  /** A site of a seat's path put in: at once, as the tab's path does it. */
  private async site(step: Extract<AuthorStep, { kind: 'site' }>, board: EditBoard): Promise<EditBoard> {
    await this.changes.changeSite(step.seat, step.at, step.blueprint);
    return this.authoring.problem() !== null ? this.refused() : (this.liveBoard() ?? board);
  }

  /** What was gathered from the table is let go of, and the refusal said (by the control, and by the panel). */
  private refused(): never {
    const said = this.authoring.problem() ?? NOT_DONE;
    this.changes.reset();
    throw refusal(said);
  }

  /** The card of a blueprint as the tab's search finds it (a copy's is known). */
  private async found(step: Extract<AuthorStep, { kind: 'add' }>): Promise<FoundCard> {
    const known = (name: string, type: string): FoundCard => ({
      id: step.blueprint,
      name,
      type,
      culture: null,
      side: null,
      cost: null,
    });
    if (step.name !== null) return known(step.name, step.type ?? '');
    try {
      const answer = await firstValueFrom(this.api.cards(step.blueprint));
      return answer.cards.find((card) => card.id === step.blueprint) ?? known(step.blueprint, '');
    } catch {
      return known(step.blueprint, '');
    }
  }

  /** The names taken: the scenario's cards and sites, and its slots. */
  private taken(): string[] {
    const state = this.authoring.draft();
    const board = state?.board;
    if (!state || !board) return [];
    const gathered = this.changes.gathered();
    const cards = gathered ? boardAliases(board, gathered) : board.cards.map((card) => card.alias);
    return [...cards, ...Object.keys(state.draft.slots ?? {})];
  }

  /* ---------------------------------------------------------------- a puzzle's slots */

  /**
   * Opens a slot at a place of the table (a puzzle's draft): a new slot there, named slot1, slot2... (renamed in the
   * Board tab), and the Board tab shows its form once the fresh game it brings is in. Resolves to the refusal's words,
   * or null.
   */
  async openSlot(place: EditPlace): Promise<string | null> {
    const state = this.authoring.draft();
    const zone = SLOT_ZONES[place.zone];
    if (!state || !this.slotted()) return NOT_DONE;
    if (zone === undefined) return 'A slot cannot be there.';
    const taken = this.taken();
    let n = 1;
    while (taken.includes(`slot${n}`)) n++;
    const id = `slot${n}`;
    const human = state.draft.human ?? 'freeps';
    this.authoring.remember({ tab: 'board', showSlot: id });
    const done = await this.authoring.edit(
      addSlotPatch(state.draft, id, { zone, ...(place.seat !== human ? { seat: place.seat } : {}) }, null),
    );
    if (done === null) {
      this.authoring.remember({ showSlot: null });
      return this.authoring.problem() ?? NOT_DONE;
    }
    return null;
  }

  /** Shows a slot's form in the Board tab. */
  showSlot(id: string): void {
    this.authoring.remember({ showSlot: id });
    this.authoring.showTab('board');
  }

  /** The draft's slots that the game in hand has no card in: drawn on the table as open slots while edited. */
  readonly emptySlots = computed<readonly FilledSlot[]>(() => {
    const state = this.authoring.draft();
    if (!state || !this.editing() || !this.slotted()) return [];
    return readPuzzleSlots(state.puzzle?.slots ?? [])
      .filter((slot) => slot.pick === null)
      .map((slot) => ({ slot, card: null }));
  });
}
