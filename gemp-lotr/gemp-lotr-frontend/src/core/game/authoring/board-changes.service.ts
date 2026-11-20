import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { Notifier } from '../../notifier/notifier';
import { AuthoringApi } from './authoring-api.service';
import { BoardSite, DraftSeat, DraftStart, FoundCard } from './authoring-state';
import { GatheredBoard, StartChange } from './authoring-ui-store';
import { Authoring } from './authoring.service';
import {
  fightCards,
  gatherControl,
  gatherFight,
  gatherPathSite,
  gatherStart,
  setupWithoutSite,
  startOf,
  StartPart,
  startPartOf,
  startParts,
  startView,
  undoStart,
  withoutStart,
} from './board-start';
import {
  BOARD_SEATS,
  addAliasProblem,
  addCard,
  bearersOf,
  BoardArg,
  boardAliases,
  boardType,
  boardView,
  BoardView,
  cardChange,
  changeCardTop,
  dropCard,
  existingCard,
  gather,
  gatheredCount,
  gatheredOf,
  keepCard,
  opText,
  pathIds,
  pruneGathered,
  renameGathered,
  replaceQuestion,
  ringChoices,
  ringTop,
  samePaths,
  sitesReordered,
  sitesTop,
  undoAdded,
  undoOp,
  undoRemoved,
  ViewCard,
} from './board-queue';

/** Said when where changes are made is changed with changes waiting (they are for the other board). */
export const WHERE_REFUSED = 'Board: submit or reset the changes waiting there first.';
/** Before what the server said, when a change to the board as it stands was refused part-way. */
export const PART_MADE = 'Some changes to the board were made, and the rest were not: ';

/**
 * What the Board tab does (the legacy scenarioAuthor.js gather, submitBoard, resetBoard, changeCard, putSites,
 * ringRow, renameCard, addCard, dropCard): the changes it gathers, kept with what the panel remembers (so that they
 * outlast the page being loaded again for a fresh game) for the draft they were gathered in; the board as it will be
 * once they are sent, and sending them. The cards' versions, the paths, the Ring and names are changed at once.
 * Provided by the Board tab, for the tab and its parts.
 */
