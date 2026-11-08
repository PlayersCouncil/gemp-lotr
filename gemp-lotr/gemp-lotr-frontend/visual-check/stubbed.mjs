// Screenshots the legacy deckBuild.html and the Angular deck builder side by side with no Gemp server: both pages are
// served from disk (the legacy web root and the Angular build output) through Playwright routes, and the server's
// answers come from src/testing/server-responses plus a generated collection. Card images (on i.lotrtcgpc.net) are
// replaced by labelled placeholders, the same on both pages.
//
//   node stubbed.mjs [--angular '/gemp-lotr/ng/deckbuilder/?ui=next&theme=classic'] [--sizes 1440x900]
//                    [--theme classic] [--out report-stubbed] [--only collection,...]
//
// Writes <out>/<scenario>-<size>-{legacy,angular}.png. Needs a build of the Angular page (npm run build). Unlike
// run.mjs this compares nothing by itself: it is for looking at the two pages while the new UI is being built.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { chromium } from 'playwright';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const FRONTEND = path.resolve(HERE, '..');
const WEB = path.resolve(FRONTEND, '../gemp-lotr-async/src/main/web');
const BUILD = path.join(WEB, 'ng/deckbuilder');
const RESPONSES = path.join(FRONTEND, 'src/testing/server-responses');
const ORIGIN = 'http://gemp.test';

const { values: options } = parseArgs({
  options: {
    legacy: { type: 'string', default: '/gemp-lotr/deckBuild.html' },
    angular: { type: 'string', default: '/gemp-lotr/ng/deckbuilder/?ui=next&theme=classic' },
    sizes: { type: 'string', default: '1440x900' },
    only: { type: 'string' },
    out: { type: 'string', default: 'report-stubbed' },
  },
});

const TYPES = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.mjs': 'text/javascript',
  '.css': 'text/css',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.json': 'application/json',
  '.woff2': 'font/woff2',
};

const FORMATS = {
  Formats: Object.fromEntries(
    [
      ['rev_tow_sta', 'Anything Goes'],
      ['fotr_block', 'Fellowship Block'],
      ['pc_movie', 'Movie Block (PC)'],
    ].map(([code, name], order) => [
      code,
      {
        code,
        name,
        order,
        hall: true,
        usesMaps: false,
        blockFilters: [
          { filter: 'fotr_block', name: 'Fellowship Block', setFilters: { 'The Fellowship of the Ring': '1' } },
        ],
        errata: {},
      },
    ]),
  ),
};

/** A collection of the first cards of set 1, a few of its sites (landscape cards) and two packs. */
const COLLECTION = [
  { pack: 'FotR - Booster', count: 2 },
  { pack: '(S)FotR - Starter', count: 2, contents: '1_1|1_2|1_3|1_4|1_319' },
  ...Array.from({ length: 60 }, (_, i) => ({
    id: `1_${i + 1}`,
    count: (i % 4) + 1,
    side: i % 3 === 0 ? 'SHADOW' : 'FREE_PEOPLE',
  })),
  ...Array.from({ length: 9 }, (_, i) => ({ id: `1_${319 + i}`, count: 1, side: null })),
];

function collectionXml(url) {
  const start = Number(url.searchParams.get('start') ?? 0);
  const count = Number(url.searchParams.get('count') ?? 18);
  const items = COLLECTION.slice(start, start + count)
    .map((item) =>
      item.pack
        ? `<pack blueprintId="${item.pack}" count="${item.count}"${item.contents ? ` contents="${item.contents}"` : ''}/>`
        : `<card blueprintId="${item.id}" count="${item.count}"${item.side ? ` side="${item.side}"` : ''}/>`,
    )
    .join('');
  return `<?xml version="1.0" encoding="UTF-8"?><collection count="${COLLECTION.length}">${items}</collection>`;
}

