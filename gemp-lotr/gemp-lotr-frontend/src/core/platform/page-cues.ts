import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';

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
  /** Whether the page fills the screen (the browser's full screen, which the player may also leave by itself). */
  readonly fullscreen = signal(false);

  constructor() {
    this.fullscreen.set(this.document.fullscreenElement != null);
    this.document.addEventListener('fullscreenchange', () =>
      this.fullscreen.set(this.document.fullscreenElement != null),
    );
  }

  /** Whether the browser lets a page fill the screen (an iPhone's doesn't: there only videos may). */
  fullscreenAvailable(): boolean {
    return this.document.fullscreenEnabled;
  }

  /** The page fills the screen, or stops filling it. A browser that refuses (no user gesture) is no error. */
  toggleFullscreen(): void {
    const request = this.document.fullscreenElement
      ? this.document.exitFullscreen()
      : this.document.documentElement.requestFullscreen();
    request.catch(() => undefined);
  }

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

  /** A short buzz on a device that can (a phone); nothing elsewhere. */
  vibrate(ms = 200): void {
    const navigator = this.document.defaultView?.navigator;
    if (navigator && typeof navigator.vibrate === 'function') {
      try {
        navigator.vibrate(ms);
      } catch {
        // Not allowed before the player has used the page
      }
    }
  }

  /**
   * Once the page's fonts are loaded (at once where they are, or the browser can't tell): text set in a font still
   * coming is laid out again when it comes, so a scroll restored before would land elsewhere.
   */
  fontsReady(): Promise<void> {
    const fonts = this.document.fonts as FontFaceSet | undefined;
    return fonts?.ready ? fonts.ready.then(() => undefined) : Promise.resolve();
  }

  reload(): void {
    this.document.defaultView?.location.reload();
  }

  /** Opens `address` in place of this page (no new history entry): a new page, nothing of this one's view kept. */
  replace(address: string): void {
    this.document.defaultView?.location.replace(address);
  }

  go(address: string): void {
    const location = this.document.defaultView?.location;
    if (location) location.href = address;
  }
}
