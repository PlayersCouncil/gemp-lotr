import { Component, computed, inject, input } from '@angular/core';
import { OpponentOrder } from '../../../../core/game/authoring/authoring-state';
import { formChanged, OrderForm } from '../../../../core/game/authoring/opponent-orders';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorKeepDirective } from '../widgets/author-keep.directive';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { OrdersEditor } from './orders-editor';

const WHEN_HINT =
  "The opponent's decisions it answers, as a step's When: kind of decision, phase(...), turn(...), inSkirmish(card), text(...), ... " +
  'Or written out in full, in HJSON: { terms: "...", require: { ... } }.';
const ANSWER_HINT =
  'What it answers with, as a step’s answer: play(card), use(card), choose(card), assign(frodo: orc, ringBearer: rest), pass. ' +
  'Several, comma-separated: the first the decision takes is used.';

/**
 * One of the opponent's standing orders (or a new one, `order` null): a line saying what it answers and with what,
 * its tags (once, used up in this game, its name), ↑ ↓ ✕; open, a small form of its parts (name, When, answer, once,
 * note), sent whole with Apply (Enter in a one-line field) and put back with Cancel. A refusal is said on it in the
 * server's words, and what was typed stays.
 *
 * DOM contract: `li.gd-author-order` (`data-index`, `data-id`, `data-open`, `data-spent`, `data-new`) holding
 * `.gd-author-order__head` with `button.gd-author-order__summary` (`aria-expanded`; `.gd-author-order__number`,
 * `.gd-author-order__when`, `.gd-author-order__answer`), `.gd-author-order__tag` (`data-tag` once, spent, id) and
 * `button[data-author]` up, down, delete; open, `.gd-author-order__form` with `[data-part]` id, when, answer, note
 * (`input`, the When a `textarea`), `input[data-part=once]`, `p.gd-author-order__refusal` and `button[data-author]`
 * apply, cancel.
 */
