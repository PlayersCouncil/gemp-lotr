import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { LoginPage } from './login-page.service';

describe('LoginPage', () => {
  it('replaces the current page with the login page', () => {
    const replace = vi.fn();
    TestBed.configureTestingModule({
      providers: [{ provide: DOCUMENT, useValue: { defaultView: { location: { replace } } } }],
    });
    TestBed.inject(LoginPage).open();
    expect(replace).toHaveBeenCalledExactlyOnceWith('/gemp-lotr/');
  });
});
