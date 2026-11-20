import { hallReturnLink, HALL_URL, loginTarget } from './return-link';

describe('hallReturnLink', () => {
  it('keeps a hall link the visitor came from', () => {
    expect(hallReturnLink('#events/leagues')).toBe('#events/leagues');
    expect(hallReturnLink('#tab?x=1&y=a%20b.c+d-e_f')).toBe('#tab?x=1&y=a%20b.c+d-e_f');
    expect(loginTarget('#events/leagues')).toBe('/gemp-lotr/hall.html#events/leagues');
  });

  it('drops anything else', () => {
    for (const hash of ['', '#', 'events', '#a b', '#<script>', '#"x"', '#' + 'a'.repeat(201)]) {
      expect(hallReturnLink(hash)).toBe('');
    }
    expect(hallReturnLink('#' + 'a'.repeat(200))).toHaveLength(201);
    expect(loginTarget('#a b')).toBe(HALL_URL);
  });
});
