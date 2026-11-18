import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CompactLayout } from '../../platform/compact-layout.service';
import { SettingCookies } from '../../platform/setting-cookies';
import { formatSplit, parseSplit, PHONE_COOKIES, PhoneSettings } from './phone-settings';

describe('the divider places in gempGameSplit', () => {
  it('reads each device: a height or hidden, ignoring what it does not know', () => {
    expect(parseSplit('phone:170|sideways:hidden|tablet:abc|watch:3')).toEqual({ phone: 170, sideways: 'hidden' });
    expect(parseSplit(null)).toEqual({});
  });

  it('writes them back in a fixed order', () => {
    expect(formatSplit({ tablet: 200, phone: 'hidden' })).toBe('phone:hidden|tablet:200');
  });
});

describe('PhoneSettings', () => {
  let cookies: Record<string, string>;
  const device = signal<'phone' | 'sideways' | 'tablet' | null>('phone');

  function create(): PhoneSettings {
    TestBed.configureTestingModule({
      providers: [
        PhoneSettings,
        { provide: CompactLayout, useValue: { device } },
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
    device.set('sideways');
    expect(settings.hand()).toBe(150);
    settings.setHand('hidden');
    expect(settings.handHidden()).toBe(true);
    expect(cookies[PHONE_COOKIES.split]).toBe('phone:222|sideways:hidden');
    device.set('phone');
    expect(settings.hand()).toBe(222);
  });

  it("sizes the table's cards for the device and the card size setting, within its range", () => {
    cookies[PHONE_COOKIES.cardSize] = '120';
    const settings = create();
    expect(settings.cardHeight()).toBe(120);
    device.set('tablet');
    expect(settings.cardHeight()).toBe(156);
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
});
