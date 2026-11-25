import { Component, computed, inject, input } from '@angular/core';
import { CardViews } from '../../../../core/cards/card-views.service';
import { Sheet } from '../../../../core/game/phone/sheet';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { GameCardViews } from '../../../../core/game/table/game-card-views';
import { PhoneCardsComponent } from '../phone-cards.component';

/**
 * A card's details in the phone layout's sheet: its picture, big, and the cards under it (attached, stacked), whose
 * details open over it.
 *
 * DOM contract: `img.gd-phone-detail__image` (`data-horizontal`), and the cards under it in `gd-phone-cards`.
 */
@Component({
  selector: 'gd-phone-card-sheet',
  styleUrl: './phone-card-sheet.component.css',
  imports: [PhoneCardsComponent],
  template: `
    @if (detail(); as d) {
      <img
        class="gd-phone-detail__image"
        [attr.data-horizontal]="d.horizontal ? '' : null"
        [src]="d.image"
        alt=""
        draggable="false"
      />
      @if (d.under.length > 0) {
        <h3>Under it ({{ d.under.length }})</h3>
        <gd-phone-cards class="gd-phone-sheet__cards" [cards]="d.under" />
      }
    }
  `,
})
export class PhoneCardSheetComponent {
  readonly sheet = input.required<Extract<Sheet, { kind: 'card' }>>();

  private readonly session = inject(GameSession);
  private readonly gameViews = inject(GameCardViews);
  private readonly cardViews = inject(CardViews);

  protected readonly detail = computed(() => {
    const sheet = this.sheet();
    const model = this.session.model();
    const card = sheet.cardId === null ? undefined : model.cards[sheet.cardId];
    const view = card
      ? this.gameViews.of(card)
      : this.cardViews.create({ ref: { hint: true }, blueprintId: sheet.blueprintId, zone: 'SPECIAL' });
    const under = card
      ? model.cardOrder
          .flatMap((id) => model.cards[id] ?? [])
          .filter((c) => c.attachedTo === card.id || c.stackedOn === card.id)
          .map((c) => ({ card: c, under: [] }))
      : [];
    return { image: view.imageUrl, horizontal: view.horizontal, under };
  });
}
