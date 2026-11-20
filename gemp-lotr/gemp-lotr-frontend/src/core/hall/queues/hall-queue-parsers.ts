import { SERVER_URL } from '../../platform/app-paths';
import { HallItemChange } from '../hall-models';
import {
  HallEventName,
  HallEventType,
  HallPrizes,
  HallQueue,
  HallQueueAction,
  HallQueueSectionRow,
  HallQueueWaitingRow,
  HallTournament,
  HallTournamentAction,
  HallTournamentRow,
} from './hall-queue-models';
import { PRIZE_BREAKDOWN, readyCheckLabel, waitingForOthersLabel } from './hall-queue-messages';

const flag = (a: Readonly<Record<string, string>>, name: string) => a[name] === 'true';
const int = (value: string | undefined, fallback: number) => {
  const parsed = parseInt(value ?? '', 10);
  return Number.isNaN(parsed) ? fallback : parsed;
};
const lower = (value: string | undefined): HallEventType => (value ?? '').toLowerCase();

/**
 * A queue's prizes. The server sends them as data beside the legacy HTML (`prizesLabel`, and `prizesLines` as a JSON
 * array of plain-text strings, "[]" for none). An older server sends only the HTML `prizes`, which is never read as
 * HTML: a breakdown there (markup) gives the label "Prize Breakdown" and no lines, plain text is the label itself.
 */
export function parsePrizes(attributes: Readonly<Record<string, string>>): HallPrizes {
  let lines: string[] = [];
  try {
    const parsed: unknown = JSON.parse(attributes['prizesLines'] ?? '');
    if (Array.isArray(parsed)) {
      lines = parsed.filter((line): line is string => typeof line === 'string');
    }
  } catch {
    lines = [];
  }
  const label = attributes['prizesLabel'];
  if (label !== undefined) {
    return { label, lines };
  }
  const old = (attributes['prizes'] ?? '').trim();
  return { label: old.startsWith('<') ? PRIZE_BREAKDOWN : old, lines };
}

/** A queue's add or update (hallUi.js drawQueue). An update carries every attribute again, so it replaces the queue. */
export function parseHallQueue(change: HallItemChange): HallQueue {
  const a = change.attributes;
  return {
    id: change.id,
    cost: int(a['cost'], 0),
    collection: a['collection'] ?? '',
    format: a['format'] ?? '',
    type: lower(a['type']),
    name: a['queue'] ?? '',
    playerCount: int(a['playerCount'], 0),
    playerList: a['playerList'] ?? '',
    prizes: parsePrizes(a),
    system: a['system'] ?? '',
    start: a['start'] ?? '',
    signedUp: flag(a, 'signedUp'),
    joinable: flag(a, 'joinable'),
    startable: flag(a, 'startable'),
    readyCheckSecsRemaining: int(a['readyCheckSecsRemaining'], -1),
    confirmedReadyCheck: flag(a, 'confirmedReadyCheck'),
    wc: flag(a, 'wc'),
    draftCode: a['draftCode'] ?? null,
    recurring: flag(a, 'recurring'),
    scheduled: flag(a, 'scheduled'),
    displayInWaitingTables: flag(a, 'displayInWaitingTables'),
  };
}

/** A tournament's add or update (hallUi.js drawTournament). */
export function parseHallTournament(change: HallItemChange): HallTournament {
  const a = change.attributes;
  return {
    id: change.id,
    collection: a['collection'] ?? '',
    format: a['format'] ?? '',
    name: a['name'] ?? '',
    system: a['system'] ?? '',
    type: lower(a['type']),
    stage: a['stage'] ?? '',
    round: int(a['round'], 0),
    playerCount: int(a['playerCount'], 0),
    playerList: a['playerList'] ?? '',
    signedUp: flag(a, 'signedUp'),
    abandoned: flag(a, 'abandoned'),
    joinable: flag(a, 'joinable'),
    wc: flag(a, 'wc'),
    timeRemaining: a['timeRemaining'] ?? null,
  };
}

const TYPE_NAMES: Readonly<Record<string, string>> = {
  sealed: 'Sealed',
  solodraft: 'Solo Draft',
  table_solodraft: 'Solo Table Draft',
  table_draft: 'Table Draft',
  constructed: 'Constructed',
};

/** "Sealed", "Solo Draft", "Table Draft", ...; an unknown type in lower case (GempLotrHallUI.displayType). */
export function displayType(type: string): string {
  const key = type.toLowerCase();
  return TYPE_NAMES[key] ?? key;
}

/** The draft format page of a table draft (GameHall.js .draftFormatInfo). */
export function draftFormatUrl(draftCode: string): string {
  return `${SERVER_URL}/deck/draftHtml?draftCode=${encodeURIComponent(draftCode)}`;
}

/** A queue's event name: a table draft's name opens its draft format (GempLotrHallUI.eventName). */
export function queueEventName(queue: HallQueue): HallEventName {
  const draft = displayType(queue.type).includes('Table') ? queue.draftCode : null;
  return { text: queue.name, draftFormatUrl: draft === null ? null : draftFormatUrl(draft) };
}

/** The entry cost as the hall writes it (common.js formatPrice): gold, then silver in two digits, each with its coin. */
export function splitPrice(price: number): { gold: number; silver: string } {
  const silver = price % 100;
  return { gold: Math.floor(price / 100), silver: String(silver).padStart(2, '0') };
}

