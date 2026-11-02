import { bootstrapApplication } from '@angular/platform-browser';
import { DeckbuilderApp } from './deckbuilder-app/deckbuilder-app';
import { deckbuilderAppConfig } from './deckbuilder-app/deckbuilder-app.config';

bootstrapApplication(DeckbuilderApp, deckbuilderAppConfig)
  .catch((err) => console.error(err));
