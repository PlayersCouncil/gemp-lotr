import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { HallComms } from '../hall-comms.service';
import { HallUpdate } from '../hall-models';
import { byOrder, RowOrder } from '../hall-row-order';
import { HallEventsState } from '../queues/hall-events-state';
import { queueSectionRow, queueWaitingRow, tournamentRow } from '../queues/hall-queue-parsers';
import { HallTablesState } from '../tables/hall-tables-state';
import { HallBoardRow, HallBoardSections, HallSectionId } from './hall-board-models';

/** What one hall answer changed on the board: the states before and after it. */
export interface HallBoardChange {
  readonly update: HallUpdate;
  readonly before: { readonly tables: HallTablesState; readonly events: HallEventsState };
  readonly after: { readonly tables: HallTablesState; readonly events: HallEventsState };
}

/**
 * The Game Hall tab's rows (hallUi.js drawHall): every hall answer's queues, then tournaments, then tables, applied
 * in that order (a snapshot on a cleared board) so that Waiting and Playing Tables show queues, tournaments and tables
 * in the order the legacy hall drew them. The one place the rows are kept; the tables, the queues and the section
 * settings read it.
 */
@Injectable()
export class HallBoard {
  private readonly order = new RowOrder();

  private readonly tables_ = signal(HallTablesState.EMPTY);
  private readonly events_ = signal(HallEventsState.EMPTY);
  readonly tables = this.tables_.asReadonly();
  readonly events = this.events_.asReadonly();

  private readonly changes = new Subject<HallBoardChange>();
  /** Each hall answer once applied. */
  readonly changes$: Observable<HallBoardChange> = this.changes.asObservable();

  readonly sections = computed<HallBoardSections>(() => {
    const tables = this.tables_().sections();
    const events = this.events_();
    const tableRows = (rows: typeof tables.waiting): HallBoardRow[] =>
      rows.map((row) => ({ kind: 'table', key: `table:${row.table.id}`, order: row.order, row }));
    const queues: HallBoardRow[] = events.waitingQueues().map(({ queue, order }) => ({
      kind: 'queue',
      key: `queue-waiting:${queue.id}`,
      order,
      row: queueWaitingRow(queue),
    }));
    const tournaments: HallBoardRow[] = events.playingTournaments().map(({ tournament, order }) => ({
      kind: 'tournament',
      key: `tournament:${tournament.id}`,
      order,
      row: tournamentRow(tournament),
    }));
    const section = (id: 'wc' | 'scheduled' | 'recurring') =>
      events.sectionQueues(id).map(({ queue }) => queueSectionRow(queue));
    return {
      waiting: byOrder(queues, tableRows(tables.waiting)),
      playing: byOrder(tournaments, tableRows(tables.playing)),
      finished: tables.finished,
      wc: section('wc'),
      scheduled: section('scheduled'),
      recurring: section('recurring'),
    };
  });

  /** The headers' counts: "Waiting Tables (3)". */
  readonly counts = computed<Readonly<Record<HallSectionId, number>>>(() => {
    const s = this.sections();
    return {
      waiting: s.waiting.length,
      playing: s.playing.length,
      finished: s.finished.length,
      wc: s.wc.length,
      scheduled: s.scheduled.length,
      recurring: s.recurring.length,
    };
  });

  /** The sections drawn: World Championship Queues and Scheduled Events only while they have rows. */
  readonly shownSections = computed<HallSectionId[]>(() => {
    const counts = this.counts();
    return (['waiting', 'playing', 'finished', 'wc', 'scheduled', 'recurring'] as const).filter(
      (id) => (id !== 'wc' && id !== 'scheduled') || counts[id] > 0,
    );
  });

  constructor() {
    const updates = inject(HallComms).updates$.subscribe((update) => this.apply(update));
    inject(DestroyRef).onDestroy(() => updates.unsubscribe());
  }

  private apply(update: HallUpdate): void {
    const before = { tables: this.tables_(), events: this.events_() };
    const events = before.events
      .applyQueues(update.queues, update.full, this.order)
      .applyTournaments(update.tournaments, update.full, this.order);
    const tables = before.tables.apply(update.tables, update.full, this.order);
    this.events_.set(events);
    this.tables_.set(tables);
    this.changes.next({ update, before, after: { tables, events } });
  }
}
