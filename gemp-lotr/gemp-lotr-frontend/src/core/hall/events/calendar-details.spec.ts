import { readServerResponse } from '../../../testing/files';
import { calendarFacts, calendarPreview, previewDate } from './calendar-details';
import { parseCalendar } from './events-parsers';
import { parseJson } from '../../api/json';

const events = parseCalendar(parseJson(readServerResponse('events-calendar.json'))).events;
const byId = (id: string) => events.find((event) => event.id === id)!;

describe('calendar details', () => {
  it('previews a league: its own drawer, Go to League, signed up', () => {
    const preview = calendarPreview(byId('1700000001'));
    expect(preview).toMatchObject({
      title: 'Constructed League',
      date: '2026-11-01 to 2026-11-29',
      goLabel: 'Go to League',
      chip: null,
      signedUp: true,
      queue: null,
      body: { kind: 'league', code: '1700000001' },
    });
  });

  it('previews a tournament that has not started with its facts and the open queue', () => {
    const preview = calendarPreview(byId('wc2026-qualifier'));
    expect(preview).toMatchObject({
      date: '2026-11-21 18:00',
      goLabel: 'Go to Tournament',
      signedUp: false,
      queue: { id: 'wc2026-qualifier', name: 'WC Qualifier', type: 'CONSTRUCTED', start: 'Starts at 18:00 UTC' },
      body: { kind: 'facts' },
    });
    expect(calendarPreview(byId('daily-pc_movie-20261120')).body).toEqual({
      kind: 'tournament',
      id: 'daily-pc_movie-20261120',
    });
  });

  it('previews a projected league with its chip and no Go button', () => {
    const preview = calendarPreview(byId('weekly:3:2026-12-01'));
    expect(preview).toMatchObject({ goLabel: null, chip: 'Scheduled', body: { kind: 'facts' } });
  });

  it('dates a one-day league by its day alone', () => {
    expect(previewDate(byId('weekly:4:2026-12-01'))).toBe('2026-12-01');
  });

  it('lists the facts, the description and the serie schedule', () => {
    expect(calendarFacts(byId('1700000001'))).toEqual({
      facts: [
        { label: 'Runs', kind: 'text', value: '2026-11-01 to 2026-11-29' },
        { label: 'Cost', kind: 'price', value: { gold: 10, silver: '50' } },
      ],
      description: [
        {
          kind: 'element',
          tag: 'p',
          children: [
            { kind: 'text', text: 'Play ' },
            { kind: 'element', tag: 'b', children: [{ kind: 'text', text: 'three' }] },
            { kind: 'text', text: ' games a week.' },
          ],
        },
      ],
      series: [
        { number: 1, name: '', format: 'Fellowship Block', dates: '2026-11-01 to 2026-11-14' },
        { number: 2, name: 'Finals', format: '', dates: '2026-11-15 to 2026-11-29' },
      ],
    });
    expect(calendarFacts(byId('wc2026-qualifier')).facts).toEqual([
      { label: 'Starts', kind: 'text', value: '2026-11-21 18:00 UTC' },
      { label: 'Format', kind: 'text', value: 'PC-Movie' },
      { label: 'Cost', kind: 'price', value: { gold: 0, silver: '00' } },
    ]);
    expect(calendarFacts(byId('weekly:4:2026-12-01')).facts).toEqual([
      { label: 'Runs', kind: 'text', value: '2026-12-01' },
      { label: 'Status', kind: 'text', value: 'Cannot be created: Unknown sealed template' },
    ]);
    expect(calendarFacts(byId('weekly:3:2026-12-01')).facts[1]).toEqual({
      label: 'Status',
      kind: 'text',
      value: "Not created yet; will appear on 2026-11-24 (schedule 'Weekly Leagues')",
    });
  });
});
