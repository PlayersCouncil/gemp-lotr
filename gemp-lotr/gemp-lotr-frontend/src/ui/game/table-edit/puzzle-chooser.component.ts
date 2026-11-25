import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { PuzzleBackend, SlotVerdict } from '../../../core/game/table-edit/puzzle-backend';
import { slotLabel } from '../../../core/game/table-edit/puzzle-slots';
import { slotTakes } from '../../../core/game/table-edit/slot-filter';
import { placeBeside, Size } from '../../../core/game/table-edit/popup-place';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';
import { CardHoverDirective } from '../authoring/widgets/card-hover.directive';
import { MenuKeysDirective } from './menu-keys.directive';
import { PopupPlaceDirective } from './popup-place.directive';
import { PuzzlePopups } from './puzzle-popups';

/** A card the chooser may offer: its blueprint, and its name where the page knows it. */
interface Candidate {
  blueprintId: string;
  name: string | null;
  /** In capitals where known (Recent): a card of a kind the slot never takes is not asked about. */
  type: string | null;
}

/**
 * The chooser of a puzzle's slot: the dropdown of the sandbox's open spots (its look, chooser.component.css) by the
 * slot, held to what the slot takes. Recent (the cards this person used lately) and In this scenario (one of each card
 * in the game) list only the cards the server says fit the slot (`POST scenarios/picks`, beside the other slots' cards:
 * PuzzleBackend.verdict), each asked about once; Search… opens the search window held to the slot's bound; Empty
 * takes the card out. A card chosen goes in the slot (TableEdit.apply, `pick`); refused, the server's words are said
 * by the slot and the chooser stays.
 *
 * DOM contract: `gd-puzzle-chooser` holding `.gd-edit-backdrop` and `.gd-edit-chooser` (`role=menu`, `data-slot`;
 * placed by gdPopupPlace) with `.gd-edit-chooser__heading`s and `button[role=menuitem]`: `[data-chooser=recent]` and
 * `[data-chooser=scenario]` (each with `data-blueprint`), `[data-chooser=search]`, and `[data-chooser=empty]` for a
 * filled slot; `.gd-edit-chooser__heading[data-checking]` while the server is asked.
 */
@Component({
  selector: 'gd-puzzle-chooser',
  styleUrl: './chooser.component.css',
  imports: [CardHoverDirective, MenuKeysDirective, PopupPlaceDirective],
  template: `
    @if (open(); as open) {
      <div class="gd-edit-backdrop" (pointerdown)="close()" (contextmenu)="$event.preventDefault(); close()"></div>
      <div
        class="gd-edit-chooser"
        role="menu"
        [attr.aria-label]="label()"
        [attr.data-slot]="open.slot.id"
        [gdPopupPlace]="placement()"
        gdMenuKeys
        (escape)="close(true)"
      >
        <div class="gd-edit-chooser__heading" [attr.data-checking]="checking() ? '' : null" [title]="open.slot.bound">
          {{ label() }}
        </div>
        @if (recent().length > 0) {
          <div class="gd-edit-chooser__heading">Recent</div>
          @for (card of recent(); track card.blueprintId) {
            <button
              type="button"
              role="menuitem"
              class="gd-edit-chooser__item"
              data-chooser="recent"
              [attr.data-blueprint]="card.blueprintId"
              [gdCardHover]="card.blueprintId"
              [disabled]="edit.busy()"
              (click)="choose(card)"
            >
              {{ card.name }}
            </button>
          }
        }
        @if (inGame().length > 0) {
          <div class="gd-edit-chooser__heading">In this scenario</div>
          @for (card of inGame(); track card.blueprintId) {
            <button
              type="button"
              role="menuitem"
              class="gd-edit-chooser__item"
              data-chooser="scenario"
              [attr.data-blueprint]="card.blueprintId"
              [gdCardHover]="card.blueprintId"
              [disabled]="edit.busy()"
              (click)="choose(card)"
            >
              {{ card.name }}
            </button>
          }
        }
        <button
          type="button"
          role="menuitem"
          class="gd-edit-chooser__item gd-edit-chooser__search"
          data-chooser="search"
          title="Find a card this slot takes, with the deck builder's filters"
          (click)="search()"
        >
          Search…
        </button>
        @if (open.card) {
          <button
            type="button"
            role="menuitem"
            class="gd-edit-chooser__item"
            data-chooser="empty"
            [title]="'Take ' + open.card.name + ' out'"
            [disabled]="edit.busy()"
            (click)="empty()"
          >
            Empty
          </button>
        }
      </div>
    }
  `,
})
export class PuzzleChooserComponent {
  protected readonly popups = inject(PuzzlePopups);
  protected readonly edit = inject(TableEdit);
  private readonly puzzle = inject(PuzzleBackend);
  private readonly recents = inject(RecentCards);
  private readonly session = inject(GameSession);
  private readonly lifetime = inject(PageLifetime);

