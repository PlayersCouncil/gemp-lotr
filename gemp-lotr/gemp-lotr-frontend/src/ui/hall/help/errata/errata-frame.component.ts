import { NgTemplateOutlet } from '@angular/common';
import { afterNextRender, Component, computed, DestroyRef, ElementRef, inject, input, output } from '@angular/core';
import { ErrataReadout } from '../../../../core/hall/help/errata/errata-diff';
import { aspectOf, ErrataImages } from '../../../../core/hall/help/errata/errata-images';
import {
  CARD_TITLE,
  CLIPPED_MORE,
  ENLARGED_ARROW,
  ENLARGED_ARROW_COLUMN,
  FIGURE_CARD_TITLE,
  frameMeta,
  frameMetaTitle,
  NEW_BADGE,
  NEW_BADGE_TITLE,
  NO_IMAGE,
  NO_ORIGINAL_IMAGE,
  ORIGINAL,
  PC_ERRATA,
  revisionBadge,
  STAT_ARROW,
  STAT_ARROW_LABEL,
} from '../../../../core/hall/help/errata/errata-messages';
import { ErrataEntry } from '../../../../core/hall/help/errata/errata-models';
import { ErrataCardComponent } from './errata-card.component';
import { FrameLayout } from './errata-frame-layout';

/** A right-click on one of the frame's cards: which side, and the event (the view opens the card preview). */
export interface ErrataPreviewRequest {
  readonly side: 'before' | 'after';
  readonly event: MouseEvent;
}

/**
 * One card in the PC Errata viewer (PcErrataUI.frame, head, flipper, figure, note). Compact: the title and the card
 * centred in the stage, the readout of what changed below it (or beside it in a short viewer). Enlarged: the title,
 * both cards side by side ("Original" → "PC errata"), the readout below them. It fits itself to the viewer whenever its
 * size changes or an image arrives (FrameLayout), so the viewer never scrolls.
 *
 * DOM contract: `gd-errata-frame.gd-errata-frame` (`data-id`, `data-layout` = compact / full, `data-readout` = bottom
 * / side, `data-column`, `data-clipped`, `data-leaving` while it slides out) holding, enlarged, `.gd-errata-frame__head`
 * then `.gd-errata-frame__body`: compact, `.gd-errata-frame__stage` (`.gd-errata-frame__head`,
 * `.gd-errata-frame__flip` > `.gd-errata-frame__cardbox` (`data-showing` = before / after, `data-turning`, the
 * `title`) with a `gd-errata-card` per side); enlarged, `.gd-errata-frame__pair` (`figure.gd-errata-frame__figure`
 * with `figcaption` and `.gd-errata-frame__cardbox` > `gd-errata-card`, for each side, around
 * `.gd-errata-frame__arrow` with `[data-arrow=row]` and `[data-arrow=column]`); then `.gd-errata-frame__note`
 * (compact: a `.gd-errata-frame__head` too, shown when the readout is beside the card) with
 * `.gd-errata-frame__side[data-side=stats]` > `ul.gd-errata-frame__stats` > `li` (`.gd-errata-frame__stat-name`,
 * `__stat-from`, `__stat-arrow` (`aria-label` "to"), `ins`), `p.gd-errata-frame__hint`, and
 * `.gd-errata-frame__texts` > `.gd-errata-frame__side[data-side=old|new|same]` > `p.gd-errata-frame__gametext`
 * (`.gd-errata-frame__side-label`, text, `ins` for what the errata added). A head: `.gd-errata-frame__name`,
 * `.gd-errata-frame__meta` (`title`), `.gd-errata-frame__badge` (`data-badge` = new / revision).
 */
