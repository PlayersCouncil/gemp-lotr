import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { LegacyCardDataService } from './core/cards/legacy-card-data.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    // Card image tables come from the legacy data files; load them before anything renders a card
    provideAppInitializer(() => inject(LegacyCardDataService).load()),
  ],
};
