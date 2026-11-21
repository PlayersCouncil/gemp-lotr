import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { ChatService } from '../../chat/chat.service';
import { TournamentAnnouncement } from '../../chat/chat-models';
import { Notifier } from '../../notifier/notifier';
import { IntervalTimer } from '../../platform/interval-timer';
import { PageNavigation } from '../../platform/page-navigation';
import { SoundPlayer } from '../../platform/sound-player';
import { Timers } from '../../platform/timers';
import { PlayerInfo } from '../../session/player-info.service';
import { HallBoard, HallBoardChange } from '../board/hall-board.service';
import { escapeHtml, HallDialogs, pageMarkup, serverMessageHtml, serverMessageMarkup } from '../hall-dialogs.service';
import { CreateTournament } from '../play/create-tournament.service';
import { JoinDialog } from '../play/join-dialog.service';
import { draftKey, HallReadyOffers } from '../ready/hall-ready-offers.service';
import { RESULT_OK } from '../tables/hall-tables-messages';
import { TableCallResult } from '../tables/hall-table-models';
import { GAME_START_SOUND } from '../tables/hall-tables.service';
import { HallPrizes, HallQueue, HallTournament } from './hall-queue-models';
import {
  ABANDONED_NOTE,
  abandonQuestion,
  ABANDON_TOURNAMENT,
  COMPETING_PLAYERS_TITLE,
  NO_PRIZE_INFO,
  PRIZES_TITLE,
  READY_CHECK_TITLE,
  RESPONSE_ERROR_TITLE,
  RESPONSE_TITLE,
  TOURNAMENT_UPDATE_TITLE,
} from './hall-queue-messages';
import { tournamentDraftUrl } from './hall-queue-parsers';
import { HallQueuesApi } from './hall-queues-api.service';

/** The ready check dialog's countdown ticks once a second (hallUi.js showReadyCheckDialog). */
export const READY_CHECK_TICK_MS = 1000;

/** A queue's ready check while the viewer is signed up (hallUi.js readyChecks). */
interface ReadyCheck {
  readonly queueId: string;
  readonly queueName: string;
  /** Our clock's time when it ends (now + readyCheckSecsRemaining at the last update). */
  readonly deadline: number;
  /** Its dialog was shown once (it is not shown again for the same check). */
  readonly shown: boolean;
  readonly dialogOpen: boolean;
}

/** The ready check dialog: "Ready Check started for the <b>name</b> tournament. Confirm ... within N seconds." */
export interface ReadyCheckDialog {
  readonly queueId: string;
  readonly queueName: string;
  readonly seconds: number;
}

/**
 * The Game Hall tab's queues and tournaments, beyond their rows (HallBoard): what each row's buttons do (Join / Leave /
 * Start Now / READY CHECK / Show Prizes; Go to Draft / Register Deck / Abandon Tournament / Join Tournament), the
 * ready check and its dialog, the drafts that open when they start, and the "Tournament Update" announcements.
 */
@Injectable()
export class HallQueuesService {
  private readonly board = inject(HallBoard);
  private readonly api = inject(HallQueuesApi);
  private readonly join = inject(JoinDialog);
  private readonly dialogs = inject(HallDialogs);
  private readonly readyOffers = inject(HallReadyOffers);
  private readonly navigation = inject(PageNavigation);
  private readonly notifier = inject(Notifier);
  private readonly chat = inject(ChatService);
  private readonly sound = inject(SoundPlayer);
  private readonly timers = inject(Timers);
  private readonly player = inject(PlayerInfo);

  /**
   * The viewer is (or just became) part of a tournament or its queue (hallUi.js inTournament): the hall-wide
   * "Tournament Update" announcements are shown only then.
   */
  readonly inTournament = signal(false);

  private readonly checks = signal<ReadonlyMap<string, ReadyCheck>>(new Map());
  private readonly now = signal(this.timers.now());

