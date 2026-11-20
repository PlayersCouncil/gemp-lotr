import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { IntervalTimer } from '../../platform/interval-timer';
import { LegacyCookies } from '../../platform/legacy-cookies';
import { PageNavigation } from '../../platform/page-navigation';
import { SoundPlayer } from '../../platform/sound-player';
import { Timers } from '../../platform/timers';
import { PlayerInfo } from '../../session/player-info.service';
import { SessionService } from '../../session/session.service';
import { HallComms } from '../hall-comms.service';
import { HallUpdate } from '../hall-models';
import { HallSession } from '../hall-session.service';
import { defaultDeck, deckMemoryCookie, hasDeck, readDeckMemory, recallDeck, rememberDeck } from './hall-deck-memory';
import { bareName, FALLBACK_TIMERS, formatTableAge, gameUrl, sameName } from './hall-table-parsers';
import {
  BotTableForm,
  CasualTableForm,
  HallDeckChoice,
  HallDeckOption,
  HallFormatOption,
  HallTable,
  HallTimer,
  ReadyGame,
  TableCallResult,
  TableFlow,
  TableResult,
} from './hall-table-models';
import {
  CANNOT_INVITE_SELF,
  formatsFailed,
  MUST_NAME_INVITEE,
  MUST_SELECT_DECK,
  MUST_SELECT_FORMAT,
  MUST_SELECT_OWN_DECK,
  RESULT_READY,
  RESULT_WORKING,
  statusMessage,
  TABLE_CREATED_KEPT_OPEN,
} from './hall-tables-messages';
import { HallTablesApi } from './hall-tables-api.service';
import { HallTablesState } from './hall-tables-state';

/** The waiting tables' timers tick once a second (GempLotrHallUI.AGE_REFRESH_MS), with no request to the server. */
export const AGE_REFRESH_MS = 1000;
/** The fanfare when a game starts (hall.html #gamestart). */
export const GAME_START_SOUND = '/gemp-lotr/src/assets/fanfare_x.mp3';
/** The bot plays Fellowship Block unless the player unlocks the other formats (CreateBotTable). */
export const BOT_DEFAULT_FORMAT = 'fotr_block';

/** Cookies the Casual form remembers its choices in (CreateUnrankedTable). */
export const CASUAL_FORMAT_COOKIE = 'unranked-table-last-format';
export const CASUAL_TIMER_COOKIE = 'unranked-table-last-timer';
export const CASUAL_DEFAULT_FLOW_COOKIE = 'unranked-table-default-flow';
/** The deck memories of the three flows (SelectDeck cookie names). */
export const CASUAL_DECK_FLOW = 'unranked-table';
export const BOT_DECK_FLOW = 'bot-table';
export const JOIN_DECK_FLOW = 'join-table';

const READY: TableResult = { kind: 'ready', message: RESULT_READY };

/**
 * The Game Hall tab's tables (part 4.2a): the Waiting, Playing and Finished sections kept from the hall poll, the
 * waiting tables' timers, the Play popup's Casual and Bot flows, Join Table, Leave Table, and the started games
 * (opened in a new tab with the fanfare, or offered in a "Your game is ready" prompt when the browser blocked it).
 * Queues, tournaments, ready checks and the League / Tournament flows are 4.2b.
 */
@Injectable()
export class HallTablesService {
  private readonly comms = inject(HallComms);
  private readonly hallSession = inject(HallSession);
  private readonly api = inject(HallTablesApi);
  private readonly navigation = inject(PageNavigation);
  private readonly sound = inject(SoundPlayer);
  private readonly cookies = inject(LegacyCookies);
  private readonly timers = inject(Timers);
  private readonly player = inject(PlayerInfo);
  private readonly participantId = inject(SessionService).participantId;

  private readonly state = signal(HallTablesState.EMPTY);
  /** The three sections, each in the order the legacy hall drew it. */
  readonly sections = computed(() => this.state().sections(this.participantId));
  /** The headers' counts: "Waiting Tables (3)". */
  readonly counts = computed(() => {
    const { waiting, playing, finished } = this.sections();
    return { waiting: waiting.length, playing: playing.length, finished: finished.length };
  });