@Component({
  selector: 'gd-author-order',
  styleUrl: './order-card.component.css',
  host: { style: 'display: contents' },
  imports: [AuthorButtonDirective, AuthorFieldComponent, AuthorKeepDirective, HjsonAreaDirective],
  template: `
    <li
      class="gd-author-order"
      [attr.data-index]="order()?.index ?? null"
      [attr.data-id]="order()?.id ?? null"
      [attr.data-open]="open() ? '' : null"
      [attr.data-spent]="order()?.spent ? '' : null"
      [attr.data-new]="order() ? null : ''"
    >
      @if (order(); as order) {
        <div class="gd-author-order__head">
          <button
            type="button"
            class="gd-author-order__summary"
            [attr.aria-expanded]="open()"
            [title]="order.note ?? 'Open it to change it'"
            (click)="editor.toggle(order.index)"
          >
            <span class="gd-author-order__number">{{ order.index + 1 }}</span>
            <span class="gd-author-order__when">{{ order.when }}</span>
            <span class="gd-author-order__answer">{{ order.answers.join(', ') }}</span>
          </button>
          @if (order.id !== null) {
            <span class="gd-author-order__tag" data-tag="id" title="Its name">{{ order.id }}</span>
          }
          @if (order.spent) {
            <span class="gd-author-order__tag" data-tag="spent" title="It has answered its one decision in this game"
              >used up</span
            >
          } @else if (order.once) {
            <span class="gd-author-order__tag" data-tag="once" title="It answers one decision, then is done with"
              >once</span
            >
          }
          <button
            gdAuthorButton
            data-author="up"
            title="Tried before the one above"
            [disabled]="order.index === 0"
            (click)="editor.move(order, -1)"
          >
            ↑
          </button>
          <button
            gdAuthorButton
            data-author="down"
            title="Tried after the one below"
            [disabled]="order.index === count() - 1"
            (click)="editor.move(order, 1)"
          >
            ↓
          </button>
          <button gdAuthorButton="danger" data-author="delete" title="Delete it" (click)="editor.remove(order)">
            ✕
          </button>
        </div>
      } @else {
        <div class="gd-author-order__head">
          <span class="gd-author-order__summary" data-new>New order</span>
        </div>
      }
      @if (open() && editor.form(); as form) {
        <div class="gd-author-order__form">
          <gd-author-field label="When" [hint]="whenHint">
            <textarea
              gdHjsonArea
              rows="1"
              data-part="when"
              aria-label="When"
              [text]="form.when"
              [value]="form.when"
              [gdAuthorKeep]="key() + '.when'"
              (input)="type('when', $event)"
            ></textarea>
          </gd-author-field>
          <gd-author-field label="Answer" [hint]="answerHint">
            <input
              type="text"
              class="gd-author-input gd-author-order__code"
              spellcheck="false"
              data-part="answer"
              list="gd-author-order-answers"
              [value]="form.answer"
              [gdAuthorKeep]="key() + '.answer'"
              (input)="type('answer', $event)"
              (keydown.enter)="apply()"
            />
          </gd-author-field>
          <div class="gd-author-order__line">
            <label class="gd-author-check" title="It answers one decision, then is done with for the game">
              <input type="checkbox" data-part="once" [checked]="form.once" (change)="tick($event)" />
              once
            </label>
            <label class="gd-author-order__named">
              <span
                class="gd-author-field__label"
                title="Optional. Letters, digits, - and _; the tab names the order by it"
                >name</span
              >
              <input
                type="text"
                class="gd-author-input gd-author-order__name"
                spellcheck="false"
                data-part="id"
                [value]="form.id"
                [gdAuthorKeep]="key() + '.id'"
                (input)="type('id', $event)"
                (keydown.enter)="apply()"
              />
            </label>
          </div>
          <gd-author-field label="Note" hint="For whoever reads the file; never shown in the game">
            <input
              type="text"
              class="gd-author-input"
              data-part="note"
              [value]="form.note"
              [gdAuthorKeep]="key() + '.note'"
              (input)="type('note', $event)"
              (keydown.enter)="apply()"
            />
          </gd-author-field>
          @if (editor.memory().refusal; as refusal) {
            <p class="gd-author-order__refusal" role="alert">{{ refusal }}</p>
          }
          <div class="gd-author-bar">
            <button
              gdAuthorButton="primary"
              data-author="apply"
              [disabled]="!changed(form)"
              [title]="
                order() ? 'Into the file (a fresh game)' : 'Into the file, as order ' + place() + ' (a fresh game)'
              "
              (click)="apply()"
            >
              {{ order() ? 'Apply' : 'Add' }}
            </button>
            <button gdAuthorButton data-author="cancel" title="Put it back as it is" (click)="editor.cancel()">
              Cancel
            </button>
          </div>
        </div>
      }
    </li>
  `,
})
export class OrderCardComponent {
  protected readonly editor = inject(OrdersEditor);
  protected readonly whenHint = WHEN_HINT;
  protected readonly answerHint = ANSWER_HINT;
  /** The order, or null for the one being written. */
  readonly order = input.required<OpponentOrder | null>();
  protected readonly count = computed(() => this.editor.orders().length);
  protected readonly open = computed(() => {
    const open = this.editor.memory().open;
    const order = this.order();
    return order ? open === order.index : open === 'new';
  });
  /** Names its fields across a fresh game. */
  protected readonly key = computed(() => `order.${this.order()?.index ?? 'new'}`);
  /** The place a new order goes, from 1. */
  protected readonly place = computed(() => (this.editor.memory().before ?? this.count()) + 1);

  protected changed(form: OrderForm): boolean {
    const order = this.order();
    return order ? formChanged(order, form) : form.when.trim() !== '' && form.answer.trim() !== '';
  }

  protected type(part: 'when' | 'answer' | 'id' | 'note', event: Event): void {
    this.editor.type({ [part]: (event.target as HTMLInputElement | HTMLTextAreaElement).value });
  }

  protected tick(event: Event): void {
    this.editor.type({ once: (event.target as HTMLInputElement).checked });
  }

  protected apply(): void {
    const form = this.editor.form();
    if (form && this.changed(form)) void this.editor.apply();
  }
}
