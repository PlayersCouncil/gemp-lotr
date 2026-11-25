// What cards do to each other, mechanic by mechanic, in the modern theme (the owner, 2026-11-20: "have you investigated
// all kinds of interaction the cards can make, like hindering and some new mechanisms? Are they part of tests?"): each
// mechanic the server can send is opened as a prepared state of a recorded game (game/phone.mjs STATES), on a phone
// and a tablet upright (the phone layout), a computer and a low phone held sideways (the computer layout), and what
// the page shows is checked: hindered cards lie face down with their numbers hidden, turned up they show them again
// once the stats name them, tokens are drawn on their cards, a moved card lies under its new bearer, a controlled site
// in its controller's support, threats and the initiative in the status, a revealed hand opens with its cards; and,
// on a computer, a support card dragged sideways with the mouse moves in its row as in game.html, and stays there after
// a reload (the owner, 2026-11-23).
//
//   node game/mechanics.mjs [--only hindered,tokens,...] [--devices phone,tablet,computer,phone-sideways]
//                           [--out report-game-mechanics]
//
// It also checks that the list is whole, from the server's own code: every attribute of a game event and of the
// game's stats (EventSerializer.java) is a mechanic here, shown by another check, or not shown by game.html either
// (each with its reason), so a new mechanism the server starts sending fails the check until it is looked at; and
// every token kind (Token.java) that a card can put on the table has a picture (images/tokens).
//
// The screenshots of each mechanic on each device are in the report, for looking at.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { tap } from '../touch.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, '../../..');
const LOGIC = path.join(ROOT, 'gemp-lotr-logic/src/main/java/com/gempukku/lotro');
const TOKEN_IMAGES = path.join(ROOT, 'gemp-lotr-async/src/main/web/images/tokens');
const CARDS = path.join(ROOT, 'gemp-lotr-cards/src/main/resources/cards');

const { values: options } = parseArgs({
  options: {
    only: { type: 'string' },
    devices: { type: 'string', default: 'phone,tablet,computer,phone-sideways' },
    out: { type: 'string', default: 'report-game-mechanics' },
  },
});

// phone.mjs reads its own options from the command line: it is given none of these
const argv = process.argv;
process.argv = argv.slice(0, 2);
const { openPhone, mechanicState } = await import('./phone.mjs');
process.argv = argv;

const OUT = path.resolve(HERE, '..', options.out);

/** The devices, by the layout each gets: the phone layout upright, the computer layout else (CompactLayout). */
const SIZES = {
  phone: null,
  tablet: null,
  computer: null,
  /** A phone held sideways: the computer layout on a low screen (the owner, 2026-11-20). */
  'phone-sideways': { width: 844, height: 390 },
};

// --- What the page shows of the cards ---

/** Each card as the page shows it (the first copy of it that isn't an opened one), by its id. */
function readCards() {
  const cards = {};
  for (const el of document.querySelectorAll('[data-card-id]:not([data-opened])')) {
    const id = el.dataset.cardId;
    if (cards[id]) continue;
    const rect = el.getBoundingClientRect();
    cards[id] = {
      zone: el.dataset.zone,
      hindered: 'hindered' in el.dataset,
      image: el.querySelector('img:not(.gd-game-token)')?.getAttribute('src') ?? null,
      // The other pictures over the card (not tokens): an overlay, e.g. a meta-site's modifier
      overlays: [...el.querySelectorAll('img:not(.gd-game-token)')].slice(1).map((img) => img.getAttribute('src')),
      numbers: el.querySelectorAll('[data-number]').length,
      tokens: [...el.querySelectorAll('img.gd-game-token')].map((img) =>
        img.getAttribute('src').replace(/^.*\/tokens\/|\.png$/g, ''),
      ),
      box: { x: rect.x, y: rect.y, width: rect.width, height: rect.height },
      shown: rect.width > 0 && rect.height > 0,
    };
  }
  return cards;
}

/** The status the page shows: threats and initiative, in the phone layout's or the computer layout's place. */
function readStatus() {
  const text = (selector) => document.querySelector(selector)?.textContent.replace(/\s+/g, ' ').trim() ?? null;
  const phone = !!document.querySelector('.gd-phone-player');
  return phone
    ? {
        layout: 'phone',
        threats: text('.gd-phone-player:first-child [data-count="threats"]'),
        initiative: text('[data-pill="initiative"]'),
      }
    : { layout: 'computer', threats: text('[data-stat="threats0"]'), initiative: text('[data-game="initiative"]') };
}

