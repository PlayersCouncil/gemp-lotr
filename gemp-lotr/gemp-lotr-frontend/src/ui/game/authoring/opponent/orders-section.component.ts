import { Component, computed, inject } from '@angular/core';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { OrderCardComponent } from './order-card.component';
import { OrdersEditor } from './orders-editor';

/**
 * The opponent's standing orders, in the order they are tried where no step answers the opponent: a card each
 * (gd-author-order), the one being written where it will go, and + Order (at the end). The answers the decision
 * waiting offers are suggested in each order's Answer field.
 *
 * DOM contract: `gd-author-orders` (in the tab's `section[data-part=orders]`) holding `ol.gd-author-orders`
 * (`data-count`) with a `gd-author-order` per order (and the new one), `p.gd-author-note[data-author=no-orders]`
 * when there is none, `button[data-author=add-order]`, and `datalist#gd-author-order-answers`.
 */
@Component({
  selector: 'gd-author-orders',
  styleUrl: './orders-section.component.css',
  imports: [AuthorButtonDirective, OrderCardComponent],
  template: `
    <ol class="gd-author-orders" [attr.data-count]="editor.orders().length">
      @for (order of editor.orders(); track order.index) {
        @if (newAt() === order.index) {
          <gd-author-order [order]="null" />
        }
        <gd-author-order [order]="order" />
      }
      @if (newAt() !== null && newAt()! >= editor.orders().length) {
        <gd-author-order [order]="null" />
      }
    </ol>
    @if (editor.orders().length === 0 && newAt() === null) {
      <p class="gd-author-note" data-author="no-orders">None.</p>
    }
    <button
      gdAuthorButton
      data-author="add-order"
      title="A standing order, tried after the others"
      (click)="editor.write(null)"
    >
      + Order
    </button>
    <datalist id="gd-author-order-answers">
      @for (term of suggestions(); track term) {
        <option [value]="term"></option>
      }
    </datalist>
  `,
})
export class OrdersSectionComponent {
  protected readonly editor = inject(OrdersEditor);

  /** Where the order being written goes among the others (from 0), or null when none is. */
  protected readonly newAt = computed(() => {
    const memory = this.editor.memory();
    if (memory.open !== 'new') return null;
    return memory.before ?? this.editor.orders().length;
  });

  /** The answers the opponent's waiting decision offers, and what answers it now. */
  protected readonly suggestions = computed(() => {
    const draft = this.editor.authoring.draft();
    const asked = draft?.waiting.opponent;
    const answering = draft?.opponent?.answering;
    const terms = [
      ...(asked?.options ?? []).map((option) => option.term),
      asked?.pass ? 'pass' : null,
      answering?.gives ?? null,
      answering?.answer ?? null,
    ];
    return [...new Set(terms.filter((term): term is string => term !== null && term !== ''))];
  });
}
