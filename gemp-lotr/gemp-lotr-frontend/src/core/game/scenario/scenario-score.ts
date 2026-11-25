/**
 * A scored puzzle's score as its end event tells it (ep-notes.md P7: `score`), and how the end panel says it. Pure.
 */

export interface ScenarioScore {
  /** The score this game made; null when it could not be read. */
  value: number | null;
  /** What the scenario calls it ("Sméagol's strength"), or null. */
  label: string | null;
  /** Which way is better. */
  better: 'higher' | 'lower';
  /** The person's best (this game's counts only if it was complete), or null for none yet. */
  best: number | null;
  /** This game beat the person's best (or is the first). */
  newBest: boolean;
}

type Json = Record<string, unknown>;
const number = (value: unknown): number | null => (typeof value === 'number' && Number.isFinite(value) ? value : null);

/** The end event's `score`; null for an unscored scenario. */
export function readScenarioScore(value: unknown): ScenarioScore | null {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) return null;
  const json = value as Json;
  return {
    value: number(json['value']),
    label: typeof json['label'] === 'string' && json['label'] !== '' ? json['label'] : null,
    better: json['better'] === 'lower' ? 'lower' : 'higher',
    best: number(json['best']),
    newBest: json['newBest'] === true,
  };
}

/** The end panel's lines for a score: what it is called, the score, the best, and the title saying which way is better. */
export interface ScoreLines {
  label: string;
  value: string;
  best: string | null;
  newBest: boolean;
  title: string;
}

export function scoreLines(score: ScenarioScore): ScoreLines {
  return {
    label: score.label ?? 'Score',
    value: score.value === null ? '?' : String(score.value),
    best: score.best === null ? null : String(score.best),
    newBest: score.newBest,
    title: score.better === 'lower' ? 'Lower is better' : 'Higher is better',
  };
}
