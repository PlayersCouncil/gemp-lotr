#!/usr/bin/env node
// What to pass a check to run again only what failed in its last report:
//
//   node tools/failed-checks.mjs <report folder>     prints the arguments, e.g. --only effects-shards,card-info
//   npm run check:game -- --pages legacy,angular --out report-game-g9 $(node tools/failed-checks.mjs report-game-g9)
//
// check:all --failed uses it for each part it runs again (tools/check-all.mjs).
//
// A report's results.json lists its checks as { name, problems, only?, check? }: `only` is the scenario, recording
// or game the check's --only takes, `check` the part of the check its --checks takes (game/devices.mjs, game/phone.mjs).
// game/table.mjs writes its own list ({ name, differences }), where the name is the --only one.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const VISUAL_CHECK = path.resolve(HERE, '..');

/** A report's checks, in one shape. */
function checksOf(results) {
  if (Array.isArray(results)) {
    return results.map((r) => ({ name: r.name, only: r.name, problems: r.differences ?? [] }));
  }
  return results.checks ?? [];
}

/**
 * The arguments that run again only the checks that failed in the report, or null when that can't be told: no
 * results.json, one older than `since` (the run that failed didn't get as far as writing it), or no failed check in it
 * (the check failed some other way, e.g. a gap or a crash). Null means: run the whole check again.
 */
export function rerunArgs(report, since = 0) {
  const file = path.join(path.resolve(VISUAL_CHECK, report), 'results.json');
  if (!fs.existsSync(file) || fs.statSync(file).mtimeMs < since) return null;
  const results = JSON.parse(fs.readFileSync(file, 'utf8'));
  if (!Array.isArray(results) && results.gaps?.length) return null;
  const failed = checksOf(results).filter((check) => check.problems.length);
  if (failed.length === 0) return null;
  const args = [];
  const parts = [...new Set(failed.map((check) => check.check).filter(Boolean))];
  if (parts.length) args.push('--checks', parts.join(','));
  // A failed check with no scenario of its own (e.g. devices.mjs's zoom) needs them all
  if (failed.every((check) => check.only))
    args.push('--only', [...new Set(failed.map((check) => check.only))].join(','));
  return args.length ? args : null;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const report = process.argv[2];
  if (!report) {
    console.error('usage: node tools/failed-checks.mjs <report folder>');
    process.exit(2);
  }
  const args = rerunArgs(report);
  if (!args) {
    console.error(
      `${report}: can't narrow (no results.json, coverage gaps, or no failed check in it): run the whole check`,
    );
    process.exit(1);
  }
  console.log(args.join(' '));
}
