import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { Notifier } from '../../core/notifier/notifier';
import { ThemeService } from '../../core/platform/theme.service';
import { WindowNotifier } from '../shared/notifier/window-notifier';
import { APP_ROUTES } from './app.routes';

/** The app's providers, shared by every page. A page's own providers go in its route (app.routes.ts). */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(APP_ROUTES),
    // Messages and questions use the browser's own dialogs, as the legacy page did; gd-notifier shows nothing without
    // DialogNotifier, which comes back with the redesign. App-wide, not in a route: the core services that ask
    // (DeckSaver, PacksService, ...) are providedIn root, and the root injector doesn't see a route's providers
    WindowNotifier,
    { provide: Notifier, useExisting: WindowNotifier },
    // Nothing is drawn until the theme's stylesheet has loaded, so the page never shows unstyled
    provideAppInitializer(() => inject(ThemeService).init()),
  ],
};
