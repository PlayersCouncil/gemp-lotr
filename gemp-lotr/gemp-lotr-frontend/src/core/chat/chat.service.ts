import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { catchError, concatMap, EMPTY, firstValueFrom, Observable, Subject, Subscription } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { LocalSettings } from '../platform/local-settings.service';
import { TimerHandle, Timers } from '../platform/timers';
import { PlayerInfo } from '../session/player-info.service';
import { ChatApi } from './chat-api.service';
import { displayedSender, highlightOf, isIgnored, orderUsers, tournamentAnnouncement } from './chat-format';
import {
  CHAT_CHOICE_KEY,
  CHAT_CONNECTION_LOST,
  CHAT_INACTIVE,
  CHAT_NO_PERMISSION,
  CHAT_NOT_LOGGED_IN,
  CHAT_ROOM_CLOSED,
  chatProblem,
  HALL_CHAT_ROOM,
  ignoredStatus,
  STATUS_IGNORE_FAILED,
  STATUS_INCOGNITO,
  STATUS_NOT_IN_CHAT,
  STATUS_NOT_LOGGED_IN,
  STATUS_ONLINE,
  STATUS_UNREACHABLE,
  STATUS_VISIBILITY_FAILED,
  unignoredStatus,
} from './chat-messages';
import { ChatLine, ChatSnapshot, ChatUser, TournamentAnnouncement } from './chat-models';

/** The most lines kept; older ones go (chat.js maxMessageCount). */
export const MAX_CHAT_LINES = 500;
/** Pause between a chat answer and the next poll (chat.js chatUpdateInterval). */
export const CHAT_POLL_DELAY_MS = 100;
/** How long the user list's status line shows. */
export const USER_LIST_STATUS_MS = 6000;

/**
 * The hall chat (chat.js ChatBoxUI as the hall builds it: the "Game Hall" room, timestamps, the user list, no
 * system-message toggle). start() joins the room (GET /chat/{room}) and then long-polls it (POST) until stop() or
 * a failure: any failed join or poll adds its warning and stops the chat (`stopped`), as the legacy chat did.
 * Messages go out one at a time, in the order typed. The user list offers ignoring players and the viewer's own
 * incognito switch. See README.md for what differs from the legacy chat.
 */
@Injectable()
export class ChatService {
  private readonly api = inject(ChatApi);
  private readonly timers = inject(Timers);
  private readonly player = inject(PlayerInfo);
  private readonly settings = inject(LocalSettings);

  readonly room = HALL_CHAT_ROOM;

  private readonly lines_ = signal<ChatLine[]>([]);
  /** The chat's lines, oldest first, at most MAX_CHAT_LINES. */
  readonly lines = this.lines_.asReadonly();

  private readonly users_ = signal<ChatUser[]>([]);
  /** The user list as shown: the viewer's own entry first. */
  readonly users = computed(() => orderUsers(this.users_()));
  /** The names in the hall (for the "invite a player" picker, part 4.2). */
  readonly players = computed(() => this.users_().map((user) => user.name));

  /** The players this one ignores (struck through in the list), null until loaded. */
  readonly ignored = signal<readonly string[] | null>(null);
  /** The user list's status line ("Ignored bob."), null when none shows. */
  readonly userListStatus = signal<string | null>(null);

  /** Joined (start() was called). */
  readonly started = signal(false);
  /** A failure stopped the chat: the message box is disabled (and drawn red in the legacy chat). */
  readonly stopped = signal(false);
  /** A visitor who is not logged in: the warning shows and the box says "Log in to chat". */
  readonly loggedOut = signal(false);
  /** Whether the message box takes messages. */
  readonly canSend = computed(() => this.started() && !this.stopped() && !this.loggedOut());

  /** The Discord chat shows instead of this one (the player's last choice; Discord until they choose). */
  readonly discord = signal(this.settings.read(CHAT_CHOICE_KEY) !== 'builtin');

