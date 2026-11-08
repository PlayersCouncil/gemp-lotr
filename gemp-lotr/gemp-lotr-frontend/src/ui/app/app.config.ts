import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { Notifier } from '../../core/notifier/notifier';
import { ThemeService } from '../../core/platform/theme.service';
import { DialogNotifier } from '../shared/notifier/dialog-notifier';
import { APP_ROUTES } from './app.routes';

/** The app's providers, shared by every page. A page's own providers go in its route (app.routes.ts). */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(APP_ROUTES),
    // Messages and questions are shown by the page's gd-notifier. App-wide, not in a route: the core services that
    // ask (DeckService, PacksService, ...) are providedIn root, and the root injector doesn't see a route's providers
    DialogNotifier,
    { provide: Notifier, useExisting: DialogNotifier },
    // Nothing is drawn until the theme's stylesheet has loaded, so the page never shows unstyled
    provideAppInitializer(() => inject(ThemeService).init()),
  ],
};
