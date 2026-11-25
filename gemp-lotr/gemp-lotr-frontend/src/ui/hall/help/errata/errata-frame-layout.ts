/**
 * Fits a frame of the PC Errata viewer to the viewer, so the viewer never scrolls inside (pcErrataUi.js layout,
 * layoutCompact, fitBottom, fitSide, placeReadout, layoutFull, sizeCard). It measures the frame's elements and sets
 * the sizes it chose as custom properties, which the frame's stylesheet applies:
 *
 * - on the frame: `data-readout` = bottom / side (compact), `data-column` (enlarged sites one above the other),
 *   `data-clipped` (the text did not fit even small);
 * - on `.gd-errata-frame__note`: `--gd-errata-note-font`, `--gd-errata-note-width`, `--gd-errata-note-max` (px);
 * - on `.gd-errata-frame__flip`: `--gd-errata-flip-width`, `--gd-errata-flip-height`;
 * - on each `.gd-errata-card` (`data-side`): `--gd-errata-card-width`, `--gd-errata-card-height`, `--gd-errata-card-border`;
 * - on `.gd-errata-frame__pair` (enlarged): `--gd-errata-pair-width` (column), and on its arrow
 *   `--gd-errata-arrow-height` (the cards' height in a row).
 *
 * A card's image, once loaded, puts its own shape on its `.gd-errata-card` (`data-aspect`, `data-natural-height`).
 */

/** No bigger than a quarter over the scan's own size: past that it only gets blurry (PcErrataUI.MAX_UPSCALE). */
export const MAX_UPSCALE = 1.25;
/** Below the card, unless that leaves the card less than this tall or cuts the text off (PcErrataUI.BOTTOM_MIN_CARD). */
export const BOTTOM_MIN_CARD = 160;

const BOTTOM_FONTS = [13, 12.5, 12, 11.5, 11, 10.5, 10];
const SIDE_TRIES: readonly (readonly [number, number])[] = [
  [13, 1],
  [13, 0.88],
  [12, 1],
  [12, 0.88],
  [11.5, 0.8],
  [11, 0.72],
  [10.5, 0.64],
  [10, 0.56],
];
const FULL_FONTS = [15, 14, 13, 12];

function px(value: number): string {
  return `${value}px`;
}

function rowGap(element: Element | null): number {
  if (!element) {
    return 0;
  }
  return parseFloat(getComputedStyle(element).rowGap) || 0;
}

/** The outer height with margins (jQuery's outerHeight(true)). */
function outerHeight(element: HTMLElement | null, margins = false): number {
  if (!element) {
    return 0;
  }
  const height = element.getBoundingClientRect().height;
  if (!margins) {
    return height;
  }
  const style = getComputedStyle(element);
  return height + (parseFloat(style.marginTop) || 0) + (parseFloat(style.marginBottom) || 0);
}

interface Fit {
  side: boolean;
  font: number;
  cardHeight: number;
  noteWidth?: number;
  noteMax: number;
  clipped: boolean;
}

export class FrameLayout {
  constructor(
    private readonly frame: HTMLElement,
    /** Width / height of a card until its image has loaded (sites lie on their side). */
    private readonly defaultAspect: number,
  ) {}

  private find(selector: string): HTMLElement | null {
    return this.frame.querySelector<HTMLElement>(selector);
  }

  private card(side: 'before' | 'after'): HTMLElement | null {
    return this.find(`.gd-errata-card[data-side='${side}']`);
  }

  private aspect(side: 'before' | 'after'): number {
    const value = parseFloat(this.card(side)?.dataset['aspect'] ?? '');
    return value > 0 ? value : this.defaultAspect;
  }

  /** The scans' own height: the taller of the two images; 497 (a Decipher portrait scan) until they load. */
  private naturalHeight(): number {
    let tallest = 0;
    for (const card of Array.from(this.frame.querySelectorAll<HTMLElement>('.gd-errata-card'))) {
      tallest = Math.max(tallest, parseFloat(card.dataset['naturalHeight'] ?? '') || 0);
    }
    if (tallest) {
      return tallest;
    }
    return this.defaultAspect > 1 ? 357 : 497;
  }

  private sizeCard(side: 'before' | 'after', width: number, height: number): void {
    const card = this.card(side);
    if (!card) {
      return;
    }
    const w = Math.max(1, Math.round(width));
    const h = Math.max(1, Math.round(height));
    card.style.setProperty('--gd-errata-card-width', px(w));
    card.style.setProperty('--gd-errata-card-height', px(h));
    // the game's border: a thirtieth of the longer side (CardGroup's layoutCardElem)
    card.style.setProperty('--gd-errata-card-border', px(Math.floor(Math.max(w, h) / 30)));
  }

  /** Fits the frame to its body's size; nothing while it isn't laid out (hidden). */
  layout(): void {
    const body = this.find('.gd-errata-frame__body');
    const width = body?.clientWidth ?? 0;
    const height = body?.clientHeight ?? 0;
    if (!body || !width || !height) {
      return;
    }
    if (this.frame.dataset['layout'] === 'full') {
      this.layoutFull(body, width, height);
    } else {
      this.layoutCompact(body, width, height);
    }
  }

