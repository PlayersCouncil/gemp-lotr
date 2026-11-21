import { DOCUMENT } from '@angular/common';
import { Component, computed, DestroyRef, ElementRef, inject, input, OnInit, output, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { findHallTab, HallTab } from '../../../core/hall/hall-tabs';
import { HallSubTabsComponent } from '../hall-sub-tabs.component';

/**
 * A hall tab with sub-tabs (help.html #helpMain, events.html #eventsMain, info.html #infoMain, account.html
 * #accountMain: a `.hall-tab` that is a jQuery UI tabs widget of its own, with vertical sub-tabs): framed, the
 * sub-tabs at the left (sticky), the sub-tab's page beside them, and the whole tab scrolling (`scroller`, which a page
 * uses to bring something into view). Laid out as the legacy tab's boxes are: the widget's .2em padding, the 5px flex
 * gaps around the jQuery UI clearfix's ::before / ::after, 0.4em narrower than its place and running 0.4em + 2px past
 * the bottom of it, its own gradient.
 *
 * It says which sub-tab the address shows (`subTab`, the HallSubTab's slug, or null) and whether the page is in front
 * of the viewer (`pageShown`: the browser tab shown and the window focused: GempHallTabs.watch), as signals for its
 * pages and as outputs for the tab around it, for the tabs that refresh a sub-tab the viewer comes back to. Whatever
 * the tab projects (its dialogs, the card information) goes after the page.
 *
 * Inputs: `tab` (required), the tab's legacy slug (hall-tabs.ts: help, events, info, account); `page`, how the page is
 * laid out: `panel` (a jQuery UI tabs panel, 1em 1.4em of padding: Help, Server Info, My Account) or `article` (the
 * Events sub-tabs' .article: also from 3em down and to 20em of space below, at 120% of the tab's font).
 *
 * DOM contract: `gd-hall-tab-frame.gd-hall-tab-frame` (`data-tab`, `data-sub-tab`; the scroller) holding
 * `gd-hall-sub-tabs`, then `div.gd-hall-tab-frame__page` (`role=tabpanel`, `data-page`) with the routed page, then
 * the projected content.
 */
@Component({
  selector: 'gd-hall-tab-frame',
  styleUrl: './hall-tab-frame.component.css',
  imports: [HallSubTabsComponent, RouterOutlet],
  host: {
    class: 'gd-hall-tab-frame',
    '[attr.data-tab]': 'tab()',
    '[attr.data-sub-tab]': 'subTab()',
    '(document:visibilitychange)': 'pageChanged()',
    '(window:blur)': 'blurred = true; pageChanged()',
    '(window:focus)': 'blurred = false; pageChanged()',
  },
  template: `
    @if (hallTab(); as hallTab) {
      <gd-hall-sub-tabs [tabs]="hallTab.subTabs" [base]="'/' + hallTab.path" [label]="hallTab.label" />
    }
    <div class="gd-hall-tab-frame__page" role="tabpanel" [attr.data-page]="page()">
      <router-outlet />
    </div>
    <ng-content />
  `,
})
export class HallTabFrameComponent implements OnInit {
  readonly tab = input.required<string>();
  readonly page = input<'panel' | 'article'>('panel');
  /** The sub-tab the address shows changed (its slug, or null). */
  readonly subTabChange = output<string | null>();
  /** The page came in front of the viewer, or left (hidden, blurred). */
  readonly pageShownChange = output<boolean>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly document = inject(DOCUMENT);
  private readonly router = inject(Router);
  protected readonly hallTab = computed<HallTab | null>(() => findHallTab(this.tab()));
  /** The sub-tab the address shows. */
  readonly subTab = signal<string | null>(null);
  /** The browser tab shown and the window focused. */
  readonly pageShown = signal(true);
  protected blurred = false;
  /** The address's sub-tab has been told. */
  private read = false;

  constructor() {
    const routes = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.followRoute());
    inject(DestroyRef).onDestroy(() => routes.unsubscribe());
  }

  /** The first reading, once the inputs are set and the tab around listens: told whatever it is. */
  ngOnInit(): void {
    this.followRoute(true);
    this.pageChanged(true);
  }

  /** The element that scrolls the tab. */
  scroller(): HTMLElement {
    return this.host;
  }

  /** GempHallTabs.watch: the browser tab hidden, or the window blurred, counts as being away. */
  protected pageChanged(always = false): void {
    const shown = this.isPageShown();
    if (always || shown !== this.pageShown()) {
      this.pageShown.set(shown);
      this.pageShownChange.emit(shown);
    }
  }

  private isPageShown(): boolean {
    return this.document.visibilityState !== 'hidden' && !this.blurred;
  }

  /** The sub-tab of the address (/<tab>/<sub>/...; a sub-tab whose path is '' for the tab's own address). */
  private subTabOfAddress(): string | null {
    const tab = this.hallTab();
    if (!tab) return null;
    const segments = this.router.parseUrl(this.router.url).root.children['primary']?.segments ?? [];
    const path = segments[0]?.path === tab.path ? (segments[1]?.path ?? '') : null;
    return path === null ? null : (tab.subTabs.find((candidate) => candidate.path === path)?.slug ?? null);
  }

  /** Told once at the start (`first`; unless the address was read already), then on each change. */
  private followRoute(first = false): void {
    const sub = this.subTabOfAddress();
    if ((first && !this.read) || sub !== this.subTab()) {
      this.read = true;
      this.subTab.set(sub);
      this.subTabChange.emit(sub);
    }
  }
}
