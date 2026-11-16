// The game's phone layout (G7 of trymarz/gemp-lotr-private#27): the Angular game page in the modern theme on a phone,
// a phone held sideways and an upright tablet, on the recorded games served without a server (stub-game.mjs).
//
//   node game/phone.mjs [--only <recording,...>] [--devices phone,sideways,tablet] [--out report-game-phone]
//                       [--shots: only screenshots of each recording's last step, nothing checked]
//                       [--checks back,reload,touch]
//
// The checks:
// - back: a long press on a card in every place a card's details open from (the table's rows, attached copies, the
//   hand, the path, cards offered in the hand's place, the assignments, the skirmish, the piles, the menu's piles, the
//   details of an attached card, the hand hidden; on each device), then closing them with ✕, Escape and the backdrop:
//   the view must be what it was (the open sheets, where everything is scrolled, the divider), as the owner asked
//   (2026-11-16: closing details returns to the view they were opened from).
// - reload: the divider's place, the hidden hand, open sheets and the scrolled table and rows come back after a reload
//   (D10).
// - touch: a tap acts only in a decision (an offered card is chosen, another does nothing), the divider is dragged and
//   kept per device, dragged to the bottom it hides the hand.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { PNG } from 'pngjs';
import { compareScreens } from '../compare.mjs';
import { longPress, PHONE_CONTEXT, tap, touchDrag } from '../phone.mjs';
import { routeToStub, STUB_ORIGIN } from '../stub-server.mjs';
import { GAME_ID, routeGame, settled } from './stub-game.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const RECORDINGS = path.resolve(HERE, '../../src/testing/game-recordings');

const { values: options } = parseArgs({
  options: {
    only: { type: 'string' },
    devices: { type: 'string', default: 'phone,sideways,tablet' },
    out: { type: 'string', default: 'report-game-phone' },
    shots: { type: 'boolean', default: false },
    checks: { type: 'string', default: 'back,reload,touch,piles,animation' },
    // The step of each recording to open (its number of exchanges); the last by default
    step: { type: 'string' },
  },
});

/** The devices: a phone upright, the same phone sideways, an upright tablet (all under COMPACT_QUERY). */
export const DEVICES = {
  phone: { viewport: { width: 390, height: 844 } },
  sideways: { viewport: { width: 844, height: 390 } },
  tablet: { viewport: { width: 768, height: 1024 } },
};

const OUT = path.resolve(HERE, '..', options.out);

function recordings() {
  const only = options.only?.split(',');
  return fs
    .readdirSync(RECORDINGS)
    .filter((file) => file.endsWith('.json'))
    .map((file) => recording(file.replace(/\.json$/, '')))
    .filter((recording) => !only || only.includes(recording.name));
}

/** Opens the Angular page on a device on the game as it stood after `count` exchanges, in the modern theme. */
export async function openPhone(browser, device, recording, count, { cookies = [] } = {}) {
  const context = await browser.newContext({ ...PHONE_CONTEXT, ...DEVICES[device] });
  const log = [];
  const errors = [];
  await routeToStub(context, log);
  let waiting = await routeGame(context, recording, count, log);
  await context.addCookies(
    [['animation-speed', '4'], ['gempGameTheme', 'modern'], ...cookies].map(([name, value]) => ({
      name,
      value,
      domain: new URL(STUB_ORIGIN).hostname,
      path: '/gemp-lotr',
    })),
  );
  const page = await context.newPage();
  page.on('pageerror', (error) => errors.push(String(error)));
  page.on('console', (message) => {
    if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) errors.push(message.text());
  });
  await page.goto(`${STUB_ORIGIN}/gemp-lotr/ng/game?gameId=${GAME_ID}`);
  return {
    page,
    context,
    errors,
    settle: () => settled(page, () => waiting()),
    /**
     * The page loaded again. As the server does, it gets the whole game as it stands in one answer (with the decision
     * waiting), not the recording's answers one by one again.
     */
    async reload() {
      await context.unroute(`${STUB_ORIGIN}/gemp-lotr-server/**`);
      const whole = asOneState(recording, count);
      waiting = await routeGame(context, whole, whole.exchanges.length, log);
      await page.reload();
      await settled(page, () => waiting());
    },
    close: () => context.close(),
  };
}

// --- Reading the view ---

