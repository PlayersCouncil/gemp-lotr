import { Component } from '@angular/core';

/**
 * Root of the Angular deck builder (opt-in preview, served at /gemp-lotr/ng/deckbuilder/).
 * Phase 1 is only an empty shell; the legacy deck builder at /gemp-lotr/deckBuild.html is still the one in use.
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styles: `
    .preview-notice {
      padding: 16px;
    }
    .preview-notice a {
      color: #a0c0ff;
    }
  `,
})
export class App {}
