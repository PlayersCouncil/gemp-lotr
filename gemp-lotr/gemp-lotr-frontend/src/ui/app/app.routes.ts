import { Routes } from '@angular/router';
import { Notifier } from '../../core/notifier/notifier';
import { loginRequired, loginRequiredUnlessReplay } from '../../core/session/login-required.guard';
import { DialogNotifier } from '../shared/notifier/dialog-notifier';

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
  {
    path: 'game',
    // As game.html's title; the page changes it while it waits for the player (GameAttention)
    title: 'GEMP: LotR-TCG - Game Table',
    // A replay's link needs no login (shared replays, as game.html's); a game does
    canActivate: [loginRequiredUnlessReplay],
    // The game page asks its questions in dialogs of its own (gd-notifier), not the browser's
    providers: [DialogNotifier, { provide: Notifier, useExisting: DialogNotifier }],
    loadComponent: () => import('../game/game-page.component').then((m) => m.GamePageComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'deckbuilder' },
  { path: '**', redirectTo: 'deckbuilder' },
];
