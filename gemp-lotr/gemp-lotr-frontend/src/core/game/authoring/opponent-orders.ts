/**
 * The Opponent tab's model of what the opponent does where no step answers for it: its standing orders (tried in
 * order; each with a When, its answers, `once`, a note and a name), its fallback, the cards a sandbox fallback may
 * play, and who takes the minions it leaves unassigned. What each control sends is an `opponent` patch
 * (ScenarioAuthoring.opponentPatch), which changes only what it names: the orders it does not name are left exactly
 * as the file has them. An order is named by its `id` if it has one, else by its place (`index`, from 0, in the list
 * as it stands when the op runs). Pure.
 */
import { DraftOrder, OpponentAnswering, OpponentOrder, OpponentState } from './authoring-state';

/** An order, by its name if it has one, else by its place in the list as it stands when the op runs. */
export type OrderRef = { readonly id: string } | { readonly index: number };

/** What `edit` does to the list of orders (ScenarioAuthoring.opponentPatch's ops). */
export type OrderOp =
  /** Before the order named or at the place given; at the end without `before`. */
  | { op: 'insert'; order: DraftOrder; before?: string | number }
  /** Changes these parts of the order, keeps the rest. */
  | ({ op: 'merge'; set?: Readonly<Record<string, unknown>>; unset?: readonly string[] } & OrderRef)
  | ({ op: 'delete' } & OrderRef)
  /** Taken out, and put in at `to`. */
  | ({ op: 'move'; to: number } & OrderRef);

/** A change to the opponent: its own parts (name, fallback, leftovers, plays), and ops on its orders, in turn. */
export interface OpponentPatch {
  set?: Readonly<Record<string, unknown>>;
  unset?: readonly string[];
  orders?: readonly OrderOp[];
}

/** How an op names the order. */
export function refOf(order: Pick<OpponentOrder, 'id' | 'index'>): OrderRef {
  return order.id !== null ? { id: order.id } : { index: order.index };
}

/* ------------------------------------------------------------------ an order's form */

/** An order as the tab's form has it, every part as typed. */
export interface OrderForm {
  id: string;
  note: string;
  once: boolean;
  when: string;
  /** Its answers on a line, as a step's. */
  answer: string;
}

export const BLANK_ORDER: OrderForm = { id: '', note: '', once: false, when: '', answer: '' };

/** An order's parts, as the form starts with them. */
export function formOf(order: OpponentOrder): OrderForm {
  return {
    id: order.id ?? '',
    note: order.note ?? '',
    once: order.once,
    when: order.when,
    answer: order.answers.join(', '),
  };
}

/** Whether a form says something other than the order it was made from. */
export function formChanged(order: OpponentOrder | null, form: OrderForm): boolean {
  return JSON.stringify(order ? formOf(order) : BLANK_ORDER) !== JSON.stringify(form);
}

/** A When written out in full ({ ... }), which the server reads as HJSON. */
const inFull = (when: string): boolean => when.trim().startsWith('{');

/**
 * The ops that change an order to what its form says: one merge of what is different (set, and unset for what was
 * cleared); none when nothing is. Named as the order was (a new name is one of the parts set).
 */
export function changeOps(order: OpponentOrder, form: OrderForm): OrderOp[] {
  const was = formOf(order);
  const set: Record<string, unknown> = {};
  const unset: string[] = [];
  for (const part of ['id', 'note'] as const) {
    const now = form[part].trim();
    if (now === was[part]) continue;
    if (now === '') unset.push(part);
    else set[part] = now;
  }
  if (form.once !== was.once) {
    if (form.once) set['once'] = true;
    else unset.push('once');
  }
  for (const part of ['when', 'answer'] as const) {
    const now = form[part].trim();
    if (now !== was[part]) set[part] = now;
  }
  if (Object.keys(set).length === 0 && unset.length === 0) return [];
  return [
    {
      op: 'merge',
      ...refOf(order),
      ...(Object.keys(set).length > 0 ? { set } : {}),
      ...(unset.length > 0 ? { unset } : {}),
    },
  ];
}

