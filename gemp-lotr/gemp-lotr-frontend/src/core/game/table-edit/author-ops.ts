/**
 * The on-table edit controls' changes (EditOp, the sandbox's vocabulary) said as the Board tab says them, for an
 * author (AuthorBackend): each one becomes what the tab's own forms gather or send for the same change, so that it ends
 * in the draft exactly as if made there. A card put in or taken out is the tab's "Add a card" and ×, which write the
 * scenario's cards and the setup, whichever board changes go to (the file has no board change that adds a card);
 * everything else is a board change the tab gathers (`wound(aragorn, 1)`), done to the board as it stands (the step's
 * `do`) or to the one the scenario starts with (the setup), as the tab's "Changes are made" says. Laying a site is the
 * start's path (setup.path) or a site of a seat's adventure path. What the tab has no way to do is refused, in words.
 * Pure.
 */
import { BoardArg, movesFor, addPlacesFor } from '../authoring/board-queue';
import { modifyArgs, unmodifyArgs } from '../authoring/board-modifiers';
import { BoardSite, DraftSeat } from '../authoring/authoring-state';
import { cardById, EditBoard, EditCard } from './edit-board';
import { EditOp, EditSeat } from './edit-ops';
import { OWN_TOKENS } from './author-board';

/** One thing the Board tab does for a change. */
export type AuthorStep =
  /** A board change gathered (BoardChanges.queue). */
  | { kind: 'queue'; name: string; args: readonly BoardArg[] }
  /**
   * A card added (BoardChanges.add): `zone` as the tab's place select has it ("" the bottom of the deck, "attach" or
   * "stack" on `host`); `then` a place it is moved to straight after (the top of the deck, out of the game). `name`
   * and `type` when known (a copy); else the card is looked up by its blueprint, as the tab's search finds it.
   */
  | {
      kind: 'add';
      blueprint: string;
      seat: EditSeat;
      zone: string;
      host: string | null;
      then: string | null;
      name: string | null;
      type: string | null;
    }
  /** A card taken out of the scenario (BoardChanges.drop). */
  | { kind: 'drop'; alias: string }
  /** The site laid at a place of the path the scenario starts with (BoardChanges.setPathSite; "" the own). */
  | { kind: 'path'; number: number; alias: string }
  /** A site of a seat's adventure path put in (BoardChanges.changeSite: at once, a fresh game). */
  | { kind: 'site'; seat: EditSeat; at: number; blueprint: string };

/** What the steps are worked out from. */
export interface AuthorContext {
  board: EditBoard;
  /** Changes go to the board the scenario starts with. */
  inSetup: boolean;
  /** The game plays its own opening (B10): the start says only each deck's order. */
  realOpening: boolean;
  /** Each seat's adventure path as the scenario has it. */
  sites: Readonly<Record<DraftSeat, readonly BoardSite[]>>;
  human: EditSeat;
  /** The markers keep together (the Shadow marker is not put by hand). */
  markersTogether: boolean;
  /** How many tokens the setup puts on a card. */
  setupTokens: (alias: string) => number;
}

export type AuthorSteps = { steps: AuthorStep[] } | { refused: string };

/** Said for what the controls offer and the tab has no way to do. */
export const AUTHOR_REFUSALS = {
  unknown: 'That card is not one of the scenario’s.',
  slot: 'A slot’s card: change it in the slot’s form (Board, Slots).',
  ringBearer: 'The Ring-bearer: choose another first (Board, the seat’s Ring-bearer).',
  ring: 'The One Ring: its seat’s Ring is chosen under Board.',
  seat: 'A card moves within its own seat.',
  deckOnly: 'The game plays its own opening: the start says only each deck’s order.',
  freepsMinions: 'The start has no Free Peoples minions on the table.',
  place: 'The Board tab cannot put a card there.',
  ownTokens: 'Only tokens of its own culture can be put on it.',
  startTokens: 'The start does not have that many tokens on it.',
  laidNow: 'A site is laid by moving a marker to it, at the board as it stands.',
  noSite: 'That site is not one of the scenario’s.',
  siteOrder: 'Sites are added to a path in order.',
  together: 'The markers keep together: the Shadow marker follows the fellowship’s.',
  pick: 'A puzzle’s slots are filled in its solutions.',
  word: 'That is not something the card can be modified in.',
  modifier: 'That modifier is not on it.',
  mixed: 'One change at a time.',
} as const;

