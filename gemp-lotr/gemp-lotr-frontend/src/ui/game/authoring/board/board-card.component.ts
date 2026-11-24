import { Component, computed, inject, input, signal } from '@angular/core';
import { FoundCard } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { ListedCard, movesFor, reordered } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { CardHoverDirective } from '../widgets/card-hover.directive';
import { CardSearchComponent } from '../widgets/card-search.component';
import { BoardRenameComponent } from './board-rename.component';
import { BoardModifiersComponent } from './board-modifiers.component';

/** An option of a select: its value, and what it says. */
type Option = readonly [value: string, label: string];

/**
 * A card of the Board tab's lists: its name in the scenario (the card shown large under the mouse; a click opens its
 * information), its name, whether it is new or to be taken out, ✎ to rename it and Change to put another card in its
 * place (at once); then what can be gathered for it: Keep it (one to be taken out), ↑ ↓ ⤒ in a hand or pile, a move,
 * wounds − +, +token, its modifiers and mod… (board-modifiers), put on
 * another card, and × (not added after all, or taken out). To the setup of a game that
 * plays its own opening, only a deck's order and a move into a deck are offered.
 *
 * DOM contract: `gd-author-board-card` holding `.gd-author-board-card` (`data-alias`, `data-changed`, `data-added`,
 * `data-removing`) with `.gd-author-board-card__name` (`span.gd-author-board-card__alias.cardHint[value]`, the name,
 * `.gd-author-board-card__tag`, `button[data-action]`: rename, change; or `gd-author-board-rename`, or
 * `gd-author-card-search`), then `.gd-author-board-card__controls` (`button[data-action]`: keep, up, down, top, heal,
 * wound, token, drop; `.gd-author-board-card__count`; `select[data-control]`: move, put; `gd-author-board-modifiers`).
 */
@Component({
  selector: 'gd-author-board-card',
  styleUrl: './board-card.component.css',
  imports: [
    AuthorButtonDirective,
    BoardModifiersComponent,
    BoardRenameComponent,
    CardHoverDirective,
    CardSearchComponent,
  ],
  template: `
    @let card = entry().card;
    <div
      class="gd-author-board-card"
      [attr.data-alias]="card.alias"
      [attr.data-changed]="card.touched ? '' : null"
      [attr.data-added]="card.added ? '' : null"
      [attr.data-removing]="card.removing ? '' : null"
    >
      @if (editing() === 'rename') {
        <div class="gd-author-board-card__name">
          <gd-author-board-rename [alias]="card.alias" (closed)="editing.set(null)" />
        </div>
      } @else {
        <div class="gd-author-board-card__name">
          <span
            class="gd-author-board-card__alias cardHint"
            [attr.value]="card.blueprintId"
            [gdCardHover]="card.blueprintId"
            >{{ card.alias }}</span
          >
          {{ card.name }}
          @if (card.added) {
            <span class="gd-author-board-card__tag">new</span>
          } @else if (card.removing) {
            <span class="gd-author-board-card__tag">to be taken out</span>
          } @else {
            <button
              gdAuthorButton="inline"
              data-action="rename"
              title="Give this card another name in the scenario. Every part of the scenario that says the old name is changed to say the new one."
              (click)="editing.set('rename')"
            >
              ✎
            </button>
            @if (editing() !== 'change') {
              <button
                gdAuthorButton="inline"
                data-action="change"
                title="Put another card (another version of this one, say) in its place: the same name in the scenario, the same place on the board"
                (click)="editing.set('change')"
              >
                Change
              </button>
            }
          }
        </div>
        @if (editing() === 'change') {
          <gd-author-card-search
            [query]="card.name.replace(beforeComma, '')"
            [immediate]="true"
            [current]="card.blueprintId"
            (picked)="changeTo($event)"
            (cancelled)="editing.set(null)"
          />
        }
      }
      <div class="gd-author-board-card__controls">
        @if (card.removing) {
          <button
            gdAuthorButton
            data-action="keep"
            title="Do not take this card out"
            (click)="changes.keep(card.alias)"
          >
            Keep it
          </button>
        } @else {
          @if (entry().ordered && (!changes.deckOnly() || card.zone === 'deck')) {
            <button
              gdAuthorButton
              data-action="up"
              [title]="pile() ? 'One nearer the top' : 'One earlier in the hand'"
              [disabled]="entry().at === 0"
              (click)="reorder(entry().at - 1)"
            >
              ↑
            </button>
            <button
              gdAuthorButton
              data-action="down"
              [title]="pile() ? 'One nearer the bottom' : 'One later in the hand'"
              [disabled]="entry().at === entry().among.length - 1"
              (click)="reorder(entry().at + 1)"
            >
              ↓
            </button>
            <button
              gdAuthorButton
              data-action="top"
              [title]="pile() ? 'To the top' : 'To the front of the hand'"
              [disabled]="entry().at === 0"
              (click)="reorder(0)"
            >
              ⤒
            </button>
          }
          <select
            class="gd-author-input"
            data-control="move"
            aria-label="Move it"
            title="Move it"
            (change)="move($any($event.target))"
          >
            @for (option of moves(); track option[0]) {
              <option [value]="option[0]">{{ option[1] }}</option>
            }
          </select>
          @if (!changes.deckOnly() && (card.zone === 'characters' || card.zone === 'minions' || card.wounds > 0)) {
            <button
              gdAuthorButton
              data-action="heal"
              title="Heal a wound"
              [disabled]="card.wounds === 0"
              (click)="changes.queue('heal', [card.alias, 1])"
            >
              −
            </button>
            <span class="gd-author-board-card__count" title="wounds">{{ card.wounds }}w</span>
            <button
              gdAuthorButton
              data-action="wound"
              title="Add a wound"
              (click)="changes.queue('wound', [card.alias, 1])"
            >
              +
            </button>
          }
          @if (!changes.deckOnly() && (card.zone === 'support' || card.tokens > 0)) {
            <button
              gdAuthorButton
              data-action="token"
              title="Add a culture token"
              (click)="changes.queue('tokens', [card.alias, 1])"
            >
              +token{{ card.tokens > 0 ? ' (' + card.tokens + ')' : '' }}
            </button>
          }
          <gd-author-board-modifiers [card]="card" />
          @if (!changes.deckOnly() && hosts().length > 1) {
            <select
              class="gd-author-input"
              data-control="put"
              aria-label="Put it on another card"
              title="Attach it to another card, or stack it on one"
              (change)="putOn($any($event.target))"
            >
              @for (option of hosts(); track option[0]) {
                <option [value]="option[0]">{{ option[1] }}</option>
              }
            </select>
          }
          <button
            gdAuthorButton="danger"
            data-action="drop"
            [title]="
              card.added
                ? 'Do not add this card'
                : 'Take this card out of the scenario (when the changes are submitted)'
            "
            (click)="changes.drop(card)"
          >
            ×
          </button>
        }
      </div>
    </div>
  `,
})
export class BoardCardComponent {
  readonly entry = input.required<ListedCard>();

