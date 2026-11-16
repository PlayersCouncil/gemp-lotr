import { DestroyRef, Directive, ElementRef, inject } from '@angular/core';

/**
 * A `<dialog>` shown as a modal while it is on the page (the top layer: the page under it is inert, the focus stays in
 * it), as the shared dialog and the deck builder's menus are. Closing it is the page's: the browser's own closing on
 * Escape is refused, and if the browser closes it anyway (Chrome may, after a second Escape without the player touching
 * the page) it is shown again; the page goes back a level on Escape itself.
 */
@Directive({
  selector: 'dialog[gdModalSheet]',
  host: {
    // The focus goes to the sheet itself, not its first button (its close button, which would show a focus ring)
    tabindex: '-1',
    autofocus: '',
    '(cancel)': '$event.preventDefault()',
    '(close)': 'show()',
    '(contextmenu)': 'refuseMenu($event)',
  },
})
export class ModalSheetDirective {
  private readonly dialog = inject<ElementRef<HTMLDialogElement>>(ElementRef).nativeElement;
  private removed = false;

  constructor() {
    queueMicrotask(() => this.show());
    inject(DestroyRef).onDestroy(() => {
      this.removed = true;
      if (typeof this.dialog.close === 'function' && this.dialog.open) this.dialog.close();
    });
  }

  protected refuseMenu(event: MouseEvent): void {
    const target = event.target instanceof Element ? event.target : null;
    if (target?.closest('img, .gd-card')) event.preventDefault();
  }

  protected show(): void {
    if (this.removed || this.dialog.open) return;
    // jsdom has no showModal
    if (typeof this.dialog.showModal === 'function' && this.dialog.isConnected) this.dialog.showModal();
    else this.dialog.setAttribute('open', '');
    // The browser may still have given the focus to the first button
    this.dialog.focus({ preventScroll: true });
  }
}
