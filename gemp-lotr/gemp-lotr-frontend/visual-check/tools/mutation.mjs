#!/usr/bin/env node
// Mutation testing of the deck builder's logic: changes one small thing in the code at a time (a comparison, a
// condition, a constant, a message) and runs the unit tests of that file. A change no test notices (a survivor) is a
// place the tests don't pin down.
//
//   npm run check:mutation                         every file in TARGETS (about an hour)
//   npm run check:mutation -- --files deck-import  only the files whose path contains one of these (comma separated)
//   npm run check:mutation -- --workers 3          copies tested side by side (default 2)
//   npm run check:mutation -- --with-parity        also runs src/parity (the legacy page side by side), much slower
//   npm run check:mutation -- --survivors          only the mutants that survived the last run
//
// It works on copies of the frontend in .local-server/mutation/, never on the checkout: the development `frontend`
// container rebuilds whenever the checkout's sources change. The survivors go to report-mutation/survivors.md, every
// result to report-mutation/results.json. It exits with 1 if a file's tests fail before any change.
import { spawn, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const VISUAL_CHECK = path.resolve(HERE, '..');
const FRONTEND = path.resolve(VISUAL_CHECK, '..');
const LEGACY = path.resolve(FRONTEND, '../gemp-lotr-async');
const WORK = path.join(VISUAL_CHECK, '.local-server/mutation');
const REPORT = path.join(VISUAL_CHECK, 'report-mutation');

/** The logic worth mutating: what the deck builder decides, not how it is drawn. */
export const TARGETS = [
  'src/core/api/api-error.ts',
  'src/core/api/form-codec.ts',
  'src/core/api/parsers.ts',
  'src/core/api/xml.ts',
  'src/core/cards/card-blueprint.ts',
  'src/core/cards/card-image.service.ts',
  'src/core/collection/collection-browser.service.ts',
  'src/core/collection/collection-cards.ts',
  'src/core/collection/collection-query.service.ts',
  'src/core/collection/filter-block-sets.ts',
  'src/core/collection/filter-cultures.ts',
  'src/core/collection/filter-fields.ts',
  'src/core/collection/filter-options.ts',
  'src/core/collection/filter-string.ts',
  'src/core/deck/deck-cards.ts',
  'src/core/deck/deck-changes.service.ts',
  'src/core/deck/deck-contents.ts',
  'src/core/deck/deck-editing.service.ts',
  'src/core/deck/deck-format-selection.service.ts',
  'src/core/deck/deck-import.ts',
  'src/core/deck/deck-library.service.ts',
  'src/core/deck/deck-model.ts',
  'src/core/deck/deck-opener.service.ts',
  'src/core/deck/deck-saver.service.ts',
  'src/core/deck/deck-stats.ts',
  'src/core/deck/format-options.ts',
  'src/core/packs/packs.service.ts',
  'src/ui/shared/auto-zoom/zoom-placement.ts',
  'src/ui/shared/fit-cards/fit-cards.ts',
];

/**
 * The changes, each made at one place at a time. Comparisons need spaces around them, so type arguments (`Array<T>`)
 * and arrows (`=>`) are left alone.
 */
const OPERATORS = [
  { name: 'equality', find: / (===|!==) /g, to: (op) => ` ${op === '===' ? '!==' : '==='} ` },
  {
    name: 'boundary',
    find: / (<=|>=|<|>) /g,
    to: (op) => ` ${{ '<=': '<', '>=': '>', '<': '<=', '>': '>=' }[op.trim()]} `,
  },
  { name: 'logic', find: / (&&|\|\|) /g, to: (op) => ` ${op.trim() === '&&' ? '||' : '&&'} ` },
  { name: 'nullish', find: / \?\? /g, to: () => ' || ' },
  { name: 'boolean', find: /\b(true|false)\b/g, to: (word) => (word === 'true' ? 'false' : 'true') },
  {
    name: 'arithmetic',
    find: / ([+\-*/]) (?=[\w(])/g,
    to: (op) => ` ${{ '+': '-', '-': '+', '*': '/', '/': '*' }[op.trim()]} `,
  },
  { name: 'negation', find: /(?<![\w)\]!=])!(?=[\w(.])/g, to: () => '' },
  {
    name: 'number',
    find: /(?<![\w.$'"`-])(\d+(?:\.\d+)?)(?![\w.'"`])/g,
    to: (n) => String(Number(n) === 0 ? 1 : Number(n) + 1),
  },
  { name: 'string', find: /'(?:[^'\\\n]|\\.)+'/g, to: () => "''" },
  { name: 'call', find: /\.(some|every)\(/g, to: (_, m) => `.${m === 'some' ? 'every' : 'some'}(` },
  { name: 'trim', find: /\.trim\(\)/g, to: () => '' },
];

/** Lines no mutation is made in: comments, imports, types and decorators' metadata. */
function skipped(line) {
  const text = line.trim();
  return (
    text === '' ||
    /^(\/\/|\/?\*|import |export (type|interface) |type |interface |@|selector:|templateUrl:|host:)/.test(text) ||
    // A field of an interface or a type: `name: Type;`
    /^(readonly )?[\w$]+\??: [^=]+;$/.test(text)
  );
}

function mutantsOf(file, source) {
  const mutants = [];
  const lines = source.split('\n');
  let inTemplate = false;
  lines.forEach((line, index) => {
    // Inline templates of components: what is drawn, not decided
    if (/^\s*template: `/.test(line)) inTemplate = !/`,?\s*$/.test(line.replace(/^\s*template: `/, ''));
    else if (inTemplate) {
      if (/`,?\s*$/.test(line)) inTemplate = false;
      return;
    }
    if (skipped(line)) return;
    // Text after a trailing `//` comment isn't code
    const code = line.replace(/\s\/\/ .*$/, '');
    for (const operator of OPERATORS) {
      for (const match of code.matchAll(operator.find)) {
        const replacement = operator.to(match[1] ?? match[0], match[1]);
        if (replacement === match[0]) continue;
        const mutated = line.slice(0, match.index) + replacement + line.slice(match.index + match[0].length);
        mutants.push({
          file,
          line: index + 1,
          operator: operator.name,
          original: line.trim(),
          mutated: mutated.trim(),
          source: [...lines.slice(0, index), mutated, ...lines.slice(index + 1)].join('\n'),
        });
      }
    }
  });
  return mutants;
}

/** Every TypeScript file under src, and the files of src it imports (relative imports only). */
function importGraph() {
  const files = spawnSync('find', ['src', '-name', '*.ts'], { cwd: FRONTEND, encoding: 'utf8' })
    .stdout.split('\n')
    .filter(Boolean);
  const known = new Set(files);
  const graph = new Map();
  for (const file of files) {
    const source = fs.readFileSync(path.join(FRONTEND, file), 'utf8');
    const imports = [...source.matchAll(/(?:from|import)\s+'(\.{1,2}\/[^']+)'/g)]
      .map(([, spec]) => path.join(path.dirname(file), spec.replace(/\.js$/, '')))
      .flatMap((base) => [`${base}.ts`, `${base}/index.ts`])
      .filter((candidate) => known.has(candidate));
    graph.set(file, imports);
  }
  return graph;
}

/** The side-by-side helpers that open a whole page: the specs that use them are slow (--with-parity adds them). */
const WHOLE_PAGE = new Set(['src/testing/parity/angular-page.ts', 'src/testing/parity/both-pages.ts']);

/**
 * The specs that use the file, directly or through other files, so a file tested through the service that uses it
 * counts as tested. Specs that reach it only by opening the whole page aren't included (see WHOLE_PAGE).
 */
function specsOf(file, graph) {
  const reaches = (start) => {
    const seen = new Set();
    const stack = [start];
    while (stack.length) {
      const at = stack.pop();
      if (at === file) return true;
      if (seen.has(at) || WHOLE_PAGE.has(at)) continue;
      seen.add(at);
      stack.push(...(graph.get(at) ?? []));
    }
    return false;
  };
  return [...graph.keys()].filter((spec) => spec.endsWith('.spec.ts') && reaches(spec)).sort();
}

/** A copy of the frontend that tests can run in: the sources, and links to what never changes. */
function makeCopy(dir) {
  fs.rmSync(dir, { recursive: true, force: true });
  const frontend = path.join(dir, 'gemp-lotr-frontend');
  fs.mkdirSync(path.join(frontend, 'visual-check'), { recursive: true });
  for (const entry of ['src', 'public', 'scripts']) {
    fs.cpSync(path.join(FRONTEND, entry), path.join(frontend, entry), { recursive: true });
  }
  for (const entry of fs.readdirSync(FRONTEND).filter((f) => /^(angular|package|tsconfig.*)\.json$/.test(f))) {
    fs.copyFileSync(path.join(FRONTEND, entry), path.join(frontend, entry));
  }
  for (const entry of ['controls.mjs', 'controls.d.mts']) {
    fs.copyFileSync(path.join(VISUAL_CHECK, entry), path.join(frontend, 'visual-check', entry));
  }
  fs.symlinkSync(path.join(FRONTEND, 'node_modules'), path.join(frontend, 'node_modules'));
  // The tests read the legacy page's files from next to the frontend
  fs.symlinkSync(LEGACY, path.join(dir, 'gemp-lotr-async'));
  return frontend;
}

/** Runs specs in a copy: 'killed', 'survived', 'timeout' or 'invalid' (the change doesn't compile). */
function runSpecs(frontend, specs, timeoutMs) {
  return new Promise((resolve) => {
    const args = ['ng', 'test', '--watch=false', ...specs.flatMap((spec) => ['--include', spec])];
    const child = spawn('npx', args, {
      cwd: frontend,
      env: { ...process.env, PARITY_SESSIONS: '3', PARITY_CASES: '500', NG_CLI_ANALYTICS: 'false' },
      detached: true,
    });
    let output = '';
    child.stdout.on('data', (chunk) => (output += chunk));
    child.stderr.on('data', (chunk) => (output += chunk));
    const timer = setTimeout(() => {
      try {
        process.kill(-child.pid, 'SIGKILL');
      } catch {
        // already gone
      }
      resolve('timeout');
    }, timeoutMs);
    child.on('close', (code) => {
      clearTimeout(timer);
      if (code === 0) resolve('survived');
      else if (/bundle generation failed|TS\d{4}|error TS/i.test(output)) resolve('invalid');
      else resolve('killed');
    });
  });
}

async function main() {
  const { values } = parseArgs({
    options: {
      files: { type: 'string' },
      workers: { type: 'string', default: '2' },
      'with-parity': { type: 'boolean', default: false },
      survivors: { type: 'boolean', default: false },
    },
  });
  const graph = importGraph();
  // --survivors: only the mutants that survived the last run, e.g. after adding tests for them
  const previous = values.survivors
    ? new Set(
        JSON.parse(fs.readFileSync(path.join(REPORT, 'results.json'), 'utf8'))
          .filter((r) => r.outcome === 'survived')
          .map((r) => `${r.file}:${r.line}:${r.mutated}`),
      )
    : null;
  const filters = values.files?.split(',').filter(Boolean) ?? [];
  const targets = TARGETS.filter((file) => !filters.length || filters.some((f) => file.includes(f)));
  const workers = Math.max(1, Number(values.workers));
  const copies = Array.from({ length: workers }, (_, i) => makeCopy(path.join(WORK, `copy-${i}`)));

  const jobs = [];
  for (const file of targets) {
    if (previous && ![...previous].some((key) => key.startsWith(`${file}:`))) continue;
    const specs = [...specsOf(file, graph), ...(values['with-parity'] ? ['src/parity/**/*.spec.ts'] : [])];
    if (!specs.length) {
      console.log(`NO TESTS  ${file}`);
      continue;
    }
    const before = await runSpecs(copies[0], specs, 300_000);
    if (before !== 'survived') {
      console.error(`${file}: its tests fail without any change (${before}); fix that first`);
      process.exit(1);
    }
    const source = fs.readFileSync(path.join(FRONTEND, file), 'utf8');
    for (const mutant of mutantsOf(file, source)) {
      if (!previous || previous.has(`${mutant.file}:${mutant.line}:${mutant.mutated}`)) jobs.push({ ...mutant, specs });
    }
  }
  console.log(`${jobs.length} mutants in ${targets.length} files, ${workers} at a time`);

  const results = [];
  let next = 0;
  const started = Date.now();
  await Promise.all(
    copies.map(async (copy) => {
      while (next < jobs.length) {
        const job = jobs[next++];
        const target = path.join(copy, job.file);
        const original = fs.readFileSync(path.join(FRONTEND, job.file), 'utf8');
        fs.writeFileSync(target, job.source);
        const outcome = await runSpecs(copy, job.specs, 120_000);
        fs.writeFileSync(target, original);
        const { source, ...result } = job;
        results.push({ ...result, outcome });
        if (outcome === 'survived') console.log(`SURVIVED  ${job.file}:${job.line}  ${job.mutated}`);
        if (results.length % 25 === 0) {
          const minutes = (Date.now() - started) / 60000;
          const left = (minutes / results.length) * (jobs.length - results.length);
          console.log(`${results.length}/${jobs.length} (about ${Math.round(left)} min left)`);
        }
      }
    }),
  );

  if (previous) {
    // The earlier results stay, with the survivors' new outcomes in place of the old ones
    const key = (r) => `${r.file}:${r.line}:${r.mutated}`;
    const fresh = new Map(results.map((r) => [key(r), r]));
    const earlier = JSON.parse(fs.readFileSync(path.join(REPORT, 'results.json'), 'utf8'));
    writeReport(earlier.map((r) => fresh.get(key(r)) ?? r));
  } else if (filters.length && fs.existsSync(path.join(REPORT, 'results.json'))) {
    // --files: the other files' earlier results stay in the report
    const earlier = JSON.parse(fs.readFileSync(path.join(REPORT, 'results.json'), 'utf8'));
    writeReport([...earlier.filter((r) => !targets.includes(r.file)), ...results]);
  } else {
    writeReport(results);
  }
}

function writeReport(results) {
  fs.mkdirSync(REPORT, { recursive: true });
  results.sort((a, b) => a.file.localeCompare(b.file) || a.line - b.line);
  fs.writeFileSync(path.join(REPORT, 'results.json'), JSON.stringify(results, null, 1));
  const count = (outcome) => results.filter((r) => r.outcome === outcome).length;
  const valid = results.length - count('invalid');
  const score = valid ? Math.round(((valid - count('survived')) / valid) * 1000) / 10 : 100;
  const lines = [
    `# Mutation testing`,
    '',
    `${results.length} mutants: ${count('killed')} killed, ${count('timeout')} timed out, ${count('invalid')} didn't ` +
      `compile, **${count('survived')} survived**. Score ${score}% of those that compiled.`,
    '',
  ];
  for (const file of [...new Set(results.map((r) => r.file))]) {
    const survivors = results.filter((r) => r.file === file && r.outcome === 'survived');
    const mine = results.filter((r) => r.file === file && r.outcome !== 'invalid');
    lines.push(`## ${file}: ${mine.length - survivors.length} of ${mine.length} killed`, '');
    for (const s of survivors) lines.push(`- line ${s.line} (${s.operator}): \`${s.original}\` → \`${s.mutated}\``);
    lines.push('');
  }
  fs.writeFileSync(path.join(REPORT, 'survivors.md'), lines.join('\n'));
  console.log(lines.slice(0, 3).join('\n'));
  console.log(`Report: ${path.join(REPORT, 'survivors.md')}`);
}

main();
