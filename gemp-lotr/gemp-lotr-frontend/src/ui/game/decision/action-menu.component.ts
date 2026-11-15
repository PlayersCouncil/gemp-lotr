import { Component, inject } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { DocumentPressDirective } from '../../shared/document-press/document-press.directive';

/**
 * The menu of a card that has several actions to choose from, where the card was clicked: an entry per action, with
 * the server's text. A click anywhere else closes it.
 *
 * DOM contract: `ul.gd-game-menu` (with `--gd-menu-left` and `--gd-menu-top`, px in the page) holding a
 * `button[data-action-id]` per action.
 */
@Component({
  selector: 'gd-game-action-menu',
  styleUrl: './action-menu.component.css',
  imports: [DocumentPressDirective],
  host: { style: 'display: contents' },
  template: `
    @if (decisions.menu(); as menu) {
      <ul
        class="gd-game-menu"
        role="menu"
        [style.--gd-menu-left]="menu.x + 'px'"
        [style.--gd-menu-top]="menu.y + 'px'"
        #list
        (gdDocumentPress)="pressed($event, list)"
      >
        @for (action of menu.actions; track action.actionId) {
          <li role="none">
            <button
              type="button"
              role="menuitem"
              [attr.data-action-id]="action.actionId"
              [innerHTML]="action.text"
              (click)="decisions.chooseAction(action.actionId)"
            ></button>
          </li>
        }
      </ul>
    }
  `,
})
export class ActionMenuComponent {
  protected readonly decisions = inject(GameDecisions);

  /** A click anywhere but on the menu closes it (the click that chooses an entry is the entry's own). */
  protected pressed(event: MouseEvent, menu: HTMLElement): void {
    if (event.type === 'click' && !(event.target instanceof Node && menu.contains(event.target))) {
      this.decisions.closeMenu();
    }
  }
}