const refuse = (said: string): AuthorSteps => ({ refused: said });

/** The card an edit names: by its card id or its name; null if the author's board does not have it. */
function named(board: EditBoard, op: { cardId?: number; card?: string }): EditCard | null {
  if (op.cardId !== undefined) return cardById(board, String(op.cardId));
  return board.cards.find((card) => card.alias === op.card) ?? null;
}

/** The place an add or a move names (`freeps.deck.top`), split. */
function target(to: string): { seat: EditSeat; zone: string } | null {
  const at = to.indexOf('.');
  const seat = to.slice(0, at);
  if (seat !== 'freeps' && seat !== 'shadow') return null;
  return { seat, zone: to.slice(at + 1) };
}

/** Why the tab could not put a card of the seat there now (null: it could). */
function addProblem(context: AuthorContext, seat: EditSeat, zone: string, hosts: boolean): string | null {
  const places = addPlacesFor(seat, context.realOpening, hosts).map(([place]) => place);
  if (places.includes(zone)) return null;
  if (context.realOpening) return AUTHOR_REFUSALS.deckOnly;
  if (seat === 'freeps' && zone === 'minions') return AUTHOR_REFUSALS.freepsMinions;
  return AUTHOR_REFUSALS.place;
}

/** Why the tab could not move a card of the seat to the zone now (null: it could). */
function moveProblem(context: AuthorContext, seat: EditSeat, zone: string): string | null {
  const deckOnly = context.realOpening && context.inSetup;
  if (movesFor(seat, context.inSetup, deckOnly).includes(zone)) return null;
  if (deckOnly) return AUTHOR_REFUSALS.deckOnly;
  if (seat === 'freeps' && zone === 'minions') return AUTHOR_REFUSALS.freepsMinions;
  return AUTHOR_REFUSALS.place;
}

/** A card added to a place: the add-card form's place, and a move straight after for the places it has not. */
function addAt(
  context: AuthorContext,
  blueprint: string,
  seat: EditSeat,
  zone: string,
  known: { name: string; type: string } | null,
): AuthorSteps {
  const base = {
    kind: 'add' as const,
    blueprint,
    seat,
    host: null,
    name: known?.name ?? null,
    type: known?.type ?? null,
  };
  if (zone === 'deck.bottom' || zone === 'deck') return { steps: [{ ...base, zone: '', then: null }] };
  if (zone === 'deck.top' || zone === 'removed') {
    const problem = moveProblem(context, seat, zone);
    return problem !== null ? refuse(problem) : { steps: [{ ...base, zone: '', then: `${seat}.${zone}` }] };
  }
  const problem = addProblem(context, seat, zone, false);
  return problem !== null ? refuse(problem) : { steps: [{ ...base, zone, then: null }] };
}

/** A card added on another (attached to it, stacked on it). */
function addOn(
  context: AuthorContext,
  blueprint: string,
  seat: EditSeat,
  how: 'attach' | 'stack',
  host: EditCard | null,
  known: { name: string; type: string } | null,
): AuthorSteps {
  if (host === null || host.slot !== undefined)
    return refuse(host === null ? AUTHOR_REFUSALS.unknown : AUTHOR_REFUSALS.slot);
  const problem = addProblem(context, seat, how, true);
  if (problem !== null) return refuse(problem);
  return {
    steps: [
      {
        kind: 'add',
        blueprint,
        seat,
        zone: how,
        host: host.alias,
        then: null,
        name: known?.name ?? null,
        type: known?.type ?? null,
      },
    ],
  };
}

