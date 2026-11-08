import { bootstrapApplication } from '@angular/platform-browser';
import { DeckbuilderPageComponent } from './ui/deckbuilder/deckbuilder-page.component';
import { deckbuilderPageConfig } from './ui/deckbuilder/deckbuilder-page.config';

bootstrapApplication(DeckbuilderPageComponent, deckbuilderPageConfig).catch((err) => console.error(err));
