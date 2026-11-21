import { DOCUMENT } from '@angular/common';
import {
  afterNextRender,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import {
  ALL_FORMATS,
  ALL_SETS,
  ERRATA_TITLE,
  FORMAT_FIELD,
  SET_FIELD,
  TITLE_FIELD,
  TITLE_PLACEHOLDER,
  VIEW_GROUP_LABEL,
} from '../../../../core/hall/help/errata/errata-messages';
import { errataKeyAction } from '../../../../core/hall/help/errata/errata-skim';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { CardInfoComponent } from '../../../shared/card-info/card-info.component';
import { ErrataInfoToggleComponent } from './errata-info-toggle.component';
import { ErrataTableComponent } from './errata-table.component';
import { ErrataViewerComponent } from './errata-viewer.component';
import { ERRATA_CARD_PARAM } from './errata-route-param';

/** The page is never shorter or taller than this (PcErrataUI.fit). */
const MIN_VIEW = 260;
const MAX_VIEW = 1400;
/** Narrower than this the viewer goes above the table; narrower than COMPACT the table drops its # column. */
const NARROW = 700;
const COMPACT = 1000;

/**
 * Help › PC Errata (includes/help/pc-errata.html, pcErrataUi.js PcErrataUI): the filterable, sortable table of every
 * errata'd card beside the viewer that shows one at a time. Both are as tall as the panel the page is in shows
 * (PcErrataUI.fit: `--gd-errata-view-h`), so the panel never scrolls; only the table's rows do. The page reads its
 * route (`help/errata[/<cardId>]` with the filters as query parameters) and, once used or opened from a link, writes
 * the card on show and the filters back into it (replaceUrl). The keys browse it from anywhere on the page but a field.
 *
 * DOM contract: `gd-errata-page.gd-errata-page` (`data-width` = narrow / compact / wide; `--gd-errata-view-h`) holding
 * `.gd-errata-page__layout` with `.gd-errata-page__list` (`.gd-errata-page__intro`: `.gd-errata-page__titlebar` with
 * `h1` and its `gd-errata-info-toggle`, `.gd-errata-page__views` (`role=radiogroup`) > `button.gd-errata-page__chip`
 * (`role=radio`, `aria-checked`, `data-show`; `.gd-errata-page__chip-label`, `.gd-errata-page__chip-count`);
 * `.gd-errata-page__about` (`hidden` until its (i) opens it: `.gd-errata-page__summary` and the fixed text);
 * `.gd-errata-page__filters` with a `.gd-errata-page__field` (`data-field` = q / set / format: a visually hidden
 * `label` and the `input[type=search]` or `select`) per filter and `span.gd-errata-page__status` (`aria-live`,
 * `data-empty`); then `gd-errata-table`), `gd-errata-viewer`, and `div.gd-errata-page__backdrop` (`hidden` unless
 * enlarged); then `gd-card-info`.
 */
@Component({
  selector: 'gd-errata-page',
  styleUrl: './errata-page.component.css',
  imports: [CardInfoComponent, ErrataInfoToggleComponent, ErrataTableComponent, ErrataViewerComponent],
  providers: [CardInfoService],
  host: {
    class: 'gd-errata-page',
    '[attr.data-width]': 'width()',
    '[style.--gd-errata-view-h.px]': 'viewHeight()',
    '(document:keydown)': 'key($event)',
  },
  template: `
    <div class="gd-errata-page__layout">
      <div class="gd-errata-page__list">
        <div class="gd-errata-page__intro">
          <div class="gd-errata-page__titlebar">
            <h1>
              {{ text.ERRATA_TITLE }}
              <gd-errata-info-toggle [(open)]="aboutOpen" controls="gd-errata-about" />
            </h1>
            <div class="gd-errata-page__views" role="radiogroup" [attr.aria-label]="text.VIEW_GROUP_LABEL">
              @for (chip of errata.chips(); track chip.value) {
                <button
                  type="button"
                  class="gd-errata-page__chip"
                  role="radio"
                  [attr.data-show]="chip.value"
                  [attr.aria-checked]="chip.value === errata.filters().show"
                  (click)="errata.setFilter('show', chip.value)"
                >
                  <span class="gd-errata-page__chip-label">{{ chip.label }}</span>
                  <span class="gd-errata-page__chip-count">{{ chip.count }}</span>
                </button>
              }
            </div>
          </div>
          <div id="gd-errata-about" class="gd-errata-page__about" [hidden]="!aboutOpen()">
            <p class="gd-errata-page__summary">{{ errata.summary() }}</p>
            <p>
              All errata are marked in-game with a red slash in the card's upper-left corner. Errata issued by the
              Player's Council instead of Decipher also carry the errata date in the lower-left corner of the card.
            </p>
            <p>
              Every released erratum has a full write-up of the PC's thoughts and reasons
              <a href="https://wiki.lotrtcgpc.net/wiki/PC_Errata" target="_blank" rel="noopener">on the LOTR-TCG wiki</a
              >.
            </p>
            <p><b>#</b> counts how many times the PC has revised a card.</p>
          </div>
          <div class="gd-errata-page__filters">
            @if (errata.load().kind === 'ready') {
              <div class="gd-errata-page__field" data-field="q">
                <label for="gd-errata-q">{{ text.TITLE_FIELD }}</label>
                <input
                  id="gd-errata-q"
                  type="search"
                  autocomplete="off"
                  spellcheck="false"
                  [placeholder]="text.TITLE_PLACEHOLDER"
                  [value]="errata.query()"
                  (input)="errata.setQuery(asValue($event))"
                />
              </div>
              <div class="gd-errata-page__field" data-field="set">
                <label for="gd-errata-set">{{ text.SET_FIELD }}</label>
                <select id="gd-errata-set" (change)="errata.setFilter('set', asValue($event))">
                  <option value="" [selected]="errata.filters().set === ''">{{ text.ALL_SETS }}</option>
                  @for (option of errata.setOptions(); track option.value) {
                    <option [value]="option.value" [selected]="option.value === errata.filters().set">
                      {{ option.label }}
                    </option>
                  }
                </select>
              </div>
              <div class="gd-errata-page__field" data-field="format">
                <label for="gd-errata-format">{{ text.FORMAT_FIELD }}</label>
                <select id="gd-errata-format" (change)="errata.setFilter('format', asValue($event))">
                  <option value="" [selected]="errata.filters().format === ''">{{ text.ALL_FORMATS }}</option>
                  @for (option of errata.formatOptions(); track option.value) {
                    <option [value]="option.value" [selected]="option.value === errata.filters().format">
                      {{ option.label }}
                    </option>
                  }
                </select>
              </div>
              <span
                class="gd-errata-page__status"
                aria-live="polite"
                [attr.data-empty]="errata.status().empty ? '' : null"
                >{{ errata.status().text }}</span
              >
            }
          </div>
        </div>
        <gd-errata-table />
      </div>
      <gd-errata-viewer />
      <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -- a click beside the enlarged viewer closes it; Escape does from the keyboard -->
      <div
        class="gd-errata-page__backdrop"
        [hidden]="!errata.enlarged()"
        (click)="errata.setEnlarged(false)"
        (wheel)="viewer().wheel($event)"
      ></div>
    </div>
    <gd-card-info />
  `,
})
export class ErrataPageComponent {
  protected readonly errata = inject(PcErrataService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly viewer = viewChild.required(ErrataViewerComponent);

  protected readonly text = {
    ALL_FORMATS,
    ALL_SETS,
    ERRATA_TITLE,
    FORMAT_FIELD,
    SET_FIELD,
    TITLE_FIELD,
    TITLE_PLACEHOLDER,
    VIEW_GROUP_LABEL,
  };
  protected readonly aboutOpen = signal(false);
  protected readonly viewHeight = signal(480);
  protected readonly width = signal<'narrow' | 'compact' | 'wide'>('wide');

  constructor() {
    const destroyRef = inject(DestroyRef);
    // the route: the sub-tab, or a link to a card or filters (GempErrataHelp.open)
    this.openRoute();
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(destroyRef),
      )
      .subscribe(() => this.openRoute());
    void this.errata.start();

    // once used, the address follows what is on show (updateHash)
    effect(() => {
      const address = this.errata.address();
      if (!this.errata.touched() || this.errata.load().kind !== 'ready') {
        return;
      }
      untracked(() => {
        const tree = this.router.createUrlTree([address.path], { queryParams: address.queryParams });
        if (this.router.serializeUrl(tree) !== this.router.url) {
          void this.router.navigateByUrl(tree, { replaceUrl: true });
        }
      });
    });

    // as tall as the panel shows (fit): measured whenever the panel, the page or the intro changes size
    afterNextRender(() => {
      this.fit();
      const Observer = this.document.defaultView?.ResizeObserver;
      if (!Observer) return;
      const observer = new Observer(() => this.fit());
      const panel = this.panel();
      if (panel) observer.observe(panel);
      observer.observe(this.host);
      const intro = this.host.querySelector('.gd-errata-page__intro');
      if (intro) observer.observe(intro);
      destroyRef.onDestroy(() => observer.disconnect());
    });
    destroyRef.onDestroy(() => {
      if (this.errata.enlarged()) {
        this.errata.enlarged.set(false);
      }
    });
  }

  private openRoute(): void {
    const snapshot = this.route.snapshot;
    const params: Record<string, string> = {};
    for (const key of snapshot.queryParamMap.keys) {
      params[key] = snapshot.queryParamMap.get(key) ?? '';
    }
    this.errata.openRoute({ card: snapshot.paramMap.get(ERRATA_CARD_PARAM), params });
  }

  protected asValue(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement).value;
  }

  /** The element the page sits in: the nearest that scrolls (in the hall, Help's panel around its sub-tabs). */
  private panel(): HTMLElement | null {
    const window = this.document.defaultView;
    for (let element = this.host.parentElement; element; element = element.parentElement) {
      const overflow = window?.getComputedStyle(element).overflowY ?? '';
      if (overflow === 'auto' || overflow === 'scroll') {
        return element;
      }
    }
    return this.host.closest('main');
  }

  /**
   * The page is as tall as the panel shows: the viewer and the table's pane run from where the layout starts to the
   * panel's bottom, so the panel itself never scrolls (PcErrataUI.fit). The layout stacks when the page is narrow.
   */
  private fit(): void {
    const window = this.document.defaultView;
    const panel = this.panel();
    let height = panel?.clientHeight ?? 0;
    if (!height) {
      height = window?.innerHeight ?? 600;
    }
    let top = 8;
    const layout = this.host.querySelector('.gd-errata-page__layout');
    if (panel && layout) {
      const start =
        layout.getBoundingClientRect().top - panel.getBoundingClientRect().top - panel.clientTop + panel.scrollTop;
      if (start > 0 && start < height / 2) {
        top = start;
      }
    }
    const panelStyle = panel && window ? window.getComputedStyle(panel) : null;
    const hostStyle = window ? window.getComputedStyle(this.host) : null;
    const below =
      (parseFloat(panelStyle?.paddingBottom ?? '') || 0) + (parseFloat(hostStyle?.paddingBottom ?? '') || 0);
    this.viewHeight.set(Math.max(MIN_VIEW, Math.min(MAX_VIEW, Math.floor(height - top - below - 1))));
    const width = this.host.clientWidth;
    this.width.set(width > 0 && width < NARROW ? 'narrow' : width > 0 && width < COMPACT ? 'compact' : 'wide');
  }

  /** The keys (onKey): not from a field, not under a dialog; on a tab only j, k and o. */
  protected key(event: KeyboardEvent): void {
    if (this.errata.load().kind !== 'ready' || !this.host.isConnected) {
      return;
    }
    if (event.altKey || event.ctrlKey || event.metaKey) {
      return;
    }
    const target = event.target instanceof Element ? event.target : null;
    if (target?.closest('input, select, textarea, [contenteditable]')) {
      return;
    }
    // a dialog over the hall (the card information) has the keys, Escape included
    if (this.document.querySelector('dialog[open]') || target?.closest('dialog')) {
      return;
    }
    const action = errataKeyAction(event.key, target?.matches('[role=tab]') ?? false, this.errata.enlarged());
    if (action === null) {
      return;
    }
    event.preventDefault();
    switch (action) {
      case 'close':
        this.errata.setEnlarged(false);
        break;
      case 'next':
        this.errata.step(1);
        break;
      case 'previous':
        this.errata.step(-1);
        break;
      case 'flip':
        this.errata.toggleOriginal();
        break;
      case 'first':
        this.errata.first();
        break;
      case 'last':
        this.errata.last();
        break;
    }
  }
}
