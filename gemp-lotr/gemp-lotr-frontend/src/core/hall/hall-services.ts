import { inject, Provider } from '@angular/core';
import { HallChannel } from '../platform/hall-tab.service';
import { ChatApi } from '../chat/chat-api.service';
import { ChatService } from '../chat/chat.service';
import { HallBoard } from './board/hall-board.service';
import { HallSectionSettings } from './board/hall-section-settings.service';
import { HallDeliveries, HallDeliveriesApi } from './deliveries/hall-deliveries.service';
import { HallApi } from './hall-api.service';
import { HallComms } from './hall-comms.service';
import { HallDialogs } from './hall-dialogs.service';
import { HallSession } from './hall-session.service';
import { CreateTournament, TournamentApi } from './play/create-tournament.service';
import { JoinDialog } from './play/join-dialog.service';
import { LeagueTableFlow, PlayLeaguesApi } from './play/league-table.service';
import { PlayOverlay } from './play/play-overlay.service';
import { PlayerSearch } from './play/player-picker';
import { PlayScenario, ScenarioListApi } from './play/scenarios/play-scenario.service';
import { HallQueuesApi } from './queues/hall-queues-api.service';
import { HallQueuesService } from './queues/hall-queues.service';
import { HallReadyOffers } from './ready/hall-ready-offers.service';
import { HallTablesApi } from './tables/hall-tables-api.service';
import { HallTablesService } from './tables/hall-tables.service';

/**
 * The hall's own state and calls, for its route's providers: the long-poll, the chat, the logged-in-or-not session,
 * and the Game Hall tab (its rows, sections, queues and tournaments, the Play overlay and Join dialog, the ready
 * prompts, its dialogs and the deliveries), and the hall's side of the channel other tabs look for it on
 * (HallChannel). GempHttp, PlayerInfo, Timers, LocalSettings and the Notifier are app-wide.
 */
export const HALL_SERVICES: Provider[] = [
  HallApi,
  HallTablesApi,
  ChatApi,
  HallComms,
  ChatService,
  HallSession,
  HallBoard,
  HallSectionSettings,
  HallReadyOffers,
  HallDialogs,
  HallTablesService,
  HallChannel,
  HallQueuesApi,
  HallQueuesService,
  JoinDialog,
  TournamentApi,
  CreateTournament,
  PlayLeaguesApi,
  LeagueTableFlow,
  ScenarioListApi,
  PlayScenario,
  PlayerSearch,
  PlayOverlay,
  HallDeliveriesApi,
  HallDeliveries,
];

/**
 * The Game Hall tab's services that follow the long-poll from its first answer. They subscribe when created, so the
 * hall page creates them (call this in its constructor) before the hall starts; otherwise the first snapshot would
 * be missed.
 */
export function injectGameHall(): void {
  inject(HallBoard);
  inject(HallSectionSettings);
  inject(HallTablesService);
  inject(HallQueuesService);
  inject(HallDeliveries);
}
