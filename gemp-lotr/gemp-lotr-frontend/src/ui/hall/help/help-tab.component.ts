import { Component } from '@angular/core';
import { HallTabFrameComponent } from './hall-tab-frame.component';

/**
 * The Help tab (includes/help.html #helpMain): its sub-tabs and the sub-tab's page, in the hall's tab frame.
 *
 * DOM contract: `gd-help-tab` (`display: contents`) holding `gd-hall-tab-frame[data-tab=help]`.
 */
@Component({
  selector: 'gd-help-tab',
  imports: [HallTabFrameComponent],
  host: { style: 'display: contents' },
  template: '<gd-hall-tab-frame tab="help" />',
})
export class HelpTabComponent {}
