import { afterNextRender, Directive, ElementRef, inject, input } from '@angular/core';
import { SessionStore } from '../../../../core/platform/session-store';

/** Where the field being typed in is kept while the page goes to a fresh game (the next page's field takes it). */
export const KEEP_KEY = 'gemp-author-keep';

interface Kept {
  key: string;
  value: string;
  start: number;
  end: number;
}

/**
 * Keeps the field being typed in across a fresh game: a change sent from one field of a section often brings a
 * fresh game (a full page load) while the author is already typing in the next. While the field has the focus,
 * what is in it and where the caret is are kept in the tab's session storage under its name (`gdAuthorKeep`, which
 * must be the same on the next page); the next page's field of that name takes them, the focus and the caret back,
 * and says it was typed in (an `input` event), so that leaving it sends it as usual. Left, it is forgotten.
 *
 * DOM contract: none of its own (an `input` or a `textarea`).
 */
@Directive({
  selector: 'input[gdAuthorKeep], textarea[gdAuthorKeep]',
  host: { '(input)': 'keep()', '(focus)': 'keep()', '(blur)': 'left()' },
})
export class AuthorKeepDirective {
  readonly key = input.required<string>({ alias: 'gdAuthorKeep' });
  private readonly store = inject(SessionStore);
  private readonly field = inject<ElementRef<HTMLInputElement | HTMLTextAreaElement>>(ElementRef).nativeElement;
  /** Taken back from the last page: leaving it sends it even with nothing typed since. */
  private restored = false;

  constructor() {
    afterNextRender(() => this.restore());
  }

  private restore(): void {
    const kept = this.store.read(KEEP_KEY) as Partial<Kept> | null;
    if (kept?.key !== this.key() || typeof kept.value !== 'string') return;
    this.store.remove(KEEP_KEY);
    if (kept.value !== this.field.value) {
      this.field.value = kept.value;
      this.restored = true;
      this.field.dispatchEvent(new Event('input'));
    }
    this.field.focus();
    this.field.setSelectionRange(kept.start ?? kept.value.length, kept.end ?? kept.value.length);
  }

  protected keep(): void {
    this.store.write(KEEP_KEY, {
      key: this.key(),
      value: this.field.value,
      start: this.field.selectionStart ?? 0,
      end: this.field.selectionEnd ?? 0,
    } satisfies Kept);
  }

  protected left(): void {
    const kept = this.store.read(KEEP_KEY) as Partial<Kept> | null;
    if (kept?.key === this.key()) this.store.remove(KEEP_KEY);
    if (!this.restored) return;
    // (what was taken back is unsent: the field's own change event fires only for what is typed after it)
    this.restored = false;
    this.field.dispatchEvent(new Event('change'));
  }
}
