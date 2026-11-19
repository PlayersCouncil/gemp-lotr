import { Component, computed, input } from '@angular/core';
import { UndoTier } from '../../../core/game/undo/undo-protocol';
import { TIERS } from '../../../core/game/undo/undo-view';

/**
 * A tier's coloured mark, with a sign that says the same without the colour: ✓ green, ~ yellow, ✕ red.
 *
 * DOM contract: `gd-undo-mark[data-undo=mark]` (`data-tier`; `data-look` own, replays, revealed: its colour), its text
 * the sign.
 */
@Component({
  selector: 'gd-undo-mark',
  styleUrl: './undo-mark.component.css',
  host: { class: 'gd-undo-mark', 'data-undo': 'mark', '[attr.data-tier]': 'tier()', '[attr.data-look]': 'look()' },
  template: `{{ mark() }}`,
})
export class UndoMarkComponent {
  readonly tier = input.required<UndoTier>();
  protected readonly mark = computed(() => TIERS[this.tier()].mark);
  protected readonly look = computed(() => TIERS[this.tier()].look);
}
