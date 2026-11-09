import { siteImage } from '../platform/app-paths';

/**
 * Card image and blueprint rules, ported from the legacy js/gemp-022/cards/Card.js (static methods).
 * The lookup tables stay in the legacy data files and are turned into JSON at build time (card-data.ts), so adding
 * cards still means editing only those files. card-blueprint.parity.spec.ts runs the legacy Card.js against this port.
 *
 * Blueprint ID markers: "*" = foil, "^" = alternate image, "T" after the card number = tengwar, e.g. "1_2*", "1_2T".
 */

export type ImageTable = Readonly<Record<string, string>>;

/** The legacy lookup tables, by the global name each data file defines. */
export interface CardImageTables {
  /** cards/CardImages.js: card back and other special images */
  fixedImages: ImageTable;
  /** set40.js: 2nd edition fan set */
  set40: ImageTable;
  /** hobbit.js: Hobbit draft game */
  hobbit: ImageTable;
  /** PC_Cards.js: Players Council cards */
  PCCards: ImageTable;
  /** cards/PackImages.js: booster and starter product images */
  packBlueprints: ImageTable;
}

export const CARD_BACK_BLUEPRINT = '-1_1';
const DECIPHER_IMAGES = 'https://i.lotrtcgpc.net/decipher/';
/** Absolute here: the legacy relative "images/..." would resolve against the Angular app's base URL. */
const FUTURE_PRIZE_IMAGE = siteImage('future_prize.jpg');
export const RULES_IMAGE = siteImage('rules.png');

/** Cards whose errata got a remade image, by set (Card.remadeErratas). */
const REMADE_ERRATAS: Partial<Record<number, readonly number[]>> = {
  0: [7],
  1: [3, 12, 43, 46, 55, 109, 113, 138, 162, 211, 235, 263, 309, 318, 331, 338, 343, 360],
  3: [48, 110],
  4: [63, 236, 237, 352],
  6: [39, 46, 85],
  7: [10, 14, 66, 114, 133, 134, 135, 182, 284, 285, 289, 302, 357],
  8: [20, 33, 69],
  17: [15, 87, 96, 118],
  18: [8, 12, 20, 25, 35, 48, 50, 55, 77, 78, 79, 80, 82, 94, 97, 133],
};

/** Set and card number, parsed like the legacy code: parseInt of the parts around the first "_". */
export function parseBlueprint(blueprintId: string): { setNo: number; cardNo: number } {
  const separator = blueprintId.indexOf('_');
  // substr(0, -1) in the legacy code yields "" (NaN) when there is no separator; keep that
  const setPart = separator >= 0 ? blueprintId.substring(0, separator) : '';
  return { setNo: parseInt(setPart, 10), cardNo: parseInt(blueprintId.substring(separator + 1), 10) };
}

/** Card.StripBlueprintId: removes the foil, alternate-image and tengwar markers. */
export function stripBlueprintId(blueprintId: string): string {
  return blueprintId
    .replace('*', '')
    .replace('^', '')
    .replace(/(_\d+)T/, '$1');
}

/** Card.GetFoil */
export function isFoil(blueprintId: string): boolean {
  return blueprintId.includes('*', 2);
}

/** Card.GetAlternateImage */
export function isAlternateImage(blueprintId: string): boolean {
  return blueprintId.includes('^', 2);
}

export function isTengwar(blueprintId: string): boolean {
  return /(_\d+)T/.test(blueprintId);
}

/** Card.getFixedImage: the first table (in legacy order) that has an image for this exact ID. */
export function getFixedImage(tables: CardImageTables, blueprintId: string): string | null {
  return (
    lookup(tables.fixedImages, blueprintId) ??
    lookup(tables.set40, blueprintId) ??
    lookup(tables.hobbit, blueprintId) ??
    lookup(tables.PCCards, blueprintId)
  );
}

function lookup(table: ImageTable, key: string): string | null {
  return Object.hasOwn(table, key) ? table[key] : null;
}

/** Card.isPack */
export function isPack(tables: CardImageTables, blueprintId: string): boolean {
  return lookup(tables.packBlueprints, blueprintId) !== null;
}

/** Card.isPlaceholder: set 404 "Future Prize" placeholders, shown in collections but never addable to a deck. */
export function isPlaceholder(blueprintId: string | null): boolean {
  if (blueprintId == null) {
    return false;
  }
  const separator = blueprintId.indexOf('_');
  return separator > 0 && parseInt(blueprintId.substring(0, separator), 10) === 404;
}

/** Card.getErrata: the remade errata image, if this card has one. */
export function getErrataImage(setNo: number, cardNo: number): string | null {
  const cards = REMADE_ERRATAS[setNo];
  if (cards?.includes(cardNo)) {
    return siteImage(`erratas/LOTR${formatCardNo(setNo, cardNo)}.jpg`);
  }
  return null;
}

/** Card.isErrata */
export function isErrata(blueprintId: string): boolean {
  const { setNo, cardNo } = parseBlueprint(blueprintId);
  return getErrataImage(setNo, cardNo) !== null;
}

/** Card.isIncomplete: sets 400-599 are in development (except 404), shown with their text instead of an image. */
export function isIncomplete(blueprintId: string): boolean {
  const { setNo } = parseBlueprint(blueprintId);
  return setNo >= 400 && setNo < 600 && setNo !== 404;
}

