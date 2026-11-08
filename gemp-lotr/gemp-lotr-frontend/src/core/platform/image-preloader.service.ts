import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/** The longest a page of cards waits for its images; a slow or missing image then shows as it arrives. */
export const IMAGE_PRELOAD_TIMEOUT_MS = 3000;

/**
 * Loads images before they are shown, so a set of cards can change all at once instead of one card after another as
 * each image arrives. Loaded images stay in the browser cache, so the cards' own <img> elements then show them at
 * once.
 */
@Injectable({ providedIn: 'root' })
export class ImagePreloader {
  private readonly window = inject(DOCUMENT).defaultView;

  /** Whether this browser can wait for images (not in the unit tests' DOM). */
  get supported(): boolean {
    const image = this.window ? new this.window.Image() : null;
    return typeof image?.decode === 'function';
  }

  /** Resolves when every image is loaded and decoded, failed, or the timeout passed, whichever comes first. */
  load(urls: Iterable<string>, timeoutMs = IMAGE_PRELOAD_TIMEOUT_MS): Promise<void> {
    if (!this.window || !this.supported) {
      return Promise.resolve();
    }
    const window = this.window;
    const loads = [...new Set(urls)].map((url) => {
      const image = new window.Image();
      image.src = url;
      // A broken image counts as done: it would not appear any sooner
      return image.decode().catch(() => undefined);
    });
    let timer: ReturnType<typeof setTimeout> | undefined;
    const timeout = new Promise<void>((resolve) => (timer = setTimeout(resolve, timeoutMs)));
    return Promise.race([Promise.all(loads).then(() => undefined), timeout]).finally(() => clearTimeout(timer));
  }
}
