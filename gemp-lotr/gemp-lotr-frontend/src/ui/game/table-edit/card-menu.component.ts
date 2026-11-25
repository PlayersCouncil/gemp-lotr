import { Component, computed, ElementRef, inject, linkedSignal, signal, viewChild } from '@angular/core';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { cardById, ModifyWord } from '../../../core/game/table-edit/edit-board';
import { cardMenu, modifyOp, nameOf, UNTIL_LABELS } from '../../../core/game/table-edit/edit-menu';
import { EditOp, ModifierUntil } from '../../../core/game/table-edit/edit-ops';
import { Box, placeAtPoint, placeBeside, placeSubmenu, Size } from '../../../core/game/table-edit/popup-place';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { siteMenu } from '../../../core/game/table-edit/edit-path';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { CardPicker } from './card-picker';
import { PathPopups } from './path-popups';
import { EditPopups } from './edit-popups';
import { MenuKeysDirective } from './menu-keys.directive';
import { PopupPlaceDirective } from './popup-place.directive';

type Submenu = 'send' | 'add' | 'modify';

/** The largest amount the Add stepper goes to (the server's limit). */
export const AMOUNT_MAX = 99;

const boxOf = (element: Element): Box => {
  const { left, top, right, bottom } = element.getBoundingClientRect();
  return { left, top, right, bottom };
};

/**
 * A card's menu while the table is edited by hand (right-click, a long press, or the context-menu key on a card):
 * Info · Send to ▸ (its owner's places it can go to) · Attach… · Stack… (a card picked on the table) · Add ▸ (wounds
 * and heals; culture tokens, its own culture first, then the others, by an amount set with a stepper) · Modify ▸ (what
 * suits its type, the amount, how long; and what is on it by hand now, each with a way to take it off) · Copy ·
 * Delete. Each entry only where the level allows its tool and the card can take it; every choice of a submenu comes
 * from the table as the level knows it (TableEdit.board). Up and Down move between entries, Right opens a submenu
 * and Left goes back, Escape closes (the focus goes back to the card); a press outside closes it.
 *
 * DOM contract: `gd-edit-menu` holding `.gd-edit-backdrop`, then `.gd-edit-menu` (`role=menu`, `data-card-id`;
 * placed by gdPopupPlace) with `.gd-edit-menu__title` (the card's name) and `button[role=menuitem][data-menu]` = info,
 * send, attach, stack, add, modify, copy, delete (a submenu's opener with `aria-haspopup`, `aria-expanded`); and the
 * submenu open, `.gd-edit-menu[data-submenu]`: `send` with a `button[data-zone]` per place; `add` with
 * `.gd-edit-menu__amount` (`button[data-step=down|up]`, `input[type=number]`), `button[data-add=wound|heal]` and a
 * `.gd-edit-menu__token[data-culture]` per culture (its count, `button[data-token=add|remove]`); `modify` with
 * `select[data-field=what]`, `input[data-field=amount]` (not for a keyword without a number),
 * `select[data-field=until]`, `button[data-action=put]`, and a `.gd-edit-menu__modifier[data-modifier]` per modifier on
 * the card (its text, `data-until`, `button[data-action=unmodify]`), then `button[data-action=unmodify-all]`.
 *
 * A site laid on the path (or controlled) has edits of its own after Info: under the heading Marker a
 * `button[data-site-action=marker][data-seat]` per seat (named by its player; `data-here`, disabled, where its marker
 * is), under Control a `button[data-site-action=control][data-seat]` per seat (`data-here` for the controller) and
 * `button[data-site-action=liberate]` while it is controlled, and `button[data-menu=replace]` (Replace…: the site
 * chooser for its number).
 *
 * On the author's table a puzzle slot's card has only Info and `button[data-menu=slot]` (Slot…: the slot's form in the
 * Board tab).
 */
