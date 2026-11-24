/**
 * What a scenario's message can point at, found on the game page: a card, a printed part of one, a row of the
 * table, a pile's counter, a piece of the page (the twilight pool, the alert box, a button...). The names are the
 * scenario format's (Anchors.java checks them); each is looked up by the game page's DOM contract and measured. A
 * thing that isn't on the screen gives null.
 */
import { around, Rect } from '../../../core/game/scenario/bubble-placement';
import { PartRect, ScenarioSeat } from '../../../core/game/scenario/scenario-events';
import { GameModel } from '../../../core/game/state/game-model';

/** What the lookup needs to know of the game. */
export interface AnchorContext {
  /** Where to look: the page. */
  root: ParentNode;
  /** The card id each name used by the step has. */
  cards: Readonly<Record<string, string>>;
  /** Where a printed part of a card is on it, by "name:part" in lower case. */
  parts: Readonly<Record<string, PartRect>>;
  /** The card id of each site on the adventure path, by its number. */
  sites: Readonly<Record<number, string>>;
  /** The places of the page's player and of the opponent among the players (the state panel counts by them). */
  self: number | null;
  opponent: number | null;
  humanSeat: ScenarioSeat;
  /**
   * The role the person plays at this moment (the shown step's, else their seat's): in a two-sided game the Free
   * Peoples player on their own turn and the Shadow player on the opponent's. What "self" and "opp" in a skirmish
   * are.
   */
  humanRole: ScenarioSeat;
  /**
   * The page looks from the seat the person does not sit in (flip table): "self" is then the player at the top of the
   * table, and the person's hand is not shown. Absent: not flipped.
   */
  flipped?: boolean;
}

/**
 * The lookup's context on the game page: the page's game (its sites, and the players' places), with the names the
 * scenario gives cards and where their printed parts are. The scenario's layer and the authoring panel's picking
 * use the same.
 */
export function pageAnchorContext(
  root: ParentNode,
  model: GameModel,
  named: Pick<AnchorContext, 'cards' | 'parts' | 'humanSeat' | 'humanRole' | 'flipped'>,
): AnchorContext {
  const own = model.participantId === null ? -1 : model.players.indexOf(model.participantId);
  const other = model.players.findIndex((player) => player !== model.participantId);
  // (flipped, the page's own player is the other seat's: the person is the other player of the game)
  const [self, opponent] = named.flipped ? [other, own] : [own, other];
  return {
    root,
    ...named,
    sites: Object.fromEntries(
      Object.values(model.cards).flatMap((card) =>
        card.zone === 'ADVENTURE_PATH' && card.index !== null ? [[card.index, card.id] as const] : [],
      ),
    ),
    self: self < 0 ? null : self,
    opponent: opponent < 0 ? null : opponent,
  };
}

const ROWS: Readonly<Record<string, string>> = {
  'self.hand': 'hand',
  'self.characters': 'characters-player',
  'self.support': 'support-player',
  'opp.characters': 'characters-opponent',
  'opp.support': 'support-opponent',
  minions: 'shadow',
};

/** A zone's name as seen from the other seat (flip table): the person's rows are the top ones, their hand not shown. */
function flippedZone(zone: string): string {
  if (zone === 'self.hand') return '';
  if (zone.startsWith('self.')) return `opp.${zone.slice(5)}`;
  return zone.startsWith('opp.') ? `self.${zone.slice(4)}` : zone;
}

const PILES: Readonly<Record<string, string>> = {
  deck: 'deck',
  discard: 'discard',
  deadpile: 'deadPile',
  adventuredeck: 'adventureDeck',
  removed: 'removedPile',
  hand: 'hand',
};

const PIECES: Readonly<Record<string, string>> = {
  twilightpool: '[data-game="twilight"]',
  phase: '[data-game="phase"]',
  initiative: '[data-game="initiative"]',
  ruleof4: '[data-game="ruleof4"]',
  clock: '.gd-game-state__decision-clock',
  advpath: '.gd-game__path',
  alertbox: 'gd-game-alert',
  chat: 'gd-game-tabs',
  gamelog: 'gd-game-tabs',
  settings: 'a[data-tab="settings"]',
  cardinfo: 'dialog.gd-card-info[open]',
  stats: 'gd-game-stats',
};

const NUMBERS: Readonly<Record<string, string>> = {
  strength: 'strength',
  vitality: 'vitality',
  resistance: 'resistance',
  sitenumber: 'site',
};

