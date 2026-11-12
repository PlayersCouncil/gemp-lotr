import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative, resolve, sep } from 'node:path';

/**
 * The themes are values only, and players can switch between them at any time: so every theme must give every value
 * the stylesheets use, and set no value they don't. Classic (on :root) gives them all; another theme
 * (`:root[data-theme=...]`) changes some and keeps classic's for the rest.
 */
const SRC_DIR = resolve(process.cwd(), 'src');
const THEMES_CSS = readFileSync(join(SRC_DIR, 'styles/themes.css'), 'utf8').replace(/\/\*[\s\S]*?\*\//g, '');

/** Values classic leaves unset on purpose (the browser's default, as on the legacy page); a theme may set them. */
const UNSET_IN_CLASSIC = ['--theme-font-page', '--theme-other-icon-filter'];

function stylesheets(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? stylesheets(path) : path.endsWith('.css') ? [path] : [];
  });
}

/** The values a block of declarations sets. */
const declared = (block: string) => new Set([...block.matchAll(/(--theme-[\w-]+)\s*:/g)].map((match) => match[1]));

/** Each `selector { ... }` block of themes.css (nested in @media or not), by its selector. */
function blocks(): { selector: string; body: string }[] {
  return [...THEMES_CSS.matchAll(/([^{}@]+)\{([^{}]*)\}/g)].map((match) => ({
    selector: match[1].trim(),
    body: match[2],
  }));
}

describe('themes', () => {
  const all = blocks();
  const classic = declared(
    all
      .filter(({ selector }) => selector === ':root')
      .map(({ body }) => body)
      .join(''),
  );
  const used = new Map<string, string[]>();
  for (const file of [...stylesheets(join(SRC_DIR, 'ui')), ...stylesheets(join(SRC_DIR, 'styles'))]) {
    const name = relative(SRC_DIR, file).split(sep).join('/');
    for (const [, value] of readFileSync(file, 'utf8').matchAll(/var\((--theme-[\w-]+)/g)) {
      used.set(value, [...(used.get(value) ?? []), name]);
    }
  }

  it('finds the classic values and the stylesheets that use them', () => {
    expect(classic.size).toBeGreaterThanOrEqual(30);
    expect(used.size).toBeGreaterThanOrEqual(30);
  });

  it('gives in classic every value the stylesheets use (unless left unset on purpose)', () => {
    const missing = [...used.keys()].filter((value) => !classic.has(value) && !UNSET_IN_CLASSIC.includes(value));
    expect(missing).toEqual([]);
    expect(UNSET_IN_CLASSIC.filter((value) => classic.has(value))).toEqual([]);
  });

  it('sets only values that are used, in every theme', () => {
    const themes = all.filter(({ selector }) => selector.startsWith(':root'));
    expect(themes.some(({ selector }) => selector === ":root[data-theme='modern']")).toBe(true);
    const unused = themes.flatMap(({ selector, body }) =>
      [...declared(body)]
        .filter((value) => !used.has(value) && !themes.some((theme) => theme.body.includes(`var(${value})`)))
        .map((value) => `${selector}: ${value}`),
    );
    expect(unused).toEqual([]);
  });

  it("is chosen only by data-theme on <html>: a theme's values are on :root, nowhere else", () => {
    const selectors = [...new Set(all.map(({ selector }) => selector))];
    expect(selectors.every((selector) => /^:root(\[data-theme='[a-z]+'\])?$/.test(selector))).toBe(true);
  });
});
