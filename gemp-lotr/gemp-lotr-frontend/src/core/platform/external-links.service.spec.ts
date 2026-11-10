import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ExternalLinks } from './external-links.service';

describe('ExternalLinks', () => {
  const PAGE = 'https://gemp.example/gemp-lotr/ng/deckbuilder/';
  let open: ReturnType<typeof vi.fn>;

  const links = (defaultView: unknown = { open, location: { href: PAGE } }) => {
    TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: { defaultView } }] });
    return TestBed.inject(ExternalLinks);
  };

  beforeEach(() => (open = vi.fn()));

  it('opens a web page in a new tab that has no handle on this page', () => {
    links().open('http://gemp.example/share/deck?id=abc');
    expect(open).toHaveBeenCalledExactlyOnceWith('http://gemp.example/share/deck?id=abc', '_blank', 'noopener');
  });

  it("opens an address on the site relative to this page's", () => {
    links().open('/gemp-lotr-server/deck/libraryHtml?deckName=Starter');
    expect(open).toHaveBeenCalledExactlyOnceWith(
      'https://gemp.example/gemp-lotr-server/deck/libraryHtml?deckName=Starter',
      '_blank',
      'noopener',
    );
  });

  it('ignores the white space around an address the server answered', () => {
    links().open(' https://gemp.example/share/deck?id=abc\n');
    expect(open).toHaveBeenCalledExactlyOnceWith('https://gemp.example/share/deck?id=abc', '_blank', 'noopener');
  });

  it.each([
    'javascript:alert(1)',
    ' JavaScript:alert(1)',
    'data:text/html,<script>alert(1)</script>',
    'file:///etc/passwd',
    'http://',
  ])('does not open %s', (address) => {
    links().open(address);
    expect(open).not.toHaveBeenCalled();
  });

  it('does nothing without a window', () => {
    expect(() => links(null).open('https://gemp.example/')).not.toThrow();
  });
});
