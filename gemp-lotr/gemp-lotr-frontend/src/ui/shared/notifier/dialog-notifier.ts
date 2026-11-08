import { Injectable, signal } from '@angular/core';
import { Notifier } from '../../../core/notifier/notifier';

/** A message or question waiting for the player. */
export type NotifierRequest =
  | { kind: 'alert'; message: string; resolve: () => void }
  | { kind: 'confirm'; message: string; resolve: (ok: boolean) => void }
  | { kind: 'prompt'; message: string; defaultValue: string; resolve: (text: string | null) => void };

/**
 * The Notifier of the page: each message or question is shown in turn by `gd-notifier` (NotifierDialogComponent), in
 * the order they were asked; the promise resolves when the player answers.
 */
@Injectable()
export class DialogNotifier extends Notifier {
  private readonly queue = signal<readonly NotifierRequest[]>([]);
  /** What is shown now. */
  readonly current = () => this.queue()[0] ?? null;

  alert(message: string): Promise<void> {
    return new Promise((resolve) => this.push({ kind: 'alert', message, resolve }));
  }

  confirm(message: string): Promise<boolean> {
    return new Promise((resolve) => this.push({ kind: 'confirm', message, resolve }));
  }

  askText(message: string, defaultValue = ''): Promise<string | null> {
    return new Promise((resolve) => this.push({ kind: 'prompt', message, defaultValue, resolve }));
  }

  /** The player answered what is shown: OK (with the text, for a prompt) or Cancel (also closing it, or Escape). */
  answer(ok: boolean, text = ''): void {
    const request = this.current();
    if (!request) {
      return;
    }
    this.queue.update(([, ...rest]) => rest);
    switch (request.kind) {
      case 'alert':
        request.resolve();
        break;
      case 'confirm':
        request.resolve(ok);
        break;
      case 'prompt':
        request.resolve(ok ? text : null);
        break;
    }
  }

  private push(request: NotifierRequest): void {
    this.queue.update((queue) => [...queue, request]);
  }
}