/** What the player sees of the phone layout, as plain data: what two moments must share to be "the same view". */
function readView() {
  const all = (selector, root = document) => [...root.querySelectorAll(selector)];
  const left = (element) => Math.round(element?.scrollLeft ?? 0);
  const body = document.querySelector('.gd-phone-sheet__body');
  const rowKey = (cards, i) =>
    `${cards.closest('[data-zone]')?.getAttribute('data-zone') ?? 'hand'}:${cards.dataset.row ?? i}`;
  return {
    sheets: all('.gd-phone-sheet').map(
      (sheet) =>
        `${sheet.dataset.sheet}/${sheet.dataset.depth ?? ''}/${sheet.querySelector('h2')?.textContent?.trim()}`,
    ),
    sheetTop: body ? Math.round(body.scrollTop) : null,
    sheetRows: body ? all('.gd-phone-cards__scroll', body).map(left) : [],
    tableTop: Math.round(document.querySelector('gd-phone-table')?.scrollTop ?? -1),
    rows: Object.fromEntries(
      all('gd-phone-table gd-phone-cards, gd-phone-hand gd-phone-cards').map((cards, i) => [
        rowKey(cards, i),
        left(cards.querySelector('.gd-phone-cards__scroll')),
      ]),
    ),
    path: left(document.querySelector('.gd-phone-path')),
    pairs: left(document.querySelector('.gd-phone-pairs')),
    hand: document.querySelector('gd-phone-hand')?.getAttribute('data-place') ?? 'hidden',
    divider: Math.round(document.querySelector('gd-phone-divider')?.getBoundingClientRect().top ?? -1),
    marks: all('.gd-card[data-mark]').map((card) => `${card.dataset.cardId}:${card.dataset.mark}`),
  };
}

async function view(shown) {
  await shown.settle();
  // Pictures drawn again (a reload) are first shrunk roughly and smoothed a moment later: wait for that
  await shown.page.evaluate(() =>
    Promise.all(Array.from(document.images, (img) => img.decode().catch(() => undefined))),
  );
  await shown.page.waitForTimeout(300);
  return {
    data: await shown.page.evaluate(readView),
    // The clocks tick: not compared
    screen: await shown.page.screenshot({
      animations: 'disabled',
      caret: 'hide',
      mask: [shown.page.locator('[data-clock]')],
      maskColor: '#ff00ff',
    }),
  };
}

/**
 * Pixels that may differ between two screenshots of the same view: the edges of a few boxes are blended a shade
 * differently after a sheet was drawn over them (seen: 4 pixels, 1 to 6 shades).
 */
const RENDERING_NOISE = 10;
/**
 * After a reload every box is drawn anew: the anti-aliased edges of rounded borders, rings and shadows (the current
 * player's chip, the site's ring, the markers) land up to ~25 shades off in a few dozen pixels. The table check's
 * tolerance; what D10 asks for (sheets, scroll, divider, hand) is compared exactly in the data.
 */
const RELOAD_NOISE = 60;

/** The differences between two views: their data, and their pixels beyond `tolerance`. */
function differences(before, after, tolerance = RENDERING_NOISE) {
  const found = [];
  for (const key of Object.keys(before.data)) {
    const [a, b] = [JSON.stringify(before.data[key]), JSON.stringify(after.data[key])];
    if (a !== b) found.push(`${key}: ${a} → ${b}`);
  }
  const { outside } = compareScreens(before.screen, after.screen, [], false);
  if (outside > tolerance) found.push(`${outside} pixels differ in ${differingBox(before.screen, after.screen)}`);
  return found;
}

/** Where the pixels that differ are: the box around them, "x0-x1 × y0-y1". */
function differingBox(a, b) {
  const [pa, pb] = [PNG.sync.read(a), PNG.sync.read(b)];
  let [x0, y0, x1, y1] = [Infinity, Infinity, -1, -1];
  for (let i = 0; i < pa.data.length; i += 4) {
    const d = Math.max(...[0, 1, 2].map((c) => Math.abs(pa.data[i + c] - pb.data[i + c])));
    if (d <= 2) continue;
    const x = (i / 4) % pa.width;
    const y = Math.floor(i / 4 / pa.width);
    [x0, y0, x1, y1] = [Math.min(x0, x), Math.min(y0, y), Math.max(x1, x), Math.max(y1, y)];
  }
  return `${x0}-${x1} × ${y0}-${y1}`;
}

/** The pixels that differ, in red over the view before (for looking into a difference). */
function writeDiff(file, before, after) {
  const { image } = compareScreens(before.screen, after.screen, [], true);
  if (image) fs.writeFileSync(file, image);
}

// --- The states of the game the checks start from ---

