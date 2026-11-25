import { HallItemChange } from '../hall-models';
import { RowOrder } from '../hall-row-order';
import { HallQueue, HallTournament } from './hall-queue-models';
import { parseHallQueue, parseHallTournament } from './hall-queue-parsers';

/** Where a queue's section row goes: World Championship Queues, Recurring Events, Scheduled Events, or none. */
export type HallQueueSection = 'wc' | 'recurring' | 'scheduled';

interface QueueEntry {
  readonly queue: HallQueue;
  /** Its section and place there (fixed when it was added, as the legacy row stayed where it was drawn). */
  readonly section: HallQueueSection | null;
  readonly sectionOrder: number;
  /** Its place in Waiting Tables, or null while it is not shown there. */
  readonly waitingOrder: number | null;
}

interface TournamentEntry {
  readonly tournament: HallTournament;
  readonly order: number;
}

/** The section a new queue's row is drawn in (drawQueue: wc first, then recurring, then scheduled). */
export function queueSection(queue: HallQueue): HallQueueSection | null {
  if (queue.wc) {
    return 'wc';
  }
  if (queue.recurring) {
    return 'recurring';
  }
  return queue.scheduled ? 'scheduled' : null;
}

/**
 * The queues and running tournaments the hall knows, kept from the poll's changes (hallUi.js drawQueue,
 * drawTournament). A queue has a row in its section (added at the end; an update redraws it in place) and, while
 * displayInWaitingTables, one in Waiting Tables (an update redraws it in place, or adds it at the end when it was not
 * there; it goes when the flag goes). A tournament has one row in Playing Tables (an update redraws it in place, or
 * adds it). A snapshot starts over. Pure: HallBoard holds it.
 */
export class HallEventsState {
  private constructor(
    private readonly queues: ReadonlyMap<string, QueueEntry>,
    private readonly tournaments: ReadonlyMap<string, TournamentEntry>,
  ) {}

  static readonly EMPTY = new HallEventsState(new Map(), new Map());

  applyQueues(changes: readonly HallItemChange[], full: boolean, order: RowOrder): HallEventsState {
    const queues = new Map(full ? [] : this.queues);
    for (const change of changes) {
      if (change.action === 'remove') {
        queues.delete(change.id);
        continue;
      }
      const queue = parseHallQueue(change);
      const old = queues.get(change.id);
      if (change.action === 'add' || old === undefined) {
        // an update of a queue never added draws no section row (replaceWith on nothing), as in the legacy hall
        const section = change.action === 'add' ? queueSection(queue) : null;
        queues.set(change.id, {
          queue,
          section,
          sectionOrder: section === null ? 0 : order.take(),
          waitingOrder: queue.displayInWaitingTables ? order.take() : null,
        });
      } else {
        queues.set(change.id, {
          ...old,
          queue,
          waitingOrder: queue.displayInWaitingTables ? (old.waitingOrder ?? order.take()) : null,
        });
      }
    }
    return new HallEventsState(queues, full ? new Map() : this.tournaments);
  }

  applyTournaments(changes: readonly HallItemChange[], full: boolean, order: RowOrder): HallEventsState {
    const tournaments = new Map(full ? [] : this.tournaments);
    for (const change of changes) {
      if (change.action === 'remove') {
        tournaments.delete(change.id);
        continue;
      }
      const old = tournaments.get(change.id);
      tournaments.set(change.id, { tournament: parseHallTournament(change), order: old?.order ?? order.take() });
    }
    return new HallEventsState(this.queues, tournaments);
  }

  queue(id: string): HallQueue | null {
    return this.queues.get(id)?.queue ?? null;
  }

  tournament(id: string): HallTournament | null {
    return this.tournaments.get(id)?.tournament ?? null;
  }

  allQueues(): HallQueue[] {
    return [...this.queues.values()].map((entry) => entry.queue);
  }

  allTournaments(): HallTournament[] {
    return [...this.tournaments.values()].map((entry) => entry.tournament);
  }

  /** The queues of a section, with their places, in order. */
  sectionQueues(section: HallQueueSection): { queue: HallQueue; order: number }[] {
    return [...this.queues.values()]
      .filter((entry) => entry.section === section)
      .sort((a, b) => a.sectionOrder - b.sectionOrder)
      .map((entry) => ({ queue: entry.queue, order: entry.sectionOrder }));
  }

  /** The queues shown in Waiting Tables, with their places. */
  waitingQueues(): { queue: HallQueue; order: number }[] {
    return [...this.queues.values()].flatMap((entry) =>
      entry.waitingOrder === null ? [] : [{ queue: entry.queue, order: entry.waitingOrder }],
    );
  }

  /** The tournaments with their places in Playing Tables. */
  playingTournaments(): { tournament: HallTournament; order: number }[] {
    return [...this.tournaments.values()].map((entry) => ({ tournament: entry.tournament, order: entry.order }));
  }
}
