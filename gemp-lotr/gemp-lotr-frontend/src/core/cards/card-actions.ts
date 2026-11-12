import { Injectable } from '@angular/core';

/**
 * What using a card means on a page (a click, Enter or Space on it). Showing its details is the same everywhere
 * (CardInfoService); this is the part each page decides. A page gets back what it put in `CardView.ref`, so its
 * implementation can narrow `ref` to its own type.
 *
 * The page provides the implementation, with provideCardInteractions.
 */
@Injectable()
export abstract class CardActions {
  abstract activate(ref: unknown): void;
}