  private readonly announcements = new Subject<TournamentAnnouncement>();
  /** Tournament messages, for their dialog (part 4.2 decides who sees it: hallUi.js tournamentCallback). */
  readonly tournamentAnnouncements$: Observable<TournamentAnnouncement> = this.announcements.asObservable();

  /** Joined and updating (start() was called, and neither stop() nor a failure has ended it). */
  private running = false;
  private nextId = 1;
  private lastWarning: string | null = null;
  private timer: TimerHandle | null = null;
  private statusTimer: TimerHandle | null = null;
  private request: Subscription | null = null;
  private ignoresRequested = false;
  private readonly outgoing = new Subject<string>();
  private readonly sending: Subscription;

  constructor() {
    this.sending = this.outgoing
      .pipe(
        concatMap((message) =>
          this.api.send(this.room, message).pipe(
            catchError((error: unknown) => {
              this.failed(error, false);
              return EMPTY;
            }),
          ),
        ),
      )
      .subscribe();
    inject(DestroyRef).onDestroy(() => {
      this.stop();
      this.sending.unsubscribe();
      this.timers.clear(this.statusTimer);
    });
  }

  /**
   * Joins the room and keeps it updated; nothing while it is. After stop() (the page was left and is shown again) it
   * joins afresh: the room's history comes again, so the lines shown before go.
   */
  start(): void {
    if (this.running) {
      return;
    }
    if (this.started()) {
      this.lines_.set([]);
      this.users_.set([]);
      this.stopped.set(false);
      this.lastWarning = null;
    }
    this.running = true;
    this.started.set(true);
    this.loggedOut.set(false);
    this.request = this.api.join(this.room).subscribe({
      next: (snapshot) => this.received(snapshot),
      error: (error: unknown) => this.failed(error, true),
    });
  }

  /** Stops the updates (the page is left, or a failure); the lines stay until start() joins again. */
  stop(): void {
    this.running = false;
    this.timers.clear(this.timer);
    this.timer = null;
    this.request?.unsubscribe();
    this.request = null;
  }

  /** A visitor who is not logged in (before start()): the warning with its login link, and no talking. */
  showLoggedOut(): void {
    this.loggedOut.set(true);
    this.appendWarningOnce(CHAT_NOT_LOGGED_IN, true);
  }

  /**
   * Sends what was typed (Enter; Shift+Enter is a new line). Blank text is not sent: the server would take a message
   * without text for a poll.
   */
  send(text: string): void {
    if (!this.canSend() || text.trim() === '') {
      return;
    }
    this.outgoing.next(text);
  }

  /** A warning line (the hall's <error>/<response> answers are added this way too: hallUi.js processResponse). */
  appendWarning(text: string): void {
    this.append({ kind: 'warning', from: null, date: null, html: text, highlight: null, loginLink: false });
  }

  /** Shows this chat or the Discord one, and remembers the choice. */
  setDiscord(discord: boolean): void {
    this.discord.set(discord);
    this.settings.write(CHAT_CHOICE_KEY, discord ? 'discord' : 'builtin');
  }

  isIgnored(name: string): boolean {
    return isIgnored(this.ignored() ?? [], name);
  }

  /** "Ignore" / "Unignore" in a user's menu. */
  async toggleIgnore(name: string): Promise<void> {
    const ignore = !this.isIgnored(name);
    try {
      const result = await firstValueFrom(this.api.setIgnored(name, ignore));
      if (result.ignored) {
        this.ignored.set(result.ignored);
      }
      this.showStatus(result.message ?? (ignore ? ignoredStatus(name) : unignoredStatus(name)));
    } catch (error) {
      const status = error instanceof ApiError ? error.status : INVALID_RESPONSE_STATUS;
      const known: Record<number, string> = {
        0: STATUS_UNREACHABLE,
        401: STATUS_NOT_LOGGED_IN,
        500: STATUS_IGNORE_FAILED,
      };
      if (known[status]) {
        this.showStatus(known[status]);
      } else {
        this.appendWarning(chatProblem(status));
      }
    }
  }

