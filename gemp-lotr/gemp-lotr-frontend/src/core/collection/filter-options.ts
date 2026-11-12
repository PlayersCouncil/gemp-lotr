/** What the collection filter's lists offer. */

export interface SelectOption {
  value: string;
  label: string;
}

/** The "Any" option the Block and Set lists start with, before a format has filled them. */
export const INITIAL_BLOCK_OPTION: SelectOption = { value: '0-33,50-200,404', label: 'Any' };
export const INITIAL_SET_OPTION: SelectOption = { value: '0-34,50-200,404', label: 'Any' };

const opts = (...pairs: [string, string][]): SelectOption[] => pairs.map(([value, label]) => ({ value, label }));

export const PRODUCT_OPTIONS = opts(
  ['', 'Any'],
  ['pack', 'Packs'],
  ['card', 'Cards'],
  ['foil', 'Foils'],
  ['nonFoil', 'Non-foils'],
  ['tengwar', 'Tengwar'],
  ['special', 'Special'],
  ['specialForDeck', 'Special in Deck'],
);

export const RARITY_OPTIONS = opts(
  ['', 'Any'],
  ['R', 'Rare'],
  ['U', 'Uncommon'],
  ['C', 'Common'],
  ['A', 'Alternate Image'],
  ['P', 'Promo'],
  ['X', 'Rare+'],
  ['S', 'Fixed'],
  ['C,U,P,S', "Poorman's"],
);

export const SIDE_OPTIONS = opts(
  ['', 'Any'],
  ['SHADOW', 'Shadow'],
  ['FREE_PEOPLES', 'Free Peoples'],
  ['NONE', 'Neither'],
);

export const SORT_OPTIONS = opts(
  ['name', 'Name'],
  ['collinfo,set', "Collector's Info"],
  ['twilight,name', 'Twilight'],
  ['siteNumber,name', 'Site Number'],
  ['strength,name', 'Strength'],
  ['vitality,name', 'Vitality'],
  ['cardType,name', 'Card Type'],
  ['culture,name', 'Culture'],
);

export const CARD_TYPE_OPTIONS = opts(
  ['', 'Any'],
  ['COMPANION,ALLY,MINION', 'Characters'],
  ['POSSESSION,ARTIFACT', 'Items'],
  ['SITE', 'Sites'],
  ['ALLY', 'Allies'],
  ['ARTIFACT', 'Artifacts'],
  ['COMPANION', 'Companions'],
  ['CONDITION', 'Conditions'],
  ['EVENT', 'Events'],
  ['FOLLOWER', 'Followers'],
  ['MINION', 'Minions'],
  ['POSSESSION', 'Possessions'],
);

export const KEYWORD_OPTIONS = opts(
  ['', 'Any'],
  ['ARCHER', 'Archer'],
  ['BATTLEGROUND', 'Battleground'],
  ['BESIEGER', 'Besieger'],
  ['CORSAIR', 'Corsair'],
  ['DWELLING', 'Dwelling'],
  ['EASTERLING', 'Easterling'],
  ['ENDURING', 'Enduring'],
  ['ENGINE', 'Engine'],
  ['FIERCE', 'Fierce'],
  ['FOREST', 'Forest'],
  ['FORTIFICATION', 'Fortification'],
  ['HUNTER', 'Hunter'],
  ['KNIGHT', 'Knight'],
  ['LURKER', 'Lurker'],
  ['MACHINE', 'Machine'],
  ['MARSH', 'Marsh'],
  ['MOUNTAIN', 'Mountain'],
  ['MUSTER', 'Muster'],
  ['PIPEWEED', 'Pipeweed'],
  ['PLAINS', 'Plains'],
  ['RANGER', 'Ranger'],
  ['RING_BOUND', 'Ring-bound'],
  ['RIVER', 'River'],
  ['SEARCH', 'Search'],
  ['SOUTHRON', 'Southron'],
  ['SPELL', 'Spell'],
  ['STEALTH', 'Stealth'],
  ['TALE', 'Tale'],
  ['TENTACLE', 'Tentacle'],
  ['TRACKER', 'Tracker'],
  ['TWILIGHT', 'Twilight'],
  ['UNDERGROUND', 'Underground'],
  ['UNHASTY', 'Unhasty'],
  ['VALIANT', 'Valiant'],
  ['VILLAGER', 'Villager'],
  ['WARG_RIDER', 'Warg-rider'],
  ['WEATHER', 'Weather'],
  ['WISE', 'Wise'],
);

export const RACE_OPTIONS = opts(
  ['', 'Any'],
  ['BALROG', 'Balrog'],
  ['CREATURE', 'Creature'],
  ['CROW', 'Crow'],
  ['DRAGON', 'Dragon'],
  ['DWARF', 'Dwarf'],
  ['EAGLE', 'Eagle'],
  ['ELF', 'Elf'],
  ['ENT', 'Ent'],
  ['GIANT', 'Giant'],
  ['HALF_TROLL', 'Half-troll'],
  ['HOBBIT', 'Hobbit'],
  ['MAIA', 'Maia'],
  ['MAN', 'Man'],
  ['NAZGUL', 'Nazgul'],
  ['ORC', 'Orc'],
  ['SPIDER', 'Spider'],
  ['TREE', 'Tree'],
  ['TROLL', 'Troll'],
  ['URUK_HAI', 'Uruk-hai'],
  ['WARG', 'Warg'],
  ['WIZARD', 'Wizard'],
  ['WRAITH', 'Wraith'],
);

export const ITEM_CLASS_OPTIONS = opts(
  ['', 'Any'],
  ['CLASSLESS', 'Classless'],
  ['SUPPORT_AREA', 'Support Area'],
  ['ARMOR', 'Armor'],
  ['BOX', 'Box'],
  ['BRACERS', 'Bracers'],
  ['BROOCH', 'Brooch'],
  ['CLOAK', 'Cloak'],
  ['GAUNTLETS', 'Gauntlets'],
  ['HAND_WEAPON', 'Hand Weapon'],
  ['HELM', 'Helm'],
  ['MOUNT', 'Mount'],
  ['PALANTIR', 'Palantir'],
  ['PHIAL', 'Phial'],
  ['PIPE', 'Pipe'],
  ['PONY', 'Pony'],
  ['RANGED_WEAPON', 'Ranged Weapon'],
  ['RING', 'Ring'],
  ['SHIELD', 'Shield'],
  ['STAFF', 'Staff'],
);

export const PHASE_OPTIONS = opts(
  ['', 'Any'],
  ['FELLOWSHIP', 'Fellowship'],
  ['SHADOW', 'Shadow'],
  ['MANEUVER', 'Maneuver'],
  ['ARCHERY', 'Archery'],
  ['ASSIGNMENT', 'Assignment'],
  ['SKIRMISH', 'Skirmish'],
  ['REGROUP', 'Regroup'],
  ['RESPONSE', 'Response'],
);

export const SIGNET_OPTIONS = opts(
  ['', 'Any'],
  ['ARAGORN', 'Aragorn'],
  ['FRODO', 'Frodo'],
  ['GANDALF', 'Gandalf'],
  ['THEODEN', 'Theoden'],
);

export const COMPARE_OPTIONS = opts(
  ['', 'Any'],
  ['EQUALS', '='],
  ['GREATER_THAN', '>'],
  ['GREATER_THAN_OR_EQUAL_TO', '>='],
  ['LESS_THAN', '<'],
  ['LESS_THAN_OR_EQUAL_TO', '<='],
);
