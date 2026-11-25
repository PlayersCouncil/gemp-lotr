import { MarkupNode } from '../../chat/chat-markup';
import { CalendarEvent, CalendarQueueOffer } from './events-models';
import { Price } from '../price';
import {
  cannotBeCreated,
  FACT_COST,
  FACT_ENTRY,
  FACT_FORMAT,
  FACT_RUNS,
  FACT_STARTS,
  FACT_STATUS,
  GO_TO_LEAGUE,
  GO_TO_TOURNAMENT,
  INVITE_ONLY,
  KIND_LABELS,
  projectedStatus,
} from './events-messages';

/**
 * What the calendar's details panel shows for the event clicked (calendarUi.js renderPreview, renderFacts,
 * previewDate, joinQueueButton).
 */

/** One "label value" line of the facts. A cost is a price, drawn with the gold and silver images. */
export type CalendarFact =
  | { readonly label: string; readonly kind: 'text'; readonly value: string }
  | { readonly label: string; readonly kind: 'price'; readonly value: Price };

/** A row of the serie schedule table: number, name (blank when it is just "Serie n"), format, dates. */
export interface CalendarSerieRow {
  readonly number: number;
  readonly name: string;
  readonly format: string;
  readonly dates: string;
}

/** renderFacts: the facts, the description and the serie schedule (the panel's content where no drawer can be). */
export interface CalendarFacts {
  readonly facts: readonly CalendarFact[];
  readonly description: readonly MarkupNode[];
  readonly series: readonly CalendarSerieRow[];
}

/**
 * What the preview's drawer holds: the league's or the tournament's own detail (with these facts in its place
 * when it cannot be fetched), or the facts straight away (a tournament that has not started, a projected league).
 */
export type CalendarPreviewBody =
  | { readonly kind: 'league'; readonly code: string }
  | { readonly kind: 'tournament'; readonly id: string }
  | { readonly kind: 'facts' };

/** The preview: a header row like the Current Leagues / Tournaments rows, with its drawer open under it. */
export interface CalendarPreview {
  readonly event: CalendarEvent;
  readonly title: string;
  /** On the right: "start to end" for a league, date and time for a tournament. */
  readonly date: string;
  /** "Go to League" / "Go to Tournament"; none for a projected league. */
  readonly goLabel: string | null;
  /** The kind's chip, for a projected league only (the others' rows say what they are). */
  readonly chip: string | null;
  readonly signedUp: boolean;
  /** The hall's Join Queue, for a scheduled tournament whose queue is open and the viewer is not in. */
  readonly queue: CalendarQueueOffer | null;
  readonly body: CalendarPreviewBody;
  readonly facts: CalendarFacts;
}

export function calendarPreview(event: CalendarEvent): CalendarPreview {
  return {
    event,
    title: event.name,
    date: previewDate(event),
    goLabel: event.kind === 'league' ? GO_TO_LEAGUE : event.kind === 'tournament' ? GO_TO_TOURNAMENT : null,
    chip: event.kind === 'projected' ? KIND_LABELS.projected : null,
    signedUp: event.joined,
    queue: event.kind === 'tournament' && !event.joined ? event.queue : null,
    body:
      event.kind === 'league'
        ? { kind: 'league', code: event.id }
        : event.kind === 'tournament' && event.started
          ? { kind: 'tournament', id: event.id }
          : { kind: 'facts' },
    facts: calendarFacts(event),
  };
}

/** previewDate */
export function previewDate(event: CalendarEvent): string {
  if (event.kind === 'tournament') {
    return event.start + (event.startTime ? ` ${event.startTime}` : '');
  }
  if (!event.end || event.end === event.start) {
    return event.start;
  }
  return `${event.start} to ${event.end}`;
}

/** renderFacts */
export function calendarFacts(event: CalendarEvent): CalendarFacts {
  const facts: CalendarFact[] = [];
  const text = (label: string, value: string) => facts.push({ label, kind: 'text', value });
  if (event.kind === 'tournament') {
    text(FACT_STARTS, `${event.start} ${event.startTime ? `${event.startTime} UTC` : ''}`);
    if (event.format) {
      text(FACT_FORMAT, event.format);
    }
  } else {
    text(FACT_RUNS, event.start === event.end ? event.start : `${event.start} to ${event.end}`);
  }
  if (event.kind !== 'projected' && event.cost !== null) {
    facts.push({ label: FACT_COST, kind: 'price', value: event.cost });
  }
  if (event.kind === 'league' && event.inviteOnly) {
    text(FACT_ENTRY, INVITE_ONLY);
  }
  if (event.kind === 'projected') {
    text(
      FACT_STATUS,
      event.error ? cannotBeCreated(event.error) : projectedStatus(event.createdOn, event.scheduleName),
    );
  }
  const series = event.kind === 'league' ? event.series : [];
  return {
    facts,
    description: event.kind === 'league' ? event.description : [],
    series: series.map((serie, i) => ({
      number: i + 1,
      name: serie.name && serie.name !== `Serie ${i + 1}` ? serie.name : '',
      format: serie.format ?? '',
      dates: `${serie.start} to ${serie.end}`,
    })),
  };
}