/** Steps of the recorded games, by what they show (see the recordings' decisions). */
const STATES = {
  /** The starting fellowship chosen from the deck: cards offered in the hand's place, the path, attached cards. */
  choices: { recording: 'effects-skirmish', count: 4 },
  /** Minions assigned, the assignments as pairs; cards in every row. */
  assign: { recording: 'effects-skirmish', count: 14 },
  /** The skirmish being fought. */
  skirmish: { recording: 'effects-skirmish', count: 15 },
  /** Late in the game: piles with cards, cards attached, a choice of cards on the table. */
  late: { recording: 'effects-skirmish', count: 25 },
  /**
   * As late, with cards in every pile of the opponent (no recorded game put any there): put there by the last answer,
   * as the server does. The discard pile and the adventure deck too, which the server sends an opponent only in a
   * format with a public discard pile (the discard) or never (the adventure deck): the page must still not show them.
   * The format of the recorded games (Debug) keeps the discard private.
   */
  opponentPiles: { recording: 'effects-skirmish', count: 25, events: opponentCards },
  /** As opponentPiles, in a format whose discard pile is public (discardPublic, e.g. pc_fotr_block, pc_expanded). */
  opponentPilesPublic: { recording: 'effects-skirmish', count: 25, events: opponentCards, discardPublic: true },
};

/** A card of the opponent's in each pile, as the server puts a card somewhere (PCIP). */
function opponentCards(opponent) {
  return [
    ['9001', '1_12', 'DEAD'],
    ['9002', '1_13', 'REMOVED'],
    ['9003', '1_14', 'DISCARD'],
    ['9004', '1_319', 'ADVENTURE_DECK'],
  ]
    .map(
      ([cardId, blueprintId, zone]) =>
        `<ge blueprintId="${blueprintId}" cardId="${cardId}" hindered="false" participantId="${opponent}" ` +
        `timestamp="2026-11-16 00:00:00.0000" type="PCIP" zone="${zone}"/>`,
    )
    .join('');
}

/**
 * The recording a state opens (a copy; the recording isn't changed): its added events in the last answer, and its
 * format's discard pile made public (the P event's discardPublic).
 */
function recordingOf(state) {
  const base = recording(state.recording);
  if (!state.events && !state.discardPublic) return base;
  const publicDiscard = (body) =>
    state.discardPublic
      ? body
          .replace(/(<ge\b[^>]*\btype="P"[^>]*\bdiscardPublic=")false"/, '$1true"')
          .replace(/(<ge\b[^>]*\bdiscardPublic=")false("[^>]*\btype="P")/, '$1true$2')
      : body;
  const answers = base.exchanges
    .slice(0, state.count)
    .map((exchange, i) => (exchange.path === '/game/<id>' && exchange.status === 200 ? i : -1))
    .filter((i) => i >= 0);
  const last = answers.at(-1);
  const opponent = opponentOf(base);
  return {
    ...base,
    exchanges: base.exchanges.map((exchange, i) => {
      if (exchange.path !== '/game/<id>' || typeof exchange.body !== 'string') return exchange;
      const body = publicDiscard(exchange.body);
      return {
        ...exchange,
        body: i === last && state.events ? body.replace(/(<update[^>]*>)/, `$1${state.events(opponent)}`) : body,
      };
    }),
  };
}

/** The other player of a recorded game: the one its events name who isn't the recording's participant. */
function opponentOf(base) {
  for (const exchange of base.exchanges) {
    for (const match of exchange.body?.matchAll?.(/participantId="([^"]+)"/g) ?? []) {
      if (match[1] !== base.participant) return match[1];
    }
  }
  return 'GameFoe0';
}

const loaded = new Map();
function recording(name) {
  if (!loaded.has(name)) {
    loaded.set(name, { name, ...JSON.parse(fs.readFileSync(path.join(RECORDINGS, `${name}.json`))) });
  }
  return loaded.get(name);
}

// --- Gestures ---

const visible = (page, selector) =>
  page.evaluate((selector) => {
    const element = document.querySelector(selector);
    if (!element) return false;
    element.scrollIntoView({ block: 'center', inline: 'center' });
    return true;
  }, selector);

/** The view made less plain before a card is pressed: its row scrolled sideways a little, the table scrolled to it. */
async function prepare(page, selector) {
  await page.evaluate((selector) => {
    const element = document.querySelector(selector);
    const row = element?.closest('.gd-phone-cards__scroll, .gd-phone-path, .gd-phone-pairs');
    if (row && row.scrollWidth > row.clientWidth) row.scrollLeft = Math.min(17, row.scrollWidth - row.clientWidth);
    element?.scrollIntoView({ block: 'center', inline: 'nearest' });
  }, selector);
  await page.waitForTimeout(400);
}

