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

export const DECK_NAME = 'Game check deck';
export const FORMAT = 'fotr_block';

const copies = (count, ...ids) => ids.flatMap((id) => Array(count).fill(id));

/**
 * Fellowship Block, 30 free peoples cards (Dwarves and Elves) and 30 Moria orcs, all from set 1: every kind of card
 * and most decisions show up in the first turns (companions, possessions, events, conditions, minions, archery,
 * assignments, skirmishes). Both players play it.
 */
export const DECK = {
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

/** The deck as POST /deck takes it. */
export function deckContents(deck = DECK) {
  return [deck.ringBearer, deck.ring, deck.sites.join(','), deck.cards.join(',')].join('|');
}
