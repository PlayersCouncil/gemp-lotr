import { Routes } from '@angular/router';
import { loginRequired } from '../../core/session/login-required.guard';

/**
 * The app's pages, each loaded on first use, under the app's base href (/gemp-lotr/ng/): /gemp-lotr/ng/<path>. The
 * server answers every such address with the app's index.html (WebRequestHandler), so a page can be reloaded or
 * linked to directly.
 */
export const APP_ROUTES: Routes = [
  {
    path: 'deckbuilder',
    title: 'GEMP: LotR-TCG - Deckbuilder',
    canActivate: [loginRequired],
    loadComponent: () => import('../deckbuilder/deckbuilder-page.component').then((m) => m.DeckbuilderPageComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'deckbuilder' },
  { path: '**', redirectTo: 'deckbuilder' },
];
