import { asText, orIfEmpty } from '../json-values';

/** Play ▸ Create Tournament's form as a pure state machine (CreateTournament), for the view to draw and change. */

/** The Tournament Type dropdown's values (live draft is "table_draft"); "" = none chosen yet. */
export type TournamentType = 'constructed' | 'sealed' | 'table_draft' | 'solodraft';

export const TOURNAMENT_TYPES: readonly { readonly value: TournamentType; readonly label: string }[] = [
  { value: 'constructed', label: 'Constructed' },
  { value: 'sealed', label: 'Sealed' },
  { value: 'table_draft', label: 'Live Draft' },
  { value: 'solodraft', label: 'Solo Draft' },
];

export type PairingType = 'SWISS' | 'SINGLE_ELIMINATION';
export const PAIRING_TYPES: readonly { readonly value: PairingType; readonly label: string }[] = [
  { value: 'SWISS', label: 'Swiss (everyone plays all rounds)' },
  { value: 'SINGLE_ELIMINATION', label: 'Single-elimination (losers play no more rounds)' },
];

/** Seconds to confirm; -1 = no ready check. */
export const READY_CHECKS: readonly { readonly value: number; readonly label: string }[] = [
  { value: -1, label: 'Start with no ready check' },
  { value: 60, label: '1 minute to confirm' },
  { value: 120, label: '2 minutes to confirm' },
  { value: 180, label: '3 minutes to confirm' },
];

export const DEFAULT_PLAYERS = '4';
export const DEFAULT_READY_CHECK = 60;
export const MIN_DECKBUILDING_MINUTES = 5;
/** The deck-building minutes each type starts with (typeChanged). */
export const DECKBUILDING_DEFAULTS: Readonly<Partial<Record<TournamentType, string>>> = {
  sealed: '30',
  table_draft: '15',
  solodraft: '30',
};

/** A format a tournament type offers (GET /tournament/tournamentFormats). */
export interface TournamentFormatOption {
  readonly code: string;
  readonly name: string;
  /** Live drafts: the most players one table seats, and the draft timer it suggests. */
  readonly maxPlayers: number | null;
  readonly recommendedTimer: string | null;
}

/** GET /tournament/tournamentFormats (JSONDefs.PlayerMadeTournamentAvailableFormats). */
export interface TournamentFormats {
  readonly constructed: readonly TournamentFormatOption[];
  readonly sealed: readonly TournamentFormatOption[];
  readonly soloDrafts: readonly TournamentFormatOption[];
  readonly tableDrafts: readonly TournamentFormatOption[];
  /** "CLASSIC", "SLOW", "NO_TIMER". */
  readonly draftTimerTypes: readonly string[];
}

export function parseTournamentFormats(json: unknown): TournamentFormats {
  const data = (json ?? {}) as Record<string, unknown>;
  const list = (value: unknown): TournamentFormatOption[] =>
    Array.isArray(value)
      ? value
          .filter((entry): entry is Record<string, unknown> => entry !== null && typeof entry === 'object')
          .map((entry) => ({
            code: asText(entry['code']),
            name: asText(entry['name']),
            maxPlayers: typeof entry['maxPlayers'] === 'number' ? entry['maxPlayers'] : null,
            recommendedTimer: typeof entry['recommendedTimer'] === 'string' ? entry['recommendedTimer'] : null,
          }))
      : [];
  return {
    constructed: list(data['constructed']),
    sealed: list(data['sealed']),
    soloDrafts: list(data['soloDrafts']),
    tableDrafts: list(data['tableDrafts']),
    draftTimerTypes: Array.isArray(data['draftTimerTypes'])
      ? data['draftTimerTypes'].filter((t): t is string => typeof t === 'string')
      : [],
  };
}