/** A copy of a card: another of it, added where it is. */
function copyOf(context: AuthorContext, card: EditCard): AuthorSteps {
  const known = { name: card.name, type: card.type };
  const host = card.on === null ? null : (context.board.cards.find((one) => one.alias === card.on) ?? null);
  if (card.zone === 'attached') return addOn(context, card.blueprintId, card.owner, 'attach', host, known);
  if (card.zone === 'stacked') return addOn(context, card.blueprintId, card.owner, 'stack', host, known);
  return addAt(context, card.blueprintId, card.owner, card.zone, known);
}

/** The steps of an edit about one card. */
function ofACard(context: AuthorContext, op: EditOp & { cardId?: number; card?: string }, card: EditCard): AuthorSteps {
  const queue = (name: string, args: readonly BoardArg[]): AuthorSteps => ({ steps: [{ kind: 'queue', name, args }] });
  const host = (id: number) => cardById(context.board, String(id));
  switch (op.op) {
    case 'copy':
      return card.ring ? refuse(AUTHOR_REFUSALS.ring) : copyOf(context, card);
    case 'delete':
      if (card.ringBearer) return refuse(AUTHOR_REFUSALS.ringBearer);
      if (card.ring) return refuse(AUTHOR_REFUSALS.ring);
      return { steps: [{ kind: 'drop', alias: card.alias }] };
    case 'move': {
      const to = target(op.to);
      if (to === null) return refuse(AUTHOR_REFUSALS.place);
      if (to.seat !== card.owner) return refuse(AUTHOR_REFUSALS.seat);
      const problem = moveProblem(context, to.seat, to.zone);
      return problem !== null ? refuse(problem) : queue('move', [card.alias, op.to]);
    }
    case 'attach':
    case 'stack': {
      const on = host(op.toId);
      if (on === null) return refuse(AUTHOR_REFUSALS.unknown);
      if (on.slot !== undefined) return refuse(AUTHOR_REFUSALS.slot);
      if (context.realOpening && context.inSetup) return refuse(AUTHOR_REFUSALS.deckOnly);
      return queue(op.op, [card.alias, on.alias]);
    }
    case 'wounds':
      if (op.amount === 0) return { steps: [] };
      return queue(op.amount > 0 ? 'wound' : 'heal', [card.alias, Math.abs(op.amount)]);
    case 'tokens':
      if (op.culture !== undefined && op.culture !== OWN_TOKENS && op.culture !== card.culture) {
        return refuse(AUTHOR_REFUSALS.ownTokens);
      }
      if (op.amount === 0) return { steps: [] };
      if (op.amount < 0 && context.inSetup && context.setupTokens(card.alias) < -op.amount) {
        return refuse(AUTHOR_REFUSALS.startTokens);
      }
      return queue('tokens', [card.alias, op.amount]);
    case 'modify': {
      const word = context.board.modify.words.find((one) => one.what === op.what);
      if (word === undefined) return refuse(AUTHOR_REFUSALS.word);
      const made = modifyArgs(card.alias, word, op.amount === undefined ? '' : String(op.amount), op.until ?? '');
      return 'problem' in made ? refuse(made.problem) : queue('modify', made.args);
    }
    case 'unmodify':
      return queue('unmodify', unmodifyArgs(card.alias, 'what' in op ? (op.what ?? null) : null));
    default:
      return refuse(AUTHOR_REFUSALS.place);
  }
}

/** A total set, or changed by an amount (the tab sets it: to what it comes to). */
function total(context: AuthorContext, op: Extract<EditOp, { op: 'twilight' | 'burdens' | 'threats' }>): AuthorSteps {
  const now = context.board[op.op];
  const value = 'set' in op ? op.set : now + op.amount;
  return { steps: [{ kind: 'queue', name: op.op, args: [Math.max(0, value)] }] };
}

