import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { Notifier } from '../core/notifier/notifier';
import { ThemeService } from '../core/platform/theme.service';
import { WindowNotifier } from './notifier/window-notifier';

export const deckbuilderAppConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    { provide: Notifier, useClass: WindowNotifier },
    // Nothing is drawn until the theme's stylesheet has loaded, so the page never shows unstyled
    provideAppInitializer(() => inject(ThemeService).init()),
  ],
};
