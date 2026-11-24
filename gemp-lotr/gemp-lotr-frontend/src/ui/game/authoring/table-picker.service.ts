import { inject, Injectable } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import {
  aliasesOf,
  cardAnchor,
  cardIdsOf,
  fightingSide,
  PickCandidate,
  PICK_TOLD,
  PickHit,
  PLACES,
  smallestAt,
} from '../../../core/game/authoring/picking';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { AnchorContext, cardRect, pageAnchorContext, rectOf, resolveAnchor } from '../scenario/scenario-anchors';
import { TablePick } from '../table-edit/table-pick';

/** How a pick is asked for: what the author is told meanwhile (the panel's notice). */
export interface PickOptions {
  told?: string;
}

export type { PickHover } from '../table-edit/table-pick';

/** Where the panel is: a click in it takes nothing (and ends the picking). */
const PANEL = 'gd-game-author';
/** What lies over the table without being of it. */
const OVER_THE_TABLE = 'gd-game-author, gd-game-scenario';

/**
 * Picking something on the game table for a message to be about (the legacy scenarioAuthor.js pick, stopPicking,
 * thingAt): while it lasts the pointer is a crosshair, the panel tells the author what to do, the thing under the
 * pointer is outlined with its anchor beside it (`gd-game-author-pickbox` draws them from `hover`), and nothing on the
 * table hears a press. A click on the table takes what it is on: `card:<alias>` for one of the scenario's cards (the
 * one on top there), `ui:site:<n>` for a site on the adventure path, `ui:assignment:<alias>` for a character's
 * assignment, and any of the places and pieces of PLACES; the smallest of them under the pointer wins. Exactly the
 * anchors scenario-anchors.ts finds, measured by it, so that what is picked points where the author clicked. A click
 * in the panel, a click on nothing, Escape, or another pick ends it with nothing.
 *
 * The picking itself (the listeners, the crosshair, the box) is the shared TablePick's (../table-edit/table-pick.ts),
 * which the on-table edit controls use too. Provided by the authoring panel (AuthoringPanelComponent); its anchor
 * fields (widgets/anchor-field.component.ts) use it.
 */
@Injectable()
export class TablePicker extends TablePick {
  private readonly authoring = inject(Authoring);
  private readonly session = inject(GameSession);
  private readonly guide = inject(ScenarioGuide);

  /**
   * Lets the author click something on the table; resolves to its anchor, or null when nothing was taken (Escape, a
   * click in the panel or on nothing, `cancel`, another pick).
   */
  pick(options: PickOptions = {}): Promise<string | null> {
    return this.start(options.told ?? PICK_TOLD);
  }

  protected began(told: string): void {
    this.authoring.startPicking(told);
  }

  protected ended(): void {
    this.authoring.stopPicking();
  }

  protected override outside(): string {
    return PANEL;
  }

  /** Where the page's anchors are looked up, with the scenario's names for the cards on the table. */
  private context(): AnchorContext {
    return pageAnchorContext(this.document, this.session.model(), {
      cards: cardIdsOf(this.authoring.draft()?.board),
      parts: {},
      humanSeat: this.guide.info()?.humanSeat ?? 'freeps',
      humanRole: this.guide.role(),
    });
  }

  /** The thing a click at a point of the window would pick, and the box round it; null for nothing. */
  thingAt(x: number, y: number): PickHit | null {
    const context = this.context();
    const candidates: PickCandidate[] = [];
    const card = this.topCard(x, y);
    const cardId = card?.getAttribute('data-card-id');
    if (card && cardId) {
      const known = this.session.model().cards[cardId.replace(/^extra/, '')];
      const site = known?.zone === 'ADVENTURE_PATH' ? known.index : null;
      const anchor = cardAnchor(cardId, aliasesOf(this.authoring.draft()?.board), site);
      if (anchor) candidates.push({ anchor, rect: rectOf(card), box: cardRect(card) });
    }
    for (const place of PLACES) candidates.push({ anchor: place, rect: resolveAnchor(context, place) });
    for (const [alias, id] of Object.entries(context.cards)) {
      if (this.assigned(id)) {
        const anchor = `ui:assignment:${alias}`;
        candidates.push({ anchor, rect: resolveAnchor(context, anchor) });
      }
    }
    return smallestAt(x, y, candidates);
  }

  /**
   * The card on top at a point: of what is there (the panel and the scenario's layer aside), the card the first thing
   * is part of; none if the first thing is no card's (a dialog over the table, say).
   */
  private topCard(x: number, y: number): Element | null {
    const first = this.document.elementsFromPoint(x, y).find((element) => element.closest(OVER_THE_TABLE) === null);
    return first?.closest('[data-card-id]') ?? null;
  }

  /** Whether the card is the character of one of the assignments on the table. */
  private assigned(cardId: string): boolean {
    return (
      this.document.querySelector(
        `.gd-game__fight[data-fight="assignment"] .gd-game__side[data-side="free-peoples"] [data-card-id="${cardId}"]`,
      ) !== null
    );
  }

  /** For `card:<alias>`: the side of the skirmish being fought that card is on, or null. */
  fightingSide(anchor: string): 'freeps' | 'shadow' | null {
    return fightingSide(anchor, cardIdsOf(this.authoring.draft()?.board), this.session.model().skirmish);
  }
}
