import { inject, Injectable } from '@angular/core';
import {
  getBackSideUrl,
  getFixedImage,
  getImageUrl,
  isAlternateImage,
  isErrata,
  isFoil,
  isHorizontal,
  isIncomplete,
  isPack,
  isPlaceholder,
  isTengwar,
  parseBlueprint,
  RULES_IMAGE,
  stripBlueprintId,
} from './card-blueprint';
import { LegacyCardDataService } from './legacy-card-data.service';

/** How to display one card: what the legacy `new Card(blueprintId, ...)` worked out. */
export interface CardImage {
  /** As given, including markers such as "*" (foil). */
  blueprintId: string;
  /** Without markers; with an "ai" suffix when an alternate image exists. */
  bareBlueprint: string;
  foil: boolean;
  tengwar: boolean;
  imageUrl: string;
  backSideImageUrl: string;
  /** In-development card: shown with its text rather than an image. */
  incomplete: boolean;
  /** Shown with the errata marker (errata sets 50-89, or a card with a remade errata image). */
  errata: boolean;
  /** Landscape card (e.g. a site), unless the zone always shows cards upright. */
  horizontal: boolean;
  pack: boolean;
  /** Set 404 "Future Prize": visible in collections but never addable to a deck. */
  placeholder: boolean;
  /** Whether the card has a wiki page (wikiUrl). */
  hasWiki: boolean;
  wikiUrl: string;
}

/** Resolves card images from blueprint IDs, using the legacy image tables. */
@Injectable({ providedIn: 'root' })
export class CardImageService {
  private readonly data = inject(LegacyCardDataService);
  private readonly cache = new Map<string, CardImage>();

  /** Mirrors the legacy Card constructor. `zone` only affects `horizontal`. */
  resolve(blueprintId: string, zone?: string | null): CardImage {
    const key = `${blueprintId}\u0000${zone ?? ''}`;
    let image = this.cache.get(key);
    if (!image) {
      image = this.compute(blueprintId, zone);
      this.cache.set(key, image);
    }
    return image;
  }

  imageUrl(blueprintId: string): string {
    return this.resolve(blueprintId).imageUrl;
  }

  /** Forgets resolved images (e.g. after the tables were reloaded). */
  clearCache(): void {
    this.cache.clear();
  }

  private compute(blueprintId: string, zone: string | null | undefined): CardImage {
    const tables = this.data.tables;
    let bareBlueprint = stripBlueprintId(blueprintId);
    if (isAlternateImage(blueprintId) && Object.hasOwn(tables.fixedImages, bareBlueprint + 'ai')) {
      bareBlueprint += 'ai';
    }
    const tengwar = isTengwar(blueprintId);
    const { setNo } = parseBlueprint(blueprintId);

    // The legacy wiki link is built from the un-stripped ID, without errata
    const wikiImage = getImageUrl(tables, blueprintId, false, true);
    const wikiName = wikiImage.substring(wikiImage.lastIndexOf('/') + 1, wikiImage.length - 4);

    const common = {
      blueprintId,
      bareBlueprint,
      foil: isFoil(blueprintId),
      tengwar,
      pack: isPack(tables, blueprintId),
      placeholder: isPlaceholder(blueprintId),
      hasWiki: getFixedImage(tables, bareBlueprint) === null && !isPack(tables, bareBlueprint),
      wikiUrl: `http://wiki.lotrtcgpc.net/wiki/${wikiName}`,
    };

    if (bareBlueprint === 'rules') {
      return {
        ...common,
        imageUrl: RULES_IMAGE,
        backSideImageUrl: '',
        incomplete: false,
        errata: false,
        horizontal: false,
      };
    }

    // Tengwar cards are looked up (and cached by the legacy code) under their "T" ID
    const imageId = tengwar ? bareBlueprint + 'T' : bareBlueprint;
    const errataSet = setNo >= 50 && setNo <= 89;
    return {
      ...common,
      imageUrl: getImageUrl(tables, imageId, tengwar),
      backSideImageUrl: getBackSideUrl(tables),
      incomplete: isIncomplete(imageId),
      errata: errataSet || isErrata(imageId),
      horizontal: isHorizontal(bareBlueprint, zone),
    };
  }
}