/** A card-sized placeholder with the image's name, so cards can be told apart. */
function placeholder(url) {
  const name = decodeURIComponent(url.pathname.split('/').pop());
  let hash = 0;
  for (const c of name) hash = (hash * 31 + c.charCodeAt(0)) >>> 0;
  const hue = hash % 360;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="357" height="497" viewBox="0 0 357 497">
<rect width="357" height="497" fill="hsl(${hue} 40% 35%)"/><rect x="20" y="20" width="317" height="250" fill="hsl(${hue} 30% 55%)"/>
<text x="178" y="340" font-family="sans-serif" font-size="34" fill="#fff" text-anchor="middle">${name}</text></svg>`;
}

/** A few saved decks (and library decks), with a name to escape and a long one. */
const DECKS = [
  ['Fellowship Block', 'Test + deck &amp; co'],
  ['Anything Goes', 'Aragorn and the Rangers'],
  ['Movie Block (PC)', 'Gandalf the Grey, Gandalf the White and the Fellowship of the Ring on their way to Mordor'],
  ['Fellowship Block', 'Moria Orcs'],
];
const DECKS_XML = `<?xml version="1.0" encoding="UTF-8"?><decks>${DECKS.map(
  ([format, name]) => `<deck targetFormat="${format}">${name}</deck>`,
).join('')}</decks>`;

function serverAnswer(url, request, scenario) {
  const p = url.pathname.replace('/gemp-lotr-server', '');
  const own = scenario.answers?.[p];
  if (own) return own;
  if (p === '/deck/formats') return { json: FORMATS };
  if (p === '/deck/list' || p === '/deck/libraryList') return { xml: DECKS_XML };
  if (p === '/deck/stats') return { file: 'stats.html', type: 'text/html' };
  if (p === '/collection') return { file: 'collection-types.xml' };
  if (p.startsWith('/collection/')) return { xml: collectionXml(url) };
  if (p === '/league') return { file: 'leagues.xml' };
  if (p === '/delivery/packContents') return { status: 404 };
  return null;
}

async function route(context, log, scenario) {
  await context.route(/.*/, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== ORIGIN) {
      if (/\.(jpe?g|png|gif)$/i.test(url.pathname)) {
        return route.fulfill({ contentType: 'image/svg+xml', body: placeholder(url) });
      }
      return route.fulfill({ status: 404, body: '' });
    }
    if (url.pathname.startsWith('/gemp-lotr-server/')) {
      const answer = serverAnswer(url, request, scenario);
      if (!answer) {
        log.push(`unanswered ${request.method()} ${url.pathname}`);
        return route.fulfill({ status: 404, body: '' });
      }
      if (answer.status) return route.fulfill({ status: answer.status, body: '' });
      if (answer.json) return route.fulfill({ contentType: 'application/json', body: JSON.stringify(answer.json) });
      if (answer.xml) return route.fulfill({ contentType: 'application/xml', body: answer.xml });
      return route.fulfill({
        path: path.join(RESPONSES, answer.file),
        contentType: answer.type ?? 'application/xml',
      });
    }
    let file;
    if (url.pathname.startsWith('/gemp-lotr/ng/deckbuilder/')) {
      file = path.join(BUILD, url.pathname.slice('/gemp-lotr/ng/deckbuilder/'.length));
      if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) file = path.join(BUILD, 'index.html');
    } else {
      file = path.join(WEB, url.pathname.replace(/^\/gemp-lotr\//, ''));
    }
    if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) {
      if (/\.(jpe?g|png|gif)$/i.test(url.pathname)) {
        return route.fulfill({ contentType: 'image/svg+xml', body: placeholder(url) });
      }
      log.push(`missing ${url.pathname}`);
      return route.fulfill({ status: 404, body: '' });
    }
    return route.fulfill({ path: file, contentType: TYPES[path.extname(file)] ?? 'application/octet-stream' });
  });
}

/** The same control on each page: the legacy element IDs, and the new UI's DOM contract. */
const CONTROLS = {
  nextPage: { legacy: '#nextPage', angular: '.gd-pager__next' },
  side: { legacy: '#sideSelect', angular: '.gd-field[data-field="side"] select' },
  firstCard: { legacy: '#collection-display .card .actionArea', angular: '.gd-collection .gd-card' },
  deckList: { legacy: '#deckListBut', angular: '.gd-toolbar [data-action="deck-list"]' },
  library: { legacy: '#libraryListBut', angular: '.gd-toolbar [data-action="library"]' },
  importDeck: { legacy: '#importDeckBut', angular: '.gd-toolbar [data-action="import"]' },
  notes: { legacy: '#notesBut', angular: '.gd-toolbar [data-action="notes"]' },
  /** The selection pack in the collection (the second item). */
  selectionPack: { legacy: '#collection-display .card .actionArea', angular: '.gd-collection .gd-card' },
  deleteDeck: { legacy: '.deckItem button[title="Delete deck"]', angular: '.gd-deck-list [data-action="delete"]' },
  renameDeck: { legacy: '.deckItem button[title="Rename deck"]', angular: '.gd-deck-list [data-action="rename"]' },
  /** A card by its image, in the deck's piles or in the collection. */
  deckCard: (image) => ({
    legacy: `#decksRegion .card:has(img[src*="${image}"]) .actionArea`,
    angular: `.gd-pile .gd-card:has(img[src*="${image}"])`,
  }),
  collectionCard: (image) => ({
    legacy: `#collection-display .card:has(img[src*="${image}"]) .actionArea`,
    angular: `.gd-collection .gd-card:has(img[src*="${image}"])`,
  }),
};

/** Rests the mouse on a card past the hover zoom's delay. */
async function hover(page, selector) {
  await page.hover(selector);
  await page.waitForTimeout(800);
}

