import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { loginPageAddress, LoginPage } from './login-page.service';

describe('LoginPage', () => {
  function setUp() {
    const replace = vi.fn();
    TestBed.configureTestingModule({
      providers: [{ provide: DOCUMENT, useValue: { defaultView: { location: { replace } } } }],
    });
    return { page: TestBed.inject(LoginPage), replace };
  }

  it('replaces the current page with the login page', () => {
    const { page, replace } = setUp();
    page.open();
    expect(replace).toHaveBeenCalledExactlyOnceWith('/gemp-lotr/ng/login');
  });

  it('names the place to come back to', () => {
    const { page, replace } = setUp();
    page.open('/deckbuilder?theme=modern');
    expect(replace).toHaveBeenCalledExactlyOnceWith('/gemp-lotr/ng/login?return=%2Fdeckbuilder%3Ftheme%3Dmodern');
  });

  it('names no place that is not one in the app, nor the hall, nor itself', () => {
    for (const path of ['/', '', 'events', '//evil.example', '/a b', '/login', '/login?return=%2Fx']) {
      expect(loginPageAddress(path)).toBe('/gemp-lotr/ng/login');
    }
    expect(loginPageAddress('/events/leagues?x=1')).toBe('/gemp-lotr/ng/login?return=%2Fevents%2Fleagues%3Fx%3D1');
  });
});
