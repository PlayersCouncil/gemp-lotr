import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { GameSession } from './game-session.service';

/** A card's open menu of actions: the card, where it was clicked, and the decision it is for (decisionKey). */
export interface SavedMenu {
  cardId: string;
  x: number;
  y: number;
  decision: string;
}

/**
 * What the game page's view holds on its own: the open tab, the players whose other piles are shown, and a card's open
 * menu of actions. Kept with the
 * game (GameSession.keepView), so a reload shows the page as it was left (D10 of trymarz/gemp-lotr-private#27).
 * Page-scoped.
 */
@Injectable()
export class GameViewState {
  private readonly session = inject(GameSession);

  readonly tab = signal('chat');
  /** The indexes of the players whose other piles are shown. */
  readonly openPiles = signal<readonly number[]>([]);
  readonly menu = signal<SavedMenu | null>(null);
  /** How far the open tab's panel is scrolled (px). */
  readonly panelScroll = signal(0);
  /** How far the chat box's list is scrolled (px), or null at its end. */
  readonly chatScroll = signal<number | null>(null);

  constructor() {
    effect(() => {
      const view = {
        tab: this.tab(),
        openPiles: this.openPiles(),
        menu: this.menu(),
        panelScroll: this.panelScroll(),
        chatScroll: this.chatScroll(),
      };
      untracked(() => this.session.keepView(view));
    });
  }

  /** Takes up what the page held for this game before a reload. */
  restore(gameId: string): void {
    const saved = this.session.savedView(gameId);
    if (typeof saved['tab'] === 'string') this.tab.set(saved['tab']);
    if (typeof saved['panelScroll'] === 'number') this.panelScroll.set(saved['panelScroll']);
    if (typeof saved['chatScroll'] === 'number') this.chatScroll.set(saved['chatScroll']);
    const piles = saved['openPiles'];
    if (Array.isArray(piles)) this.openPiles.set(piles.filter((index) => typeof index === 'number'));
    const menu = saved['menu'] as Partial<SavedMenu> | null | undefined;
    if (
      menu &&
      typeof menu.cardId === 'string' &&
      typeof menu.x === 'number' &&
      typeof menu.y === 'number' &&
      typeof menu.decision === 'string'
    ) {
      this.menu.set({ cardId: menu.cardId, x: menu.x, y: menu.y, decision: menu.decision });
    }
  }

  togglePiles(index: number): void {
    this.openPiles.update((open) => (open.includes(index) ? open.filter((i) => i !== index) : [...open, index]));
  }
}
