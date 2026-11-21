/** The wording of Help (includes/help.html, includes/help/formatRules.html, formatDefinitions.js), as the legacy hall has it. */

// ---- the sub-tabs (help.html #help-tabs) ----
export const HOW_TO_PLAY_TAB = 'How Do I Play?';
export const CONDUCT_TAB = 'Code of Conduct';
export const FORMATS_TAB = 'Format Definitions';
export const LEAGUES_TAB = 'What Are Leagues?';
export const ERRATA_TAB = 'PC Errata';

// ---- Format Definitions ----
/** What the "could not load" sentences name (neutralErrorMap). */
export const FORMAT_DEFINITIONS_SUBJECT = 'the format definitions';
export const FORMATS_LOADING = 'Loading the format definitions…';
export const NO_FORMATS = 'No formats are available right now.';
export const FILTER_PLACEHOLDER = 'Find a format or card';
export const FILTER_LABEL = 'Find a format, or the formats that list a card';
export const IMAGES_LABEL = 'Card images';
export const NO_LISTS = 'No X-, R- or limited cards: every card from its sets is legal.';

export const PC_ERRATA_BADGE = 'PC errata';
export const PC_ERRATA_BADGE_TITLE = "Uses the Player's Council errata";
export const PLAYTEST_BADGE = 'Playtest';
export const PERMALINK_TEXT = '#';
export const PERMALINK_TITLE = 'Link to this format';
export const SHARE_LABEL = 'Share';
export const shareAriaLabel = (formatName: string) => `Copy a share link to ${formatName}`;

export const SETS_FACT = 'Sets';
export const SITES_FACT = 'Sites';
export const CANCEL_FACT = 'Ring-bearer skirmish cancel';
export const YES = 'Yes';
export const NO = 'No';
export const ERRATA_FACT = 'Errata';
export const OTHER_RULES_FACT = 'Other rules';
export const ERRATA_LINK = 'PC Errata';
export const PC_CARDS_LEGAL = ' versions are legal alongside the originals';
export const PLAYTEST_ERRATA = 'playtest errata';
export const PLUS = ', plus ';

/** formatDefinitions.js otherRules. */
export const RULE_WIN_AT_END_OF_REGROUP =
  'The game ends after Regroup actions are made (instead of at the start of Regroup)';
export const RULE_DISCARD_PUBLIC = 'Discard piles are public information for both sides';
export const RULE_USES_MAPS = 'Each deck includes a Map';
export const RULE_WIN_ON_5_SITES = 'A player who controls 5 sites wins';
export const RULE_NO_RULE_OF_FOUR = 'No rule of 4';
export const RULE_NO_MULLIGAN = 'No mulligan';

export const RESTRICTED_NAMES_TITLE = 'R-list by title';
export const RESTRICTED_NAMES_NOTE = 'one copy of any card with the title';

/** "3 of 12 formats (2 by a listed card)" (applyFilter). */
export function filterStatus(shown: number, total: number, byCard: number): string {
  return `${shown} of ${total} formats${byCard > 0 ? ` (${byCard} by a listed card)` : ''}`;
}

/** The contents list's "X3 R2" and its tooltip "3 X-listed, 2 R-listed" (renderNavItem). */
export function navMetaTitle(banned: number, restricted: number): string {
  return [banned ? `${banned} X-listed` : '', restricted ? `${restricted} R-listed` : '']
    .filter((part) => part)
    .join(', ');
}
