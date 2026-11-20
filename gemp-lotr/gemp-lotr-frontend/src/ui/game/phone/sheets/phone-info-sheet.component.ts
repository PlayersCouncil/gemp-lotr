import { Component, computed, inject } from '@angular/core';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { phaseLabel } from '../phone-status.component';

/**
 * The game's information in the phone layout's sheet: the players, the phase, whose turn it is, the server's summary
 * of the game (its format, its timer) and whether the page watches it.
 *
 * DOM contract: `dl.gd-phone-facts`.
 */
@Component({
  selector: 'gd-phone-info-sheet',
  styleUrl: './phone-info-sheet.component.css',
  template: `
    <dl class="gd-phone-facts">
      @for (fact of facts(); track fact.name) {
        <dt>{{ fact.name }}</dt>
        @if (fact.html) {
          <!-- The server's summary is HTML (the format, the timer), shown as the pre-game panel does -->
          <dd [innerHTML]="fact.value"></dd>
        } @else {
          <dd>{{ fact.value }}</dd>
        }
      }
    </dl>
  `,
})
export class PhoneInfoSheetComponent {
  private readonly session = inject(GameSession);

  protected readonly facts = computed(() => {
    const model = this.session.model();
    const pre = model.preGame;
    const facts: { name: string; value: string; html?: boolean }[] = [
      { name: 'Players', value: model.players.join(' against ') || '–' },
      { name: 'Phase', value: phaseLabel(model.phase) || '–' },
      { name: 'Turn', value: model.currentPlayer ?? '–' },
      ...(pre?.summary ? [{ name: 'Game', value: pre.summary, html: true }] : []),
      { name: 'Watching', value: model.spectator ? 'Yes' : this.session.replaying() ? 'A replay' : 'No' },
    ];
    return facts;
  });
}
