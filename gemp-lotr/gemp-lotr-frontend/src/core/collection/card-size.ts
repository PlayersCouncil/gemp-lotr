import { inject, Injectable, signal } from '@angular/core';
import { SettingCookies } from '../platform/setting-cookies';

/** The collection's card size in the compact layout, in % of the layout's own size: at first, at least, at most. */
export const CARD_SIZE_DEFAULT = 100;
export const CARD_SIZE_MIN = 50;
export const CARD_SIZE_MAX = 200;
export const CARD_SIZE_STEP = 10;

/** Remembers the card size on this device: the deck builder's own setting. */
export const CARD_SIZE_COOKIE = 'gempDeckbuilderCardSize';

/** The remembered size, kept within its limits and on a step; the default when there is none (or it isn't a number). */
export function parseCardSize(value: string | null): number {
  const size = Number.parseFloat(value ?? '');
  if (!Number.isFinite(size)) {
    return CARD_SIZE_DEFAULT;
  }
  const stepped = Math.round(size / CARD_SIZE_STEP) * CARD_SIZE_STEP;
  return Math.min(CARD_SIZE_MAX, Math.max(CARD_SIZE_MIN, stepped));
}

/**
 * How large the collection's cards are in the compact layout, where the pager's slider sets it (a phone's cards keep
 * their size and the pane scrolls, so the number of cards on a page isn't what the slider changes there). Remembered on
 * the device.
 */
@Injectable()
export class CardSize {
  private readonly cookies = inject(SettingCookies);

  readonly percent = signal(parseCardSize(this.cookies.read(CARD_SIZE_COOKIE)));

  set(percent: number): void {
    this.percent.set(parseCardSize(String(percent)));
    this.cookies.write(CARD_SIZE_COOKIE, String(this.percent()));
  }
}
