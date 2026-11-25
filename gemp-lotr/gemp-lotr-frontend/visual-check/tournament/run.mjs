#!/usr/bin/env node
// The tournament check: the basic life of a tournament, on a real server. The admin's side goes through the
// Tournament Admin page in a browser (creating a scheduled tournament, removing a player, setting a stage); the
// players only talk to the server over HTTP. See README.md, and tournament/local.mjs to run it on a throwaway server.
//
//   node tournament/run.mjs --server http://127.0.0.1:17301        (password in GEMP_TOURNAMENT_PASSWORD)
//
// Three scenarios, each a list of checks; a failed check is reported and the scenario stops there, the others go on:
//   sealed-queue      four players fill a player-made sealed queue and confirm its ready check: every confirmation
//                     is answered (the last one used to answer 500) and the tournament starts and hands out product
//   sealed-scheduled  a scheduled sealed tournament with manual kickoff: an uninvited player is taken out of its
//                     queue and cannot come back, it starts at its start time and is marked started, a late
//                     uninvited player is dropped and cannot come back
//   kickoff           a scheduled constructed tournament with manual kickoff waits at Awaiting Kickoff, the admin
//                     sets Preparing, a queue waiting elsewhere survives that, and round 1 is paired
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { DECKS } from '../game/players.mjs';
import { session } from '../local-server/seed.mjs';
import { ADMIN, GUESTS, LATE_CRASHER, QUEUE_CRASHER, SEALED_PLAYERS, WAITER } from './players.mjs';

const { values: options } = parseArgs({
  options: { server: { type: 'string' }, out: { type: 'string', default: 'report-tournament' } },
});
const password = process.env.GEMP_TOURNAMENT_PASSWORD;
if (!options.server || !password) {
  console.error('Usage: GEMP_TOURNAMENT_PASSWORD=... node tournament/run.mjs --server <url> [--out <folder>]');
  process.exit(2);
}
const SERVER = options.server;
const OUT = path.resolve(options.out);
fs.mkdirSync(OUT, { recursive: true });

