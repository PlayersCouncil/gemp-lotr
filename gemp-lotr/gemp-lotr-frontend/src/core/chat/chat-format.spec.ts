import {
  displayedSender,
  formatChatTimestamp,
  highlightOf,
  isIgnored,
  orderUsers,
  tournamentAnnouncement,
} from './chat-format';

describe('chat format', () => {
  it('writes timestamps like the legacy hall, in local time', () => {
    expect(formatChatTimestamp(new Date(2026, 10, 9, 8, 5, 3).getTime())).toBe('Nov 9 08:05:03');
  });

  it('tells pings from mentions', () => {
    expect(highlightOf('<p>hi @alice</p>', 'alice')).toBe('ping');
    expect(highlightOf('<p>alice is great</p>', 'alice')).toBe('mention');
    expect(highlightOf('<p>@everyone ready?</p>', 'alice')).toBe('mention');
    expect(highlightOf('<p>@anyone?</p>', null)).toBe('mention');
    expect(highlightOf('<p>malice</p>', 'alice')).toBeNull();
    expect(highlightOf('<p>Alice</p>', 'alice')).toBeNull();
    // names are matched literally
    expect(highlightOf('<p>a.b</p>', 'a.b')).toBe('mention');
    expect(highlightOf('<p>axb</p>', 'a.b')).toBeNull();
  });

  it('routes tournament messages', () => {
    expect(tournamentAnnouncement('TournamentSystem', 'x')).toEqual({ message: 'x', recipients: null });
    expect(tournamentAnnouncement('TournamentSystemTo:alice;dave', 'x')).toEqual({
      message: 'x',
      recipients: ['alice', 'dave'],
    });
    expect(tournamentAnnouncement('bob', 'x')).toBeNull();
    expect(displayedSender('TournamentSystemTo:alice')).toBe('TournamentSystem');
    expect(displayedSender('bob')).toBe('bob');
  });

  it('matches ignored names ignoring case, and lists the viewer first', () => {
    expect(isIgnored(['Carol'], 'carol')).toBe(true);
    expect(isIgnored(['Carol'], 'carl')).toBe(false);
    const users = [
      { name: 'bob', role: null, self: false, incognito: false },
      { name: 'zed', role: null, self: true, incognito: false },
    ];
    expect(orderUsers(users).map((u) => u.name)).toEqual(['zed', 'bob']);
  });
});
