import { inject, Injectable, signal } from '@angular/core';
import { SessionStore } from '../../platform/session-store';
import { AuthoringTab, DraftSeat } from './authoring-state';
import { Selection } from './selection';

/** Where the panel remembers its view in the tab's session storage (the legacy panel's key, kept). */
export const AUTHOR_UI_KEY = 'gemp-author-ui';

/** A card the Board tab will add to the scenario. */
export interface GatheredCard {
  alias: string;
  blueprintId: string;
  name: string;
  type: string;
  seat: DraftSeat;
  /** As the Board tab's place select has it ("" = the bottom of the deck). */
  zone: string;
  /** For a card attached to or stacked on another: that one's alias. */
  host: string | null;
}

/** What the Board tab has gathered and not yet sent, for the draft it was gathered in. */
export interface GatheredBoard {
  draft: string;
  /** Changes to the board ("wound", ["aragorn", 1]), in the order they were made. */
  ops: readonly { name: string; args: readonly (string | number)[] }[];
  added: readonly GatheredCard[];
  /** Aliases of cards to take out. */
  removed: readonly string[];
  /** Where the story starts, the parts changed. */
  start?: StartChange;
}

/**
 * What the Board tab has gathered of where the story starts (setup.startAt and the parts that go with it): only the
 * parts changed; null is the part's default.
 */
export interface StartChange {
  site?: number;
  opponentSite?: number | null;
  phase?: string;
  turn?: number;
  twilight?: number;
  moves?: number | null;
  skirmishes?: Readonly<Record<string, readonly string[]>>;
  controlled?: Partial<Record<DraftSeat, readonly string[]>>;
  path?: readonly string[];
}

/**
 * What the panel remembers of its view from one game of the draft to the next (each fresh game is a new page): the
 * tab, the step selected, folding, and what the tabs gather.
 */
export interface AuthoringUi extends Selection {
  tab: AuthoringTab;
  folded: boolean;
  /** Board: changes go to the board as it stands, or to the one the scenario starts with. */
  where: 'now' | 'setup';
  board: GatheredBoard | null;
  /** Board: the seats' paths are edited apart; null until the Board tab has looked. */
  sitesApart: boolean | null;
  /** The skirmishes page: aliases given a skirmish section before any step was written for it. */
  newFights: readonly string[];
  /** Something that went wrong, carried to the page of the next game (said there as the panel's problem). */
  carried: string | null;
}

const TABS: readonly AuthoringTab[] = ['timeline', 'step', 'opponent', 'board', 'scenario'];

const text = (value: unknown) => (typeof value === 'string' ? value : null);
const count = (value: unknown) => (typeof value === 'number' && Number.isFinite(value) ? value : null);

/** The view as remembered, with anything missing or not understood as it starts. */
export function recallUi(value: unknown): AuthoringUi {
  const saved = value !== null && typeof value === 'object' ? (value as Readonly<Record<string, unknown>>) : {};
  const tab = TABS.find((one) => one === saved['tab']) ?? 'timeline';
  const board = saved['board'];
  return {
    tab,
    selected: count(saved['selected']),
    selectedUid: text(saved['selectedUid']),
    sub: text(saved['sub']),
    subAt: count(saved['subAt']),
    follow: saved['follow'] !== false,
    atUid: text(saved['atUid']),
    hold: saved['hold'] === true,
    jump: saved['jump'] === true,
    folded: saved['folded'] === true,
    where: saved['where'] === 'setup' ? 'setup' : 'now',
    board: board !== null && typeof board === 'object' ? (board as GatheredBoard) : null,
    sitesApart: typeof saved['sitesApart'] === 'boolean' ? saved['sitesApart'] : null,
    newFights: Array.isArray(saved['newFights'])
      ? (saved['newFights'] as unknown[]).filter((one): one is string => typeof one === 'string')
      : [],
    carried: text(saved['carried']),
  };
}

/** The panel's remembered view (AuthoringUi), kept in the tab's session storage as it changes. Page-scoped. */
@Injectable()
export class AuthoringUiStore {
  private readonly store = inject(SessionStore);
  private readonly value = signal(recallUi(this.store.read(AUTHOR_UI_KEY)));
  readonly ui = this.value.asReadonly();

  /** Changes some of it, and remembers it. */
  update(patch: Partial<AuthoringUi>): void {
    this.value.update((ui) => ({ ...ui, ...patch }));
    this.remember();
  }

  remember(): void {
    this.store.write(AUTHOR_UI_KEY, this.value());
  }
}
