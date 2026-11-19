import { computed, inject, Injectable, signal } from '@angular/core';
import { CompactLayout } from '../../platform/compact-layout.service';
import { SettingCookies } from '../../platform/setting-cookies';

export type PhoneDevice = 'phone' | 'sideways' | 'tablet';

/** The cookies of the phone layout's own settings (under /gemp-lotr, as the theme's gempGameTheme). */
export const PHONE_COOKIES = {
  /** The divider's place on each device: the hand's height (px) or "hidden", e.g. `phone:170|sideways:hidden`. */
  split: 'gempGameSplit',
  /** The table's card size, in % of the device's own (70 to 150). */
  cardSize: 'gempGameCardSize',
  /** "Your move" makes the phone buzz / plays a sound (default on, the owner 2026-11-16). */
  moveVibrate: 'gempGameMoveVibrate',
  moveSound: 'gempGameMoveSound',
  /** How a deck or other cards to choose scroll in the hand's place: sideways (default) or down, in rows. */
  choicesScroll: 'gempGameChoicesScroll',
} as const;

/**
 * How cards offered from off the table (a deck, a pile) scroll in the hand's place (the owner, 2026-11-19): sideways
 * in one row, or wrapped into rows scrolled down. The player's own hand always scrolls sideways.
 */
export type ChoicesScroll = 'sideways' | 'down';

/** The hand's height at first, on each device (px); held sideways it is a panel in the right half, about half high. */
export const DEFAULT_HAND: Readonly<Record<PhoneDevice, number>> = { phone: 170, sideways: 150, tablet: 180 };
/** The table's cards' height on each device at 100% (px): one readable size, never shrunk to fit (the owner). */
export const CARD_HEIGHT: Readonly<Record<PhoneDevice, number>> = { phone: 100, sideways: 80, tablet: 130 };
export const CARD_SIZE_MIN = 70;
export const CARD_SIZE_MAX = 150;
/** The least height a shown hand keeps (px); dragged lower than HIDE_BELOW it hides. */
export const HAND_MIN = 76;
export const HIDE_BELOW = 50;

/** The divider's places as kept in the cookie: per device, the hand's height or hidden. */
export function parseSplit(text: string | null): Partial<Record<PhoneDevice, number | 'hidden'>> {
  const result: Partial<Record<PhoneDevice, number | 'hidden'>> = {};
  for (const part of (text ?? '').split('|')) {
    const [device, value] = part.split(':');
    if (device !== 'phone' && device !== 'sideways' && device !== 'tablet') continue;
    if (value === 'hidden') result[device] = 'hidden';
    else if (value && Number.isFinite(Number(value)) && Number(value) > 0) result[device] = Math.round(Number(value));
  }
  return result;
}

export function formatSplit(split: Partial<Record<PhoneDevice, number | 'hidden'>>): string {
  return (['phone', 'sideways', 'tablet'] as const)
    .flatMap((device) => (split[device] === undefined ? [] : [`${device}:${split[device]}`]))
    .join('|');
}

/**
 * The phone layout's own settings (G7 of trymarz/gemp-lotr-private#27): where the divider is on this device (per
 * device and orientation, the hand hidden when it was dragged to the bottom), the table's card size, and the "Your
 * move" alerts. Kept in cookies. Page-scoped.
 */
@Injectable()
export class PhoneSettings {
  private readonly cookies = inject(SettingCookies);
  private readonly split = signal(parseSplit(this.cookies.read(PHONE_COOKIES.split)));

  /** The device the compact layout is on, or null outside it. */
  readonly device = inject(CompactLayout).gameDevice;
  private readonly tallSideways = inject(CompactLayout).tallSideways;
  /** The table's card size (%). */
  readonly cardSize = signal(clampSize(Number(this.cookies.read(PHONE_COOKIES.cardSize) ?? '100')));
  readonly moveVibrate = signal(this.cookies.read(PHONE_COOKIES.moveVibrate) !== 'false');
  readonly moveSound = signal(this.cookies.read(PHONE_COOKIES.moveSound) !== 'false');
  readonly choicesScroll = signal<ChoicesScroll>(
    this.cookies.read(PHONE_COOKIES.choicesScroll) === 'down' ? 'down' : 'sideways',
  );

  /** The hand's height on this device (px), or "hidden". */
  readonly hand = computed<number | 'hidden'>(() => {
    const device = this.device() ?? 'phone';
    return this.split()[device] ?? this.defaultHand();
  });
  readonly handHidden = computed(() => this.hand() === 'hidden');
  /** The hand's height at first on this device (px). */
  readonly defaultHand = computed(() =>
    this.tallSideways() ? DEFAULT_HAND.tablet : DEFAULT_HAND[this.device() ?? 'phone'],
  );
  /** The table's cards' height (px). */
  readonly cardHeight = computed(
    () => ((this.tallSideways() ? CARD_HEIGHT.tablet : CARD_HEIGHT[this.device() ?? 'phone']) * this.cardSize()) / 100,
  );

  /** The divider dropped: the hand this high (px), or hidden. */
  setHand(height: number | 'hidden'): void {
    const device = this.device() ?? 'phone';
    const value = height === 'hidden' ? height : Math.round(height);
    this.split.update((split) => ({ ...split, [device]: value }));
    this.cookies.write(PHONE_COOKIES.split, formatSplit(this.split()));
  }

  /** The hand shown again at its last height (an offer of cards needs it). */
  showHand(): void {
    if (this.handHidden()) this.setHand(this.defaultHand());
  }

  setCardSize(percent: number): void {
    this.cardSize.set(clampSize(percent));
    this.cookies.write(PHONE_COOKIES.cardSize, String(this.cardSize()));
  }

  setMoveVibrate(value: boolean): void {
    this.moveVibrate.set(value);
    this.cookies.write(PHONE_COOKIES.moveVibrate, String(value));
  }

  setChoicesScroll(value: ChoicesScroll): void {
    this.choicesScroll.set(value);
    this.cookies.write(PHONE_COOKIES.choicesScroll, value);
  }

  setMoveSound(value: boolean): void {
    this.moveSound.set(value);
    this.cookies.write(PHONE_COOKIES.moveSound, String(value));
  }
}

function clampSize(percent: number): number {
  if (!Number.isFinite(percent)) return 100;
  return Math.max(CARD_SIZE_MIN, Math.min(CARD_SIZE_MAX, Math.round(percent / 5) * 5));
}
