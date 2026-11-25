import { Component, input } from '@angular/core';
import { TableResult } from '../../../../core/hall/tables/hall-table-models';
import { RESULT_LABEL } from '../../../../core/hall/play/play-messages';

/**
 * A flow's result box ("Result: Ready."; TableFlow.showResult): text only, coloured by its kind.
 *
 * DOM contract: `gd-result-box.gd-play-form__result` holding `b.gd-play-form__result-label` and
 * `div.gd-play-form__result-box` (`role=status`, `aria-live=polite`, `data-kind` = ready, pending, success, error).
 */
@Component({
  selector: 'gd-result-box',
  host: { class: 'gd-play-form__result' },
  template: `
    <b class="gd-play-form__result-label">{{ label }}</b>
    <div class="gd-play-form__result-box" role="status" aria-live="polite" [attr.data-kind]="result().kind">
      {{ result().message }}
    </div>
  `,
})
export class ResultBoxComponent {
  readonly result = input.required<TableResult>();
  protected readonly label = RESULT_LABEL;
}
