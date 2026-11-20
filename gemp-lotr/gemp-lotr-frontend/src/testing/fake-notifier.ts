import { Injectable, Provider } from '@angular/core';
import { ConfirmWords, Notifier } from '../core/notifier/notifier';

/**
 * A Notifier for tests: records every message as "alert: ...", "confirm: ..." or "prompt: ..." in `dialogs`, and
 * answers confirm/askText from `answers` in order (confirm: true for OK; askText: the text, or null for Cancel). The
 * words each question gives its dialog (its title and buttons) are in `words`, one entry per confirm.
 */
@Injectable()
export class FakeNotifier extends Notifier {
  readonly dialogs: string[] = [];
  readonly answers: (boolean | string | null)[] = [];
  readonly words: (ConfirmWords | undefined)[] = [];

  alert(message: string): Promise<void> {
    this.dialogs.push(`alert: ${message}`);
    return Promise.resolve();
  }

  confirm(message: string, words?: ConfirmWords): Promise<boolean> {
    this.dialogs.push(`confirm: ${message}`);
    this.words.push(words);
    return Promise.resolve(this.answers.shift() === true);
  }

  askText(message: string): Promise<string | null> {
    this.dialogs.push(`prompt: ${message}`);
    const answer = this.answers.shift();
    return Promise.resolve(typeof answer === 'string' ? answer : null);
  }
}

/** Provides a FakeNotifier as the Notifier; get it with TestBed.inject(Notifier) as FakeNotifier. */
export function provideFakeNotifier(): Provider[] {
  return [FakeNotifier, { provide: Notifier, useExisting: FakeNotifier }];
}

/** Lets pending promise continuations (notifier answers) run. */
export async function settle(): Promise<void> {
  for (let i = 0; i < 10; i++) {
    await Promise.resolve();
  }
}
