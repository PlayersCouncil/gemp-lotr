import {
  CHOOSE_A_FORMAT,
  compareFormat,
  DRAFT_FORMAT_INFO,
  FORMAT_HELP_LINK,
  FORMAT_INTRO,
  formatSites,
  ringBearerCancel,
  SEALED_FORMAT_INFO,
} from './play-messages';
import { asText } from '../json-values';

/** What /deck/formats tells about a format that its (i) shows (JSONDefs.Format). */
export interface HallFormatDetails {
  readonly code: string;
  readonly name: string;
  readonly description: string | null;
  /** "Cards from sets 1-10, V1-V3". */
  readonly setSummary: string | null;
  readonly sites: string | null;
  readonly cancelRingBearerSkirmish: boolean;
}

/** The format (i) of the Casual, Bot and Tournament forms, as data (TableFlow.renderFormatInfo). */
export interface FormatInfo {
  readonly intro: string;
  /** "Choose a format to see what it allows." when no format is chosen, else the format's own description, or null. */
  readonly text: string | null;
  readonly facts: readonly string[];
  /** "Compare X with the other formats: " and its link to Help › Format Definitions at that format. */
  readonly compare: { readonly text: string; readonly label: string; readonly route: string } | null;
}

/** A format's entry on Help › Format Definitions (the deep link that replaces #format-<code>). */
export const formatHelpRoute = (code: string) => `help/formats/${encodeURIComponent(code)}`;

export function readFormatDetails(raw: Record<string, unknown>): HallFormatDetails {
  const text = (value: unknown) => (typeof value === 'string' && value !== '' ? value : null);
  return {
    code: asText(raw['code']),
    name: asText(raw['name']),
    description: text(raw['description']),
    setSummary: text(raw['setSummary']),
    sites: text(raw['sites']),
    cancelRingBearerSkirmish: raw['cancelRingBearerSkirmish'] === true,
  };
}

export function formatInfo(format: HallFormatDetails | null): FormatInfo {
  if (format === null) {
    return { intro: FORMAT_INTRO, text: CHOOSE_A_FORMAT, facts: [], compare: null };
  }
  const facts: string[] = [];
  if (format.setSummary) {
    facts.push(format.setSummary);
  }
  if (format.sites) {
    facts.push(formatSites(format.sites));
  }
  facts.push(ringBearerCancel(format.cancelRingBearerSkirmish));
  return {
    intro: FORMAT_INTRO,
    text: format.description,
    facts,
    compare: { text: compareFormat(format.name), label: FORMAT_HELP_LINK, route: formatHelpRoute(format.code) },
  };
}

/** A Sealed or Draft tournament's format (i): what the format means there (CreateTournament.formatChanged). */
export function limitedFormatInfo(sealed: boolean): FormatInfo {
  return { intro: FORMAT_INTRO, text: sealed ? SEALED_FORMAT_INFO : DRAFT_FORMAT_INFO, facts: [], compare: null };
}