const CLOSE = {
  '✕': async (page) => tap(page, page.locator('[data-phone="sheet-close"]').last()),
  Escape: async (page) => page.keyboard.press('Escape'),
  backdrop: async (page) => {
    const { width } = page.viewportSize();
    await page.touchscreen.tap(width / 2, 6);
  },
};

const OWN_CHIP = '.gd-phone-player:first-child';
const TABLE_CARD = 'gd-phone-table [data-zone="characters-player"] .gd-phone-slot > .gd-card';

/**
 * Where a card's details open from: the state, what is done first (sheets opened), and the card pressed. Each runs on
 * every device and is closed in each of the three ways.
 */
const PLACES = [
  { name: "a table row (the player's characters)", state: 'late', target: TABLE_CARD },
  {
    name: "the opponent's characters",
    state: 'late',
    target: 'gd-phone-table [data-zone="characters-opponent"] .gd-phone-slot > .gd-card',
  },
  {
    name: 'a support row',
    state: 'late',
    target:
      'gd-phone-table [data-zone="support-player"] .gd-phone-slot > .gd-card, gd-phone-table [data-zone="support-opponent"] .gd-phone-slot > .gd-card',
  },
  { name: 'an attached copy', state: 'late', target: 'gd-phone-table .gd-phone-slot__under > .gd-card' },
  { name: 'the hand', state: 'late', target: 'gd-phone-hand .gd-phone-slot > .gd-card' },
  { name: 'the adventure path', state: 'late', target: '.gd-phone-site > .gd-card' },
  {
    name: "cards offered in the hand's place",
    state: 'choices',
    target: 'gd-phone-hand[data-place="choices"] .gd-card',
  },
  { name: 'an assignment', state: 'assign', target: '.gd-phone-pair[data-pair="assignment"] .gd-card' },
  { name: 'the skirmish', state: 'skirmish', target: '.gd-phone-pair[data-pair="skirmish"] .gd-card' },
  {
    name: "the player's piles (chip)",
    state: 'late',
    before: async (page) => tap(page, page.locator(OWN_CHIP)),
    target: '.gd-phone-sheet[data-sheet="piles"] .gd-card',
  },
  {
    name: "the opponent's piles (chip)",
    state: 'opponentPiles',
    before: async (page) => tap(page, page.locator('.gd-phone-player:nth-child(2)')),
    target: '.gd-phone-sheet[data-sheet="piles"] .gd-card',
  },
  {
    name: "the opponent's public discard pile",
    state: 'opponentPilesPublic',
    before: async (page) => tap(page, page.locator('.gd-phone-player:nth-child(2)')),
    target: '.gd-phone-sheet[data-sheet="piles"] [data-pile="discard"] .gd-card',
  },
  {
    name: "the menu's piles",
    state: 'late',
    before: async (page) => {
      await tap(page, page.locator('[data-phone="menu"]'));
      await page.waitForTimeout(200);
      await tap(page, page.locator('[data-menu="own-piles"]'));
    },
    target: '.gd-phone-sheet[data-sheet="piles"] .gd-card',
  },
  {
    name: "an attached card's details",
    state: 'late',
    before: async (page) => {
      const owner = page.locator('gd-phone-table .gd-phone-slot:has(.gd-phone-slot__under) > .gd-card').first();
      await owner.scrollIntoViewIfNeeded();
      await longPress(page, owner);
    },
    target: '.gd-phone-sheet[data-sheet="card"] .gd-card',
  },
  { name: 'the hand hidden', state: 'late', hidden: true, target: TABLE_CARD },
];

/** One place on one device: the view before, a long press, each way of closing: the view after must be the same. */
async function backCase(browser, device, place) {
  const state = STATES[place.state];
  const cookies = place.hidden ? [['gempGameSplit', `${device}:hidden`]] : [];
  const shown = await openPhone(browser, device, recordingOf(state), state.count, { cookies });
  const results = [];
  try {
    await shown.settle();
    if (place.before) {
      await place.before(shown.page);
      await shown.settle();
    }
    const target = shown.page.locator(place.target).first();
    if (!(await target.count())) {
      return [{ close: '-', problems: [`no card at ${place.target}`] }];
    }
    await prepare(shown.page, place.target);
    const before = await view(shown);
    for (const [how, close] of Object.entries(CLOSE)) {
      await longPress(shown.page, shown.page.locator(place.target).first());
      const opened = await view(shown);
      const problems = [];
      const depth = (sheets) => Number(sheets.find((sheet) => !sheet.startsWith('actions/'))?.split('/')[1] || 0);
      if (depth(opened.data.sheets) !== depth(before.data.sheets) + 1 || !opened.data.sheets[0]?.startsWith('card/')) {
        problems.push(`the details didn't open: ${JSON.stringify(opened.data.sheets)}`);
      }
      if (JSON.stringify(opened.data.marks) !== JSON.stringify(before.data.marks)) {
        problems.push('the long press chose a card');
      }
      await close(shown.page);
      const after = await view(shown);
      problems.push(...differences(before, after));
      if (problems.length) {
        fs.mkdirSync(OUT, { recursive: true });
        const base = path.join(OUT, `back-${device}-${slug(place.name)}-${slug(how)}`);
        fs.writeFileSync(`${base}-before.png`, before.screen);
        fs.writeFileSync(`${base}-after.png`, after.screen);
        writeDiff(`${base}-diff.png`, before, after);
      }
      results.push({ close: how, problems: [...problems, ...shown.errors.splice(0)] });
    }
  } finally {
    await shown.close();
  }
  return results;
}

