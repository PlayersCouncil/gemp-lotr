import { computed, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { OpponentOrder } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  BLANK_ORDER,
  changeOps,
  deleteOps,
  emptyMemory,
  formOf,
  insertOps,
  moveOps,
  OpponentPatch,
  OrderForm,
  OrderOp,
  ORDERS_MEMORY_KEY,
  OrdersMemory,
  placeAfterDelete,
  placeAfterMove,
  recallMemory,
} from '../../../../core/game/authoring/opponent-orders';
import { Notifier } from '../../../../core/notifier/notifier';
import { SessionStore } from '../../../../core/platform/session-store';
import { askAuthor } from '../widgets/author-confirm';

/**
 * The Opponent tab's editor of the opponent's standing orders and rules (provided by the tab): which order is open,
 * what has been typed into it and not sent (a small form, sent whole with Apply: every change to the opponent brings
 * a fresh game, a full page load, so one Apply is one fresh game however many parts were changed), the server's
 * refusal of it, and how far the panel was scrolled. All of that is kept in the tab's session storage as it changes,
 * so the page of the fresh game comes back to it as it was; a refusal keeps the form as it was typed, with the
 * server's words on it. Every change is an `opponent` patch, which leaves the orders it does not name as they are.
 */
@Injectable()
export class OrdersEditor {
  readonly authoring = inject(Authoring);
  private readonly store = inject(SessionStore);
  private readonly notifier = inject(Notifier);

  readonly opponent = computed(() => this.authoring.draft()?.opponent ?? null);
  readonly orders = computed<readonly OpponentOrder[]>(() => this.opponent()?.orders ?? []);
  private readonly draftId = computed(() => this.authoring.draft()?.draft.id ?? null);

  /** What the tab keeps of itself (for the draft in hand). */
  readonly memory = signal<OrdersMemory>(emptyMemory(''));
  /** Where the panel is scrolled from, once the tab is on the page. */
  scroller: HTMLElement | null = null;

  /** The order open, if it is one of the orders. */
  readonly openOrder = computed<OpponentOrder | null>(() => {
    const open = this.memory().open;
    return typeof open === 'number' ? (this.orders()[open] ?? null) : null;
  });
  /** The open order's form: as typed, or as the order is. */
  readonly form = computed<OrderForm | null>(() => {
    const memory = this.memory();
    if (memory.open === 'new') return memory.form ?? BLANK_ORDER;
    const order = this.openOrder();
    return order ? (memory.form ?? formOf(order)) : null;
  });

  constructor() {
    // The draft's game is on the page: what was kept for it comes back (once)
    effect(() => {
      const draft = this.draftId();
      if (draft === null || untracked(this.memory).draft === draft) return;
      this.memory.set(recallMemory(this.store.read(ORDERS_MEMORY_KEY), draft));
    });
  }

  private keep(change: Partial<OrdersMemory>): void {
    this.memory.update((memory) => ({ ...memory, ...change }));
    this.store.write(ORDERS_MEMORY_KEY, this.memory());
  }

  /** Opens an order (from 0), or closes it when it is the one open; what was typed into another is dropped. */
  toggle(index: number): void {
    this.keep({ open: this.memory().open === index ? null : index, before: null, form: null, refusal: null });
  }

  /** A new order, written at `before` (from 0; null: at the end), starting from `start`. */
  write(before: number | null, start: Partial<OrderForm> = {}): void {
    this.keep({ open: 'new', before, form: { ...BLANK_ORDER, ...start }, refusal: null });
  }

  /** What was typed into the open order's form. */
  type(change: Partial<OrderForm>): void {
    const form = this.form();
    if (form) this.keep({ form: { ...form, ...change } });
  }

  /** Puts the open order's form back as the order is (a new one is given up). */
  cancel(): void {
    const open = this.memory().open;
    this.keep({ open: open === 'new' ? null : open, before: null, form: null, refusal: null });
  }

  /** Sends the open order's form: a new order put in, or the parts of the order that changed. */
  async apply(): Promise<void> {
    const memory = this.memory();
    const form = this.form();
    if (!form) return;
    const count = this.orders().length;
    if (memory.open === 'new') {
      const at = memory.before === null || memory.before >= count ? count : memory.before;
      await this.change(insertOps(form, memory.before, count), { open: at, before: null });
      return;
    }
    const order = this.openOrder();
    if (!order) return;
    const ops = changeOps(order, form);
    if (ops.length === 0) {
      this.keep({ form: null, refusal: null });
      return;
    }
    await this.change(ops, { open: order.index });
  }

  /** Moves an order one place up (-1) or down (+1). */
  async move(order: OpponentOrder, by: -1 | 1): Promise<void> {
    const ops = moveOps(order, by, this.orders().length);
    if (ops.length === 0) return;
    const open = this.memory().open;
    const next = typeof open === 'number' ? placeAfterMove(open, order.index, order.index + by) : open;
    await this.change(ops, { open: next });
  }

  /** Takes an order out, once the author says so. */
  async remove(order: OpponentOrder): Promise<void> {
    const name = `order ${order.index + 1}${order.id !== null ? ` (${order.id})` : ''}`;
    if (!(await askAuthor(this.notifier, `Delete ${name}?`, 'Delete'))) return;
    const open = this.memory().open;
    const next = typeof open === 'number' ? placeAfterDelete(open, order.index) : open;
    const before = this.memory().before;
    await this.change(deleteOps(order), {
      open: next,
      before: before !== null && open === 'new' ? placeAfterDelete(before, order.index) : before,
      ...(next === open ? {} : { form: null }),
    });
  }

  /**
   * A change to the opponent's own parts (its fallback, leftovers, the sandbox's cards): the order open stays open,
   * with what was typed into it.
   */
  async patch(patch: OpponentPatch): Promise<boolean> {
    return this.send(patch, {});
  }

  /** Sends ops on the orders; `then` is what the tab is once they are in (the page of the fresh game comes back to it). */
  private async change(ops: readonly OrderOp[], then: Partial<OrdersMemory>): Promise<void> {
    await this.send({ orders: ops }, { form: null, ...then });
  }

  private async send(patch: OpponentPatch, then: Partial<OrdersMemory>): Promise<boolean> {
    const was = this.memory();
    // (kept before it goes: a change brings a fresh game, and the page of it reads what is kept)
    this.keep({ ...then, refusal: null, scroll: this.scroller?.scrollTop ?? was.scroll });
    const result = await this.authoring.edit({ opponent: patch });
    if (result) return true;
    // Refused: the tab is as it was, with the server's words on the order the change was for
    this.keep({ ...was, refusal: this.authoring.problem() });
    return false;
  }
}
