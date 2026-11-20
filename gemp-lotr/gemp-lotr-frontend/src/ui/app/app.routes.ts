import { Routes } from '@angular/router';
import { HALL_SERVICES } from '../../core/hall/hall-services';
import { LOGIN_SERVICES } from '../../core/login/login-services';
import { Notifier } from '../../core/notifier/notifier';
import { loginRequired, loginRequiredUnlessReplay } from '../../core/session/login-required.guard';
import { HALL_ROUTES } from '../hall/hall.routes';
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
  {
    path: 'login',
    // As index.html's title
    title: 'GEMP: LotR-TCG - Login',
    providers: [...LOGIN_SERVICES],
    loadComponent: () => import('../login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    // The Game Hall (hall.html): its frame around the tabs, each a route of its own (hall.routes.ts); last, so the
    // pages above match first. Unknown addresses go to the Game Hall
    path: '',
    // As hall.html's title
    title: 'GEMP: LotR-TCG - Main Hall',
    providers: [...HALL_SERVICES],
    loadComponent: () => import('../hall/hall-shell.component').then((m) => m.HallShellComponent),
    children: HALL_ROUTES,
  },
];