  /**
   * The readout across the bottom at its own height, the title and card centred above it, the card as large as that
   * allows. A short viewer can't fit both that way: then the card goes on the left and the readout beside it.
   */
  private layoutCompact(body: HTMLElement, width: number, height: number): void {
    const note = this.find('.gd-errata-frame__note');
    const flip = this.find('.gd-errata-frame__flip');
    const cardbox = this.find('.gd-errata-frame__cardbox');
    if (!note || !flip) {
      return;
    }
    const aspectAfter = this.aspect('after');
    const aspectBefore = this.aspect('before');
    const aspect = Math.max(aspectAfter, aspectBefore);
    const natural = this.naturalHeight() * MAX_UPSCALE;
    const bottom = this.fitBottom(body, note, width, height, aspect, natural);
    let chosen = bottom;
    if (bottom.clipped || bottom.cardHeight < BOTTOM_MIN_CARD) {
      const side = this.fitSide(note, width, height, aspect, natural);
      // (only for a clearly bigger card: below it reads better, sites especially)
      if (side.cardHeight > bottom.cardHeight * 1.1 && (!side.clipped || bottom.clipped)) {
        chosen = side;
      }
    }
    this.frame.dataset['readout'] = chosen.side ? 'side' : 'bottom';
    note.style.setProperty('--gd-errata-note-font', px(chosen.font));
    if (chosen.side && chosen.noteWidth !== undefined) {
      note.style.setProperty('--gd-errata-note-width', px(chosen.noteWidth));
    } else {
      note.style.removeProperty('--gd-errata-note-width');
    }
    note.style.setProperty('--gd-errata-note-max', px(chosen.noteMax));
    const cardHeight = Math.floor(chosen.cardHeight);
    flip.style.setProperty('--gd-errata-flip-width', px(Math.round(cardHeight * aspect)));
    cardbox?.style.setProperty('--gd-errata-flip-height', px(cardHeight));
    this.sizeCard('after', cardHeight * aspectAfter, cardHeight);
    this.sizeCard('before', cardHeight * aspectBefore, cardHeight);
    this.setFlag('clipped', chosen.clipped);
  }

  private setFlag(name: 'clipped' | 'column', on: boolean): void {
    this.frame.toggleAttribute(`data-${name}`, on);
  }

  /** The head shown for the readout's place (the frame draws it in both, and its stylesheet shows one). */
  private head(): HTMLElement | null {
    const side = this.frame.dataset['readout'] === 'side';
    return this.find(
      side ? '.gd-errata-frame__note .gd-errata-frame__head' : '.gd-errata-frame__stage .gd-errata-frame__head',
    );
  }

  private fitBottom(
    body: HTMLElement,
    note: HTMLElement,
    width: number,
    height: number,
    aspect: number,
    natural: number,
  ): Fit {
    this.frame.dataset['readout'] = 'bottom';
    note.style.removeProperty('--gd-errata-note-width');
    note.style.setProperty('--gd-errata-note-max', 'none');
    const stage = this.find('.gd-errata-frame__stage');
    const gap = rowGap(body);
    const stageGap = rowGap(stage);
    const headHeight = outerHeight(this.head());
    // the card's height with the whole body to itself (no readout)
    const alone = Math.min(natural, width / aspect, height - headHeight - stageGap);
    let font = BOTTOM_FONTS[BOTTOM_FONTS.length - 1] ?? 10;
    for (const size of BOTTOM_FONTS) {
      note.style.setProperty('--gd-errata-note-font', px(size));
      if (Math.min(alone, height - note.scrollHeight - gap - headHeight - stageGap) >= alone * 0.66 - 0.5) {
        font = size;
        break;
      }
    }
    note.style.setProperty('--gd-errata-note-font', px(font));
    const noteHeight = note.scrollHeight;
    // the readout gives way (clipped: "Enlarge to read it all") below a small card
    const cardHeight = Math.max(
      Math.min(alone, 110),
      Math.min(alone, height - noteHeight - gap - headHeight - stageGap),
    );
    const noteMax = Math.max(0, Math.floor(height - gap - headHeight - stageGap - cardHeight));
    return { side: false, font, cardHeight, noteMax, clipped: noteHeight > noteMax + 1 };
  }

