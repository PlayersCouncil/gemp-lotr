#!/usr/bin/env node
// Converts the legacy client's card image tables into src/generated/card-data.json, which the deck builder imports.
//
// The tables stay in the legacy files (gemp-lotr-async/src/main/web/js/gemp-022/...), so adding a card image still
// means editing only those files; this script runs before every `npm run build`, `npm start`, `npm test` (npm
// pre-scripts, so also from Maven) and in docker/frontend-watch.sh.
//
// Usage: node scripts/extract-card-data.mjs [--check]
//   --check  writes nothing; exits with 1 if the JSON is missing or out of date.

import { mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import vm from 'node:vm';

const FRONTEND_DIR = resolve(dirname(fileURLToPath(import.meta.url)), '..');
/** The legacy client's web root. */
export const LEGACY_WEB_DIR = resolve(FRONTEND_DIR, '../gemp-lotr-async/src/main/web');
export const OUTPUT_FILE = resolve(FRONTEND_DIR, 'src/generated/card-data.json');
/** The server's card definitions (HJSON), where each card's side is written. */
export const CARD_DEFINITIONS_DIR = resolve(FRONTEND_DIR, '../gemp-lotr-cards/src/main/resources');
/** The Shadow cards' blueprint ids, which the game's phone layout sorts after the Free Peoples ones. */
export const SIDES_FILE = resolve(FRONTEND_DIR, 'src/generated/card-sides.json');

/** Each legacy data file (under the web root) and the global table it defines, in the order the pages load them. */
export const CARD_DATA_FILES = [
  { file: 'js/gemp-022/cards/CardImages.js', table: 'fixedImages' },
  { file: 'js/gemp-022/set40.js', table: 'set40' },
  { file: 'js/gemp-022/hobbit.js', table: 'hobbit' },
  { file: 'js/gemp-022/PC_Cards.js', table: 'PCCards' },
  { file: 'js/gemp-022/cards/PackImages.js', table: 'packBlueprints' },
];

/**
 * Runs a legacy data file in an empty sandbox (no browser globals) and returns the table it defines. Works for both
 * `var` and `const` declarations. Throws unless the table is an object of strings.
 */
export function evaluateTable(source, table, file = table) {
  const script = new vm.Script(`${source}\n;typeof ${table} === 'undefined' ? undefined : ${table};`, {
    filename: file,
  });
  const value = script.runInNewContext({}, { timeout: 5000 });
  if (value === null || typeof value !== 'object' || Array.isArray(value)) {
    throw new Error(`${file} does not define the table "${table}"`);
  }
  const result = {};
  for (const [key, image] of Object.entries(value)) {
    if (typeof image !== 'string') {
      throw new Error(`${file}: ${table}["${key}"] is not a string`);
    }
    result[key] = image;
  }
  return result;
}

/** All tables, keyed by their global name. */
export function extractCardData(webDir = LEGACY_WEB_DIR) {
  const tables = {};
  for (const { file, table } of CARD_DATA_FILES) {
    tables[table] = evaluateTable(readFileSync(resolve(webDir, file), 'utf8'), table, file);
  }
  return tables;
}

/**
 * The cultures and their side, as the server's Culture enum: a card's culture decides its side
 * (CultureFieldProcessor), but Gollum's, whose cards say their side themselves.
 */
const SHADOW_CULTURES = new Set([
  'dunland',
  'isengard',
  'men',
  'moria',
  'orc',
  'raider',
  'sauron',
  'uruk-hai',
  'wraith',
  'ringwraith',
  'fallen realms',
  'gundabad',
  'mirkwood',
  'smaug',
  'spider',
  'troll',
]);
const FREE_CULTURES = new Set(['dwarven', 'elven', 'gandalf', 'gondor', 'rohan', 'shire', 'esgaroth']);

/**
 * The blueprint ids of the Shadow cards, from the card definitions, with the server's rule for a card's side (the side
 * the deck builder's Side filter shows): its culture's side, or, for Gollum and cards without a culture, its `side`.
 * Promos mapped to a card in blueprintMapping.txt take its side. game/card-sides.mjs (visual-check)
 * compares the result with the server's.
 */
export function extractShadowCards(definitionsDir = CARD_DEFINITIONS_DIR) {
  const walk = (dir) =>
    readdirSync(dir, { withFileTypes: true }).flatMap((entry) =>
      entry.isDirectory() ? walk(join(dir, entry.name)) : entry.name.endsWith('.hjson') ? [join(dir, entry.name)] : [],
    );
  const files = walk(join(definitionsDir, 'cards')).sort();
  if (files.length === 0) throw new Error(`No card definitions under ${definitionsDir}/cards`);
  /** Per card: its culture and its own side, as written. */
  const cards = new Map();
  for (const file of files) readCards(readFileSync(file, 'utf8'), cards);
  const shadow = new Set();
  for (const [id, { culture, side }] of cards) {
    const byCulture =
      culture === null || culture === 'gollum'
        ? null
        : SHADOW_CULTURES.has(culture)
          ? 'shadow'
          : FREE_CULTURES.has(culture)
            ? 'free'
            : null;
    if ((byCulture ?? side) === 'shadow') shadow.add(id);
  }
  for (const line of readFileSync(join(definitionsDir, 'blueprintMapping.txt'), 'utf8').split(/\r?\n/)) {
    const [from, to] = line.split(',').map((id) => id?.trim());
    if (from && to && !from.startsWith('#') && shadow.has(to)) shadow.add(from);
  }
  return [...shadow].sort((a, b) => a.localeCompare(b, 'en', { numeric: true }));
}

/**
 * Reads the cards of one definitions file (HJSON) into `cards`: each card's culture and side, its own fields only
 * (one level inside it; deeper ones are a filter's or an effect's). Follows the braces rather than the indentation,
 * which differs between files; a quoteless value runs to the end of its line, so braces in a card's text don't count.
 */
function readCards(source, cards) {
  let depth = 0;
  let card = null;
  let inText = false;
  for (const raw of source.split(/\r?\n/)) {
    if (inText) {
      if (raw.includes("'''")) inText = false;
      continue;
    }
    const line = raw
      .replace(/\/\*.*?\*\//g, '')
      .replace(/^\s*(?:\/\/|#).*$/, '')
      .trim();
    if (line === '') continue;
    const field = line.match(/^"?([\w.-]+)"?\s*:\s*(.*)$/);
    if (field) {
      const [, key, value] = field;
      if (value.startsWith("'''") && !value.slice(3).includes("'''")) {
        inText = true;
        continue;
      }
      if (value.startsWith('{') || value.startsWith('[')) {
        if (depth === 1 && value.startsWith('{') && /^[0-9]+_[0-9A-Za-z.]+$/.test(key)) {
          card = { culture: null, side: null };
          cards.set(key, card);
        }
        depth += count(value, '{[') - count(value, '}]');
      } else if (depth === 2 && card !== null && (key === 'culture' || key === 'side')) {
        card[key] = value.replace(/^"|"$/g, '').trim().toLowerCase().replace(/_/g, ' ');
      }
      continue;
    }
    // A line of braces, or an array's item: an object in it opens with a brace
    if (/^[{}[\],\s]+$/.test(line) || line.startsWith('{')) {
      depth += count(line, '{[') - count(line, '}]');
      if (depth < 2) card = depth === 1 ? null : card;
    }
  }
}

function count(text, characters) {
  return [...text].filter((c) => characters.includes(c)).length;
}

export function renderCardSides(shadow) {
  return JSON.stringify({ shadow }) + '\n';
}

export function renderCardData(tables) {
  return JSON.stringify(tables, null, 1) + '\n';
}

function main(args) {
  for (const [file, json] of [
    [OUTPUT_FILE, renderCardData(extractCardData())],
    [SIDES_FILE, renderCardSides(extractShadowCards())],
  ]) {
    let current = null;
    try {
      current = readFileSync(file, 'utf8');
    } catch {
      // missing: written below, or reported by --check
    }
    if (args.includes('--check')) {
      if (current !== json) {
        console.error(`${file} is out of date; run: node scripts/extract-card-data.mjs`);
        process.exit(1);
      }
      continue;
    }
    if (current !== json) {
      mkdirSync(dirname(file), { recursive: true });
      writeFileSync(file, json);
      console.log(`Card data written to ${file}`);
    }
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2));
}
