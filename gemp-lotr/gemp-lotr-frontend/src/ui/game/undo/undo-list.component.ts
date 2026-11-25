import { Component, computed, inject, linkedSignal } from '@angular/core';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { UndoTier } from '../../../core/game/undo/undo-protocol';
import { goLabel, listGroups, listNote, TIERS } from '../../../core/game/undo/undo-view';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { UndoHelpComponent } from './undo-help.component';
import { UndoInfoComponent } from './undo-info.component';
import { UndoWhyComponent } from './undo-why.component';

/**
 * Revert...: the list of every place the game can be taken back to, latest first, as undo.js's list: a heading for
 * each turn and phase, a row for each place with its coloured button (✓ Undo for green, ~ or ✕ Revert for yellow and
 * red), what was done there ("Played Gimli"), and in bold what makes it yellow or red; a place that can't be asked for
 * now is off, with the reason. The "i" button explains the tiers. A green place is taken back at once; any other is
 * asked of the opponent. Not modal; it closes when the place is asked for, or with its close button.
 *
 * DOM contract: `gd-dialog` with `data-dialog=undo-list`, holding `[data-undo=list]`: `[data-undo=head]` (with
 * `gd-undo-info`), `gd-undo-help`, `[data-undo=note]` (a request pending, nothing to go back to), and
 * `[data-undo=points]` with a `[data-undo=when]` heading per turn and phase and a `[data-undo=point]` (`data-tier`)
 * per place: `button[data-undo=go]` (`data-tier`, `data-look` as `gd-undo-mark`'s), `[data-undo=label]`,
 * `gd-undo-why`, `[data-undo=off]`.
 */
@Component({
  selector: 'gd-undo-list',
  styleUrl: './undo-list.component.css',
  imports: [DialogComponent, UndoHelpComponent, UndoInfoComponent, UndoWhyComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="undo-list" heading="Revert" [modal]="false" [open]="list() !== null" (closed)="undo.closeList()">
      @if (list(); as shown) {
        <div class="gd-undo-list" data-undo="list">
          <div class="gd-undo-list__head" data-undo="head">
            <span>Go back to before:</span>
            <gd-undo-info [(open)]="help" />
          </div>
          <gd-undo-help [policy]="shown.policy" [hidden]="!help()" />
          @if (note(); as text) {
            <div class="gd-undo-list__note" data-undo="note">{{ text }}</div>
          }
          @if (groups().length > 0) {
            <div class="gd-undo-list__points" data-undo="points">
              @for (group of groups(); track $index) {
                <div class="gd-undo-list__when" data-undo="when">{{ group.when }}</div>
                @for (point of group.points; track point.entryCount) {
                  <div class="gd-undo-list__point" data-undo="point" [attr.data-tier]="point.tier">
                    <button
                      type="button"
                      class="gd-undo-list__go"
                      data-undo="go"
                      [attr.data-tier]="point.tier"
                      [attr.data-look]="look(point.tier)"
                      [disabled]="point.unavailable !== null"
                      (click)="undo.ask(point.entryCount)"
                    >
                      {{ go(point.tier) }}
                    </button>
                    <span class="gd-undo-list__label" data-undo="label">{{ point.label }}</span>
                    @if (point.reasons.length > 0) {
                      <gd-undo-why class="gd-undo-list__why" [reasons]="point.reasons" />
                    }
                    @if (point.unavailable !== null) {
                      <div class="gd-undo-list__off" data-undo="off">{{ point.unavailable }}</div>
                    }
                  </div>
                }
              }
            </div>
          }
        </div>
      }
    </gd-dialog>
  `,
})
export class UndoListComponent {
  protected readonly undo = inject(GameUndo);
  protected readonly list = this.undo.list;
  /** The explanation of the tiers: closed each time the list opens. */
  protected readonly help = linkedSignal({ source: this.list, computation: () => false });
  protected readonly note = computed(() => {
    const list = this.list();
    return list ? listNote(list.points, list.pending) : null;
  });
  /** With a request pending, no place is offered. */
  protected readonly groups = computed(() => {
    const list = this.list();
    return list && !list.pending ? listGroups(list.points) : [];
  });
  protected readonly go = goLabel;
  protected look(tier: UndoTier): string {
    return TIERS[tier].look;
  }
}
