import { Component, computed, inject, linkedSignal } from '@angular/core';
import { EditCard } from '../../../core/game/table-edit/edit-board';
import { EditOp, EditSeat } from '../../../core/game/table-edit/edit-ops';
import { deckSites, siteFromDeck, siteFromSeat } from '../../../core/game/table-edit/edit-path';
import { bothSeats } from '../../../core/game/table-edit/edit-seats';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { placeBeside, Size } from '../../../core/game/table-edit/popup-place';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CardHoverDirective } from '../authoring/widgets/card-hover.directive';
import { MenuKeysDirective } from './menu-keys.directive';
import { PathPopups } from './path-popups';
import { PopupPlaceDirective } from './popup-place.directive';

/** Where the chooser is: its first list, the seats' adventure decks, or one seat's deck. */
type Step = 'start' | 'decks' | EditSeat;

/**
 * The site chooser (the path's open spot, a site's Replace…): Search… (the search window, held to sites of the
 * number where the game's sites have numbers) and From adventure deck… (Ketura's words): then whose deck, then its
 * sites to pick from, by printed number, those of the chooser's number marked. A deck the page is not shown (the other
 * seat's, without the `piles` tool) offers its site of that number, which the server finds. The site chosen is laid at
 * the number; whatever site was there goes back to its owner's adventure deck. Left (or Back) goes a step back,
 * Escape or a press outside closes it.
 *
 * DOM contract: `gd-site-chooser` holding `.gd-edit-backdrop` and `.gd-edit-chooser[data-site-chooser]` (`role=menu`,
 * `data-site` the number, `data-step` start, decks, freeps or shadow) with `.gd-edit-chooser__heading` and
 * `button[role=menuitem]`: `[data-site-chooser=search]`, `[data-site-chooser=decks]`; `[data-site-chooser=deck]
 * [data-seat]` per seat; a deck's `[data-site-chooser=site][data-alias][data-printed]` (`data-match` for the
 * number's), or `[data-site-chooser=from][data-seat]`; `[data-site-chooser=back]`.
 */
@Component({
  selector: 'gd-site-chooser',
  // (the open spot's chooser's own stylesheet: the two look alike)
  styleUrl: './chooser.component.css',
  imports: [CardHoverDirective, MenuKeysDirective, PopupPlaceDirective],
  template: `
    @if (popups.chooser(); as open) {
      <div class="gd-edit-backdrop" (pointerdown)="close()" (contextmenu)="$event.preventDefault(); close()"></div>
      <!-- (drawn afresh at each step, so its first item takes the focus) -->
      @for (now of [step()]; track now) {
        <div
          class="gd-edit-chooser"
          role="menu"
          data-site-chooser
          [attr.aria-label]="heading()"
          [attr.data-site]="open.number"
          [attr.data-step]="now"
          [gdPopupPlace]="placement()"
          gdMenuKeys
          (back)="back()"
          (escape)="close(true)"
        >
          <div class="gd-edit-chooser__heading">{{ heading() }}</div>
          @switch (now) {
            @case ('start') {
              <button
                type="button"
                role="menuitem"
                class="gd-edit-chooser__item"
                data-site-chooser="search"
                title="Find any site, with the deck builder's filters"
                (click)="popups.openSearch()"
              >
                Search…
              </button>
              <button
                type="button"
                role="menuitem"
                class="gd-edit-chooser__item"
                data-site-chooser="decks"
                title="A site from either player's adventure deck"
                (click)="step.set('decks')"
              >
                From adventure deck…
              </button>
            }
            @case ('decks') {
              @for (seat of seats(); track seat) {
                <button
                  type="button"
                  role="menuitem"
                  class="gd-edit-chooser__item"
                  data-site-chooser="deck"
                  [attr.data-seat]="seat"
                  (click)="step.set(seat)"
                >
                  {{ names()[seat] }}
                </button>
              }
            }
            @default {
              @if (sites(); as sites) {
                @for (site of sites; track site.alias) {
                  <button
                    type="button"
                    role="menuitem"
                    class="gd-edit-chooser__item"
                    data-site-chooser="site"
                    [attr.data-alias]="site.alias"
                    [attr.data-printed]="site.printed"
                    [attr.data-match]="site.printed === open.number ? '' : null"
                    [gdCardHover]="site.blueprintId"
                    [disabled]="edit.busy()"
                    (click)="lay(siteOf(open.number, site))"
                  >
                    {{ siteLabel(site) }}
                  </button>
                } @empty {
                  <div class="gd-edit-chooser__heading">No sites</div>
                }
              } @else {
                <button
                  type="button"
                  role="menuitem"
                  class="gd-edit-chooser__item"
                  data-site-chooser="from"
                  [attr.data-seat]="now"
                  title="The site of this number in that adventure deck"
                  [disabled]="edit.busy()"
                  (click)="lay(fromSeat(open.number, now))"
                >
                  Site {{ open.number }}
                </button>
              }
            }
          }
          @if (now !== 'start') {
            <button
              type="button"
              role="menuitem"
              class="gd-edit-chooser__item gd-edit-chooser__search"
              data-site-chooser="back"
              (click)="back()"
            >
              ‹ Back
            </button>
          }
        </div>
      }
    }
  `,
})
export class SiteChooserComponent {
  protected readonly popups = inject(PathPopups);
  protected readonly edit = inject(TableEdit);
  private readonly seating = inject(EditSeating);

  /** The chooser's step: its first list again each time it opens. */
  protected readonly step = linkedSignal<Step>(() => {
    this.popups.chooser();
    return 'start';
  });
  protected readonly seats = computed(() => bothSeats(this.seating.seats()));
  protected readonly names = this.seating.names;
  protected readonly heading = computed(() => {
    const open = this.popups.chooser();
    const step = this.step();
    if (!open) return '';
    if (step === 'decks') return 'Adventure deck';
    if (step !== 'start') return this.names()[step];
    return `${open.replacing ? 'Replace site' : 'Site'} ${open.number}`;
  });
  /** The sites of the deck chosen, or null where the page is not shown that deck. */
  protected readonly sites = computed(() => {
    const step = this.step();
    if (step === 'start' || step === 'decks') return null;
    return deckSites(this.edit.board(), step, this.seating.seats().human);
  });
  protected readonly placement = computed(() => {
    const anchor = this.popups.chooser()?.anchor;
    return (size: Size, room: Size) => (anchor ? placeBeside(anchor, size, room) : { left: 0, top: 0 });
  });

  protected readonly siteOf = siteFromDeck;
  protected readonly fromSeat = siteFromSeat;

  protected siteLabel(site: EditCard): string {
    return site.printed ? `${site.printed} · ${site.name}` : site.name;
  }

  /** Lays the site; the chooser closes once it is there (refused: it stays, and the refusal is said by it). */
  protected async lay(op: EditOp): Promise<void> {
    const open = this.popups.chooser();
    if (!open) return;
    if (await this.edit.apply([op], { x: open.anchor.right, y: open.anchor.top })) this.popups.closeChooser();
  }

  protected back(): void {
    const step = this.step();
    if (step === 'start') return;
    this.step.set(step === 'decks' ? 'start' : 'decks');
  }

  protected close(focusBack = false): void {
    this.popups.closeChooser(focusBack);
  }
}
