import { inject, Injectable, signal } from '@angular/core';
import { PageNavigation, ReservedTab } from '../../platform/page-navigation';

/** What became ready: a game (game page) or a draft (soloDraft.html / tableDraft.html). */
export type ReadyKind = 'game' | 'draft';

/**
 * A "your game is ready" prompt, after hallUi.js showReadyModal: shown when the browser blocked the new tab, with the same
 * page behind a real link (a click is a gesture). It has no close button; it goes when its link is followed, or when
 * its game or draft is over.
 */
export interface ReadyOffer {
  /** "game:<gameId>" or "draft:<tournamentId>". */
  readonly key: string;
  readonly kind: ReadyKind;
  readonly url: string;
  readonly title: string;
  readonly text: string;
  readonly label: string;
}

/** The prompts' wording (openOrOffer's callers). */
export const READY_WORDS: Readonly<Record<ReadyKind, { title: string; text: string; label: string }>> = {
  game: {
    title: 'Your game is ready',
    text: 'Your game has started, but your browser blocked the new window.',
    label: 'Open game',
  },
  draft: {
    title: 'Your draft is ready',
    text: 'Your draft has started, but your browser blocked the new window.',
    label: 'Open draft',
  },
};

export const gameKey = (gameId: string) => `game:${gameId}`;
export const draftKey = (tournamentId: string) => `draft:${tournamentId}`;

/**
 * Games and drafts that became ready (hallUi.js openOrOffer / showReadyModal / closeReadyModal): opened in a new tab,
 * or, when the browser blocks it (a tab opened from a poll answer has no click behind it), offered in a prompt.
 * Shared by the tables (started games), the tournaments (drafts) and the scenarios (a scenario's game).
 */
@Injectable()
export class HallReadyOffers {
  private readonly navigation = inject(PageNavigation);

  private readonly offers_ = signal<readonly ReadyOffer[]>([]);
  /** The prompts showing, oldest first. */
  readonly offers = this.offers_.asReadonly();

  /** Opens `url` in a new tab; when blocked, offers it. True when the tab opened. */
  openOrOffer(key: string, kind: ReadyKind, url: string): boolean {
    if (this.navigation.openInNewTab(url)) {
      return true;
    }
    this.offer(key, kind, url);
    return false;
  }

  /** Points a tab reserved on a click at `url` (PlayScenario.open); offers it when there is none, or it was closed. */
  openReserved(tab: ReservedTab | null, key: string, kind: ReadyKind, url: string): void {
    if (!tab?.go(url)) {
      this.offer(key, kind, url);
    }
  }

  /** Shows the prompt, once per key. */
  offer(key: string, kind: ReadyKind, url: string): void {
    if (this.offers_().some((offer) => offer.key === key)) {
      return;
    }
    this.offers_.update((offers) => [...offers, { key, kind, url, ...READY_WORDS[kind] }]);
  }

  has(key: string): boolean {
    return this.offers_().some((offer) => offer.key === key);
  }

  /** The prompt's link was followed, or its game or draft is over. */
  dismiss(key: string): void {
    if (this.has(key)) {
      this.offers_.update((offers) => offers.filter((offer) => offer.key !== key));
    }
  }

  /** Closes every prompt of `kind` whose key is not in `live` (after a full snapshot: pruneReadyModals). */
  keepOnly(kind: ReadyKind, live: ReadonlySet<string>): void {
    if (this.offers_().some((offer) => offer.kind === kind && !live.has(offer.key))) {
      this.offers_.update((offers) => offers.filter((offer) => offer.kind !== kind || live.has(offer.key)));
    }
  }
}
