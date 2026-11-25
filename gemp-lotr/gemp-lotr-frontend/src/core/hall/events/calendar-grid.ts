import { CalendarEvent, CalendarSerie } from './events-models';
import { MONTH_NAMES, cannotBeCreated, projectedTooltip, SIGNED_UP } from './events-messages';

/**
 * The calendar's month grid as data (calendarUi.js EventCalendarUI: gridRange, renderWeek, weekSegments, assignLanes,
 * barParts, tooltip). All dates are UTC days, "yyyy-MM-dd", as the server sends them.
 */

export interface YearMonth {
  readonly year: number;
  /** 1-12 */
  readonly month: number;
}

/** The days the grid shows: whole weeks, Monday on or before the 1st to Sunday on or after the last day. */
export interface GridRange {
  readonly from: string;
  readonly to: string;
}

export interface CalendarDay {
  readonly date: string;
  /** The day of the month. */
  readonly day: number;
  readonly inMonth: boolean;
  readonly today: boolean;
}

/** One part of a bar: a stretch of days, one per serie of a league that changes serie this week. */
export interface BarPart {
  readonly days: number;
  readonly text: string;
  readonly title: string;
  /** Every part after the first starts a new serie (drawn after a divider). */
  readonly serieStart: boolean;
  /** Its share of the bar, in percent (flex-basis). */
  readonly percent: number;
}

/** An event's bar in one week row. */
export interface CalendarBar {
  readonly event: CalendarEvent;
  /** 1 = Monday, as CSS grid lines count. */
  readonly column: number;
  readonly span: number;
  /** 0-based; the bar's grid row is lane + 2 (row 1 holds the day numbers). */
  readonly lane: number;
  readonly continuesBefore: boolean;
  readonly continuesAfter: boolean;
  readonly parts: readonly BarPart[];
  readonly tooltip: string;
}

export interface CalendarWeek {
  readonly days: readonly CalendarDay[];
  readonly bars: readonly CalendarBar[];
  /** How many lanes of bars the week needs (its grid rows after the day numbers). */
  readonly lanes: number;
}

export interface CalendarGrid {
  /** "November 2026" */
  readonly title: string;
  readonly weeks: readonly CalendarWeek[];
}

const DAY_MS = 86400000;

