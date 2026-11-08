import { bootstrapApplication } from '@angular/platform-browser';
import { DeckbuilderApp } from './deckbuilder-app/deckbuilder-app';
import { deckbuilderAppConfig } from './deckbuilder-app/deckbuilder-app.config';
import { DeckbuilderPageComponent } from './ui/deckbuilder/deckbuilder-page.component';
import { leaveLegacyStyles, wantsNextUi } from './ui-switch';

const nextUi = wantsNextUi(window.location.search);
if (nextUi) {
  leaveLegacyStyles(document);
}

bootstrapApplication(nextUi ? DeckbuilderPageComponent : DeckbuilderApp, deckbuilderAppConfig).catch((err) =>
  console.error(err),
);
