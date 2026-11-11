import { Component, computed, ElementRef, input, signal, viewChild } from '@angular/core';

/**
 * A burger menu: a button, and the panel it opens from the left edge of the window (a modal `<dialog>`, so it is above
 * everything, Escape closes it and the rest of the page waits). A press outside the panel closes it, and so does a
 * button pressed in it, after doing its job; a list or a field in it leaves it open.
 *
 * Projected content: the menu's items.
 *
 * DOM contract: `button.gd-burger` (`aria-label` = the menu's name, and what `note` says; `aria-expanded`), holding
 * `span.gd-burger__badge` with `badge` when it isn't 0, then `dialog.gd-drawer` (`data-drawer` = which menu,
 * `aria-label` = its name) holding the items.
 */
@Component({
  selector: 'gd-drawer',
  styleUrl: './drawer.component.css',
  host: { style: 'display: contents' },
  template: `
    <button
      type="button"
      class="gd-burger"
      aria-haspopup="dialog"
      [attr.aria-label]="buttonName()"
      [title]="buttonName()"
      [attr.aria-expanded]="open()"
      (click)="show()"
    >
      @if (badge()) {
        <span class="gd-burger__badge" aria-hidden="true">{{ badge() }}</span>
      }
    </button>
    <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -- hears clicks on its items and its backdrop; Escape closes it natively -->
    <dialog
      #panel
      class="gd-drawer"
      [attr.data-drawer]="name()"
      [attr.aria-label]="label()"
      (close)="open.set(false)"
      (click)="clicked($event)"
    >
      <ng-content />
    </dialog>
  `,
})
export class DrawerComponent {
  /** Which menu (`data-drawer`). */
  readonly name = input.required<string>();
  /** The menu's name, for the button and the panel. */
  readonly label = input.required<string>();
  /** A number shown on the button (e.g. of filters set in the menu), when it isn't 0. */
  readonly badge = input(0);
  /** What the badge means, added to the button's name while it shows. */
  readonly note = input('');

  protected readonly open = signal(false);
  /** The button's name: the menu's, and the note while the badge shows. */
  protected readonly buttonName = computed(() =>
    this.badge() && this.note() ? `${this.label()}, ${this.note()}` : this.label(),
  );
  private readonly panel = viewChild.required<ElementRef<HTMLDialogElement>>('panel');

  show(): void {
    const panel = this.panel().nativeElement;
    // jsdom has no showModal
    if (typeof panel.showModal === 'function') {
      panel.showModal();
    } else {
      panel.setAttribute('open', '');
    }
    this.open.set(true);
  }

  hide(): void {
    const panel = this.panel().nativeElement;
    if (typeof panel.close === 'function') {
      panel.close();
    } else {
      panel.removeAttribute('open');
    }
    this.open.set(false);
  }

  /** A button in the panel did its job (its own handler ran first); a press on the backdrop is on the dialog itself. */
  protected clicked(event: MouseEvent): void {
    const target = event.target as Element;
    const panel = this.panel().nativeElement;
    if (target === panel) {
      const box = panel.getBoundingClientRect();
      const inside =
        event.clientX >= box.left &&
        event.clientX <= box.right &&
        event.clientY >= box.top &&
        event.clientY <= box.bottom;
      // A key press on a focused element of the panel also clicks, at 0,0
      if (!inside && event.detail !== 0) {
        this.hide();
      }
      return;
    }
    if (target.closest('button')) {
      this.hide();
    }
  }
}
