import { Component, computed, ElementRef, inject, viewChild } from '@angular/core';
import { DialogComponent } from '../dialog/dialog.component';
import { DialogNotifier } from './dialog-notifier';

const TITLES = { alert: 'Message', confirm: 'Please confirm', prompt: 'Please enter' } as const;

/**
 * Shows the DialogNotifier's messages and questions (the legacy page used the browser's alert, confirm and prompt):
 * a modal `gd-dialog` (`data-dialog` = alert, confirm or prompt) with the message, a text field for a prompt, and OK
 * (and Cancel). Enter in the text field is OK; Escape or the close button is Cancel. Questions asked while one is
 * shown wait their turn.
 *
 * DOM contract: inside the dialog, `p.gd-notifier__message` (line breaks kept), `input.gd-notifier__input` (prompt;
 * focused and selected when shown), and `.gd-dialog__actions` with `button.gd-button[data-action=ok]` (focused for
 * alert and confirm) and `button.gd-button[data-action=cancel]` (confirm, prompt).
 */
@Component({
  selector: 'gd-notifier',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      [name]="request()?.kind ?? 'alert'"
      [heading]="title()"
      [open]="request() !== null"
      (closed)="notifier.answer(false)"
    >
      @for (r of request() ? [request()!] : []; track r) {
        <p class="gd-notifier__message">{{ r.message }}</p>
        @if (r.kind === 'prompt') {
          <input
            #text
            class="gd-notifier__input"
            type="text"
            autofocus
            [attr.aria-label]="r.message"
            [value]="r.defaultValue"
            (focus)="text.select()"
            (keydown.enter)="$event.preventDefault(); notifier.answer(true, text.value)"
          />
        }
      }
      <div class="gd-dialog__actions" gdDialogActions>
        <button
          type="button"
          class="gd-button"
          data-action="ok"
          [attr.autofocus]="request()?.kind === 'prompt' ? null : ''"
          (click)="ok()"
        >
          OK
        </button>
        @if (request()?.kind !== 'alert') {
          <button
            type="button"
            class="gd-button"
            data-action="cancel"
            (click)="notifier.answer(false)"
          >
            Cancel
          </button>
        }
      </div>
    </gd-dialog>
  `,
})
export class NotifierDialogComponent {
  /** Absent when another Notifier is provided (e.g. in tests): nothing is shown. */
  private readonly dialogNotifier = inject(DialogNotifier, { optional: true });
  protected readonly notifier = {
    answer: (ok: boolean, text?: string) => this.dialogNotifier?.answer(ok, text),
  };
  protected readonly request = computed(() => this.dialogNotifier?.current() ?? null);
  protected readonly title = computed(() => TITLES[this.request()?.kind ?? 'alert']);

  private readonly input = viewChild<ElementRef<HTMLInputElement>>('text');

  protected ok(): void {
    this.notifier.answer(true, this.input()?.nativeElement.value ?? '');
  }
}
