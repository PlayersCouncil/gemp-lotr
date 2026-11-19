import { Component, inject } from '@angular/core';
import { noEffectReason } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { DocumentPressDirective } from '../../shared/document-press/document-press.directive';

/**
 * The menu of a card that has several actions to choose from, where the card was clicked: an entry per action, with
 * the server's text. A click anywhere else closes it.
 *
 * DOM contract: `ul.gd-game-menu` (with `--gd-menu-left` and `--gd-menu-top`, px in the page) holding a
 * `button[data-action-id]` per action; an action that would do nothing has `data-no-effect` (its reason) and ends with
 * `.gd-game-menu__reason` ("(limit reached)"), as game.html's noEffectAction entries. `inert` while a revert request
 * waits.
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
        [attr.inert]="undo.frozen() ? '' : null"
        #list
        (gdDocumentPress)="pressed($event, list)"
      >
        @for (action of menu.actions; track action.actionId) {
          <li role="none">
            <button
              type="button"
              role="menuitem"
              [attr.data-action-id]="action.actionId"
              [attr.data-no-effect]="action.noEffect"
              (click)="decisions.chooseAction(action.actionId)"
            >
              <span [innerHTML]="action.text"></span>
              @if (action.noEffect; as why) {
                <span class="gd-game-menu__reason">{{ ' (' + reason(why) + ')' }}</span>
              }
            </button>
          </li>
        }
      </ul>
    }
  `,
})
export class ActionMenuComponent {
  protected readonly decisions = inject(GameDecisions);
  protected readonly undo = inject(GameUndo);
  protected readonly reason = noEffectReason;

  /** A click anywhere but on the menu closes it (the click that chooses an entry is the entry's own). */
  protected pressed(event: MouseEvent, menu: HTMLElement): void {
    if (event.type === 'click' && !(event.target instanceof Node && menu.contains(event.target))) {
      this.decisions.closeMenu();
    }
  }
}
