import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { LegacyCardDataService } from '../core/cards/legacy-card-data.service';
import { Notifier } from '../core/notifier/notifier';
import { WindowNotifier } from './notifier/window-notifier';

export const deckbuilderAppConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    { provide: Notifier, useClass: WindowNotifier },
    // Card image tables come from the legacy data files; load them before anything renders a card
    provideAppInitializer(() => inject(LegacyCardDataService).load()),
  ],
};