  /** Our clock, refreshed every second, for the waiting timers. */
  private readonly now = signal(this.timers.now());

  /** Formats offered for hall tables (`hall: true`), in the server's order. */
  readonly formats = signal<readonly HallFormatOption[]>([]);
  /** The Casual timers: the server's, or the page's own when it sent none. */
  readonly timersOffered = signal<readonly HallTimer[]>(FALLBACK_TIMERS);
  readonly playerDecks = signal<readonly HallDeckOption[]>([]);
  readonly libraryDecks = signal<readonly HallDeckOption[]>([]);
  /** Formats and decks have arrived at least once. */
  readonly optionsLoaded = signal(false);
  /** The formats could not be loaded (shown inline in the Play popup), else null. */
  readonly optionsError = signal<string | null>(null);
  private formatNames: Record<string, string> = {};

  private readonly results_ = signal<Readonly<Record<TableFlow, TableResult>>>({
    casual: READY,
    bot: READY,
    join: READY,
    leave: READY,
  });
  /** Each flow's result box. */
  readonly results = this.results_.asReadonly();
  /** A call is on its way. */
  readonly busy = computed(() => Object.values(this.results_()).some((result) => result.kind === 'pending'));

  /** Started games the browser would not open: the "Your game is ready" prompts, oldest first. */
  readonly readyGames = signal<readonly ReadyGame[]>([]);

  /** "Invite to table" from the chat's user list: the Casual form opens invite-only for this player (contract C1). */
  readonly invitee = signal<string | null>(null);

  constructor() {
    const updates = this.comms.updates$.subscribe((update) => this.applyUpdate(update));
    const games = this.comms.startedGames$.subscribe((gameId) => this.gameStarted(gameId));
    const stopTicking = inject(IntervalTimer).every(AGE_REFRESH_MS, () => this.now.set(this.timers.now()));
    inject(DestroyRef).onDestroy(() => {
      updates.unsubscribe();
      games.unsubscribe();
      stopTicking();
    });
  }

  table(id: string): HallTable | null {
    return this.state().get(id);
  }

  /** "01:30": how long a waiting table has been open, on the server's clock; null when it has no createdAt. */
  ageOf(table: HallTable): string | null {
    if (table.status !== 'WAITING' || table.createdAt === null) {
      return null;
    }
    return formatTableAge(this.now() + this.comms.serverClockOffset() - table.createdAt);
  }

  // ---- the poll ----

  private applyUpdate(update: HallUpdate): void {
    const before = this.state();
    const after = before.apply(update.tables, update.full);
    this.state.set(after);
    // a game's prompt goes with its table: removed, finished, or missing from a snapshot
    const live = after.liveGameIds();
    const gone = new Set<string>();
    if (update.full) {
      for (const game of this.readyGames()) {
        if (!live.has(game.gameId)) {
          gone.add(game.gameId);
        }
      }
    } else {
      for (const change of update.tables) {
        const gameId = (change.action === 'remove' ? before.get(change.id) : after.get(change.id))?.gameId;
        if (gameId && !live.has(gameId)) {
          gone.add(gameId);
        }
      }
    }
    if (gone.size > 0) {
      this.readyGames.update((games) => games.filter((game) => !gone.has(game.gameId)));
    }
  }

  private gameStarted(gameId: string): void {
    const url = gameUrl(gameId, this.participantId);
    if (!this.navigation.openInNewTab(url) && !this.readyGames().some((game) => game.gameId === gameId)) {
      this.readyGames.update((games) => [...games, { gameId, url }]);
    }
    this.sound.play(GAME_START_SOUND);
  }

  /** The prompt's link was followed, or it was closed. */
  dismissReadyGame(gameId: string): void {
    this.readyGames.update((games) => games.filter((game) => game.gameId !== gameId));
  }

  // ---- the Play popup ----

  /**
   * Play was pressed: a visitor gets the "Not logged in" dialog (false); otherwise the results are reset, the formats
   * and decks are fetched again (true).
   */
  openPlay(): boolean {
    if (this.hallSession.loggedOut()) {
      this.hallSession.requireLogin();
      return false;
    }
    this.resetResult('casual');
    this.resetResult('bot');
    void this.loadOptions();
    return true;
  }

