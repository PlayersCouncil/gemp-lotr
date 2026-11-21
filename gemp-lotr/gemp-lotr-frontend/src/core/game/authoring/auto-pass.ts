/**
 * The phases the player is passed through (options.autoPass) as the Scenario tab's "How it plays" ticks them: one
 * list, or (A3, a two-sided game) a list for each role the person plays — while they are the Free Peoples player
 * (their own turns) and while they are the Shadow player. Read from any way the file writes it (a line of phases,
 * `true` for an ordinary table's, `false`, or `{freeps, shadow}`), and written back as the server writes it: the one
 * list as a line (none: taken out), the roles always both, a role with none as `false`. Pure.
 */
import { DraftOptions } from './authoring-state';
import { PHASES, splitTerms } from './terms';

export type Role = 'freeps' | 'shadow';
export const ROLE_NAMES: Readonly<Record<Role, string>> = { freeps: 'as Free Peoples', shadow: 'as Shadow' };

/** The phases an ordinary table passes a player through (what `autoPass: true` means). */
export const TABLE_PHASES: readonly string[] = ['fellowship', 'maneuver', 'archery', 'assignment', 'regroup'];

/** What the ticks show: said for each role or not, and the phases of each (in a turn's order). */
export interface AutoPass {
  byRole: boolean;
  /** The one list (byRole false). */
  all: readonly string[];
  freeps: readonly string[];
  shadow: readonly string[];
}

type Written = NonNullable<DraftOptions['autoPass']>;

/** A turn's phases, in order, of those named. */
const inOrder = (names: Iterable<string>): string[] => {
  const named = new Set([...names].map((name) => name.toLowerCase()));
  return PHASES.filter((phase) => named.has(phase));
};

/** One list as the file can write it: a line of phases, true (an ordinary table's) or false (none). */
function phasesOf(value: string | boolean | undefined): string[] {
  if (value === undefined || value === false) return [];
  if (value === true) return [...TABLE_PHASES];
  const said = value.trim().toLowerCase();
  if (said === 'true') return [...TABLE_PHASES];
  return said === 'false' ? [] : inOrder(splitTerms(value));
}

/** The options' autoPass, read. */
export function autoPassView(options: DraftOptions | undefined): AutoPass {
  const value = options?.autoPass;
  if (value !== undefined && typeof value === 'object') {
    return { byRole: true, all: [], freeps: phasesOf(value.freeps), shadow: phasesOf(value.shadow) };
  }
  const all = phasesOf(value);
  return { byRole: false, all, freeps: all, shadow: all };
}

/** What `options.autoPass` becomes; null: taken out (no phase at all, said as one list). */
export function autoPassValue(view: AutoPass): Written | null {
  if (view.byRole) {
    const role = (phases: readonly string[]) => (phases.length === 0 ? false : inOrder(phases).join(', '));
    return { freeps: role(view.freeps), shadow: role(view.shadow) };
  }
  return view.all.length === 0 ? null : inOrder(view.all).join(', ');
}

/** One phase ticked or not: in the one list, or in a role's. */
export function withPhase(view: AutoPass, role: Role | null, phase: string, on: boolean): AutoPass {
  const tick = (phases: readonly string[]) => inOrder(on ? [...phases, phase] : phases.filter((one) => one !== phase));
  if (role === null) {
    const all = tick(view.all);
    return { byRole: false, all, freeps: all, shadow: all };
  }
  return { ...view, [role]: tick(view[role]) };
}

/**
 * "For each role" ticked: each role starts from the one list. Unticked: one list, every phase either role had (null
 * where that loses nothing; else `lost` says what goes).
 */
export function withByRole(view: AutoPass, byRole: boolean): { view: AutoPass; lost: string | null } {
  if (byRole) return { view: { byRole: true, all: [], freeps: view.all, shadow: view.all }, lost: null };
  const all = inOrder([...view.freeps, ...view.shadow]);
  const differ = (phases: readonly string[]) => phases.length !== all.length;
  const lost =
    differ(view.freeps) || differ(view.shadow)
      ? `One list for both roles: ${all.join(', ') || 'none'}. (Free Peoples had ${
          view.freeps.join(', ') || 'none'
        }; Shadow had ${view.shadow.join(', ') || 'none'}.)`
      : null;
  return { view: { byRole: false, all, freeps: all, shadow: all }, lost };
}

/** Whether the person is passed through a phase while playing a role. */
export function passedIn(options: DraftOptions | undefined, role: Role, phase: string): boolean {
  return autoPassView(options)[role].includes(phase.toLowerCase());
}
