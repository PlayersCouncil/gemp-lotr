import { HallItemChange } from '../hall-models';
import { RowOrder } from '../hall-row-order';
import { gameUrl, parseHallTable, tableAction, tableNote } from './hall-table-parsers';
import { HallTable, HallTableRow, HallTableSections } from './hall-table-models';

interface Entry {
  readonly table: HallTable;
  /** Its place in its section: rows are drawn in the order they came into the section. */
  readonly seq: number;
}

/**
 * The tables the hall knows, kept from the poll's changes (hallUi.js drawTable; the mobile hallState.js): a snapshot
 * starts over; add appends a row to its status's section; an update replaces the row in place, or, when the status
 * changed, moves it to the end of its new section; remove takes it away. Pure: HallTablesService holds it in a signal.
 */
export class HallTablesState {
  private constructor(private readonly entries: ReadonlyMap<string, Entry>) {}

  static readonly EMPTY = new HallTablesState(new Map());

  /**
   * The state after one hall answer's table changes (`full`: a snapshot, drawn on a cleared hall). Rows take their
   * places from `order` (shared with the queues and tournaments, see RowOrder).
   */
  apply(changes: readonly HallItemChange[], full: boolean, order: RowOrder): HallTablesState {
    const entries = new Map(full ? [] : this.entries);
    for (const change of changes) {
      if (change.action === 'remove') {
        entries.delete(change.id);
        continue;
      }
      const table = parseHallTable(change);
      const old = entries.get(change.id);
      const seq = old?.table.status === table.status ? old.seq : order.take();
      entries.set(change.id, { table, seq });
    }
    return new HallTablesState(entries);
  }

  get(id: string): HallTable | null {
    return this.entries.get(id)?.table ?? null;
  }

  get size(): number {
    return this.entries.size;
  }

  /** Game ids of the tables still in the hall (a started game's prompt goes when its table does). */
  liveGameIds(): Set<string> {
    const ids = new Set<string>();
    for (const { table } of this.entries.values()) {
      if (table.gameId !== null && table.status !== 'FINISHED') {
        ids.add(table.gameId);
      }
    }
    return ids;
  }

  /** The Waiting, Playing and Finished sections; a table of another status is in none (the legacy hall drew none). */
  sections(participantId: string | null): HallTableSections {
    const sorted = [...this.entries.values()].sort((a, b) => a.seq - b.seq);
    const rows = (status: string): HallTableRow[] =>
      sorted
        .filter(({ table }) => table.status === status)
        .map(({ table, seq }) => tableRow(table, participantId, seq));
    return { waiting: rows('WAITING'), playing: rows('PLAYING'), finished: rows('FINISHED') };
  }
}

export function tableRow(table: HallTable, participantId: string | null, order = 0): HallTableRow {
  const action = tableAction(table);
  return {
    order,
    table,
    action,
    note: tableNote(table),
    gameUrl: (action === 'play' || action === 'watch') && table.gameId ? gameUrl(table.gameId, participantId) : null,
  };
}
