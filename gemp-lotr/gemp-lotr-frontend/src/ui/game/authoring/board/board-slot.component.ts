import {
  afterNextRender,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  signal,
  untracked,
} from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import {
  onACard,
  removeSlotPatch,
  SLOT_ZONE_LABELS,
  slotBoundPatch,
  slotNumber,
  slotPatch,
  SlotChange,
  SlotRow,
  slotSeatOptions,
} from '../../../../core/game/authoring/puzzle-slots';
import { Notifier } from '../../../../core/notifier/notifier';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { askAuthor } from '../widgets/author-confirm';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorKeepDirective } from '../widgets/author-keep.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { CardHoverDirective } from '../widgets/card-hover.directive';
import { FilterFieldComponent } from '../widgets/filter-field.component';
import { BoardRenameComponent } from './board-rename.component';
import { FindBoundComponent } from './find-bound.component';

/**
 * One of a puzzle's slots in the Board tab (ep-notes.md P2), every part of it changed at once (`top: {slots}`): its id
 * (✎ renames it, as a card is: everything that names it follows), what the page calls it, whose card it is, where it
 * sits (on which declared card, at which place in the draw deck), its bound (a deck builder filter, with how many
 * cards it finds with the pool, or why the server refused it), the wounds and culture tokens it comes with; the card
 * the game in hand has in it; × takes it out (from every solution too, after the page's own question).
 *
 * A bound handed to the panel from outside (Authoring.offerSlotBound: the table's search window) fills the Bound
 * field and is sent, as if typed there. A slot opened on the table (or its Slot… there: AuthoringUi.showSlot) has its
 * form scrolled to, the focus in its label.
 *
 * DOM contract: `.gd-author-slot` (`data-slot`, `data-zone`) holding `.gd-author-slot__head`
 * (`.gd-author-slot__id`, `button[data-action]` rename, remove; or `gd-author-board-rename`;
 * `.gd-author-slot__pick`), then `gd-author-field`s with `input[data-part=slot-label]`,
 * `gd-author-select[data-part]` slot-zone, slot-seat, slot-on, `input[data-part=slot-position]`,
 * `gd-author-filter` (data-part=slot-filter) with `gd-author-find-bound` (Find…: the card search, whose "Use as bound"
 * hands its filters to the slot), `input[data-part]` slot-tokens, slot-wounds.
 */
@Component({
  selector: 'gd-author-board-slot',
  styleUrl: './board-slot.component.css',
  imports: [
    AuthorButtonDirective,
    AuthorFieldComponent,
    AuthorKeepDirective,
    AuthorSelectComponent,
    AuthorSendDirective,
    BoardRenameComponent,
    CardHoverDirective,
    FilterFieldComponent,
    FindBoundComponent,
  ],
  template: `
    @let row = slot();
    <div class="gd-author-slot" [attr.data-slot]="row.id" [attr.data-zone]="row.slot.zone">
      <div class="gd-author-slot__head">
        @if (renaming()) {
          <gd-author-board-rename [alias]="row.id" (closed)="renaming.set(false)" />
        } @else {
          <span class="gd-author-slot__id" title="The name its card goes by in the scenario">{{ row.id }}</span>
          @if (row.slot.zone !== 'site') {
            <button
              gdAuthorButton="inline"
              data-action="rename"
              title="Rename the slot: every part of the scenario that names it (steps, solutions, messages) follows"
              (click)="renaming.set(true)"
            >
              ✎
            </button>
          }
          @if (row.info?.pick; as pick) {
            <span
              class="gd-author-slot__pick cardHint"
              [attr.value]="pick"
              [gdCardHover]="pick"
              title="The card the game in hand has in it"
              >{{ row.info.pickName ?? pick }}</span
            >
          } @else {
            <span class="gd-author-slot__pick" data-empty title="The game in hand has no card in it">(empty)</span>
          }
          <button gdAuthorButton="danger" data-action="remove" title="Take the slot out" (click)="remove()">×</button>
        }
      </div>
      <gd-author-field label="Called" hint="What the page calls it: a Rohan condition">
        <input
          type="text"
          class="gd-author-input"
          data-part="slot-label"
          [value]="row.slot.label ?? ''"
          [gdAuthorKeep]="'slot.' + row.id + '.label'"
          gdAuthorSend
          (sent)="change({ label: $event.trim() === '' ? undefined : $event })"
        />
      </gd-author-field>
      <gd-author-field label="Where" hint="Where its card is put">
        <span class="gd-author-slot__where">
          <gd-author-select
            data-part="slot-zone"
            label="Where"
            [options]="zones()"
            [value]="row.slot.zone"
            (changed)="change({ zone: $event })"
          />
          @if (row.slot.zone !== 'site') {
            <gd-author-select
              data-part="slot-seat"
              label="Whose"
              hint="Whose card it is"
              [options]="seats()"
              [value]="row.slot.seat && row.slot.seat !== human() ? row.slot.seat : ''"
              (changed)="change({ seat: $event === '' ? undefined : $any($event) })"
            />
          }
          @if (onCard()) {
            <gd-author-select
              data-part="slot-on"
              label="On"
              hint="The declared card it is on (not another slot's)"
              [options]="hosts()"
              [value]="row.slot.on ?? ''"
              (changed)="change({ on: $event === '' ? undefined : $event })"
            />
          }
          @if (row.slot.zone === 'deck') {
            <input
              type="number"
              min="1"
              class="gd-author-input gd-author-slot__number"
              data-part="slot-position"
              aria-label="Place from the top"
              title="Its place from the top of the deck as the setup leaves it: 1 is the top"
              [value]="row.slot.position ?? 1"
              gdAuthorSend
              (sent)="change({ position: number($event, 1) })"
            />
          }
        </span>
      </gd-author-field>
      <gd-author-field
        label="Bound"
        hint="The deck builder's filter its card has to pass, beside the pool: culture:ROHAN cardtype:CONDITION. Empty: anything the pool allows."
      >
        <gd-author-filter
          part="slot-filter"
          [key]="'slot.' + row.id + '.filter'"
          label="Bound"
          hint="A deck builder filter: set:4 cardtype:SITE, culture:ROHAN cardtype:ALLY, side:FREE_PEOPLE…"
          placeholder="anything the pool allows"
          [value]="offered() ?? row.slot.filter ?? ''"
          [pool]="state().puzzle?.pool ?? ''"
          [refused]="refused()"
          (sent)="bound($event)"
        />
        <gd-author-find-bound [part]="row.id" [what]="'bound of ' + row.id" (used)="offer(row.id, $event)" />
      </gd-author-field>
      <gd-author-field
        label="Comes with"
        hint="The wounds and the culture tokens (of its own culture) its card comes with"
      >
        <span class="gd-author-slot__where">
          <input
            type="number"
            min="0"
            class="gd-author-input gd-author-slot__number"
            data-part="slot-wounds"
            aria-label="Wounds"
            title="Wounds"
            [value]="row.slot.wounds ?? 0"
            gdAuthorSend
            (sent)="change({ wounds: number($event, 1) })"
          />
          <span class="gd-author-note">wounds</span>
          <input
            type="number"
            min="0"
            class="gd-author-input gd-author-slot__number"
            data-part="slot-tokens"
            aria-label="Tokens"
            title="Culture tokens of its own culture"
            [value]="row.slot.tokens ?? 0"
            gdAuthorSend
            (sent)="change({ tokens: number($event, 1) })"
          />
          <span class="gd-author-note">tokens</span>
        </span>
      </gd-author-field>
    </div>
  `,
})
export class BoardSlotComponent {
  private readonly authoring = inject(Authoring);
  private readonly changes = inject(BoardChanges);
  private readonly notifier = inject(Notifier);
  readonly state = input.required<DraftState>();
  readonly slot = input.required<SlotRow>();

