import { readServerResponse } from '../../testing/files';
import {
  ACTION_FAILED,
  ALIAS_BANNED,
  ALIAS_BUSY,
  BAD_CREDENTIALS,
  blockedReason,
  INVALID_ALIAS_NAME,
  LINKED_ELSEWHERE,
  NAME_TAKEN,
  NO_PC_ACCOUNT,
  NOT_LINKED,
  pageLine,
  PC_CANCELLED,
  PC_CONNECTED,
  PC_FAILED,
  PC_LINKED_ELSEWHERE,
  PcAction,
  pcNotice,
  refusalCode,
  refusalMessage,
  signedInAs,
  SLOW_DOWN,
  UNAVAILABLE,
} from './pc-account-messages';

const ERRORS = JSON.parse(readServerResponse('argonauth-errors.json')) as Record<PcAction, [number, string][]>;

describe('the PC account refusals', () => {
  it("words every error of PC-ACCOUNT.md's tables but reauth, which is no message (a sign-in instead)", () => {
    const worded = Object.entries(ERRORS).flatMap(([action, errors]) =>
      errors
        .filter(([, code]) => code !== 'reauth')
        .map(([status, code]) => refusalMessage(refusalCode(action as PcAction, status, code))),
    );
    expect(worded).not.toContain(ACTION_FAILED);
    expect(new Set(worded)).toEqual(
      new Set([
        NO_PC_ACCOUNT,
        NOT_LINKED,
        ALIAS_BANNED,
        ALIAS_BUSY,
        UNAVAILABLE,
        INVALID_ALIAS_NAME,
        NAME_TAKEN,
        SLOW_DOWN,
        BAD_CREDENTIALS,
        LINKED_ELSEWHERE,
      ]),
    );
  });

  it("tells the codes that share a status apart by the server's code", () => {
    expect(refusalCode('enter', 403, 'banned')).toBe('banned');
    expect(refusalCode('enter', 403, 'not-linked')).toBe('not-linked');
    expect(refusalCode('create', 409, 'busy')).toBe('busy');
    expect(refusalCode('create', 409, 'name-taken')).toBe('name-taken');
  });

  it('takes the likeliest code for an answer without one, and none for a status the table has not', () => {
    expect(refusalCode('enter', 409, null)).toBe('busy');
    expect(refusalCode('create', 409, null)).toBe('name-taken');
    expect(refusalCode('link', 401, 'whatever')).toBe('bad-credentials');
    expect(refusalCode('enter', 0, null)).toBeNull();
    expect(refusalCode('link', 500, null)).toBeNull();
    expect(refusalMessage(null)).toBe(ACTION_FAILED);
    expect(refusalMessage('reauth')).toBe(ACTION_FAILED);
  });
});

describe('pcNotice', () => {
  it("gives the login page's lines for a cancelled or failed sign-in", () => {
    expect(pcNotice('?pc=cancelled', 'login')).toBe(PC_CANCELLED);
    expect(pcNotice('?pc=failed', 'login')).toBe(PC_FAILED);
    expect(pcNotice('?pc=connected', 'login')).toBeNull();
    expect(pcNotice('', 'login')).toBeNull();
  });

  it("gives My Account's lines for a Connect", () => {
    expect(pcNotice('?pc=connected', 'account')).toBe(PC_CONNECTED);
    expect(pcNotice('?pc=linked-elsewhere', 'account')).toBe(PC_LINKED_ELSEWHERE);
    expect(pcNotice('?pc=failed', 'account')).toBe(PC_FAILED);
    expect(pcNotice('?pc=cancelled', 'account')).toBe(PC_CANCELLED);
    expect(pcNotice('?pc=toString', 'account')).toBeNull();
    expect(pcNotice('', 'account')).toBeNull();
  });
});

describe('the alias list wording', () => {
  it('names the account, the busy alias and the page', () => {
    expect(signedInAs('Frodo Baggins')).toBe('Signed in as Frodo Baggins');
    expect(blockedReason('Underhill')).toBe('Underhill is still playing: only it can be entered.');
    expect(pageLine(2, 3)).toBe('Page 2 of 3');
  });
});
