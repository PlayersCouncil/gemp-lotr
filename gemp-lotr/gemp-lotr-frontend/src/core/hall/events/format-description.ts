import { FormatCard, FormatDefinition } from '../help/format-definitions-models';

/**
 * A league serie's "Format description": the facts the legacy dialog (GET /hall/format/{code}, appendFormat) listed,
 * from a format's definition (GET /hall/format/{code}/json, read by Help's parseFormat: one reader of a format entry
 * for both tabs).
 */

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
  list("X-listed (can't be played): ", format.lists.banned);
  list('R-listed (can play just one copy): ', format.lists.restricted);
  list('Limited to 2 in deck: ', format.lists.limit2);
  list('Limited to 3 in deck: ', format.lists.limit3);
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
  list('Additional valid: ', format.lists.valid);
  return lines;
}
