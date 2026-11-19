import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { foilOverlayImage, readFoilPresentation } from '../cards/foil-presentation';

/** The player's foil setting, from the cookie the game settings write. */
@Injectable({ providedIn: 'root' })
export class FoilSetting {
  private readonly document = inject(DOCUMENT);

  /** The image drawn over a foil card now, or null when foils are shown plain. */
  overlayImage(): string | null {
    return foilOverlayImage(readFoilPresentation(this.document.cookie));
  }
}
