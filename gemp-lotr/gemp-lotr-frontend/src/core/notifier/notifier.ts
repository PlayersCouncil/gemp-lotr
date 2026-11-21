import { Injectable } from '@angular/core';

/**
 * How core tells the player something or asks them (the legacy page's alert, confirm and prompt). Every method is
 * asynchronous, so the view can show its own dialogs; core waits for the answer instead of blocking the page.
 *
 * The view layer provides the implementation (`{ provide: Notifier, useClass: ... }`).
 */
/** The words of a question's dialog, where the view draws its own: its title and its two buttons. */
export interface ConfirmWords {
  title?: string;
  ok?: string;
  cancel?: string;
}

@Injectable()
export abstract class Notifier {
  /** Shows a message; resolves when the player has dismissed it. */
  abstract alert(message: string): Promise<void>;

  /** A message that needs no answer. */
  tell(message: string): void {
    void this.alert(message);
  }

  /** Asks a yes/no question; true for OK. `words` names the dialog and its buttons where the view has its own. */
  abstract confirm(message: string, words?: ConfirmWords): Promise<boolean>;

  /** Asks for a line of text, starting with `defaultValue`; null when the player cancelled. */
  abstract askText(message: string, defaultValue?: string): Promise<string | null>;
}
