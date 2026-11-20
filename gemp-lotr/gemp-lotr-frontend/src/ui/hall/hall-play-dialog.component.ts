import { Component, input, output } from '@angular/core';
import { DialogComponent } from '../shared/dialog/dialog.component';

/**
 * The Play overlay's place (hall.html #create-table-popup): a dialog the hall's frame opens when a player presses
 * Play. For now it only says the choices are on the way; part 4.2 (the Play and Join flows) replaces its content with
 * the choices (scenario, bots, casual, league, tournament) and their forms, and keeps its place in the frame.
 *
 * DOM contract: `gd-dialog[data-dialog=play]` holding `p.gd-hall-play__note` and `button.gd-button[data-action=close]`.
 */
@Component({
  selector: 'gd-hall-play-dialog',
  styleUrl: './hall-play-dialog.component.css',
  imports: [DialogComponent],
  template: `
    <gd-dialog name="play" heading="Play" [open]="open()" (closed)="closed.emit()">
      <p class="gd-hall-play__note">Opening tables comes with the next part of the new hall.</p>
      <div class="gd-dialog__actions" gdDialogActions>
        <button type="button" class="gd-button" data-action="close" (click)="closed.emit()">Close</button>
      </div>
    </gd-dialog>
  `,
})
export class HallPlayDialogComponent {
  readonly open = input(false);
  readonly closed = output();
}
