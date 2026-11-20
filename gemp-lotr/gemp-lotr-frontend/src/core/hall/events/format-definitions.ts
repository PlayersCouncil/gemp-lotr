/**
 * GET /hall/formats/json (HallRequestHandler.getFormatDefinitions, FormatDefinitions.describe), read as far as a
 * league serie's "Format description" needs: the facts the legacy dialog (GET /hall/format/{code}, appendFormat)
 * listed. Help › Format Definitions reads the same answer; once it has its own reader, this one gives way to it.
 */

/** A card of a format's lists. */
export interface FormatCard {
  readonly id: string;
  /** With the uniqueness dots and subtitle; the id when the server does not know the card. */
  readonly name: string;
}

export interface FormatDefinition {
  readonly code: string;
  readonly name: string;
  /** The legal sets in words (FormatSummary.describeSets); null when the server gave none. */
  readonly setSummary: string | null;
  /** The site block's name. */
  readonly sites: string | null;
  readonly cancelRingBearerSkirmish: boolean;
  /** The game ends after Regroup actions (winAtEndOfRegroup). */
  readonly winAtEndOfRegroup: boolean;
  readonly discardPileIsPublic: boolean;
  readonly errata: { readonly pc: boolean; readonly playtest: boolean; readonly pcCardsLegal: boolean };
  readonly banned: readonly FormatCard[];
  readonly restricted: readonly FormatCard[];
  readonly limit2: readonly FormatCard[];
  readonly limit3: readonly FormatCard[];
  readonly valid: readonly FormatCard[];
  readonly restrictedNames: readonly string[];
}

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function list(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function text(value: unknown): string | null {
  return typeof value === 'string' ? value : null;
}

function cards(value: unknown): FormatCard[] {
  return list(value).flatMap((entry) => {
    const card = object(entry);
    const id = text(card['id']);
    return id === null ? [] : [{ id, name: text(card['name']) ?? id }];
  });
}

/** {"formats": [entry, ...]}; entries without a code are left out. */
export function parseFormatDefinitions(json: unknown): FormatDefinition[] {
  return list(object(json)['formats']).flatMap((value) => {
    const entry = object(value);
    const code = text(entry['code']);
    if (code === null) {
      return [];
    }
    const lists = object(entry['lists']);
    const errata = object(entry['errata']);
    return [
      {
        code,
        name: text(entry['name']) ?? code,
        setSummary: text(entry['setSummary']),
        sites: text(entry['sites']),
        cancelRingBearerSkirmish: entry['cancelRingBearerSkirmish'] === true,
        winAtEndOfRegroup: entry['winAtEndOfRegroup'] === true,
        discardPileIsPublic: entry['discardPileIsPublic'] === true,
        errata: {
          pc: errata['pc'] === true,
          playtest: errata['playtest'] === true,
          pcCardsLegal: errata['pcCardsLegal'] === true,
        },
        banned: cards(lists['banned']),
        restricted: cards(lists['restricted']),
        limit2: cards(lists['limit2']),
        limit3: cards(lists['limit3']),
        valid: cards(lists['valid']),
        restrictedNames: list(entry['restrictedNames']).filter((name): name is string => typeof name === 'string'),
      },
    ];
  });
}
