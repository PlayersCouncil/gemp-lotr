import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { Notifier } from '../../../core/notifier/notifier';

/**
 * The Notifier in use for now: the browser's own alert, confirm and prompt dialogs, as the legacy page used, so the
 * deck builder keeps its look. DialogNotifier (in-page dialogs) is kept for the redesign.
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
