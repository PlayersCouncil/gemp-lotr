import { inject, Injectable, signal } from '@angular/core';
import { SettingCookies } from '../../platform/setting-cookies';

/** The phases the game passes for the player when they have nothing to play, as the server names them. */
export const AUTO_PASS_PHASES = ['FELLOWSHIP', 'SHADOW', 'MANEUVER', 'ARCHERY', 'ASSIGNMENT', 'SKIRMISH', 'REGROUP'];

/** A timer alert rule: below `belowSeconds` left in the bank, a sound every `everySeconds` (0 = never). */
export interface TimerAlertRule {
  belowSeconds: number;
  everySeconds: number;
}

/**
 * The legacy cookies of the game page's settings (D4 of trymarz/gemp-lotr-private#27: names and values kept; the
 * server itself reads autoPassPhases). Each default is the legacy page's.
 */
export const COOKIES = {
  autoAccept: 'autoAccept',
  alwaysDropDown: 'alwaysDropDown',
  autoPassPhases: 'autoPassPhases',
  autoDismissDeckReveal: 'autoDismissDeckReveal',
  oldStack: 'use-old-stack',
  animationSpeed: 'animation-speed',
  timerAudioMuted: 'timerAudioMuted',
  timerAlertRules: 'timerAlertRules',
} as const;

const DEFAULT_AUTO_PASS = 'FELLOWSHIP0MANEUVER0ARCHERY0ASSIGNMENT0REGROUP';
const DEFAULT_TIMER_ALERTS = '600:30,300:10,60:1';

function parseRules(text: string): TimerAlertRule[] {
  return text
    .split(',')
    .map((rule) => rule.split(':').map(Number))
    .filter(([below, every]) => Number.isFinite(below) && Number.isFinite(every))
    .map(([below = 0, every = 0]) => ({ belowSeconds: below, everySeconds: every }));
}

/** The game page's settings, read from and written to the legacy cookies. Page-scoped. */
@Injectable()
export class GameSettings {
  private readonly cookies = inject(SettingCookies);

  /** After choosing a card or an action, the decision is answered at once (default on). */
  readonly autoAccept = signal(this.cookies.read(COOKIES.autoAccept) !== 'false');
  /** A choice of two shows a drop-down list too (default off). */
  readonly alwaysDropDown = signal(this.cookies.read(COOKIES.alwaysDropDown) === 'true');
  readonly autoPassPhases = signal<readonly string[]>(
    (this.cookies.read(COOKIES.autoPassPhases) ?? DEFAULT_AUTO_PASS).split('0').filter(Boolean),
  );
  /** Searching one's own deck skips the window showing the whole deck (default off). */
  readonly autoDismissDeckReveal = signal(this.cookies.read(COOKIES.autoDismissDeckReveal) === 'true');
  /** Stacked cards drawn the old way (default off). */
  readonly oldStackVisuals = signal(this.cookies.read(COOKIES.oldStack) === 'true');
  /** -4 (slowest) to 4 (fastest); animations last 2^-speed times as long (default 0). */
  readonly animationSpeed = signal(Number(this.cookies.read(COOKIES.animationSpeed) ?? '0') || 0);
  readonly timerAudioMuted = signal(this.cookies.read(COOKIES.timerAudioMuted) === 'true');
  readonly timerAlertRules = signal<readonly TimerAlertRule[]>(
    parseRules(this.cookies.read(COOKIES.timerAlertRules) ?? DEFAULT_TIMER_ALERTS),
  );

  setAutoAccept(value: boolean): void {
    this.autoAccept.set(value);
    this.cookies.write(COOKIES.autoAccept, String(value));
  }

  setAlwaysDropDown(value: boolean): void {
    this.alwaysDropDown.set(value);
    this.cookies.write(COOKIES.alwaysDropDown, String(value));
  }

  /** Auto-pass in one phase on or off; written in the server's order, as the legacy page wrote it. */
  setAutoPass(phase: string, value: boolean): void {
    const phases = AUTO_PASS_PHASES.filter((p) => (p === phase ? value : this.autoPassPhases().includes(p)));
    this.autoPassPhases.set(phases);
    this.cookies.write(COOKIES.autoPassPhases, phases.join('0'));
  }

  setAutoDismissDeckReveal(value: boolean): void {
    this.autoDismissDeckReveal.set(value);
    this.cookies.write(COOKIES.autoDismissDeckReveal, String(value));
  }

  setOldStackVisuals(value: boolean): void {
    this.oldStackVisuals.set(value);
    this.cookies.write(COOKIES.oldStack, String(value));
  }

  setAnimationSpeed(value: number): void {
    const speed = Math.max(-4, Math.min(4, Math.round(value)));
    this.animationSpeed.set(speed);
    this.cookies.write(COOKIES.animationSpeed, String(speed));
  }

  setTimerAudioMuted(value: boolean): void {
    this.timerAudioMuted.set(value);
    this.cookies.write(COOKIES.timerAudioMuted, String(value));
  }

  setTimerAlertRules(rules: readonly TimerAlertRule[]): void {
    this.timerAlertRules.set(rules);
    this.cookies.write(COOKIES.timerAlertRules, rules.map((r) => `${r.belowSeconds}:${r.everySeconds}`).join(','));
  }
}
