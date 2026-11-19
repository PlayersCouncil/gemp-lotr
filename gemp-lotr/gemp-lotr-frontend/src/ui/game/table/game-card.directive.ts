import { computed, Directive, inject, input } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameCard } from '../../../core/game/state/game-model';
import { assignmentOf } from '../../../core/game/state/local-choices';

/**
 * What a card of the game says about itself in the page (the DOM contract of the game page, which the game check
 * reads): `data-card-id`, `data-blueprint-id`, `data-zone` (the server's zone), `data-assign` (the character a minion
 * is assigned to), `data-inactive` (not active this turn), `data-hindered`, `data-under` (attached, stacked) on a
 * card lying under another, and `data-mark` (selectable, actionable, selected) while the waiting decision offers it,
 * with `data-no-effect` when every action it offers would do nothing (legacy noEffectCard). In a scripted game
 * whose step takes only some answers, an offered card also has `data-scenario`: `live` (the step wants it clicked) or
 * `notnow` (the game would take it, the step doesn't).
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
    '[attr.data-mark]': 'mark()',
    '[attr.data-no-effect]': "noEffect() ? '' : null",
    '[attr.data-scenario]': 'scenario()',
  },
})
export class GameCardDirective {
  readonly card = input.required<GameCard>({ alias: 'gdGameCard' });
  readonly under = input<'attached' | 'stacked' | null>(null);

  private readonly session = inject(GameSession);
  private readonly decisions = inject(GameDecisions);
  protected readonly mark = computed(() => this.decisions.view().marks[this.card().id] ?? null);
  protected readonly noEffect = computed(
    () => this.mark() === 'actionable' && this.decisions.view().noEffect[this.card().id] === true,
  );
  private readonly guide = inject(ScenarioGuide);
  protected readonly scenario = computed(() => this.guide.cardMarks()[this.card().id] ?? null);
  protected readonly assign = computed(() => assignmentOf(this.session.model(), this.card().id));
}
