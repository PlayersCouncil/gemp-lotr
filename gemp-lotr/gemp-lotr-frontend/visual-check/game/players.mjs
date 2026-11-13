// The players and the deck of the game check (see README.md). Each worker has its own pair, and a scenario plays its
// game on both pages with the same pair, so the names on the two screens are the same.

/** At most this many scenarios run at once: one pair of players each. */
export const MAX_WORKERS = 4;

/**
 * The players of worker `index`: the player in the browser and the opponent the check plays over HTTP, and a third
 * player who watches (spectator scenarios, where the check plays both sides over HTTP).
 */
export function pairOf(index) {
  return { player: `GamePlayer${index}`, foe: `GameFoe${index}`, watcher: `GameWatcher${index}` };
}

/** Every player of the game check, for the database's first start. */
export const GAME_PLAYERS = Array.from({ length: MAX_WORKERS }, (_, index) => {
  const { player, foe, watcher } = pairOf(index);
  return [
    { name: player, type: 'aut' },
    { name: foe, type: 'aut' },
    // Watches the games of the spectator scenarios
    { name: watcher, type: 'aut' },
  ];
}).flat();

const copies = (count, ...ids) => ids.flatMap((id) => Array(count).fill(id));

/**
 * Fellowship Block, 30 free peoples cards (Dwarves and Elves) and 30 Moria orcs, all from set 1: every kind of card
 * and most decisions show up in the first turns (companions, possessions, events, conditions, minions, archery,
 * assignments, skirmishes). Both players play it.
 */
const MAIN = {
  name: 'Game check deck',
  format: 'fotr_block',
  ringBearer: '1_290',
  ring: '1_2',
  sites: ['1_319', '1_327', '1_337', '1_343', '1_349', '1_350', '1_353', '1_356', '1_360'],
  cards: [
    ...copies(2, '1_12', '1_50'),
    ...copies(4, '1_7', '1_53', '1_9', '1_3', '1_299'),
    ...copies(2, '1_41', '1_42', '1_37'),
    ...copies(4, '1_178', '1_181', '1_185', '1_191', '1_184', '1_176', '1_180'),
    ...copies(2, '1_183'),
  ],
};

const FELLOWSHIP_SITES = ['1_319', '1_327', '1_337', '1_343', '1_349', '1_350', '1_353', '1_356', '1_360'];

/**
 * The Debug decks (a format only the seeded test server plays, HallServer) are small: once the starting fellowship
 * is out, what is left fits in the opening hand of 8, so a scenario knows every card it can play. Each has as many
 * Free Peoples cards as Shadow cards, as the format requires.
 */
const debugDeck = (name, cards) => ({
  name,
  format: 'debug',
  ringBearer: '1_290',
  ring: '1_2',
  sites: FELLOWSHIP_SITES,
  cards,
});

/**
 * Gimli and Sam start. Gimli with the Dwarven Axe and Let Them Come! beats an Orc: two required responses at once and
 * the Orc discarded from its skirmish; For Frodo exerts Sam (removed from the game); Goblin Swarms stacks an Orc that
 * wins; A Shadow of the Past is hindered.
 */
const SKIRMISH = debugDeck('Game check skirmish deck', [
  ...['1_12', '1_311', '1_9', '1_20', '103_107'],
  ...['1_183', '1_178', '1_178', '1_185', '101_49'],
]);

/**
 * Gimli and a Dwarf Guard start; the Dwarven Axe is played on one and transferred to the other. The Uruk Soldier
 * (damage +1) beats the Dwarf Guard (vitality 2), which dies.
 */
const TRANSFER = debugDeck('Game check transfer deck', [...['1_12', '1_7', '1_9'], ...['1_154', '1_185', '1_191']]);

/**
 * The Shards of Narsil and two [gondor] cards: one stacked on it, then both its Fellowship actions are offered at
 * once, so its card opens a menu of them.
 */
const SHARDS = debugDeck('Game check shards deck', [...['3_44', '1_116', '1_116'], ...['1_178', '1_185', '1_191']]);

/**
 * Gimli and Merry start; a [sauron] Orc skirmishes Gimli and Merry, not assigned, is discarded to remove it from the
 * skirmish (the server's only way to tell a page a card left a skirmish, besides minions replaced or split).
 */
const RESCUE = debugDeck('Game check rescue deck', [...['1_12', '4_311'], ...['1_261', '1_271']]);

/** The test decks by the names scenarios give (`deck`), each saved for every player. */
export const DECKS = {
  main: MAIN,
  skirmish: SKIRMISH,
  transfer: TRANSFER,
  shards: SHARDS,
  rescue: RESCUE,
};

/** The deck as POST /deck takes it. */
export function deckContents(deck = MAIN) {
  const parts = [deck.ringBearer, deck.ring, deck.sites.join(','), deck.cards.join(',')];
  // The fifth part is the Map (DeckSerialization.buildDeckFromContents)
  return (deck.map ? [...parts, deck.map] : parts).join('|');
}
