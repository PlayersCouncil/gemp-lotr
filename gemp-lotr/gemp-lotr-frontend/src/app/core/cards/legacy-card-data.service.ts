import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { CardImageTables, EMPTY_CARD_IMAGE_TABLES, ImageTable } from './card-blueprint';

/** Where the legacy client's files are served (the web root, gemp-lotr-async/src/main/web). */
export const LEGACY_WEB_ROOT = '/gemp-lotr/';

/** Each legacy data file and the global table it defines. */
export const LEGACY_CARD_DATA_FILES: readonly { file: string; table: keyof CardImageTables }[] = [
  { file: 'js/gemp-022/cards/CardImages.js', table: 'fixedImages' },
  { file: 'js/gemp-022/set40.js', table: 'set40' },
  { file: 'js/gemp-022/hobbit.js', table: 'hobbit' },
  { file: 'js/gemp-022/PC_Cards.js', table: 'PCCards' },
  { file: 'js/gemp-022/cards/PackImages.js', table: 'packBlueprints' },
];

/**
 * Runs a legacy data file in its own function scope and returns the table it defines. Works for both `var` and
 * `const` declarations, and adds nothing to the page's globals.
 */
export function evaluateLegacyTable(source: string, tableName: string): ImageTable {
  if (!/^[A-Za-z_$][\w$]*$/.test(tableName)) {
    throw new Error(`Invalid table name: ${tableName}`);
  }
  const read = new Function(`${source}\n;return typeof ${tableName} === 'undefined' ? undefined : ${tableName};`);
  const table: unknown = read();
  if (table === null || typeof table !== 'object') {
    throw new Error(`The file does not define "${tableName}"`);
  }
  return table as ImageTable;
}

/**
 * The card image tables, loaded from the same files the legacy pages use (see LEGACY_CARD_DATA_FILES), so the
 * maintainers keep a single place to add card images. Loaded once before the app starts (deckbuilder-app.config.ts).
 */
@Injectable({ providedIn: 'root' })
export class LegacyCardDataService {
  private readonly http = inject(HttpClient);
  private loaded: CardImageTables = EMPTY_CARD_IMAGE_TABLES;

  get tables(): CardImageTables {
    return this.loaded;
  }

  /**
   * Loads every table. A file that fails to load or evaluate is logged and left empty, so the deck builder still
   * starts (those cards fall back to the default Decipher image location).
   */
  async load(): Promise<void> {
    const entries = await Promise.all(
      LEGACY_CARD_DATA_FILES.map(async ({ file, table }) => {
        try {
          const source = await firstValueFrom(this.http.get(LEGACY_WEB_ROOT + file, { responseType: 'text' }));
          return [table, evaluateLegacyTable(source, table)] as const;
        } catch (error) {
          console.error(`Could not load card images from ${file}`, error);
          return [table, {}] as const;
        }
      }),
    );
    this.loaded = { ...EMPTY_CARD_IMAGE_TABLES, ...Object.fromEntries(entries) };
  }
}
