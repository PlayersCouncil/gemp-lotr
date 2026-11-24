import { Component, computed, inject, signal } from '@angular/core';
import { FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { siteAliases } from '../../../../core/game/authoring/board-queue';
import { puzzleLike } from '../../../../core/game/authoring/puzzle-settings';
import { addSlotPatch, SLOT_ZONE_LABELS, slotIdProblem, slotRows } from '../../../../core/game/authoring/puzzle-slots';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { CardSearchComponent } from '../widgets/card-search.component';
import { BoardSlotComponent } from './board-slot.component';

/**
 * The Board tab's slots of a puzzle (ep-notes.md P2): the cards the solver chooses before the game begins, each a
 * form of its own (board-slot), and a line to add one: where it sits, its id (a site slot: which of the scenario's
 * sites it is), and, when the puzzle has solutions (a solution fills every slot), the card each of them has in it.
 *
 * DOM contract: `gd-author-board-slots` holding `.gd-author-board__heading`, a `gd-author-board-slot` per slot (or
 * `p.gd-author-note[data-empty]`), then `.gd-author-slots__add` with `gd-author-select[data-part=new-slot-zone]`,
 * `input[data-part=new-slot-id]` (a site slot: `gd-author-select[data-part=new-slot-site]`), with solutions
 * `button[data-action=new-slot-pick]` (or `gd-author-card-search`), `button[data-action=add-slot]`, and
 * `.gd-author-slots__problem` when the id will not do.
 */
@Component({
  selector: 'gd-author-board-slots',
  styleUrl: './board-slots.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent, BoardSlotComponent, CardSearchComponent],
  template: `
    @if (shown() && authoring.draft(); as state) {
      <div class="gd-author-board__heading" title="The cards the solver chooses before the game begins">Slots</div>
      @for (row of rows(); track row.id) {
        <gd-author-board-slot [state]="state" [slot]="row" />
      } @empty {
        <p class="gd-author-note" data-empty>No slots: every card is the scenario’s.</p>
      }
      <div class="gd-author-slots__add">
        <gd-author-select
          data-part="new-slot-zone"
          label="New slot: where"
          [options]="zones()"
          [value]="zone()"
          (changed)="zone.set($event)"
        />
        @if (zone() === 'site') {
          <gd-author-select
            data-part="new-slot-site"
            label="Which site"
            hint="A site slot is one of the scenario's sites: the pick is a site of the same number"
            [options]="sites()"
            [value]="id()"
            (changed)="id.set($event)"
          />
        } @else {
          <input
            type="text"
            class="gd-author-input gd-author-slots__id"
            data-part="new-slot-id"
            aria-label="Its id"
            placeholder="id"
            title="The name its card goes by everywhere in the scenario"
            [value]="id()"
            (input)="id.set($any($event.target).value)"
            (keydown.enter)="add()"
          />
        }
        @if (solutions() > 0) {
          @if (finding()) {
            <gd-author-card-search
              [sites]="zone() === 'site'"
              (picked)="choose($event)"
              (cancelled)="finding.set(false)"
            />
          } @else {
            <button
              gdAuthorButton
              data-action="new-slot-pick"
              title="A solution fills every slot: the card each solution has in the new one"
              (click)="finding.set(true)"
            >
              {{ pick()?.name ?? 'its card…' }}
            </button>
          }
        }
        <button gdAuthorButton="primary" data-action="add-slot" [disabled]="problem() !== null" (click)="add()">
          Add slot
        </button>
        @if (problem(); as said) {
          @if (id() !== '') {
            <span class="gd-author-slots__problem">{{ said }}</span>
          }
        }
      </div>
    }
  `,
})
export class BoardSlotsComponent {
  protected readonly authoring = inject(Authoring);
  private readonly changes = inject(BoardChanges);

  protected readonly shown = computed(() => {
    const draft = this.authoring.draft()?.draft;
    return draft !== undefined && puzzleLike(draft);
  });
  protected readonly rows = computed(() => {
    const state = this.authoring.draft();
    return state ? slotRows(state.draft, state.puzzle?.slots ?? []) : [];
  });
  protected readonly zones = computed<AuthorOption[]>(() =>
    (this.authoring.draft()?.puzzle?.slotZones ?? ['hand']).map((zone) => [zone, SLOT_ZONE_LABELS[zone] ?? zone]),
  );
  private readonly siteNames = computed(() => siteAliases(this.changes.board()));
  protected readonly sites = computed<AuthorOption[]>(() => [
    ['', 'which site?'],
    ...this.siteNames().map((alias): AuthorOption => [alias, alias]),
  ]);
  protected readonly solutions = computed(() => this.authoring.draft()?.draft.solutions?.length ?? 0);

  protected readonly zone = signal('hand');
  protected readonly id = signal('');
  protected readonly pick = signal<FoundCard | null>(null);
  protected readonly finding = signal(false);
  protected readonly problem = computed(() => {
    const id = this.id().trim();
    if (id === '') return 'Its id';
    const taken = [...this.changes.aliases(), ...this.rows().map((row) => row.id)];
    return slotIdProblem(id, this.zone(), taken, this.siteNames());
  });

  protected choose(card: FoundCard): void {
    this.pick.set(card);
    this.finding.set(false);
  }

  protected async add(): Promise<void> {
    const state = this.authoring.draft();
    if (!state || this.problem() !== null) return;
    const id = this.id().trim();
    const done = await this.authoring.edit(
      addSlotPatch(state.draft, id, { zone: this.zone() }, this.pick()?.id ?? null),
    );
    if (done !== null) {
      this.id.set('');
      this.pick.set(null);
    }
  }
}
