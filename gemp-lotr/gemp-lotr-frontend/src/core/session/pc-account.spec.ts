import { readServerResponse } from '../../testing/files';
import {
  canEnter,
  isAliasName,
  NO_PC_SESSION,
  openedForm,
  parsePcSession,
  pcSignInAddress,
  sessionKind,
} from './pc-account';

const session = (name: string) => parsePcSession(JSON.parse(readServerResponse(name)));

describe('parsePcSession', () => {
  it("reads PC-ACCOUNT.md's example", () => {
    expect(session('argonauth-session.json')).toEqual({
      mode: 'optional',
      player: 'Underhill',
      pcAccount: { name: 'Frodo Baggins', manageUrl: 'https://auth.lotrtcgpc.net/', fresh: true },
      linked: true,
      aliases: [
        { id: 900001, name: 'Frodo', current: false, busy: false },
        { id: 900002, name: 'Underhill', current: true, busy: true },
      ],
      blockedBy: 'Underhill',
    });
  });

  it('reads a lapsed sign-in, and a password login', () => {
    expect(session('argonauth-session-stale.json').pcAccount?.fresh).toBe(false);
    expect(session('argonauth-session-password.json')).toEqual({
      mode: 'optional',
      player: 'Samwise',
      pcAccount: null,
      linked: false,
      aliases: [],
      blockedBy: null,
    });
  });

  it('takes anything it does not understand as test mode with nobody', () => {
    expect(parsePcSession(null)).toBe(NO_PC_SESSION);
    expect(parsePcSession('optional')).toBe(NO_PC_SESSION);
    expect(parsePcSession({ mode: 'required', player: 'x' }).mode).toBe('test');
    expect(
      parsePcSession({
        mode: 'optional',
        player: '',
        pcAccount: { fresh: 'yes' },
        linked: 'no',
        aliases: [{ id: '1', name: 'a' }, { id: 2 }, null, { id: 3, name: 'Merry', current: 1 }],
        blockedBy: 7,
      }),
    ).toEqual({
      mode: 'optional',
      player: null,
      pcAccount: { name: '', manageUrl: '', fresh: true },
      linked: null,
      aliases: [{ id: 3, name: 'Merry', current: false, busy: false }],
      blockedBy: null,
    });
    expect(parsePcSession({ mode: 'optional', pcAccount: 'x', aliases: 'x' }).aliases).toEqual([]);
  });
});

describe('sessionKind', () => {
  it('tells test mode, nobody, a password login and a PC account apart', () => {
    expect(sessionKind(session('argonauth-session-test.json'))).toBe('test');
    expect(sessionKind(session('argonauth-session-nobody.json'))).toBe('nobody');
    expect(sessionKind(session('argonauth-session-password.json'))).toBe('password');
    expect(sessionKind(session('argonauth-session.json'))).toBe('pc');
    expect(sessionKind(session('argonauth-session-no-aliases.json'))).toBe('pc');
  });
});

describe('canEnter', () => {
  it("lets only this browser's alias be entered while it is busy", () => {
    const blocked = session('argonauth-session.json');
    expect(canEnter(blocked, blocked.aliases[0])).toBe(false);
    expect(canEnter(blocked, blocked.aliases[1])).toBe(true);
    // An alias busy in another browser gates nothing here
    const elsewhere = { ...blocked, player: 'Frodo', blockedBy: null };
    expect(elsewhere.aliases.every((alias) => canEnter(elsewhere, alias))).toBe(true);
    const free = session('argonauth-session-aliases.json');
    expect(free.aliases.every((alias) => canEnter(free, alias))).toBe(true);
  });
});

describe('isAliasName', () => {
  it("keeps Gemp's rule: 2 to 30 of letters, digits, _ and -", () => {
    expect(isAliasName('ab')).toBe(true);
    expect(isAliasName('Frodo_of-the9')).toBe(true);
    expect(isAliasName('x'.repeat(30))).toBe(true);
    expect(isAliasName('x'.repeat(31))).toBe(false);
    expect(isAliasName('a')).toBe(false);
    expect(isAliasName('two words')).toBe(false);
    expect(isAliasName('Éowyn')).toBe(false);
  });
});

describe('pcSignInAddress', () => {
  it("is the server's sign-in, with what it is for and where to come back to", () => {
    expect(pcSignInAddress()).toBe('/gemp-lotr-server/argonauth/login');
    expect(pcSignInAddress('connect')).toBe('/gemp-lotr-server/argonauth/login?then=connect');
    expect(pcSignInAddress(null, '/events/leagues')).toBe(
      '/gemp-lotr-server/argonauth/login?return=%2Fevents%2Fleagues',
    );
    expect(pcSignInAddress('create', '/deckbuilder')).toBe(
      '/gemp-lotr-server/argonauth/login?then=create&return=%2Fdeckbuilder',
    );
  });
});

describe('openedForm', () => {
  it('opens the link or create form a sign-in came back for, nothing else', () => {
    expect(openedForm('?open=link')).toBe('link');
    expect(openedForm('?pc=x&open=create')).toBe('create');
    expect(openedForm('?open=connect')).toBeNull();
    expect(openedForm('')).toBeNull();
  });
});
