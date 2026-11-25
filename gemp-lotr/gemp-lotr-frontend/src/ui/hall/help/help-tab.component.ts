import { Component } from '@angular/core';
import { HallCardInfoComponent } from '../shared/hall-card-info.component';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';

/**
 * The Help tab (includes/help.html #helpMain): its sub-tabs and the sub-tab's page in the hall's tab frame, with the
 * card information (and a card share link's card).
 *
 * DOM contract: `gd-help-tab` (`display: contents`) holding `gd-hall-tab-frame[data-tab=help]` with
 * `gd-hall-card-info` after the page.
 */
@Component({
  selector: 'gd-help-tab',
  imports: [HallTabFrameComponent, HallCardInfoComponent],
  host: { style: 'display: contents' },
  template: '<gd-hall-tab-frame tab="help"><gd-hall-card-info /></gd-hall-tab-frame>',
})
export class HelpTabComponent {}