@Component({
  selector: 'gd-edit-menu',
  styleUrl: './card-menu.component.css',
  imports: [MenuKeysDirective, PopupPlaceDirective],
  template: `
    @if (open(); as open) {
      <div class="gd-edit-backdrop" (pointerdown)="close()" (contextmenu)="$event.preventDefault(); close()"></div>
      <div
        #main
        class="gd-edit-menu"
        role="menu"
        [attr.aria-label]="title()"
        [attr.data-card-id]="open.cardId"
        [gdPopupPlace]="placement()"
        gdMenuKeys
        (forward)="forward($event)"
        (escape)="close(true)"
      >
        <div class="gd-edit-menu__title">{{ title() }}</div>
        <button type="button" role="menuitem" data-menu="info" (click)="info()" (pointerenter)="sub.set(null)">
          Info
        </button>
        @if (menu(); as menu) {
          @if (menu.sendTo.length > 0) {
            <button
              type="button"
              role="menuitem"
              data-menu="send"
              aria-haspopup="menu"
              [attr.aria-expanded]="sub() === 'send'"
              (click)="openSub('send', $event)"
              (pointerenter)="openSub('send', $event)"
            >
              Send to <span class="gd-edit-menu__more" aria-hidden="true">▸</span>
            </button>
          }
          @if (menu.attach) {
            <button
              type="button"
              role="menuitem"
              data-menu="attach"
              title="Then click the card it goes on"
              (click)="pickFor('attach')"
              (pointerenter)="sub.set(null)"
            >
              Attach…
            </button>
          }
          @if (menu.stack) {
            <button
              type="button"
              role="menuitem"
              data-menu="stack"
              title="Then click the card it goes under"
              (click)="pickFor('stack')"
              (pointerenter)="sub.set(null)"
            >
              Stack…
            </button>
          }
          @if (menu.add) {
            <button
              type="button"
              role="menuitem"
              data-menu="add"
              aria-haspopup="menu"
              [attr.aria-expanded]="sub() === 'add'"
              (click)="openSub('add', $event)"
              (pointerenter)="openSub('add', $event)"
            >
              Add <span class="gd-edit-menu__more" aria-hidden="true">▸</span>
            </button>
          }
          @if (menu.modify) {
            <button
              type="button"
              role="menuitem"
              data-menu="modify"
              aria-haspopup="menu"
              [attr.aria-expanded]="sub() === 'modify'"
              (click)="openSub('modify', $event)"
              (pointerenter)="openSub('modify', $event)"
            >
              Modify <span class="gd-edit-menu__more" aria-hidden="true">▸</span>
            </button>
          }
          @if (menu.copy) {
            <button
              type="button"
              role="menuitem"
              data-menu="copy"
              title="Another one, in the same place"
              (click)="send({ op: 'copy', ...nameOf(menu.card) })"
              (pointerenter)="sub.set(null)"
            >
              Copy
            </button>
          }
          @if (menu.delete) {
            <button
              type="button"
              role="menuitem"
              data-menu="delete"
              data-danger
              title="Out of the game (not a discard)"
              (click)="send({ op: 'delete', ...nameOf(menu.card) })"
              (pointerenter)="sub.set(null)"
            >
              Delete
            </button>
          }
        }
        @if (site(); as site) {
          @if (site.markers.length > 0) {
            <div class="gd-edit-menu__heading">Marker</div>
            @for (seat of site.markers; track seat.seat) {
              <button
                type="button"
                role="menuitem"
                data-site-action="marker"
                [attr.data-seat]="seat.seat"
                [attr.data-here]="seat.here ? '' : null"
                [disabled]="seat.here"
                [title]="seat.here ? 'Its marker is here' : 'Put its marker here'"
                (click)="send({ op: 'marker', seat: seat.seat, site: site.number })"
                (pointerenter)="sub.set(null)"
              >
                {{ seat.label }} <span class="gd-edit-menu__count">{{ seat.here ? '✓' : '' }}</span>
              </button>
            }
          }
          @if (site.control.length > 0) {
            <div class="gd-edit-menu__heading">Control</div>
            @for (seat of site.control; track seat.seat) {
              <button
                type="button"
                role="menuitem"
                data-site-action="control"
                [attr.data-seat]="seat.seat"
                [attr.data-here]="seat.here ? '' : null"
                [disabled]="seat.here"
                [title]="seat.here ? 'It controls this site' : 'Give it this site'"
                (click)="send({ op: 'control', site: site.number, seat: seat.seat })"
                (pointerenter)="sub.set(null)"
              >
                {{ seat.label }} <span class="gd-edit-menu__count">{{ seat.here ? '✓' : '' }}</span>
              </button>
            }
            @if (site.liberate) {
              <button
                type="button"
                role="menuitem"
                data-site-action="liberate"
                title="Nobody controls it"
                (click)="send({ op: 'liberate', site: site.number })"
                (pointerenter)="sub.set(null)"
              >
                Liberate
              </button>
            }
          }
          @if (site.replace) {
            <button
              type="button"
              role="menuitem"
              data-menu="replace"
              title="Another site here (this one goes back to its adventure deck)"
              (click)="replaceSite(site.number)"
              (pointerenter)="sub.set(null)"
            >
              Replace…
            </button>
          }
        }
        @if (slotOf(); as slot) {
          <button
            type="button"
            role="menuitem"
            data-menu="slot"
            [title]="'The puzzle’s slot ' + slot + ': its form in the Board tab'"
            (click)="openSlot(slot)"
            (pointerenter)="sub.set(null)"
          >
            Slot…
          </button>
        }
      </div>

      @if (menu(); as menu) {
        @switch (sub()) {
          @case ('send') {
            <div
              class="gd-edit-menu"
              role="menu"
              data-submenu="send"
              aria-label="Send to"
              [gdPopupPlace]="subPlacement()"
              gdMenuKeys
              (back)="closeSub()"
              (escape)="closeSub()"
            >
              @for (place of menu.sendTo; track place.zone) {
                <button
                  type="button"
                  role="menuitem"
                  [attr.data-zone]="place.zone"
                  (click)="send({ op: 'move', ...nameOf(menu.card), to: place.to })"
                >
                  {{ place.label }}
                </button>
              }
            </div>
          }
          @case ('add') {
            @if (menu.add; as add) {
              <div
                class="gd-edit-menu gd-edit-menu--form"
                role="menu"
                data-submenu="add"
                aria-label="Add"
                [gdPopupPlace]="subPlacement()"
                gdMenuKeys
                (back)="closeSub()"
                (escape)="closeSub()"
              >
                <div class="gd-edit-menu__amount">
                  <span>Amount</span>
                  <button
                    type="button"
                    data-step="down"
                    aria-label="Fewer"
                    [disabled]="amount() <= 1"
                    (click)="step(-1)"
                  >
                    −
                  </button>
                  <input
                    #amountBox
                    type="number"
                    min="1"
                    [max]="amountMax"
                    aria-label="Amount"
                    [value]="amount()"
                    (change)="setAmount(amountBox.value)"
                  />
                  <button
                    type="button"
                    data-step="up"
                    aria-label="More"
                    [disabled]="amount() >= amountMax"
                    (click)="step(1)"
                  >
                    +
                  </button>
                </div>
                @if (add.wounds) {
                  <button
                    type="button"
                    role="menuitem"
                    data-add="wound"
                    (click)="send({ op: 'wounds', ...nameOf(menu.card), amount: amount() })"
                  >
                    Wound <span class="gd-edit-menu__count">{{ menu.card.wounds }}</span>
                  </button>
                  <button
                    type="button"
                    role="menuitem"
                    data-add="heal"
                    [disabled]="menu.card.wounds === 0"
                    (click)="send({ op: 'wounds', ...nameOf(menu.card), amount: -amount() })"
                  >
                    Heal
                  </button>
                }
                @if (add.tokens.length > 0) {
                  <div class="gd-edit-menu__heading">Tokens</div>
                  <div class="gd-edit-menu__tokens">
                    @for (token of add.tokens; track token.culture) {
                      <div
                        class="gd-edit-menu__token"
                        [attr.data-culture]="token.culture"
                        [attr.data-own]="token.own ? '' : null"
                      >
                        <span class="gd-edit-menu__token-name">{{ token.label }}</span>
                        <span class="gd-edit-menu__count">{{ token.count }}</span>
                        <button
                          type="button"
                          role="menuitem"
                          data-token="add"
                          [attr.aria-label]="'Add ' + token.label + ' tokens'"
                          (click)="tokens(token.culture, amount())"
                        >
                          +
                        </button>
                        <button
                          type="button"
                          role="menuitem"
                          data-token="remove"
                          [attr.aria-label]="'Remove ' + token.label + ' tokens'"
                          [disabled]="token.count === 0"
                          (click)="tokens(token.culture, -amount())"
                        >
                          −
                        </button>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          }
          @case ('modify') {
            @if (menu.modify; as modify) {
              <div
                class="gd-edit-menu gd-edit-menu--form"
                role="menu"
                data-submenu="modify"
                aria-label="Modify"
                [gdPopupPlace]="subPlacement()"
                gdMenuKeys
                (back)="closeSub()"
                (escape)="closeSub()"
              >
                @if (modify.words.length > 0) {
                  <label class="gd-edit-menu__field">
                    <span>What</span>
                    <select #whatPick data-field="what" (change)="setWhat(whatPick.value)">
                      @for (word of modify.words; track word.what) {
                        <option [value]="word.what" [selected]="word.what === what()?.what">{{ word.label }}</option>
                      }
                    </select>
                  </label>
                  @if (what(); as word) {
                    @if (word.number !== 'none') {
                      <label class="gd-edit-menu__field">
                        <span>{{ word.number === 'signed' ? 'By' : 'Count' }}</span>
                        <input
                          #byBox
                          type="number"
                          data-field="amount"
                          [min]="word.number === 'positive' ? 1 : -amountMax"
                          [max]="amountMax"
                          [value]="modifyAmount()"
                          (change)="modifyAmount.set(+byBox.value)"
                        />
                      </label>
                    }
                  }
                  <label class="gd-edit-menu__field">
                    <span>How long</span>
                    <select #untilPick data-field="until" (change)="setUntil(untilPick.value)">
                      @for (time of modify.until; track time) {
                        <option [value]="time" [selected]="time === until()">{{ untilLabels[time] }}</option>
                      }
                    </select>
                  </label>
                  <button
                    type="button"
                    role="menuitem"
                    class="gd-edit-menu__put"
                    data-action="put"
                    [disabled]="putOp() === null"
                    (click)="put()"
                  >
                    Put on
                  </button>
                }
                @if (modify.present.length > 0) {
                  <div class="gd-edit-menu__heading">On it</div>
                  @for (modifier of modify.present; track modifier.id) {
                    <div
                      class="gd-edit-menu__modifier"
                      [attr.data-modifier]="modifier.id"
                      [attr.data-until]="modifier.until"
                    >
                      <span>{{ modifier.text }}</span>
                      @if (modifier.until !== 'removed') {
                        <span class="gd-edit-menu__until">{{ untilLabels[modifier.until] }}</span>
                      }
                      <button
                        type="button"
                        role="menuitem"
                        data-action="unmodify"
                        [attr.aria-label]="'Take off ' + modifier.text"
                        title="Take it off"
                        (click)="send({ op: 'unmodify', id: modifier.id })"
                      >
                        ×
                      </button>
                    </div>
                  }
                  @if (modify.present.length > 1) {
                    <button
                      type="button"
                      role="menuitem"
                      data-action="unmodify-all"
                      (click)="send({ op: 'unmodify', ...nameOf(menu.card) })"
                    >
                      Take all off
                    </button>
                  }
                }
              </div>
            }
          }
        }
      }
    }
  `,
})
export class CardMenuComponent {
  private readonly popups = inject(EditPopups);
  private readonly edit = inject(TableEdit);
  private readonly picker = inject(CardPicker);
  private readonly cardInfo = inject(CardInfoService);
  private readonly main = viewChild<ElementRef<HTMLElement>>('main');

