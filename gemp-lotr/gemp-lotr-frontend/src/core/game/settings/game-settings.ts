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

/**
 * The phone layout's button under a card that others lie under (the owner, 2026-11-18): shown (▾ opens them below it,
 * ▴ closes them), an empty space that still opens them, or none (they only peek out on its left). A cookie of the
 * modern layout's own, not a legacy one.
 */
export const UNDER_BUTTONS = ['button', 'space', 'none'] as const;
export type UnderButton = (typeof UNDER_BUTTONS)[number];
export const UNDER_BUTTON_COOKIE = 'gempPhoneUnderButton';

/**
 * The modern theme's chat order (the owner, 2026-11-20): the newest lines first (its default since 2026-11-16) or last,
 * as classic always has them. A cookie of the modern layout's own.
 */
export const CHAT_NEWEST_FIRST_COOKIE = 'gempChatNewestFirst';

/**
 * The server reads autoPassPhases from the cookies of its own requests, which a cookie under /gemp-lotr never reaches,
 * so the value is written under the server's path too (the legacy page does the same).
 */
export const SERVER_COOKIE_PATH = '/gemp-lotr-server';
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
  /** The phone layout's button under a card others lie under (default shown). */
  readonly underButton = signal<UnderButton>(
    UNDER_BUTTONS.find((value) => value === this.cookies.read(UNDER_BUTTON_COOKIE)) ?? 'button',
  );
  /** The modern theme's chat lists the newest lines first (default on); classic never does. */
  readonly chatNewestFirst = signal(this.cookies.read(CHAT_NEWEST_FIRST_COOKIE) !== 'false');
  /** -4 (slowest) to 4 (fastest); animations last 2^-speed times as long (default 0). */
  readonly animationSpeed = signal(Number(this.cookies.read(COOKIES.animationSpeed) ?? '0') || 0);
  readonly timerAudioMuted = signal(this.cookies.read(COOKIES.timerAudioMuted) === 'true');
  readonly timerAlertRules = signal<readonly TimerAlertRule[]>(
    parseRules(this.cookies.read(COOKIES.timerAlertRules) ?? DEFAULT_TIMER_ALERTS),
  );

  constructor() {
    // A choice saved before the server's copy existed reaches the server from the game's first request
    const saved = this.cookies.read(COOKIES.autoPassPhases);
    if (saved !== null) this.cookies.write(COOKIES.autoPassPhases, saved, SERVER_COOKIE_PATH);
  }

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
    this.cookies.write(COOKIES.autoPassPhases, phases.join('0'), SERVER_COOKIE_PATH);
  }

  setAutoDismissDeckReveal(value: boolean): void {
    this.autoDismissDeckReveal.set(value);
    this.cookies.write(COOKIES.autoDismissDeckReveal, String(value));
  }

  setOldStackVisuals(value: boolean): void {
    this.oldStackVisuals.set(value);
    this.cookies.write(COOKIES.oldStack, String(value));
  }

  setUnderButton(value: UnderButton): void {
    this.underButton.set(value);
    this.cookies.write(UNDER_BUTTON_COOKIE, value);
  }

  setChatNewestFirst(value: boolean): void {
    this.chatNewestFirst.set(value);
    this.cookies.write(CHAT_NEWEST_FIRST_COOKIE, String(value));
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