const BUTTONS: Readonly<Record<string, readonly string[]>> = {
  pass: ['Pass'],
  done: ['Done'],
  reset: ['Reset choice', 'Clear selection'],
};

/** The rectangle an element takes on the screen; null when it takes none (hidden, or not laid out). */
export function rectOf(element: Element | null | undefined): Rect | null {
  if (!element) return null;
  const box = element.getBoundingClientRect();
  return box.width > 0 && box.height > 0
    ? { left: box.left, top: box.top, width: box.width, height: box.height }
    : null;
}

/** The rectangle around every one of the elements that is on the screen. */
export function rectAround(elements: Iterable<Element>): Rect | null {
  const rects: Rect[] = [];
  for (const element of elements) {
    const rect = rectOf(element);
    if (rect) rects.push(rect);
  }
  return around(rects);
}

/**
 * A card of the game on the page, by its id. The same card can be drawn twice (on the table and in a pile's or a
 * decision's dialog): the one in a dialog is the one in front. A card that isn't on the table but has an action is
 * drawn under a made-up id.
 */
export function cardElement(root: ParentNode, cardId: string): Element | null {
  const shown = [...root.querySelectorAll(`[data-card-id="${cardId}"], [data-card-id="extra${cardId}"]`)].filter(
    (element) => rectOf(element) !== null,
  );
  const inDialog = shown.filter((element) => element.closest('dialog') !== null);
  return inDialog.at(-1) ?? shown.at(-1) ?? null;
}

/** What is left of a rectangle beside another that covers part of it: the largest strip clear of it. */
function clearOf(rect: Rect, cover: Rect): Rect {
  const right = rect.left + rect.width;
  const bottom = rect.top + rect.height;
  const coverRight = cover.left + cover.width;
  const coverBottom = cover.top + cover.height;
  if (cover.left >= right || coverRight <= rect.left || cover.top >= bottom || coverBottom <= rect.top) return rect;
  const strips: Rect[] = [
    { left: rect.left, top: rect.top, width: cover.left - rect.left, height: rect.height },
    { left: coverRight, top: rect.top, width: right - coverRight, height: rect.height },
    { left: rect.left, top: rect.top, width: rect.width, height: cover.top - rect.top },
    { left: rect.left, top: coverBottom, width: rect.width, height: bottom - coverBottom },
  ].filter((strip) => strip.width > 0 && strip.height > 0);
  const largest = strips.sort((a, b) => b.width * b.height - a.width * a.height).at(0);
  // (wholly covered: there is nothing better to point at than where it is)
  return largest ?? rect;
}

/**
 * Where a card shows on the screen. A card lying under another (attached to it, stacked on it) only peeks out from
 * under it: what points at such a card, or says to click it, means the part that can be seen and clicked.
 */
export function cardRect(card: Element | null | undefined): Rect | null {
  let shown = rectOf(card);
  if (!card || !shown || !card.hasAttribute('data-under')) return shown;
  // (the cards drawn after it in its stack lie over it)
  for (let above = card.nextElementSibling; above; above = above.nextElementSibling) {
    const cover = above.hasAttribute('data-card-id') ? rectOf(above) : null;
    if (cover) shown = clearOf(shown, cover);
  }
  return shown;
}

function all(root: ParentNode, selector: string): Rect | null {
  return rectAround(root.querySelectorAll(selector));
}

function cardPart(context: AnchorContext, alias: string, part: string): Rect | null {
  const cardId = context.cards[alias];
  const card = cardId === undefined ? null : cardElement(context.root, cardId);
  const whole = rectOf(card);
  if (!card || !whole) return null;
  // What the game draws on the card: its wounds, and the figures as they stand now
  if (part === 'wounds') return all(card, 'img.gd-game-token[src*="wound"]') ?? whole;
  const figure = NUMBERS[part];
  const drawn = figure === undefined ? null : all(card, `.gd-game-number[data-number="${figure}"]`);
  if (drawn) return drawn;
  // What is printed on it: where the server says that part of the card's face is
  const printed = context.parts[`${alias.toLowerCase()}:${part}`];
  if (!printed) return whole;
  const [left, top, width, height] = printed;
  return {
    left: whole.left + (whole.width * left) / 100,
    top: whole.top + (whole.height * top) / 100,
    width: (whole.width * width) / 100,
    height: (whole.height * height) / 100,
  };
}

