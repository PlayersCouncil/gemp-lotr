import { computed, Directive, inject, input } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameCard } from '../../../core/game/state/game-model';
import { assignmentOf } from '../../../core/game/state/local-choices';

/**
 * What a card of the game says about itself in the page (the DOM contract of the game page, which the game check
 * reads): `data-card-id`, `data-blueprint-id`, `data-zone` (the server's zone), `data-assign` (the character a minion
 * is assigned to), `data-inactive` (not active this turn), `data-hindered`, and `data-under` (attached, stacked) on a
 * card lying under another.
 */
@Directive({
  selector: '[gdGameCard]',
  host: {
    '[attr.data-card-id]': 'card().id',
    '[attr.data-blueprint-id]': 'card().blueprintId',
    '[attr.data-zone]': 'card().zone',
    '[attr.data-assign]': 'assign()',
    '[attr.data-inactive]': "card().inactive ? '' : null",
    '[attr.data-hindered]': "card().hindered ? '' : null",
    '[attr.data-under]': 'under()',
  },
})
export class GameCardDirective {
  readonly card = input.required<GameCard>({ alias: 'gdGameCard' });
  readonly under = input<'attached' | 'stacked' | null>(null);

  private readonly session = inject(GameSession);
  protected readonly assign = computed(() => assignmentOf(this.session.model(), this.card().id));
}
