import { HallDeckChoice, HallDeckOption } from './hall-table-models';

/** How many keys a flow's memory keeps besides the last deck overall (SelectDeck.MAX_REMEMBERED). */
export const MAX_REMEMBERED = 12;
/** The key of the last deck overall. */
const LAST = '_';

/** A flow's deck memory (cookie "<flow>-decks"): key (format, league, ...) -> [deck name, "player" | "library"]. */
export type DeckMemory = Record<string, [string, string]>;

/** The cookie holding a flow's memory ("unranked-table-decks", "bot-table-decks", "join-table-decks"). */
export const deckMemoryCookie = (flow: string) => `${flow}-decks`;

export function readDeckMemory(raw: string | null): DeckMemory {
  if (raw === null) {
    return {};
  }
  try {
    const parsed: unknown = JSON.parse(raw);
    return parsed !== null && typeof parsed === 'object' && !Array.isArray(parsed) ? (parsed as DeckMemory) : {};
  } catch {
    return {};
  }
}

/** The deck last used under `key`, else the last one overall; null when there is none. */
export function recallDeck(memory: DeckMemory, key: string | null): HallDeckChoice | null {
  const entry = key !== null && key !== '' && memory[key] ? memory[key] : memory[LAST];
  if (!Array.isArray(entry) || typeof entry[0] !== 'string') {
    return null;
  }
  return { name: entry[0], source: entry[1] === 'library' ? 'library' : 'player' };
}

/** The memory with `choice` as the latest under `key` and overall; the oldest keys go past MAX_REMEMBERED. */
export function rememberDeck(memory: DeckMemory, choice: HallDeckChoice, key: string | null): DeckMemory {
  const entry: [string, string] = [choice.name, choice.source];
  const named = key !== null && key !== '' && key !== LAST ? key : null;
  // The latest entries go last, as the legacy page's delete-then-set left them
  const kept = Object.entries(memory).filter(([name]) => name !== LAST && name !== named);
  const entries: [string, [string, string]][] = [
    ...kept,
    ...(named === null ? [] : [[named, entry] as [string, [string, string]]]),
  ];
  const overflow = Math.max(0, entries.length + 1 - MAX_REMEMBERED);
  return Object.fromEntries([...entries.slice(overflow), [LAST, entry]]);
}

/** Whether the deck is in its list. */
export function hasDeck(choice: HallDeckChoice, player: readonly HallDeckOption[], library: readonly HallDeckOption[]) {
  return (choice.source === 'library' ? library : player).some((deck) => deck.name === choice.name);
}

/** With nothing remembered: the player's first deck, or a library deck for a player with none (selectDefault). */
export function defaultDeck(
  player: readonly HallDeckOption[],
  library: readonly HallDeckOption[],
): HallDeckChoice | null {
  const own = player[0];
  if (own) {
    return { name: own.name, source: 'player' };
  }
  const sample = library[0];
  return sample ? { name: sample.name, source: 'library' } : null;
}