/** A site laid on the path at a place. */
function laid(context: AuthorContext, op: Extract<EditOp, { op: 'site' }>): AuthorSteps {
  if ('blueprint' in op) {
    const seat = op.seat ?? context.human;
    const path = context.sites[seat];
    if (op.number < 1 || op.number > path.length + 1) return refuse(AUTHOR_REFUSALS.siteOrder);
    return { steps: [{ kind: 'site', seat, at: op.number - 1, blueprint: op.blueprint }] };
  }
  if (!context.inSetup) return refuse(AUTHOR_REFUSALS.laidNow);
  if (context.realOpening) return refuse(AUTHOR_REFUSALS.deckOnly);
  if ('from' in op) {
    // (the Free Peoples seat's own site is what the start lays when it says nothing: "")
    if (op.from === 'freeps') return { steps: [{ kind: 'path', number: op.number, alias: '' }] };
    const own = context.sites[op.from][op.number - 1]?.alias ?? null;
    return own === null ? refuse(AUTHOR_REFUSALS.noSite) : { steps: [{ kind: 'path', number: op.number, alias: own }] };
  }
  const known = (['freeps', 'shadow'] as const).some((seat) =>
    context.sites[seat].some((site) => site.alias === op.card),
  );
  return known ? { steps: [{ kind: 'path', number: op.number, alias: op.card }] } : refuse(AUTHOR_REFUSALS.noSite);
}

/** The steps of one edit. */
export function authorStep(context: AuthorContext, op: EditOp): AuthorSteps {
  switch (op.op) {
    case 'add': {
      const seat: EditSeat = 'to' in op ? (target(op.to)?.seat ?? context.human) : (op.seat ?? context.human);
      if ('to' in op) {
        const to = target(op.to);
        return to === null ? refuse(AUTHOR_REFUSALS.place) : addAt(context, op.blueprint, to.seat, to.zone, null);
      }
      const how = 'attachId' in op ? 'attach' : 'stack';
      const id = 'attachId' in op ? op.attachId : op.stackId;
      return addOn(context, op.blueprint, seat, how, cardById(context.board, String(id)), null);
    }
    case 'twilight':
    case 'burdens':
    case 'threats':
      return total(context, op);
    case 'marker':
      if (context.realOpening && context.inSetup) return refuse(AUTHOR_REFUSALS.deckOnly);
      if (op.seat === 'shadow' && context.markersTogether) return refuse(AUTHOR_REFUSALS.together);
      return { steps: [{ kind: 'queue', name: 'marker', args: [op.seat, op.site] }] };
    case 'control':
      return { steps: [{ kind: 'queue', name: 'control', args: [op.site, op.seat] }] };
    case 'liberate':
      return { steps: [{ kind: 'queue', name: 'liberate', args: [op.site] }] };
    case 'site':
      return laid(context, op);
    case 'pick':
      return refuse(AUTHOR_REFUSALS.pick);
    case 'unmodify':
      if ('id' in op) {
        const card = context.board.cards.find((one) => one.modifiers.some((modifier) => modifier.id === op.id));
        const what = card?.modifiers.find((modifier) => modifier.id === op.id)?.what ?? null;
        if (!card || what === null) return refuse(AUTHOR_REFUSALS.modifier);
        // (the file has no ids: unmodify(card, what) takes off every one of that kind)
        return ofACard(context, { op: 'unmodify', card: card.alias, what }, card);
      }
      break;
  }
  const card = named(context.board, op);
  if (card === null) return refuse(AUTHOR_REFUSALS.unknown);
  if (card.slot !== undefined) return refuse(AUTHOR_REFUSALS.slot);
  return ofACard(context, op, card);
}

/**
 * The steps of the edits, in order, or the first refusal. A site of a seat's path is a change to the scenario made at
 * once, as the tab makes it: it goes alone.
 */
export function authorSteps(context: AuthorContext, ops: readonly EditOp[]): AuthorSteps {
  const steps: AuthorStep[] = [];
  for (const op of ops) {
    const made = authorStep(context, op);
    if ('refused' in made) return made;
    steps.push(...made.steps);
  }
  if (steps.some((step) => step.kind === 'site') && steps.length > 1) return refuse(AUTHOR_REFUSALS.mixed);
  return { steps };
}
