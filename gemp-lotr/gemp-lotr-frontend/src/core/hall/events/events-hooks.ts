import { InjectionToken } from '@angular/core';
import { CalendarQueueOffer } from './events-models';

/**
 * What the Events tab needs from the rest of the hall, and tells it, as optional hooks the hall's frame (or the Game
 * Hall's part) provides. Without them the tab works on its own: no Join Queue button on the calendar, and nobody told
 * of a league joined.
 */

/**
 * The hall's queue join (JoinTable.startQueueJoin: the deck dialog for a constructed queue, at once for the others),
 * for the calendar's "Join Queue" on a scheduled tournament whose hall queue is open.
 */
export interface EventsQueueJoiner {
  joinQueue(offer: CalendarQueueOffer): void;
}

export const EVENTS_QUEUE_JOINER = new InjectionToken<EventsQueueJoiner>('EVENTS_QUEUE_JOINER');

/** Told of each league the viewer joins (events.html's join callback: the Play popup reloads its league formats). */
export interface EventsLeagueJoinListener {
  leagueJoined(code: string): void;
}

export const EVENTS_LEAGUE_JOIN_LISTENER = new InjectionToken<EventsLeagueJoinListener>('EVENTS_LEAGUE_JOIN_LISTENER');
