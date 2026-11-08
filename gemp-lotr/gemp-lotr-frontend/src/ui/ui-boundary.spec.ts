import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative, resolve, sep } from 'node:path';

/**
 * src/ui holds semantic markup and interaction: it builds on core, never on the old view (src/deckbuilder-app, which
 * goes away), and it has no visual CSS: the themes (src/themes) draw everything.
 */
const SRC_DIR = resolve(process.cwd(), 'src');
const UI_DIR = join(SRC_DIR, 'ui');

function sourceFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) {
      return sourceFiles(path);
    }
    return /\.(ts|html)$/.test(path) && !path.endsWith('.spec.ts') ? [path] : [];
  });
}

describe('ui boundary', () => {
  const files = sourceFiles(UI_DIR).map((file) => ({
    file,
    name: relative(SRC_DIR, file).split(sep).join('/'),
    source: readFileSync(file, 'utf8'),
  }));

  it('finds the ui sources', () => {
    expect(files.length).toBeGreaterThanOrEqual(8);
  });

  it('imports only core and ui (not the old view, not the themes)', () => {
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

  it('has no component styles, and no inline styles but `display: contents`', () => {
    const styled = files.flatMap(({ name, source }) => [
      ...[...source.matchAll(/\bstyles\s*:|styleUrls?\s*:/g)].map((match) => `${name}: ${match[0]}`),
      ...[...source.matchAll(/\bstyle="([^"]*)"|style:\s*'([^']*)'/g)]
        .filter((match) => (match[1] ?? match[2]).trim() !== 'display: contents')
        .map((match) => `${name}: ${match[0]}`),
      ...[...source.matchAll(/\[style\.(?!--)[\w-]+/g)].map((match) => `${name}: ${match[0]}`),
    ]);
    expect(styled).toEqual([]);
  });
});
