import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { SettingCookies } from './setting-cookies';

describe('SettingCookies', () => {
  it('writes under /gemp-lotr, or under the path given for a cookie the server reads', () => {
    const written: string[] = [];
    const document = {
      get cookie(): string {
        return 'a=1%202';
      },
      set cookie(value: string) {
        written.push(value);
      },
    };
    TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: document }] });
    const cookies = TestBed.inject(SettingCookies);
    expect(cookies.read('a')).toBe('1 2');
    cookies.write('b', 'x');
    cookies.write('b', 'y', '/gemp-lotr-server');
    expect(written[0]).toMatch(/^b=x; expires=.*; path=\/gemp-lotr; SameSite=Lax$/);
    expect(written[1]).toMatch(/^b=y; expires=.*; path=\/gemp-lotr-server; SameSite=Lax$/);
  });
});
