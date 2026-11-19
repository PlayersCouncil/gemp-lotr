import { Component, computed, input } from '@angular/core';
import { UndoPolicy } from '../../../core/game/undo/undo-protocol';
import { tierHelp } from '../../../core/game/undo/undo-view';
import { UndoMarkComponent } from './undo-mark.component';

/**
 * What the three tiers mean, as the "i" button opens it: a line per tier with its mark, its word in bold, and what it
 * means (a revert needs the opponent's approval where anybody is asked).
 *
 * DOM contract: `gd-undo-help[data-undo=help]` (`hidden` until the "i" button opens it), a `div` per tier.
 */
@Component({
  selector: 'gd-undo-help',
  styleUrl: './undo-help.component.css',
  imports: [UndoMarkComponent],
  host: { class: 'gd-undo-help', 'data-undo': 'help' },
  template: `
    @for (line of lines(); track line.tier) {
      <div>
        <gd-undo-mark [tier]="line.tier" /><b>{{ line.word }}</b
        ><span>{{ line.means }}</span>
      </div>
    }
  `,
})
export class UndoHelpComponent {
  readonly policy = input.required<UndoPolicy | null>();
  protected readonly lines = computed(() => tierHelp(this.policy()));
}
