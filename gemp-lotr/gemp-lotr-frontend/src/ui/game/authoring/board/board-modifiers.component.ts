import { Component, computed, inject, input, signal } from '@angular/core';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import {
  defaultAmount,
  modifyArgs,
  shownModifiers,
  UNTIL_CHOICES,
  untilMark,
  unmodifyArgs,
  wordsFor,
} from '../../../../core/game/authoring/board-modifiers';
import { ViewCard } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';

/**
 * A Board card's modifiers put on by hand (em-notes.md 1): what is on it (at the board as it stands, what is in force;
 * at the board the scenario starts with, the setup's), each with × (one gathered: let go of; else all of that kind
 * taken off, `unmodify(card, what)`), then those gathered for it; and mod…, which opens a line to put one on: what
 * (the words that suit the card), by how much (a stat's signed change, a numbered keyword's number), for how long.
 * Each is gathered with the tab's other changes (`modify(...)`, `unmodify(...)`) and goes where they go.
 *
 * DOM contract: `gd-author-board-modifiers` holding a `.gd-author-mods__chip` per modifier (`data-what`, `data-until`,
 * `data-pending` for one gathered, `data-removing` for one a change gathered takes off; `button[data-action=unmodify]`
 * unless it is being taken off), `button[data-action=modify]`, and while it is
 * open `.gd-author-mods__form` with `select[data-control]` what, until, `input[data-control=amount]` where it takes
 * one, `button[data-action]` add, cancel, and `.gd-author-mods__problem` when the amount is wrong.
 */
@Component({
  selector: 'gd-author-board-modifiers',
  styleUrl: './board-modifiers.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent],
  template: `
    @for (one of shown(); track $index) {
      <span
        class="gd-author-mods__chip"
        [attr.data-what]="one.what"
        [attr.data-until]="one.until"
        [attr.data-pending]="one.op !== null ? '' : null"
        [attr.data-removing]="one.removing ? '' : null"
        [title]="one.op !== null ? 'Gathered: not yet submitted' : 'Put on by hand'"
        >{{ one.text }}
        @if (mark(one.until); as until) {
          <span class="gd-author-mods__until">{{ until }}</span>
        }
        @if (!one.removing) {
          <button
            gdAuthorButton="inline"
            data-action="unmodify"
            [title]="
              one.op !== null
                ? 'Do not put it on'
                : 'Take off what is put on it by hand of this kind (' + one.what + ')'
            "
            (click)="takeOff(one.what, one.op)"
          >
            ×
          </button>
        }
      </span>
    }
    @if (words().length > 0) {
      @if (!open()) {
        <button
          gdAuthorButton
          data-action="modify"
          title="Modify a stat or keyword of this card, for a time"
          (click)="start()"
        >
          mod…
        </button>
      } @else {
        <span class="gd-author-mods__form">
          <gd-author-select
            data-control="what"
            label="Modify"
            [options]="whatOptions()"
            [value]="what()"
            (changed)="choose($event)"
          />
          @if (word()?.number !== 'none') {
            <input
              #amountBox
              type="text"
              class="gd-author-input gd-author-mods__amount"
              data-control="amount"
              aria-label="By"
              [title]="word()?.number === 'signed' ? 'Its change, with its sign: +2, -1' : 'Its number: 1 or more'"
              [value]="amount()"
              (input)="amount.set(amountBox.value)"
              (keydown.enter)="add()"
            />
          }
          <gd-author-select
            data-control="until"
            label="How long"
            [options]="untils"
            [value]="until()"
            (changed)="until.set($event)"
          />
          <button gdAuthorButton="primary" data-action="add" title="Gather it" (click)="add()">Add</button>
          <button gdAuthorButton data-action="cancel" (click)="open.set(false)">Cancel</button>
          @if (problem(); as said) {
            <span class="gd-author-mods__problem">{{ said }}</span>
          }
        </span>
      }
    }
  `,
})
export class BoardModifiersComponent {
  readonly card = input.required<ViewCard>();
  private readonly authoring = inject(Authoring);
  protected readonly changes = inject(BoardChanges);
  protected readonly untils: readonly AuthorOption[] = UNTIL_CHOICES;
  protected readonly mark = untilMark;

  private readonly vocabulary = computed(() => this.authoring.draft()?.modify);
  protected readonly words = computed(() =>
    this.changes.deckOnly() ? [] : wordsFor(this.vocabulary(), this.card().type),
  );
  protected readonly whatOptions = computed<AuthorOption[]>(() => this.words().map((word) => [word.what, word.label]));
  protected readonly shown = computed(() => {
    const card = this.card();
    return shownModifiers(
      this.vocabulary(),
      card.modifiers,
      this.authoring.draft()?.start?.modifiers?.[card.alias],
      this.changes.inSetup(),
      this.changes.gathered()?.ops ?? [],
      card.alias,
    );
  });

  protected readonly open = signal(false);
  protected readonly what = signal('');
  protected readonly amount = signal('');
  protected readonly until = signal('');
  protected readonly problem = signal<string | null>(null);
  protected readonly word = computed(() => this.words().find((word) => word.what === this.what()));

  protected start(): void {
    const first = this.words()[0];
    this.what.set(first?.what ?? '');
    this.amount.set(defaultAmount(first));
    this.until.set('');
    this.problem.set(null);
    this.open.set(true);
  }

  protected choose(what: string): void {
    this.what.set(what);
    this.amount.set(defaultAmount(this.word()));
    this.problem.set(null);
  }

  protected add(): void {
    const word = this.word();
    if (!word) return;
    const made = modifyArgs(this.card().alias, word, this.amount(), this.until());
    if ('problem' in made) {
      this.problem.set(made.problem);
      return;
    }
    this.changes.queue('modify', made.args);
    this.open.set(false);
  }

  protected takeOff(what: string, op: number | null): void {
    if (op !== null) this.changes.undoOp(op);
    else this.changes.queue('unmodify', unmodifyArgs(this.card().alias, what));
  }
}
