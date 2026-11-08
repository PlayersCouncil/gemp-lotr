import { CardImage } from './card-image.service';

/** What a large card view shows (CardDisplay.reloadFromCard). */
export interface CardDisplayData {
  image: string;
  /** The card back, shown when the card is flipped; null when it can't be flipped. */
  back: string | null;
  horizontal: boolean;
  foil: boolean;
  /** RTMD meta-site modifier drawn over the lower part. */
  overlayImage: string | null;
}

/**
 * The large card view from a resolved card image. The standard card back makes every card flippable, as before.
 * `ignoreTengwar` shows a tengwar card's normal image (the hover zoom did that).
 */
export function cardDisplayData(
  image: CardImage,
  overlayImage: string | null,
  ignoreTengwar = false,
  baseImage?: string,
): CardDisplayData {
  const back = image.backSideImageUrl;
  const flippable = !!back && !back.includes('darkcardback') && !back.includes('lightcardback');
  return {
    image: ignoreTengwar && image.tengwar && baseImage ? baseImage : image.imageUrl,
    back: flippable ? back : null,
    horizontal: image.horizontal,
    foil: image.foil,
    overlayImage,
  };
}
