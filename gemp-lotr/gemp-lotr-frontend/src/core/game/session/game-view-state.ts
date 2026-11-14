import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { GameSession } from './game-session.service';

/**
 * What the game page's view holds on its own: the open tab, and the players whose other piles are shown. Kept with the
 * game (GameSession.keepView), so a reload shows the page as it was left (D10 of trymarz/gemp-lotr-private#27).
 * Page-scoped.
 */
@Injectable()
export class GameViewState {
  private readonly session = inject(GameSession);

  readonly tab = signal('chat');
  /** The indexes of the players whose other piles are shown. */
  readonly openPiles = signal<readonly number[]>([]);

  constructor() {
    effect(() => {
      const view = { tab: this.tab(), openPiles: this.openPiles() };
      untracked(() => this.session.keepView(view));
    });
  }

  /** Takes up what the page held for this game before a reload. */
  restore(gameId: string): void {
    const saved = this.session.savedView(gameId);
    if (typeof saved['tab'] === 'string') this.tab.set(saved['tab']);
    const piles = saved['openPiles'];
    if (Array.isArray(piles)) this.openPiles.set(piles.filter((index) => typeof index === 'number'));
  }

  togglePiles(index: number): void {
    this.openPiles.update((open) => (open.includes(index) ? open.filter((i) => i !== index) : [...open, index]));
  }
}
