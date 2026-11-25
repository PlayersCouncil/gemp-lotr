import { parseXml } from '../api/xml';
import { readServerXml } from '../../testing/files';
import { formatServerTime, parseHallUpdate } from './hall-parsers';

describe('parseHallUpdate', () => {
  it('reads a snapshot: channel, pocket, clock, motd and the rows as sent', () => {
    const update = parseHallUpdate(readServerXml('hall.xml'), true)!;
    expect(update.full).toBe(true);
    expect(update.channelNumber).toBe(7);
    expect(update.currency).toBe(1234);
    expect(update.serverTime).toBe('2026-11-09 14:03:05');
    expect(update.serverTimeMs).toBe(1794232985000);
    expect(update.motd).toBe('Welcome <b>back</b>!');
    expect(update.shutdown).toBe(false);
    expect(update.queues.map((q) => [q.action, q.id])).toEqual([['add', 'fotr_queue']]);
    expect(update.queues[0].attributes['queue']).toBe('FotR Block Daily');
    expect(update.queues[0].attributes['action']).toBeUndefined();
    expect(update.tournaments.map((t) => t.id)).toEqual(['t1']);
    expect(update.tables.map((t) => [t.action, t.id, t.attributes['status']])).toEqual([
      ['add', '11', 'WAITING'],
      ['add', '12', 'PLAYING'],
    ]);
    expect(update.newGames).toEqual(['g-9']);
  });

  it('reads an update: changes, removals, shutdown, no motd', () => {
    const update = parseHallUpdate(readServerXml('hall-update.xml'), false)!;
    expect(update.full).toBe(false);
    expect(update.motd).toBeNull();
    expect(update.shutdown).toBe(true);
    expect(update.tables).toEqual([
      {
        action: 'update',
        id: '11',
        attributes: {
          status: 'PLAYING',
          statusDescription: 'Playing',
          format: 'Fellowship Block',
          players: 'alice,erin',
          playing: 'true',
          gameId: 'g-11',
          watchable: 'true',
        },
      },
      { action: 'remove', id: '12', attributes: {} },
    ]);
    expect(update.queues).toEqual([{ action: 'remove', id: 'fotr_queue', attributes: {} }]);
    expect(update.newGames).toEqual(['g-11', 'g-9']);
  });

  it('leaves out rows with an unknown action or no id, and reads missing numbers as null', () => {
    const update = parseHallUpdate(
      parseXml('<hall><table action="move" id="1"/><table action="add"/><newGame/><table action="add" id="2"/></hall>'),
      false,
    )!;
    expect(update.tables.map((t) => t.id)).toEqual(['2']);
    expect(update.newGames).toEqual([]);
    expect([update.channelNumber, update.currency, update.serverTimeMs]).toEqual([null, null, null]);
  });

  it('gives null for any other root', () => {
    expect(parseHallUpdate(parseXml('<error message="x"/>'), true)).toBeNull();
  });
});

describe('formatServerTime', () => {
  it('writes epoch ms like the server clock, in GMT', () => {
    expect(formatServerTime(1794232985000)).toBe('2026-11-09 14:03:05');
    expect(formatServerTime(Date.UTC(2026, 0, 2, 3, 4, 5))).toBe('2026-01-02 03:04:05');
  });
});