  /** The slot whose chooser is open, with its card. */
  protected readonly open = computed(() => {
    const id = this.popups.chooser()?.slot;
    return this.puzzle.filled().find((filled) => filled.slot.id === id) ?? null;
  });
  protected readonly label = computed(() => {
    const open = this.open();
    return open ? slotLabel(open.slot) : '';
  });
  protected readonly placement = computed(() => {
    const anchor = this.popups.chooser()?.anchor;
    return (size: Size, room: Size) => (anchor ? placeBeside(anchor, size, room) : { left: 0, top: 0 });
  });

  /** The server's word on each card asked about, by slot and blueprint. */
  private readonly verdicts = signal<Readonly<Record<string, SlotVerdict>>>({});
  private readonly asking = signal(0);
  protected readonly checking = computed(() => this.asking() > 0);

  private readonly recentCandidates = computed<readonly Candidate[]>(() => this.recents.list());
  /** One of each card in the game (the server names them as it answers). */
  private readonly gameCandidates = computed<Candidate[]>(() => {
    const seen = new Set<string>();
    // (only a site slot takes a site: the path's cards are asked about for it alone)
    const site = this.open()?.slot.zone === 'site';
    return Object.values(this.session.model().cards).flatMap((card) => {
      if (seen.has(card.blueprintId) || card.blueprintId === '' || card.blueprintId.startsWith('-')) return [];
      if ((card.index !== null) !== site) return [];
      seen.add(card.blueprintId);
      return [{ blueprintId: card.blueprintId, name: null, type: null }];
    });
  });

  protected readonly recent = computed(() => this.fitting(this.recentCandidates()));
  protected readonly inGame = computed(() => {
    const listed = new Set(this.recent().map((card) => card.blueprintId));
    return this.fitting(this.gameCandidates()).filter((card) => !listed.has(card.blueprintId));
  });

  constructor() {
    // A chooser opened (or the other slots' cards changed): the server is asked about each card not yet known
    effect(() => {
      const open = this.open();
      this.puzzle.picks();
      if (!open) return;
      const slot = open.slot;
      const candidates = [...this.recentCandidates(), ...this.gameCandidates()].filter(
        (card) => card.type === null || card.type === '' || slotTakes(slot.zone, card.type),
      );
      untracked(() => {
        for (const card of candidates) {
          const key = this.key(slot.id, card.blueprintId);
          this.asking.update((count) => count + 1);
          this.lifetime.read(this.puzzle.verdict(slot.id, card.blueprintId), {
            next: (verdict) => {
              this.asking.update((count) => count - 1);
              this.verdicts.update((known) => ({ ...known, [key]: verdict }));
            },
            error: () => this.asking.update((count) => count - 1),
          });
        }
      });
    });
  }

  protected async choose(card: { blueprintId: string; name: string; type: string | null }): Promise<void> {
    const open = this.popups.chooser();
    if (!open) return;
    const at = { x: open.anchor.left, y: open.anchor.bottom };
    if (await this.edit.apply([{ op: 'pick', slot: open.slot, blueprint: card.blueprintId }], at)) {
      if (card.type) this.recents.use({ blueprintId: card.blueprintId, name: card.name, type: card.type });
      this.popups.closeChooser();
    }
  }

  protected async empty(): Promise<void> {
    const open = this.popups.chooser();
    if (open && (await this.edit.apply([{ op: 'pick', slot: open.slot, blueprint: null }], null))) {
      this.popups.closeChooser(true);
    }
  }

  protected search(): void {
    const open = this.popups.chooser();
    if (open) this.popups.openSearch(open.slot);
  }

  protected close(focusBack = false): void {
    this.popups.closeChooser(focusBack);
  }

  /** The candidates the server has said fit the open slot, named as it named them. */
  private fitting(candidates: readonly Candidate[]): { blueprintId: string; name: string; type: string | null }[] {
    const slot = this.open()?.slot.id;
    if (!slot) return [];
    const verdicts = this.verdicts();
    return candidates.flatMap((card) => {
      const verdict = verdicts[this.key(slot, card.blueprintId)];
      return verdict?.fits
        ? [{ blueprintId: card.blueprintId, name: card.name ?? verdict.name ?? card.blueprintId, type: card.type }]
        : [];
    });
  }

  private key(slot: string, blueprint: string): string {
    return `${slot} ${blueprint}`;
  }
}
