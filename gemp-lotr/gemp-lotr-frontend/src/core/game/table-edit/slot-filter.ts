/**
 * What a puzzle slot's place holds at all, in the deck builder's filter language (SortAndFilterCards). A slot's search
 * sends the puzzle's pool and the slot's own filter to the server, which searches with each by itself and gives only
 * the cards that every one of them finds (`GET scenarios/search` with `pool=` and `bound=`), exactly as it checks a card
 * put in the slot: so the page no longer joins the two itself. What the server's bound does not say is the place's own
 * rule (SlotBounds.kindProblem: only companions and allies among the characters, no site in a hand...), and for a
 * site slot its number on the path: those are part of every search of the slot, beside what the person sets. Pure.
 */

/** The kinds of card a slot's place can hold at all (SlotBounds.kindProblem); null: any but those below. */
const ZONE_TYPES: Readonly<Record<string, readonly string[]>> = {
  characters: ['COMPANION', 'ALLY'],
  minions: ['MINION'],
  support: ['CONDITION', 'POSSESSION', 'ARTIFACT', 'ALLY', 'FOLLOWER'],
  attached: ['CONDITION', 'POSSESSION', 'ARTIFACT', 'FOLLOWER'],
  site: ['SITE'],
};
/** What no slot but a site's takes. */
const NEVER = ['THE_ONE_RING', 'SITE', 'MAP', 'METASITE', 'ADVENTURE'];

/** Whether a card of the type (in capitals) can be in a slot of the zone at all. */
export function slotTakes(zone: string, type: string): boolean {
  const upper = type.toUpperCase();
  const only = ZONE_TYPES[zone];
  if (only) return only.includes(upper);
  return !NEVER.includes(upper);
}

/**
 * What every search for a slot's card says beside the person's own filter: the kinds of card its place takes, and for
 * a site slot the number it has on the path (when known). `zone` as the scenario writes it.
 */
export function slotKinds(zone: string, siteNumber: number | null = null): string {
  const kinds = ZONE_TYPES[zone];
  const types = `cardtype:${kinds ? kinds.join(',') : NEVER.map((type) => `-${type}`).join(',')}`;
  return zone === 'site' && siteNumber !== null ? `${types} sitenumber:${siteNumber}` : types;
}