  protected readonly changes = inject(BoardChanges);
  /** (a card's other versions are found by its name before the comma) */
  protected readonly beforeComma = /,.*$/s;
  /** Being renamed, or another card being found for its place. */
  protected readonly editing = signal<'rename' | 'change' | null>(null);
  /** A pile (not the hand): its order is from the top. */
  protected readonly pile = computed(() => this.entry().card.zone !== 'hand');

  /**
   * Where it can be moved to: its place first (choosing it does nothing), then every place of its seat's the change
   * can put it (to the setup: no minions on the table for the Free Peoples seat; with the game's own opening, only a
   * deck).
   */
  protected readonly moves = computed<Option[]>(() => {
    const { card } = this.entry();
    const seat = card.owner;
    return [
      ['', card.zone + (card.on ? ` on ${card.on}` : '')],
      ...movesFor(seat, this.changes.inSetup(), this.changes.deckOnly()).map((zone): Option => [
        `${seat}.${zone}`,
        zone,
      ]),
    ];
  });

  /** What it can be put on: any other card on the table, attached to it or stacked on it. */
  protected readonly hosts = computed<Option[]>(() => {
    const alias = this.entry().card.alias;
    const hosts = this.changes.bearers().filter((bearer) => bearer.alias !== alias);
    return [
      ['', 'put on…'],
      ...hosts.map((host): Option => [`attach|${host.alias}`, `attach to ${host.alias}`]),
      ...hosts.map((host): Option => [`stack|${host.alias}`, `stack on ${host.alias}`]),
    ];
  });

  protected reorder(to: number): void {
    const { card, among, at } = this.entry();
    this.changes.queue('order', [`${card.owner}.${card.zone}`, ...reordered(among, at, to)]);
  }

  /** (a select that gathers a change goes back to saying where the card is) */
  protected move(select: HTMLSelectElement): void {
    const zone = select.value;
    select.value = '';
    if (zone !== '') this.changes.queue('move', [this.entry().card.alias, zone]);
  }

  protected putOn(select: HTMLSelectElement): void {
    const [how, host] = select.value.split('|');
    select.value = '';
    if (how && host) this.changes.queue(how, [this.entry().card.alias, host]);
  }

  protected async changeTo(found: FoundCard): Promise<void> {
    const card = this.entry().card;
    this.editing.set(null);
    if (found.id !== card.blueprintId) await this.changes.changeCard(card, found.id);
  }
}