/** The order a form makes, as the file has one (blank parts left out). */
export function orderOf(form: OrderForm): DraftOrder {
  const id = form.id.trim();
  const note = form.note.trim();
  return {
    ...(id !== '' ? { id } : {}),
    ...(note !== '' ? { note } : {}),
    ...(form.once ? { once: true } : {}),
    when: form.when.trim(),
    answer: form.answer.trim(),
  };
}

/**
 * The ops that put a new order in at `before` (its place, from 0; null: at the end), among `count` orders. A When
 * written out in full is typed text, which only a merge reads as HJSON: it is merged into the order just put in.
 */
export function insertOps(form: OrderForm, before: number | null, count: number): OrderOp[] {
  const at = before === null || before >= count ? null : Math.max(0, before);
  const ops: OrderOp[] = [{ op: 'insert', order: orderOf(form), ...(at !== null ? { before: at } : {}) }];
  if (inFull(form.when)) ops.push({ op: 'merge', index: at ?? count, set: { when: form.when.trim() } });
  return ops;
}

/** The op that takes an order out. */
export function deleteOps(order: OpponentOrder): OrderOp[] {
  return [{ op: 'delete', ...refOf(order) }];
}

/** The op that moves an order one place up (-1) or down (+1); none past either end. */
export function moveOps(order: OpponentOrder, by: -1 | 1, count: number): OrderOp[] {
  const to = order.index + by;
  return to < 0 || to >= count ? [] : [{ op: 'move', ...refOf(order), to }];
}

/** Where the order open (from 0) is once the order at `at` has been taken out (null: it was that one). */
export function placeAfterDelete(open: number | null, at: number): number | null {
  if (open === null || open === at) return null;
  return open > at ? open - 1 : open;
}

/** Where the order open (from 0) is once the order at `from` has been moved to `to`. */
export function placeAfterMove(open: number | null, from: number, to: number): number | null {
  if (open === null) return null;
  if (open === from) return to;
  const out = open > from ? open - 1 : open;
  return out >= to ? out + 1 : out;
}

/** Which order a refusal is about, from the parser's words ("opponent.orders[2].answer: ...", from 1); or null. */
export function refusedOrder(said: string | null): number | null {
  const found = /opponent\.orders\[(\d+)\]/.exec(said ?? '');
  return found ? Number(found[1]) - 1 : null;
}

/* ------------------------------------------------------------------ what answers the opponent */

/** A line of the tab: what it says, and the rest as its tooltip. */
export interface RuleLine {
  text: string;
  title: string;
}

/** What would answer the opponent's waiting decision if the author does nothing ("Order 2 (first)", ...). */
export function answeringLine(answering: OpponentAnswering): RuleLine {
  if (answering.order !== null) {
    return {
      text: `Order ${answering.order + 1}${answering.id !== null ? ` (${answering.id})` : ''}`,
      title: `Its standing order ${answering.order + 1} answers it: ${answering.answer ?? ''}`,
    };
  }
  if (answering.rule === 'leftovers') {
    return { text: 'Leftovers', title: 'No order answers it: the minions left go to the leftovers character' };
  }
  if (answering.rule === 'sandbox') return { text: 'Sandbox', title: 'No order answers it: its sandbox fallback does' };
  return { text: 'Nothing', title: 'No step, order or rule answers it: its fallback is all there is' };
}

/** Where an order for the waiting decision goes: before the order that answers it now (else at the end). */
export function placeForAnswering(answering: OpponentAnswering | null): number | null {
  return answering?.order ?? null;
}

/* ------------------------------------------------------------------ its fallback and rules */

/** What each fallback is called in the selects (one the server names and this does not is shown as itself). */
export const FALLBACK_LABELS: Readonly<Record<string, string>> = {
  fail: 'fail: off the script',
  pass: 'pass: does least',
  bot: 'bot: plays as a bot',
  sandbox: 'sandbox: plays minions',
};

