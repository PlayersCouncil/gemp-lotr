// A stand-in for the Gemp server, for running the check without one: both pages are served from disk (the legacy web
// root and the Angular build output) through Playwright routes, and the server's answers come from
// src/testing/server-responses plus a generated collection. Card images are replaced by labelled placeholders, the same
// on both pages.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { FIXTURE_DECK } from './scenarios.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const FRONTEND = path.resolve(HERE, '..');
const WEB = path.resolve(FRONTEND, '../gemp-lotr-async/src/main/web');
const BUILD = path.join(WEB, 'ng');
const RESPONSES = path.join(FRONTEND, 'src/testing/server-responses');
export const STUB_ORIGIN = 'http://gemp.test';

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

/** The formats as a real server lists them: their blocks decide which rows of culture buttons the filter shows. */
const FORMATS = JSON.parse(fs.readFileSync(path.join(RESPONSES, 'formats.json'), 'utf8'));

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

/** What a search for sites finds: more than one page of landscape cards. */
const SITES = Array.from({ length: 24 }, (_, i) => ({ id: `1_${319 + i}`, count: 1, side: null }));

function collectionXml(url) {
  const start = Number(url.searchParams.get('start') ?? 0);
  const count = Number(url.searchParams.get('count') ?? 18);
  const source = /cardType:SITE/.test(url.searchParams.get('filter') ?? '') ? SITES : COLLECTION;
  const items = source
    .slice(start, start + count)
    .map((item) =>
      item.pack
        ? `<pack blueprintId="${item.pack}" count="${item.count}"${item.contents ? ` contents="${item.contents}"` : ''}/>`
        : `<card blueprintId="${item.id}" count="${item.count}"${item.side ? ` side="${item.side}"` : ''}/>`,
    )
    .join('');
  return `<?xml version="1.0" encoding="UTF-8"?><collection count="${source.length}">${items}</collection>`;
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
  ['Fellowship Block', FIXTURE_DECK.name],
  ['Fellowship Block', 'Test + deck &amp; co'],
  ['Anything Goes', 'Aragorn and the Rangers'],
  ['Movie Block (PC)', 'Gandalf the Grey, Gandalf the White and the Fellowship of the Ring on their way to Mordor'],
  ['Fellowship Block', 'Moria Orcs'],
];
const DECKS_XML = `<?xml version="1.0" encoding="UTF-8"?><decks>${DECKS.map(
  ([format, name]) => `<deck targetFormat="${format}">${name}</deck>`,
).join('')}</decks>`;

/** The deck the deck list scenarios open. */
function fixtureDeckXml() {
  const [ringBearer, ring, sites, draw] = FIXTURE_DECK.contents.split('|');
  const siteCards = sites.split(',').map((id) => `<site blueprintId="${id}"/>`);
  const drawCards = draw
    .split(',')
    .map((id, i) => `<card blueprintId="${id}" side="${i % 2 ? 'SHADOW' : 'FREE_PEOPLE'}"/>`);
  return (
    `<?xml version="1.0" encoding="UTF-8"?><deck><targetFormat formatCode="${FIXTURE_DECK.targetFormat}" ` +
    `formatName="Fellowship Block"/><notes>${FIXTURE_DECK.notes}</notes><ringBearer blueprintId="${ringBearer}"/>` +
    `<ring blueprintId="${ring}"/>${siteCards.join('')}${drawCards.join('')}</deck>`
  );
}

function serverAnswer(url) {
  const p = url.pathname.replace('/gemp-lotr-server', '');
  if (p === '/player') return { json: {} };
  if (p === '/deck/formats') return { json: FORMATS };
  if (p === '/deck') return { xml: fixtureDeckXml() };
  if (p === '/deck/list' || p === '/deck/libraryList') return { xml: DECKS_XML };
  if (p === '/deck/stats') return { file: 'stats.html', type: 'text/html' };
  if (p === '/collection') return { file: 'collection-types.xml' };
  if (p.startsWith('/collection/')) return { xml: collectionXml(url) };
  if (p === '/league') return { file: 'leagues.xml' };
  if (p === '/delivery/packContents') return { status: 404 };
  return null;
}

/** Answers everything the context's pages ask for; what can't be answered goes to `log`. */
export async function routeToStub(context, log) {
  await context.route(/.*/, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== STUB_ORIGIN) {
      if (/\.(jpe?g|png|gif)$/i.test(url.pathname)) {
        return route.fulfill({ contentType: 'image/svg+xml', body: placeholder(url) });
      }
      return route.fulfill({ status: 404, body: '' });
    }
    if (url.pathname.startsWith('/gemp-lotr-server/')) {
      const answer = serverAnswer(url);
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
    if (url.pathname.startsWith('/gemp-lotr/ng/')) {
      file = path.join(BUILD, url.pathname.slice('/gemp-lotr/ng/'.length));
      // As the server does (WebRequestHandler): an app route (no extension) that isn't a file serves the app's index
      const route = !path.basename(url.pathname).includes('.') || url.pathname.endsWith('/');
      if (route && (!fs.existsSync(file) || fs.statSync(file).isDirectory())) file = path.join(BUILD, 'index.html');
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
