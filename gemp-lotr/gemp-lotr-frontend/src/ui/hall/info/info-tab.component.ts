import { Component } from '@angular/core';
import { HallCardInfoComponent } from '../shared/hall-card-info.component';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';

/**
 * The Server Info tab (includes/info.html #infoMain): its sub-tabs and the sub-tab's page in the hall's tab frame,
 * with the card information (a patch note's cards).
 *
 * DOM contract: `gd-info-tab` (`display: contents`) holding `gd-hall-tab-frame[data-tab=info]` with
 * `gd-hall-card-info` after the page.
 */
@Component({
  selector: 'gd-info-tab',
  imports: [HallTabFrameComponent, HallCardInfoComponent],
  host: { style: 'display: contents' },
  template: '<gd-hall-tab-frame tab="info"><gd-hall-card-info /></gd-hall-tab-frame>',
})
export class InfoTabComponent {}
