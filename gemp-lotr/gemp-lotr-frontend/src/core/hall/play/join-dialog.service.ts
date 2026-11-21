import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable, Subject } from 'rxjs';
import { HallDialogs, pageMarkup, plainTextMarkup } from '../hall-dialogs.service';
import { HallQueue, HallTournament } from '../queues/hall-queue-models';
import {
  DECK_REGISTERED_TITLE,
  describeEvent,
  failedTitle,
  JOIN_TABLE,
  JOINED_TOURNAMENT_TITLE,
  joinQueueTitle,
  joinTournamentTitle,
  MUST_SELECT_DECK_SHORT,
  queueJoinedMessage,
  REGISTER_DECK_SUBTITLE,
  registerDeckTitle,
} from '../queues/hall-queue-messages';
import { HallQueuesApi } from '../queues/hall-queues-api.service';
import { HallTablesApi } from '../tables/hall-tables-api.service';
import { MUST_SELECT_DECK, RESULT_READY, RESULT_WORKING } from '../tables/hall-tables-messages';
import { HallDeckChoice, TableCallResult, TableResult } from '../tables/hall-table-models';
import { HallTablesService, JOIN_DECK_FLOW } from '../tables/hall-tables.service';

/** Which of the four flows the Join dialog serves (JoinTable). */
export type JoinKind = 'table' | 'queue' | 'late-join' | 'register-deck';

/** The deck memory of each flow (SelectDeck.setFlow): "<flow>-decks". */
export const JOIN_FLOWS: Readonly<Record<JoinKind, string>> = {
  table: JOIN_DECK_FLOW,
  queue: 'join-queue',
  'late-join': 'late-join',
  'register-deck': 'register-deck',
};

/** The open Join dialog: what it says and what it joins. */
export interface JoinDialogState {
  readonly kind: JoinKind;
  /** The dialog's title and its heading. */
  readonly title: string;
  readonly subtitle: string | null;
  /** The button: "Join Table", "Join Queue", "Join Tournament", "Register Deck". */
  readonly buttonLabel: string;
  /** The deck memory's flow and key (format, tournament id; null = the last deck overall). */
  readonly flow: string;
  readonly memoryKey: string | null;
  readonly targetId: string;
}

const READY: TableResult = { kind: 'ready', message: RESULT_READY };

/**
 * The Join dialog and the joins that need no dialog (JoinTable): join a waiting table, join a tournament queue, join a
 * running tournament late, and register a deck for a limited tournament. One dialog serves all four; its title,
 * heading and button say which. A Constructed queue or late join asks for a deck; the others (Sealed, drafts) are sent
 * at once. Every outcome is shown (TableFlow.respond): in the dialog's result box while it is open, otherwise in a
 * hall dialog; a queue or tournament joined shows its own confirmation.
 */
@Injectable()
export class JoinDialog {
  private readonly tables = inject(HallTablesService);
  private readonly tablesApi = inject(HallTablesApi);
  private readonly queuesApi = inject(HallQueuesApi);
  private readonly dialogs = inject(HallDialogs);

  private readonly state_ = signal<JoinDialogState | null>(null);
  /** The dialog, or null while it is closed. */
  readonly state = this.state_.asReadonly();
  private readonly result_ = signal<TableResult>(READY);
  readonly result = this.result_.asReadonly();
  readonly busy = signal(false);

  private readonly joined = new Subject<void>();
  /** A queue or tournament was joined, or a deck registered (the hall's setPendingTournament(true)). */
  readonly joinedEvent$: Observable<void> = this.joined.asObservable();

  /** The deck the dialog starts with (the flow's memory for its key, else the first deck: SelectDeck.requestRestore). */
  initialDeck(): HallDeckChoice | null {
    const state = this.state_();
    return state === null ? null : this.tables.initialDeck(state.flow, state.memoryKey);
  }

  close(): void {
    this.state_.set(null);
  }

  /** Join Table at a waiting table. */
  openTable(tableId: string): void {
    this.open({
      kind: 'table',
      title: JOIN_TABLE,
      subtitle: null,
      buttonLabel: JOIN_TABLE,
      flow: JOIN_FLOWS.table,
      memoryKey: null,
      targetId: tableId,
    });
  }

  /** Join Queue (contract C3: startQueueJoin): the dialog for a Constructed queue, else the join at once. */
  async joinQueue(queue: HallQueue): Promise<void> {
    if (queue.type === 'constructed') {
      this.open({
        kind: 'queue',
        title: joinQueueTitle(queue.name),
        subtitle: describeEvent(queue.format, queue.start) || null,
        buttonLabel: 'Join Queue',
        flow: JOIN_FLOWS.queue,
        memoryKey: queue.format,
        targetId: queue.id,
      });
      this.queue = queue;
      return;
    }
    await this.submitQueue(queue, null);
  }

  /** Join Tournament (a late join): the dialog for a Constructed tournament, else the join at once. */
  async joinLate(tournament: HallTournament): Promise<void> {
    if (tournament.type === 'constructed') {
      this.open({
        kind: 'late-join',
        title: joinTournamentTitle(tournament.name),
        subtitle: tournament.format ? `Format: ${tournament.format}` : null,
        buttonLabel: 'Join Tournament',
        flow: JOIN_FLOWS['late-join'],
        memoryKey: tournament.format,
        targetId: tournament.id,
      });
      this.tournament = tournament;
      return;
    }
    await this.submitLate(tournament, null);
  }

