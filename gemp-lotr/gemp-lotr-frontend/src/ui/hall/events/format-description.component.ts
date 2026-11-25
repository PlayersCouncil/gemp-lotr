import { Component, effect, inject, input, output, signal } from '@angular/core';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardViews } from '../../../core/cards/card-views.service';
import { EventsApi } from '../../../core/hall/events/events-api.service';
import { FORMAT_DIALOG_TITLE } from '../../../core/hall/events/events-messages';
import { FormatDescriptionLine, formatDescriptionLines } from '../../../core/hall/events/format-description';
import { FormatCard } from '../../../core/hall/help/format-definitions-models';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * A league serie's "Format description" (leagueResultsUi.js formatDialog): the format's name and its lines, from the
 * format definitions. While they load, and for a format they don't list, the dialog is empty, as the legacy dialog
 * was when its GET /hall/format/{code} failed. A listed card's name shows the card on a click.
 *
 * Inputs: `code` (the format's code; null: closed). Emits `closed`.
 *
 * DOM contract: `gd-dialog[data-dialog=format-description]` holding, once loaded, `section.gd-format-description`
 * (`data-format`) with `h2.gd-format-description__name` and `ul` of `li.gd-format-description__line`, whose cards are
 * `button.gd-format-description__card`.
 */
@Component({
  selector: 'gd-format-description',
  styleUrl: './format-description.component.css',
  imports: [DialogComponent],
  template: `
    <gd-dialog name="format-description" [heading]="heading" [open]="code() !== null" (closed)="closed.emit()">
      @if (loaded(); as format) {
        <section class="gd-format-description" [attr.data-format]="format.code">
          <h2 class="gd-format-description__name">{{ format.name }}</h2>
          <ul>
            @for (line of format.lines; track $index) {
              <li class="gd-format-description__line">
                {{ line.text }}
                @for (card of line.cards; track card.id) {
                  <button type="button" class="gd-format-description__card" (click)="show(card)">
                    {{ card.name }}</button
                  >,
                }
              </li>
            }
          </ul>
        </section>
      }
    </gd-dialog>
  `,
})
export class FormatDescriptionComponent {
  private readonly api = inject(EventsApi);
  private readonly views = inject(CardViews);
  private readonly info = inject(CardInfoService);

  readonly code = input<string | null>(null);
  readonly closed = output();

  protected readonly heading = FORMAT_DIALOG_TITLE;
  protected readonly loaded = signal<{ code: string; name: string; lines: FormatDescriptionLine[] } | null>(null);

  constructor() {
    effect((onCleanup) => {
      const code = this.code();
      this.loaded.set(null);
      if (code === null) {
        return;
      }
      const request = this.api.formatDefinition(code).subscribe({
        next: (format) =>
          this.loaded.set(
            format ? { code: format.code, name: format.name, lines: formatDescriptionLines(format) } : null,
          ),
        error: () => this.loaded.set(null),
      });
      onCleanup(() => request.unsubscribe());
    });
  }

  protected show(card: FormatCard): void {
    this.info.show(this.views.create({ ref: null, blueprintId: card.id, zone: null }));
  }
}
