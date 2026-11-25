import { readServerResponse } from '../../../testing/files';
import {
  buildCalendarGrid,
  calendarTitle,
  eventTooltip,
  gridRange,
  sameCalendarEvent,
  shiftMonth,
  utcMonthOf,
} from './calendar-grid';
import { CalendarEvent, CalendarLeague, CalendarTournament } from './events-models';
import { parseCalendar } from './events-parsers';
import { parseJson } from '../../api/json';

const events = parseCalendar(parseJson(readServerResponse('events-calendar.json'))).events;
const byId = (id: string) => events.find((event) => event.id === id)!;

function league(id: string, start: string, end: string, series: CalendarLeague['series'] = []): CalendarLeague {
  return {
    kind: 'league',
    id,
    name: id,
    start,
    end,
    joined: false,
    leagueType: null,
    inviteOnly: false,
    cost: null,
    description: [],
    scheduleId: null,
    series,
  };
}

describe('calendar grid', () => {
  it('shows whole weeks, Monday to Sunday, around the month', () => {
    expect(gridRange({ year: 2026, month: 11 })).toEqual({ from: '2026-10-26', to: '2026-12-06' });
    // February 2027 starts on a Monday and ends on a Sunday: no leading or trailing days
    expect(gridRange({ year: 2027, month: 2 })).toEqual({ from: '2027-02-01', to: '2027-02-28' });
  });

  it('steps months across years and names them', () => {
    expect(shiftMonth({ year: 2026, month: 12 }, 1)).toEqual({ year: 2027, month: 1 });
    expect(shiftMonth({ year: 2026, month: 1 }, -1)).toEqual({ year: 2025, month: 12 });
    expect(calendarTitle({ year: 2026, month: 11 })).toBe('November 2026');
    expect(utcMonthOf(Date.UTC(2026, 10, 30, 23, 59))).toEqual({ year: 2026, month: 11 });
  });

  it('lays out the days, marking the other months and today', () => {
    const grid = buildCalendarGrid({ year: 2026, month: 11 }, [], '2026-11-20');
    expect(grid.title).toBe('November 2026');
    expect(grid.weeks).toHaveLength(6);
    expect(grid.weeks[0]!.days[0]).toEqual({ date: '2026-10-26', day: 26, inMonth: false, today: false });
    expect(grid.weeks[3]!.days[4]).toEqual({ date: '2026-11-20', day: 20, inMonth: true, today: true });
    expect(grid.weeks[0]!.lanes).toBe(0);
  });

  it('draws an event as one bar per week, marked where it continues', () => {
    const grid = buildCalendarGrid({ year: 2026, month: 11 }, [byId('1700000001')], '2026-11-20');
    const bars = grid.weeks.map((week) =>
      week.bars.map((bar) => [bar.column, bar.span, bar.continuesBefore, bar.continuesAfter]),
    );
    expect(bars).toEqual([
      [[7, 1, false, true]], // Sunday 1 November
      [[1, 7, true, true]],
      [[1, 7, true, true]],
      [[1, 7, true, true]],
      [[1, 7, true, false]], // to Sunday 29
      [],
    ]);
  });

  it('splits a league bar where a serie starts and names the serie in progress on later rows', () => {
    const grid = buildCalendarGrid({ year: 2026, month: 11 }, [byId('1700000001')], '2026-11-20');
    // the week of 9-15 November: Serie 1 to the 14th, Finals from the 15th
    expect(grid.weeks[2]!.bars[0]!.parts).toEqual([
      { days: 6, text: '◂ Constructed League', title: '◂ Constructed League', serieStart: false, percent: 600 / 7 },
      { days: 1, text: 'Finals', title: 'Finals starts 2026-11-15', serieStart: true, percent: 100 / 7 },
    ]);
    expect(grid.weeks[3]!.bars[0]!.parts.map((part) => part.text)).toEqual(['◂ Constructed League · Finals']);
  });

  it('puts the start time before a tournament name', () => {
    const grid = buildCalendarGrid({ year: 2026, month: 11 }, [byId('wc2026-qualifier')], '2026-11-20');
    expect(grid.weeks[3]!.bars[0]!.parts[0]!.text).toBe('18:00 WC Qualifier');
  });

  it('packs bars into lanes: by start, longest first, then leagues before projected before tournaments', () => {
    const tournament: CalendarTournament = {
      ...(byId('wc2026-qualifier') as CalendarTournament),
      start: '2026-11-16',
      end: '2026-11-16',
    };
    const week = buildCalendarGrid(
      { year: 2026, month: 11 },
      [
        tournament,
        league('short', '2026-11-16', '2026-11-17'),
        league('long', '2026-11-16', '2026-11-20'),
        league('later', '2026-11-19', '2026-11-22'),
      ],
      '2026-11-20',
    ).weeks[3]!;
    expect(week.bars.map((bar) => [bar.event.id, bar.lane])).toEqual([
      ['long', 0],
      ['short', 1],
      ['wc2026-qualifier', 2],
      ['later', 1],
    ]);
    expect(week.lanes).toBe(3);
  });

  it('draws events without dates nowhere', () => {
    const grid = buildCalendarGrid({ year: 2026, month: 11 }, [league('none', '', '')], '2026-11-20');
    expect(grid.weeks.every((week) => week.bars.length === 0)).toBe(true);
  });

  it('writes the tooltip as the legacy page did', () => {
    expect(eventTooltip(byId('1700000001'))).toBe(
      'Constructed League (2026-11-01 to 2026-11-29)\nYou are signed up\nFinals starts 2026-11-15',
    );
    expect(eventTooltip(byId('wc2026-qualifier'))).toBe('WC Qualifier (2026-11-21 18:00 UTC)');
    expect(eventTooltip(byId('weekly:3:2026-12-01'))).toBe(
      "December League (2026-12-01 to 2026-12-28)\nWill be created on 2026-11-24 by schedule 'Weekly Leagues'",
    );
    expect(eventTooltip(byId('weekly:4:2026-12-01'))).toBe(
      'Broken League (2026-12-01)\nCannot be created: Unknown sealed template',
    );
  });

  it('knows an event again by kind, id and schedule', () => {
    const event = byId('1700000001');
    expect(sameCalendarEvent(event, { ...event, name: 'Renamed' } as CalendarEvent)).toBe(true);
    expect(sameCalendarEvent(event, { ...event, scheduleId: 'other' } as CalendarEvent)).toBe(false);
  });
});
