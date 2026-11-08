import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { Notifier } from '../core/notifier/notifier';
import { WindowNotifier } from './notifier/window-notifier';

export const deckbuilderAppConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    { provide: Notifier, useClass: WindowNotifier },
  ],
};
