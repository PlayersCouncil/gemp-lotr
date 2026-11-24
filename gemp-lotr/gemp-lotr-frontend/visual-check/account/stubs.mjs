// A stand-in for the server's PC account addresses (gemp-lotr/PC-ACCOUNT.md, "The Server's Addresses"), for
// account/run.mjs: one per scenario, starting from a saved session of src/testing/server-responses (the files the
// unit tests read), answering the actions as the document says and remembering every POST it was sent. Everything
// else (the hall, the deck builder) is answered by hall/stubs.mjs and stub-server.mjs.
import fs from 'node:fs';
import path from 'node:path';
import { hallAnswers } from '../hall/stubs.mjs';
import { RESPONSES_DIR } from '../stub-server.mjs';

export const read = (name) => JSON.parse(fs.readFileSync(path.join(RESPONSES_DIR, name), 'utf8'));

/** A PC account with `count` aliases (Alias1, Alias2, ...), none played (as src/testing/pc-sessions.ts). */
export function sessionWithAliases(count) {
  return {
    ...read('argonauth-session-aliases.json'),
    aliases: Array.from({ length: count }, (_, i) => ({
      id: 900001 + i,
      name: `Alias${i + 1}`,
      current: false,
      busy: false,
    })),
  };
}

/** What the server says to a refused action. */
const refusal = (status, error) => ({ status, text: JSON.stringify({ error }), type: 'application/json' });

/**
 * The PC account's server for one scenario. `session`: a saved session's file name, or a session. `refuse`: a
 * refusal per action (`{ create: [409, 'name-taken'] }`), each given once, the next call answered as usual. `linked`:
 * the alias a link adds. `nextAll`: where Log Out from the alias list sends the browser.
 */
export class FakePcServer {
  constructor({ session, refuse = {}, linked = { id: 900099, name: 'OldSam', current: false, busy: false } } = {}) {
    this.session = typeof session === 'string' ? read(session) : session;
    this.refuse = { ...refuse };
    this.linked = linked;
    /** Every POST, as `{ path, body }` with the body form-decoded. */
    this.posts = [];
  }

  /** The answer function for stub-server.mjs's routeToStub: these addresses first, then the hall's. */
  answers(hallStubs = {}) {
    const hall = hallAnswers(hallStubs);
    return (url, request) => {
      const p = url.pathname.replace('/gemp-lotr-server', '');
      if (p.startsWith('/argonauth/')) return this.answer(p.slice('/argonauth'.length), url, request);
      return hall(url, request);
    };
  }

  answer(p, url, request) {
    if (p === '/session') return { json: this.session };
    // The pages the browser is sent to (a PC sign-in): a page that says where it is, to be looked at by the scenario
    if (p === '/login') return { text: `<!doctype html><title>PC sign-in</title>${url.search}`, type: 'text/html' };
    if (request.method() !== 'POST') return { status: 404 };
    const body = Object.fromEntries(new URLSearchParams(request.postData() ?? ''));
    this.posts.push({ path: p, body });
    const action = p.slice(1);
    const refused = this.refuse[action];
    if (refused) {
      delete this.refuse[action];
      // An enter refused not-linked: the server has corrected the list (PC-ACCOUNT.md)
      if (action === 'enter' && refused[1] === 'not-linked') {
        this.session = { ...this.session, aliases: this.session.aliases.filter((a) => String(a.id) !== body.playerId) };
      }
      return refusal(...refused);
    }
    if (action === 'enter') {
      const alias = this.session.aliases.find((a) => String(a.id) === body.playerId);
      if (!alias) return refusal(403, 'not-linked');
      return { json: this.play(alias.name) };
    }
    if (action === 'create') {
      if (!/^[A-Za-z0-9_-]{2,30}$/.test(body.name ?? '')) return refusal(400, 'invalid-name');
      if (this.session.aliases.some((a) => a.name.toLowerCase() === body.name.toLowerCase())) {
        return refusal(409, 'name-taken');
      }
      const id = 900000 + this.session.aliases.length + 50;
      this.session = {
        ...this.session,
        aliases: [...this.session.aliases, { id, name: body.name, current: false, busy: false }],
      };
      return { json: this.play(body.name) };
    }
    if (action === 'link') {
      this.session = { ...this.session, aliases: [...this.session.aliases, this.linked] };
      return { json: this.session };
    }
    if (action === 'connect') return { json: read('argonauth-connect.json') };
    if (action === 'logout') {
      const all = body.scope === 'all';
      this.session = { ...this.session, player: null, linked: null, ...(all ? { pcAccount: null, aliases: [] } : {}) };
      return { json: read(all ? 'argonauth-logout-all.json' : 'argonauth-logout-alias.json') };
    }
    return { status: 404 };
  }

  /** The session playing `name`. */
  play(name) {
    this.session = {
      ...this.session,
      player: name,
      linked: true,
      aliases: this.session.aliases.map((a) => ({ ...a, current: a.name === name })),
    };
    return this.session;
  }
}
