import { CardImageTables, getImageUrl } from '../../../cards/card-blueprint';
import { previewTitle } from './errata-messages';
import { ErrataEntry } from './errata-models';

/** The two images of an errata'd card (PcErrata.imageUrls, previewArgs, aspectOf). */

/** Decipher scans are 357 x 497 (sites lie on their side): width / height until an image has loaded. */
export const PORTRAIT = 357 / 497;
export const LANDSCAPE = 497 / 357;

export interface ErrataImages {
  /** The original, or null when there is none to show. */
  readonly before: string | null;
  readonly after: string;
}

/**
 * The PC version's image, and the original's: for an errata, its base card's image without the errata image the
 * blueprint may have; for a V-set revision, the same image name with `.0` (and a revised image's "E" for errata as the
 * set's "S": LOTR-ENV2E005.1 → LOTR-ENV2S005.0).
 */
export function errataImages(tables: CardImageTables, entry: ErrataEntry): ErrataImages {
  const after = getImageUrl(tables, entry.id);
  if (entry.kind === 'revision') {
    const first = after
      .replace(/\.\d+_card\.jpg$/, '.0_card.jpg')
      .replace(/(LOTR-ENV\d+)E(\d+\.0_card\.jpg)$/, '$1S$2');
    return { before: first !== after ? first : null, after };
  }
  return { before: getImageUrl(tables, entry.base, false, true), after };
}

/** What the card preview shows for one side: the blueprint, the exact image and the title; null with no image. */
export interface ErrataPreview {
  readonly blueprintId: string;
  readonly imageUrl: string;
  readonly title: string;
}

export function errataPreview(
  tables: CardImageTables,
  entry: ErrataEntry,
  side: 'before' | 'after',
): ErrataPreview | null {
  const urls = errataImages(tables, entry);
  const before = side === 'before';
  const url = before ? urls.before : urls.after;
  if (!url) {
    return null;
  }
  return { blueprintId: before ? entry.base : entry.id, imageUrl: url, title: previewTitle(entry.name, before) };
}

/** Width / height of a card's image until it has loaded. */
export function aspectOf(entry: ErrataEntry | null): number {
  return entry?.type === 'Site' ? LANDSCAPE : PORTRAIT;
}
