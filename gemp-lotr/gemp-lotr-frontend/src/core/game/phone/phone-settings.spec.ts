import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CompactLayout } from '../../platform/compact-layout.service';
import { SettingCookies } from '../../platform/setting-cookies';
import { CARD_HEIGHT, DEFAULT_HAND, formatSplit, parseSplit, PHONE_COOKIES, PhoneSettings } from './phone-settings';

describe('the divider places in gempGameSplit', () => {
  it('reads each device: a height or hidden, ignoring what it does not know', () => {
    // A phone held sideways has the computer layout: a place kept for it before is dropped
    expect(parseSplit('phone:170|sideways:hidden|tablet:abc|watch:3')).toEqual({ phone: 170 });
    expect(parseSplit('tablet:hidden')).toEqual({ tablet: 'hidden' });
    expect(parseSplit(null)).toEqual({});
  });

  it('writes them back in a fixed order', () => {
    expect(formatSplit({ tablet: 200, phone: 'hidden' })).toBe('phone:hidden|tablet:200');
  });
});

describe('PhoneSettings', () => {
  let cookies: Record<string, string>;
  const device = signal<'phone' | 'tablet' | null>('phone');

  function create(): PhoneSettings {
    TestBed.configureTestingModule({
      providers: [
        PhoneSettings,
        { provide: CompactLayout, useValue: { gameDevice: device } },
        {
          provide: SettingCookies,
          useValue: {
            read: (name: string) => cookies[name] ?? null,
            write: (name: string, value: string) => (cookies[name] = value),
          },
        },
      ],
    });
    return TestBed.inject(PhoneSettings);
  }

  beforeEach(() => {
    cookies = {};
    device.set('phone');
  });

  it("keeps the divider's place per device, the hand hidden too", () => {
    const settings = create();
    expect(settings.hand()).toBe(170);
    settings.setHand(222.4);
    device.set('tablet');
    expect(settings.hand()).toBe(DEFAULT_HAND.tablet);
    settings.setHand('hidden');
    expect(settings.handHidden()).toBe(true);
    expect(cookies[PHONE_COOKIES.split]).toBe('phone:222|tablet:hidden');
    device.set('phone');
    expect(settings.hand()).toBe(222);
  });

  it("sizes the table's cards for the device and the card size setting, within its range", () => {
    cookies[PHONE_COOKIES.cardSize] = '120';
    const settings = create();
    expect(settings.cardHeight()).toBe(120);
    device.set('tablet');
    // A tablet's cards at 100% are CARD_HEIGHT.tablet: 120% of them
    expect(settings.cardHeight()).toBe(CARD_HEIGHT.tablet * 1.2);
    settings.setCardSize(400);
    expect(settings.cardSize()).toBe(150);
    expect(cookies[PHONE_COOKIES.cardSize]).toBe('150');
  });

  it('has the "Your move" alerts on unless the player turned them off', () => {
    cookies[PHONE_COOKIES.moveSound] = 'false';
    const settings = create();
    expect(settings.moveVibrate()).toBe(true);
    expect(settings.moveSound()).toBe(false);
    settings.setMoveVibrate(false);
    expect(cookies[PHONE_COOKIES.moveVibrate]).toBe('false');
  });

  it('scrolls offered cards sideways unless the player chose rows scrolled down, kept in its cookie', () => {
    expect(create().choicesScroll()).toBe('sideways');
    TestBed.resetTestingModule();
    cookies[PHONE_COOKIES.choicesScroll] = 'down';
    const settings = create();
    expect(settings.choicesScroll()).toBe('down');
    settings.setChoicesScroll('sideways');
    expect(settings.choicesScroll()).toBe('sideways');
    expect(cookies[PHONE_COOKIES.choicesScroll]).toBe('sideways');
    TestBed.resetTestingModule();
    cookies[PHONE_COOKIES.choicesScroll] = 'diagonal';
    expect(create().choicesScroll()).toBe('sideways');
  });
});
