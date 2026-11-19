import { TestBed } from '@angular/core/testing';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { LEGACY_CARD_DATA_FILES, loadLegacyCardScripts } from '../../testing/legacy-cards';
import { readLegacyWebFile } from '../../testing/files';
import { CARD_IMAGE_TABLES } from './card-data';

const SCRIPT = resolve(process.cwd(), 'scripts/extract-card-data.mjs');
const GENERATED = resolve(process.cwd(), 'src/generated/card-data.json');

/**
 * The card image tables are generated from the legacy data files (scripts/extract-card-data.mjs). These checks fail
 * when a legacy file was edited without regenerating, or when the generated tables differ from what the legacy
 * pages see.
 */
describe('card data parity with the legacy data files', () => {
  const onDisk = (): Record<string, Record<string, string>> => JSON.parse(readFileSync(GENERATED, 'utf8'));

  it('is up to date (the extraction script finds nothing to change)', () => {
    expect(() => execFileSync(process.execPath, [SCRIPT, '--check'], { stdio: 'pipe' })).not.toThrow();
  });

  it('holds every table the legacy pages define, with every key and image, and nothing else', () => {
    // Evaluated independently of the script, all files in one scope as a page loads them
    expect(onDisk()).toStrictEqual(loadLegacyCardScripts().tables);
  });

  it.each(LEGACY_CARD_DATA_FILES)('$table lists its keys in the order of $file', ({ file, table }) => {
    const legacy = new Function(`${readLegacyWebFile(file)}\n;return ${table};`)() as Record<string, string>;
    expect(Object.keys(onDisk()[table])).toEqual(Object.keys(legacy));
    expect(Object.keys(legacy).length).toBeGreaterThan(0);
  });

  it('is what the app uses', () => {
    expect(TestBed.inject(CARD_IMAGE_TABLES)).toStrictEqual(onDisk());
  });
});
