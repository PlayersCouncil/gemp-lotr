import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { GempHttp } from '../api/gemp-http.service';
import { withPollTimeout } from '../api/poll-timeout';
import { Timers } from '../platform/timers';
import { ChatSnapshot, IgnoreResult } from './chat-models';
import { parseChat, parseIgnored, parseIgnoreResult, parseIncognito } from './chat-parsers';

/**
 * The chat's server calls (ChatRequestHandler, PlayerInfoRequestHandler), as communication.js made them
 * (named in each comment). participantId is added by GempHttp; failed calls arrive as ApiError.
 */
@Injectable()
export class ChatApi {
  private readonly http = inject(GempHttp);
  private readonly timers = inject(Timers);

  /** startChat: GET /chat/{room} joins the room; the answer holds the recent history and the user list. */
  join(room: string): Observable<ChatSnapshot> {
    return this.http.getXml(roomPath(room)).pipe(map(snapshot));
  }

  /**
   * updateChat: POST /chat/{room} without a message long-polls for new messages (about 5 s at most on the server);
   * 20 s time-out. 410 once the server dropped the player from the room (5 minutes without a poll).
   */
  poll(room: string): Observable<ChatSnapshot> {
    return withPollTimeout(this.http.postXml(roomPath(room)), this.timers).pipe(map(snapshot));
  }

  /**
   * sendChatMessage: POST /chat/{room} with the message. A message starting with "/" is a command. 400 when a command
   * failed (e.g. /ban by a non-admin). The message comes back through the poll, like everyone's.
   */
  send(room: string, message: string): Observable<void> {
    return this.http.postText(roomPath(room), { message }).pipe(map(() => undefined));
  }

  /** getIgnoredPlayers: GET /player/ignores. */
  ignoredPlayers(): Observable<string[]> {
    return this.http.getJson<unknown>('/player/ignores').pipe(map(parseIgnored));
  }

  /** ignorePlayer / unignorePlayer: POST /player/ignores/add or /remove with the name. */
  setIgnored(name: string, ignore: boolean): Observable<IgnoreResult> {
    return this.http
      .postJson<unknown>(ignore ? '/player/ignores/add' : '/player/ignores/remove', { name })
      .pipe(map(parseIgnoreResult));
  }

  /** setIncognito: POST /player/incognito; the hall chat visibility. 409 when not in the hall chat. */
  setIncognito(incognito: boolean): Observable<boolean | null> {
    return this.http
      .postJson<unknown>('/player/incognito', { incognito: incognito ? 'true' : 'false' })
      .pipe(map(parseIncognito));
  }
}

/** The room goes in the path, encoded ("Game Hall" -> "Game%20Hall", as the browser sent it); the server decodes it. */
function roomPath(room: string): string {
  return `/chat/${encodeURIComponent(room)}`;
}

function snapshot(doc: Document): ChatSnapshot {
  const result = parseChat(doc);
  if (!result) {
    throw new ApiError(INVALID_RESPONSE_STATUS, 'The chat answer is not a <chat>');
  }
  return result;
}
