import { Injectable } from '@angular/core';

/**
 * How core tells the player something or asks them (the legacy page's alert, confirm and prompt). Every method is
 * asynchronous, so the view can show its own dialogs; core waits for the answer instead of blocking the page.
 *
 * The view layer provides the implementation (`{ provide: Notifier, useClass: ... }`).
 */
@Injectable()
export abstract class Notifier {
  /** Shows a message; resolves when the player has dismissed it. */
  abstract alert(message: string): Promise<void>;

  /** A message that needs no answer. */
  tell(message: string): void {
    void this.alert(message);
  }

  /** Asks a yes/no question; true for OK. */
  abstract confirm(message: string): Promise<boolean>;

  /** Asks for a line of text, starting with `defaultValue`; null when the player cancelled. */
  abstract askText(message: string, defaultValue?: string): Promise<string | null>;
}