  /** The Join dialog opens: a fresh result and fresh deck lists. */
  openJoin(): void {
    this.resetResult('join');
    void this.loadDecks();
  }

  resetResult(flow: TableFlow): void {
    this.setResult(flow, READY);
  }

  /** Formats, timers and decks (FormatManager.updateFormats and DeckManager.updateDecks). */
  async loadOptions(): Promise<void> {
    await Promise.all([this.loadFormats(), this.loadDecks()]);
    this.optionsLoaded.set(true);
  }

  private async loadFormats(): Promise<void> {
    try {
      const readout = await firstValueFrom(this.api.tableOptions());
      const formats = Object.values(readout.formats)
        .filter((format) => format.hall)
        .sort((a, b) => a.order - b.order);
      this.formatNames = Object.fromEntries(Object.values(readout.formats).map((f) => [f.code, f.name]));
      this.formats.set(formats.map((format) => ({ code: format.code, name: format.name })));
      if (readout.timers.length > 0) {
        this.timersOffered.set(readout.timers);
      }
      this.optionsError.set(null);
    } catch (error) {
      this.optionsError.set(formatsFailed(statusMessage(Math.max(0, ApiError.from(error).status))));
    }
  }

  private async loadDecks(): Promise<void> {
    const decks = await firstValueFrom(this.api.decks());
    if (decks.player !== null) {
      this.playerDecks.set(decks.player);
    }
    if (decks.library !== null) {
      this.libraryDecks.set(decks.library);
    }
  }

  /** The hall format whose name is the deck's format (decks carry the name), or null: picking a deck follows it. */
  formatOfDeck(choice: HallDeckChoice): string | null {
    const deck = (choice.source === 'library' ? this.libraryDecks() : this.playerDecks()).find(
      (d) => d.name === choice.name,
    );
    if (!deck) {
      return null;
    }
    const code = Object.keys(this.formatNames).find((c) => this.formatNames[c] === deck.formatName) ?? null;
    return code !== null && this.formats().some((format) => format.code === code) ? code : null;
  }

  /**
   * The deck a flow starts with: the one last used under `key` (a format code; null = the last one overall) if it is
   * still in the lists, else the player's first deck, else a library deck for a player with none.
   */
  initialDeck(flow: string, key: string | null): HallDeckChoice | null {
    const player = this.playerDecks();
    const library = this.libraryDecks();
    const remembered = recallDeck(this.deckMemory(flow), key);
    if (remembered !== null && hasDeck(remembered, player, library)) {
      return remembered;
    }
    return defaultDeck(player, library);
  }

  /** The deck last used in `format` (CreateUnrankedTable: picking a format brings it back), or null. */
  rememberedDeck(flow: string, format: string): HallDeckChoice | null {
    const memory = this.deckMemory(flow);
    const remembered = memory[format] ? recallDeck(memory, format) : null;
    return remembered !== null && hasDeck(remembered, this.playerDecks(), this.libraryDecks()) ? remembered : null;
  }

  /** What the Casual form opens with: the last format and timer, and whether it opens by default. */
  casualDefaults(): { format: string | null; timer: string; openByDefault: boolean } {
    return {
      format: this.cookies.read(CASUAL_FORMAT_COOKIE),
      timer: nonEmpty(this.cookies.read(CASUAL_TIMER_COOKIE)) ?? 'default',
      openByDefault: this.cookies.read(CASUAL_DEFAULT_FLOW_COOKIE) === 'true',
    };
  }

  /** "Open this flow by default". */
  setCasualOpenByDefault(value: boolean): void {
    this.cookies.write(CASUAL_DEFAULT_FLOW_COOKIE, value ? 'true' : 'false');
  }

  /** The (i) text of a timer: the server's sentence, else the general one. */
  timerDescription(code: string): string | null {
    return this.timersOffered().find((t) => t.code.toLowerCase() === code.toLowerCase())?.description ?? null;
  }

  /** Contract C1: "Invite to table" for `name` (the chat's bare or older prefixed name). */
  requestInvite(name: string): void {
    this.invitee.set(bareName(name));
  }

  clearInvite(): void {
    this.invitee.set(null);
  }