const slug = (text) =>
  text
    .replace(/[^a-z0-9]+/gi, '-')
    .replace(/^-|-$/g, '')
    .toLowerCase() || 'x';

async function backCheck(browser, devices) {
  let same = 0;
  let total = 0;
  for (const device of devices) {
    for (const place of PLACES) {
      for (const result of await backCase(browser, device, place)) {
        total++;
        if (result.problems.length === 0) same++;
        else console.log(`  ✗ back · ${device} · ${place.name} · ${result.close}: ${result.problems.join('; ')}`);
      }
    }
  }
  console.log(`back: ${same} of ${total} the same view`);
  return same === total;
}

// --- Piles ---

/**
 * Who sees what of a pile (the server's rules, Zone and LotroFormat.discardPileIsPublic): the dead pile and the
 * removed pile everyone; the adventure deck its owner; the discard pile its owner, and everyone in a format that makes
 * it public. Each player's piles from their chip, in both kinds of format: which piles show their cards.
 */
const PILE_RULES = [
  {
    state: 'opponentPiles',
    chip: 2,
    expect: { deadPile: true, removedPile: true, discard: false, adventureDeck: false },
  },
  {
    state: 'opponentPilesPublic',
    chip: 2,
    expect: { deadPile: true, removedPile: true, discard: true, adventureDeck: false },
  },
  {
    state: 'opponentPiles',
    chip: 1,
    expect: { deadPile: true, removedPile: true, discard: true, adventureDeck: true },
  },
];

async function pilesCheck(browser, devices) {
  let ok = 0;
  for (const rule of PILE_RULES) {
    const state = STATES[rule.state];
    const shown = await openPhone(browser, devices[0], recordingOf(state), state.count);
    try {
      await shown.settle();
      await tap(shown.page, shown.page.locator(`.gd-phone-player:nth-child(${rule.chip})`));
      await shown.settle();
      const seen = await shown.page.evaluate(() =>
        Object.fromEntries(
          Array.from(document.querySelectorAll('.gd-phone-sheet [data-pile]'), (section) => [
            section.getAttribute('data-pile'),
            section.querySelector('gd-phone-cards') !== null,
          ]),
        ),
      );
      const wrong = Object.entries(rule.expect).filter(([pile, cards]) => seen[pile] !== cards);
      const who = rule.chip === 1 ? 'own' : "opponent's";
      const format = state.discardPublic ? 'public discard' : 'private discard';
      if (wrong.length === 0 && shown.errors.length === 0) ok++;
      else {
        console.log(
          `  ✗ piles · ${who} · ${format}: ${wrong.map(([pile, cards]) => `${pile} should ${cards ? '' : 'not '}show its cards`).join('; ')} ${shown.errors.join(' | ')}`,
        );
      }
    } finally {
      await shown.close();
    }
  }
  console.log(`piles: ${ok} of ${PILE_RULES.length} as the rules allow`);
  return ok === PILE_RULES.length;
}

// --- Animations ---

/**
 * The opponent's cards put into play, at the normal animation speed, in a recorded game played from its start: their
 * picture over everything must stay whole on the screen, and no card in a row may be enlarged where its row clips it
 * (the owner, 2026-11-16: half the card was cut off).
 */
