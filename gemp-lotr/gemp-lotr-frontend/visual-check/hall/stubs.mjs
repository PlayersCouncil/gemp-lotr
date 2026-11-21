// The server's answers to the hall's and the login page's calls, for hall/run.mjs: the saved answers of
// src/testing/server-responses (the ones the unit tests read), the same for the legacy pages and the Angular ones.
// Passed to stub-server.mjs's routeToStub as its `answer`; what isn't here (formats, decks, leagues) the shared
// stub answers.
import fs from 'node:fs';
import path from 'node:path';
import { RESPONSES_DIR } from '../stub-server.mjs';

/** The hall's long-polls (hall, chat): the server answers after at most 5 s when nothing changed. */
const POLL_MS = 4000;

const file = (name, type = 'application/xml') => ({ file: name, type });
const json = (name) => file(name, 'application/json');

/** Calls a visitor (not logged in) is refused with 401, as the server refuses them. */
const NEEDS_LOGIN =
  /^\/(player|hall|chat|deck|league|tournament|calendar|eventHistory|gameHistory|playerStats|scenarios)(\/|$)/;
/** What the server answers anyone: the format definitions and errata, server stats and patch notes, the status. */
const PUBLIC =
  /^\/(hall\/formats\/json|hall\/format\/[^/]+\/json|hall\/errata\/json|stats|patchnotes(\/months)?|status|)$/;

const read = (name) => fs.readFileSync(path.join(RESPONSES_DIR, name), 'utf8');

/** hall.xml with its waiting table another player's, so the viewer can join it. */
function joinableHall() {
  return read('hall.xml').replace(/(<table action="add" id="11"[^>]*?)playing="true"/, '$1playing="false"');
}

/**
 * The answer function for a scenario. `visitor`: nobody is logged in. `hall`: the hall's snapshot (hall.xml: tables;
 * hall-queues.xml: queues and tournaments; 'join': hall.xml with a table to join). `history`: My Account's game history
 * (game-history.xml). `player`: who is logged in (player.json; e.g. an admin, `{ type: 'au' }`).
 */
export function hallAnswers({ visitor = false, hall = 'hall.xml', history = 'game-history.xml', player = null } = {}) {
  return (url, request) => {
    const p = decodeURIComponent(url.pathname.replace('/gemp-lotr-server', ''));
    const get = request.method() === 'GET';
    if (visitor && NEEDS_LOGIN.test(p) && !PUBLIC.test(p)) return { status: 401 };
    // The login page's server status: the legacy page reads the text of GET /, the Angular page GET /status
    if (p === '/') return { text: read('server-status.txt'), type: 'text/html' };
    if (p === '/status') return json('server-status.json');
    if (p === '/player')
      return player ? { json: { ...JSON.parse(read('player.json')), ...player } } : json('player.json');
    if (p === '/player/ignores') return { json: { ignored: [] } };
    if (p === '/hall' && get) return hall === 'join' ? { xml: joinableHall() } : file(hall);
    if (p === '/hall/update') return { ...file('hall-update-empty.xml'), delay: POLL_MS };
    if (p === '/chat/Game Hall') {
      return get ? file('chat.xml') : { xml: '<chat roomName="Game Hall"/>', delay: POLL_MS };
    }
    if (p === '/hall/players') return { json: { players: ['erin', 'eric'] } };
    if (p === '/hall/formats/json') return json('format-definitions.json');
    if (/^\/hall\/format\/[^/]+\/json$/.test(p)) return json('hall-format-definition.json');
    if (p === '/hall/errata/json') return json('errata.json');
    if (p === '/stats') return json('server-stats.json');
    if (p === '/patchnotes') return json('patch-notes-page.json');
    if (p === '/patchnotes/months') return json('patch-notes-months.json');
    if (p === '/calendar') return json('events-calendar.json');
    if (p === '/eventHistory/months') return json('event-history-months.json');
    if (p === '/eventHistory') {
      return json(
        url.searchParams.get('kind') === 'league' ? 'event-history-league.json' : 'event-history-tournament.json',
      );
    }
    if (p === '/league/1700000002') return file('league-detail-rtmd.xml');
    if (p.startsWith('/league/') && get) return file('league-detail.xml');
    if (p === '/tournament') return file('tournaments.xml');
    if (p === '/tournament/tournamentFormats') return json('tournament-formats.json');
    if (p === '/tournament/daily-pc_movie-20261031') return file('tournament-finished.xml');
    if (p.startsWith('/tournament/') && get) return file('tournament-playing.xml');
    if (p === '/gameHistory/filters') return json('game-history-filters.json');
    if (p === '/gameHistory') return file(history);
    if (p === '/playerStats') return file('player-stats.xml');
    if (p === '/scenarios') return json('scenarios.json');
    if (p === '/scenarios/mine') return json('scenarios-mine.json');
    if (p === '/scenarios/public') return json('scenarios-public.json');
    return null;
  };
}
