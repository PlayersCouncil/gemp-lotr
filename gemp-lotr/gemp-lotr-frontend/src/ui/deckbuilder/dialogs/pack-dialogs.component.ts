import { Component, computed, inject, linkedSignal } from '@angular/core';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';
import { PacksService } from '../../../core/packs/packs.service';
import { deliveredCards, selectionChoiceCards } from '../../../core/packs/shown-cards';
import { CardComponent } from '../../shared/card/card.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { cardFits, FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';

interface ShownGroup {
  cards: CardView[];
  fit: FitCard[];
}

function group(cards: CardView[]): ShownGroup {
  return { cards, fit: cardFits(cards) };
}

/**
 * The selection pack's "Choose one" (modal; using a card picks it and opens the pack) and a "New items - <collection>"
 * dialog per collection something was opened into (not modal). Their cards are sized to fit (fitCards), so they
 * follow the dialog's size. Both focus themselves when they open, as the legacy ones did. A "New items" dialog once opened stays on the page, closed, and so keeps the size the
 * player gave it, as the legacy dialogs were kept.
 *
 * DOM contract: `gd-dialog`s with `data-dialog` = selection or delivery, each holding `.gd-shown-cards` (the fitCards
 * box, `data-count`) with a `button.gd-card` per card (selection choices: `data-role=card`; delivered packs `pack` and
 * cards `card`, all with their count).
 */
@Component({
  selector: 'gd-pack-dialogs',
  styleUrl: './pack-dialogs.component.css',
  imports: [DialogComponent, CardComponent, FitCardsDirective],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="selection"
      heading="Choose one"
      [focusSelf]="true"
      [open]="packs.selection() !== null"
      (closed)="packs.selection.set(null)"
    >
      <div class="gd-shown-cards" [attr.data-count]="selection().cards.length" [gdFitCards]="selection().fit">
        @for (card of selection().cards; track $index) {
          <button [gdCard]="card"></button>
        }
      </div>
    </gd-dialog>
    @for (name of deliveryNames(); track name) {
      <gd-dialog
        name="delivery"
        [heading]="'New items - ' + name"
        [modal]="false"
        [focusSelf]="true"
        [open]="deliveries().has(name)"
        (closed)="packs.closeDelivery(name)"
      >
        @if (deliveries().get(name); as shown) {
          <div class="gd-shown-cards" [attr.data-count]="shown.cards.length" [gdFitCards]="shown.fit">
            @for (card of shown.cards; track $index) {
              <button [gdCard]="card"></button>
            }
          </div>
        }
      </gd-dialog>
    }
  `,
})
export class PackDialogsComponent {
  protected readonly packs = inject(PacksService);
  private readonly views = inject(CardViews);

  protected readonly selection = computed(() =>
    group(selectionChoiceCards(this.packs.selection()?.choices ?? [], this.views)),
  );

  protected readonly deliveries = computed(
    () =>
      new Map(
        [...this.packs.deliveries().entries()].map(([name, cards]) => [name, group(deliveredCards(cards, this.views))]),
      ),
  );

  /** Every collection a "New items" dialog was opened for, in order. */
  protected readonly deliveryNames = linkedSignal<string[], string[]>({
    source: () => [...this.packs.deliveries().keys()],
    computation: (names, previous) => {
      const seen = previous?.value ?? [];
      const added = names.filter((name) => !seen.includes(name));
      return added.length ? [...seen, ...added] : seen;
    },
  });
}