  /** The card on the left, as tall as the viewer (at most half its width), the readout beside it. */
  private fitSide(note: HTMLElement, width: number, height: number, aspect: number, natural: number): Fit {
    this.frame.dataset['readout'] = 'side';
    note.style.setProperty('--gd-errata-note-max', 'none');
    const gap = 12;
    const maxWidth = Math.max(
      60,
      Math.min(height * aspect, natural * aspect, width * (aspect > 1 ? 0.56 : 0.5), width - 170 - gap),
    );
    let chosen: readonly [number, number] | null = null;
    for (const attempt of SIDE_TRIES) {
      note.style.setProperty('--gd-errata-note-font', px(attempt[0]));
      note.style.setProperty('--gd-errata-note-width', px(Math.floor(width - maxWidth * attempt[1] - gap)));
      if (note.scrollHeight <= height + 1) {
        chosen = attempt;
        break;
      }
    }
    const clipped = chosen === null;
    const [font, share] = chosen ?? SIDE_TRIES[SIDE_TRIES.length - 1] ?? [10, 0.56];
    const cardWidth = maxWidth * share;
    return {
      side: true,
      font,
      cardHeight: cardWidth / aspect,
      noteWidth: Math.floor(width - cardWidth - gap),
      noteMax: Math.floor(height),
      clipped,
    };
  }

  /**
   * Enlarged: both cards, then the text across the width below them (Old under Original, New under PC errata). Sites
   * go one above the other with the text beside them when that shows them bigger.
   */
  private layoutFull(body: HTMLElement, width: number, height: number): void {
    const note = this.find('.gd-errata-frame__note');
    const pair = this.find('.gd-errata-frame__pair');
    const arrow = this.find('.gd-errata-frame__arrow');
    if (!note || !pair || !arrow) {
      return;
    }
    const figure = this.find('.gd-errata-frame__figure');
    const captionHeight = (outerHeight(this.find('.gd-errata-frame__figure figcaption'), true) || 18) + rowGap(figure);
    const bodyGap = rowGap(body);
    const aspectBefore = this.aspect('before');
    const aspectAfter = this.aspect('after');
    const aspect = Math.max(aspectBefore, aspectAfter);
    const gap = 14;
    const arrowSize = 34;

    const measure = (font: number, textWidth: number): number => {
      note.style.setProperty('--gd-errata-note-font', px(font));
      note.style.setProperty('--gd-errata-note-width', px(textWidth));
      return note.scrollHeight;
    };
    const sideBySide = (font: number) => {
      const textHeight = measure(font, width);
      const byWidth = (width - arrowSize - 2 * gap) / 2 / aspect;
      const byHeight = height - textHeight - captionHeight - bodyGap;
      return { font, cardHeight: Math.min(byWidth, byHeight), byWidth, fits: byHeight > 0 };
    };
    const stacked = (font: number) => {
      const cardHeight = (height - 2 * captionHeight - arrowSize) / 2;
      const cardWidth = Math.min(cardHeight * aspect, width * 0.55);
      const textHeight = measure(font, Math.floor(width - cardWidth - bodyGap));
      return { font, cardHeight: cardWidth / aspect, cardWidth, fits: textHeight <= height + 1 };
    };

    let best: { font: number; cardHeight: number; cardWidth?: number; column: boolean } | null = null;
    for (const font of FULL_FONTS) {
      const option = sideBySide(font);
      // big enough: the cards take nearly what the width alone would allow them, or 62% of the height
      if (option.fits && option.cardHeight >= Math.min(option.byWidth, height * 0.62) * 0.98) {
        best = { font, cardHeight: option.cardHeight, column: false };
        break;
      }
    }
    if (!best) {
      const smallest = FULL_FONTS[FULL_FONTS.length - 1] ?? 12;
      const option = sideBySide(smallest);
      best = { font: smallest, cardHeight: option.cardHeight > 40 ? option.cardHeight : 40, column: false };
    }
    if (aspect > 1) {
      for (const font of FULL_FONTS) {
        const option = stacked(font);
        if (option.fits) {
          if (option.cardHeight > best.cardHeight * 1.04) {
            best = { font, cardHeight: option.cardHeight, cardWidth: option.cardWidth, column: true };
          }
          break;
        }
      }
    }

    this.setFlag('column', best.column);
    const natural = this.naturalHeight() * MAX_UPSCALE;
    if (best.cardHeight > natural) {
      if (best.column && best.cardWidth !== undefined) {
        best.cardWidth = (best.cardWidth * natural) / best.cardHeight;
      }
      best.cardHeight = natural;
    }
    const cardHeight = Math.max(20, Math.floor(best.cardHeight));
    note.style.setProperty('--gd-errata-note-font', px(best.font));
    if (best.column) {
      const cardWidth = Math.floor(best.cardWidth ?? cardHeight * aspect);
      note.style.setProperty('--gd-errata-note-width', px(Math.floor(width - cardWidth - bodyGap)));
      pair.style.setProperty('--gd-errata-pair-width', px(cardWidth));
      arrow.style.setProperty('--gd-errata-arrow-height', px(arrowSize));
    } else {
      note.style.setProperty('--gd-errata-note-width', px(width));
      pair.style.removeProperty('--gd-errata-pair-width');
      arrow.style.setProperty('--gd-errata-arrow-height', px(cardHeight));
    }
    this.sizeCard('before', cardHeight * aspectBefore, cardHeight);
    this.sizeCard('after', cardHeight * aspectAfter, cardHeight);
    this.setFlag('clipped', body.scrollHeight > body.clientHeight + 1);
  }
}