  /** The own entry's online / incognito chip: shown at once, confirmed by the next update. */
  async setIncognito(incognito: boolean): Promise<void> {
    try {
      const now = (await firstValueFrom(this.api.setIncognito(incognito))) ?? incognito;
      this.users_.update((users) => users.map((user) => (user.self ? { ...user, incognito: now } : user)));
      this.showStatus(now ? STATUS_INCOGNITO : STATUS_ONLINE);
    } catch (error) {
      const status = error instanceof ApiError ? error.status : INVALID_RESPONSE_STATUS;
      const known: Record<number, string> = {
        0: STATUS_UNREACHABLE,
        401: STATUS_NOT_LOGGED_IN,
        409: STATUS_NOT_IN_CHAT,
        500: STATUS_VISIBILITY_FAILED,
      };
      if (known[status]) {
        this.showStatus(known[status]);
      } else {
        this.appendWarning(chatProblem(status));
      }
    }
  }

  private poll(): void {
    this.timer = null;
    this.request = this.api.poll(this.room).subscribe({
      next: (snapshot) => this.received(snapshot),
      error: (error: unknown) => this.failed(error, true),
    });
  }

  private received(snapshot: ChatSnapshot): void {
    this.request = null;
    try {
      const userName = this.player.player()?.name ?? null;
      for (const message of snapshot.messages) {
        const announcement = tournamentAnnouncement(message.from, message.html);
        if (announcement) {
          this.announcements.next(announcement);
        }
        this.append({
          kind: message.from === 'System' ? 'system' : 'chat',
          from: displayedSender(message.from),
          date: message.date,
          html: message.html,
          highlight: highlightOf(message.html, userName),
          loginLink: false,
        });
      }
      this.users_.set([...snapshot.users]);
      this.loadIgnoresOnce();
    } finally {
      if (!this.stopped()) {
        this.timer = this.timers.after(CHAT_POLL_DELAY_MS, () => this.poll());
      }
    }
  }

  /**
   * chatErrorMap: the warning (once), and the chat stops. Other statuses only say so; after a failed join or poll the
   * chat stops all the same (the legacy chat then stopped updating but left the box enabled).
   */
  private failed(error: unknown, polling: boolean): void {
    if (polling) {
      this.request = null;
    }
    const status = error instanceof ApiError ? error.status : INVALID_RESPONSE_STATUS;
    const warnings: Record<number, string> = {
      0: CHAT_CONNECTION_LOST,
      401: CHAT_NOT_LOGGED_IN,
      403: CHAT_NO_PERMISSION,
      404: CHAT_ROOM_CLOSED,
      410: CHAT_INACTIVE,
    };
    const warning = warnings[status];
    if (warning) {
      this.stopped.set(true);
      this.stop();
      this.appendWarningOnce(warning, status === 401);
    } else {
      this.appendWarning(chatProblem(status));
      if (polling) {
        this.stopped.set(true);
      }
    }
  }

  /** The ignore list, asked once, when the user list first shows; a failure leaves it unknown, quietly. */
  private loadIgnoresOnce(): void {
    if (this.ignoresRequested) {
      return;
    }
    this.ignoresRequested = true;
    this.api.ignoredPlayers().subscribe({ next: (names) => this.ignored.set(names), error: () => undefined });
  }

  private appendWarningOnce(text: string, loginLink: boolean): void {
    if (text === this.lastWarning) {
      return;
    }
    this.lastWarning = text;
    this.append({ kind: 'warning', from: null, date: null, html: text, highlight: null, loginLink });
  }

  private append(line: Omit<ChatLine, 'id'>): void {
    const added: ChatLine = { id: this.nextId++, ...line };
    this.lines_.update((lines) => [...lines, added].slice(-MAX_CHAT_LINES));
  }

  private showStatus(text: string): void {
    this.userListStatus.set(text);
    this.timers.clear(this.statusTimer);
    this.statusTimer = this.timers.after(USER_LIST_STATUS_MS, () => this.userListStatus.set(null));
  }
}