@Component({
  selector: 'gd-errata-frame',
  styleUrl: './errata-frame.component.css',
  imports: [ErrataCardComponent, NgTemplateOutlet],
  host: {
    class: 'gd-errata-frame',
    '[attr.data-id]': 'entry().id',
    '[attr.data-layout]': "full() ? 'full' : 'compact'",
    '[attr.data-leaving]': "leaving() ? '' : null",
  },
  template: `
    <ng-template #head>
      <div class="gd-errata-frame__head">
        <span class="gd-errata-frame__name">{{ entry().name }}</span>
        <span class="gd-errata-frame__meta" [title]="metaTitle()">{{ meta() }}</span>
        @if (entry().recent) {
          <span class="gd-errata-frame__badge" data-badge="new" [title]="text.NEW_BADGE_TITLE">{{
            text.NEW_BADGE
          }}</span>
        }
        @if (entry().revision >= 1) {
          <span class="gd-errata-frame__badge" data-badge="revision">{{ revision() }}</span>
        }
      </div>
    </ng-template>
    @if (full()) {
      <ng-container [ngTemplateOutlet]="head" />
    }
    <div class="gd-errata-frame__body">
      @if (full()) {
        <div class="gd-errata-frame__pair">
          <figure class="gd-errata-frame__figure">
            <figcaption>{{ text.ORIGINAL }}</figcaption>
            <div class="gd-errata-frame__cardbox" [title]="text.FIGURE_CARD_TITLE">
              <gd-errata-card
                side="before"
                [url]="images().before"
                [alt]="text.ORIGINAL"
                [missing]="text.NO_ORIGINAL_IMAGE"
                (sized)="layout()"
                (contextmenu)="contextMenu('before', $event)"
              />
            </div>
          </figure>
          <div class="gd-errata-frame__arrow" aria-hidden="true">
            <span data-arrow="row">{{ text.ENLARGED_ARROW }}</span
            ><span data-arrow="column">{{ text.ENLARGED_ARROW_COLUMN }}</span>
          </div>
          <figure class="gd-errata-frame__figure">
            <figcaption>{{ text.PC_ERRATA }}</figcaption>
            <div class="gd-errata-frame__cardbox" [title]="text.FIGURE_CARD_TITLE">
              <gd-errata-card
                side="after"
                [url]="images().after"
                [alt]="text.PC_ERRATA"
                [missing]="text.NO_IMAGE"
                (sized)="layout()"
                (contextmenu)="contextMenu('after', $event)"
              />
            </div>
          </figure>
        </div>
      } @else {
        <div class="gd-errata-frame__stage">
          <ng-container [ngTemplateOutlet]="head" />
          <div class="gd-errata-frame__flip">
            <div
              class="gd-errata-frame__cardbox"
              [title]="text.CARD_TITLE"
              [attr.data-showing]="showOriginal() ? 'before' : 'after'"
              [attr.data-turning]="turning() ? '' : null"
            >
              <gd-errata-card
                side="before"
                [url]="images().before"
                [alt]="text.ORIGINAL"
                [missing]="text.NO_ORIGINAL_IMAGE"
                (sized)="layout()"
                (contextmenu)="contextMenu('before', $event)"
              />
              <gd-errata-card
                side="after"
                [url]="images().after"
                [alt]="text.PC_ERRATA"
                [missing]="text.NO_IMAGE"
                (sized)="layout()"
                (contextmenu)="contextMenu('after', $event)"
              />
            </div>
          </div>
        </div>
      }
      <div class="gd-errata-frame__note">
        @if (!full()) {
          <ng-container [ngTemplateOutlet]="head" />
        }
        @if (readout().stats.length) {
          <div class="gd-errata-frame__side" data-side="stats">
            <ul class="gd-errata-frame__stats">
              @for (change of readout().stats; track change.name) {
                <li>
                  <span class="gd-errata-frame__stat-name">{{ change.name }}</span>
                  <span class="gd-errata-frame__stat-from">{{ change.from }}</span>
                  <span class="gd-errata-frame__stat-arrow" [attr.aria-label]="text.STAT_ARROW_LABEL">{{
                    text.STAT_ARROW
                  }}</span>
                  <ins>{{ change.to }}</ins>
                </li>
              }
            </ul>
          </div>
        }
        @if (readout().hint; as hint) {
          <p class="gd-errata-frame__hint">{{ hint }}</p>
        }
        <div class="gd-errata-frame__texts">
          @for (block of readout().texts; track block.side) {
            <div class="gd-errata-frame__side" [attr.data-side]="block.side">
              <p class="gd-errata-frame__gametext">
                <span class="gd-errata-frame__side-label">{{ block.label }}</span>
                @for (op of block.ops; track $index) {
                  @if (op.op === '+') {
                    <ins>{{ op.text }}</ins>
                  } @else {
                    <ng-container>{{ op.text }}</ng-container>
                  }
                }
              </p>
            </div>
          }
        </div>
        <span class="gd-errata-frame__more" aria-hidden="true">{{ text.CLIPPED_MORE }}</span>
      </div>
    </div>
  `,
})
export class ErrataFrameComponent {
  readonly entry = input.required<ErrataEntry>();
  readonly images = input.required<ErrataImages>();
  readonly readout = input.required<ErrataReadout>();
  /** Enlarged: both versions side by side. */
  readonly full = input(false);
  readonly showOriginal = input(false);
  /** The card is turning (a quick squeeze while it flips). */
  readonly turning = input(false);
  /** Sliding out: no longer fitted. */
  readonly leaving = input(false);
  readonly preview = output<ErrataPreviewRequest>();

  protected readonly text = {
    CARD_TITLE,
    CLIPPED_MORE,
    ENLARGED_ARROW,
    ENLARGED_ARROW_COLUMN,
    FIGURE_CARD_TITLE,
    NEW_BADGE,
    NEW_BADGE_TITLE,
    NO_IMAGE,
    NO_ORIGINAL_IMAGE,
    ORIGINAL,
    PC_ERRATA,
    STAT_ARROW,
    STAT_ARROW_LABEL,
  };
  protected readonly meta = computed(() => frameMeta(this.entry().coll, this.entry().culture, this.entry().type));
  protected readonly metaTitle = computed(() =>
    frameMetaTitle(this.entry().set, this.entry().setName, this.entry().cardNum),
  );
  protected readonly revision = computed(() => revisionBadge(this.entry().revision));

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    afterNextRender(() => {
      this.layout();
      const Observer = this.host.ownerDocument.defaultView?.ResizeObserver;
      if (!Observer) {
        return;
      }
      const observer = new Observer(() => this.layout());
      observer.observe(this.host);
      this.destroyRef.onDestroy(() => observer.disconnect());
    });
  }

  /** Fits the frame to the viewer (PcErrataUI.layout); not while it slides out. */
  layout(): void {
    if (this.leaving()) {
      return;
    }
    new FrameLayout(this.host, aspectOf(this.entry())).layout();
  }

  protected contextMenu(side: 'before' | 'after', event: MouseEvent): void {
    this.preview.emit({ side, event });
  }
}
