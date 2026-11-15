import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/** The sounds a page can play, by name: the site's own files (the legacy game page's). */
export const PAGE_SOUNDS = {
  awaitAction: '/gemp-lotr/src/assets/awaiting_decision.mp3',
  timerAlert: '/gemp-lotr/src/assets/timer_alert.mp3',
} as const;

export type PageSound = keyof typeof PAGE_SOUNDS;

/**
 * What a page does to get the player's attention, and where it can send them: the tab's title, sounds, reloading or
 * leaving the page. The browser, behind a service, so core can ask for these.
 */
@Injectable({ providedIn: 'root' })
export class PageCues {
  private readonly document = inject(DOCUMENT);
  private readonly audio = new Map<PageSound, HTMLAudioElement>();

  setTitle(title: string): void {
    this.document.title = title;
  }

  /** Whether the player is looking at another window or tab. */
  away(): boolean {
    return !this.document.hasFocus() || this.document.hidden;
  }

  /**
   * Plays a sound from its start. A browser may refuse before the player has used the page: that is no error. jsdom
   * has no audio: nothing happens there.
   */
  play(sound: PageSound): void {
    const window = this.document.defaultView;
    if (!window || typeof window.Audio !== 'function') return;
    let audio = this.audio.get(sound);
    if (!audio) {
      audio = new window.Audio(PAGE_SOUNDS[sound]);
      this.audio.set(sound, audio);
    }
    try {
      audio.currentTime = 0;
      audio.play().catch(() => undefined);
    } catch {
      // Not allowed yet, or no audio
    }
  }

  reload(): void {
    this.document.defaultView?.location.reload();
  }

  go(address: string): void {
    const location = this.document.defaultView?.location;
    if (location) location.href = address;
  }
}
