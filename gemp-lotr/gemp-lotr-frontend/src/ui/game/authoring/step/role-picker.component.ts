import { Component, computed, inject, input } from '@angular/core';
import { pickersOf, putTerm } from '../../../../core/game/authoring/terms';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { WhenHost } from './when-host';

const HINT =
  'The role the seat plays at that moment: as Free Peoples on its own turns, as Shadow on the other seat’s. ' +
  'Either: whichever it is.';

/**
 * The When composer's role (A3: `as(freeps)`, `as(shadow)`), for a When, an Until or a Skip when (`path`): either
 * role, as the Free Peoples player (whose turn it is), or as the Shadow player; the term is put into the line in its
 * place (after the turn, as the server writes it), keeping the others. Shown only in a game of two sides, or where
 * the line already says a role; not for one written out in full, nor for an Until or a Skip when that says nothing yet.
 *
 * DOM contract: `gd-author-role` holding `gd-author-select[data-picker=role]` (`data-path`).
 */
@Component({
  selector: 'gd-author-role',
  host: { style: 'display: contents' },
  imports: [AuthorSelectComponent],
  template: `
    @if (picked(); as picked) {
      <gd-author-select
        data-picker="role"
        [attr.data-path]="path()"
        label="Role"
        [hint]="hint"
        [options]="options"
        [value]="picked.role"
        (changed)="pick($event)"
      />
    }
  `,
})
export class RolePickerComponent {
  /** The part it puts its term into: when, until or skipWhen. */
  readonly path = input('when');

  private readonly host = inject(WhenHost);
  protected readonly hint = HINT;
  protected readonly options: readonly AuthorOption[] = [
    ['', 'either role'],
    ['freeps', 'as Free Peoples'],
    ['shadow', 'as Shadow'],
  ];

  protected readonly picked = computed(() => {
    const value = this.host.whenText(this.path());
    if (value === null || (this.path() !== 'when' && value.trim() === '')) return null;
    const pickers = pickersOf(value);
    if (pickers === null || (!this.host.whenFacts().twoSided && pickers.role === '')) return null;
    return pickers;
  });

  protected pick(role: string): void {
    this.host.putWhen(this.path(), putTerm(this.host.whenText(this.path()), 'role', role));
    this.host.pickedWhen();
  }
}
