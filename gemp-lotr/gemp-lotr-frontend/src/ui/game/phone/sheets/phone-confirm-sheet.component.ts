import { Component, inject, input } from '@angular/core';
import { GameSheets } from '../../../../core/game/phone/game-sheets';
import { Sheet } from '../../../../core/game/phone/sheet';
import { GameSession } from '../../../../core/game/session/game-session.service';

/**
 * Conceding or asking to cancel, confirmed in the phone layout's sheet: what it does, Back, and the button that does it.
 *
 * DOM contract: `p.gd-phone-confirm`, `button[data-phone=confirm-no]` and `button[data-phone=confirm-yes]`.
 */
@Component({
  selector: 'gd-phone-confirm-sheet',
  styleUrl: './phone-confirm-sheet.component.css',
  template: `
    <p class="gd-phone-confirm">{{ text() }}</p>
    <div class="gd-phone-confirm__buttons">
      <button type="button" class="gd-phone-menu__item" data-phone="confirm-no" (click)="sheets.back()">Back</button>
      <button type="button" class="gd-phone-menu__item" data-danger data-phone="confirm-yes" (click)="confirmed()">
        {{ sheet().what === 'concede' ? 'Concede' : 'Request cancel' }}
      </button>
    </div>
  `,
})
export class PhoneConfirmSheetComponent {
  readonly sheet = input.required<Extract<Sheet, { kind: 'confirm' }>>();

  protected readonly sheets = inject(GameSheets);
  private readonly session = inject(GameSession);

  protected text(): string {
    return this.sheet().what === 'concede'
      ? 'You lose this game. If you have to leave, conceding lets your opponent move on.'
      : 'The game is cancelled, with no winner, if your opponent asks for it too.';
  }

  protected confirmed(): void {
    this.sheets.closeAll();
    if (this.sheet().what === 'concede') this.session.concede();
    else this.session.cancel();
  }
}
