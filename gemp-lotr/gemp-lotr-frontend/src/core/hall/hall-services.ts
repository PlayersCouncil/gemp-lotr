import { Provider } from '@angular/core';
import { ChatApi } from '../chat/chat-api.service';
import { ChatService } from '../chat/chat.service';
import { HallApi } from './hall-api.service';
import { HallComms } from './hall-comms.service';
import { HallSession } from './hall-session.service';
import { HallTablesApi } from './tables/hall-tables-api.service';
import { HallTablesService } from './tables/hall-tables.service';

/**
 * The hall's own state and calls, for its route's providers: the long-poll, the chat, the logged-in-or-not session
 * and the tables. GempHttp, PlayerInfo, Timers and LocalSettings are app-wide.
 */
export const HALL_SERVICES: Provider[] = [
  HallApi,
  HallTablesApi,
  ChatApi,
  HallComms,
  ChatService,
  HallSession,
  HallTablesService,
];