  /**
   * Play ▸ Open Casual Table. Checks the form as the legacy one did, remembers the deck, format and timer, and creates
   * the table. True when the popup should close (a success without "keep open").
   */
  async createCasual(form: CasualTableForm): Promise<boolean> {
    if (!form.format) {
      return this.refuse('casual', MUST_SELECT_FORMAT);
    }
    let description = form.description;
    if (form.isInviteOnly) {
      const invitee = bareName(form.invitee);
      const me = bareName(this.player.player()?.name);
      if (invitee === '') {
        return this.refuse('casual', MUST_NAME_INVITEE);
      }
      if (me !== '' && sameName(invitee, me)) {
        return this.refuse('casual', CANNOT_INVITE_SELF);
      }
      description = invitee;
    }
    if (form.deck === null) {
      return this.refuse('casual', MUST_SELECT_DECK);
    }
    const timer = form.timer || 'default';
    this.remember(CASUAL_DECK_FLOW, form.deck, form.format);
    this.cookies.write(CASUAL_FORMAT_COOKIE, form.format);
    this.cookies.write(CASUAL_TIMER_COOKIE, timer);
    const result = await this.run(
      'casual',
      this.api.createTable({
        format: form.format,
        deckName: form.deck.name,
        deckSource: form.deck.source,
        timer,
        desc: description,
        isPrivate: form.isPrivate,
        isInviteOnly: form.isInviteOnly,
      }),
    );
    if (result.ok && form.keepOpen) {
      this.setResult('casual', { kind: 'success', message: TABLE_CREATED_KEPT_OPEN });
      return false;
    }
    if (result.ok) {
      this.clearInvite();
    }
    return result.ok;
  }

  /** Play ▸ Play Against Bots. True when the popup should close. */
  async createBot(form: BotTableForm): Promise<boolean> {
    if (!form.format) {
      return this.refuse('bot', MUST_SELECT_FORMAT);
    }
    if (form.deck === null) {
      return this.refuse('bot', MUST_SELECT_OWN_DECK);
    }
    this.remember(BOT_DECK_FLOW, form.deck, form.format);
    const result = await this.run(
      'bot',
      this.api.createSoloTable({
        format: form.format,
        deckName: form.deck.name,
        deckSource: form.deck.source,
        botDeckName: form.botDeck,
        isPrivate: false,
      }),
    );
    return result.ok;
  }

  /** Join Table with a deck. True when the dialog should close (the game opens from the hall when it starts). */
  async join(tableId: string, deck: HallDeckChoice | null): Promise<boolean> {
    if (deck === null) {
      return this.refuse('join', MUST_SELECT_DECK);
    }
    this.remember(JOIN_DECK_FLOW, deck, null);
    return (await this.run('join', this.api.joinTable(tableId, deck.name, deck.source))).ok;
  }

  /** Leave Table (the viewer's own waiting table); the row goes with the next poll answer. */
  async leave(tableId: string): Promise<boolean> {
    return (await this.run('leave', this.api.leaveTable(tableId))).ok;
  }

  // ---- helpers ----

  private async run(flow: TableFlow, call$: ReturnType<HallTablesApi['joinTable']>): Promise<TableCallResult> {
    this.setResult(flow, { kind: 'pending', message: RESULT_WORKING });
    const result = await firstValueFrom(call$);
    this.setResult(flow, { kind: result.ok ? 'success' : 'error', message: result.message });
    return result;
  }

  private refuse(flow: TableFlow, message: string): false {
    this.setResult(flow, { kind: 'error', message });
    return false;
  }

  private setResult(flow: TableFlow, result: TableResult): void {
    this.results_.update((results) => ({ ...results, [flow]: result }));
  }

  private deckMemory(flow: string) {
    return readDeckMemory(this.cookies.read(deckMemoryCookie(flow)));
  }

  private remember(flow: string, deck: HallDeckChoice, key: string | null): void {
    this.cookies.write(deckMemoryCookie(flow), JSON.stringify(rememberDeck(this.deckMemory(flow), deck, key)));
  }
}

/** A cookie's value, or null for none or an empty one. */
function nonEmpty(value: string | null): string | null {
  return value === '' ? null : value;
}
