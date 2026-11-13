// The game server's protocol as the check uses it from Node: reading its XML, starting a game between a pair of
// players through the hall, and the opponent, who plays over HTTP by a fixed policy (see README.md).
import { session } from '../local-server/seed.mjs';
import { DECKS } from './players.mjs';

const ENTITIES = { amp: '&', lt: '<', gt: '>', quot: '"', apos: "'" };
const unescape = (text) =>
  text.replace(/&(#x?[0-9a-f]+|\w+);/gi, (whole, name) => {
    if (name[0] === '#') {
      return String.fromCodePoint(
        name[1] === 'x' || name[1] === 'X' ? parseInt(name.slice(2), 16) : Number(name.slice(1)),
      );
    }
    return ENTITIES[name] ?? whole;
  });

function attributes(text) {
  return Object.fromEntries(
    [...text.matchAll(/([\w-]+)="([^"]*)"/g)].map(([, name, value]) => [name, unescape(value)]),
  );
}

/**
 * A `gameState` / `update` answer of /game: its channel number, its events in order (each with its attributes and its
 * `parameter` children) and the clocks.
 */
export function parseGameXml(xml) {
  const root = xml.match(/<(gameState|update)\b([^>]*)>/);
  const events = [];
  for (const match of xml.matchAll(/<ge\b([^>]*?)(\/>|>([\s\S]*?)<\/ge>)/g)) {
    const params = [...(match[3] ?? '').matchAll(/<parameter\b([^>]*)\/>/g)].map(([, text]) => attributes(text));
    events.push({ ...attributes(match[1]), params });
  }
  const error = xml.match(/<error\b([^>]*)/);
  return {
    cn: root ? (attributes(root[2]).cn ?? null) : null,
    events,
    error: error ? attributes(error[1]) : null,
  };
}

/** A decision event as an object: its id, type, text and parameters by name (each a list, in order). */
export function readDecision(event) {
  const values = {};
  for (const { name, value } of event.params) {
    (values[name] ??= []).push(value);
  }
  return { id: event.id, type: event.decisionType, text: event.text ?? '', values };
}

/** The decision waiting for `participant` in these events, or null if a newer event ended it. */
export function pendingDecision(events, participant) {
  let pending = null;
  for (const event of events) {
    if (event.type === 'D' && event.participantId === participant) {
      pending = readDecision(event);
    } else if (event.type === 'EG') {
      pending = null;
    }
  }
  return pending;
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Starts a game of the test deck between the pair: the player makes the table, the opponent joins it. Ends any game
 * either of them still plays first. Returns the game's id and both sessions.
 */
/** `deckKey`: which of the test decks (players.mjs DECKS) both play. */
export async function startGame(server, pair, password, deckKey = 'main') {
  const deck = DECKS[deckKey];
  if (!deck) throw new Error(`no test deck "${deckKey}"`);
  // One login at a time (see "One login for all pages" in ../README.md)
  const player = await session(server, pair.player, password);
  const foe = await session(server, pair.foe, password);
  await clearTables(player, pair.player);
  await clearTables(foe, pair.foe);
  const created = await (
    await player.post('/hall', {
      format: deck.format,
      deckName: deck.name,
      deckSource: 'player',
      timer: 'default',
      desc: '',
      isPrivate: 'false',
      isInviteOnly: 'false',
    })
  ).text();
  if (!created.includes('OK')) {
    throw new Error(`${pair.player} could not make a table: ${created.slice(0, 300)}`);
  }
  const tableId = (await tablesOf(foe)).find((table) => table.players === pair.player && !table.gameId)?.id;
  if (!tableId) {
    throw new Error(`The table of ${pair.player} is not in the hall`);
  }
  const joined = await (await foe.post(`/hall/${tableId}`, { deckName: deck.name, deckSource: 'player' })).text();
  if (!joined.includes('OK')) {
    throw new Error(`${pair.foe} could not join: ${joined.slice(0, 300)}`);
  }
  for (let attempt = 0; attempt < 50; attempt++) {
    const tables = await tablesOf(player);
    const game = tables.find((table) => table.id === tableId && table.gameId);
    if (game) {
      await untilStarted(player, game.gameId);
      return { gameId: game.gameId, player, foe };
    }
    await sleep(200);
  }
  throw new Error(`No game started for ${pair.player}`);
}

async function tablesOf(who) {
  const hall = await (await who.get('/hall')).text();
  return [...hall.matchAll(/<table\b([^>]*)>/g)].map(([, text]) => attributes(text));
}

/**
 * The hall shows the game's id a moment before the game can be read: until then its state answers an error (seen: 500).
 * Neither the page nor the bots may ask before.
 */
async function untilStarted(player, gameId) {
  let last = '';
  for (let attempt = 0; attempt < 50; attempt++) {
    try {
      await player.get(`/game/${gameId}`);
      return;
    } catch (error) {
      last = error.message;
      await sleep(200);
    }
  }
  throw new Error(`game ${gameId} did not start: ${last}`);
}

/** A run that stopped half way can leave a table waiting or a game going: the player leaves or concedes them. */
async function clearTables(who, name) {
  for (const table of await tablesOf(who)) {
    if (!table.players?.split(',').includes(name) || table.status === 'FINISHED') continue;
    if (table.gameId) {
      await who.post(`/game/${table.gameId}/concede`, {}).catch(() => undefined);
    } else {
      await who.post(`/hall/${table.id}/leave`, {}).catch(() => undefined);
    }
  }
}

/**
 * The replay of a finished game: its `replayId` for game.html, `<player>$<recording id>`, from the player's game
 * history (the newest game first).
 */
export async function replayIdOf(game, player) {
  for (let attempt = 0; attempt < 50; attempt++) {
    const history = await (await game.player.get('/gameHistory?start=0&count=5')).text();
    const recording = history.match(/gameRecordingId="([^"]+)"/)?.[1];
    if (recording) return `${player}$${recording}`;
    await sleep(200);
  }
  throw new Error(`no replay in the game history of ${player}`);
}