  /** The ready check dialogs showing, with their seconds left. */
  readonly readyCheckDialogs = computed<ReadyCheckDialog[]>(() =>
    [...this.checks().values()]
      .filter((check) => check.dialogOpen)
      .map((check) => ({
        queueId: check.queueId,
        queueName: check.queueName,
        seconds: Math.max(0, Math.round((check.deadline - this.now()) / 1000)),
      })),
  );
  readonly readyCheckTitle = READY_CHECK_TITLE;

  constructor() {
    const changes = this.board.changes$.subscribe((change) => this.applied(change));
    const announcements = this.chat.tournamentAnnouncements$.subscribe((a) => this.announced(a));
    const stopTicking = inject(IntervalTimer).every(READY_CHECK_TICK_MS, () => {
      if (this.readyCheckDialogs().length > 0) {
        this.now.set(this.timers.now());
      }
    });
    const joined = this.join.joinedEvent$.subscribe(() => this.inTournament.set(true));
    const created = inject(CreateTournament).created$.subscribe(() => this.inTournament.set(true));
    inject(DestroyRef).onDestroy(() => {
      changes.unsubscribe();
      joined.unsubscribe();
      created.unsubscribe();
      announcements.unsubscribe();
      stopTicking();
    });
  }

  // ---- the poll ----

  private applied({ update, after }: HallBoardChange): void {
    if (update.full) {
      // clearHallRows: every check ends (those still running come back with the snapshot)
      this.checks.set(new Map());
    }
    for (const change of update.queues) {
      if (change.action === 'remove') {
        this.endReadyCheck(change.id);
        continue;
      }
      const queue = after.events.queue(change.id);
      if (queue !== null) {
        if (queue.signedUp) {
          this.inTournament.set(true);
        }
        this.updateReadyCheck(queue);
      }
    }
    for (const change of update.tournaments) {
      if (change.action === 'remove') {
        this.readyOffers.dismiss(draftKey(change.id));
        continue;
      }
      const tournament = after.events.tournament(change.id);
      if (tournament !== null) {
        this.tournamentDrawn(tournament, change.action === 'add');
      }
    }
    if (update.full) {
      // pruneReadyModals: a draft's prompt goes with its tournament
      this.readyOffers.keepOnly('draft', new Set(after.events.allTournaments().map((t) => draftKey(t.id))));
    }
  }

  private tournamentDrawn(tournament: HallTournament, added: boolean): void {
    if (tournament.signedUp) {
      this.inTournament.set(true);
    }
    const draftUrl = tournamentDraftUrl(tournament);
    if (draftUrl === null) {
      this.readyOffers.dismiss(draftKey(tournament.id));
    } else if (added && tournament.signedUp) {
      this.readyOffers.openOrOffer(draftKey(tournament.id), 'draft', draftUrl);
      this.sound.play(GAME_START_SOUND);
    }
  }

  private announced(announcement: TournamentAnnouncement): void {
    const me = this.player.player()?.name ?? null;
    const shown =
      announcement.recipients === null ? this.inTournament() : me !== null && announcement.recipients.includes(me);
    if (shown) {
      this.dialogs.show(TOURNAMENT_UPDATE_TITLE, serverMessageMarkup(announcement.message));
    }
  }

  // ---- the ready check (updateReadyCheck, tickReadyCheck, confirmReadyCheck, endReadyCheck) ----

  private updateReadyCheck(queue: HallQueue): void {
    const secs = queue.readyCheckSecsRemaining;
    if (!queue.signedUp || !(secs > -1)) {
      this.endReadyCheck(queue.id);
      return;
    }
    const old = this.checks().get(queue.id);
    const deadline = this.timers.now() + secs * 1000;
    let check: ReadyCheck;
    if (queue.confirmedReadyCheck) {
      check = { queueId: queue.id, queueName: queue.name, deadline, shown: true, dialogOpen: false };
    } else if (!old?.shown) {
      check = { queueId: queue.id, queueName: queue.name, deadline, shown: true, dialogOpen: true };
      this.sound.play(GAME_START_SOUND);
    } else {
      check = { ...old, deadline };
    }
    this.now.set(this.timers.now());
    this.checks.update((checks) => new Map(checks).set(queue.id, check));
  }