const count = (list, item) => list.filter((x) => x === item).length;
const centre = (box) => ({ x: box.x + box.width / 2, y: box.y + box.height / 2 });
const distance = (a, b) => Math.hypot(centre(a).x - centre(b).x, centre(a).y - centre(b).y);

/** Problems with the cards: each `want` (id → what it must be) against what the page shows. */
function cardProblems(cards, want) {
  const problems = [];
  for (const [id, expected] of Object.entries(want)) {
    const card = cards[id];
    if (!card) {
      problems.push(`card ${id} isn't on the page`);
      continue;
    }
    for (const [key, value] of Object.entries(expected)) {
      const actual = key === 'numbers' ? card.numbers > 0 : card[key];
      if (actual !== value)
        problems.push(`card ${id}: ${key} ${JSON.stringify(actual)}, wanted ${JSON.stringify(value)}`);
    }
  }
  return problems;
}

// --- The mechanics ---

/** The cards of a row, left to right: the ids given, as the page places them (by their left edge). */
const rowOrder = (cards, ids) =>
  ids
    .filter((id) => cards[id]?.shown)
    .sort((a, b) => cards[a].box.x - cards[b].box.x)
    .join(' ');

const SUPPORT_ROW = ['50', '9501', '9502', '9503'];

/**
 * Each mechanic: the prepared state that shows it (phone.mjs STATES), what is done first (`open`, by layout, then
 * `act`, given the page and its cards), and what must be true of it (`expect`, given what the page shows, `start`: its
 * cards before `act`, `reloaded`: its cards after a reload that follows `act`, and, with `baseline`, what it showed
 * before the change). `devices`: only these devices (a mouse gesture: the computer).
 */
