import { Injectable } from '@angular/core';

/** The legacy card info dialog's zoom slider is being used: a click then doesn't close the dialog. */
@Injectable({ providedIn: 'root' })
export class CardInfoSliderState {
  sliding = false;
}
