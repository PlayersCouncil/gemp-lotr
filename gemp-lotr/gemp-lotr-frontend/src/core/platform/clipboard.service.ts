import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * Copying text to the clipboard (shareLinks.js GempShareLinks.copy) with the Clipboard API. Says whether it worked, so
 * the page can show the text to copy by hand when it did not. The legacy fallback, the deprecated copy command on a
 * hidden text box, is not used: where the API is refused (a page not served over https) the text is shown instead.
 */
@Injectable({ providedIn: 'root' })
export class ClipboardWriter {
  private readonly window = inject(DOCUMENT).defaultView;

  async copy(text: string): Promise<boolean> {
    const window = this.window;
    const clipboard = window?.navigator.clipboard;
    if (!window || !clipboard || !window.isSecureContext) {
      return false;
    }
    try {
      await clipboard.writeText(text);
      return true;
    } catch {
      return false;
    }
  }
}
