import { afterRenderEffect, Component, computed, ElementRef, inject, input, viewChild } from '@angular/core';
import {
  LINK_COPIED,
  SHARE_BUTTON_LABEL,
  SHARE_BUTTON_TITLE,
  SHARE_ICON,
  SHARE_LINK_FIELD_LABEL,
  ShareKind,
  ShareLinks,
} from '../../../core/hall/share-links';

/**
 * A "Copy share link" button (shareLinks.js GempShareLinks.button): copies the share link of kind/id, then says "Link
 * copied" beside it for a moment, or shows the link, selected, to copy by hand when the browser would not copy it.
 * The note is placed after the button (`note` = after, Format Definitions) or to its left out of the flow, so nothing
 * moves (`note` = left, Patch Notes).
 *
 * Inputs: `kind`, `id` (required), `label` (default "Copy share link"), `ariaLabel`, `note` (after | left), `look`
 * (plain | filled).
 *
 * DOM contract: `gd-hall-share-button.gd-hall-share` (`data-note`, `data-look`) holding `button.gd-hall-share__button` (`title`,
 * `aria-label`; `img.gd-hall-share__icon` and `span.gd-hall-share__label`) and, while there is one,
 * `span.gd-hall-share__note` (`role=status`; `data-manual` with `input.gd-hall-share__field` when copying failed).
 */
@Component({
  selector: 'gd-hall-share-button',
  styleUrl: './hall-share-button.component.css',
  host: { class: 'gd-hall-share', '[attr.data-note]': 'note()', '[attr.data-look]': 'look()' },
  template: `
    <button
      type="button"
      class="gd-hall-share__button"
      [title]="title"
      [attr.aria-label]="ariaLabel()"
      [disabled]="!id()"
      (click)="copy($event)"
    >
      <img class="gd-hall-share__icon" alt="" aria-hidden="true" [src]="icon" /><span class="gd-hall-share__label">{{
        label()
      }}</span>
    </button>
    @if (shown(); as shown) {
      <span class="gd-hall-share__note" role="status" [attr.data-manual]="shown.kind === 'manual' ? '' : null">
        @if (shown.kind === 'copied') {
          {{ copied }}
        } @else {
          <input
            #field
            class="gd-hall-share__field"
            type="text"
            readonly
            [value]="shown.url"
            [attr.aria-label]="fieldLabel"
          />
        }
      </span>
    }
  `,
})
export class HallShareButtonComponent {
  readonly kind = input.required<ShareKind>();
  readonly id = input.required<string>();
  readonly label = input(SHARE_BUTTON_LABEL);
  readonly ariaLabel = input<string | null>(null);
  readonly note = input<'after' | 'left'>('after');
  /** plain: the share button's own look; filled: the patch notes' header buttons (patchNotes.css .patchnote-head button). */
  readonly look = input<'plain' | 'filled'>('plain');

  private readonly links = inject(ShareLinks);
  private readonly field = viewChild<ElementRef<HTMLInputElement>>('field');
  protected readonly title = SHARE_BUTTON_TITLE;
  protected readonly icon = SHARE_ICON;
  protected readonly copied = LINK_COPIED;
  protected readonly fieldLabel = SHARE_LINK_FIELD_LABEL;
  protected readonly shown = computed(() => this.links.noteOf(this.kind(), this.id()));

  constructor() {
    // the link to copy by hand is selected, ready for Ctrl+C
    afterRenderEffect(() => {
      const field = this.field()?.nativeElement;
      if (field) {
        field.focus();
        field.select();
      }
    });
  }

  protected copy(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    void this.links.copy(this.kind(), this.id());
  }
}
