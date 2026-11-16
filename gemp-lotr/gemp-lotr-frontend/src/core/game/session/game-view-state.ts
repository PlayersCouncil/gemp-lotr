import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { readSheet, SheetEntry } from '../phone/sheet';
import { PileName } from '../table/game-table';
import { GameSession } from './game-session.service';

/** A card's open menu of actions: the card, where it was clicked, and the decision it is for (decisionKey). */
export interface SavedMenu {
  cardId: string;
  x: number;
  y: number;
  decision: string;
}

/** Where the phone layout's table was scrolled, and what the hand's place shows (D10 for G7). */
export interface PhoneView {
  /** How far the table is scrolled down (px). */
  tableTop: number;
  /** How far each row is scrolled sideways (px), by row. */
  rows: Readonly<Record<string, number>>;
  /** The offer of cards from off the table closed (✕) to see the hand: its key (OffTableChoices). */
  closedOffer: string | null;
  /** How many chat lines the player has seen (the chat button counts the others). */
  chatSeen: number;
}

export const NO_PHONE_VIEW: PhoneView = { tableTop: 0, rows: {}, closedOffer: null, chatSeen: 0 };

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
  /** The piles open in dialogs, in the order they were opened. */
  readonly pileDialogs = signal<readonly { pile: PileName; player: string }[]>([]);
  /** The card shown in the card information: a card of the game by its id, or a card named in a text. */
  readonly cardInfo = signal<{ cardId: string | null; blueprintId: string } | null>(null);
  /** Card ids in the order the player arranged them by dragging (the others follow in the server's order). */
  readonly cardOrder = signal<readonly string[]>([]);
  /** How far the chat box's list is scrolled (px), or null at its end. */
  readonly chatScroll = signal<number | null>(null);
  /** The phone layout's sheets, the one shown last (GameSheets). */
  readonly sheets = signal<readonly SheetEntry[]>([]);
  /** The phone layout's table and hand (PhoneView). */
  readonly phone = signal<PhoneView>(NO_PHONE_VIEW);

  constructor() {
    effect(() => {
      const view = {
        tab: this.tab(),
        openPiles: this.openPiles(),
        menu: this.menu(),
        panelScroll: this.panelScroll(),
        chatScroll: this.chatScroll(),
        pileDialogs: this.pileDialogs(),
        cardOrder: this.cardOrder(),
        cardInfo: this.cardInfo(),
        sheets: this.sheets(),
        phone: this.phone(),
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
    const info = saved['cardInfo'] as { cardId?: unknown; blueprintId?: unknown } | null | undefined;
    if (info && typeof info.blueprintId === 'string') {
      this.cardInfo.set({
        cardId: typeof info.cardId === 'string' ? info.cardId : null,
        blueprintId: info.blueprintId,
      });
    }
    const order = saved['cardOrder'];
    if (Array.isArray(order)) this.cardOrder.set(order.filter((id): id is string => typeof id === 'string'));
    const dialogs = saved['pileDialogs'];
    if (Array.isArray(dialogs)) {
      this.pileDialogs.set(
        dialogs.filter(
          (d): d is { pile: PileName; player: string } =>
            typeof (d as { pile?: unknown }).pile === 'string' &&
            typeof (d as { player?: unknown }).player === 'string',
        ),
      );
    }
    const sheets = saved['sheets'];
    if (Array.isArray(sheets)) this.sheets.set(sheets.flatMap((entry) => readSheet(entry) ?? []));
    const phone = saved['phone'] as Partial<PhoneView> | null | undefined;
    if (phone && typeof phone === 'object') {
      const rows = phone.rows && typeof phone.rows === 'object' ? phone.rows : {};
      this.phone.set({
        tableTop: typeof phone.tableTop === 'number' ? phone.tableTop : 0,
        rows: Object.fromEntries(Object.entries(rows).filter(([, left]) => typeof left === 'number')),
        closedOffer: typeof phone.closedOffer === 'string' ? phone.closedOffer : null,
        chatSeen: typeof phone.chatSeen === 'number' ? phone.chatSeen : 0,
      });
    }
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

  /** A pile's dialog opened (again: in front). */
  openPile(pile: PileName, player: string): void {
    this.pileDialogs.update((open) => [
      ...open.filter((d) => d.pile !== pile || d.player !== player),
      { pile, player },
    ]);
  }

  closePile(pile: PileName, player: string): void {
    this.pileDialogs.update((open) => open.filter((d) => d.pile !== pile || d.player !== player));
  }

  togglePiles(index: number): void {
    this.openPiles.update((open) => (open.includes(index) ? open.filter((i) => i !== index) : [...open, index]));
  }
}
