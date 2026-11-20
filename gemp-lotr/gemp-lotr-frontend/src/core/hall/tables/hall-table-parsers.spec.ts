import { readServerResponse, readServerXml } from '../../../testing/files';
import { parseHallUpdate } from '../hall-parsers';
import {
  bareName,
  formatTableAge,
  gameUrl,
  parseHallTable,
  parseHallTimers,
  parseTableCallResult,
  tableAction,
  tableAgeTitle,
  tableNote,
  timerLabel,
} from './hall-table-parsers';
import { HallTable } from './hall-table-models';

const tablesOf = (file: string) => parseHallUpdate(readServerXml(file), true)!.tables.map(parseHallTable);

const base: HallTable = {
  id: '1',
  gameId: null,
  status: 'WAITING',
  statusDescription: 'Waiting',
  format: 'Movie Block',
  tournament: 'Casual',
  description: '',
  players: ['alice'],
  playing: false,
  watchable: false,
  isPrivate: false,
  isInviteOnly: false,
  invitedYou: false,
  winner: null,
  createdAt: null,
};

describe('hall table parsers', () => {
  it('types the tables of a snapshot', () => {
    const [waiting, playing] = tablesOf('hall.xml');
    expect(waiting).toEqual({
      id: '11',
      gameId: null,
      status: 'WAITING',
      statusDescription: 'Waiting',
      format: 'Fellowship Block',
      tournament: '',
      description: 'new player, go easy',
      players: ['alice'],
      playing: true,
      watchable: false,
      isPrivate: false,
      isInviteOnly: false,
      invitedYou: false,
      winner: null,
      createdAt: 1794232900000,
    });
    expect(playing).toMatchObject({ id: '12', gameId: 'g-12', status: 'PLAYING', players: ['bob', 'carol'] });
    expect(playing.watchable).toBe(true);
  });

  it('reads invitations, winners and empty game ids', () => {
    const [invited, other, finished] = tablesOf('hall-tables-update.xml');
    expect(invited).toMatchObject({ gameId: null, isPrivate: true, isInviteOnly: true, invitedYou: true });
    expect(other.invitedYou).toBe(false);
    expect(finished).toMatchObject({ status: 'FINISHED', winner: 'carol' });
  });

  it('gives each table its one action', () => {
    expect(tableAction({ ...base, playing: true })).toBe('leave');
    expect(tableAction(base)).toBe('join');
    expect(tableAction({ ...base, isInviteOnly: true })).toBeNull();
    expect(tableAction({ ...base, isInviteOnly: true, invitedYou: true })).toBe('join');
    const playing = { ...base, status: 'PLAYING', gameId: 'g' };
    expect(tableAction({ ...playing, playing: true })).toBe('play');
    expect(tableAction({ ...playing, watchable: true })).toBe('watch');
    expect(tableAction(playing)).toBeNull();
    expect(tableAction({ ...playing, gameId: null, watchable: true })).toBeNull();
    expect(tableAction({ ...base, status: 'FINISHED', winner: 'bob' })).toBeNull();
  });

  it('writes the note after the tournament name', () => {
    expect(tableNote(base)).toBeNull();
    expect(tableNote({ ...base, isPrivate: true })).toBe('Private.');
    expect(tableNote({ ...base, description: 'go easy' })).toBe('[go easy]');
    expect(tableNote({ ...base, description: 'go easy', isPrivate: true })).toBe('Private match: [go easy]');
    expect(tableNote({ ...base, description: 'bob', isInviteOnly: true })).toBe("Match for user 'bob'.");
    expect(tableNote({ ...base, description: 'bob', isInviteOnly: true, isPrivate: true })).toBe(
      "Private match for user 'bob'.",
    );
  });

  it('formats the waiting timer', () => {
    expect(formatTableAge(-5000)).toBe('00:00');
    expect(formatTableAge(7999)).toBe('00:07');
    expect(formatTableAge(90_000)).toBe('01:30');
    expect(formatTableAge(3_599_000)).toBe('59:59');
    expect(formatTableAge(3_600_000)).toBe('1:00:00');
    expect(formatTableAge((26 * 3600 + 5 * 60 + 9) * 1000)).toBe('26:05:09');
    expect(tableAgeTitle(1794232900000)).toBe('Open since 2026-11-09 14:01:40 (server time)');
  });

  it('links the game page with the participant an admin acts as', () => {
    expect(gameUrl('g 1', null)).toBe('/gemp-lotr/game.html?gameId=g%201');
    expect(gameUrl('g1', 'bob&co')).toBe('/gemp-lotr/game.html?gameId=g1&participantId=bob%26co');
  });

  it('reads what a table call answered', () => {
    expect(parseTableCallResult(readServerResponse('hall-table-ok.xml'))).toEqual({ ok: true, message: 'OK' });
    expect(parseTableCallResult('')).toEqual({ ok: true, message: 'OK' });
    expect(parseTableCallResult(readServerResponse('hall-table-error.xml'))).toEqual({
      ok: false,
      message: "Cannot find player 'nobody'. Check your spelling and ensure the name is exact.",
    });
    expect(parseTableCallResult('<error/>')).toEqual({ ok: false, message: 'The server refused the request.' });
    expect(parseTableCallResult('<response message="You left the queue"/>')).toEqual({
      ok: true,
      message: 'You left the queue',
    });
    expect(parseTableCallResult('not xml')).toEqual({ ok: true, message: 'not xml' });
  });

  it('reads the timers and labels them', () => {
    const timers = parseHallTimers(JSON.parse(readServerResponse('hall-formats.json')).HallTimers);
    expect(timers.map(timerLabel)).toEqual([
      'Default (45m/6m)',
      'Blitz! (25m/3m)',
      'Championship (20m/10m)',
      'Slow (80m/10m)',
      'Glacial (1d/1d)',
    ]);
    expect(timerLabel({ ...timers[0], minutesPerPlayer: 120, minutesPerDecision: 60 })).toBe('Default (2h/60m)');
    expect(parseHallTimers(null)).toEqual([]);
    expect(parseHallTimers([{ name: 'no code' }, null])).toEqual([]);
  });

  it('strips the chat list prefixes from names', () => {
    expect(bareName('* admin-1')).toBe('admin-1');
    expect(bareName('+ lead_admin')).toBe('lead_admin');
    expect(bareName(' bob ')).toBe('bob');
    expect(bareName(null)).toBe('');
  });
});
