#!/usr/bin/env node
// Decks saved by the legacy deck builder must keep working in the Angular one. Run by `npm run check:legacy-decks`
// (local-server.mjs --legacy-decks) on a throwaway server, logged in as the test player:
//
//   1. makes the decks a live database holds: through the server's own save, as the legacy page sent them (every
//      format kind, a map, a Race to Mount Doom league, errata and foils, names and notes with markup, quotes, line
//      breaks and other alphabets, the "null" notes the legacy page saved, notes escaped once and twice as it did when
//      it saved a deck again), and straight into the database what only an older server wrote (a format that no
//      longer exists, empty contents);
//   2. opens some on the legacy page and saves them there, to show what it does to them;
//   3. for every deck: what the legacy page shows, what the Angular page shows, then saves it on the Angular page and
//      checks the server kept exactly what it had, then what the legacy page shows again.
//
// Writes report-legacy-decks/results.json and exits with 1 if a deck doesn't open, isn't shown the same, or changes.
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from './browser.mjs';
import { CONTROLS } from './controls.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'report-legacy-decks');
const { values: options } = parseArgs({
  options: {
    server: { type: 'string' },
    legacy: { type: 'string', default: '/gemp-lotr/deckBuild.html' },
    angular: { type: 'string', default: '/gemp-lotr/ng/deckbuilder/' },
  },
});
const user = process.env.GEMP_USER;
const password = process.env.GEMP_PASSWORD;
const API = `${options.server}/gemp-lotr-server`;

/** Each place a card can be in a deck, on each page. */
const PLACES = {
  ringBearer: { legacy: '#ringBearerDiv .cardInDeck', angular: '.gd-slot[data-slot="ring-bearer"] .gd-card' },
  ring: { legacy: '#ringDiv .cardInDeck', angular: '.gd-slot[data-slot="ring"] .gd-card' },
  map: { legacy: '#map-div .cardInDeck', angular: '.gd-slot[data-slot="map"] .gd-card' },
  sites: { legacy: '#sitesDiv .cardInDeck', angular: '.gd-slot[data-slot="sites"] .gd-card' },
  // Every copy in the piles: the legacy page draws each as a card of its own, the Angular page too (data-role=copy)
  piles: { legacy: '#decksRegion .card', angular: '.gd-pile .gd-card' },
};

const FULL = '1_2|1_1|1_319,1_327,1_331,1_337,1_343,1_349,1_354,1_356,1_360|1_7,1_7,1_12,1_13,1_131,1_131,1_178';

/** The decks to make through the server's save, as the legacy page sent them. */
function apiDecks() {
  return [
    { deckName: 'Plain deck', targetFormat: 'fotr_block', notes: 'Plain notes', deckContents: FULL },
    { deckName: 'Anything Goes deck', targetFormat: 'rev_tow_sta', notes: '', deckContents: '|||1_5,1_6,1_6' },
    { deckName: 'Empty deck', targetFormat: 'rev_tow_sta', notes: '', deckContents: '|||' },
    { deckName: 'Only a Ring-bearer', targetFormat: 'standard', notes: '', deckContents: '1_2|||' },
    {
      deckName: 'Movie deck with a map',
      targetFormat: 'pc_movie',
      notes: 'With a map',
      deckContents: `${FULL}|102_74`,
    },
    {
      deckName: 'Errata and foils',
      targetFormat: 'pc_fotr_block',
      notes: '',
      deckContents: '1_2|1_1|51_331|1_12*,1_13,51_12',
    },
    {
      deckName: 'Notes typed with markup',
      targetFormat: 'fotr_block',
      notes: 'Tom & Jerry <3 > "q" \'s',
      deckContents: FULL,
    },
    { deckName: 'Notes escaped once', targetFormat: 'fotr_block', notes: 'Tom &amp; Jerry &lt;3', deckContents: FULL },
    { deckName: 'Notes escaped twice', targetFormat: 'fotr_block', notes: 'Tom &amp;amp; Jerry', deckContents: FULL },
    { deckName: 'Notes null', targetFormat: 'rev_tow_sta', notes: 'null', deckContents: '|||1_5' },
    { deckName: 'Notes in lines', targetFormat: 'fotr_block', notes: 'one\ntwo\n\tthree', deckContents: FULL },
    { deckName: 'Éowyn ☃ – “quotes”', targetFormat: 'fotr_block', notes: 'Ünïcødé ☃ 日本', deckContents: FULL },
    { deckName: 'Rock & "roll" <b>100%</b> #1?', targetFormat: 'fotr_block', notes: '', deckContents: FULL },
    { deckName: 'x'.repeat(100), targetFormat: 'fotr_block', notes: '', deckContents: FULL },
  ];
}

