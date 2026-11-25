/**
 * When the clocks call for the player's attention (the legacy game page's fireTimerCues and fireDecisionCues), and the
 * colour bands of the clocks. Pure.
 */
import { TimerAlertRule } from '../settings/game-settings';

/**
 * The bank clock went from `previous` to `current` seconds: on a whole minute, a pulse only; else, where the most
 * specific rule (the smallest threshold under which the clock is) has its moment, a pulse and a sound.
 */
export function bankCue(previous: number, current: number, rules: readonly TimerAlertRule[]): 'pulse' | 'sound' | null {
  if (current >= previous) return null;
  // The clock may have jumped more than a second (the server's value came): any minute mark passed counts
  for (let s = current; s < previous; s++) {
    if (s % 60 === 0 && s > 0) return 'pulse';
  }
  let rule: TimerAlertRule | null = null;
  for (const candidate of rules) {
    if (
      current < candidate.belowSeconds &&
      candidate.everySeconds > 0 &&
      (!rule || candidate.belowSeconds < rule.belowSeconds)
    ) {
      rule = candidate;
    }
  }
  return rule && current > 0 && current % rule.everySeconds === 0 ? 'sound' : null;
}

/** The decision's time left went from `previous` to `remaining`: at 60, 30 and 10 seconds, then every second. */
export function decisionCue(previous: number, remaining: number): boolean {
  if (remaining >= previous) return false;
  for (let s = remaining; s < previous; s++) {
    if (s === 60 || s === 30 || s === 10 || (s > 0 && s < 10)) return true;
  }
  return false;
}

export type ClockBand = 'warn' | 'danger' | null;

/** The player's bank clock: orange under 10 minutes, red under 5. */
export function bankBand(seconds: number): ClockBand {
  return seconds < 300 ? 'danger' : seconds < 600 ? 'warn' : null;
}

/** The decision's clock: red under 30 seconds left, orange under a minute or a quarter of its limit. */
export function decisionBand(remaining: number | null, limit: number): ClockBand {
  if (remaining === null) return null;
  return remaining < 30 ? 'danger' : remaining < 60 || remaining < limit / 4 ? 'warn' : null;
}
