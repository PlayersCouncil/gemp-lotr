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
  ...['FotR - Booster', '(S)FotR - Starter'].map((id) => ({ pack: id, count: 2 })),
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
        ? `<pack blueprintId="${item.pack}" count="${item.count}"/>`
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

function serverAnswer(url, request) {
  const p = url.pathname.replace('/gemp-lotr-server', '');
  if (p === '/deck/formats') return { json: FORMATS };
  if (p === '/deck/list') return { file: 'deck-list.xml' };
  if (p === '/deck/stats') return { file: 'stats.html', type: 'text/html' };
  if (p === '/collection') return { file: 'collection-types.xml' };
  if (p.startsWith('/collection/')) return { xml: collectionXml(url) };
  if (p === '/league') return { file: 'leagues.xml' };
  if (p === '/delivery/packContents') return { status: 404 };
  return null;
}

async function route(context, log) {
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
      const answer = serverAnswer(url, request);
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
      await route(context, log);
      const page = await context.newPage();
      page.on('pageerror', (error) => log.push(`page error: ${error.message}`));
      await page.goto(ORIGIN + address);
      await settle(page);
      await scenario.act(page, which);
      await settle(page);
      const file = path.join(outDir, `${scenario.name}-${size.label}-${which}.png`);
      await page.screenshot({ path: file });
      console.log(`${file}${log.length ? `\n  ${[...new Set(log)].join('\n  ')}` : ''}`);
      await context.close();
    }
  }
}
await browser.close();