/** What only an older server wrote: straight into the database, as rows of the deck table. */
const DATABASE_DECKS = [
  { name: 'Format that is gone', format: 'A Format No Longer There', contents: FULL, notes: 'old' },
  // The server can't read this one (it answers 500): both pages must tell the player the same
  { name: 'Stored without contents', format: 'Anything Goes', contents: '', notes: '', serverFails: true },
];

/** Decks saved again on the legacy page, as players did: what it does to them is shown in the report. */
const RESAVED_ON_LEGACY = ['Notes typed with markup', 'Notes in lines', 'Plain deck'];

const COMPOSE = path.join(HERE, 'local-server', 'compose.yml');

/**
 * Restarts the throwaway server's app: the server loads its active leagues once a day, so a league the seed created
 * after it started is only known for saving and loading decks after a restart (a property of the server, not of the
 * pages).
 */
async function restartApp() {
  const result = spawnSync('docker', ['compose', '-f', COMPOSE, 'restart', 'app'], {
    env: process.env,
    stdio: 'ignore',
  });
  if (result.status !== 0) {
    throw new Error('Restarting the test server failed');
  }
  for (let i = 0; i < 120; i++) {
    const answer = await fetch(`${API}/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ login: user, password }),
    }).catch(() => null);
    if (answer?.ok) {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
  throw new Error('The test server did not come back after the restart');
}

function sql(statement) {
  const compose = COMPOSE;
  const result = spawnSync(
    'docker',
    [
      'compose',
      '-f',
      compose,
      'exec',
      '-T',
      'db',
      'sh',
      '-c',
      'mariadb -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" -N',
    ],
    { input: statement, encoding: 'utf8', env: process.env },
  );
  if (result.status !== 0) {
    throw new Error(`SQL failed: ${result.stderr}`);
  }
  return result.stdout;
}

const quote = (text) => `'${text.replace(/\\/g, '\\\\').replace(/'/g, "\\'")}'`;

async function api(context, method, url, form) {
  const response =
    method === 'GET' ? await context.request.get(`${API}${url}`) : await context.request.post(`${API}${url}`, { form });
  if (!response.ok()) {
    throw new Error(`${method} ${url}: HTTP ${response.status()}`);
  }
  return response.text();
}

/** The deck as the server has it (its answer to GET /deck), for comparing before and after. */
const storedDeck = (context, name) => api(context, 'GET', `/deck?deckName=${encodeURIComponent(name)}`);

/** The decks of the deck list, in the server's order. */
async function deckNames(context) {
  const xml = await api(context, 'GET', '/deck/list');
  const decode = (text) =>
    text.replace(
      /&(lt|gt|quot|apos|amp|#\d+);/g,
      (_, e) => ({ lt: '<', gt: '>', quot: '"', apos: "'", amp: '&' })[e] ?? String.fromCodePoint(Number(e.slice(1))),
    );
  return [...xml.matchAll(/<deck[^>]*>([^<]*)<\/deck>/g)].map(([, name]) => decode(name));
}

/** Waits until the page has asked and been answered, and the stats (refreshed on a timer) have arrived. */
async function settle(page) {
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(800);
  await page.waitForLoadState('networkidle');
}

async function openPage(context, which) {
  const page = await context.newPage();
  page.messages = [];
  /** What to type into the page's next prompts. */
  page.answers = [];
  page.on('dialog', (dialog) => {
    page.messages.push(`${dialog.type()}: ${dialog.message()}`);
    const answer = dialog.type() === 'prompt' ? page.answers.shift() : undefined;
    dialog.accept(answer).catch(() => undefined);
  });
  await page.goto(`${options.server}${options[which]}`);
  await settle(page);
  return page;
}

/** Opens the deck at that place of the deck list, through the deck list as a player would. */
async function openDeck(page, which, index) {
  await page.locator(CONTROLS.deckList[which]).first().click();
  await settle(page);
  await page.locator(CONTROLS.openDeck[which]).nth(index).click();
  await settle(page);
  await page
    .locator(CONTROLS.dialogClose[which])
    .first()
    .click()
    .catch(() => undefined);
  await settle(page);
}

/** What a page shows of the open deck. */
async function shown(page, which) {
  const view = await page.evaluate(
    ({ places, which, deckName, format, stats }) => {
      const images = (selector) =>
        [...document.querySelectorAll(selector)]
          .map((card) => decodeURIComponent(card.querySelector('img')?.getAttribute('src')?.split('/').pop() ?? ''))
          .sort();
      const select = document.querySelector(format);
      return {
        deckName: document.querySelector(deckName)?.textContent.replace(/\s+/g, ' ').trim(),
        format: select?.selectedOptions[0]?.textContent.trim(),
        cards: Object.fromEntries(
          Object.entries(places).map(([place, selectors]) => [place, images(selectors[which])]),
        ),
        stats: document.querySelector(stats)?.textContent.replace(/\s+/g, ' ').trim(),
      };
    },
    {
      places: PLACES,
      which,
      deckName: CONTROLS.deckName[which],
      format: CONTROLS.format[which],
      stats: CONTROLS.stats[which],
    },
  );
  // The notes, in the notes dialog
  await page.locator(CONTROLS.notes[which]).first().click();
  await settle(page);
  const notes = page.locator(CONTROLS.notesText[which]).last();
  view.notes = await notes.inputValue();
  await page.keyboard.press('Escape');
  await settle(page);
  return view;
}

/** The legacy page showed notes as it read them as HTML: one level of escaping more than the server stored. */
const escapedOnce = (text) => text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

async function main() {
  await restartApp();
  const browser = await launchBrowser();
  const context = await browser.newContext({ viewport: { width: 1366, height: 768 } });
  const login = await context.request.post(`${API}/login`, { form: { login: user, password } });
  if (!login.ok()) {
    throw new Error(`Login failed: HTTP ${login.status()}`);
  }
  const problems = [];
  const report = { made: [], resavedOnLegacy: [], decks: [] };

  // 1. The decks
  const leagues = await api(context, 'GET', '/league');
  const leagueCode = leagues
    .match(/<league\b[^>]*>/g)
    ?.find((tag) => tag.includes('type="RTMD"') && tag.includes('member="true"'))
    ?.match(/\bcode="([^"]+)"/)?.[1];
  for (const deck of apiDecks()) {
    try {
      await api(context, 'POST', '/deck', deck);
      report.made.push(deck.deckName);
    } catch (error) {
      problems.push(`making "${deck.deckName}": ${error.message}`);
    }
  }
  const playerId = sql(`select id from player where name=${quote(user)};`).trim();
  for (const deck of DATABASE_DECKS) {
    sql(
      `insert into deck (player_id, name, target_format, contents, notes) values (${playerId}, ${quote(deck.name)}, ` +
        `${quote(deck.format)}, ${quote(deck.contents)}, ${quote(deck.notes)});`,
    );
    report.made.push(deck.name);
  }

  // A Race to Mount Doom deck, made on the legacy page as players made them: the league chosen in the format list
  if (leagueCode) {
    const page = await openPage(context, 'legacy');
    const saves = [];
    page.on('request', (request) => {
      if (request.method() === 'POST' && /\/gemp-lotr-server\/deck$/.test(request.url())) {
        saves.push(request.postData());
      }
    });
    report.raceFormatOptions = await page.$$eval(`${CONTROLS.format.legacy} option`, (options) =>
      options.map((option) => `${option.value}=${option.textContent}`),
    );
    await page.selectOption(CONTROLS.format.legacy, { value: leagueCode });
    await settle(page);
    await page.locator(CONTROLS.collectionCards.legacy).first().click();
    await settle(page);
    page.answers.push('Race deck');
    await page.locator(CONTROLS.saveDeck.legacy).first().click();
    await settle(page);
    await page.close();
    report.raceSaves = saves;
    report.raceRow = sql(`select target_format, contents from deck where name='Race deck';`).trim();
    // The legacy page sends the league's code as the format, and the server stores it. When a deck is read, though,
    // the server drops a league it doesn't find among its active ones and rewrites the deck to the league's format
    // (DeckRequestHandler.serializeDeck), which on this server it does: both pages then show the same. Recorded here,
    // not a failure: it is the server's, not the deck builder's.
    report.raceServerKeptLeague = (await storedDeck(context, 'Race deck')).includes(`leagueCode="${leagueCode}"`);
    report.made.push('Race deck');
  } else {
    problems.push('the test player is in no Race to Mount Doom league');
  }

  // 2. Saved again on the legacy page
  for (const name of RESAVED_ON_LEGACY) {
    const before = await storedDeck(context, name);
    const page = await openPage(context, 'legacy');
    await openDeck(page, 'legacy', (await deckNames(context)).indexOf(name));
    await page.locator(CONTROLS.saveDeck.legacy).first().click();
    await settle(page);
    await page.close();
    const after = await storedDeck(context, name);
    report.resavedOnLegacy.push({ name, before, after, changed: before !== after });
  }

  // 3. Every deck on both pages, saved on the Angular page
  const names = await deckNames(context);
  for (const [index, name] of names.entries()) {
    const result = { name };
    report.decks.push(result);
    const problemsBefore = problems.length;
    try {
      if (DATABASE_DECKS.find((deck) => deck.name === name)?.serverFails) {
        const messages = {};
        for (const which of ['legacy', 'angular']) {
          const page = await openPage(context, which);
          await openDeck(page, which, index);
          messages[which] = page.messages;
          await page.close();
        }
        result.messages = messages;
        if (JSON.stringify(messages.legacy) !== JSON.stringify(messages.angular) || !messages.legacy.length) {
          problems.push(`"${name}" (the server can't read it): ${JSON.stringify(messages)}`);
        }
        console.log(`${problems.length === problemsBefore ? 'ok    ' : 'DIFFER'}  ${name} (the server can't read it)`);
        continue;
      }
      const stored = await storedDeck(context, name);
      result.stored = stored;

      const legacyPage = await openPage(context, 'legacy');
      await openDeck(legacyPage, 'legacy', index);
      result.legacy = await shown(legacyPage, 'legacy');
      await legacyPage.close();

      const angularPage = await openPage(context, 'angular');
      await openDeck(angularPage, 'angular', index);
      result.angular = await shown(angularPage, 'angular');
      await angularPage.locator(CONTROLS.saveDeck.angular).first().click();
      await settle(angularPage);
      await angularPage.close();

      result.storedAfterAngularSave = await storedDeck(context, name);
      const legacyAgain = await openPage(context, 'legacy');
      await openDeck(legacyAgain, 'legacy', (await deckNames(context)).indexOf(name));
      result.legacyAfter = await shown(legacyAgain, 'legacy');
      await legacyAgain.close();

      const { notes: legacyNotes, ...legacyRest } = result.legacy;
      const { notes: angularNotes, ...angularRest } = result.angular;
      if (JSON.stringify(legacyRest) !== JSON.stringify(angularRest)) {
        problems.push(`"${name}": the pages show it differently: ${JSON.stringify({ legacyRest, angularRest })}`);
      }
      // The Angular page shows the notes the server stored; the legacy page showed them escaped once more
      if (legacyNotes !== escapedOnce(angularNotes)) {
        problems.push(
          `"${name}": notes ${JSON.stringify(angularNotes)} on the Angular page, ${JSON.stringify(legacyNotes)} on the legacy page`,
        );
      }
      if (result.storedAfterAngularSave !== stored) {
        problems.push(
          `"${name}": saving it on the Angular page changed it:\n  before ${stored}\n  after  ${result.storedAfterAngularSave}`,
        );
      }
      if (JSON.stringify(result.legacyAfter) !== JSON.stringify(result.legacy)) {
        problems.push(`"${name}": the legacy page shows it differently after the Angular page saved it`);
      }
      console.log(`${problems.length === problemsBefore ? 'ok    ' : 'DIFFER'}  ${name}`);
    } catch (error) {
      problems.push(`"${name}": ${error.message}`);
    }
  }

  await browser.close();
  fs.mkdirSync(OUT, { recursive: true });
  fs.writeFileSync(path.join(OUT, 'results.json'), JSON.stringify({ ...report, problems }, null, 1));
  console.log(`\n${names.length} decks checked, ${report.made.length} made for this run.`);
  for (const resaved of report.resavedOnLegacy) {
    console.log(`Saved again on the legacy page: "${resaved.name}" ${resaved.changed ? 'CHANGED' : 'unchanged'}`);
  }
  for (const problem of problems) console.log(`PROBLEM ${problem}`);
  console.log(`Report: ${path.join(OUT, 'results.json')}`);
  return problems.length ? 1 : 0;
}

process.exitCode = await main();
