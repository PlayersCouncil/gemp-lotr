import { HallQueueSectionRow, HallQueueWaitingRow, HallTournamentRow } from '../queues/hall-queue-models';
import { HallTableRow } from '../tables/hall-table-models';

/** The Game Hall tab's collapsible sections, in the order they are drawn. */
export type HallSectionId = 'waiting' | 'playing' | 'finished' | 'wc' | 'scheduled' | 'recurring';

export const HALL_SECTION_ORDER: readonly HallSectionId[] = [
  'waiting',
  'playing',
  'finished',
  'wc',
  'scheduled',
  'recurring',
];

/** The sections' headers (hall.html, hallUi.js addWcQueuesSection / addScheduledQueuesSection). */
export const HALL_SECTION_TITLES: Readonly<Record<HallSectionId, string>> = {
  waiting: 'Waiting Tables',
  playing: 'Playing Tables',
  finished: 'Finished Tables',
  wc: 'World Championship Queues',
  scheduled: 'Scheduled Events',
  recurring: 'Recurring Events',
};

/** A column of a section's table: its header and its width (the legacy `<th width>`). */
export interface HallColumn {
  readonly label: string;
  readonly widthPercent: number;
}

const tableColumns = (last: string): HallColumn[] => [
  { label: 'Format', widthPercent: 20 },
  { label: 'Table Info', widthPercent: 40 },
  { label: 'Status', widthPercent: 10 },
  { label: 'Players', widthPercent: 20 },
  { label: last, widthPercent: 10 },
];
const queueColumns = (start: string): HallColumn[] => [
  { label: 'Format', widthPercent: 10 },
  { label: 'Collection', widthPercent: 8 },
  { label: 'Event Name', widthPercent: 30 },
  { label: start, widthPercent: 16 },
  { label: 'System', widthPercent: 16 },
  { label: 'Cost', widthPercent: 8 },
  { label: 'Prizes', widthPercent: 12 },
];

export const HALL_SECTION_COLUMNS: Readonly<Record<HallSectionId, readonly HallColumn[]>> = {
  waiting: tableColumns('Actions'),
  playing: tableColumns('Actions'),
  finished: tableColumns('Winner'),
  wc: queueColumns('Starts'),
  scheduled: queueColumns('Starts'),
  recurring: queueColumns('Next Start'),
};

/** A row of Waiting or Playing Tables: a game table, a queue (Waiting) or a running tournament (Playing). */
export type HallBoardRow =
  | { readonly kind: 'table'; readonly key: string; readonly order: number; readonly row: HallTableRow }
  | { readonly kind: 'queue'; readonly key: string; readonly order: number; readonly row: HallQueueWaitingRow }
  | { readonly kind: 'tournament'; readonly key: string; readonly order: number; readonly row: HallTournamentRow };

/** Everything the Game Hall tab's sections draw. */
export interface HallBoardSections {
  readonly waiting: readonly HallBoardRow[];
  readonly playing: readonly HallBoardRow[];
  readonly finished: readonly HallTableRow[];
  readonly wc: readonly HallQueueSectionRow[];
  readonly scheduled: readonly HallQueueSectionRow[];
  readonly recurring: readonly HallQueueSectionRow[];
}