  private endReadyCheck(queueId: string): void {
    if (this.checks().has(queueId)) {
      this.checks.update((checks) => {
        const next = new Map(checks);
        next.delete(queueId);
        return next;
      });
    }
  }

  /** The dialog's OK: it closes, the check goes on (the row keeps its READY CHECK button). */
  closeReadyCheckDialog(queueId: string): void {
    const check = this.checks().get(queueId);
    if (check?.dialogOpen) {
      this.checks.update((checks) => new Map(checks).set(queueId, { ...check, dialogOpen: false }));
    }
  }

  /** "Ready" in the dialog, or the row's READY CHECK button. */
  async confirmReady(queueId: string): Promise<void> {
    this.closeReadyCheckDialog(queueId);
    await this.processResponse(this.api.confirmReadyCheck(queueId));
  }

  // ---- the rows' buttons ----

  /** Join Queue (the Join dialog for a Constructed queue, else at once). */
  joinQueue(queue: HallQueue): Promise<void> {
    return this.join.joinQueue(queue);
  }

  async leaveQueue(queueId: string): Promise<void> {
    if (await this.processResponse(this.api.leaveQueue(queueId))) {
      this.inTournament.set(false);
    }
  }

  async startQueue(queueId: string): Promise<void> {
    await this.processResponse(this.api.startQueue(queueId));
  }

  /** Show Prizes, or a Prizes cell's breakdown: the "Prizes details" dialog (showPrizes). */
  showPrizes(prizes: HallPrizes): void {
    let html: string;
    if (prizes.lines.length > 0) {
      html = `<ul>${prizes.lines.map((line) => `<li>${escapeHtml(line)}</li>`).join('')}</ul>`;
    } else {
      html = `<p>${escapeHtml(prizes.label || NO_PRIZE_INFO)}</p>`;
    }
    this.dialogs.show(PRIZES_TITLE, pageMarkup(html));
  }

  /** A tournament of more than 8 players: its count opens the list. */
  showCompetingPlayers(tournament: HallTournament): void {
    this.dialogs.show(
      COMPETING_PLAYERS_TITLE,
      pageMarkup(`${escapeHtml(tournament.playerList)}<br><br>${escapeHtml(ABANDONED_NOTE)}`),
    );
  }

  /** Go to Draft: the draft page in a new tab (no prompt when blocked, as the legacy button). */
  goToDraft(url: string): void {
    this.navigation.openInNewTab(url);
  }

  openRegisterDeck(tournament: HallTournament): void {
    this.join.openRegisterDeck(tournament);
  }

  joinLate(tournament: HallTournament): Promise<void> {
    return this.join.joinLate(tournament);
  }

  /** Abandon Tournament, after the question. A refusal (still playing a match, ...) shows as an "Error". */
  async abandon(tournament: HallTournament): Promise<void> {
    const sure = await this.notifier.confirm(abandonQuestion(tournament.name), { title: ABANDON_TOURNAMENT });
    if (sure && (await this.processResponse(this.api.dropFromTournament(tournament.id)))) {
      this.inTournament.set(false);
    }
  }

  /**
   * hallUi.js processResponse: an <error> or <response> message is shown in a dialog ("Error" / "Response") and added
   * to the chat as a warning; anything else passes quietly. False for a refusal.
   */
  private async processResponse(call$: Observable<TableCallResult>): Promise<boolean> {
    const result = await firstValueFrom(call$);
    if (!result.ok || result.message !== RESULT_OK) {
      this.chat.appendWarning(serverMessageHtml(result.message));
      this.dialogs.show(result.ok ? RESPONSE_TITLE : RESPONSE_ERROR_TITLE, serverMessageMarkup(result.message));
    }
    return result.ok;
  }
}