  protected readonly nameOf = nameOf;
  protected readonly untilLabels = UNTIL_LABELS;
  protected readonly amountMax = AMOUNT_MAX;

  protected readonly open = this.popups.menu;
  private readonly card = computed(() => {
    const open = this.open();
    return open ? cardById(this.edit.board(), open.cardId) : null;
  });
  /** The menu's edits (null: the card is not one the level knows, so only Info). */
  protected readonly menu = computed(() => {
    const board = this.edit.board();
    const card = this.card();
    // (a puzzle slot's card, on the author's table, is changed through its slot: no edits of its own)
    return board && card && card.slot === undefined ? cardMenu(board, card, (tool) => this.edit.may(tool)) : null;
  });
  protected readonly title = computed(() => this.card()?.name ?? 'Card');

  /** The submenu open, and the item it hangs from. */
  protected readonly sub = signal<Submenu | null>(null);
  private readonly subFrom = signal<Box | null>(null);
  /** The Add submenu's amount (1 again for each card's menu). */
  protected readonly amount = linkedSignal(() => {
    this.open();
    return 1;
  });
  /** The Modify form: what, by how much, how long (a fresh form for each card). */
  protected readonly what = linkedSignal<ModifyWord | null>(() => this.menu()?.modify?.words[0] ?? null);
  protected readonly modifyAmount = linkedSignal(() => {
    this.what();
    return 1;
  });
  protected readonly until = linkedSignal<ModifierUntil>(() => {
    this.open();
    return 'removed';
  });
  protected readonly putOp = computed<EditOp | null>(() => {
    const card = this.card();
    const word = this.what();
    return card && word ? modifyOp(card, word, this.modifyAmount(), this.until()) : null;
  });

