import {
  CardGroup,
  CollectionItem,
  CollectionPage,
  CollectionTypeInfo,
  Deck,
  DeckSummary,
  League,
  MetaSite,
  PackDelivery,
} from './models';
import { attr, boolAttr, childElements, firstChild, intAttr, requiredAttr, rootElement } from './xml';

/**
 * Turns the Gemp server's XML responses into typed models. The XML is produced by DeckRequestHandler,
 * CollectionRequestHandler, LeagueRequestHandler and DeliveryRequestHandler in gemp-lotr-async.
 */

const CARD_GROUPS: readonly string[] = ['ring', 'site', 'map', 'ringBearer', 'fp', 'shadow'];

/** <decks><deck targetFormat="...">name</deck>...</decks> */
export function parseDeckList(doc: Document): DeckSummary[] {
  const root = rootElement(doc, 'decks');
  if (!root) {
    return [];
  }
  return childElements(root, 'deck').map((deck) => ({
    name: deck.textContent ?? '',
    formatName: attr(deck, 'targetFormat') ?? '',
  }));
}

/**
 * <deck><targetFormat .../><notes/><ringBearer blueprintId/><ring/><map/><site/>*<card side blueprintId/>*</deck>.
 * The server answers an empty <deck/> when the deck does not exist; that becomes null.
 */
export function parseDeck(doc: Document): Deck | null {
  const root = rootElement(doc, 'deck');
  if (!root || root.children.length === 0) {
    return null;
  }
  const targetFormat = firstChild(root, 'targetFormat');
  const blueprintOf = (tag: string): string | null => {
    const element = firstChild(root, tag);
    return element ? requiredAttr(element, 'blueprintId') : null;
  };
  return {
    targetFormat: targetFormat
      ? {
          formatName: attr(targetFormat, 'formatName') ?? '',
          formatCode: attr(targetFormat, 'formatCode') ?? '',
          leagueCode: attr(targetFormat, 'leagueCode'),
        }
      : null,
    notes: firstChild(root, 'notes')?.textContent ?? '',
    ringBearer: blueprintOf('ringBearer'),
    ring: blueprintOf('ring'),
    map: blueprintOf('map'),
    sites: childElements(root, 'site').map((site) => requiredAttr(site, 'blueprintId')),
    drawDeck: childElements(root, 'card').map((card) => ({
      blueprintId: requiredAttr(card, 'blueprintId'),
      side: attr(card, 'side') ?? 'FREE_PEOPLE',
    })),
  };
}

function parseCollectionItems(root: Element): CollectionItem[] {
  const items: CollectionItem[] = [];
  for (const element of Array.from(root.children)) {
    if (element.tagName === 'card') {
      const group = attr(element, 'group');
      items.push({
        kind: 'card',
        blueprintId: requiredAttr(element, 'blueprintId'),
        count: intAttr(element, 'count', 0),
        side: attr(element, 'side'),
        group: group !== null && CARD_GROUPS.includes(group) ? (group as CardGroup) : null,
      });
    } else if (element.tagName === 'pack') {
      const contents = attr(element, 'contents');
      items.push({
        kind: 'pack',
        blueprintId: requiredAttr(element, 'blueprintId'),
        count: intAttr(element, 'count', 0),
        selection: contents ? contents.split('|') : null,
      });
    }
  }
  return items;
}

/** <collection count="total"><card .../>|<pack .../>...</collection>, in server (filter/sort) order. */
export function parseCollectionPage(doc: Document): CollectionPage {
  const root = rootElement(doc, 'collection');
  if (!root) {
    return { total: 0, items: [] };
  }
  return { total: intAttr(root, 'count', 0), items: parseCollectionItems(root) };
}

/** GET /collection/import/ answers a <collection> of the recognised decklist cards. */
export function parseImportedCards(doc: Document): CollectionItem[] {
  return parseCollectionPage(doc).items;
}

/** <collections><collection type name format/>...</collections> */
export function parseCollectionTypes(doc: Document): CollectionTypeInfo[] {
  const root = rootElement(doc, 'collections');
  if (!root) {
    return [];
  }
  return childElements(root, 'collection').map((collection) => ({
    type: requiredAttr(collection, 'type'),
    name: attr(collection, 'name') ?? '',
    format: attr(collection, 'format') ?? '',
  }));
}

function parseMetaSite(site: Element): MetaSite {
  return {
    position: intAttr(site, 'position', 0),
    blueprintId: requiredAttr(site, 'blueprintId'),
    visualBlueprintId: attr(site, 'visualBlueprintId'),
    name: attr(site, 'name'),
    siteOverride: boolAttr(site, 'siteOverride'),
  };
}

/** <leagues><league code name type member formatCode [pathLength playerPosition] ><metaSite/>*</league>...</leagues> */
export function parseLeagues(doc: Document): League[] {
  const root = rootElement(doc, 'leagues');
  if (!root) {
    return [];
  }
  return childElements(root, 'league').map((league) => {
    const isRtmd = attr(league, 'type') === 'RTMD';
    return {
      code: requiredAttr(league, 'code'),
      name: attr(league, 'name') ?? '',
      type: attr(league, 'type') ?? '',
      member: boolAttr(league, 'member'),
      formatCode: attr(league, 'formatCode'),
      // Same defaults as the legacy deck builder: position 1 of a 0-length path when not given
      playerPosition: isRtmd ? intAttr(league, 'playerPosition', 1) : null,
      pathLength: isRtmd ? intAttr(league, 'pathLength', 0) : null,
      metaSites: childElements(league, 'metaSite').map(parseMetaSite),
    };
  });
}

/** <delivery><collectionType name><pack count blueprintId/>*<card count blueprintId/>*</collectionType>*</delivery> */
export function parsePackDelivery(doc: Document): PackDelivery[] {
  const root = rootElement(doc, 'delivery');
  if (!root) {
    return [];
  }
  const entries = (parent: Element, tag: string) =>
    childElements(parent, tag).map((element) => ({
      blueprintId: requiredAttr(element, 'blueprintId'),
      count: intAttr(element, 'count', 0),
    }));
  return childElements(root, 'collectionType').map((collection) => ({
    collectionName: attr(collection, 'name') ?? '',
    packs: entries(collection, 'pack'),
    cards: entries(collection, 'card'),
  }));
}
