import { bootstrapApplication } from '@angular/platform-browser';
import { DeckbuilderApp } from './deckbuilder-app/deckbuilder-app';
import { deckbuilderAppConfig } from './deckbuilder-app/deckbuilder-app.config';
import { DeckbuilderPageComponent } from './ui/deckbuilder/deckbuilder-page.component';
import { deckbuilderPageConfig } from './ui/deckbuilder/deckbuilder-page.config';
import { leaveLegacyStyles, wantsNextUi } from './ui-switch';

const nextUi = wantsNextUi(window.location.search);
if (nextUi) {
  leaveLegacyStyles(document);
}

const app = nextUi
  ? bootstrapApplication(DeckbuilderPageComponent, deckbuilderPageConfig)
  : bootstrapApplication(DeckbuilderApp, deckbuilderAppConfig);
app.catch((err) => console.error(err));
