import { Component, input } from '@angular/core';
import { UiButtonDirective } from '../shared/ui-button.directive';

/**
 * The deck builder's top bar (#manageDecks): deck buttons, format and collection selectors, the deck name and the
 * auto-zoom toggle. Same markup as the legacy page after jQuery UI had turned the buttons into widgets.
 * Phase 3 shows it only; the buttons get their actions in later phases.
 */
@Component({
  selector: '[gempDeckToolbar]',
  imports: [UiButtonDirective],
  templateUrl: './deck-toolbar.component.html',
})
export class DeckToolbarComponent {
  readonly deckName = input('New deck');
  readonly convertErrataEnabled = input(false);
  readonly autoZoomEnabled = input(true);
}
