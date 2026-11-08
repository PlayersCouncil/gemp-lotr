#!/usr/bin/env node
// Converts the legacy client's card image tables into src/generated/card-data.json, which the deck builder imports.
//
// The tables stay in the legacy files (gemp-lotr-async/src/main/web/js/gemp-022/...), so adding a card image still
// means editing only those files; this script runs before every `npm run build`, `npm start`, `npm test` (npm
// pre-scripts, so also from Maven) and in docker/frontend-watch.sh.
//
// Usage: node scripts/extract-card-data.mjs [--check]
//   --check  writes nothing; exits with 1 if the JSON is missing or out of date.

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import vm from 'node:vm';

const FRONTEND_DIR = resolve(dirname(fileURLToPath(import.meta.url)), '..');
/** The legacy client's web root. */
export const LEGACY_WEB_DIR = resolve(FRONTEND_DIR, '../gemp-lotr-async/src/main/web');
export const OUTPUT_FILE = resolve(FRONTEND_DIR, 'src/generated/card-data.json');

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

export function renderCardData(tables) {
  return JSON.stringify(tables, null, 1) + '\n';
}

function main(args) {
  const json = renderCardData(extractCardData());
  let current = null;
  try {
    current = readFileSync(OUTPUT_FILE, 'utf8');
  } catch {
    // missing: written below, or reported by --check
  }
  if (args.includes('--check')) {
    if (current !== json) {
      console.error(`${OUTPUT_FILE} is out of date; run: node scripts/extract-card-data.mjs`);
      process.exit(1);
    }
    return;
  }
  if (current !== json) {
    mkdirSync(dirname(OUTPUT_FILE), { recursive: true });
    writeFileSync(OUTPUT_FILE, json);
    console.log(`Card data written to ${OUTPUT_FILE}`);
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2));
}
