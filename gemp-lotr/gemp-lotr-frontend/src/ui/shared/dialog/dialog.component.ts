import { DOCUMENT } from '@angular/common';
import {
  afterRenderEffect,
  Component,
  ElementRef,
  inject,
  Injectable,
  input,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { DragHandlers, PointerDragDirective } from '../pointer-drag/pointer-drag.directive';
import { ThemeService } from '../../../core/platform/theme.service';

const FOCUSABLE = 'input, select, textarea, button, a[href], [tabindex]';

/** Every dialog opened or pressed goes in front of the other non-modal ones (as jQuery UI's dialogs did). */
@Injectable({ providedIn: 'root' })
export class DialogLayers {
  private top = 0;
  private ids = 0;

  next(): number {
    return ++this.top;
  }

  /** A unique id for a dialog's title. */
  nextId(): string {
    return `gd-dialog-title-${++this.ids}`;
  }
}

/**
 * A dialog: a native `<dialog>` with a title bar (title and close button), the content, and optional actions below.
 * `open` shows it (modal by default, or not, with `[modal]="false"`) and hides it; it closes itself on the close
 * button and on Escape, and then emits `closed` (the owner sets `open` back). Dragging the title bar moves it; a theme
 * may let the player resize it (CSS `resize`), and pressing it anywhere first pins it where it is, so a resize grows it
 * right and down. Opening it again puts it back where the theme places it (centred, normally). It focuses an
 * `autofocus` control when it opens, else its content's first control, else its actions' first.
 *
 * Projected content: the content (the owner renders it only while open, if it should start afresh each time), and an
 * element with `gdDialogActions` for the action buttons.
 *
 * DOM contract: `dialog.gd-dialog` with `data-dialog` (which dialog: deck-list, library, import, notes, selection,
 * delivery, alert, confirm, prompt), `data-placed` once moved or pinned, with `--gd-dialog-left` and
 * `--gd-dialog-top` (px, its top left corner in the window), and `--gd-dialog-layer` (a number that grows each time a
 * dialog opens or is pressed, for the stacking of non-modal dialogs). Inside: `header.gd-dialog__header` with
 * `h2.gd-dialog__title` and `button.gd-dialog__close`, `.gd-dialog__body` (the content), then the actions.
 */
@Component({
  selector: 'gd-dialog',
  imports: [PointerDragDirective],
  host: { style: 'display: contents' },
  template: `
    <dialog
      #dialog
      class="gd-dialog"
      [attr.tabindex]="focusSelf() ? -1 : null"
      [attr.data-dialog]="name()"
      [attr.aria-labelledby]="titleId"
      [attr.autofocus]="focusSelf() ? '' : null"
      [attr.data-placed]="placed() ? '' : null"
      [style.--gd-dialog-left]="placed() ? placed()!.left + 'px' : null"
      [style.--gd-dialog-top]="placed() ? placed()!.top + 'px' : null"
      [style.--gd-dialog-layer]="layer()"
      (close)="onNativeClose()"
      (keydown)="onKeyDown($event)"
      (pointerdown)="onPointerDown($event)"
    >
      <header class="gd-dialog__header" [gdPointerDrag]="moveDrag">
        <h2 class="gd-dialog__title" [id]="titleId">{{ heading() }}</h2>
        <button type="button" class="gd-dialog__close" aria-label="Close" title="Close" (click)="close()"></button>
      </header>
      <div class="gd-dialog__body">
        <ng-content />
      </div>
      <ng-content select="[gdDialogActions]" />
    </dialog>
  `,
})
export class DialogComponent {
  private readonly layers = inject(DialogLayers);
  private readonly theme = inject(ThemeService);
  private readonly window = inject(DOCUMENT).defaultView;
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  /** Which dialog this is (`data-dialog`), for the theme. */
  readonly name = input.required<string>();
  readonly heading = input.required<string>();
  readonly open = input(false);
  /** Modal: the page behind can't be used while it is open. */
  readonly modal = input(true);
  /** Focus the dialog itself when it opens, not its first control. */
  readonly focusSelf = input(false);

  /** Closed by the player (close button, Escape), not by `open` going false. */
  readonly closed = output();

  protected readonly titleId = this.layers.nextId();
  protected readonly placed = signal<{ left: number; top: number } | null>(null);
  protected readonly layer = signal(0);
  /** `open` went false: the close that follows is not the player's. */
  private closingQuietly = false;

  constructor() {
    afterRenderEffect(() => {
      const dialog = this.dialog().nativeElement;
      const open = this.open();
      const modal = untracked(this.modal);
      if (open && !dialog.open) {
        this.placed.set(null);
        this.layer.set(this.layers.next());
        // jsdom has no showModal/show
        if (typeof dialog.showModal !== 'function') {
          dialog.setAttribute('open', '');
        } else if (modal) {
          dialog.showModal();
        } else {
          dialog.show();
        }
        this.focusContent(dialog);
      } else if (!open && dialog.open) {
        this.closingQuietly = true;
        this.closeElement(dialog);
      }
    });
  }

  close(): void {
    const dialog = this.dialog().nativeElement;
    if (dialog.open) {
      this.closeElement(dialog);
    } else {
      this.closed.emit();
    }
  }

  protected onNativeClose(): void {
    this.dropReturnedFocus();
    if (this.closingQuietly) {
      this.closingQuietly = false;
    } else {
      this.closed.emit();
    }
  }

  /** The browser gives the focus back to what had it when the dialog opened; not every theme wants that. */
  private dropReturnedFocus(): void {
    const dialog = this.dialog().nativeElement;
    const returned = dialog.ownerDocument.activeElement;
    if (
      !this.theme.behaviour().dialogsReturnFocus &&
      returned instanceof HTMLElement &&
      returned !== dialog.ownerDocument.body &&
      !returned.closest('dialog')
    ) {
      returned.blur();
    }
  }

  /** Modal dialogs close on Escape by themselves; non-modal ones when it is pressed inside them. */
  protected onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && !this.modal()) {
      event.preventDefault();
      this.close();
    }
  }

  /** In front; and pinned where it is, so that resizing it (from its edge) grows it right and down. */
  protected onPointerDown(event: PointerEvent): void {
    this.layer.set(this.layers.next());
    if (event.target === event.currentTarget && !this.placed()) {
      const { left, top } = (event.currentTarget as HTMLElement).getBoundingClientRect();
      this.placed.set({ left: Math.round(left), top: Math.round(top) });
    }
  }

  /** Dragging the title bar moves the dialog, kept inside the window. */
  protected readonly moveDrag = (event: PointerEvent): DragHandlers | null => {
    if ((event.target as Element).closest('button')) {
      return null;
    }
    const start = this.dialog().nativeElement.getBoundingClientRect();
    const { clientX: startX, clientY: startY } = event;
    return {
      move: (e) => {
        const maxLeft = (this.window?.innerWidth ?? 0) - start.width;
        const maxTop = (this.window?.innerHeight ?? 0) - start.height;
        this.placed.set({
          left: Math.round(Math.max(0, Math.min(maxLeft, start.left + e.clientX - startX))),
          top: Math.round(Math.max(0, Math.min(maxTop, start.top + e.clientY - startY))),
        });
      },
      end: () => undefined,
    };
  };

  /**
   * Without an autofocus control, the browser focuses the first one, the title bar's close button; the content's
   * first control (or else the actions') is focused instead, as jQuery UI did.
   */
  private focusContent(dialog: HTMLDialogElement): void {
    if (this.focusSelf()) {
      dialog.focus();
      return;
    }
    const focused = dialog.ownerDocument.activeElement;
    if (focused && focused !== dialog && dialog.contains(focused) && !focused.matches('.gd-dialog__close')) {
      return;
    }
    const first = Array.from(dialog.querySelectorAll<HTMLElement>(FOCUSABLE)).find(
      (el) => !el.matches('.gd-dialog__close') && el.tabIndex >= 0 && !(el as HTMLButtonElement).disabled,
    );
    if (first) {
      first.focus();
    } else {
      // Nothing to focus in it: the dialog itself, as jQuery UI did, not the close button the browser picks
      dialog.tabIndex = -1;
      dialog.focus();
    }
  }

  private closeElement(dialog: HTMLDialogElement): void {
    if (typeof dialog.close === 'function') {
      dialog.close();
    } else {
      dialog.removeAttribute('open');
      this.onNativeClose();
    }
  }
}
