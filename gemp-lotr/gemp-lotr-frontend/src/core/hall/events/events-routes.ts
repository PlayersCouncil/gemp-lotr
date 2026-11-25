/**
 * The Events tab's addresses. Each sub-tab is a route, and a league's or tournament's own link opens its drawer:
 * `events/calendar`, `events/leagues[/<code>]`, `events/tournaments[/<id>]`. The legacy hall's links (hallLinks.js
 * GempLinks: `#events`, `#events/<sub>`, `#league-<code>`, `#tournament-<id>`) map onto them.
 */

export type EventsSubTab = 'calendar' | 'leagues' | 'tournaments';

/** Where in the Events tab: a sub-tab, and for the lists the league or tournament whose drawer to open. */
export type EventsLink =
  | { readonly sub: 'calendar' }
  | { readonly sub: 'leagues'; readonly code: string | null }
  | { readonly sub: 'tournaments'; readonly id: string | null };

export const EVENTS_PATH = 'events';
/** The sub-tab opened by `events` alone (the legacy tab's first). */
export const DEFAULT_EVENTS_SUB_TAB: EventsSubTab = 'calendar';

/** The route parameters' names (`events/leagues/:code`, `events/tournaments/:id`). */
export const LEAGUE_CODE_PARAM = 'code';
export const TOURNAMENT_ID_PARAM = 'id';

/** What hallLinks.js accepted in a league code and a tournament id. */
const LEAGUE_CODE = /^[A-Za-z0-9_-]+$/;
const TOURNAMENT_ID = /^[A-Za-z0-9_.-]+$/;

export function leagueLink(code: string): EventsLink {
  return { sub: 'leagues', code };
}

export function tournamentLink(id: string): EventsLink {
  return { sub: 'tournaments', id };
}

/** The router's commands for a link, from the hall's routes: ['events', 'leagues', '<code>']. */
export function eventsCommands(link: EventsLink): string[] {
  const commands = [EVENTS_PATH, link.sub];
  if (link.sub === 'leagues' && link.code !== null) {
    commands.push(link.code);
  } else if (link.sub === 'tournaments' && link.id !== null) {
    commands.push(link.id);
  }
  return commands;
}

/**
 * A route's sub-tab and parameter as a link; null for a sub-tab the tab does not have. A code or id that is not
 * one the legacy links accepted opens the list alone.
 */
export function parseEventsRoute(sub: string | null, param: string | null): EventsLink | null {
  switch (sub ?? DEFAULT_EVENTS_SUB_TAB) {
    case 'calendar':
      return { sub: 'calendar' };
    case 'leagues':
      return { sub: 'leagues', code: param !== null && LEAGUE_CODE.test(param) ? param : null };
    case 'tournaments':
      return { sub: 'tournaments', id: param !== null && TOURNAMENT_ID.test(param) ? param : null };
    default:
      return null;
  }
}

/** A legacy hall fragment of the Events tab as a link, or null when it is not one. */
export function legacyEventsLink(fragment: string): EventsLink | null {
  const league = /^#league-([A-Za-z0-9_-]+)$/.exec(fragment);
  if (league?.[1] !== undefined) {
    return leagueLink(league[1]);
  }
  const tournament = /^#tournament-([A-Za-z0-9_.-]+)$/.exec(fragment);
  if (tournament?.[1] !== undefined) {
    return tournamentLink(tournament[1]);
  }
  const tab = /^#events(?:\/([a-z]+))?$/.exec(fragment);
  return tab ? parseEventsRoute(tab[1] ?? null, null) : null;
}
