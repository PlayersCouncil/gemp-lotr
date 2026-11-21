import { DestroyRef, Directive, ElementRef, inject, input, output } from '@angular/core';

/** How long after typing stops a field that sends as it is typed in sends (ms). */
export const TYPING_SEND_MS = 700;

/**
 * When a bound field of the panel sends what is in it (`(sent)`, with its value; the same value is not sent twice in
 * a row):
 * - `change` (the default): when it is left or Enter is pressed in a one-line field (most of a step's parts);
 * - `typing`: also 700 ms after typing stops (a message's fields: their bubble shows the change soon after);
 * Either way `send()` sends what is waiting at once (a form's flush does, before the panel goes elsewhere), and
 * `pending` says whether anything is.
 *
 * DOM contract: none of its own (an `input` or a `textarea`).
 */
@Directive({
  selector: 'input[gdAuthorSend], textarea[gdAuthorSend]',
  host: {
    '(input)': 'typed()',
    '(change)': 'send()',
    '(keydown.enter)': 'enter()',
  },
})
export class AuthorSendDirective {
  readonly policy = input<'' | 'change' | 'typing'>('', { alias: 'gdAuthorSend' });
  readonly sent = output<string>();

  private readonly field = inject<ElementRef<HTMLInputElement | HTMLTextAreaElement>>(ElementRef).nativeElement;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private last: string | null = null;
  /** Typed in since it was last sent. */
  private changed = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stop());
  }

  /** Typed in since it was last sent. */
  pending(): boolean {
    return this.changed;
  }

  protected typed(): void {
    this.changed = true;
    if (this.policy() !== 'typing') return;
    this.stop();
    this.timer = setTimeout(() => this.send(), TYPING_SEND_MS);
  }

  protected enter(): void {
    if (this.field instanceof HTMLInputElement) this.send();
  }

  /** Sends now (unless it was sent as it is). */
  send(): void {
    this.stop();
    this.changed = false;
    const value = this.field.value;
    if (value === this.last) return;
    this.last = value;
    this.sent.emit(value);
  }

  private stop(): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
  }
}
