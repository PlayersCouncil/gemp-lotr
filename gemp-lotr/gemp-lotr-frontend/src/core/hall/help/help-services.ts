import { Provider } from '@angular/core';
import { ShareLinks } from '../share-links';
import { FormatDefinitionsApi } from './format-definitions-api.service';
import { FormatDefinitionsService } from './format-definitions.service';

/**
 * Help's own state and calls, for its route's providers: Format Definitions and its share buttons. PC Errata adds its
 * own (help/errata). How Do I Play?, Code of Conduct and What Are Leagues? are prose and need none.
 */
export const HELP_SERVICES: Provider[] = [FormatDefinitionsApi, FormatDefinitionsService, ShareLinks];
