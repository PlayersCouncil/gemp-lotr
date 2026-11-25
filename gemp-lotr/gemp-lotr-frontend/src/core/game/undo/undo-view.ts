/**
 * What taking a game back shows, in the legacy page's words (js/gemp-022/undo.js): each tier's mark and word, the list
 * of places divided by turn and phase, the explanation of the tiers, the request's countdown. Pure.
 */
import { PendingUndo, RestorePoint, UndoPolicy, UndoTier } from './undo-protocol';

/**
 * How a tier is shown: its mark (the colour says the same), what going back to it is called, what it means, and what
 * the page's styles call its look (the colour: own, replays, revealed).
 */
export const TIERS: Readonly<
  Record<UndoTier, { mark: string; word: string; means: string; look: 'own' | 'replays' | 'revealed' }>
> = {
  GREEN: { mark: '✓', word: 'Undo', means: 'Only your own choice. Nobody is asked.', look: 'own' },
  YELLOW: { mark: '~', word: 'Revert', means: 'Your opponent replays choices.', look: 'replays' },
  RED: { mark: '✕', word: 'Revert', means: 'Hidden information was revealed.', look: 'revealed' },
};

/** The Undo button's text. */
export const UNDO_BUTTON = `${TIERS.GREEN.mark} Undo`;

/** "Turn 2 · Fellowship"; what comes before the first turn is the game being set up. */
export function when(turn: number, phase: string): string {
  return (turn > 0 ? `Turn ${turn}` : 'Setup') + (phase ? ` · ${phase}` : '');
}

/** A place's button: the tier's mark and word ("✓ Undo", "~ Revert"). */
export function goLabel(tier: UndoTier): string {
  return `${TIERS[tier].mark} ${TIERS[tier].word}`;
}

/** The explanation the "i" button opens: a line per tier. A revert needs approval only where somebody is asked. */
export function tierHelp(policy: UndoPolicy | null): { tier: UndoTier; word: string; means: string }[] {
  const asks = policy?.asks ?? false;
  return (Object.keys(TIERS) as UndoTier[]).map((tier) => ({
    tier,
    word: ` ${TIERS[tier].word}. `,
    means: TIERS[tier].means + (tier === 'GREEN' || !asks ? '' : ' Needs their approval.'),
  }));
}

/** The list's places under their headings, latest first: a heading each time the turn or phase changes. */
export function listGroups(points: readonly RestorePoint[]): { when: string; points: RestorePoint[] }[] {
  const groups: { when: string; points: RestorePoint[] }[] = [];
  for (const point of points) {
    const heading = when(point.turn, point.phase);
    const last = groups.at(-1);
    if (last?.when === heading) last.points.push(point);
    else groups.push({ when: heading, points: [point] });
  }
  return groups;
}

/** What the list says above its places, if anything: a request waiting, or nothing to go back to. */
export function listNote(points: readonly RestorePoint[], pending: PendingUndo | null): string | null {
  if (pending) return 'A revert request is pending.';
  return points.length === 0 ? 'Nothing to go back to.' : null;
}

/** The request's countdown. */
export function clockLine(secondsLeft: number): string {
  return secondsLeft > 0 ? `Game paused · auto-declines in ${secondsLeft}s` : 'Out of time';
}

/** The head of a request's dialog, and its title: the asker's own, or another player's. */
export function requestHead(pending: PendingUndo, mine: boolean): { title: string; head: string } {
  return mine
    ? { title: 'Revert requested', head: 'You asked to revert to before:' }
    : { title: 'Revert request', head: `${pending.requester} asks to revert to before:` };
}
