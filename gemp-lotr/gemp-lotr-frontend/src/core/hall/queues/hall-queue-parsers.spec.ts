import { readServerXml } from '../../../testing/files';
import { parseHallUpdate } from '../hall-parsers';
import { HallItemChange } from '../hall-models';
import {
  displayType,
  parseHallQueue,
  parseHallTournament,
  parsePrizes,
  queueActions,
  queueSectionRow,
  queueWaitingRow,
  splitPrice,
  tournamentActions,
  tournamentDraftUrl,
  tournamentRow,
} from './hall-queue-parsers';
import { queueJoinedMessage, startPhrase } from './hall-queue-messages';

const snapshot = parseHallUpdate(readServerXml('hall-queues.xml'), true)!;
const queue = (id: string) => parseHallQueue(snapshot.queues.find((q) => q.id === id)!);
const tournament = (id: string) => parseHallTournament(snapshot.tournaments.find((t) => t.id === id)!);
const change = (attributes: Record<string, string>): HallItemChange => ({ action: 'add', id: 'q', attributes });

describe('the hall queue and tournament parsers', () => {
  it('reads the prizes as data, never the old HTML', () => {
    expect(queue('daily').prizes).toEqual({
      label: 'Prize Breakdown',
      lines: [
        '2+ Wins: Required for Event Awards and Tengwar cards',
        'Top 4: Get Tengwar cards (up to 4 for 1st place)',
      ],
    });
    expect(queue('wc_queue').prizes).toEqual({ label: 'No prizes', lines: [] });
    // an older server: the old attribute only
    expect(queue('pm1').prizes).toEqual({ label: 'Prize Breakdown', lines: [] });
    expect(parsePrizes({ prizes: 'No prizes' })).toEqual({ label: 'No prizes', lines: [] });
    expect(parsePrizes({ prizesLabel: 'X', prizesLines: 'not json' })).toEqual({ label: 'X', lines: [] });
    expect(parsePrizes({})).toEqual({ label: '', lines: [] });
  });

  it('types a queue', () => {
    expect(queue('pm1')).toMatchObject({
      type: 'table_draft',
      name: 'Casual FotR Live Draft',
      draftCode: 'fotr_table_draft',
      signedUp: true,
      startable: true,
      readyCheckSecsRemaining: 45,
      displayInWaitingTables: true,
    });
    expect(parseHallQueue(change({})).readyCheckSecsRemaining).toBe(-1);
  });

  it('draws the section rows: limited events show their type, a table draft links its format', () => {
    expect(queueSectionRow(queue('daily'))).toMatchObject({
      collection: 'All cards',
      name: { text: 'Daily FotR', draftFormatUrl: null },
      start: 'at 2026-11-20 19:00',
      cost: 250,
    });
    expect(queueSectionRow(queue('pm1'))).toMatchObject({
      collection: 'Table Draft',
      name: { draftFormatUrl: '/gemp-lotr-server/deck/draftHtml?draftCode=fotr_table_draft' },
    });
    expect(splitPrice(250)).toEqual({ gold: 2, silver: '50' });
    expect(splitPrice(5)).toEqual({ gold: 0, silver: '05' });
  });

  it('draws the Waiting Tables rows with the legacy prefixes and buttons', () => {
    expect(queueWaitingRow(queue('wc_queue'))).toMatchObject({
      prefix: 'World Championship - Constructed - ',
      players: 'alice, bob',
      played: true,
      bold: true,
    });
    expect(queueWaitingRow(queue('pm1')).prefix).toBe('Swiss Tournament - Table Draft - ');
    expect(queueWaitingRow(parseHallQueue(change({ start: 'When 2 players join', type: 'sealed' }))).prefix).toBe(
      'Sealed - ',
    );
    expect(queueActions(queue('pm1'))).toEqual([
      { kind: 'leave' },
      { kind: 'start' },
      { kind: 'ready-check', label: 'READY CHECK - 45 s', confirmed: false },
      { kind: 'prizes' },
    ]);
    expect(queueActions(queue('daily'))).toEqual([{ kind: 'join' }, { kind: 'prizes' }]);
    const confirmed = parseHallQueue(
      change({ signedUp: 'true', readyCheckSecsRemaining: '9', confirmedReadyCheck: 'true' }),
    );
    expect(queueActions(confirmed)).toEqual([
      { kind: 'leave' },
      { kind: 'ready-check', label: 'Waiting for others - 9 s', confirmed: true },
    ]);
  });

  it('draws the tournaments with their labels, status and buttons', () => {
    expect(tournamentRow(tournament('t1'))).toMatchObject({
      info: 'Swiss Tournament - Constructed - Casual Fellowship Block',
      status: 'Playing Games - Round 2',
      players: { list: 'alice, bob, carol, dave' },
      actions: [{ kind: 'abandon' }],
      draftUrl: null,
    });
    expect(tournamentRow(tournament('t2'))).toMatchObject({
      info: 'Swiss Tournament - Solo Draft - Casual FotR Solo Draft',
      status: 'Deck-building - 12 minutes',
      players: { count: 9 },
      draftUrl: '/gemp-lotr/soloDraft.html?eventId=t2',
    });
    expect(tournamentActions(tournament('t2'))).toEqual([
      { kind: 'go-to-draft', url: '/gemp-lotr/soloDraft.html?eventId=t2' },
      { kind: 'register-deck' },
      { kind: 'abandon' },
    ]);
    expect(tournamentRow(tournament('t3'))).toMatchObject({ info: 'Tournament - Constructed - Casual Movie Block' });
    expect(tournamentActions(tournament('t3'))).toEqual([{ kind: 'late-join' }]);
    const tableDraft = (stage: string) =>
      tournamentDraftUrl(parseHallTournament({ action: 'add', id: 'd', attributes: { type: 'TABLE_DRAFT', stage } }));
    expect(tableDraft('Drafting')).toBe('/gemp-lotr/tableDraft.html?eventId=d');
    expect(tableDraft('Deck-building')).toBeNull();
    expect(displayType('weird')).toBe('weird');
  });

  it('words the queue confirmation with the server values escaped', () => {
    expect(startPhrase('When 8 players join')).toBe('when 8 players join');
    expect(startPhrase('2026-09-24')).toBe('at 2026-09-24');
    expect(queueJoinedMessage('constructed', 'A<b>', 'Elves', 'at 18:00')).toContain(
      "You will use a snapshot of your '<b>Elves</b>' deck",
    );
    expect(queueJoinedMessage('constructed', 'A<b>', 'Elves', 'at 18:00')).toContain('<b>A&lt;b&gt;</b>');
    expect(queueJoinedMessage('table_draft', 'X', null, 'at 18:00')).toContain('Draft begins at at 18:00');
  });
});
