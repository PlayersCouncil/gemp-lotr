import { Provider } from '@angular/core';
import { ShareLinks } from '../share-links';
import { PatchNotesApi } from './patch-notes-api.service';
import { PatchNotesService } from './patch-notes.service';
import { ServerStatsApi } from './server-stats-api.service';
import { ServerStatsService } from './server-stats.service';

/**
 * Server Info's own state and calls, for its route's providers: Server Stats, Patch Notes and its share buttons.
 * Contribute and About are prose and need none. The Notifier is the app's.
 */
export const INFO_SERVICES: Provider[] = [
  ServerStatsApi,
  ServerStatsService,
  PatchNotesApi,
  PatchNotesService,
  ShareLinks,
];
