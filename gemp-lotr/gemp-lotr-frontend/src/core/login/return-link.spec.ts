import { loginTarget, returnPath } from './return-link';

const asked = (path: string) => '?return=' + encodeURIComponent(path);

describe('returnPath', () => {
  it('keeps a place in the app the visitor came from', () => {
    expect(returnPath(asked('/events/leagues'))).toBe('/events/leagues');
    expect(returnPath(asked('/deckbuilder?theme=modern&x=a%20b.c+d-e_f'))).toBe(
      '/deckbuilder?theme=modern&x=a%20b.c+d-e_f',
    );
    expect(loginTarget(asked('/events/leagues'))).toBe('/gemp-lotr/ng/events/leagues');
    expect(loginTarget('?theme=modern&' + asked('/help').slice(1))).toBe('/gemp-lotr/ng/help');
  });

  it('drops anything else, and goes to the hall', () => {
    for (const path of ['', 'events', '//evil.example', '/a b', '/<script>', '/"x"', '/' + 'a'.repeat(201), '/login']) {
      expect(returnPath(asked(path))).toBe('');
    }
    expect(returnPath('')).toBe('');
    expect(returnPath(asked('/' + 'a'.repeat(200)))).toHaveLength(201);
    expect(loginTarget(asked('https://evil.example/'))).toBe('/gemp-lotr/ng/');
    expect(loginTarget('')).toBe('/gemp-lotr/ng/');
  });
});
