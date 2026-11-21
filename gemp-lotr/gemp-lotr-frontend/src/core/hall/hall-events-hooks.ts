import { inject, Injectable } from '@angular/core';
import { EventsLeagueJoinListener, EventsQueueJoiner } from './events/events-hooks';
import { CalendarQueueOffer } from './events/events-models';
import { HallBoard } from './board/hall-board.service';
import { LeagueTableFlow } from './play/league-table.service';
import { HallQueue } from './queues/hall-queue-models';
import { HallQueuesService } from './queues/hall-queues.service';

/**
 * What the Events tab asks of the Game Hall (events-hooks.ts), provided with the hall's services: the calendar's Join
 * Queue goes through the hall's join-queue flow (JoinTable.startQueueJoin: the deck dialog for a Constructed queue,
 * at once for the others), and a league joined on the Events tab reloads Play ▸ League's leagues (events.html's join
 * callback: FormatManager.updateFormats).
 */
@Injectable()
export class HallEventsHooks implements EventsQueueJoiner, EventsLeagueJoinListener {
  private readonly board = inject(HallBoard);
  private readonly queues = inject(HallQueuesService);
  private readonly league = inject(LeagueTableFlow);

  joinQueue(offer: CalendarQueueOffer): void {
    void this.queues.joinQueue(this.board.events().queue(offer.id) ?? queueOfOffer(offer));
  }

  /** Any league: Play > League's list is fetched again, its choice kept. */
  leagueJoined(): void {
    void this.league.load(null);
  }
}

/** A queue the hall has not listed (yet), from what the calendar knows of it: enough to join it. */
export function queueOfOffer(offer: CalendarQueueOffer): HallQueue {
  return {
    id: offer.id,
    cost: 0,
    collection: '',
    format: '',
    type: offer.type.toLowerCase(),
    name: offer.name,
    playerCount: 0,
    playerList: '',
    prizes: { label: '', lines: [] },
    system: '',
    start: offer.start,
    signedUp: false,
    joinable: true,
    startable: false,
    readyCheckSecsRemaining: -1,
    confirmedReadyCheck: false,
    wc: false,
    draftCode: null,
    recurring: false,
    scheduled: true,
    displayInWaitingTables: false,
  };
}
