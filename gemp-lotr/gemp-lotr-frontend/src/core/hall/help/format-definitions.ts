import {
  CANCEL_FACT,
  ERRATA_FACT,
  ERRATA_LINK,
  navMetaTitle,
  NO,
  OTHER_RULES_FACT,
  PC_CARDS_LEGAL,
  PLAYTEST_ERRATA,
  PLUS,
  RULE_DISCARD_PUBLIC,
  RULE_NO_MULLIGAN,
  RULE_NO_RULE_OF_FOUR,
  RULE_USES_MAPS,
  RULE_WIN_AT_END_OF_REGROUP,
  RULE_WIN_ON_5_SITES,
  SETS_FACT,
  SITES_FACT,
  YES,
} from './help-messages';
import {
  FORMAT_LISTS,
  FormatCard,
  FormatDefinition,
  FormatFact,
  FormatListKey,
  FormatListView,
  FormatNavMeta,
  FormatSetRow,
} from './format-definitions-models';

/** Ported from js/gemp-022/formatDefinitions.js (GempFormatDefinitions) and tables/TableFlow.js (GempFormatHelp). */

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function text(value: unknown): string | null {
  return typeof value === 'string' ? value : null;
}

function flag(value: unknown, fallback: boolean): boolean {
  return typeof value === 'boolean' ? value : fallback;
}

function count(value: unknown, fallback: number): number {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback;
}

/** The id of a format's section (FormatSummary.anchorId, GempFormatHelp.anchorId). */
export function formatAnchor(code: string): string {
  return `format-${code.replace(/[^A-Za-z0-9_-]/g, '-')}`;
}

function parseCard(value: unknown): FormatCard | null {
  const json = object(value);
  const id = text(json['id']);
  if (id === null || id === '') {
    return null;
  }
  return {
    id,
    name: text(json['name']) ?? id,
    collInfo: text(json['collInfo']),
    set: typeof json['set'] === 'number' ? json['set'] : null,
    setLabel: text(json['setLabel']),
    setName: text(json['setName']),
  };
}

function parseCards(value: unknown): FormatCard[] {
  return Array.isArray(value) ? value.map(parseCard).filter((card): card is FormatCard => card !== null) : [];
}

/** One format's entry (of /hall/formats/json, or GET /hall/format/{code}/json); null without a code. */
export function parseFormat(value: unknown): FormatDefinition | null {
  const json = object(value);
  const code = text(json['code']);
  if (code === null || code === '') {
    return null;
  }
  const errata = object(json['errata']);
  const lists = object(json['lists']);
  return {
    code,
    name: text(json['name']) ?? code,
    anchor: text(json['anchor']) ?? formatAnchor(code),
    description: text(json['description']),
    setSummary: text(json['setSummary']),
    sites: text(json['sites']),
    sitesName: text(json['sitesName']),
    cancelRingBearerSkirmish: flag(json['cancelRingBearerSkirmish'], false),
    minimumDeckSize: count(json['minimumDeckSize'], 60),
    maximumSameName: count(json['maximumSameName'], 4),
    validateShadowFPCount: flag(json['validateShadowFPCount'], true),
    winAtEndOfRegroup: flag(json['winAtEndOfRegroup'], false),
    discardPileIsPublic: flag(json['discardPileIsPublic'], false),
    winOnControlling5Sites: flag(json['winOnControlling5Sites'], false),
    ruleOfFour: flag(json['ruleOfFour'], true),
    mulliganRule: flag(json['mulliganRule'], true),
    usesMaps: flag(json['usesMaps'], false),
    playtest: flag(json['playtest'], false),
    errata: {
      pc: flag(errata['pc'], false),
      playtest: flag(errata['playtest'], false),
      pcCardsLegal: flag(errata['pcCardsLegal'], false),
    },
    lists: {
      banned: parseCards(lists['banned']),
      restricted: parseCards(lists['restricted']),
      limit2: parseCards(lists['limit2']),
      limit3: parseCards(lists['limit3']),
      valid: parseCards(lists['valid']),
    },
    restrictedNames: Array.isArray(json['restrictedNames'])
      ? json['restrictedNames'].filter((name): name is string => typeof name === 'string')
      : [],
  };
}

/** The answer of GET /hall/formats/json; anything but a list of formats reads as none (GempFormatDefinitions.render). */
export function parseFormatDefinitions(json: unknown): FormatDefinition[] {
  const formats = object(json)['formats'];
  return Array.isArray(formats)
    ? formats.map(parseFormat).filter((format): format is FormatDefinition => format !== null)
    : [];
}

/** The format a link names, by its code or by its anchor's part (`#format-<x>`, where x is the code made safe). */
export function findFormat(formats: readonly FormatDefinition[], codeOrAnchor: string): FormatDefinition | null {
  return (
    formats.find((format) => format.code === codeOrAnchor) ??
    formats.find((format) => format.anchor === `format-${codeOrAnchor}`) ??
    null
  );
}

// ---- the facts ----

/** "Cards from sets 1-10, V1-V3" reads "1-10, V1-V3" under the "Sets" label (setsText). */
export function setsText(setSummary: string): string {
  const match = /^Cards from sets? (.*)$/.exec(setSummary);
  return match?.[1] ?? setSummary;
}