  /** Register Deck for a limited tournament. */
  openRegisterDeck(tournament: HallTournament): void {
    this.open({
      kind: 'register-deck',
      title: registerDeckTitle(tournament.name),
      subtitle: REGISTER_DECK_SUBTITLE,
      buttonLabel: 'Register Deck',
      flow: JOIN_FLOWS['register-deck'],
      memoryKey: tournament.id,
      targetId: tournament.id,
    });
    this.tournament = tournament;
  }

  /** The dialog's button. */
  async submit(deck: HallDeckChoice | null): Promise<void> {
    const state = this.state_();
    if (state === null) {
      return;
    }
    switch (state.kind) {
      case 'table':
        return this.submitTable(state.targetId, deck);
      case 'queue':
        if (deck === null || this.queue === null) {
          this.result_.set({ kind: 'error', message: MUST_SELECT_DECK });
          return;
        }
        return this.submitQueue(this.queue, deck);
      case 'late-join':
        if (deck === null || this.tournament === null) {
          this.result_.set({ kind: 'error', message: MUST_SELECT_DECK });
          return;
        }
        return this.submitLate(this.tournament, deck);
      case 'register-deck':
        return this.tournament === null ? undefined : this.submitRegistration(this.tournament, deck);
    }
  }

  // ---- the four submits ----

  private queue: HallQueue | null = null;
  private tournament: HallTournament | null = null;

  private async submitTable(tableId: string, deck: HallDeckChoice | null): Promise<void> {
    if (deck === null) {
      this.result_.set({ kind: 'error', message: MUST_SELECT_DECK });
      return;
    }
    this.remember(JOIN_FLOWS.table, deck, null);
    // quiet on success: the game starts and opens from the hall
    await this.respond(JOIN_TABLE, this.tablesApi.joinTable(tableId, deck.name, deck.source), () => {
      this.close();
      return true;
    });
  }

  private async submitQueue(queue: HallQueue, deck: HallDeckChoice | null): Promise<void> {
    if (deck !== null) {
      this.remember(JOIN_FLOWS.queue, deck, queue.format);
    }
    await this.respond(
      joinQueueTitle(queue.name),
      this.queuesApi.joinQueue(queue.id, deck?.name ?? '', deck?.source ?? ''),
      () => {
        this.joined.next();
        this.close();
        this.dialogs.show(
          JOINED_TOURNAMENT_TITLE,
          pageMarkup(queueJoinedMessage(queue.type, queue.name, deck?.name ?? null, queue.start)),
        );
        return true;
      },
    );
  }

  private async submitLate(tournament: HallTournament, deck: HallDeckChoice | null): Promise<void> {
    if (deck !== null) {
      this.remember(JOIN_FLOWS['late-join'], deck, tournament.format);
    }
    await this.respond(
      joinTournamentTitle(tournament.name),
      this.queuesApi.joinTournamentLate(tournament.id, deck?.name ?? '', deck?.source ?? ''),
      (message) => {
        this.joined.next();
        this.close();
        this.dialogs.show(JOINED_TOURNAMENT_TITLE, plainTextMarkup(message));
        return true;
      },
    );
  }

  private async submitRegistration(tournament: HallTournament, deck: HallDeckChoice | null): Promise<void> {
    if (deck === null) {
      this.result_.set({ kind: 'error', message: MUST_SELECT_DECK_SHORT });
      return;
    }
    this.remember(JOIN_FLOWS['register-deck'], deck, tournament.id);
    await this.respond(
      registerDeckTitle(tournament.name),
      this.queuesApi.registerDeck(tournament.id, deck.name, deck.source),
      (message) => {
        this.joined.next();
        this.close();
        this.dialogs.show(DECK_REGISTERED_TITLE, plainTextMarkup(message));
        return true;
      },
    );
  }

  // ---- helpers ----

  private open(state: JoinDialogState): void {
    this.result_.set(READY);
    this.state_.set(state);
    void this.tables.loadDecks();
  }

  /**
   * TableFlow.respond: "Working…", then the outcome in the result box; `onSuccess` returns true when it showed the
   * outcome itself. When the box is not on screen (no dialog open), the outcome goes in a hall dialog instead.
   */
  private async respond(
    title: string,
    call$: Observable<TableCallResult>,
    onSuccess: (message: string) => boolean,
  ): Promise<void> {
    this.result_.set({ kind: 'pending', message: RESULT_WORKING });
    this.busy.set(true);
    const result = await firstValueFrom(call$);
    this.busy.set(false);
    this.result_.set({ kind: result.ok ? 'success' : 'error', message: result.message });
    const visible = this.state_() !== null;
    if (result.ok && onSuccess(result.message)) {
      return;
    }
    if (!visible) {
      this.dialogs.showText(result.ok ? title : failedTitle(title), result.message);
    }
  }

  private remember(flow: string, deck: HallDeckChoice, key: string | null): void {
    this.tables.rememberDeck(flow, deck, key);
  }
}
