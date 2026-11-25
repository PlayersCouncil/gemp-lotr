import { Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { AuthoringApi } from '../../../../core/game/authoring/authoring-api.service';
import { Authoring } from '../../../../core/game/authoring/authoring.service';

/** A picture of the server's a message can show large. */
export interface ScenarioPicture {
  name: string;
  url: string;
}

/** What a message's Shows large says for a picture of the server's (`image:map.png`). */
export function pictureZoom(name: string): string {
  return `image:${name}`;
}

/**
 * The pictures a message can show large that are not cards (the legacy scenarioAuthor.js choosePicture): the files
 * of the server's images/scenarios folder (`GET scenarios/author/images`), looked for each time it is shown, as a
 * grid of thumbnails with their names; the one `value` names is marked. Choosing one says `chosen` with what Shows
 * large is to say (`image:<name>`), then `closed`; Escape says `closed`.
 *
 * DOM contract: `gd-author-pictures` holding `.gd-author-pictures` (`data-state`: loading, none, failed, shown) with
 * `.gd-author-note` while it looks, when there are none or the list could not be had, else a
 * `button.gd-author-pictures__item` per picture (`data-name`, `aria-pressed` for the one `value` names, `title` its
 * `image:` name) holding `img.gd-author-pictures__image` and `.gd-author-pictures__name`.
 */
@Component({
  selector: 'gd-author-pictures',
  styleUrl: './picture-chooser.component.css',
  host: { '(keydown.escape)': 'close($event)' },
  template: `
    <div class="gd-author-pictures" role="group" aria-label="Pictures" [attr.data-state]="state()">
      @switch (state()) {
        @case ('loading') {
          <p class="gd-author-note">Looking in images/scenarios…</p>
        }
        @case ('none') {
          <p
            class="gd-author-note"
            title="They are files (.png, .jpg, .gif, .webp) in the web folder's images/scenarios, put there by whoever runs the server."
          >
            There are no pictures.
          </p>
        }
        @case ('failed') {
          <p class="gd-author-note">The pictures could not be listed.</p>
        }
        @default {
          @for (picture of pictures(); track picture.name) {
            <button
              type="button"
              class="gd-author-pictures__item"
              [attr.data-name]="picture.name"
              [attr.aria-pressed]="zoomOf(picture) === value().trim()"
              [title]="zoomOf(picture)"
              (click)="choose(picture)"
            >
              <img class="gd-author-pictures__image" alt="" [src]="picture.url" />
              <span class="gd-author-pictures__name">{{ picture.name }}</span>
            </button>
          }
        }
      }
    </div>
  `,
})
export class PictureChooserComponent {
  /** What the message's Shows large says now. */
  readonly value = input('');
  /** What Shows large is to say: `image:<name>`. */
  readonly chosen = output<string>();
  readonly closed = output();

  private readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);
  /** Null while it looks; undefined if they could not be had. */
  protected readonly pictures = signal<readonly ScenarioPicture[] | null | undefined>(null);
  protected readonly state = computed(() => {
    const pictures = this.pictures();
    if (pictures === null) return 'loading';
    if (pictures === undefined) return 'failed';
    return pictures.length === 0 ? 'none' : 'shown';
  });
  private destroyed = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => (this.destroyed = true));
    void this.authoring.send(this.api.images(), false).then((answer) => {
      if (!this.destroyed) this.pictures.set(answer ? answer.images : undefined);
    });
  }

  protected zoomOf(picture: ScenarioPicture): string {
    return pictureZoom(picture.name);
  }

  protected choose(picture: ScenarioPicture): void {
    this.chosen.emit(pictureZoom(picture.name));
    this.closed.emit();
  }

  protected close(event: Event): void {
    event.preventDefault();
    this.closed.emit();
  }
}