/** The rules the format changes, beyond the Play popup's three (otherRules). */
export function otherRules(format: FormatDefinition): string[] {
  const rules: string[] = [];
  if (format.winAtEndOfRegroup) rules.push(RULE_WIN_AT_END_OF_REGROUP);
  if (format.discardPileIsPublic) rules.push(RULE_DISCARD_PUBLIC);
  if (format.usesMaps) rules.push(RULE_USES_MAPS);
  if (format.winOnControlling5Sites) rules.push(RULE_WIN_ON_5_SITES);
  if (!format.ruleOfFour) rules.push(RULE_NO_RULE_OF_FOUR);
  if (!format.mulliganRule) rules.push(RULE_NO_MULLIGAN);
  return rules;
}

/**
 * The facts (renderFacts): the Play popup's three (sets, sites, Ring-bearer skirmish cancel) first, then the errata
 * and whatever else the format changes. (No deck-size / copy-limit line: it read the same for every hall format.)
 */
export function formatFacts(format: FormatDefinition): FormatFact[] {
  const facts: FormatFact[] = [];
  if (format.setSummary) {
    facts.push({ kind: 'text', label: SETS_FACT, text: setsText(format.setSummary) });
  }
  if (format.sites) {
    facts.push({ kind: 'text', label: SITES_FACT, text: format.sites });
  }
  facts.push({ kind: 'text', label: CANCEL_FACT, text: format.cancelRingBearerSkirmish ? YES : NO });
  const { pc, pcCardsLegal, playtest } = format.errata;
  if (pc || pcCardsLegal || playtest) {
    let after = !pc && pcCardsLegal ? PC_CARDS_LEGAL : '';
    if (playtest) {
      after += (pc || pcCardsLegal ? PLUS : '') + PLAYTEST_ERRATA;
    }
    facts.push({
      kind: 'errata',
      label: ERRATA_FACT,
      // a format that applies PC errata links to its own filtered list; one where the PC versions are only legal
      // alongside the originals (Anything Goes) links to the whole errata list
      link: pc || pcCardsLegal ? { text: ERRATA_LINK, format: pc ? format.code : null } : null,
      text: after,
    });
  }
  const rules = otherRules(format);
  if (rules.length > 0) {
    facts.push({ kind: 'rules', label: OTHER_RULES_FACT, rules });
  }
  return facts;
}

// ---- the card lists ----

/** Names grouped by set, one row per set in the order they come (fillList). */
export function groupBySet(cards: readonly FormatCard[]): FormatSetRow[] {
  const rows: { label: string; name: string | null; cards: FormatCard[] }[] = [];
  const bySet = new Map<string, { label: string; name: string | null; cards: FormatCard[] }>();
  for (const card of cards) {
    const label = card.setLabel ?? '?';
    let row = bySet.get(label);
    if (row === undefined) {
      row = { label, name: card.setName, cards: [] };
      bySet.set(label, row);
      rows.push(row);
    }
    row.cards.push(card);
  }
  return rows;
}

/** The format's card lists that have cards, in the page's order (renderFormat). */
export function formatLists(format: FormatDefinition): FormatListView[] {
  return FORMAT_LISTS.filter((list) => format.lists[list.key].length > 0).map((list) => ({
    ...list,
    cards: format.lists[list.key],
    sets: groupBySet(format.lists[list.key]),
  }));
}

/** No card list and no titles: "every card from its sets is legal". */
export function hasNoLists(format: FormatDefinition): boolean {
  return FORMAT_LISTS.every((list) => format.lists[list.key].length === 0) && format.restrictedNames.length === 0;
}

/**
 * The collector's info as printed on the card ("1R29"); the V-sets' "V1_5" (rarity still unset) as "V1 5". A card
 * without one: its set and card number ("V1 5"). (collectorLabel; pcErrataUi.js has the same.)
 */
export function collectorLabel(card: FormatCard): string {
  const info = card.collInfo?.trim() ?? '';
  if (info) {
    return info.replace(/_+/g, ' ');
  }
  const match = /^\d+_(\d+)/.exec(card.id);
  if (match?.[1] !== undefined && card.setLabel !== null) {
    return `${card.setLabel} ${parseInt(match[1], 10)}`;
  }
  return '';
}

/** "<name> (1R29)": a card's tooltip (tooltip). */
export function cardTooltip(card: FormatCard): string {
  const info = collectorLabel(card);
  return info ? `${card.name} (${info})` : card.name;
}

/** Lower case, without accents, so "eowyn" finds "Éowyn" (fold). */
export function foldText(value: string): string {
  return value.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
}

/** "X3 R2" beside a format in the contents list, or null with neither list (renderNavItem). */
export function navMeta(format: FormatDefinition): FormatNavMeta | null {
  const banned = format.lists.banned.length;
  const restricted = format.lists.restricted.length;
  const parts = [banned ? `X${banned}` : '', restricted ? `R${restricted}` : ''].filter((part) => part);
  return parts.length === 0 ? null : { text: parts.join(' '), title: navMetaTitle(banned, restricted) };
}

/** Every card list's key, for the filter's hits. */
export const FORMAT_LIST_KEYS: readonly FormatListKey[] = FORMAT_LISTS.map((list) => list.key);
