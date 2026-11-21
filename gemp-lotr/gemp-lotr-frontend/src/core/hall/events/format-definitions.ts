/**
 * GET /hall/format/{code}/json (HallRequestHandler, FormatDefinitions.describe: one entry of /hall/formats/json, for
 * any format the library knows), read as far as a league serie's "Format description" needs: the facts the legacy
 * dialog (GET /hall/format/{code}, appendFormat) listed. Help › Format Definitions reads the same entries
 * (core/hall/help); once its reader exports one entry's parser and reads sitesName, this one gives way to it.
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
  /** The site block's code ("FELLOWSHIP"). */
  readonly sites: string | null;
  /** The site block's readable name, as the legacy HTML showed it; null when the server has none. */
  readonly sitesName: string | null;
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

/** One format's entry (GET /hall/format/{code}/json); null without a code. */
export function parseFormatDefinition(value: unknown): FormatDefinition | null {
  const entry = object(value);
  const code = text(entry['code']);
  if (code === null) {
    return null;
  }
  const lists = object(entry['lists']);
  const errata = object(entry['errata']);
  return {
    code,
    name: text(entry['name']) ?? code,
    setSummary: text(entry['setSummary']),
    sites: text(entry['sites']),
    sitesName: text(entry['sitesName']),
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
  };
}

/** A line of the format description: its text, then the cards it lists (each shown on a click), if any. */
export interface FormatDescriptionLine {
  readonly text: string;
  readonly cards: readonly FormatCard[];
}

/**
 * The format description's lines, as GET /hall/format/{code} wrote them (HallRequestHandler.appendFormat): the legal
 * sets, the sites, the Ring-bearer skirmish rule, the end of the game and the discard piles where they differ, the
 * card lists, the errata, the additional valid cards.
 */
export function formatDescriptionLines(format: FormatDefinition): FormatDescriptionLine[] {
  const lines: FormatDescriptionLine[] = [];
  const line = (text: string, cards: readonly FormatCard[] = []) => lines.push({ text, cards });
  if (format.setSummary !== null) {
    line(format.setSummary);
  }
  line(`Sites: ${format.sitesName ?? format.sites ?? ''}`);
  line(`Ring-bearer skirmish cancel: ${format.cancelRingBearerSkirmish ? 'Yes' : 'No'}`);
  if (format.winAtEndOfRegroup) {
    line('The game ends after Regroup actions are made (instead of at the start of Regroup)');
  }
  if (format.discardPileIsPublic) {
    line('Discard piles are public information for both sides');
  }
  const list = (text: string, cards: readonly FormatCard[]) => {
    if (cards.length > 0) {
      line(text, cards);
    }
  };
  list("X-listed (can't be played): ", format.banned);
  list('R-listed (can play just one copy): ', format.restricted);
  list('Limited to 2 in deck: ', format.limit2);
  list('Limited to 3 in deck: ', format.limit3);
  if (format.restrictedNames.length > 0) {
    line(`Restricted by card name: ${format.restrictedNames.join(', ')}`);
  }
  if (format.errata.pc) {
    line('Errata: PC Errata (see Help › PC Errata)');
  } else if (format.errata.pcCardsLegal) {
    line('Errata: PC Errata versions are legal alongside the originals (see Help › PC Errata)');
  }
  if (format.errata.playtest) {
    line('Errata: playtest errata');
  }
  list('Additional valid: ', format.valid);
  return lines;
}
