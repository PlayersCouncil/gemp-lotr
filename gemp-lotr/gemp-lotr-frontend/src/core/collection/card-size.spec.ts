import { TestBed } from '@angular/core/testing';
import { SettingCookies } from '../platform/setting-cookies';
import { CARD_SIZE_COOKIE, CARD_SIZE_DEFAULT, CardSize, parseCardSize } from './card-size';

describe('CardSize', () => {
  let stored: Map<string, string>;

  function cardSize(): CardSize {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        CardSize,
        {
          provide: SettingCookies,
          useValue: {
            read: (name: string) => stored.get(name) ?? null,
            write: (n: string, v: string) => stored.set(n, v),
          },
        },
      ],
    });
    return TestBed.inject(CardSize);
  }

  beforeEach(() => (stored = new Map()));

  it('starts at the layout’s own size, or the remembered one', () => {
    expect(cardSize().percent()).toBe(CARD_SIZE_DEFAULT);
    stored.set(CARD_SIZE_COOKIE, '150');
    expect(cardSize().percent()).toBe(150);
  });

  it('remembers a size set, within its limits and on a step', () => {
    const size = cardSize();
    size.set(130);
    expect(size.percent()).toBe(130);
    expect(stored.get(CARD_SIZE_COOKIE)).toBe('130');
    size.set(1000);
    expect(size.percent()).toBe(200);
    expect(cardSize().percent()).toBe(200);
  });

  it('reads a broken or unknown value as the default, and keeps others within the limits', () => {
    expect(parseCardSize(null)).toBe(CARD_SIZE_DEFAULT);
    expect(parseCardSize('big')).toBe(CARD_SIZE_DEFAULT);
    expect(parseCardSize('10')).toBe(50);
    expect(parseCardSize('500')).toBe(200);
    expect(parseCardSize('124')).toBe(120);
  });
});