// ---- small tools -----------------------------------------------------------------------------------------------

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function expect(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

/** Asks `probe` once a second until it answers something truthy, which is returned. */
async function until(what, timeoutMs, probe) {
  const deadline = Date.now() + timeoutMs;
  let last;
  for (;;) {
    try {
      const answer = await probe();
      if (answer) {
        return answer;
      }
      last = 'not yet';
    } catch (error) {
      last = error.message;
    }
    if (Date.now() > deadline) {
      throw new Error(`Gave up after ${Math.round(timeoutMs / 1000)}s waiting for ${what} (last: ${last})`);
    }
    await sleep(1000);
  }
}

const unescapeXml = (text) =>
  text
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .replaceAll('&quot;', '"')
    .replaceAll('&apos;', "'")
    .replaceAll('&amp;', '&');

/** The attributes of every `<name ...>` element of an XML text. */
function elements(xml, name) {
  return [...xml.matchAll(new RegExp(`<${name}\\b([^>]*)>`, 'g'))].map((match) =>
    Object.fromEntries([...match[1].matchAll(/([\w-]+)="([^"]*)"/g)].map((attr) => [attr[1], unescapeXml(attr[2])])),
  );
}

/** The hall as the server shows it to this player now: its queues, tournaments and tables. */
async function hallOf(player) {
  const xml = await (await player.get('/hall')).text();
  return { queues: elements(xml, 'queue'), tournaments: elements(xml, 'tournament'), tables: elements(xml, 'table') };
}

/** What the server answered a hall request: the refusal it gave the player, or null when it did as asked. */
async function refusalOf(response) {
  const error = elements(await response.text(), 'error')[0];
  return error ? (error.message ?? 'refused') : null;
}

// ---- the people ------------------------------------------------------------------------------------------------

const players = {};
async function as(name) {
  // One login at a time (see "One login for all pages" in ../README.md)
  players[name] ??= await session(SERVER, name, password);
  return players[name];
}

const adminView = async (id) =>
  (await (await as(ADMIN)).get(`/admin/tournament?tournamentId=${encodeURIComponent(id)}`)).json();
const adminPlayers = async (id) =>
  (await (await (await as(ADMIN)).get(`/admin/tournamentPlayers?tournamentId=${encodeURIComponent(id)}`)).json())
    .players;

const joinQueue = async (name, queueId, deckName = '') =>
  refusalOf(await (await as(name)).post(`/hall/queue/${encodeURIComponent(queueId)}`, { deckName }));
const joinLate = async (name, tournamentId, deckName = '') =>
  refusalOf(await (await as(name)).post(`/hall/tournament/${encodeURIComponent(tournamentId)}/join`, { deckName }));

// ---- the admin's browser ---------------------------------------------------------------------------------------

let browser;
let page;

/** The Tournament Admin page, logged in as the admin, with its format lists loaded. */
async function openAdminPage() {
  browser = await launchBrowser();
  const context = await browser.newContext({ viewport: { width: 1366, height: 900 } });
  const admin = await as(ADMIN);
  await context.addCookies(
    admin.cookie.split('; ').map((pairText) => {
      const [name, ...value] = pairText.split('=');
      return { name, value: value.join('='), domain: new URL(SERVER).hostname, path: '/' };
    }),
  );
  page = await context.newPage();
  // "Drop X from the tournament?" and the like: the admin says yes
  page.on('dialog', (dialog) => void dialog.accept());
  // the legacy hall, open for its Admin tab alone: hall.html itself now forwards to the Angular hall
  await page.goto(`${SERVER}/gemp-lotr/hall-legacy.html?admin=1`);
  await page.locator('a[href="includes/admin.html"]').click();
  await page.locator('#tournamentTab').click();
  await page.locator('#tourney-save-button').waitFor({ state: 'visible', timeout: 30000 });
  await page.waitForFunction(
    () =>
      document.querySelectorAll('#tourney-format option').length > 0 &&
      document.querySelectorAll('#tourney-sealed-format option').length > 0,
    null,
    { timeout: 30000 },
  );
}

/** Waits until a result box of the page says something matching `pattern`; any other final text is the failure. */
async function resultSays(selector, pattern) {
  const box = page.locator(selector);
  try {
    await page.waitForFunction(
      ([sel, source, flags]) => new RegExp(source, flags).test(document.querySelector(sel)?.textContent ?? ''),
      [selector, pattern.source, pattern.flags],
      { timeout: 15000 },
    );
  } catch {
    throw new Error(`${selector} says "${(await box.textContent())?.trim()}", expected ${pattern}`);
  }
}

/** UTC, as the page's start field takes it. */
const startField = (date) => date.toISOString().slice(0, 16);

async function createScheduled({ name, id, type, format, start }) {
  await page.locator('#tourney-new-button').click();
  await page.locator(`.tourney-type-btn[data-type="${type}"]`).click();
  await page.locator('#tourney-name').fill(name);
  await page.locator('#tourney-id').fill(id);
  if (type === 'SEALED') {
    // long enough that the check is over before anybody is disqualified for not registering a deck
    await page.locator('#tourney-deckbuild').fill('30');
    await page.locator('#tourney-registration').fill('5');
  } else {
    await page.locator('#tourney-format').selectOption(format);
  }
  await page.locator('#tourney-start').fill(startField(start));
  await page.locator('#tourney-min-players').fill('2');
  await page.locator('#tourney-kickoff').setChecked(true);
  await page.locator('#tourney-save-button').click();
  await resultSays('#tourney-result', /scheduled/);
}

/** Opens a tournament on the page by its id, as an admin does from "Open by id". */
async function openTournament(id) {
  await page.locator('#open-tourney-id').fill(id);
  await page.locator('#open-tourney-button').click();
  await resultSays('#tourney-result', /^Loaded /);
}

/** Presses Remove on a player's row of the Players section. */
async function removePlayerOnPage(name, expected) {
  const row = page.locator('#players-list .managed-player', { hasText: name });
  await row.waitFor({ state: 'visible', timeout: 15000 });
  await row.locator('button.player-remove').click();
  await resultSays('#players-result', expected);
}

// ---- running the checks ----------------------------------------------------------------------------------------

const results = [];

/** One scenario: its checks in order, stopping at the first that fails. */
async function scenario(name, steps) {
  console.log(`\n${name}`);
  for (const [what, step] of steps) {
    try {
      await step();
      results.push({ scenario: name, check: what, ok: true });
      console.log(`  ✓ ${what}`);
    } catch (error) {
      results.push({ scenario: name, check: what, ok: false, problem: error.message });
      console.log(`  ✗ ${what}\n      ${error.message}`);
      if (page) {
        const shot = path.join(OUT, `${name}-${results.length}.png`);
        await page.screenshot({ path: shot, fullPage: true }).catch(() => {});
        console.log(`      (the admin page at that moment: ${shot})`);
      }
      console.log('      (the rest of this scenario is skipped)');
      return;
    }
  }
}

const stamp = Date.now().toString(36);
const SEALED_ID = `tcheck-sealed-${stamp}`;
const KICKOFF_ID = `tcheck-kickoff-${stamp}`;
// Both scheduled tournaments start at the same minute, at least 90 seconds from now: time to sign up and to take
// the uninvited player out of the queue first
const START = new Date(Math.ceil((Date.now() + 90_000) / 60_000) * 60_000);
const untilStart = () => Math.max(0, START.getTime() - Date.now());
const DECK = DECKS.main;

let sealedQueueId;
let waiterQueueId;

try {
  await openAdminPage();

  // The two scheduled tournaments are made first, so that their start time passes while the rest runs
  await scenario('scheduling', [
    [
      'the admin page schedules a sealed tournament with manual kickoff',
      async () => {
        await createScheduled({ name: `Check sealed ${stamp}`, id: SEALED_ID, type: 'SEALED', start: START });
        const view = await adminView(SEALED_ID);
        expect(view.status === 'scheduled', `its status is ${view.status}, expected scheduled`);
        expect(view.params?.manualKickoff === true, 'it was not stored with manual kickoff');
      },
    ],
    [
      'the admin page schedules a constructed tournament with manual kickoff',
      async () => {
        await createScheduled({
          name: `Check kickoff ${stamp}`,
          id: KICKOFF_ID,
          type: 'CONSTRUCTED',
          format: DECK.format,
          start: START,
        });
        const view = await adminView(KICKOFF_ID);
        expect(view.status === 'scheduled', `its status is ${view.status}, expected scheduled`);
        expect(view.params?.manualKickoff === true, 'it was not stored with manual kickoff');
      },
    ],
    [
      'the invited players sign up for both, and an uninvited one for the sealed tournament',
      async () => {
        for (const guest of GUESTS) {
          const sealed = await joinQueue(guest, SEALED_ID);
          expect(sealed === null, `${guest} was refused by the sealed tournament: ${sealed}`);
          const kickoff = await joinQueue(guest, KICKOFF_ID, DECK.name);
          expect(kickoff === null, `${guest} was refused by the constructed tournament: ${kickoff}`);
        }
        const crasher = await joinQueue(QUEUE_CRASHER, SEALED_ID);
        expect(crasher === null, `${QUEUE_CRASHER} was refused: ${crasher}`);
        expect((await adminView(SEALED_ID)).signedUp === 3, 'the sealed tournament does not have 3 players signed up');
        expect((await adminView(KICKOFF_ID)).signedUp === 2, 'the constructed one does not have 2 players signed up');
      },
    ],
    [
      'the admin page takes the uninvited player out of the queue',
      async () => {
        await openTournament(SEALED_ID);
        await removePlayerOnPage(QUEUE_CRASHER, /was removed from the queue/);
        const waiting = (await adminPlayers(SEALED_ID)).map((player) => player.name).sort();
        expect(waiting.join() === [...GUESTS].sort().join(), `signed up now: ${waiting.join(', ')}`);
      },
    ],
    [
      'who cannot sign up for it again',
      async () => {
        const refusal = await joinQueue(QUEUE_CRASHER, SEALED_ID);
        expect(refusal?.includes('administrator removed you'), `the server answered: ${refusal ?? 'signed up'}`);
        expect((await adminView(SEALED_ID)).signedUp === 2, 'the queue does not have 2 players');
      },
    ],
  ]);

  await scenario('sealed-queue', [
    [
      'a player makes a sealed tournament for four, with a ready check',
      async () => {
        const maker = await as(SEALED_PLAYERS[0]);
        const formats = await (await maker.get('/tournament/tournamentFormats')).json();
        expect(formats.sealed?.length > 0, 'the server offers no sealed format');
        await maker.post('/tournament/create', {
          type: 'sealed',
          formatCode: formats.sealed[0].code,
          deckbuildingDuration: '30',
          playoff: 'swiss',
          maxPlayers: '4',
          competitive: 'false',
          startable: 'false',
          readyCheck: '120',
          deckName: '',
        });
        const queue = (await hallOf(await as(ADMIN))).queues.find(
          (entry) => entry.type === 'SEALED' && entry.playerList === SEALED_PLAYERS[0],
        );
        expect(queue, 'the hall does not show its queue');
        sealedQueueId = queue.id;
      },
    ],
    [
      'three more join, and the ready check starts',
      async () => {
        for (const name of SEALED_PLAYERS.slice(1)) {
          const refusal = await joinQueue(name, sealedQueueId);
          expect(refusal === null, `${name} was refused: ${refusal}`);
        }
        await until('the ready check', 20_000, async () => {
          const queue = (await hallOf(await as(ADMIN))).queues.find((entry) => entry.id === sealedQueueId);
          return queue && Number(queue.readyCheckSecsRemaining) > 0;
        });
      },
    ],
    [
      'every player confirms, the last one included, and each is answered',
      async () => {
        for (const name of SEALED_PLAYERS) {
          // throws on anything but 200: the last confirmation starts the tournament, and used to answer 500
          await (await as(name)).post(`/hall/queue/${encodeURIComponent(sealedQueueId)}/ready`, {});
        }
      },
    ],
    [
      'the tournament runs with the four of them, building decks',
      async () => {
        const view = await until('the tournament to build decks', 20_000, async () => {
          const now = await adminView(sealedQueueId);
          return now.status === 'live' && now.stage === 'DECK_BUILDING' ? now : null;
        });
        expect(view.playerCount === 4, `it has ${view.playerCount} players, expected 4`);
        const queueLeft = (await hallOf(await as(ADMIN))).queues.some((entry) => entry.id === sealedQueueId);
        expect(!queueLeft, 'its queue is still in the hall');
      },
    ],
  ]);

  await scenario('waiting-queue', [
    [
      'another player waits alone in a queue of their own',
      async () => {
        await (
          await as(WAITER)
        ).post('/tournament/create', {
          type: 'constructed',
          formatCode: DECK.format,
          deckbuildingDuration: '',
          playoff: 'swiss',
          maxPlayers: '4',
          competitive: 'false',
          startable: 'false',
          readyCheck: '0',
          deckName: DECK.name,
        });
        const queue = (await hallOf(await as(ADMIN))).queues.find((entry) => entry.playerList === WAITER);
        expect(queue, 'the hall does not show the queue');
        waiterQueueId = queue.id;
      },
    ],
  ]);

  console.log(`\n(waiting ${Math.round(untilStart() / 1000)}s for the scheduled tournaments' start time)`);

  await scenario('sealed-scheduled', [
    [
      'at its start time the sealed tournament starts, with the two invited players',
      async () => {
        const view = await until('the sealed tournament to start', untilStart() + 30_000, async () => {
          const now = await adminView(SEALED_ID);
          return now.status === 'live' ? now : null;
        });
        expect(view.playerCount === 2, `it has ${view.playerCount} players, expected 2`);
        expect(view.params?.manualKickoff === true, 'the started tournament lost its manual kickoff');
        expect(
          ['STARTING', 'DECK_BUILDING'].includes(view.stage),
          `it is at ${view.stage}, expected Starting or Deck-building`,
        );
      },
    ],
    [
      'the player taken out of its queue cannot join it late',
      async () => {
        const refusal = await joinLate(QUEUE_CRASHER, SEALED_ID);
        expect(refusal !== null, `${QUEUE_CRASHER} joined the tournament`);
      },
    ],
    [
      'another uninvited player joins it late',
      async () => {
        const refusal = await joinLate(LATE_CRASHER, SEALED_ID);
        expect(refusal === null, `${LATE_CRASHER} was refused: ${refusal}`);
        expect((await adminView(SEALED_ID)).playerCount === 3, 'the tournament does not have 3 players');
      },
    ],
    [
      'the admin page drops that player',
      async () => {
        await openTournament(SEALED_ID);
        await removePlayerOnPage(LATE_CRASHER, /was dropped from/);
        const dropped = (await adminPlayers(SEALED_ID)).filter((player) => player.dropped).map((player) => player.name);
        expect(dropped.join() === LATE_CRASHER, `dropped now: ${dropped.join(', ') || 'nobody'}`);
      },
    ],
    [
      'who cannot join it again',
      async () => {
        const refusal = await joinLate(LATE_CRASHER, SEALED_ID);
        expect(refusal !== null, `${LATE_CRASHER} joined the tournament again`);
      },
    ],
  ]);

  await scenario('kickoff', [
    [
      'at its start time the constructed tournament starts and waits for its kickoff',
      async () => {
        const view = await until('the constructed tournament to start', untilStart() + 30_000, async () => {
          const now = await adminView(KICKOFF_ID);
          return now.status === 'live' ? now : null;
        });
        expect(view.stage === 'AWAITING_KICKOFF', `it is at ${view.stage}, expected Awaiting Kickoff`);
        expect(view.playerCount === 2, `it has ${view.playerCount} players, expected 2`);
        // and stays there: nothing pairs a round before the kickoff
        await sleep(5000);
        const later = await adminView(KICKOFF_ID);
        expect(
          later.stage === 'AWAITING_KICKOFF' && later.round === 0,
          `5s later it is at ${later.stage}, round ${later.round}`,
        );
      },
    ],
    [
      'the admin page sets its stage to Preparing',
      async () => {
        await openTournament(KICKOFF_ID);
        await page.locator('#stage-select').selectOption('PREPARING');
        await page.locator('#stage-button').click();
        await resultSays('#stage-result', /Stage set to/);
      },
    ],
    [
      'which leaves the queue waiting elsewhere, and the other running tournaments, alone',
      async () => {
        expect(waiterQueueId, 'the waiting queue was never made (see waiting-queue above)');
        const queue = (await hallOf(await as(ADMIN))).queues.find((entry) => entry.id === waiterQueueId);
        expect(queue, 'the waiting queue is gone from the hall');
        expect(queue.playerCount === '1', `the waiting queue has ${queue.playerCount} players, expected 1`);
        const sealed = await adminView(SEALED_ID);
        expect(sealed.status === 'live', `the scheduled sealed tournament is now ${sealed.status}`);
      },
    ],
    [
      'the tournament plays, and round 1 is paired after the pairing delay',
      async () => {
        const view = await until('round 1', 120_000, async () => {
          const now = await adminView(KICKOFF_ID);
          return now.round === 1 ? now : null;
        });
        expect(view.stage === 'PLAYING_GAMES', `it is at ${view.stage}, expected Playing Games`);
        const table = await until('the table of round 1', 15_000, async () =>
          (await hallOf(await as(ADMIN))).tables.find((entry) =>
            GUESTS.every((guest) => entry.players?.includes(guest)),
          ),
        );
        expect(table, 'the hall shows no table with the two players');
      },
    ],
  ]);
} catch (error) {
  results.push({ scenario: 'setup', check: 'the check itself', ok: false, problem: error.message });
  console.error(`\nThe check stopped: ${error.message}`);
} finally {
  await browser?.close();
}

const failed = results.filter((result) => !result.ok);
fs.writeFileSync(path.join(OUT, 'result.json'), JSON.stringify(results, null, 2));
console.log(
  `\n${results.length - failed.length} of ${results.length} checks passed (${path.join(OUT, 'result.json')}).`,
);
process.exitCode = failed.length ? 1 : 0;
