#!/usr/bin/env node
// For reading what a check found. A report folder is what `--out` named (report-local, report-matrix, ...).
//
//   npm run inspect -- summary report-matrix                 the differences, grouped by cause
//   npm run inspect -- where report-matrix <name>            where a difference is on the screen
//   npm run inspect -- crop report-matrix <name> x y w h [zoom]    the legacy page above the Angular page, as one picture
//
// <name> is the start of the picture's file name, e.g. empty--resetAll--hover-1366x768 or card-info-1366x768-step1.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { PNG } from 'pngjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const reportDir = (name) => path.resolve(HERE, '..', name);
const readPicture = (dir, name, kind) =>
  PNG.sync.read(fs.readFileSync(path.join(reportDir(dir), `${name}-${kind}.png`)));

/** A problem's text without what varies between scenarios (pixel counts, values, long lists). */
function cause(problem) {
  return problem
    .replace(/^(on load|step \d+|end): /, '')
    .replace(/^\d+ pixels differ$/, 'pixels differ')
    .replace(/^\d+ pixels differ in the accepted area (.*) \(limit \d+\)$/, 'over the limit of the accepted area $1')
    .replace(/over [^)]*\)/, 'over ...)')
    .replace(/^request, (legacy|Angular) only: (\S+ \S+).*/, 'request only on the $1 page: $2')
    .replace(/^([a-zA-Z ]+): legacy (null|\{.*?\}), Angular (null|\{.*?\}).*/, (_, name, a, b) =>
      a === 'null' || b === 'null' ? `${name} is on one page only` : `${name} differs`,
    )
    .slice(0, 110);
}

function summary(dir) {
  const results = JSON.parse(fs.readFileSync(path.join(reportDir(dir), 'results.json'), 'utf8'));
  const failing = results.checks.filter((check) => check.problems.length);
  console.log(`${results.checks.length} checks, ${failing.length} differ, ${results.gaps.length} not tested\n`);
  const causes = new Map();
  for (const check of failing) {
    for (const reason of new Set(check.problems.map(cause))) {
      causes.set(reason, [...(causes.get(reason) ?? []), check.name]);
    }
  }
  for (const [reason, names] of [...causes.entries()].sort((a, b) => b[1].length - a[1].length)) {
    console.log(`${String(names.length).padStart(4)}  ${reason}\n      e.g. ${names.slice(0, 3).join(', ')}`);
  }
  for (const gap of results.gaps) {
    console.log(`GAP   ${gap}`);
  }
}

function where(dir, name) {
  const diff = readPicture(dir, name, 'diff');
  const cells = new Map();
  for (let y = 0; y < diff.height; y++) {
    for (let x = 0; x < diff.width; x++) {
      const i = (y * diff.width + x) * 4;
      if (diff.data[i] === 255 && diff.data[i + 1] === 0 && diff.data[i + 2] === 0) {
        const key = `${x >> 5},${y >> 5}`;
        cells.set(key, (cells.get(key) ?? 0) + 1);
      }
    }
  }
  const total = [...cells.values()].reduce((sum, count) => sum + count, 0);
  console.log(`${total} pixels differ. Where most of them are (32px squares):`);
  for (const [key, count] of [...cells.entries()].sort((a, b) => b[1] - a[1]).slice(0, 8)) {
    const [x, y] = key.split(',').map((n) => n * 32);
    console.log(`  x ${x}, y ${y}: ${count}`);
  }
}

function crop(dir, name, x, y, width, height, zoom = 1) {
  const [legacy, angular] = [readPicture(dir, name, 'legacy'), readPicture(dir, name, 'angular')];
  const out = new PNG({ width: width * zoom, height: (height * 2 + 4) * zoom });
  out.data.fill(255);
  const draw = (picture, top) => {
    for (let row = 0; row < height; row++) {
      for (let column = 0; column < width; column++) {
        const from = ((y + row) * picture.width + x + column) * 4;
        for (let i = 0; i < zoom; i++) {
          for (let j = 0; j < zoom; j++) {
            picture.data.copy(
              out.data,
              ((top + row) * zoom + j) * out.width * 4 + (column * zoom + i) * 4,
              from,
              from + 4,
            );
          }
        }
      }
    }
  };
  draw(legacy, 0);
  draw(angular, height + 4);
  const file = path.join(reportDir(dir), `${name}-crop.png`);
  fs.writeFileSync(file, PNG.sync.write(out));
  console.log(file);
}

const [command, dir, name, ...numbers] = process.argv.slice(2);
if (command === 'summary' && dir) {
  summary(dir);
} else if (command === 'where' && name) {
  where(dir, name);
} else if (command === 'crop' && numbers.length >= 4) {
  crop(dir, name, ...numbers.map(Number));
} else {
  console.error('Usage: inspect summary <report> | where <report> <name> | crop <report> <name> x y w h [zoom]');
  process.exitCode = 2;
}
