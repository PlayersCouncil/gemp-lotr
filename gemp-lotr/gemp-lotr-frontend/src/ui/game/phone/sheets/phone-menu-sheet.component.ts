import { Component, computed, inject } from '@angular/core';
import { GameSheets } from '../../../../core/game/phone/game-sheets';
import { GameEnding } from '../../../../core/game/session/game-ending';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { GameUndo } from '../../../../core/game/undo/game-undo.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { GAME_THEME_PICKER, THEME_LABELS, THEMES, ThemeService } from '../../../../core/platform/theme.service';

/** The hall the menu goes back to. */
const HALL = '/gemp-lotr/hall.html';

/**
 * The phone layout's menu: the theme (red, first, as in the deck builder), the game's result once it ended, the chat,
 * the game's information, the settings, full screen, Revert... (the list of places to go back to, UndoListComponent, as
 * in the computer layout's Options tab), conceding or asking to cancel while the player plays, and back to the hall.
 *
 * DOM contract: `.gd-phone-menu` holding `button.gd-phone-menu__item[data-menu]` (`data-danger` on concede; `revert`
 * opens the list).
 */
@Component({
  selector: 'gd-phone-menu-sheet',
  styleUrl: './phone-menu-sheet.component.css',
  template: `
    <div class="gd-phone-menu">
      @for (item of items(); track item.key) {
        <button
          type="button"
          class="gd-phone-menu__item"
          [class.gd-button]="item.key === 'theme'"
          [attr.data-icon]="item.key === 'theme' ? 'theme' : null"
          [attr.data-menu]="item.key"
          [attr.data-danger]="item.key === 'concede' ? '' : null"
          (click)="choose(item.key)"
        >
          {{ item.label }}
        </button>
      }
    </div>
  `,
})
export class PhoneMenuSheetComponent {
  private readonly sheets = inject(GameSheets);
  private readonly session = inject(GameSession);
  private readonly cues = inject(PageCues);
  private readonly ending = inject(GameEnding);
  private readonly undo = inject(GameUndo);
  private readonly theme = inject(ThemeService);
  /** Players may switch themes (GAME_THEME_PICKER): the menu's first item does it. */
  private readonly themePicker = inject(GAME_THEME_PICKER);
  /** The theme the menu's first item switches to: the next one, after the last the first. */
  private readonly nextTheme = computed(
    () => THEMES[(THEMES.indexOf(this.theme.theme()) + 1) % THEMES.length] ?? THEMES[0],
  );

  protected readonly items = computed(() => {
    const model = this.session.model();
    const playing = !model.spectator && !this.session.replaying();
    const items: { key: string; label: string }[] = [];
    // The theme first, red, as in the deck builder's menu (the owner, 2026-11-18); the piles are on the players' chips
    if (this.themePicker) items.push({ key: 'theme', label: `Switch to the ${THEME_LABELS[this.nextTheme()]} theme` });
    // The game's end again, with the replay (the owner, 2026-11-20)
    if (this.ending.ended()) items.push({ key: 'ended', label: 'Game result and replay' });
    items.push(
      { key: 'chat', label: 'Chat and game log' },
      { key: 'info', label: 'Game info' },
      { key: 'settings', label: 'Settings' },
    );
    if (this.cues.fullscreenAvailable()) {
      items.push({ key: 'fullscreen', label: this.cues.fullscreen() ? 'Leave full screen' : 'Full screen' });
    }
    if (playing && !model.ended && this.undo.allowed()) items.push({ key: 'revert', label: 'Revert...' });
    if (playing && !model.ended) {
      items.push({ key: 'cancel', label: 'Request game cancel' }, { key: 'concede', label: 'Concede game' });
    }
    items.push({ key: 'hall', label: 'Back to the game hall' });
    return items;
  });

  protected choose(key: string): void {
    switch (key) {
      case 'theme':
        this.theme.select(this.nextTheme());
        break;
      case 'chat':
      case 'info':
        this.sheets.open({ kind: key });
        break;
      case 'settings':
        this.sheets.open({ kind: 'settings', tab: 'game' });
        break;
      case 'cancel':
      case 'concede':
        this.sheets.open({ kind: 'confirm', what: key });
        break;
      case 'revert':
        // The list is a dialog over the table: the sheets go
        this.sheets.closeAll();
        this.undo.openList();
        break;
      case 'ended':
        this.sheets.closeAll();
        this.ending.show();
        break;
      case 'hall':
        this.cues.go(HALL);
        break;
      case 'fullscreen':
        // The menu closes: the game fills the screen behind it
        this.sheets.closeAll();
        this.cues.toggleFullscreen();
        break;
    }
  }
}
