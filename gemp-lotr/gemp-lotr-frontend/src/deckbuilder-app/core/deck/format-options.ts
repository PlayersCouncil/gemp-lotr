import { CollectionTypeInfo, GempFormat, League } from '../api/models';

/** One entry of the deck builder's format list (#formatSelect). */
export interface FormatOption {
  /** Format code; for a Race to Mount Doom league, its league code (which is also what a saved deck stores). */
  value: string;
  label: string;
  /** Collection the deck is checked against ("default" for the normal formats); none for RTMD leagues. */
  collectionType: string | null;
  /** Separator line. */
  disabled: boolean;
  /** A Race to Mount Doom league the player is in. */
  rtmd: boolean;
}

/** What the list shows before the formats have loaded (deckBuild.html). */
export const INITIAL_FORMAT_OPTIONS: readonly FormatOption[] = [
  { value: 'rev_tow_sta', label: 'Anything Goes', collectionType: null, disabled: false, rtmd: false },
];

/**
 * The formats shown in the game hall, in their display order (updateFormatOptions). As in the legacy code, two
 * formats with the same order keep only the later one.
 */
export function hallFormatOptions(formats: Record<string, GempFormat>): FormatOption[] {
  const byOrder = new Map<number, FormatOption>();
  for (const [code, format] of Object.entries(formats)) {
    if (format.hall) {
      byOrder.set(format.order, {
        value: code,
        label: format.name,
        collectionType: 'default',
        disabled: false,
        rtmd: false,
      });
    }
  }
  return [...byOrder.entries()]
    .filter(([order]) => order >= -1)
    .sort(([a], [b]) => a - b)
    .map(([, option]) => option);
}

const separator = (label: string): FormatOption => ({
  value: label,
  label,
  collectionType: null,
  disabled: true,
  rtmd: false,
});

/** The player's limited collections, each listed under its format (getCollectionTypes); the header is always added. */
export function limitedFormatOptions(collections: readonly CollectionTypeInfo[]): FormatOption[] {
  return [
    separator('---Limited---'),
    ...collections.map((c) => ({
      value: c.format,
      label: c.name,
      collectionType: c.type,
      disabled: false,
      rtmd: false,
    })),
  ];
}

/** The Race to Mount Doom leagues the player is in, with their position on the path (getRtmdLeagues). */
export function rtmdFormatOptions(leagues: readonly League[]): FormatOption[] {
  if (leagues.length === 0) {
    return [];
  }
  return [
    separator('---Race to Mount Doom---'),
    ...leagues.map((league) => ({
      value: league.code,
      label: `${league.name} (pos ${league.playerPosition}/${league.pathLength})`,
      collectionType: null,
      disabled: false,
      rtmd: true,
    })),
  ];
}

/**
 * The option a value selects, like jQuery's .val() on the list: the first one with that value, or the first option
 * when none has it.
 */
export function optionIndexOf(options: readonly FormatOption[], value: string): number {
  const index = options.findIndex((option) => option.value === value && !option.disabled);
  return index >= 0 ? index : 0;
}