/** "NO_TIMER" -> "No Timer" (setupTournamentSpawner: the first "_" a space, then title case). */
export function draftTimerLabel(type: string): string {
  return type
    .replace('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

export interface TournamentFormState {
  readonly type: TournamentType | '';
  readonly format: string;
  /** As typed (a number input). */
  readonly players: string;
  readonly pairing: PairingType;
  readonly deckbuilding: string;
  readonly competitive: boolean;
  readonly earlyStart: boolean;
  readonly readyCheck: number;
  readonly draftTimer: string;
  readonly advancedShown: boolean;
  /** The player count is 2 or fewer: no ready check, no Early Start, no pairing choice. */
  readonly small: boolean;
  /** The ready check and Early Start chosen while above 2 players, given back when the count goes above again. */
  readonly savedLarge: { readonly readyCheck: number; readonly earlyStart: boolean } | null;
}

/** The page's defaults (resetForm, each Play open). */
export function initialTournamentForm(): TournamentFormState {
  return {
    type: '',
    format: '',
    players: DEFAULT_PLAYERS,
    pairing: 'SWISS',
    deckbuilding: '15',
    competitive: false,
    earlyStart: true,
    readyCheck: DEFAULT_READY_CHECK,
    draftTimer: '',
    advancedShown: false,
    small: false,
    savedLarge: null,
  };
}

/** The formats a type offers. */
export function formatsFor(
  formats: TournamentFormats | null,
  type: TournamentType | '',
): readonly TournamentFormatOption[] {
  if (formats === null || type === '') {
    return [];
  }
  switch (type) {
    case 'constructed':
      return formats.constructed;
    case 'sealed':
      return formats.sealed;
    case 'solodraft':
      return formats.soloDrafts;
    case 'table_draft':
      return formats.tableDrafts;
  }
}

/** Which of the conditional fields show (typeChanged's .conditional-display); nothing below the type until one is chosen. */
export interface TournamentFields {
  /** Format, Number of Players, the advanced settings and the button. */
  readonly startup: boolean;
  readonly deck: boolean;
  readonly deckbuilding: boolean;
  readonly draftInfo: boolean;
  readonly draftTimer: boolean;
  /** The pairing, ready check and Early Start can be changed (above 2 players). */
  readonly largeSettingsEnabled: boolean;
  /** The Number of Players' max (a live draft's table size), or null. */
  readonly maxPlayers: number | null;
}

export function tournamentFields(form: TournamentFormState, formats: TournamentFormats | null): TournamentFields {
  return {
    startup: form.type !== '',
    deck: form.type === 'constructed',
    deckbuilding: form.type === 'sealed' || form.type === 'table_draft' || form.type === 'solodraft',
    draftInfo: form.type === 'table_draft',
    draftTimer: form.type === 'table_draft',
    largeSettingsEnabled: !form.small,
    maxPlayers: selectedFormat(form, formats)?.maxPlayers ?? null,
  };
}

export function selectedFormat(
  form: TournamentFormState,
  formats: TournamentFormats | null,
): TournamentFormatOption | null {
  return formatsFor(formats, form.type).find((f) => f.code === form.format) ?? null;
}

/** A type is chosen (typeChanged): its deck-building default, its formats with the first selected (formatChanged). */
export function withType(
  form: TournamentFormState,
  type: TournamentType,
  formats: TournamentFormats | null,
): TournamentFormState {
  const next: TournamentFormState = {
    ...form,
    type,
    deckbuilding: DECKBUILDING_DEFAULTS[type] ?? form.deckbuilding,
    format: formatsFor(formats, type)[0]?.code ?? '',
  };
  return withFormat(next, next.format, formats);
}

/** A format is chosen (formatChanged): a live draft caps the players at its table and suggests its timer. */
export function withFormat(
  form: TournamentFormState,
  code: string,
  formats: TournamentFormats | null,
): TournamentFormState {
  let next: TournamentFormState = { ...form, format: code };
  const format = selectedFormat(next, formats);
  if (format?.maxPlayers) {
    const current = parseInt(next.players, 10);
    if (current > format.maxPlayers) {
      next = withPlayers(next, String(format.maxPlayers));
    }
  }
  if (format?.recommendedTimer) {
    next = { ...next, draftTimer: format.recommendedTimer };
  }
  return next;
}

/** The player count changes (playerCountChanged): only crossing the 2-player line changes the other settings. */
export function withPlayers(form: TournamentFormState, players: string): TournamentFormState {
  const count = parseInt(players, 10);
  const small = !Number.isNaN(count) && count <= 2;
  if (small && !form.small) {
    return {
      ...form,
      players,
      small,
      savedLarge: { readyCheck: form.readyCheck, earlyStart: form.earlyStart },
      readyCheck: -1,
      earlyStart: false,
    };
  }
  if (!small && form.small) {
    const saved = form.savedLarge ?? { readyCheck: DEFAULT_READY_CHECK, earlyStart: true };
    return { ...form, players, small, readyCheck: saved.readyCheck, earlyStart: saved.earlyStart };
  }
  return { ...form, players, small };
}

/** The draft timers offered, with the server's first one when none was chosen yet. */
export function draftTimerOf(form: TournamentFormState, formats: TournamentFormats | null): string {
  return orIfEmpty(form.draftTimer, formats?.draftTimerTypes[0] ?? '');
}

/** Host Tournament is enabled (validateTournamentForm): a type, a format and a pairing. */
export function canSubmitTournament(form: TournamentFormState): boolean {
  return form.type !== '' && form.format !== '';
}

/** POST /tournament/create's fields (CreateTournament.createTournament; communication.js createTournament). */
export interface TournamentRequest {
  readonly type: TournamentType;
  readonly deckName: string;
  readonly deckSource: string;
  readonly maxPlayers: number;
  readonly formatCode: string;
  readonly tableDraftTimer: string;
  readonly playoff: PairingType;
  /** "" when not a number. */
  readonly deckbuildingDuration: number | '';
  readonly competitive: boolean;
  readonly startable: boolean;
  readonly readyCheck: number;
}
