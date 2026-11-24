import { afterNextRender, Directive, ElementRef, inject, input, output } from '@angular/core';

/** The items of a menu the arrow keys move between. */
const ITEMS = '[role="menuitem"]:not([disabled])';

/**
 * A menu's keys (the edit controls' chooser, card menu and its submenus): Up and Down (and Home, End) move the focus
 * between its items, wrapping round; Right says `forward` (open the submenu of the item), Left `back`, Escape `escape`.
 * Keys typed in a field or a list of the menu are the field's own. The first item takes the focus when the menu opens
 * (`gdMenuKeysFocus`, default on).
 *
 * DOM contract: none of its own (its items are the host's `[role=menuitem]`).
 */
@Directive({
  selector: '[gdMenuKeys]',
  host: { '(keydown)': 'key($event)' },
})
export class MenuKeysDirective {
  readonly gdMenuKeysFocus = input(true);
  readonly forward = output<HTMLElement>();
  readonly back = output();
  readonly escape = output();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  constructor() {
    afterNextRender(() => {
      if (this.gdMenuKeysFocus()) this.items()[0]?.focus();
    });
  }

  private items(): HTMLElement[] {
    return Array.from(this.host.querySelectorAll<HTMLElement>(ITEMS));
  }

  protected key(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.escape.emit();
      return;
    }
    const target = event.target as HTMLElement | null;
    // (a field's own keys: the caret, a list's choice)
    if (target && /^(INPUT|SELECT|TEXTAREA)$/.test(target.tagName)) return;
    const items = this.items();
    const at = target ? items.indexOf(target) : -1;
    const go = (index: number) => {
      event.preventDefault();
      items[(index + items.length) % items.length]?.focus();
    };
    switch (event.key) {
      case 'ArrowDown':
        go(at + 1);
        break;
      case 'ArrowUp':
        go(at < 0 ? -1 : at - 1);
        break;
      case 'Home':
        go(0);
        break;
      case 'End':
        go(-1);
        break;
      case 'ArrowRight':
        if (target && at >= 0) {
          event.preventDefault();
          this.forward.emit(target);
        }
        break;
      case 'ArrowLeft':
        event.preventDefault();
        this.back.emit();
        break;
    }
  }
}
