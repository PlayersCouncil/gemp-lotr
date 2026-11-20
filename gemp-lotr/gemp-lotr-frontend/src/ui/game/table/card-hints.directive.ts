import { Directive, inject } from '@angular/core';
import { CardViews } from '../../../core/cards/card-views.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { GameCardViews } from '../../../core/game/table/game-card-views';

/**
 * Card names in the server's texts (`.cardHint` with the card's blueprint in `value`, a game message, a decision's
 * text) show the card's information when clicked, as on the legacy page (without the game's modifiers: it's a card's
 * name, not a card on the table). A meta-site the game names is shown as one, as on the table.
 */
@Directive({
  selector: '[gdCardHints]',
  host: { '(click)': 'clicked($event)' },
})
export class CardHintsDirective {
  private readonly info = inject(CardInfoService);
  private readonly views = inject(CardViews);
  private readonly gameViews = inject(GameCardViews);

  protected clicked(event: MouseEvent): void {
    const hint = event.target instanceof Element ? event.target.closest('.cardHint') : null;
    const blueprintId = hint?.getAttribute('value');
    if (!hint || !blueprintId) return;
    event.stopPropagation();
    this.info.show(
      this.gameViews.asMetaSite(
        this.views.create({
          ref: { hint: true, testingText: hint.getAttribute('data-testingText') },
          blueprintId,
          zone: 'SPECIAL',
        }),
      ),
    );
  }
}