@Injectable()
export class BoardChanges {
  private readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);
  private readonly notifier = inject(Notifier);

  /** The table as the scenario names it; null until it has been laid out. */
  readonly board = computed(() => this.authoring.draft()?.board ?? null);
  /** The Free Peoples take every turn (rules.oneSided). */
  readonly oneSided = computed(() => this.authoring.draft()?.draft.rules?.oneSided === true);
  /** Changes go to the board the scenario starts with (else to the board as it stands). */
  readonly inSetup = computed(() => this.authoring.ui().where === 'setup');
  /**
   * What has been gathered, without what is about cards the scenario no longer has (the legacy panel let go of
   * those as it drew the tab; each change made here starts from this).
   */
  readonly gathered = computed<GatheredBoard | null>(() => {
    const draft = this.authoring.draft();
    const board = draft?.board;
    if (!draft || !board) return null;
    return pruneGathered(gatheredOf(this.authoring.ui().board, draft.draft.id), board);
  });
  readonly count = computed(() => {
    const gathered = this.gathered();
    return gathered ? gatheredCount(gathered) : 0;
  });
  /** The board as it will be once what has been gathered is sent. */
  readonly view = computed<BoardView | null>(() => {
    const board = this.board();
    const gathered = this.gathered();
    return board && gathered ? boardView(board, gathered) : null;
  });
  /** What another card can be put on. */
  readonly bearers = computed(() => {
    const view = this.view();
    return view ? bearersOf(view) : [];
  });
  /** The names the scenario's cards and sites have (as they will be). */
  readonly aliases = computed(() => {
    const board = this.board();
    const gathered = this.gathered();
    return board && gathered ? boardAliases(board, gathered) : [];
  });
  /** The seats' paths are edited apart (remembered once the author says; until then, apart if they differ). */
  readonly sitesApart = computed(() => {
    const board = this.board();
    return this.authoring.ui().sitesApart ?? (board !== null && !samePaths(board));
  });

  /** Changes what has been gathered, and remembers it. */
  private change(how: (gathered: GatheredBoard) => GatheredBoard): void {
    const gathered = this.gathered();
    if (gathered) this.authoring.remember({ board: how(gathered) });
  }

  /**
   * Gathers a change to the board ("wound", ["frodo", 1]); see gather. For the board the scenario starts with it is
   * not compared with the board in hand (which the script may have got further than the setup), and a part of where
   * the story starts that it says the same as is no longer gathered.
   */
  queue(name: string, args: readonly BoardArg[]): void {
    const inSetup = this.inSetup();
    const part = inSetup ? startPartOf(name, args) : null;
    this.change((gathered) => {
      const made = gather(gathered, inSetup ? null : this.board(), name, args);
      return part === null ? made : undoStart(made, part);
    });
  }

  /* ---------------------------------------------------------------- where the story starts */

  /** Where the draft's story starts (as the server says). */
  readonly start = computed(() => {
    const state = this.authoring.draft();
    return state ? startOf(state) : null;
  });
  /** Where the story starts as it will be once what has been gathered is sent. */
  readonly startView = computed(() => {
    const start = this.start();
    const gathered = this.gathered();
    return start && gathered ? startView(start, gathered, this.inSetup(), this.board()) : null;
  });
  /** What a skirmish at the start can have (the setup's characters and minions, as they will be). */
  readonly fightCards = computed(() => {
    const draft = this.authoring.draft()?.draft;
    const gathered = this.gathered();
    return draft && gathered ? fightCards(draft, gathered) : { characters: [], minions: [] };
  });
  /** What the server said when the last submit with a change to where the story starts was refused. */
  readonly startRefused = signal<string | null>(null);

  private changeStart(how: (gathered: GatheredBoard, start: DraftStart) => GatheredBoard) {
    const start = this.start();
    if (!start) return;
    this.startRefused.set(null);
    this.change((gathered) => how(gathered, start));
  }

  /** Gathers a part of where the story starts (see gatherStart). */
  setStart<K extends StartPart>(part: K, value: StartChange[K]): void {
    this.changeStart((gathered, start) => gatherStart(gathered, start, this.board(), part, value, this.inSetup()));
  }

  /** A site ticked (or not) as a seat's at the start. */
  setControl(seat: DraftSeat, number: number, on: boolean): void {
    this.changeStart((gathered, start) =>
      gatherControl(gathered, start, this.board(), seat, number, on, this.inSetup()),
    );
  }

  /** A minion put in a character's skirmish at the start, or taken out of it. */
  setFight(character: string, minion: string, on: boolean): void {
    this.changeStart((gathered, start) => gatherFight(gathered, start, this.board(), character, minion, on));
  }

  /** The site laid at a place on the path ("" the Free Peoples seat's own). */
  setPathSite(number: number, alias: string): void {
    this.changeStart((gathered, start) => gatherPathSite(gathered, start, this.board(), number, alias));
  }

  undoStart(part: StartPart): void {
    this.change((gathered) => undoStart(gathered, part));
  }

  undoRemoved(index: number): void {
    this.change((gathered) => undoRemoved(gathered, index));
  }

  undoAdded(index: number): void {
    this.change((gathered) => undoAdded(gathered, index));
  }

  undoOp(index: number): void {
    this.change((gathered) => undoOp(gathered, index));
  }

  /** "Keep it": a card gathered to be taken out stays. */
  keep(alias: string): void {
    this.change((gathered) => keepCard(gathered, alias));
  }

  /** A card's ×. */
  drop(card: Pick<ViewCard, 'alias' | 'added'>): void {
    this.change((gathered) => dropCard(gathered, card));
  }

  /** Lets go of what has been gathered: the board is as the scenario has it. */
  reset(): void {
    this.authoring.remember({ board: null });
  }

  /**
   * Where changes are made. What has been gathered is for one of the two boards: it is sent, or let go of, before
   * the other is chosen (false, and the author is told, if it hasn't been). Where the story starts is the setup's
   * whichever is chosen, and stays gathered.
   */
  setWhere(where: 'now' | 'setup'): boolean {
    const gathered = this.gathered();
    if (gathered && gatheredCount(gathered) > startParts(gathered).length) {
      this.authoring.problem.set(WHERE_REFUSED);
      return false;
    }
    this.authoring.remember({ where });
    return true;
  }

  /**
   * Sends what has been gathered, all at once: the cards put in and taken out first, as one change to the scenario
   * (which brings a fresh game), then the changes to the board as one request, which the server makes in turn (in the
   * setup, with one fresh game for them all). A fresh game either brings is gone to once both have been sent, with
   * what went wrong carried to its page. A refused change to the board as it stands has had the changes before it
   * made, and the rest not: none is sent again. A refused change to the setup has had none made: they stay to be put
   * right. Refused cards stay, and nothing more is sent.
   */
  async submit(): Promise<void> {
    const draft = this.authoring.draft();
    const gathered = this.gathered();
    if (!draft || !gathered || gatheredCount(gathered) === 0) return;
    const inSetup = this.inSetup();
    const starting = startParts(gathered).length > 0;
    const change = cardChange(draft.draft, gathered, this.start());
    const ops = gathered.ops.map(opText);
    if (!(await this.authoring.flush()).went) return;
    // (a fresh game that the cards bring is gone to once the changes to the board have been made to it)
    this.authoring.holdArrivals();
    if (change !== null) {
      if (!(await this.authoring.edit({ top: { ...change } }))) {
        // (said where the start is changed too, what was typed there staying gathered)
        if (starting) this.startRefused.set(this.authoring.problem());
        this.authoring.releaseArrivals();
        return;
      }
      this.startRefused.set(null);
      this.authoring.remember({ board: { ...withoutStart(this.stored(draft.draft.id)), added: [], removed: [] } });
    }
    if (ops.length === 0) {
      this.authoring.releaseArrivals();
      return;
    }
    if (await this.authoring.send(this.api.apply(ops, inSetup))) {
      this.authoring.remember({ board: { ...this.stored(draft.draft.id), ops: [] } });
      this.authoring.releaseArrivals();
      return;
    }
    if (!inSetup) {
      this.authoring.remember({ board: { ...this.stored(draft.draft.id), ops: [] } });
      this.authoring.problem.set(PART_MADE + (this.authoring.problem() ?? ''));
    }
    this.authoring.releaseArrivals(this.authoring.problem());
  }

  /** What is gathered as remembered (not pruned: the board in hand may be the one before the change sent). */
  private stored(draft: string): GatheredBoard {
    return gatheredOf(this.authoring.ui().board, draft);
  }

  /**
   * Gives a card (or a site) another name: what has been gathered about it goes on being about it (and back, if the
   * server refuses). The server says the new name wherever the scenario said the old one, in a fresh game.
   * Resolves whether it did.
   */
  async rename(from: string, to: string): Promise<boolean> {
    const draft = this.authoring.draft();
    if (!draft) return false;
    if (!(await this.authoring.flush()).went) return false;
    const swap = (one: string, other: string) =>
      this.authoring.remember({ board: renameGathered(this.stored(draft.draft.id), one, other) });
    swap(from, to);
    const done = await this.authoring.send(this.api.alias(from, to));
    if (!done) swap(to, from);
    return done !== null;
  }

  /** One of the scenario's cards becomes another (by its blueprint), keeping its name and its place: at once. */
  async changeCard(card: Pick<ViewCard, 'owner' | 'alias'>, blueprintId: string): Promise<void> {
    const draft = this.authoring.draft();
    const top = draft ? changeCardTop(draft.draft, card, blueprintId) : null;
    if (top) await this.authoring.edit({ top });
  }

  /** The seats' paths (each a list of blueprint ids): at once. */
  async putSites(freeps: readonly string[], shadow: readonly string[]): Promise<void> {
    const draft = this.authoring.draft();
    if (!draft) return;
    await this.authoring.edit({ top: sitesTop(draft.board, draft.draft.human ?? 'freeps', freeps, shadow) });
  }

  /**
   * A site of a seat's path changed (`id`), or taken off (null); with the paths the same, both seats'. A site taken
   * off is taken out of the setup's sites controlled and its path too (the draft would not be read otherwise).
   */
  async changeSite(seat: DraftSeat, at: number, id: string | null): Promise<void> {
    const draft = this.authoring.draft();
    const board = this.board();
    if (!draft || !board) return;
    const now = pathIds(board, seat);
    if (id === null) now.splice(at, 1);
    else now[at] = id;
    const freeps = seat === 'freeps' || !this.sitesApart() ? now : pathIds(board, 'freeps');
    const shadow = seat === 'shadow' || !this.sitesApart() ? now : pathIds(board, 'shadow');
    const top: Record<string, unknown> = sitesTop(board, draft.draft.human ?? 'freeps', freeps, shadow);
    if (id === null) {
      const gone = BOARD_SEATS.filter((one) => one === seat || !this.sitesApart())
        .map((one) => board.sites[one][at]?.alias)
        .filter((alias): alias is string => !!alias);
      let setup = draft.draft.setup ?? null;
      for (const alias of gone) setup = setupWithoutSite(setup, alias);
      if (JSON.stringify(setup) !== JSON.stringify(draft.draft.setup ?? null)) top['setup'] = setup;
    }
    await this.authoring.edit({ top });
  }

  /**
   * A site of a seat's path put before the one above it (with the paths the same, both seats'), keeping its name: on
   * a path whose sites have no numbers, the first listed is the one laid first.
   */
  async raiseSite(seat: DraftSeat, at: number): Promise<void> {
    const draft = this.authoring.draft();
    const board = this.board();
    if (!draft || !board || at < 1) return;
    const seats = this.sitesApart() ? [seat] : BOARD_SEATS;
    await this.authoring.edit({ top: sitesReordered(board, draft.draft.human ?? 'freeps', seats, at, at - 1) });
  }

  /* ---------------------------------------------------------------- sites without numbers */

  /** The number printed on a site, by its blueprint (0: none), as far as it has been looked up. */
  private readonly printed = signal<ReadonlyMap<string, number>>(new Map());
  private readonly asked = new Set<string>();

  /**
   * Whether a seat's path is one whose sites have no numbers (the Shadows block on): as the game decides it, by the
   * first site of the path. Looked up once per site (GET cards).
   */
  unnumbered(seat: DraftSeat): boolean {
    const first = this.path(seat)[0]?.id;
    return first !== undefined && this.printed().get(first) === 0;
  }

  private readonly lookUp = effect(() => {
    for (const seat of BOARD_SEATS) {
      const first = this.board()?.sites[seat][0]?.id;
      if (first === undefined || this.asked.has(first)) continue;
      this.asked.add(first);
      void this.authoring.send(this.api.cards(first, true), false).then((found) => {
        const card = found?.cards.find((one) => one.id === first);
        if (card?.site !== undefined) this.printed.update((known) => new Map([...known, [first, card.site ?? 0]]));
      });
    }
  });

  /** "The same sites for both seats": ticked with the paths apart, the Free Peoples' is the Shadow's too. */
  async samePath(same: boolean): Promise<void> {
    this.authoring.remember({ sitesApart: !same });
    const board = this.board();
    if (same && board && !samePaths(board)) {
      const freeps = pathIds(board, 'freeps');
      await this.putSites(freeps, freeps);
    }
  }

  /** A seat's Ring-bearer or Ring (`""`: none): at once. */
  async setRing(part: 'ringBearer' | 'ring', seat: DraftSeat, value: string): Promise<void> {
    const draft = this.authoring.draft();
    if (draft) await this.authoring.edit({ top: ringTop(draft.draft, part, seat, value) });
  }

  /**
   * Gathers a card found to be added under a name, in a seat and a place ("" the deck; "attach" or "stack" on
   * `host`). A name the rule refuses, or a site's, is said; a name a card has asks whether the new one replaces it.
   * Resolves whether it was gathered.
   */
  async add(found: FoundCard, alias: string, seat: DraftSeat, zone: string, host: string | null): Promise<boolean> {
    const view = this.view();
    if (!view) return false;
    const problem = addAliasProblem(alias, this.board());
    if (problem !== null) {
      this.authoring.problem.set(problem);
      return false;
    }
    const existing = existingCard(view, alias);
    if (
      existing !== null &&
      !(await this.notifier.confirm(replaceQuestion(existing, found), {
        title: `Replace ${alias}?`,
        ok: `Replace ${alias}`,
        cancel: 'Cancel',
      }))
    ) {
      return false;
    }
    const onCard = zone === 'attach' || zone === 'stack';
    this.change((gathered) =>
      addCard(gathered, {
        alias,
        blueprintId: found.id,
        name: found.name,
        type: boardType(found.type),
        seat,
        zone,
        host: onCard ? host : null,
      }),
    );
    return true;
  }

  /** A seat's cards of a type the board has, as the Ring selects offer them ([alias, "alias (name)"]). */
  ringChoices(seat: DraftSeat, type: string): [string, string][] {
    const board = this.board();
    return board ? ringChoices(board, seat, type) : [];
  }

  /** A seat's Ring-bearer or Ring ("" for none). */
  ringOf(part: 'ringBearer' | 'ring', seat: DraftSeat): string {
    return this.authoring.draft()?.draft[part]?.[seat] ?? '';
  }

  /** A seat's path as the tab shows it. */
  path(seat: DraftSeat): readonly BoardSite[] {
    return this.board()?.sites[seat] ?? [];
  }
}