async function animationCheck(browser, devices) {
  let ok = 0;
  for (const device of devices) {
    const state = STATES.late;
    const shown = await openPhone(browser, device, recordingOf(state), state.count, {
      cookies: [['animation-speed', '0']],
    });
    try {
      // Sampled every frame from the start, while the game's answers come one by one
      await shown.page.evaluate(() => {
        const seen = (window.__animations = { shown: 0, outside: [], grown: [] });
        const frame = () => {
          const { innerWidth: width, innerHeight: height } = window;
          for (const image of document.querySelectorAll('.gd-game-animation__card')) {
            const box = image.getBoundingClientRect();
            if (box.width === 0 || Number(getComputedStyle(image).opacity) === 0) continue;
            seen.shown++;
            if (box.left < -1 || box.top < -1 || box.right > width + 1 || box.bottom > height + 1) {
              seen.outside.push(
                `${Math.round(box.left)},${Math.round(box.top)} ${Math.round(box.width)}x${Math.round(box.height)}`,
              );
            }
          }
          for (const card of document.querySelectorAll('gd-phone-table .gd-card, gd-phone-hand .gd-card')) {
            // Grown: drawn bigger than it is (a flip only narrows a card, a stacked one only turns)
            const box = card.getBoundingClientRect();
            if (box.width > card.offsetWidth + 1 || box.height > card.offsetHeight + 1) {
              seen.grown.push(`${card.dataset.cardId} (${getComputedStyle(card).transform})`);
            }
          }
          requestAnimationFrame(frame);
        };
        requestAnimationFrame(frame);
      });
      await shown.settle();
      const seen = await shown.page.evaluate(() => window.__animations);
      const problems = [
        ...(seen.shown === 0 ? ["no opponent's card was shown"] : []),
        ...(seen.outside.length
          ? [`cut off by the screen's edge: ${[...new Set(seen.outside)].slice(0, 3).join('; ')}`]
          : []),
        ...(seen.grown.length ? [`enlarged in its row: ${[...new Set(seen.grown)].join(', ')}`] : []),
        ...shown.errors,
      ];
      if (problems.length === 0) ok++;
      else console.log(`  ✗ animation · ${device}: ${problems.join('; ')}`);
    } finally {
      await shown.close();
    }
  }
  console.log(`animation: ${ok} of ${devices.length} devices show the opponent's cards whole`);
  return ok === devices.length;
}

// --- Reload (D10) ---

/** A view set up step by step, reloaded: it must come back as it was. */
const RELOADS = [
  {
    name: 'divider dragged up, table and a row scrolled',
    state: 'late',
    act: async (page) => {
      await dragDivider(page, -120);
      await page.evaluate(() => {
        const table = document.querySelector('gd-phone-table');
        table.scrollTop = Math.min(140, table.scrollHeight - table.clientHeight);
        const row = document.querySelector('gd-phone-table [data-zone="characters-player"] .gd-phone-cards__scroll');
        if (row) row.scrollLeft = Math.min(60, row.scrollWidth - row.clientWidth);
      });
    },
  },
  { name: 'hand hidden', state: 'late', act: async (page) => dragDivider(page, 2000) },
  {
    name: 'menu, piles and a card open, scrolled',
    state: 'late',
    act: async (page) => {
      await tap(page, page.locator('[data-phone="menu"]'));
      await page.waitForTimeout(200);
      await tap(page, page.locator('[data-menu="own-piles"]'));
      await page.waitForTimeout(300);
      await page.evaluate(() => {
        const body = document.querySelector('.gd-phone-sheet__body');
        body.scrollTop = Math.min(80, body.scrollHeight - body.clientHeight);
      });
      const card = page.locator('.gd-phone-sheet[data-sheet="piles"] .gd-card').first();
      if (await card.count()) await longPress(page, card);
    },
  },
  {
    name: "cards offered in the hand's place closed with ✕",
    state: 'choices',
    act: async (page) => tap(page, page.locator('[data-phone="close-choices"]')),
  },
];

async function dragDivider(page, by) {
  const box = await page.locator('gd-phone-divider').boundingBox();
  const from = { x: box.x + 30, y: box.y + 5 };
  await touchDrag(page, from, { x: from.x, y: Math.max(1, Math.min(page.viewportSize().height - 1, from.y + by)) });
  await page.waitForTimeout(300);
}

async function reloadCheck(browser, devices) {
  let same = 0;
  let total = 0;
  for (const device of devices) {
    for (const reload of RELOADS) {
      total++;
      const state = STATES[reload.state];
      const shown = await openPhone(browser, device, recordingOf(state), state.count);
      try {
        await shown.settle();
        await reload.act(shown.page);
        const before = await view(shown);
        await shown.reload();
        // Every picture is new after a reload: Chrome smooths them a while after drawing them
        await shown.page.waitForTimeout(2000);
        const after = await view(shown);
        const problems = [...differences(before, after, RELOAD_NOISE), ...shown.errors.splice(0)];
        if (problems.length === 0) same++;
        else {
          console.log(`  ✗ reload · ${device} · ${reload.name}: ${problems.join('; ')}`);
          fs.mkdirSync(OUT, { recursive: true });
          const base = path.join(OUT, `reload-${device}-${slug(reload.name)}`);
          fs.writeFileSync(`${base}-before.png`, before.screen);
          fs.writeFileSync(`${base}-after.png`, after.screen);
        }
      } finally {
        await shown.close();
      }
    }
  }
  console.log(`reload: ${same} of ${total} the same after a reload`);
  return same === total;
}

