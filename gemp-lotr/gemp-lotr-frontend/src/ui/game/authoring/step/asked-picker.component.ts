import { Component, computed, inject, input } from '@angular/core';
import {
  Asked,
  ASKED,
  ASKED_WORDS,
  askedNamed,
  askedOf,
  OPENING_ASKED,
  putAsked,
} from '../../../../core/game/authoring/terms';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { WhenHost } from './when-host';

const HINT =
  'What the decision is for, whatever kind of decision it comes as: the choice of the next skirmish, an archery wound, ' +
  'reconciling, sanctuary healing, "make another move?", optional or required responses, choosing a site, or a ' +
  "phase's own play-or-pass; in the game's own opening, the bid, going first or second, the starting fellowship, a mulligan. " +
  'Not: any decision but that one.';

/**
 * The When composer's "what is being asked" (B5), for a When, an Until or a Skip when (`path`): a select of what a
 * decision can be for, and "not", which puts that term (`nextSkirmish`, `not(nextSkirmish)`) into the line in its
 * place, keeping the other terms, and sends it (through its WhenHost: the Step tab's form, an opponent's order). The
 * decisions of the game's own opening (the bid, going first, the starting fellowship, a mulligan) are offered where
 * the draft plays it, or the line names one. Not shown for one written out in full, nor for an Until or a Skip when
 * that says nothing yet.
 *
 * DOM contract: `gd-author-asked` holding `.gd-author-asked` (`data-path`) with `gd-author-select[data-picker=asked]`
 * and `label.gd-author-check` with `input[data-picker=not]`.
 */
@Component({
  selector: 'gd-author-asked',
  styleUrl: './asked-picker.component.css',
  imports: [AuthorSelectComponent],
  template: `
    @if (picked(); as picked) {
      <span class="gd-author-asked" [attr.data-path]="path()">
        <gd-author-select
          data-picker="asked"
          label="What is being asked"
          [hint]="hint"
          [options]="options()"
          [value]="picked.asked"
          (changed)="pick($event, picked.not)"
        />
        <label class="gd-author-check" title="Any decision but this one">
          <input
            #notBox
            type="checkbox"
            data-picker="not"
            [checked]="picked.not"
            [disabled]="picked.asked === ''"
            (change)="pick(picked.asked, notBox.checked)"
          />
          not
        </label>
      </span>
    }
  `,
})
export class AskedPickerComponent {
  /** The field of the form it puts its term into: when, until or skipWhen. */
  readonly path = input('when');

  private readonly host = inject(WhenHost);
  protected readonly hint = HINT;

  private readonly value = computed(() => this.host.whenText(this.path()));
  protected readonly picked = computed(() => {
    const value = this.value();
    return value !== null && (this.path() === 'when' || value.trim() !== '') ? askedOf(value) : null;
  });
  /** What can be picked: the opening's own only where the game plays it (or the line says one already). */
  protected readonly options = computed<readonly AuthorOption[]>(() => {
    const shown = this.picked()?.asked ?? '';
    const opening = this.host.whenFacts().opening || OPENING_ASKED.includes(shown as Asked);
    return [
      ['', 'anything asked'],
      ...ASKED.filter((asked) => opening || !OPENING_ASKED.includes(asked)).map(
        (asked) => [asked, ASKED_WORDS[asked]] as const,
      ),
    ];
  });

  protected pick(value: string, not: boolean): void {
    const asked: Asked | '' = askedNamed(value) ?? '';
    this.host.putWhen(this.path(), putAsked(this.value(), asked, asked !== '' && not));
    this.host.pickedWhen();
  }
}
