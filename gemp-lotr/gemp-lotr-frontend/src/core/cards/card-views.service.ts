import { inject, Injectable } from '@angular/core';
import { FoilSetting } from '../platform/foil-setting';
import { CardImageService } from './card-image.service';
import { CardRole, CardView } from './card-view';

/** The card to show. Anything left out takes what the card itself says, as Card.CreateCardDiv did. */
export interface CardViewSpec<Ref> {
  /** What the card stands for on its page (CardView.ref). */
  ref: Ref;
  blueprintId: string;
  /** The zone it is shown in (it decides e.g. whether a site is drawn landscape). */
  zone: string | null;
  /** Default `card`. */
  role?: CardRole;
  /** The number shown on the card; default none. */
  count?: number | null;
  /** Never drawn with the errata marker, whatever the blueprint says. */
  noErrata?: boolean;
  /** Never drawn with the foil layer, whatever the blueprint says. */
  noFoil?: boolean;
  /** Default: a border unless it is a pack. */
  border?: boolean;
  /** An RTMD meta-site's modifier, drawn over the lower part of the card. */
  overlayBlueprintId?: string | null;
  /** See CardView.wholePixels. */
  wholePixels?: boolean;
}

/**
 * Builds the CardView of every card a page shows, from its blueprint. Per page (provideCardInteractions), since the foil
 * setting is read when the page opens.
 */
@Injectable()
export class CardViews {
  private readonly images = inject(CardImageService);
  private readonly foilImage = inject(FoilSetting).overlayImage();

  create<Ref>(spec: CardViewSpec<Ref>): CardView<Ref> {
    const image = this.images.resolve(spec.blueprintId, spec.zone);
    return {
      blueprintId: spec.blueprintId,
      zone: spec.zone,
      ref: spec.ref,
      role: spec.role ?? 'card',
      imageUrl: image.imageUrl,
      horizontal: image.horizontal,
      errata: !spec.noErrata && image.errata,
      foilImage: !spec.noFoil && image.foil ? this.foilImage : null,
      border: spec.border ?? !image.pack,
      count: spec.count ?? null,
      overlayImage: spec.overlayBlueprintId ? this.images.imageUrl(spec.overlayBlueprintId) : null,
      wholePixels: spec.wholePixels ?? false,
    };
  }
}
