import { Component, computed, inject, input } from '@angular/core';
import { DraftSeat } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { seatCards } from '../../../../core/game/authoring/board-queue';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { BoardCardComponent } from './board-card.component';

/**
 * A seat of the Board tab: its Ring-bearer (one of its companions) and its Ring (none, or one of its One Rings),
 * changed at once, then its cards as the board will have them, by where they are (what is on the table, then the
 * hand and the piles, each of those in the order its cards come), each place headed with its count.
 *
 * DOM contract: `gd-author-board-seat` (`data-seat`) holding `.gd-author-board__heading`, `.gd-author-board-seat__ring`
 * (`gd-author-select`s, `data-ring`: bearer, ring), then per place `.gd-author-board-seat__place` (`data-zone`)
 * followed by a `gd-author-board-card` per card.
 */
@Component({
  selector: 'gd-author-board-seat',
  styleUrl: './board-seat.component.css',
  imports: [AuthorSelectComponent, BoardCardComponent],
  host: { '[attr.data-seat]': 'seat()' },
  template: `
    <div class="gd-author-board__heading">{{ seat() === 'freeps' ? 'Free Peoples seat' : 'Shadow seat' }}</div>
    <div class="gd-author-board-seat__ring">
      <span class="gd-author-label">Ring-bearer</span>
      <gd-author-select
        data-ring="bearer"
        label="Ring-bearer"
        hint="The companion who carries the Ring"
        [options]="bearers()"
        [value]="ringBearer()"
        (changed)="changes.setRing('ringBearer', seat(), $event)"
      />
      <span class="gd-author-label">Ring</span>
      <gd-author-select
        data-ring="ring"
        label="Ring"
        hint="The One Ring the Ring-bearer bears (Add a card: The One Ring, to have another one to choose)"
        [options]="rings()"
        [value]="ring()"
        (changed)="changes.setRing('ring', seat(), $event)"
      />
    </div>
    @for (entry of listed(); track entry.card.alias) {
      @if (entry.heading !== null) {
        <div class="gd-author-board-seat__place" [attr.data-zone]="entry.card.zone">{{ entry.heading }}</div>
      }
      <gd-author-board-card [entry]="entry" />
    }
  `,
})
export class BoardSeatComponent {
  readonly seat = input.required<DraftSeat>();

  protected readonly changes = inject(BoardChanges);

  protected readonly listed = computed(() => {
    const view = this.changes.view();
    return view ? seatCards(view, this.seat()) : [];
  });
  /** (the Ring selects offer the cards the board has, as the legacy panel did, not those still to be added) */
  protected readonly bearers = computed<AuthorOption[]>(() => this.changes.ringChoices(this.seat(), 'COMPANION'));
  protected readonly rings = computed<AuthorOption[]>(() => [
    ['', 'none'],
    ...this.changes.ringChoices(this.seat(), 'THE_ONE_RING'),
  ]);
  protected readonly ringBearer = computed(() => this.changes.ringOf('ringBearer', this.seat()));
  protected readonly ring = computed(() => this.changes.ringOf('ring', this.seat()));
}
