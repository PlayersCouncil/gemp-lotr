import {
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  input,
  linkedSignal,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { addAnchor, cardPartOf, fightingAnchor, PARTS, withPart } from '../../../../core/game/authoring/picking';
import { TablePicker } from '../table-picker.service';
import { AuthorButtonDirective } from './author-button.directive';

/** What a pick that replaced the field's value took: the anchor, and the one it took the place of. */
export interface AnchorPicked {
  /** What the field holds now. */
  anchor: string;
  /**
   * Null for a click on the table; for "Whichever … is fighting" the card anchor it replaced (a field that copied
   * that card, Outlines say, can follow).
   */
  replacing: string | null;
}

/** The skirmish being fought has the card just picked: whichever card is fighting there can be pointed at instead. */
interface Either {
  picked: string;
  name: string;
  side: 'freeps' | 'shadow';
}

/**
 * A field for what a message is about (the legacy scenarioAuthor.js message card's Points at, Outlines and Keeps
 * clear of, with partSelect and the skirmish-side suggestion): the anchor typed (`many`: several, separated by
 * commas), or picked on the table (TablePicker): **Pick** puts what is clicked in the field's place, **+ Pick** adds
 * it to the list. Holding one card, it offers the card's parts (`card:frodo:strength`). A card picked that fights in
 * the skirmish being fought can be swapped for whichever card fights on that side (`ui:skirmish:shadow`).
 *
 * `typed` says the text as it is typed (the host sends it when typing stops), `committed` what to send at once: a pick,
 * a part chosen, the field left or Enter. Setting `value` to what the field already shows changes nothing in it (the
 * caret and the focus stay).
 *
 * DOM contract: `gd-author-anchor` (`data-picking` while its own pick is under way) holding `.gd-author-anchor` with
 * `span.gd-author-anchor__label` (`title`) when it has a label, `input.gd-author-input.gd-author-anchor__input`,
 * `button[data-pick=replace]` (Pick; not with `addOnly`) and, with `many`, `button[data-pick=add]` (+ Pick),
 * `select.gd-author-anchor__part` while it holds one card (not with `addOnly`), and `.gd-author-anchor__either` with
 * `button[data-pick=either]` after a pick of a card in the skirmish.
 */
@Component({
  selector: 'gd-author-anchor',
  styleUrl: './anchor-field.component.css',
  imports: [AuthorButtonDirective],
  host: { '[attr.data-picking]': "mine() ? '' : null" },
  template: `
    <div class="gd-author-anchor">
      @if (label()) {
        <span class="gd-author-anchor__label" [title]="hint()">{{ label() }}</span>
      }
      <input
        #field
        type="text"
        class="gd-author-input gd-author-anchor__input"
        spellcheck="false"
        [attr.aria-label]="label() || 'Anchor'"
        [placeholder]="placeholder()"
        [title]="hint()"
        (input)="typing()"
        (change)="commit()"
        (keydown.enter)="commit()"
      />
      @if (!addOnly()) {
        <button
          gdAuthorButton
          data-pick="replace"
          title="Click the thing on the table"
          [attr.aria-pressed]="mine() && !adding()"
          (click)="pick(false)"
        >
          Pick
        </button>
      }
      @if (many()) {
        <button
          gdAuthorButton
          data-pick="add"
          title="Click another thing on the table to add it"
          [attr.aria-pressed]="mine() && adding()"
          (click)="pick(true)"
        >
          + Pick
        </button>
      }
      @if (card(); as card) {
        <select
          #partPick
          class="gd-author-input gd-author-anchor__part"
          aria-label="Which part of the card"
          title="Which part of the card"
          (change)="choosePart(partPick.value)"
        >
          <option value="" [selected]="card.part === ''">the whole card</option>
          @for (part of parts; track part) {
            <option [value]="part" [selected]="card.part === part">{{ part }}</option>
          }
        </select>
      }
    </div>
    @if (either(); as either) {
      <p
        class="gd-author-note gd-author-anchor__either"
        title="If another game can have a different card there, point at whichever is"
      >
        {{ either.name }} is {{ either.side === 'shadow' ? 'a minion' : 'the character' }} in this skirmish.
        <button
          gdAuthorButton="inline"
          data-pick="either"
          [title]="'Points at (and outlines) the cards on that side of the skirmish being fought: ' + whichever(either)"
          (click)="takeEither(either)"
        >
          Whichever {{ either.side === 'shadow' ? 'minion' : 'character' }} is fighting
        </button>
      </p>
    }
  `,
})
export class AnchorFieldComponent {
  /** The anchor (or the comma-separated anchors) the field holds. */
  readonly value = input('');
  readonly label = input('');
  /** Its explanation: the label's and the field's tooltip. */
  readonly hint = input('');
  /** Several anchors, separated by commas: + Pick adds one. */
  readonly many = input(false);
  /** Only added to by picking (+ Pick), and no card parts offered: the legacy Outlines and Keeps clear of. */
  readonly addOnly = input(false);
  readonly placeholder = input('');

  /** The text, as it is typed. */
  readonly typed = output<string>();
  /** The value to send now: picked, a part chosen, or the field left (or Enter pressed) after typing. */
  readonly committed = output<string>();
  /** What Pick (not + Pick) put in the field, or "Whichever … is fighting" (see AnchorPicked). */
  readonly picked = output<AnchorPicked>();

  private readonly picker = inject(TablePicker);
  private readonly field = viewChild.required<ElementRef<HTMLInputElement>>('field');
  protected readonly parts = PARTS;

  /** What the field shows: the value given, until it is typed in or picked. */
  private readonly text = linkedSignal(() => this.value());
  protected readonly card = computed(() => (this.addOnly() ? null : cardPartOf(this.text())));
  protected readonly mine = signal(false);
  protected readonly adding = signal(false);
  protected readonly either = signal<Either | null>(null);
  /** Typed in since it was last committed. */
  private changed = false;
  private destroyed = false;

  constructor() {
    afterRenderEffect({
      write: () => {
        const text = this.text();
        const field = this.field().nativeElement;
        // (only what differs is written: the caret stays where it is)
        if (field.value !== text) field.value = text;
      },
    });
    inject(DestroyRef).onDestroy(() => {
      this.destroyed = true;
      if (this.mine()) this.picker.cancel();
    });
  }

  protected typing(): void {
    const text = this.field().nativeElement.value;
    this.changed = true;
    this.text.set(text);
    this.either.set(null);
    this.typed.emit(text);
  }

  protected commit(): void {
    if (!this.changed) return;
    this.changed = false;
    this.committed.emit(this.field().nativeElement.value);
  }

  private put(text: string): void {
    this.changed = false;
    this.text.set(text);
    this.field().nativeElement.value = text;
    this.committed.emit(text);
  }

  protected async pick(add: boolean): Promise<void> {
    this.either.set(null);
    this.mine.set(true);
    this.adding.set(add);
    const anchor = await this.picker.pick();
    if (this.destroyed) return;
    this.mine.set(false);
    if (anchor === null) return;
    if (add) {
      this.put(addAnchor(this.field().nativeElement.value, anchor));
      return;
    }
    this.put(anchor);
    this.picked.emit({ anchor, replacing: null });
    const side = this.picker.fightingSide(anchor);
    if (side) this.either.set({ picked: anchor, name: anchor.replace(/^card:/, ''), side });
  }

  protected choosePart(part: string): void {
    const text = withPart(this.text(), part);
    if (text !== null) this.put(text);
  }

  protected whichever(either: Either): string {
    return fightingAnchor(either.side);
  }

  protected takeEither(either: Either): void {
    const anchor = fightingAnchor(either.side);
    this.either.set(null);
    this.put(anchor);
    this.picked.emit({ anchor, replacing: either.picked });
  }
}