// --- Touch ---

/** What the page sends the server: the answers to decisions. */
function answers(page) {
  const sent = [];
  page.on('request', (request) => {
    const body = request.postData() ?? '';
    if (request.method() === 'POST' && /\/game\//.test(request.url()) && /decisionId=/.test(body)) sent.push(body);
  });
  return sent;
}

const TOUCH = [
  {
    name: 'a tap on a card not offered does nothing',
    state: 'late',
    run: async (page, sent) => {
      const card = page.locator('gd-phone-table .gd-phone-slot > .gd-card:not([data-mark])').first();
      const before = await page.evaluate(readView);
      await tap(page, card);
      await page.waitForTimeout(500);
      const after = await page.evaluate(readView);
      return [
        ...(sent.length ? [`sent ${sent.join(' | ')}`] : []),
        ...(after.sheets.length !== before.sheets.length ? ['a sheet opened'] : []),
      ];
    },
  },
  {
    name: 'a tap on an offered card chooses it',
    state: 'choices',
    run: async (page, sent) => {
      const card = page.locator('gd-phone-hand .gd-card[data-mark="selectable"]').first();
      if (!(await card.count())) return ['no card offered'];
      const id = await card.getAttribute('data-card-id');
      await tap(page, card);
      await page.waitForTimeout(500);
      const mark = await page
        .locator(`.gd-card[data-card-id="${id}"]`)
        .first()
        .getAttribute('data-mark')
        .catch(() => null);
      return mark === 'selected' || sent.length > 0 ? [] : [`card ${id} not chosen (mark ${mark})`];
    },
  },
  {
    name: 'the divider is dragged up and kept per device',
    state: 'late',
    run: async (page, sent, device) => {
      const before = await page.locator('gd-phone-hand').boundingBox();
      await dragDivider(page, -100);
      const after = await page.locator('gd-phone-hand').boundingBox();
      const cookies = await page.context().cookies();
      const split = decodeURIComponent(cookies.find((cookie) => cookie.name === 'gempGameSplit')?.value ?? '');
      const problems = [];
      if (!(after.height > before.height + 50)) problems.push(`hand ${before.height} → ${after.height}`);
      if (!split.includes(`${device}:`)) problems.push(`cookie "${split}"`);
      return problems;
    },
  },
  {
    name: "held sideways, the hand's grip slides it up as a panel in the right half (hidden elsewhere)",
    state: 'late',
    run: async (page, sent, device) => {
      const grip = page.locator('[data-phone="hand-grip"]');
      if (device !== 'sideways') return (await grip.isVisible()) ? ['the grip shows'] : [];
      const before = await page.locator('gd-phone-hand').boundingBox();
      const box = await grip.boundingBox();
      const from = { x: box.x + box.width / 2, y: box.y + box.height / 2 };
      await touchDrag(page, from, { x: from.x, y: from.y - 80 });
      await page.waitForTimeout(300);
      const after = await page.locator('gd-phone-hand').boundingBox();
      const width = page.viewportSize().width;
      const problems = [];
      if (!(after.height > before.height + 50)) problems.push(`hand ${before.height} → ${after.height}`);
      if (Math.abs(after.x - width / 2) > 2 || Math.abs(after.x + after.width - width) > 2) {
        problems.push(`not the right half: ${after.x}–${after.x + after.width} of ${width}`);
      }
      await touchDrag(page, { x: from.x, y: after.y + 8 }, { x: from.x, y: 2 });
      await page.waitForTimeout(300);
      const top = await page.locator('gd-phone-hand').boundingBox();
      const status = await page.locator('gd-phone-status').boundingBox();
      if (Math.abs(top.y - (status.y + status.height)) > 2) problems.push(`slid up to ${top.y}, not to the status bar`);
      return problems;
    },
  },
  {
    name: "the divider (held sideways: the hand's grip) dragged to the bottom hides the hand; dragged up, it is back",
    state: 'late',
    run: async (page, sent, device) => {
      if (device === 'sideways') {
        // The divider is at the bottom already, beside the hand: the hand's own grip slides it down
        const box = await page.locator('[data-phone="hand-grip"]').boundingBox();
        const from = { x: box.x + box.width / 2, y: box.y + box.height / 2 };
        await touchDrag(page, from, { x: from.x, y: page.viewportSize().height - 1 });
        await page.waitForTimeout(300);
      } else {
        await dragDivider(page, 2000);
      }
      const hidden = await page.locator('gd-game-phone[data-hand-hidden]').count();
      const hint = await page.locator('.gd-phone-divider__hand-hint').count();
      await dragDivider(page, -200);
      const back = await page.locator('gd-phone-hand').count();
      return [
        ...(hidden ? [] : ['the hand is still shown']),
        ...(hint ? [] : ['no hint to drag it up']),
        ...(back ? [] : ["dragged up, the hand didn't come back"]),
      ];
    },
  },
];

async function touchCheck(browser, devices) {
  let ok = 0;
  let total = 0;
  for (const device of devices) {
    for (const test of TOUCH) {
      total++;
      const state = STATES[test.state];
      const shown = await openPhone(browser, device, recordingOf(state), state.count);
      try {
        await shown.settle();
        const sent = answers(shown.page);
        const problems = [...(await test.run(shown.page, sent, device)), ...shown.errors.splice(0)];
        if (problems.length === 0) ok++;
        else console.log(`  ✗ touch · ${device} · ${test.name}: ${problems.join('; ')}`);
      } finally {
        await shown.close();
      }
    }
  }
  console.log(`touch: ${ok} of ${total} as expected`);
  return ok === total;
}

/**
 * A recording up to `count` exchanges with the game's answers made one: the opening state with every later answer's
 * events after its own (a decision only from the last, as gameUpTo keeps it), as the server sends a page that opens
 * the game again.
 */
function asOneState(recording, count) {
  const exchanges = recording.exchanges.slice(0, count);
  const games = exchanges.flatMap((exchange, i) =>
    exchange.path === '/game/<id>' && exchange.status === 200 ? [i] : [],
  );
  const last = games.at(-1);
  const withoutDecisions = (body) =>
    body.replace(/<ge\b[^>]*?(?:\/>|>[\s\S]*?<\/ge>)/g, (event) =>
      /\stype="D"/.test(event.slice(0, event.indexOf('>'))) ? '' : event,
    );
  const inner = (body) =>
    body.replace(/^[\s\S]*?<(?:gameState|update)\b[^>]*>/, '').replace(/<\/(?:gameState|update)>\s*$/, '');
  const [first, ...rest] = games;
  const events = rest.map((i) => (i === last ? inner(exchanges[i].body) : withoutDecisions(inner(exchanges[i].body))));
  const opening = first === last ? exchanges[first].body : withoutDecisions(exchanges[first].body);
  const merged = opening.replace(/<\/gameState>\s*$/, `${events.join('')}</gameState>`);
  return {
    ...recording,
    exchanges: exchanges
      .filter((_, i) => !rest.includes(i))
      .map((exchange, i) => (i === first ? { ...exchange, body: merged } : exchange)),
  };
}

const stepsOf = (recording) => (options.step ? Number(options.step) : recording.exchanges.length);

async function shots(browser) {
  fs.mkdirSync(OUT, { recursive: true });
  for (const recording of recordings()) {
    for (const device of options.devices.split(',')) {
      const shown = await openPhone(browser, device, recording, stepsOf(recording));
      await shown.settle();
      const file = path.join(OUT, `${recording.name}-${device}.png`);
      await shown.page.screenshot({ path: file });
      if (shown.errors.length) console.log(`${recording.name} ${device}: ${shown.errors.join(' | ')}`);
      console.log(file);
      await shown.close();
    }
  }
}

async function main() {
  const browser = await launchBrowser();
  try {
    if (options.shots) {
      await shots(browser);
      return;
    }
    const devices = options.devices.split(',');
    const chosen = options.checks.split(',');
    let passed = true;
    if (chosen.includes('back')) passed = (await backCheck(browser, devices)) && passed;
    if (chosen.includes('reload')) passed = (await reloadCheck(browser, devices)) && passed;
    if (chosen.includes('touch')) passed = (await touchCheck(browser, devices)) && passed;
    if (chosen.includes('piles')) passed = (await pilesCheck(browser, devices)) && passed;
    if (chosen.includes('animation')) passed = (await animationCheck(browser, devices)) && passed;
    process.exitCode = passed ? 0 : 1;
  } finally {
    await browser.close();
  }
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}
