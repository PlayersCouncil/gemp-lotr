/** Test-only: the legacy card scripts, loaded from disk, for the parity checks. */
import { CardImageTables } from '../core/cards/card-blueprint';
import { readLegacyWebFile } from './files';

/**
 * Each legacy data file and the global table it defines, as the legacy pages include them. Kept apart from the list in
 * scripts/extract-card-data.mjs on purpose: card-data.parity.spec.ts fails if the two disagree.
 */
export const LEGACY_CARD_DATA_FILES: readonly { file: string; table: keyof CardImageTables }[] = [
  { file: 'js/gemp-022/cards/CardImages.js', table: 'fixedImages' },
  { file: 'js/gemp-022/set40.js', table: 'set40' },
  { file: 'js/gemp-022/hobbit.js', table: 'hobbit' },
  { file: 'js/gemp-022/PC_Cards.js', table: 'PCCards' },
  { file: 'js/gemp-022/cards/PackImages.js', table: 'packBlueprints' },
];

/** The subset of the legacy Card class (js/gemp-022/cards/Card.js) the parity tests call. */
export interface LegacyCardClass {
  new (blueprintId: string, testingText?: string, backSideTestingText?: string, zone?: string): LegacyCard;
  StripBlueprintId(id: string): string;
  GetFoil(id: string): boolean;
  GetAlternateImage(id: string): boolean;
  isPlaceholder(id: string): boolean;
  isErrata(id: string): boolean;
  isIncomplete(id: string): boolean;
  isBlueprintHorizontal(id: string): boolean;
  isHorizontal(id: string, zone?: string): boolean;
  getImageUrl(id: string, tengwar?: boolean, ignoreErrata?: boolean): string;
}

export interface LegacyCard {
  bareBlueprint: string;
  imageUrl: string;
  backSideImageUrl: string;
  foil: boolean;
  tengwar: unknown;
  incomplete: boolean;
  horizontal: boolean;
  hasWiki: boolean;
  isPack(): boolean;
  isPlaceholder(): boolean;
  hasErrata(): boolean;
  getWikiLink(): string;
}

export interface LegacyCardScripts {
  Card: LegacyCardClass;
  tables: Record<string, Record<string, string>>;
}

/** Evaluates the data files and Card.js together, as a page would load them; jQuery is stubbed ($.inArray only). */
export function loadLegacyCardScripts(): LegacyCardScripts {
  const files = [...LEGACY_CARD_DATA_FILES.map((f) => f.file), 'js/gemp-022/cards/Card.js'];
  const source = files.map(readLegacyWebFile).join('\n;\n');
  const tableNames = LEGACY_CARD_DATA_FILES.map((f) => f.table);
  const jQueryStub = { inArray: (value: unknown, array: unknown[]) => array.indexOf(value) };
  const result = new Function('$', `${source}\n;return { Card, tables: { ${tableNames.join(', ')} } };`)(jQueryStub);
  return result as LegacyCardScripts;
}
