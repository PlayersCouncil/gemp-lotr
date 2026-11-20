import { GameSheets } from './phone/game-sheets';
import { PhoneBridge } from './phone/phone-bridge';
import { PhoneSettings } from './phone/phone-settings';
import { Provider } from '@angular/core';
import { ChatRoom } from '../chat/chat-room.service';
import { PageLifetime } from '../platform/page-lifetime.service';
import { GameApi } from './api/game-api.service';
import { GameAttention } from './clock/game-attention.service';
import { GameClocks } from './clock/game-clocks.service';
import { DecisionGates } from './decision/decision-gate';
import { GameDecisions } from './decision/game-decisions.service';
import { GameEnding } from './session/game-ending';
import { AuthoringApi } from './authoring/authoring-api.service';
import { Authoring } from './authoring/authoring.service';
import { AuthoringUiStore } from './authoring/authoring-ui-store';
import { ScenarioApi } from './scenario/scenario-api.service';
import { ScenarioGuide } from './scenario/scenario-guide.service';
import { ScenarioRoom } from './scenario/scenario-room';
import { GameLog } from './session/game-log';
import { GameRestore } from './session/game-restore';
import { GameSession } from './session/game-session.service';
import { GameViewState } from './session/game-view-state';
import { GameSettings } from './settings/game-settings';
import { GameCardInfo } from './table/game-card-info';
import { GameUndo } from './undo/game-undo.service';
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
    GameEnding,
    GameRestore,
    GameSettings,
    ChatRoom,
    GameLog,
    GameUndo,
    DecisionGates,
    GameDecisions,
    ScenarioApi,
    ScenarioGuide,
    ScenarioRoom,
    AuthoringApi,
    AuthoringUiStore,
    Authoring,
    GameClocks,
    GameAttention,
    GameViewState,
    GameTableState,
    GameCardViews,
    GameCardInfo,
    GameSheets,
    PhoneSettings,
    PhoneBridge,
  ];
}
