import { readCookie, writeCookie } from './cookies';

describe('the legacy cookies (cookies.ts)', () => {
  it('reads a cookie by name, decoded as jquery.cookie wrote it', () => {
    const cookies = 'other=1; =odd; gemp.chat=hall%20chat ;autoZoom=true';
    expect(readCookie(cookies, 'gemp.chat')).toBe('hall chat');
    expect(readCookie(cookies, 'autoZoom')).toBe('true');
    expect(readCookie(cookies, 'missing')).toBeNull();
    expect(readCookie('', 'other')).toBeNull();
  });

  it('gives a value that cannot be decoded as it is', () => {
    expect(readCookie('broken=100%', 'broken')).toBe('100%');
  });

  it('writes for a year, under /gemp-lotr unless told otherwise', () => {
    const document = { cookie: '' } as Document;
    writeCookie(document, 'gemp.chat', 'a b', new Date(Date.UTC(2026, 0, 1)));
    expect(document.cookie).toBe(
      'gemp.chat=a%20b; expires=Fri, 01 Jan 2027 00:00:00 GMT; path=/gemp-lotr; SameSite=Lax',
    );
    writeCookie(document, 'x', '1', new Date(Date.UTC(2026, 0, 1)), '/gemp-lotr-server');
    expect(document.cookie).toContain('path=/gemp-lotr-server;');
  });
});
