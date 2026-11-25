import { afterRenderEffect, Directive, ElementRef, inject, input } from '@angular/core';

/** The most rows an HJSON field grows to; it scrolls beyond. */
export const HJSON_MAX_ROWS = 16;

/**
 * A field for a part of the scenario's file as HJSON (a When, a requirement, a whole part): set in the code font, as
 * many rows high as its text (from its own `rows` up to `max`), and Tab types a tab: always (`gdHjsonArea="always"`),
 * or only once the text has more than one line (the default), so that Tab still leaves a one-line field.
 *
 * DOM contract: `textarea.gd-author-code` with `rows` set to its text's lines.
 */
@Directive({
  selector: 'textarea[gdHjsonArea]',
  host: {
    class: 'gd-author-code',
    spellcheck: 'false',
    '(input)': 'fit()',
    '(keydown.tab)': 'tab($event)',
  },
})
export class HjsonAreaDirective {
  readonly tabs = input<'' | 'multi-line' | 'always'>('', { alias: 'gdHjsonArea' });
  readonly max = input(HJSON_MAX_ROWS);
  /** The value, when it is bound: the field fits it again when it changes. */
  readonly text = input<string | null>(null);

  private readonly area = inject<ElementRef<HTMLTextAreaElement>>(ElementRef).nativeElement;
  private readonly least = Math.max(1, Number(this.area.getAttribute('rows')) || 1);

  constructor() {
    afterRenderEffect(() => {
      this.text();
      this.fit();
    });
  }

  /** As many rows as its text has lines, between its own and the most. */
  fit(): void {
    const lines = this.area.value.split('\n').length;
    this.area.rows = Math.max(this.least, Math.min(this.max(), lines));
  }

  protected tab(event: Event): void {
    const key = event as KeyboardEvent;
    if (key.shiftKey || key.ctrlKey || key.altKey || key.metaKey) return;
    const area = this.area;
    if (this.tabs() !== 'always' && !area.value.includes('\n')) return;
    event.preventDefault();
    const { selectionStart: start, selectionEnd: end, value } = area;
    area.value = value.substring(0, start) + '\t' + value.substring(end);
    area.selectionStart = area.selectionEnd = start + 1;
    // (as typing it would: whatever listens for input hears it)
    area.dispatchEvent(new Event('input', { bubbles: true }));
  }
}
