import { parseXml } from '../api/xml';
import { readServerResponse, readServerXml } from '../../testing/files';
import { parseChat, parseIgnored, parseIgnoreResult, parseIncognito } from './chat-parsers';

describe('chat parsers', () => {
  it('reads the messages (server markup as text) and the user list', () => {
    const chat = parseChat(readServerXml('chat.xml'))!;
    expect(chat.room).toBe('Game Hall');
    expect(chat.messages.map((m) => m.from)).toEqual([
      'System',
      'bob',
      'carol',
      'TournamentSystemTo:alice;dave',
      'System',
    ]);
    expect(chat.messages[1]).toEqual({
      from: 'bob',
      date: 1794232860000,
      html: '<p>hi @alice, see <a href="https://wiki.lotrtcgpc.net" target="_blank">https://wiki.lotrtcgpc.net</a></p>',
    });
    expect(chat.users).toEqual([
      { name: 'alice', role: null, self: true, incognito: false },
      { name: 'bob', role: 'admin', self: false, incognito: false },
      { name: 'carol', role: null, self: false, incognito: false },
      { name: 'dave', role: 'leagueAdmin', self: false, incognito: false },
    ]);
  });

  it('understands the old "* " admin prefix, and gives null for another root', () => {
    expect(parseChat(parseXml('<chat><user>* boss</user></chat>'))!.users[0]).toMatchObject({
      name: 'boss',
      role: 'admin',
    });
    expect(parseChat(parseXml('<hall/>'))).toBeNull();
  });

  it('reads the ignore list and the answers of ignoring and going incognito', () => {
    expect(parseIgnored(JSON.parse(readServerResponse('ignores.json')))).toEqual(['Carol']);
    expect(parseIgnored(null)).toEqual([]);
    expect(parseIgnoreResult(JSON.parse(readServerResponse('ignore-added.json')))).toEqual({
      message: 'erin is now ignored.',
      ignored: ['Carol', 'erin'],
    });
    expect(parseIgnoreResult({})).toEqual({ message: null, ignored: null });
    expect(parseIncognito({ inRoom: true, incognito: true })).toBe(true);
    expect(parseIncognito({})).toBeNull();
  });
});