  protected readonly renaming = signal(false);
  /** Why the bound sent last was refused (null once one goes through). */
  protected readonly refused = signal<string | null>(null);
  /** A bound handed from outside, shown in the field while it is sent (and kept there if it is refused). */
  protected readonly offered = signal<string | null>(null);

  protected readonly human = computed(() => this.state().draft.human ?? 'freeps');
  protected readonly zones = computed<AuthorOption[]>(() =>
    (this.state().puzzle?.slotZones ?? []).map((zone) => [zone, SLOT_ZONE_LABELS[zone] ?? zone]),
  );
  protected readonly seats = computed<AuthorOption[]>(() => slotSeatOptions(this.human()));
  protected readonly onCard = computed(() => onACard(this.slot().slot.zone));
  /** The declared cards on the table it can be put on (what the file says shown even when it is none of them). */
  protected readonly hosts = computed<AuthorOption[]>(() => {
    const on = this.slot().slot.on;
    const cards = this.changes.bearers().map((card) => card.alias);
    if (on && !cards.includes(on)) cards.push(on);
    return [['', 'which?'], ...cards.map((alias): AuthorOption => [alias, alias])];
  });

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly injector = inject(Injector);

  constructor() {
    // (a slot opened on the table, or its Slot… there: its form is shown, its label to be typed)
    effect(() => {
      if (this.authoring.ui().showSlot !== this.slot().id) return;
      untracked(() => {
        this.authoring.remember({ showSlot: null });
        // (once the form is drawn: its fields are not bound yet when this first runs)
        afterNextRender(
          () => {
            (this.host as Partial<HTMLElement>).scrollIntoView?.({ block: 'center' });
            this.host.querySelector<HTMLInputElement>('input[data-part=slot-label]')?.focus();
          },
          { injector: this.injector },
        );
      });
    });
    // (a bound handed from outside, for this slot: filled in and sent)
    effect(() => {
      const offer = this.authoring.slotBoundOffer();
      const id = this.slot().id;
      if (offer?.slot !== id) return;
      untracked(() => {
        this.authoring.tookSlotBound(offer.serial);
        this.offered.set(offer.filter);
        void this.bound(offer.filter);
      });
    });
  }

  protected number(typed: string, least: number): number | undefined {
    return slotNumber(typed, least);
  }

  protected change(change: SlotChange): void {
    void this.authoring.edit(slotPatch(this.state().draft, this.slot().id, change));
  }

  protected async bound(typed: string): Promise<void> {
    const done = await this.authoring.edit(slotBoundPatch(this.state().draft, this.slot().id, typed));
    this.refused.set(done === null ? (this.authoring.problem() ?? 'Refused') : null);
    if (done !== null) this.offered.set(null);
  }

  /** A filter found with the card search (Find…): handed to this slot as any bound from outside is. */
  protected offer(slot: string, filter: string): void {
    this.authoring.offerSlotBound(slot, filter);
  }

  protected async remove(): Promise<void> {
    const id = this.slot().id;
    const solutions = this.state().draft.solutions?.length ?? 0;
    if (
      solutions > 0 &&
      !(await askAuthor(this.notifier, `Take the slot ${id} out, and its card out of every solution?`, 'Take it out'))
    ) {
      return;
    }
    await this.authoring.edit(removeSlotPatch(this.state().draft, id));
  }
}
