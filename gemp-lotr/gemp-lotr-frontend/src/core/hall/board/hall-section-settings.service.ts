import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { LegacyCookies } from '../../platform/legacy-cookies';
import { HallBoard } from './hall-board.service';
import { HallSectionId } from './hall-board-models';
import { orIfEmpty } from '../json-values';

/** The cookie the sections' open / closed state is kept in, as "|"-separated "1" / "0" flags (hallUi.js). */
export const HALL_SETTINGS_COOKIE = 'hallSettings';
export const HALL_SETTINGS_DEFAULT = '1|1|0|0|0|0|0|0';
/** Cookie slot -> section (null: a retired slot, for the solo and queue-spawner sections, kept so cookies line up). */
export const HALL_SETTINGS_SLOTS: readonly (HallSectionId | null)[] = [
  'waiting',
  'playing',
  'finished',
  'wc',
  'scheduled',
  null,
  'recurring',
  null,
];

/** The flags of the cookie (readHallSettings), the default when there is none. */
export function readHallSettings(raw: string | null): string[] {
  return orIfEmpty(raw, HALL_SETTINGS_DEFAULT).split('|');
}

/** Whether a section opens, by its slot. */
export function sectionOpen(flags: readonly string[], section: HallSectionId): boolean {
  return flags[HALL_SETTINGS_SLOTS.indexOf(section)] === '1';
}

/** The cookie with the given sections' states written into their slots, the others kept (updateHallSettings). */
export function writeHallSettings(flags: readonly string[], states: Partial<Record<HallSectionId, boolean>>): string {
  const next = [...flags];
  HALL_SETTINGS_SLOTS.forEach((section, slot) => {
    if (section !== null && states[section] !== undefined) {
      next[slot] = states[section] ? '1' : '0';
    }
  });
  return next.join('|');
}

/**
 * Which sections are open (hallUi.js initTable / updateHallSettings). The state is read from the cookie when the
 * hall opens, and for World Championship Queues and Scheduled Events again each time they reappear (the legacy hall
 * built those sections anew). A toggle writes the state of every section on the page into the cookie.
 */
@Injectable()
export class HallSectionSettings {
  private readonly cookies = inject(LegacyCookies);
  private readonly board = inject(HallBoard);

  private readonly open_ = signal<Readonly<Record<HallSectionId, boolean>>>(this.fromCookie());
  readonly open = this.open_.asReadonly();

  constructor() {
    let shown = new Set(this.board.shownSections());
    const changes = this.board.changes$.subscribe(() => {
      const now = new Set(this.board.shownSections());
      const flags = this.flags();
      for (const id of ['wc', 'scheduled'] as const) {
        if (now.has(id) && !shown.has(id)) {
          this.open_.update((open) => ({ ...open, [id]: sectionOpen(flags, id) }));
        }
      }
      shown = now;
    });
    inject(DestroyRef).onDestroy(() => changes.unsubscribe());
  }

  isOpen(section: HallSectionId): boolean {
    return this.open_()[section];
  }

  toggle(section: HallSectionId): void {
    this.open_.update((open) => ({ ...open, [section]: !open[section] }));
    const open = this.open_();
    const states: Partial<Record<HallSectionId, boolean>> = {};
    for (const id of this.board.shownSections()) {
      states[id] = open[id];
    }
    this.cookies.write(HALL_SETTINGS_COOKIE, writeHallSettings(this.flags(), states));
  }

  private flags(): string[] {
    return readHallSettings(this.cookies.read(HALL_SETTINGS_COOKIE));
  }

  private fromCookie(): Record<HallSectionId, boolean> {
    const flags = this.flags();
    return {
      waiting: sectionOpen(flags, 'waiting'),
      playing: sectionOpen(flags, 'playing'),
      finished: sectionOpen(flags, 'finished'),
      wc: sectionOpen(flags, 'wc'),
      scheduled: sectionOpen(flags, 'scheduled'),
      recurring: sectionOpen(flags, 'recurring'),
    };
  }
}
