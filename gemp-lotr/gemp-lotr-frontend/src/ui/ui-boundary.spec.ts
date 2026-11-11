import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative, resolve, sep } from 'node:path';

/**
 * src/ui holds the components: markup, interaction and each component's own stylesheet. It builds on core only. The
 * themes are values only (src/styles/themes.css), which every stylesheet draws with.
 */
const SRC_DIR = resolve(process.cwd(), 'src');
const UI_DIR = join(SRC_DIR, 'ui');
const STYLES_DIR = join(SRC_DIR, 'styles');

function filesIn(dir: string, pattern: RegExp): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) {
      return filesIn(path, pattern);
    }
    return pattern.test(path) && !path.endsWith('.spec.ts') ? [path] : [];
  });
}

const sourceFiles = (dir: string) => filesIn(dir, /\.(ts|html)$/);

/** A colour named in CSS, not taken from the theme: hex, a colour function or a colour keyword. */
const NAMED_COLOUR =
  /#[0-9a-f]{3,8}\b|\b(?:rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\(|:[^;]*\b(?:red|green|yellow|orange|blue|white|black|gr[ae]y)\b/gi;
/** The cards' own black frame and copy count: part of the card art, the same in every theme. */
const CARD_ART = new Set([
  'ui/shared/card/card.component.css',
  'ui/shared/auto-zoom/auto-zoom.component.css',
  'ui/shared/card-info/card-info.component.css',
]);

describe('ui boundary', () => {
  const files = sourceFiles(UI_DIR).map((file) => ({
    file,
    name: relative(SRC_DIR, file).split(sep).join('/'),
    source: readFileSync(file, 'utf8'),
  }));

  it('finds the ui sources', () => {
    expect(files.length).toBeGreaterThanOrEqual(8);
    // The app shell, the pages and their shared parts
    for (const folder of ['ui/app/', 'ui/deckbuilder/', 'ui/shared/']) {
      expect(files.some(({ name }) => name.startsWith(folder))).toBe(true);
    }
  });

  it('imports only core and ui (not the themes)', () => {
    const outside = files.flatMap(({ file, name, source }) =>
      [...source.matchAll(/(?:import|export)[^'"]*from\s*'(\.[^']+)'/g)]
        .map((match) =>
          relative(SRC_DIR, resolve(file, '..', match[1]))
            .split(sep)
            .join('/'),
        )
        .filter((path) => !path.startsWith('core/') && !path.startsWith('ui/'))
        .map((path) => `${name} -> ${path}`),
    );
    expect(outside).toEqual([]);
  });

  it('keeps ui/shared free of any one page, so other pages can use it', () => {
    const pageOnly = [
      'ui/deckbuilder/',
      'core/deck/',
      'core/collection/',
      'core/packs/',
      'core/delivery/',
      'core/actions/',
      'core/deckbuilder-state',
    ];
    const tied = files
      .filter(({ name }) => name.startsWith('ui/shared/'))
      .flatMap(({ file, name, source }) =>
        [...source.matchAll(/(?:import|export)[^'"]*from\s*'(\.[^']+)'/g)]
          .map((match) =>
            relative(SRC_DIR, resolve(file, '..', match[1]))
              .split(sep)
              .join('/'),
          )
          .filter((path) => pageOnly.some((folder) => path.startsWith(folder)))
          .map((path) => `${name} -> ${path}`),
      );
    expect(tied).toEqual([]);
  });

  it('styles a component with its own stylesheet (styleUrl), and nothing inline but `display: contents`', () => {
    const styled = files.flatMap(({ name, source }) => [
      ...[...source.matchAll(/\bstyles\s*:|styleUrls\s*:/g)].map((match) => `${name}: ${match[0]}`),
      ...[...source.matchAll(/\bstyle="([^"]*)"|style:\s*'([^']*)'/g)]
        .filter((match) => (match[1] ?? match[2]).trim() !== 'display: contents')
        .map((match) => `${name}: ${match[0]}`),
      ...[...source.matchAll(/\[style\.(?!--)[\w-]+/g)].map((match) => `${name}: ${match[0]}`),
    ]);
    expect(styled).toEqual([]);
    const withStyles = files.filter(({ source }) => /\bstyleUrl\s*:/.test(source)).map(({ name }) => name);
    expect(withStyles.length).toBeGreaterThanOrEqual(10);
  });

  it("draws with the theme values: no colours named outside src/styles/themes.css, but the cards' black and white", () => {
    const stylesheets = [...filesIn(UI_DIR, /\.css$/), ...filesIn(STYLES_DIR, /\.css$/)]
      .map((file) => ({ name: relative(SRC_DIR, file).split(sep).join('/'), source: readFileSync(file, 'utf8') }))
      .filter(({ name }) => name !== 'styles/themes.css');
    expect(stylesheets.length).toBeGreaterThanOrEqual(10);
    const named = stylesheets.flatMap(({ name, source }) =>
      [...source.replace(/\/\*[\s\S]*?\*\//g, '').matchAll(NAMED_COLOUR)]
        .filter((match) => !(CARD_ART.has(name) && /^#(000|fff)$/i.test(match[0])))
        .map((match) => `${name}: ${match[0]}`),
    );
    expect(named).toEqual([]);
  });

  it("never uses the browser's alert, confirm or prompt (the Notifier asks the player)", () => {
    // WindowNotifier is the one place that may: it is the Notifier that keeps the browser's dialogs
    const blocking = files
      .filter(({ name }) => name !== 'ui/shared/notifier/window-notifier.ts')
      .flatMap(({ name, source }) =>
        // Calls, not the DialogNotifier's own methods (a declaration is followed by its typed parameters)
        [...source.matchAll(/(?<![\w.])(alert|confirm|prompt)\s*\((?!\w+\s*:)|window\.(alert|confirm|prompt)\b/g)].map(
          (match) => `${name}: ${match[0]}`,
        ),
      );
    expect(blocking).toEqual([]);
  });
});