/** Each scenario runs on a freshly loaded page; `act` does the same thing on both. */
export const STUBBED_SCENARIOS = [
  { name: 'collection', act: async () => {} },
  {
    name: 'second-page',
    act: async (page, which) => {
      await page.click(CONTROLS.nextPage[which]);
    },
  },
  {
    name: 'shadow-filter',
    act: async (page, which) => {
      await page.selectOption(CONTROLS.side[which], 'SHADOW');
    },
  },
  {
    name: 'deck',
    act: async (page, which) => {
      await addDeck(page, which);
    },
  },
  {
    name: 'zoom-deck',
    act: async (page, which) => {
      await addDeck(page, which);
      await hover(page, CONTROLS.deckCard('LOTR01005')[which]);
    },
  },
  {
    name: 'zoom-collection',
    act: async (page, which) => {
      await hover(page, CONTROLS.collectionCard('LOTR01006')[which]);
    },
  },
  {
    name: 'card-info',
    act: async (page, which) => {
      await page.locator(CONTROLS.firstCard[which]).nth(2).click({ button: 'right' });
    },
  },
  {
    name: 'deck-list',
    act: async (page, which) => {
      await page.click(CONTROLS.deckList[which]);
    },
  },
  {
    name: 'library',
    act: async (page, which) => {
      await page.click(CONTROLS.library[which]);
    },
  },
  {
    name: 'import',
    act: async (page, which) => {
      await page.click(CONTROLS.importDeck[which]);
    },
  },
  {
    name: 'import-typed',
    act: async (page, which) => {
      await page.click(CONTROLS.importDeck[which]);
      await page.waitForTimeout(200);
      await page.fill(which === 'legacy' ? '#deckImport' : '.gd-import__text', '1x Gandalf\n3x Aragorn');
      await page.mouse.move(0, 0);
    },
  },
  {
    name: 'notes',
    act: async (page, which) => {
      await page.click(CONTROLS.notes[which]);
    },
  },
  {
    name: 'selection',
    act: async (page, which) => {
      await page.locator(CONTROLS.selectionPack[which]).nth(1).click();
    },
  },
  {
    name: 'delivery',
    answers: { '/delivery/packContents': { file: 'pack-delivery.xml' } },
    act: async () => {},
  },
  /*
   * The legacy page asked with the browser's own dialogs, which a screenshot doesn't show (Playwright answers them);
   * the legacy side shows the page behind them.
   */
  {
    name: 'confirm',
    act: async (page, which) => {
      await page.click(CONTROLS.deckList[which]);
      await page.waitForTimeout(300);
      await page.locator(CONTROLS.deleteDeck[which]).first().click();
    },
  },
  {
    name: 'prompt',
    act: async (page, which) => {
      await page.click(CONTROLS.deckList[which]);
      await page.waitForTimeout(300);
      await page.locator(CONTROLS.renameDeck[which]).first().click();
    },
  },
];

/**
 * A few cards in the deck: Free Peoples and Shadow cards from the first page (one of them three times, so copies show
 * behind it) and three sites from the last page (12 items a page).
 */
async function addDeck(page, which) {
  const cards = page.locator(CONTROLS.firstCard[which]);
  for (const index of [2, 3, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11]) {
    await cards.nth(index).click();
    await page.waitForTimeout(50);
  }
  for (let i = 0; i < 5; i++) {
    await page.click(CONTROLS.nextPage[which]);
    await settle(page);
  }
  for (const index of [2, 3, 4]) {
    await cards.nth(index).click();
    await page.waitForTimeout(50);
  }
}

async function settle(page) {
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(400);
}

const sizes = options.sizes.split(',').map((s) => {
  const [width, height] = s.split('x').map(Number);
  return { width, height, label: s };
});
const only = options.only ? new Set(options.only.split(',')) : null;
const outDir = path.resolve(HERE, options.out);
fs.rmSync(outDir, { recursive: true, force: true });
fs.mkdirSync(outDir, { recursive: true });

const browser = await chromium.launch();
for (const size of sizes) {
  for (const scenario of STUBBED_SCENARIOS.filter((s) => !only || only.has(s.name))) {
    for (const [which, address] of [
      ['legacy', options.legacy],
      ['angular', options.angular],
    ]) {
      const context = await browser.newContext({ viewport: { width: size.width, height: size.height } });
      const log = [];
      await route(context, log, scenario);
      const page = await context.newPage();
      page.on('pageerror', (error) => log.push(`page error: ${error.message}`));
      await page.goto(ORIGIN + address);
      await settle(page);
      await scenario.act(page, which);
      await settle(page);
      const file = path.join(outDir, `${scenario.name}-${size.label}-${which}.png`);
      await page.screenshot({ path: file });
      if (process.env.MEASURE) {
        const rects = await page.evaluate((selectors) => selectors.flatMap((sel) => [...document.querySelectorAll(sel)].slice(0, 3).map((el) => {
          const r = el.getBoundingClientRect();
          const cs = getComputedStyle(el);
          return `${sel}: ${r.left.toFixed(2)},${r.top.toFixed(2)} ${r.width.toFixed(2)}x${r.height.toFixed(2)} font ${cs.fontSize} m ${cs.margin} p ${cs.padding} lh ${cs.lineHeight} va ${cs.verticalAlign} d ${cs.display}`;
        })), process.env.MEASURE.split('|'));
        console.log(rects.join('\n'));
      }
      console.log(`${file}${log.length ? `\n  ${[...new Set(log)].join('\n  ')}` : ''}`);
      await context.close();
    }
  }
}
await browser.close();
