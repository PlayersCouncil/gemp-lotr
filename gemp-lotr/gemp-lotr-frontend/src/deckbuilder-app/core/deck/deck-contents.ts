/** The deck parts the server needs to save, validate or convert a deck. */
export interface DeckContents {
  ringBearer: string | null;
  ring: string | null;
  sites: readonly string[];
  /** One entry per copy. */
  drawDeck: readonly string[];
  map: string | null;
}

/**
 * Serializes a deck into the "deckContents" string the server parses (DeckSerialization.buildDeckFromContents):
 * ringBearer|ring|site,site,...|card,card,...[|map]
 * The map part is only sent for formats that use maps, as the legacy deck builder does.
 */
export function serializeDeckContents(deck: DeckContents, includeMap: boolean): string {
  const parts = [deck.ringBearer ?? '', deck.ring ?? '', deck.sites.join(','), deck.drawDeck.join(',')];
  if (includeMap) {
    parts.push(deck.map ?? '');
  }
  return parts.join('|');
}

/** Format codes whose decks include a Map (legacy deckBuildingUi.js checks exactly these two). */
const MAP_FORMATS: readonly string[] = ['pc_movie', 'test_pc_movie'];

export function formatUsesMap(formatCode: string | null | undefined): boolean {
  return formatCode != null && MAP_FORMATS.includes(formatCode);
}
