import { inject, Injectable } from '@angular/core';
import { WhenFacts, whenFacts } from '../../../../core/game/authoring/terms';
import { WhenHost } from '../step/when-host';
import { OrdersEditor } from './orders-editor';

/**
 * The When composer's host for the open order's form (provided by each order's card): the pickers change the form's
 * When as typing does, and nothing is sent until Apply (the form's own model: one Apply is one fresh game).
 */
@Injectable()
export class OrderWhenHost implements WhenHost {
  private readonly editor = inject(OrdersEditor);

  whenText(path: string): string | null {
    return path === 'when' ? (this.editor.form()?.when ?? null) : null;
  }

  putWhen(path: string, value: string): void {
    if (path === 'when') this.editor.type({ when: value });
  }

  pickedWhen(): void {
    // (sent with the rest of the form, by Apply)
  }

  whenFacts(): WhenFacts {
    return whenFacts(this.editor.authoring.draft());
  }
}
