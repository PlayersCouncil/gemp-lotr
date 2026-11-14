import { Provider } from '@angular/core';
import { ChatRoom } from '../chat/chat-room.service';
import { PageLifetime } from '../platform/page-lifetime.service';
import { GameApi } from './api/game-api.service';
import { GameLog } from './session/game-log';
import { GameRestore } from './session/game-restore';
import { GameSession } from './session/game-session.service';
import { GameViewState } from './session/game-view-state';
import { GameSettings } from './settings/game-settings';
import { GameCardViews } from './table/game-card-views';
import { GameTableState } from './table/game-table.service';

/**
 * The game page's state, for the providers of its page (as provideDeckbuilderState): created when the page opens and
 * destroyed when it closes, so the long poll stops and another game starts fresh.
 */
export function provideGameState(): Provider[] {
  return [
    PageLifetime,
    GameApi,
    GameSession,
    GameRestore,
    GameSettings,
    ChatRoom,
    GameLog,
    GameViewState,
    GameTableState,
    GameCardViews,
  ];
}
