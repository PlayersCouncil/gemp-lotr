import { Component } from '@angular/core';

/**
 * The alert box: what the game asks the player (the waiting decision's text) and the buttons to answer with. The
 * decisions come with G3 of trymarz/gemp-lotr-private#27; until then the box is there, empty.
 *
 * DOM contract: `gd-game-alert` holding `[data-game="alert"]` (the text) and `.gd-game-alert__buttons`.
 */
@Component({
  selector: 'gd-game-alert',
  styleUrl: './game-alert.component.css',
  template: `
    <div class="gd-game-alert__text" data-game="alert"></div>
    <div class="gd-game-alert__buttons"></div>
  `,
})
export class GameAlertComponent {}