/** The Recurring / Scheduled / World Championship row (drawQueue's `row`). */
export function queueSectionRow(queue: HallQueue): HallQueueSectionRow {
  const type = displayType(queue.type);
  const limited = type.includes('Sealed') || type.includes('Draft');
  return {
    queue,
    format: queue.format,
    collection: limited ? type : queue.collection,
    name: queueEventName(queue),
    start: queue.start,
    system: queue.system,
    cost: queue.cost,
    prizes: queue.prizes,
  };
}

/** The buttons of a queue's Waiting Tables row, in the legacy order. */
export function queueActions(queue: HallQueue): HallQueueAction[] {
  const actions: HallQueueAction[] = [];
  if (!queue.signedUp && queue.joinable) {
    actions.push({ kind: 'join' });
  } else if (queue.signedUp) {
    actions.push({ kind: 'leave' });
    if (queue.startable) {
      actions.push({ kind: 'start' });
    }
    const secs = queue.readyCheckSecsRemaining;
    if (secs > -1) {
      actions.push({
        kind: 'ready-check',
        label: queue.confirmedReadyCheck ? waitingForOthersLabel(secs) : readyCheckLabel(secs),
        confirmed: queue.confirmedReadyCheck,
      });
    }
  }
  // the Waiting Tables row is the only row player-made queues have, so it keeps its own prize button
  if (queue.prizes.label !== '' || queue.prizes.lines.length > 0) {
    actions.push({ kind: 'prizes' });
  }
  return actions;
}

/** The Waiting Tables row of a queue (drawQueue's `tablesRow`). */
export function queueWaitingRow(queue: HallQueue): HallQueueWaitingRow {
  let prefix: string;
  if (queue.wc) {
    prefix = 'World Championship - ';
  } else if (queue.start === 'When 2 players join' || queue.start === 'When 1 players join') {
    prefix = '';
  } else {
    // the system without what follows its comma (the minimum players)
    prefix = `${queue.system.split(',')[0]} Tournament - `;
  }
  return {
    queue,
    format: queue.format,
    prefix: `${prefix}${displayType(queue.type)} - `,
    name: queueEventName(queue),
    start: queue.start,
    players: queue.playerList,
    actions: queueActions(queue),
    played: queue.signedUp,
    bold: queue.wc,
  };
}

/** Stages before the draft (and the building after it), lower case as drawTournament compares them. */
const PRE_DRAFT_STAGES = ['deck-building', 'registering decks', 'awaiting kickoff'];

/** The draft page while a solo or table draft is on (drawTournament's draftUrl), else null. */
export function tournamentDraftUrl(tournament: HallTournament): string | null {
  const type = tournament.type;
  const stage = tournament.stage.toLowerCase();
  let page: string | null = null;
  if (type === 'solodraft' && PRE_DRAFT_STAGES.includes(stage)) {
    page = 'soloDraft.html';
  } else if (
    (type === 'table_solodraft' && PRE_DRAFT_STAGES.includes(stage)) ||
    (type === 'table_draft' && stage === 'drafting')
  ) {
    page = 'tableDraft.html';
  }
  return page === null ? null : `/gemp-lotr/${page}?eventId=${encodeURIComponent(tournament.id)}`;
}

/** A tournament's buttons, in the legacy order. */
export function tournamentActions(tournament: HallTournament): HallTournamentAction[] {
  const actions: HallTournamentAction[] = [];
  const draftUrl = tournamentDraftUrl(tournament);
  const stage = tournament.stage.toLowerCase();
  if (tournament.signedUp) {
    if (draftUrl !== null) {
      actions.push({ kind: 'go-to-draft', url: draftUrl });
    }
    const limited = ['sealed', 'solodraft', 'table_solodraft', 'table_draft'].includes(tournament.type);
    if (limited && (PRE_DRAFT_STAGES.includes(stage) || stage === 'paused between rounds')) {
      actions.push({ kind: 'register-deck' });
    }
    actions.push({ kind: 'abandon' });
  } else if (!tournament.abandoned && tournament.joinable) {
    actions.push({ kind: 'late-join' });
  }
  return actions;
}

/** The Playing Tables row of a running tournament (drawTournament's `tablesRow`). */
export function tournamentRow(tournament: HallTournament): HallTournamentRow {
  let label: string;
  if (tournament.wc) {
    label = 'World Championship - ';
  } else if (tournament.playerCount === 2) {
    label = 'Tournament - ';
  } else if (!tournament.playerList.includes(',')) {
    label = 'Practice - ';
  } else {
    label = `${tournament.system} Tournament - `;
  }
  let status: string;
  if (tournament.timeRemaining !== null) {
    status = `${tournament.stage} - ${tournament.timeRemaining}`;
  } else if (tournament.stage === 'Playing Games') {
    status = `${tournament.stage} - Round ${tournament.round}`;
  } else {
    status = tournament.stage;
  }
  return {
    tournament,
    format: tournament.format,
    info: `${label}${displayType(tournament.type)} - ${tournament.name}`,
    status,
    players:
      tournament.playerCount <= 8
        ? { list: tournament.playerList }
        : { count: tournament.playerCount, list: tournament.playerList },
    actions: tournamentActions(tournament),
    draftUrl: tournamentDraftUrl(tournament),
    played: tournament.signedUp,
    bold: tournament.wc,
  };
}