function skirmish(context: AnchorContext, what: string | undefined): Rect | null {
  const fight = context.root.querySelector('.gd-game__fight[data-fight="skirmish"]');
  if (!fight) return null;
  // (the sides of a skirmish are its roles: on the opponent's turn of a two-sided game the person's are the minions)
  const own = context.humanRole === 'freeps' ? 'free-peoples' : 'shadow';
  const other = context.humanRole === 'freeps' ? 'shadow' : 'free-peoples';
  switch (what) {
    case undefined:
      return rectOf(fight);
    case 'freeps':
      return all(fight, '.gd-game__side[data-side="free-peoples"] [data-card-id]');
    case 'shadow':
      return all(fight, '.gd-game__side[data-side="shadow"] [data-card-id]');
    case 'selfstrength':
      return all(fight, `.gd-game__strength[data-side="${own}"]`);
    case 'oppstrength':
      return all(fight, `.gd-game__strength[data-side="${other}"]`);
    case 'fpstrength':
      return all(fight, '.gd-game__strength[data-side="free-peoples"]');
    case 'shadowstrength':
      return all(fight, '.gd-game__strength[data-side="shadow"]');
    case 'selfdamage':
      return all(fight, `.gd-game__damage[data-side="${own}"]:not([data-none])`);
    case 'oppdamage':
      return all(fight, `.gd-game__damage[data-side="${other}"]:not([data-none])`);
    default:
      return null;
  }
}

function piece(context: AnchorContext, name: string, rest: readonly string[]): Rect | null {
  const { root } = context;
  switch (name) {
    case 'site': {
      const cardId = context.sites[Number(rest[0])];
      return cardId === undefined ? null : all(root, `.gd-game__path [data-card-id="${cardId}"]`);
    }
    case 'button': {
      const labels = BUTTONS[(rest[0] ?? '').toLowerCase()] ?? [];
      return all(root, labels.map((label) => `button[data-decision-button="${label}"]`).join(', ') || 'none');
    }
    case 'assignment': {
      const cardId = context.cards[rest[0] ?? ''];
      const fights = [...root.querySelectorAll('.gd-game__fight[data-fight="assignment"]')];
      return rectOf(fights.find((fight) => fight.querySelector(`[data-card-id="${cardId ?? ''}"]`) !== null));
    }
    case 'skirmish':
      return skirmish(context, rest[0]?.toLowerCase());
    case 'stats':
      if (rest[0]?.toLowerCase() === 'archery') {
        return all(root, '[data-game="fp-archery"], [data-game="shadow-archery"]');
      }
      if (rest[0]?.toLowerCase() === 'move') return all(root, '[data-game="move"]');
      return all(root, 'gd-game-stats');
    case 'dialog':
      // (the one opened or pressed last is the one in front)
      return rectOf([...root.querySelectorAll('dialog.gd-dialog[open]')].at(-1));
    default: {
      const selector = PIECES[name];
      return selector === undefined ? null : all(root, selector);
    }
  }
}

/**
 * Where on the screen the thing an anchor names is.
 * @param onCards A row of the table is where its cards are (what a bubble points at); otherwise the whole row (what
 *                is outlined, or kept clear of). An empty row is the whole row either way.
 */
export function resolveAnchor(context: AnchorContext, anchor: string, onCards = false): Rect | null {
  const [kind = '', ...rest] = anchor.split(':');
  const name = rest[0] ?? '';
  switch (kind.toLowerCase()) {
    case 'card': {
      if (rest[1] !== undefined) return cardPart(context, name, rest[1].toLowerCase());
      const cardId = context.cards[name];
      return cardId === undefined ? null : cardRect(cardElement(context.root, cardId));
    }
    case 'zone': {
      const zone = context.flipped ? flippedZone(name.toLowerCase()) : name.toLowerCase();
      const row = context.root.querySelector(`.gd-game__row[data-row="${ROWS[zone] ?? ''}"]`);
      if (!row) return null;
      return (onCards ? all(row, '[data-card-id]') : null) ?? rectOf(row);
    }
    case 'pile': {
      const [whose = '', pile = ''] = name.toLowerCase().split('.');
      const player = whose === 'self' ? context.self : whose === 'opp' ? context.opponent : null;
      const counter = PILES[pile];
      return player === null || counter === undefined ? null : all(context.root, `[data-stat="${counter}${player}"]`);
    }
    case 'ui':
      return piece(context, name.toLowerCase(), rest.slice(1));
    default:
      return null;
  }
}