  protected readonly placement = computed(() => {
    const open = this.open();
    return (size: Size, room: Size) =>
      !open
        ? { left: 0, top: 0 }
        : open.point
          ? placeAtPoint(open.point, size, room)
          : placeBeside(open.anchor, size, room);
  });
  protected readonly subPlacement = computed(() => {
    const from = this.subFrom();
    return (size: Size, room: Size) => (from ? placeSubmenu(from, size, room) : { left: 0, top: 0 });
  });

  protected openSub(which: Submenu, event: Event): void {
    const item = event.currentTarget as HTMLElement | null;
    if (this.sub() === which) return;
    this.subFrom.set(item ? boxOf(item) : null);
    this.sub.set(which);
  }

  /** Right on an item: its submenu, with the focus in it. */
  protected forward(item: HTMLElement): void {
    const which = item.getAttribute('data-menu');
    if (which !== 'send' && which !== 'add' && which !== 'modify') return;
    this.subFrom.set(boxOf(item));
    this.sub.set(which);
  }

  protected closeSub(): void {
    const which = this.sub();
    this.sub.set(null);
    this.main()?.nativeElement.querySelector<HTMLElement>(`[data-menu="${which}"]`)?.focus();
  }

  protected close(focusBack = false): void {
    this.sub.set(null);
    this.popups.closeMenu(focusBack);
  }

