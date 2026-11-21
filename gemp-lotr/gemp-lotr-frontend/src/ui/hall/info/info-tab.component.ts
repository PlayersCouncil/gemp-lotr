import { Component } from '@angular/core';
import { HallTabFrameComponent } from '../help/hall-tab-frame.component';

/**
 * The Server Info tab (includes/info.html #infoMain): its sub-tabs and the sub-tab's page, in the hall's tab frame.
 *
 * DOM contract: `gd-info-tab` (`display: contents`) holding `gd-hall-tab-frame[data-tab=info]`.
 */
@Component({
  selector: 'gd-info-tab',
  imports: [HallTabFrameComponent],
  host: { style: 'display: contents' },
  template: '<gd-hall-tab-frame tab="info" />',
})
export class InfoTabComponent {}