/** Card.formatSetNo */
export function formatSetNo(setNo: number): string {
  return setNo < 10 ? `0${setNo}` : `${setNo}`;
}

/** Card.formatCardNo */
export function formatCardNo(setNo: number, cardNo: number): string {
  const setNoStr = formatSetNo(setNo);
  if (cardNo < 10) {
    return `${setNoStr}00${cardNo}`;
  }
  if (cardNo < 100) {
    return `${setNoStr}0${cardNo}`;
  }
  return `${setNoStr}${cardNo}`;
}

/** Card.getMasterworksOffset */
function getMasterworksOffset(setNo: number): number {
  if (setNo === 17) {
    return 148;
  }
  if (setNo === 18) {
    return 140;
  }
  return 194;
}

/** Card.isMasterworks */
function isMasterworks(setNo: number, cardNo: number): boolean {
  switch (setNo) {
    case 12:
    case 13:
      return cardNo > 194;
    case 15:
      return cardNo > 194 && cardNo < 204;
    case 17:
      return cardNo > 148;
    case 18:
      return cardNo > 140;
    default:
      return false;
  }
}

/** Card.getImageUrl */
export function getImageUrl(
  tables: CardImageTables,
  blueprintId: string,
  tengwar = false,
  ignoreErrata = false,
): string {
  const fixed = getFixedImage(tables, blueprintId);
  if (fixed !== null) {
    return fixed;
  }
  const pack = lookup(tables.packBlueprints, blueprintId);
  if (pack !== null) {
    return pack;
  }

  const { setNo, cardNo } = parseBlueprint(blueprintId);
  if (setNo === 404) {
    return FUTURE_PRIZE_IMAGE;
  }

  const errata = getErrataImage(setNo, cardNo);
  if (errata !== null && !ignoreErrata) {
    return errata;
  }

  const cardStr = isMasterworks(setNo, cardNo)
    ? `${formatSetNo(setNo)}O0${cardNo - getMasterworksOffset(setNo)}`
    : formatCardNo(setNo, cardNo);
  return `${DECIPHER_IMAGES}LOTR${cardStr}${tengwar ? 'T' : ''}.jpg`;
}

/** Card.getBackSideUrl: every card currently shares the standard back. */
export function getBackSideUrl(tables: CardImageTables): string {
  return getImageUrl(tables, CARD_BACK_BLUEPRINT);
}

/** Card.isZoneNeverHorizontal */
export function isZoneNeverHorizontal(zone: string | null | undefined): boolean {
  return (
    zone === 'DECK' ||
    zone === 'TOP_OF_DISCARD' ||
    zone === 'TOP_OF_REMOVED' ||
    zone === 'STACKED' ||
    zone === 'STACKED_FACE_DOWN'
  );
}

const between = (value: number, min: number, max: number): boolean => value >= min && value <= max;

/** Card.isBlueprintHorizontal: sites and other landscape cards. */
export function isBlueprintHorizontal(blueprintId: string): boolean {
  const { setNo, cardNo } = parseBlueprint(blueprintId);
  switch (setNo) {
    case 0:
      return cardNo === 1 || cardNo === 4 || cardNo === 6 || cardNo === 8;
    case 1:
    case 51:
    case 71:
      return between(cardNo, 319, 363) || cardNo === 367;
    case 2:
    case 52:
    case 72:
    case 3:
    case 53:
    case 73:
    case 6:
    case 56:
    case 76:
      return between(cardNo, 115, 120);
    case 4:
    case 54:
    case 74:
      return between(cardNo, 323, 363);
    case 5:
    case 55:
    case 75:
      return between(cardNo, 118, 120);
    case 7:
    case 57:
    case 77:
      return between(cardNo, 329, 363);
    case 8:
    case 58:
    case 78:
    case 10:
    case 60:
    case 80:
      return between(cardNo, 117, 120);
    case 11:
    case 61:
    case 81:
      return between(cardNo, 227, 266);
    case 12:
    case 62:
    case 82:
    case 13:
    case 63:
    case 83:
      return between(cardNo, 185, 194);
    case 15:
    case 65:
    case 85:
      return between(cardNo, 187, 194) || cardNo === 210;
    case 17:
    case 67:
    case 87:
      return between(cardNo, 145, 148);
    case 18:
    case 68:
    case 88:
      return between(cardNo, 134, 140) || cardNo === 151;
    case 20:
      return between(cardNo, 416, 469);
    case 30:
      return between(cardNo, 49, 65);
    case 31:
      return between(cardNo, 44, 47);
    case 32:
      return between(cardNo, 46, 49);
    case 33:
      return between(cardNo, 55, 58);
    case 40:
      return between(cardNo, 273, 309);
    case 100:
    case 150:
      return cardNo === 1;
    case 101:
    case 151:
      return between(cardNo, 57, 64);
    case 102:
      return between(cardNo, 67, 73);
    case 103:
      return between(cardNo, 121, 130);
    default:
      return false;
  }
}

/** Card.isHorizontal */
export function isHorizontal(blueprintId: string, zone?: string | null): boolean {
  return !isZoneNeverHorizontal(zone) && isBlueprintHorizontal(blueprintId);
}
