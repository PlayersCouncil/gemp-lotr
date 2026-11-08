import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative, resolve, sep } from 'node:path';

/** src/core holds state, rules and server calls only: the view and the themes build on it, never the reverse. */
const CORE_DIR = resolve(process.cwd(), 'src/core');
const SRC_DIR = resolve(process.cwd(), 'src');

function sourceFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) {
      return sourceFiles(path);
    }
    return path.endsWith('.ts') && !path.endsWith('.spec.ts') ? [path] : [];
  });
}

/** Relative imports of a file, resolved against src/ ("core/deck/deck.service", "ui/shared/..."). */
function importedPaths(file: string, source: string): string[] {
  return [...source.matchAll(/(?:import|export)[^'"]*from\s*'(\.[^']+)'/g)].map((match) =>
    relative(SRC_DIR, resolve(file, '..', match[1]))
      .split(sep)
      .join('/'),
  );
}

/** The page and browser globals; DOCUMENT is Angular's token for the page. `Document` (a type, e.g. XML) is fine. */
const BROWSER_GLOBALS =
  /\b(DOCUMENT|window|document|globalThis|navigator|localStorage|sessionStorage|matchMedia|location)\b(?!\s*:)/g;

/** Comments may mention the page ("in document order"), and module paths may too ('./page-location'). */
function withoutComments(source: string): string {
  return source
    .replace(/from\s*'[^']*'/g, "from ''")
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/(^|[^:'"])\/\/[^\n]*/g, '$1');
}

describe('core boundary', () => {
  const files = sourceFiles(CORE_DIR).map((file) => ({
    file,
    name: relative(SRC_DIR, file),
    source: readFileSync(file, 'utf8'),
  }));

  it('finds the core sources', () => {
    expect(files.length).toBeGreaterThan(20);
  });

  it('imports nothing from outside core (no view, ui or themes), except the generated card data', () => {
    const outside = files.flatMap(({ file, name, source }) =>
      importedPaths(file, source)
        .filter((path) => !path.startsWith('core/') && !path.startsWith('generated/'))
        .map((path) => `${name} -> ${path}`),
    );
    expect(outside).toEqual([]);
  });

  it("uses the Notifier, never the browser's alert, confirm or prompt", () => {
    const calls = files.flatMap(({ name, source }) =>
      [...source.matchAll(/(?<![\w.]|abstract )(alert|confirm|prompt)\s*\(|window\.(alert|confirm|prompt)\b/g)].map(
        (match) => `${name}: ${match[0]}`,
      ),
    );
    expect(calls).toEqual([]);
  });

  it('reaches the browser (DOCUMENT, window, document, navigator, storage) only in core/platform', () => {
    const uses = files
      .filter(({ name }) => !name.startsWith('core/platform/'))
      .flatMap(({ name, source }) =>
        [...withoutComments(source).matchAll(BROWSER_GLOBALS)].map((match) => `${name}: ${match[0]}`),
      );
    expect(uses).toEqual([]);
  });

  it('has no styles or templates', () => {
    const viewCode = files.filter(({ source }) => /@Component\(|@Directive\(|\.css'|styleUrl|templateUrl/.test(source));
    expect(viewCode.map(({ name }) => name)).toEqual([]);
  });
});
