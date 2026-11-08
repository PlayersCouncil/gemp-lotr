import { provideHttpClient } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { Notifier } from '../../core/notifier/notifier';
import { ThemeService } from '../../core/platform/theme.service';
import { DialogNotifier } from '../shared/notifier/dialog-notifier';

/** The deck builder page's providers. */
export const deckbuilderPageConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    // Messages and questions are shown by the page's gd-notifier
    DialogNotifier,
    { provide: Notifier, useExisting: DialogNotifier },
    // Nothing is drawn until the theme's stylesheet has loaded, so the page never shows unstyled
    provideAppInitializer(() => inject(ThemeService).init()),
  ],
};
