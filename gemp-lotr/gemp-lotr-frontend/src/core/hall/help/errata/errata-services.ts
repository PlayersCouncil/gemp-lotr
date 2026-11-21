import { Provider } from '@angular/core';
import { ErrataApi } from './errata-api.service';
import { PcErrataService } from './errata.service';

/**
 * Help › PC Errata's own state and calls, for its route's providers (`help/errata`, beside Help's HELP_SERVICES and its
 * ShareLinks, which the Share button uses). A route's providers live as long as the page, so the errata are fetched
 * once and a later visit finds the page as it was left, as the legacy page's PcErrataUI.cache and lastState did.
 */
export const ERRATA_SERVICES: Provider[] = [ErrataApi, PcErrataService];