/** The fallbacks to choose from (the server's list; before it says, the two the file always had). */
export function fallbackOptions(opponent: OpponentState | undefined): (readonly [string, string])[] {
  return (opponent?.fallbacks ?? ['fail', 'pass']).map((name) => [name, FALLBACK_LABELS[name] ?? name] as const);
}

/**
 * The patch that changes the fallback: `fail` (the default) is taken out; any but `sandbox` takes the sandbox's
 * cards out with it (they go only with it).
 */
export function fallbackPatch(fallback: string): OpponentPatch {
  if (fallback === 'fail') return { unset: ['fallback', 'plays'] };
  if (fallback === 'sandbox') return { set: { fallback } };
  return { set: { fallback }, unset: ['plays'] };
}

/** The patch that changes the sandbox's cards (none: all the opponent's cards). */
export function playsPatch(plays: readonly string[]): OpponentPatch {
  return plays.length === 0 ? { unset: ['plays'] } : { set: { plays: plays.join(', ') } };
}

/** The sandbox's cards with one ticked (last: they are tried in this order) or not. */
export function playsWith(plays: readonly string[], card: string, on: boolean): string[] {
  const rest = plays.filter((one) => one !== card);
  return on ? [...rest, card] : rest;
}

/** The patch that changes who takes the minions left unassigned ('': nobody). */
export function leftoversPatch(leftovers: string): OpponentPatch {
  return leftovers === '' ? { unset: ['leftovers'] } : { set: { leftovers } };
}

/** The leftovers to choose from: none, then the server's choices (the Ring-bearer first). */
export function leftoversOptions(opponent: OpponentState): (readonly [string, string])[] {
  return [
    ['', 'none'],
    ...opponent.leftoversChoices.map((name) => [name, name === 'ringBearer' ? 'the Ring-bearer' : name] as const),
  ];
}

/* ------------------------------------------------------------------ what the tab keeps across a fresh game */

/** Where the tab keeps its view while the page goes to a fresh game (every change to the opponent brings one). */
export const ORDERS_MEMORY_KEY = 'gemp-author-opponent';

/** What the tab keeps of itself for the draft: the order open (or 'new'), its form unsent, a refusal, the scroll. */
export interface OrdersMemory {
  draft: string;
  /** The order open (from 0), 'new' for one being written, or null. */
  open: number | 'new' | null;
  /** Where a new order goes (from 0; null: at the end). */
  before: number | null;
  /** What was typed into the open order and not sent; null: as the order is. */
  form: OrderForm | null;
  /** The server's refusal of what was sent, said on the order. */
  refusal: string | null;
  /** How far the panel was scrolled. */
  scroll: number;
}

export function emptyMemory(draft: string): OrdersMemory {
  return { draft, open: null, before: null, form: null, refusal: null, scroll: 0 };
}

const isForm = (value: unknown): value is OrderForm => {
  if (value === null || typeof value !== 'object') return false;
  const form = value as Record<string, unknown>;
  return (
    ['id', 'note', 'when', 'answer'].every((part) => typeof form[part] === 'string') &&
    typeof form['once'] === 'boolean'
  );
};

/** What was kept, for this draft (anything else, or kept for another draft, is forgotten). */
export function recallMemory(value: unknown, draft: string): OrdersMemory {
  const kept = value !== null && typeof value === 'object' ? (value as Readonly<Record<string, unknown>>) : {};
  if (kept['draft'] !== draft) return emptyMemory(draft);
  const open = kept['open'];
  const count = (one: unknown) => (typeof one === 'number' && Number.isInteger(one) && one >= 0 ? one : null);
  return {
    draft,
    open: open === 'new' ? 'new' : count(open),
    before: count(kept['before']),
    form: isForm(kept['form']) ? kept['form'] : null,
    refusal: typeof kept['refusal'] === 'string' ? kept['refusal'] : null,
    scroll: count(kept['scroll']) ?? 0,
  };
}