export function isoDate(date: Date): string {
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}-${String(date.getUTCDate()).padStart(2, '0')}`;
}

/** "yyyy-MM-dd" → that day's 00:00 UTC. */
export function parseIsoDate(iso: string): Date {
  const [year = NaN, month = NaN, day = NaN] = iso.split('-').map((part) => parseInt(part, 10));
  return new Date(Date.UTC(year, month - 1, day));
}

export function addDays(date: Date, days: number): Date {
  return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + days));
}

/** Days from a UTC midnight to a "yyyy-MM-dd" day. */
export function dayOffset(from: Date, toIso: string): number {
  return Math.round((parseIsoDate(toIso).getTime() - from.getTime()) / DAY_MS);
}

/** The UTC month of an instant (the calendar opens on the current one). */
export function utcMonthOf(nowMs: number): YearMonth {
  const now = new Date(nowMs);
  return { year: now.getUTCFullYear(), month: now.getUTCMonth() + 1 };
}

export function shiftMonth(month: YearMonth, delta: number): YearMonth {
  const index = month.year * 12 + (month.month - 1) + delta;
  return { year: Math.floor(index / 12), month: (index % 12) + 1 };
}

export function calendarTitle(month: YearMonth): string {
  return `${MONTH_NAMES[month.month - 1]} ${month.year}`;
}

/** gridRange: the fetched range covers the leading and trailing days of the grid too. */
export function gridRange(month: YearMonth): GridRange {
  const first = new Date(Date.UTC(month.year, month.month - 1, 1));
  const start = addDays(first, -((first.getUTCDay() + 6) % 7));
  const last = new Date(Date.UTC(month.year, month.month, 0));
  const end = addDays(last, (7 - last.getUTCDay()) % 7);
  return { from: isoDate(start), to: isoDate(end) };
}

/** The whole grid of a month: its weeks, each with its days and packed bars (render / renderWeek). */
export function buildCalendarGrid(month: YearMonth, events: readonly CalendarEvent[], todayIso: string): CalendarGrid {
  const range = gridRange(month);
  const end = parseIsoDate(range.to);
  const weeks: CalendarWeek[] = [];
  for (let weekStart = parseIsoDate(range.from); weekStart <= end; weekStart = addDays(weekStart, 7)) {
    const days: CalendarDay[] = [];
    for (let i = 0; i < 7; i++) {
      const day = addDays(weekStart, i);
      const date = isoDate(day);
      days.push({
        date,
        day: day.getUTCDate(),
        inMonth: day.getUTCMonth() + 1 === month.month,
        today: date === todayIso,
      });
    }
    const segments = weekSegments(events, days[0]?.date ?? '', days[6]?.date ?? '');
    const lanes = assignLanes(segments);
    weeks.push({
      days,
      lanes,
      bars: segments.map((segment) => ({
        event: segment.event,
        column: segment.col,
        span: segment.span,
        lane: segment.lane,
        continuesBefore: segment.continuesBefore,
        continuesAfter: segment.continuesAfter,
        parts: barParts(segment),
        tooltip: eventTooltip(segment.event),
      })),
    });
  }
  return { title: calendarTitle(month), weeks };
}

interface Segment {
  readonly event: CalendarEvent;
  readonly from: string;
  readonly to: string;
  readonly col: number;
  readonly span: number;
  readonly continuesBefore: boolean;
  readonly continuesAfter: boolean;
  lane: number;
}

/** weekSegments: each event that touches the week, clipped to it. Events without dates are drawn nowhere. */
function weekSegments(events: readonly CalendarEvent[], weekFrom: string, weekTo: string): Segment[] {
  const weekStart = parseIsoDate(weekFrom);
  const segments: Segment[] = [];
  for (const event of events) {
    if (!event.start || !event.end || event.end < weekFrom || event.start > weekTo) {
      continue;
    }
    const from = event.start < weekFrom ? weekFrom : event.start;
    const to = event.end > weekTo ? weekTo : event.end;
    const col = dayOffset(weekStart, from) + 1;
    segments.push({
      event,
      from,
      to,
      col,
      span: dayOffset(weekStart, to) + 2 - col,
      continuesBefore: event.start < weekFrom,
      continuesAfter: event.end > weekTo,
      lane: 0,
    });
  }
  return segments;
}

/** Leagues, then projected leagues, then tournaments, for bars of the same shape. */
function kindOrder(event: CalendarEvent): number {
  return event.kind === 'league' ? 0 : event.kind === 'projected' ? 1 : 2;
}

/**
 * assignLanes: greedy packing, by start, longest first, then kind; each segment into the first lane whose columns are
 * all free. Sorts the segments in place (the order the bars are drawn in) and returns the number of lanes.
 */
function assignLanes(segments: Segment[]): number {
  segments.sort((a, b) => a.col - b.col || b.span - a.span || kindOrder(a.event) - kindOrder(b.event));
  const lanes: boolean[][] = [];
  for (const segment of segments) {
    const columns = Array.from({ length: segment.span }, (_, i) => segment.col - 1 + i);
    let taken = lanes.find((lane) => columns.every((c) => !lane[c]));
    if (!taken) {
      taken = [false, false, false, false, false, false, false];
      lanes.push(taken);
    }
    for (const c of columns) {
      taken[c] = true;
    }
    const lane = lanes.indexOf(taken);
    segment.lane = lane;
  }
  return lanes.length;
}

/** The serie's own name ("Serie 2" unless an admin named it). */
export function serieLabel(series: readonly CalendarSerie[], index: number): string {
  const name = series[index]?.name ?? '';
  return name === '' ? `Serie ${index + 1}` : name;
}

/** barParts: one part per serie the bar covers this week; other events are a single part. */
function barParts(segment: Segment): BarPart[] {
  const event = segment.event;
  let label = event.name;
  if (event.kind === 'tournament' && event.startTime) {
    label = `${event.startTime} ${label}`;
  }
  if (segment.continuesBefore) {
    label = `◂ ${label}`;
  }
  const series = event.kind === 'league' ? event.series : [];
  const part = (days: number, text: string, title: string, serieStart: boolean): BarPart => ({
    days,
    text,
    title,
    serieStart,
    percent: (100 * days) / segment.span,
  });
  if (series.length < 2) {
    return [part(segment.span, label, label, false)];
  }

  const segmentStart = parseIsoDate(segment.from);
  const cuts: { day: number; index: number }[] = [];
  let currentSerie = 0;
  for (let s = 1; s < series.length; s++) {
    const serieStart = series[s]?.start;
    if (!serieStart) {
      continue;
    }
    if (serieStart <= segment.from) {
      currentSerie = s;
    } else if (serieStart <= segment.to) {
      cuts.push({ day: dayOffset(segmentStart, serieStart), index: s });
    }
  }
  // A continuation row names the serie in progress: the divider that introduced it was on an earlier row
  if (segment.continuesBefore && currentSerie > 0) {
    label += ` · ${serieLabel(series, currentSerie)}`;
  }

  const parts: BarPart[] = [];
  let from = 0;
  for (let c = 0; c <= cuts.length; c++) {
    const to = cuts[c]?.day ?? segment.span;
    const previous = cuts[c - 1];
    if (to > from) {
      if (previous === undefined) {
        parts.push(part(to - from, label, label, false));
      } else {
        const name = serieLabel(series, previous.index);
        parts.push(part(to - from, name, `${name} starts ${series[previous.index]?.start ?? ''}`, true));
      }
    }
    from = to;
  }
  return parts;
}

/** tooltip: the bar's title, one fact per line. */
export function eventTooltip(event: CalendarEvent): string {
  const lines: string[] = [];
  if (event.kind === 'tournament' && event.startTime) {
    lines.push(`${event.name} (${event.start} ${event.startTime} UTC)`);
  } else if (event.start === event.end) {
    lines.push(`${event.name} (${event.start})`);
  } else {
    lines.push(`${event.name} (${event.start} to ${event.end})`);
  }
  if (event.kind === 'projected') {
    lines.push(event.error ? cannotBeCreated(event.error) : projectedTooltip(event.createdOn, event.scheduleName));
  }
  if (event.joined) {
    lines.push(SIGNED_UP);
  }
  if (event.kind === 'league' && event.series.length > 1) {
    event.series.forEach((serie, s) => {
      if (s === 0) {
        return;
      }
      lines.push(`${serie.name || `Serie ${s + 1}`} starts ${serie.start}${serie.format ? ` (${serie.format})` : ''}`);
    });
  }
  return lines.join('\n');
}

/** Two calendar entries are the same event (refreshSelected; a bar is selected). */
export function sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): boolean {
  return a.kind === b.kind && a.id === b.id && scheduleOf(a) === scheduleOf(b);
}

function scheduleOf(event: CalendarEvent): string | null {
  return event.kind === 'tournament' ? null : event.scheduleId;
}
