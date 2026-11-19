import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * The app's root: shows the page of the current address (app.routes.ts). It draws no box of its own, so a page lays
 * itself out as a child of body.
 */
@Component({
  selector: 'gemp-app',
  imports: [RouterOutlet],
  host: { style: 'display: contents' },
  template: `<router-outlet />`,
})
export class AppComponent {}
