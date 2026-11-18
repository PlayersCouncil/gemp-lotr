/** The game the legacy page holds (controls.mjs readLegacyModel): the same in a browser and in jsdom. */
export interface LegacyGameModel {
  twilight: string | null;
  phase: string | null;
  initiative: string | null;
  ruleOf4: string | null;
  alert: string | null;
  stats: Record<string, string | null>;
  cards: string[];
}

/** Runs in the legacy game page (uses its window.$ and window.ui). */
export function readLegacyModel(): LegacyGameModel;
