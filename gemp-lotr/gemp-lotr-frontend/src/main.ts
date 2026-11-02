import { bootstrapApplication } from '@angular/platform-browser';
import { DeckbuilderApp } from './app/deckbuilder-app';
import { deckbuilderAppConfig } from './app/deckbuilder-app.config';

bootstrapApplication(DeckbuilderApp, deckbuilderAppConfig)
  .catch((err) => console.error(err));
