import { afterEveryRender, Directive, ElementRef, inject } from '@angular/core';
import { GameViewState } from '../../../core/game/session/game-view-state';

/**
 * A tab's panel keeps how far it is scrolled with the game (GameViewState.panelScroll), so a reload shows it where it
 * was; a panel opened by the player starts at its top. After a reload part of the panel comes only with the game, so
 * the place is taken up again as the panel grows, until it is reached.
 */
@Directive({
  selector: '[gdKeepScroll]',
  host: { '(scroll)': 'scrolled()' },
})
export class KeepScrollDirective {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly view = inject(GameViewState);
  /** The place still to reach, or null. */
  private pending: number | null = this.view.panelScroll() > 0 ? this.view.panelScroll() : null;

  constructor() {
    afterEveryRender(() => {
      if (this.pending === null) return;
      this.host.scrollTop = this.pending;
      if (Math.abs(this.host.scrollTop - this.pending) < 1) this.pending = null;
    });
  }

  protected scrolled(): void {
    // Moves made to reach the kept place aren't the player's
    if (this.pending === null) this.view.panelScroll.set(this.host.scrollTop);
  }
}
