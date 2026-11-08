import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { Notifier } from '../../core/notifier/notifier';

/**
 * Temporary Notifier for the current view: the browser's own alert, confirm and prompt dialogs, as the legacy page
 * used. Replaced by in-app dialogs when the new view layer lands.
 */
@Injectable()
export class WindowNotifier extends Notifier {
  private readonly window = inject(DOCUMENT).defaultView!;

  alert(message: string): Promise<void> {
    this.window.alert(message);
    return Promise.resolve();
  }

  confirm(message: string): Promise<boolean> {
    return Promise.resolve(this.window.confirm(message));
  }

  askText(message: string, defaultValue = ''): Promise<string | null> {
    return Promise.resolve(this.window.prompt(message, defaultValue));
  }
}
