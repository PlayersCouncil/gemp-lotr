import { DOCUMENT } from '@angular/common';
import { Injector } from '@angular/core';
import { LegacyCookies } from './legacy-cookies';

describe('LegacyCookies', () => {
  it("reads and writes the page's cookies the legacy way", () => {
    const document = { cookie: 'autoZoomInHall=false; foil=1' } as Document;
    const cookies = Injector.create({ providers: [{ provide: DOCUMENT, useValue: document }, LegacyCookies] }).get(
      LegacyCookies,
    );
    expect(cookies.read('foil')).toBe('1');
    expect(cookies.read('none')).toBeNull();
    cookies.write('foil', 'a b');
    expect(document.cookie).toMatch(/^foil=a%20b; expires=.+; path=\/gemp-lotr; SameSite=Lax$/);
  });
});
