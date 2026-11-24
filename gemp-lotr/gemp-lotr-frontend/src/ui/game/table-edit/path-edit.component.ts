import { Component, computed, inject } from '@angular/core';
import { FoundCard } from '../../../core/game/table-edit/card-search';
import { siteBound, siteOfBlueprint, sitesNumbered } from '../../../core/game/table-edit/edit-path';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { PathPopups } from './path-popups';
import { SearchWindowComponent } from './search-window.component';
import { SiteChooserComponent } from './site-chooser.component';

/**
 * What the adventure path's edit controls draw over the page (in gd-table-edit): the site chooser, and the search
 * window for a site at a number (held to sites of that number where the game's sites have numbers), drawn only while
 * it is open so the page never has two search windows. A site found is laid at the number (a new card of the seat the
 * page looks from, said in the edit);
 * the window closes once it is there, a refusal is said where the chooser was.
 *
 * DOM contract: `gd-path-edit` holding `gd-site-chooser` and, while a site is searched for, `gd-edit-search`
 * (`dialog[data-dialog=edit-search]`, headed "Site <n>").
 */
@Component({
  selector: 'gd-path-edit',
  imports: [SiteChooserComponent, SearchWindowComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-site-chooser />
    @if (popups.search(); as search) {
      <gd-edit-search
        [open]="true"
        [bound]="bound()"
        [heading]="'Site ' + search.number"
        (chosen)="chosen($event)"
        (closed)="popups.closeSearch()"
      />
    }
  `,
})
export class PathEditComponent {
  protected readonly popups = inject(PathPopups);
  private readonly edit = inject(TableEdit);
  private readonly seating = inject(EditSeating);

  protected readonly bound = computed(() => {
    const search = this.popups.search();
    return search ? siteBound(search.number, sitesNumbered(this.edit.board())) : null;
  });

  protected async chosen(found: FoundCard): Promise<void> {
    const search = this.popups.search();
    if (!search) return;
    const at = { x: search.anchor.right, y: search.anchor.top };
    // (the site is the seat's the page looks from, said: a flipped page's is the other seat's)
    const site = siteOfBlueprint(search.number, found.blueprintId, this.seating.looked());
    if (await this.edit.apply([site], at)) this.popups.closeSearch();
  }
}
