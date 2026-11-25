import { Component, inject, input, output, ViewEncapsulation } from '@angular/core';
import { MarkupNode } from '../../../core/chat/chat-markup';
import { MarkupComponent } from '../../shared/chat/markup.component';
import { HallCards } from '../shared/hall-cards';

/** A screenshot to enlarge: its address and description, and the picture itself (the focus goes back to it). */
export interface PatchNotePicture {
  readonly src: string;
  readonly alt: string;
  readonly from: HTMLElement;
}

/**
 * A patch note's summary or body (`kind`), drawn by gd-markup from the server's rendered Markdown, laid out as
 * patchNotes.css lays out .patchnote-body and .patchnote-summary. gd-markup builds its elements itself, so this
 * component's rules are not scoped: they all start at `.gd-patch-note-text`. A card's name shows the card (click,
 * right-click, Enter or Space); a link into the hall goes to its route; a screenshot (a picture that is not an icon nor
 * inside a link) is reported to enlarge (`picture`).
 *
 * DOM contract: `gd-patch-note-text.gd-patch-note-text` (`data-kind` = summary, body) holding `gd-markup`.
 */
@Component({
  selector: 'gd-patch-note-text',
  styleUrl: './patch-note-text.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [MarkupComponent],
  host: {
    class: 'gd-patch-note-text',
    '[attr.data-kind]': 'kind()',
    '(click)': 'click($event)',
    '(contextmenu)': 'contextMenu($event)',
  },
  template: '<gd-markup [nodes]="nodes()" (card)="cards.show($event)" />',
})
export class PatchNoteTextComponent {
  readonly nodes = input.required<readonly MarkupNode[]>();
  readonly kind = input<'summary' | 'body'>('body');
  /** A screenshot was clicked. */
  readonly picture = output<PatchNotePicture>();

  protected readonly cards = inject(HallCards);

  protected click(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target?.matches('img.gd-markup__image:not([data-icon])') || target.closest('a')) {
      return;
    }
    event.preventDefault();
    const image = target as HTMLImageElement;
    this.picture.emit({ src: image.getAttribute('src') ?? '', alt: image.alt, from: image });
  }

  protected contextMenu(event: MouseEvent): void {
    const card = (event.target as HTMLElement | null)?.closest<HTMLElement>('.gd-markup__card');
    const id = card?.dataset['blueprint'];
    if (id) {
      event.preventDefault();
      this.cards.show(id);
    }
  }
}