/** Ends the game for both: the player concedes (a finished game answers 409/410, which is fine). */
export async function endGame(game) {
  await game.player.post(`/game/${game.gameId}/concede`, {}).catch(() => undefined);
}

/**
 * The opponent's answer to a decision. Fixed, so both runs of a scenario get the same game: bid 0, the first choice,
 * the fewest cards, pass. A scenario's `foe` rules come first: `{ when: /text/, answer: (decision) => value }`.
 */
export function foeAnswer(decision, rules = []) {
  for (const rule of rules) {
    if (rule.when.test(decision.text)) {
      const value = rule.answer(decision);
      if (value !== undefined) {
        return value;
      }
    }
  }
  const values = decision.values;
  const min = Number(values.min?.[0] ?? 0);
  switch (decision.type) {
    case 'INTEGER':
      return values.defaultValue?.[0] ?? String(min);
    case 'MULTIPLE_CHOICE':
      return '0';
    case 'CARD_SELECTION':
    case 'ARBITRARY_CARDS':
      return (values.cardId ?? []).slice(0, min).join(',');
    case 'ASSIGN_MINIONS':
      return '';
    case 'ACTION_CHOICE':
      // Required responses: one must be chosen
      return values.actionId?.[0] ?? '';
    default:
      // CARD_ACTION_CHOICE and ACTION_CHOICE: pass
      return '';
  }
}

/**
 * The opponent: reads its side of the game and answers each decision by `foeAnswer`, until stopped. `idle()` resolves
 * once it has nothing to answer and no answer on the way, so a step ends with the opponent done too.
 */
export class Foe {
  /**
   * `mayAnswer()` says whether to answer now. The check lets the player in the browser answer first: when both are
   * asked at once (bids, starting fellowships, mulligans), the server's order depends on who answered first.
   */
  /**
   * `session`: whose side it plays (the opponent's by default). `budget`: answers shared by the bots of a game that both
   * sides play over HTTP (spectator and replay scenarios), `{ left: n }`: once none are left, it answers no more, so the
   * game stops at the same point every time.
   */
  constructor(game, participant, rules = [], mayAnswer = () => true, { session = game.foe, budget = null } = {}) {
    this.game = game;
    this.participant = participant;
    this.rules = rules;
    this.mayAnswer = mayAnswer;
    this.session = session;
    this.budget = budget;
    /** Whether a decision waits for it. */
    this.waiting = false;
    /**
     * Resolves once its first request (it joins the game) is answered. The check lets players join one at a time,
     * so the joins come in the same order on every run.
     */
    this.ready = new Promise((resolve) => (this.joined = resolve));
    this.stopped = false;
    this.busy = true;
    this.answers = [];
    this.problems = [];
    this.loop = this.run();
  }

  async request(fields) {
    const url = `/game/${this.game.gameId}`;
    const response = fields ? await this.session.post(url, fields) : await this.session.get(url);
    return parseGameXml(await response.text());
  }

  async run() {
    try {
      let answer;
      try {
        answer = await this.request(null);
      } finally {
        this.joined();
      }
      let cn = answer.cn;
      let events = answer.events;
      while (!this.stopped) {
        if (events.some((event) => event.type === 'EG')) {
          break;
        }
        const decision = pendingDecision(events, this.participant);
        this.waiting = Boolean(decision);
        if (decision && this.budget && this.budget.left <= 0) {
          // Out of answers: it waits here for good
          this.busy = false;
          await sleep(100);
          continue;
        }
        if (decision && !this.mayAnswer()) {
          this.busy = false;
          await sleep(100);
          continue;
        }
        if (decision) {
          this.busy = true;
          const value = foeAnswer(decision, this.rules);
          if (this.budget) this.budget.left--;
          this.answers.push(`${decision.text} => ${value}`);
          this.log?.(`  ${this.participant} answers "${decision.text}" (${decision.type}) = ${value}`);
          answer = await this.request({ channelNumber: cn, decisionId: decision.id, decisionValue: value });
        } else {
          this.busy = false;
          // The server holds this until something happens (long poll)
          answer = await this.request({ channelNumber: cn });
        }
        if (answer.error) {
          this.problems.push(`the opponent's request failed: ${JSON.stringify(answer.error)}`);
          break;
        }
        cn = answer.cn ?? cn;
        events = answer.events;
      }
    } catch (error) {
      if (!this.stopped) {
        this.problems.push(`the opponent stopped: ${error.message}`);
      }
    }
    this.busy = false;
  }

  async idle(quiet = 300, max = 20000) {
    const start = Date.now();
    let calmSince = Date.now();
    while (Date.now() - start < max) {
      if (this.busy) {
        calmSince = Date.now();
      } else if (Date.now() - calmSince >= quiet) {
        return;
      }
      await sleep(50);
    }
  }

  stop() {
    this.stopped = true;
  }
}
