import { Component, computed, inject } from '@angular/core';
import { THEME_LABELS, THEMES, ThemeService } from '../../../core/platform/theme.service';

/**
 * The game page's theme button: switches to the next theme, which its title names (the deck builder's red button;
 * the choice is remembered in `gempGameTheme`). The page shows it only with GAME_THEME_PICKER.
 *
 * DOM contract: `button.gd-button.gd-game-theme[data-icon=theme]`.
 */
@Component({
  selector: 'gd-game-theme-button',
  styleUrl: './game-theme-button.component.css',
  template: `
    <button
      type="button"
      class="gd-button gd-game-theme"
      data-icon="theme"
      [title]="label()"
      [attr.aria-label]="label()"
      (click)="theme.select(next())"
    ></button>
  `,
})
export class GameThemeButtonComponent {
  protected readonly theme = inject(ThemeService);

  /** The theme the button switches to: the next one, after the last the first. */
  protected readonly next = computed(
    () => THEMES[(THEMES.indexOf(this.theme.theme()) + 1) % THEMES.length] ?? THEMES[0],
  );
  protected readonly label = computed(() => `Switch to the ${THEME_LABELS[this.next()]} theme`);
}