export const MECHANICS = [
  {
    name: 'hindered',
    what: 'cards hindered (turned face down): a companion, a minion, a possession in support, an attached card',
    baseline: 'late',
    expect: ({ cards, before }) => [
      ...cardProblems(cards, {
        47: { hindered: true, numbers: false },
        56: { hindered: true, numbers: false },
        50: { hindered: true },
        44: { hindered: true },
        48: { hindered: false, numbers: true },
      }),
      ...['47', '56', '50', '44']
        .filter((id) => cards[id] && cards[id].image === before[id]?.image)
        .map((id) => `card ${id}: still shows its face (${cards[id].image}), not its back`),
    ],
  },
  {
    name: 'restored',
    what: 'hindered cards turned up again: numbers hidden until the stats name the card again (as game.html)',
    baseline: 'late',
    expect: ({ cards, before }) => [
      ...cardProblems(cards, {
        47: { hindered: false, numbers: false },
        56: { hindered: false, numbers: true },
      }),
      ...['47', '56']
        .filter((id) => cards[id] && cards[id].image !== before[id]?.image)
        .map((id) => `card ${id}: shows ${cards[id].image}, not its face ${before[id]?.image}`),
    ],
  },
  {
    name: 'tokens',
    what: 'tokens: wounds, burdens, culture tokens on a possession and a condition, as many as put there',
    expect: ({ cards }) =>
      [
        ['47', 'wound', 2],
        ['43', 'burden', 3],
        ['50', 'elven', 2],
        ['53', 'sauron', 4],
      ]
        .filter(([id, token, n]) => count(cards[id]?.tokens ?? [], token) < n)
        .map(
          ([id, token, n]) =>
            `card ${id}: ${count(cards[id]?.tokens ?? [], token)} ${token} tokens drawn, wanted at least ${n}`,
        ),
  },
  {
    name: 'moved',
    what: "an attached card moved to another character (a possession transferred): Gimli's axe onto Sam",
    expect: ({ cards }) => {
      const [axe, gimli, sam] = [cards[49], cards[47], cards[48]];
      if (!axe || !gimli || !sam) return ["the axe 49, Gimli 47 or Sam 48 isn't on the page"];
      return distance(axe.box, sam.box) < distance(axe.box, gimli.box)
        ? []
        : ['the axe 49 still lies by Gimli 47, not by Sam 48'];
    },
  },
  {
    name: 'siteControlled',
    what: "a site the opponent controls: off the path, in the opponent's support with its other cards",
    expect: ({ cards }) => {
      const [site, support] = [cards[42], cards[52]];
      if (!site || !support) return ["the site 42 or the opponent's support card 52 isn't on the page"];
      const problems = cardProblems(cards, { 42: { zone: 'SUPPORT', shown: true } });
      if (Math.abs(site.box.y - support.box.y) > 4) {
        problems.push(
          `the site 42 isn't in the opponent's support row (top ${site.box.y}, the row's ${support.box.y})`,
        );
      }
      return problems;
    },
  },
  {
    name: 'threats',
    what: 'threats on the player (2 of 3) and the Shadow holding the initiative',
    expect: ({ status }) => {
      const problems = [];
      if (!/^2\b|\b2\s*\/\s*3\b/.test(status.threats ?? '')) problems.push(`threats show "${status.threats}"`);
      const shadow = status.layout === 'phone' ? /\bSh\b/ : /Shadow/;
      if (!shadow.test(status.initiative ?? '')) problems.push(`the initiative shows "${status.initiative}"`);
      return problems;
    },
  },
  {
    name: 'revealedHand',
    what: "the opponent's hand revealed: its cards in the opponent's piles (the chip on a phone, the hand count else)",
    open: { phone: '.gd-phone-player:nth-child(2)', computer: '[data-pile="revealedHand"]' },
    expect: ({ cards }) =>
      ['9301', '9302', '9303']
        .filter((id) => !cards[id]?.shown)
        .map((id) => `the revealed card ${id} isn't shown once the piles are opened`),
  },
  {
    name: 'supportDrag',
    state: 'supportRow',
    what:
      'a support card dragged sideways with the mouse, two card widths to the right: it moves two places in its row, ' +
      'as in game.html (gameUi.js dragContinuesCardFunction), and keeps its place after a reload',
    devices: ['computer'],
    act: async (page, cards) => {
      const box = cards[50]?.box;
      if (!box) return;
      const start = { x: box.x + box.width / 2, y: box.y + box.height / 2 };
      await page.mouse.move(start.x, start.y);
      await page.mouse.down();
      for (let step = 1; step <= 12; step++) {
        await page.mouse.move(start.x + ((box.width * 2 + 10) * step) / 12, start.y);
      }
      await page.mouse.up();
    },
    reload: true,
    expect: ({ cards, start, reloaded }) => {
      const want = { start: '50 9501 9502 9503', moved: '9501 9502 50 9503' };
      const problems = [];
      if (rowOrder(start, SUPPORT_ROW) !== want.start)
        problems.push(`before the drag the row is "${rowOrder(start, SUPPORT_ROW)}", wanted "${want.start}"`);
      if (rowOrder(cards, SUPPORT_ROW) !== want.moved)
        problems.push(`after the drag the row is "${rowOrder(cards, SUPPORT_ROW)}", wanted "${want.moved}"`);
      if (rowOrder(reloaded, SUPPORT_ROW) !== want.moved)
        problems.push(`after a reload the row is "${rowOrder(reloaded, SUPPORT_ROW)}", wanted "${want.moved}"`);
      return problems;
    },
  },
  {
    name: 'metaSite',
    what:
      "an RTMD meta-site in support: game.html draws the visual card's picture with the modifier's bottom over it " +
      '(Card.js CreateSplitCardDiv, MetaSiteOverlayHeight 27%)',
    baseline: 'metaSitePlain',
    expect: ({ cards, before }) => {
      const [card, plain] = [cards[9401], before[9401]];
      if (!card || !plain) return ["the meta-site card 9401 isn't on the page"];
      const problems = [];
      if (card.image === plain.image)
        problems.push(`it shows the modifier card's own picture (${card.image}), not the visual card's`);
      if (!card.overlays.includes(plain.image)) problems.push("the modifier card's picture isn't laid over it");
      return problems;
    },
  },
];

/** What the page shows of a mechanic's state on a device: its cards and status, after opening what it opens. */
async function look(browser, device, stateName, { open, act, reload } = {}) {
  const state = mechanicState(stateName);
  const shown = await openPhone(browser, SIZES[device] ? 'phone' : device, state, state.count, {
    viewport: SIZES[device],
  });
  try {
    await shown.settle();
    const layout = (await shown.page.locator('.gd-phone-player').count()) ? 'phone' : 'computer';
    if (open?.[layout]) {
      const target = shown.page.locator(open[layout]).first();
      await target.scrollIntoViewIfNeeded();
      if (layout === 'phone') await tap(shown.page, target);
      else await target.click();
      await shown.settle();
    }
    const start = await shown.page.evaluate(readCards);
    if (act) {
      await act(shown.page, start);
      await shown.settle();
    }
    const cards = await shown.page.evaluate(readCards);
    const screen = await shown.page.screenshot();
    let reloaded = {};
    if (reload) {
      await shown.reload();
      reloaded = await shown.page.evaluate(readCards);
    }
    return {
      start,
      cards,
      reloaded,
      status: await shown.page.evaluate(readStatus),
      screen,
      errors: shown.errors,
    };
  } finally {
    await shown.close();
  }
}

// --- The list is whole ---

