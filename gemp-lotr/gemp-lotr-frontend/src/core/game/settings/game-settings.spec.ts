import { TestBed } from '@angular/core/testing';
import { SettingCookies } from '../../platform/setting-cookies';
import { COOKIES, GameSettings, SERVER_COOKIE_PATH } from './game-settings';

class FakeCookies {
  readonly values = new Map<string, string>();
  read(name: string): string | null {
    return this.values.get(name) ?? null;
  }
  /** A cookie under another path is kept as `path:name`. */
  write(name: string, value: string, path?: string): void {
    this.values.set(path === undefined ? name : `${path}:${name}`, value);
  }
}

function settings(cookies: Record<string, string> = {}): { settings: GameSettings; cookies: FakeCookies } {
  const fake = new FakeCookies();
  for (const [name, value] of Object.entries(cookies)) fake.values.set(name, value);
  TestBed.configureTestingModule({ providers: [GameSettings, { provide: SettingCookies, useValue: fake }] });
  return { settings: TestBed.inject(GameSettings), cookies: fake };
}

describe('GameSettings', () => {
  it("starts from the legacy page's defaults", () => {
    const { settings: s } = settings();
    expect(s.autoAccept()).toBe(true);
    expect(s.alwaysDropDown()).toBe(false);
    expect(s.autoPassPhases()).toEqual(['FELLOWSHIP', 'MANEUVER', 'ARCHERY', 'ASSIGNMENT', 'REGROUP']);
    expect(s.autoDismissDeckReveal()).toBe(false);
    expect(s.oldStackVisuals()).toBe(false);
    expect(s.animationSpeed()).toBe(0);
    expect(s.timerAudioMuted()).toBe(false);
    expect(s.timerAlertRules()).toEqual([
      { belowSeconds: 600, everySeconds: 30 },
      { belowSeconds: 300, everySeconds: 10 },
      { belowSeconds: 60, everySeconds: 1 },
    ]);
  });

  it('reads the legacy cookies', () => {
    const { settings: s } = settings({
      autoAccept: 'false',
      alwaysDropDown: 'true',
      autoPassPhases: 'SHADOW0SKIRMISH',
      autoDismissDeckReveal: 'true',
      'use-old-stack': 'true',
      'animation-speed': '-2',
      timerAudioMuted: 'true',
      timerAlertRules: '120:5,bad,30:0',
    });
    expect(s.autoAccept()).toBe(false);
    expect(s.alwaysDropDown()).toBe(true);
    expect(s.autoPassPhases()).toEqual(['SHADOW', 'SKIRMISH']);
    expect(s.autoDismissDeckReveal()).toBe(true);
    expect(s.oldStackVisuals()).toBe(true);
    expect(s.animationSpeed()).toBe(-2);
    expect(s.timerAudioMuted()).toBe(true);
    expect(s.timerAlertRules()).toEqual([
      { belowSeconds: 120, everySeconds: 5 },
      { belowSeconds: 30, everySeconds: 0 },
    ]);
  });

  it('the auto-pass phases go to the server too: a saved choice when the page opens, then each change', () => {
    expect(settings().cookies.values.size).toBe(0);
    TestBed.resetTestingModule();
    const { settings: s, cookies } = settings({ autoPassPhases: '' });
    expect(cookies.values.get(`${SERVER_COOKIE_PATH}:${COOKIES.autoPassPhases}`)).toBe('');
    expect(s.autoPassPhases()).toEqual([]);
    s.setAutoPass('SKIRMISH', true);
    expect(cookies.values.get(`${SERVER_COOKIE_PATH}:${COOKIES.autoPassPhases}`)).toBe('SKIRMISH');
  });

  it('an animation speed that is not a number is the default', () => {
    expect(settings({ 'animation-speed': 'fast' }).settings.animationSpeed()).toBe(0);
  });

  it('writes each change to its legacy cookie, as the legacy page wrote it', () => {
    const { settings: s, cookies } = settings();
    s.setAutoAccept(false);
    s.setAlwaysDropDown(true);
    s.setAutoPass('SHADOW', true);
    s.setAutoPass('FELLOWSHIP', false);
    s.setAutoDismissDeckReveal(true);
    s.setOldStackVisuals(true);
    s.setAnimationSpeed(9);
    s.setTimerAudioMuted(true);
    s.setTimerAlertRules([{ belowSeconds: 90, everySeconds: 3 }]);
    expect(Object.fromEntries(cookies.values)).toEqual({
      [COOKIES.autoAccept]: 'false',
      [COOKIES.alwaysDropDown]: 'true',
      [COOKIES.autoPassPhases]: 'SHADOW0MANEUVER0ARCHERY0ASSIGNMENT0REGROUP',
      [`${SERVER_COOKIE_PATH}:${COOKIES.autoPassPhases}`]: 'SHADOW0MANEUVER0ARCHERY0ASSIGNMENT0REGROUP',
      [COOKIES.autoDismissDeckReveal]: 'true',
      [COOKIES.oldStack]: 'true',
      [COOKIES.animationSpeed]: '4',
      [COOKIES.timerAudioMuted]: 'true',
      [COOKIES.timerAlertRules]: '90:3',
    });
    expect(s.autoPassPhases()).toEqual(['SHADOW', 'MANEUVER', 'ARCHERY', 'ASSIGNMENT', 'REGROUP']);
    expect(s.animationSpeed()).toBe(4);
  });
});
