import { Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HallSubTab } from '../../core/hall/hall-tabs';

/**
 * A hall tab's sub-tabs (the vertical jQuery UI tabs inside includes/help.html, events.html, info.html and
 * account.html), as router links: each sub-tab is an address of its own (`<base>/<path>`), shown as selected while the
 * address is it or below it (`/help/formats/pc_movie` selects Format Definitions; a sub-tab whose path is '' only on
 * the tab's own address). Left and Right, Up and Down (and Home, End) move the focus between them; Enter opens one.
 * Used by each tab with sub-tabs, drawn at its left with the tab's content beside it.
 *
 * Inputs:
 * - `tabs` (required): the sub-tabs, in order (a HallTab's `subTabs`, hall-tabs.ts: `path` and the legacy `label`);
 * - `base` (required): the tab's own route, absolute ("/help");
 * - `label`: the list's name for assistive technology (the tab's label, "Help").
 *
 * DOM contract: `gd-hall-sub-tabs.gd-hall-sub-tabs` holding `div.gd-hall-sub-tabs__list` (`role=tablist`,
 * `aria-orientation=vertical`, `aria-label`) with an `a.gd-hall-sub-tabs__tab` per sub-tab (`role=tab`,
 * `data-sub-tab` = its legacy slug, `aria-selected`, `href`).
 */
@Component({
  selector: 'gd-hall-sub-tabs',
  styleUrl: './hall-sub-tabs.component.css',
  imports: [RouterLink, RouterLinkActive],
  host: { class: 'gd-hall-sub-tabs' },
  template: `
    <div class="gd-hall-sub-tabs__list" role="tablist" aria-orientation="vertical" [attr.aria-label]="label()">
      @for (tab of tabs(); track tab.slug) {
        <a
          class="gd-hall-sub-tabs__tab"
          role="tab"
          [routerLink]="link(tab)"
          routerLinkActive
          #active="routerLinkActive"
          [routerLinkActiveOptions]="{ exact: tab.path === '' }"
          [attr.aria-selected]="active.isActive"
          [attr.data-sub-tab]="tab.slug"
          (keydown)="key($event)"
          >{{ tab.label }}</a
        >
      }
    </div>
  `,
})
export class HallSubTabsComponent {
  readonly tabs = input.required<readonly HallSubTab[]>();
  readonly base = input.required<string>();
  readonly label = input<string | null>(null);

  protected link(tab: HallSubTab): string {
    return tab.path === '' ? this.base() : `${this.base()}/${tab.path}`;
  }

  /** The arrow keys, Home and End move the focus along the list (a tab list's keys). */
  protected key(event: KeyboardEvent): void {
    const current = event.currentTarget as HTMLElement;
    const all = Array.from(current.parentElement?.querySelectorAll<HTMLElement>('[role="tab"]') ?? []);
    const at = all.indexOf(current);
    const next =
      event.key === 'ArrowDown' || event.key === 'ArrowRight'
        ? (at + 1) % all.length
        : event.key === 'ArrowUp' || event.key === 'ArrowLeft'
          ? (at - 1 + all.length) % all.length
          : event.key === 'Home'
            ? 0
            : event.key === 'End'
              ? all.length - 1
              : -1;
    if (next < 0) {
      return;
    }
    event.preventDefault();
    all[next]?.focus();
  }
}