/** Every attribute a game event and the game's stats can carry (EventSerializer.java), as "event x" / "stats x". */
function serverFields() {
  const text = fs.readFileSync(path.join(LOGIC, 'game/state/EventSerializer.java'), 'utf8');
  const fields = new Set();
  let section = 'event';
  for (const line of text.split('\n')) {
    if (/void serializeGameStats\(/.test(line)) section = 'stats';
    else if (/void serialize\w+\(/.test(line))
      section = /serializeDecision|serializeDecks|PreGame/i.test(line) ? 'other' : 'event';
    if (section === 'other') continue;
    for (const match of line.matchAll(/(\w+)\.setAttribute\("(\w+)"/g)) {
      const element = match[1];
      const where = element === 'eventElem' ? section : `${section} ${element.replace(/Elem$/, '')}`;
      fields.add(`${where} ${match[2]}`);
    }
  }
  return fields;
}

/**
 * Every field the server sends, with the mechanic of this check that shows it, or why it isn't one here. A field the
 * server starts sending that isn't in this list fails the check: a new mechanism, to be looked at.
 */
export const FIELDS = {
  'event hindered': 'hindered, restored',
  'event controllerId': 'siteControlled',
  'event targetType': 'moved (attached); stacked cards: game/phone.mjs underOffer and its stack check',
  'event targetCardId': 'moved',
  'event token': 'tokens',
  'event count': 'tokens (and the twilight pool, in every recording)',
  'stats hindered':
    'hindered (the server lists the hindered cards; the pages go by the cards turned over, as game.html)',
  'stats charStats': 'restored (numbers back once the stats name the card)',
  'stats initiative': 'threats',
  'stats threats value': 'threats',
  'stats threatTotals value': 'threats',
  'stats playerZones handRevealed': 'revealedHand',
  // Shown by the other checks
  'stats fellowshipArchery': 'game/phone.mjs archery check; the game check (recordings)',
  'stats shadowArchery': 'game/phone.mjs archery check; the game check (recordings)',
  'stats fellowshipStrength': 'the skirmish: game/phone.mjs skirmish states; the game check (recordings)',
  'stats shadowStrength': 'the skirmish: game/phone.mjs skirmish states; the game check (recordings)',
  'stats fellowshipDamageBonus': 'the skirmish: the game check (recordings)',
  'stats shadowDamageBonus': 'the skirmish: the game check (recordings)',
  'stats fpOverwhelmed': 'the shadow-overwhelmed recording (game check, phone check)',
  'stats shadowOverwhelmed': 'the shadow-overwhelmed recording (game check, phone check)',
  'stats moveLimit': 'the moves, in every recording',
  'stats moveCount': 'the moves, in every recording',
  'stats ruleof4': 'the Rule of 4 count, in the recordings of the fellowship phase',
  'stats playerZones name': 'every player, in every recording',
  'stats threats name': 'every player, in every recording',
  'stats threatTotals name': 'every player, in every recording',
  'stats wearingRing': 'not shown: game.html shows nothing for it, nor does the Angular page (it reads it only)',
  // What every event carries
  'event type': 'every event',
  'event timestamp': 'every event',
  'event blueprintId': 'every card',
  'event cardId': 'every card',
  'event index': 'the players’ seats (PP), in every recording',
  'event participantId': 'every card and player',
  'event allParticipantIds': 'the players (P), in every recording',
  'event phase': 'the phase, in every recording',
  'event zone': 'every card; every zone: game/coverage.mjs',
  'event otherCardIds': 'removed cards, skirmishes, turns: the game check (recordings)',
  'event message': 'the chat and warnings, in every recording',
  'event version': 'the game.html version check: the game check',
  'event discardPublic': 'game/phone.mjs opponentPilesPublic, game/discard-formats.mjs',
  'event metaSites': 'metaSite',
  'event maps': "the pre-game panel's maps (PC Movie): the map card lies in support as any card (the start recording)",
  'event summary': 'the pre-game panel, the phone info sheet: the game check (start recording)',
  'event notes': "the pre-game panel's deck notes: the game check (start recording)",
};

/** The cultures whose tokens a card can add (AddCultureTokens with its culture, in the cards' definitions). */
function culturesTokensAdded() {
  const cultures = new Set();
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const file = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(file);
      else if (entry.name.endsWith('.hjson')) {
        const text = fs.readFileSync(file, 'utf8');
        for (const match of text.matchAll(/type:\s*AddCultureTokens[\s\S]{0,200}?culture:\s*(\w+)/gi)) {
          cultures.add(match[1].toUpperCase().replace(/-/g, '_'));
        }
      }
    }
  };
  walk(CARDS);
  return cultures;
}

/** Every token kind (Token.java), whether images/tokens has its picture and whether a card can put it on the table. */
function tokenKinds() {
  const text = fs.readFileSync(
    path.join(ROOT, 'gemp-lotr-common/src/main/java/com/gempukku/lotro/common/Token.java'),
    'utf8',
  );
  const body = text
    .match(/enum Token \{([\s\S]*?);/)[1]
    .replace(/\([^)]*\)/g, '')
    .replace(/\/\/.*$/gm, '');
  const added = culturesTokensAdded();
  return body
    .split(',')
    .map((name) => name.trim())
    .filter(Boolean)
    .map((token) => ({
      token,
      picture: fs.existsSync(path.join(TOKEN_IMAGES, `${token.toLowerCase()}.png`)),
      used: token === 'BURDEN' || token === 'WOUND' || added.has(token),
    }));
}

/** Gaps in the list: server fields this check doesn't know, and tokens a card can add that have no picture. */
export function coverageGaps() {
  const gaps = [];
  for (const field of serverFields()) {
    if (!FIELDS[field]) gaps.push(`${field}: sent by the server (EventSerializer.java), not in this check's list`);
  }
  for (const { token, picture, used } of tokenKinds()) {
    if (!picture && used) gaps.push(`token ${token}: a card adds it, but images/tokens has no picture for it`);
  }
  return gaps;
}

// --- The run ---

async function main() {
  const only = options.only?.split(',');
  const mechanics = MECHANICS.filter((mechanic) => !only || only.includes(mechanic.name));
  const devices = options.devices.split(',');
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await launchBrowser();
  const checks = [];
  try {
    for (const mechanic of mechanics) {
      for (const device of devices.filter((one) => !mechanic.devices || mechanic.devices.includes(one))) {
        const name = `${mechanic.name}-${device}`;
        const now = await look(browser, device, mechanic.state ?? mechanic.name, mechanic);
        const before = mechanic.baseline ? (await look(browser, device, mechanic.baseline)).cards : {};
        const problems = [...now.errors.map((error) => `page error: ${error}`), ...mechanic.expect({ ...now, before })];
        fs.writeFileSync(path.join(OUT, `${name}.png`), now.screen);
        checks.push({ name, only: mechanic.name, problems });
        console.log(`${problems.length ? '✗' : 'ok'}  ${name}${problems.length ? `: ${problems.join(' | ')}` : ''}`);
      }
    }
  } finally {
    await browser.close();
  }
  const gaps = only ? [] : coverageGaps();
  for (const gap of gaps) console.log(`GAP  ${gap}`);
  if (!only) {
    for (const { token } of tokenKinds().filter((kind) => !kind.picture && !kind.used)) {
      console.log(`note token ${token}: no picture in images/tokens, and no card adds it yet`);
    }
  }
  writeReport(checks, gaps, mechanics);
  fs.writeFileSync(path.join(OUT, 'results.json'), JSON.stringify({ checks, gaps }));
  const failed = checks.filter((check) => check.problems.length).length;
  console.log(
    `\n${checks.length} checks, ${failed} wrong, ${gaps.length} not covered. Report: ${path.join(OUT, 'index.html')}`,
  );
  process.exitCode = failed || gaps.length ? 1 : 0;
}

function writeReport(checks, gaps, mechanics) {
  const escape = (text) => String(text).replace(/[&<>]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[c]);
  const sections = mechanics
    .map((mechanic) => {
      const shots = checks
        .filter((check) => check.only === mechanic.name)
        .map(
          (check) =>
            `<figure><img src="${check.name}.png" loading="lazy"><figcaption>${escape(check.name)} ${
              check.problems.length ? `✗ ${escape(check.problems.join('; '))}` : 'ok'
            }</figcaption></figure>`,
        )
        .join('');
      return `<h2>${escape(mechanic.name)}</h2><p>${escape(mechanic.what)}</p><div class="shots">${shots}</div>`;
    })
    .join('');
  fs.writeFileSync(
    path.join(OUT, 'index.html'),
    `<!doctype html><meta charset="utf-8"><title>Card mechanics</title><style>
body{font:14px system-ui;margin:16px;background:#fff;color:#111}.shots{display:flex;flex-wrap:wrap;gap:12px}
figure{margin:0}img{max-height:420px;border:1px solid #ccc}figcaption{max-width:420px}</style>
<h1>Card mechanics in the modern theme</h1>
${gaps.length ? `<h2>Not covered</h2><ul>${gaps.map((gap) => `<li>${escape(gap)}</li>`).join('')}</ul>` : ''}
${sections}`,
  );
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}