  protected info(): void {
    const open = this.open();
    this.close();
    if (open) this.cardInfo.show(open.view);
  }

  protected step(by: number): void {
    this.setAmount(this.amount() + by);
  }

  protected setAmount(value: number | string): void {
    const amount = Math.trunc(Number(value));
    this.amount.set(Number.isFinite(amount) ? Math.min(AMOUNT_MAX, Math.max(1, amount)) : 1);
  }

  protected setWhat(what: string): void {
    this.what.set(this.menu()?.modify?.words.find((word) => word.what === what) ?? null);
  }

  protected setUntil(value: string): void {
    const until = this.menu()?.modify?.until.find((time) => time === value);
    if (until !== undefined) this.until.set(until);
  }

  protected tokens(culture: string, amount: number): void {
    const card = this.card();
    if (card) void this.send({ op: 'tokens', ...nameOf(card), amount, culture });
  }

  protected put(): void {
    const op = this.putOp();
    if (op) void this.send(op);
  }

  /** Makes an edit of the card; the menu closes (a refusal is said where the menu was). */
  protected async send(op: EditOp): Promise<void> {
    const at = this.where();
    this.close();
    await this.edit.apply([op], at);
  }

  /** Attach… and Stack…: the card the menu's card goes on, picked on the table. */
  protected async pickFor(how: 'attach' | 'stack'): Promise<void> {
    const card = this.card();
    const at = this.where();
    this.close();
    if (!card) return;
    const told = how === 'attach' ? `Attach ${card.name} to…` : `Stack ${card.name} on…`;
    const target = await this.picker.pickCard(told, card.cardId);
    const toId = target === null ? NaN : Number(target);
    if (Number.isSafeInteger(toId)) await this.edit.apply([{ op: how, ...nameOf(card), toId }], at);
  }

  /** Where the menu is: a refusal of what it did is said there. */
  private where(): { x: number; y: number } | null {
    const open = this.open();
    if (!open) return null;
    return open.point ?? { x: open.anchor.left, y: open.anchor.bottom };
  }

  // (a page without the path's controls gets no site entries)
  private readonly paths = inject(PathPopups, { optional: true });
  private readonly seating = inject(EditSeating, { optional: true });
  /** A site's own edits (a site on the path, or controlled): its markers, control, Replace…; null for other cards. */
  protected readonly site = computed(() => {
    const board = this.edit.board();
    const card = this.card();
    const seating = this.seating;
    return board && card && seating
      ? siteMenu(board, card, (tool) => this.edit.may(tool), seating.seats(), seating.names())
      : null;
  });

  /** Replace…: the site chooser for the site's number, where the menu was. */
  protected replaceSite(number: number): void {
    const open = this.open();
    this.close();
    if (!open) return;
    const at = open.point;
    const anchor = at ? { left: at.x, top: at.y, right: at.x, bottom: at.y } : open.anchor;
    this.paths?.openChooserAt(number, anchor, open.element, true);
  }

  // (the author's table: a puzzle slot's card opens its slot's form)
  private readonly author = inject(AuthorBackend, { optional: true });
  protected readonly slotOf = computed(() => (this.author ? (this.card()?.slot ?? null) : null));

  protected openSlot(slot: string): void {
    this.close();
    this.author?.showSlot(slot);
  }
}
