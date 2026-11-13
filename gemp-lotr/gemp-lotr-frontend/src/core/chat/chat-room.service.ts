import { inject, Injectable, signal } from '@angular/core';
import { ApiError } from '../api/api-error';
import { GempHttp } from '../api/gemp-http.service';
import { attr, childElements } from '../api/xml';
import { PageLifetime } from '../platform/page-lifetime.service';

/** A chat message. `html` is as the server sent it (it may hold card hints and links): the view must sanitize it. */
export interface ChatMessage {
  from: string;
  html: string;
  /** When it was said, in ms since 1970; null if the server didn't say. */
  date: number | null;
  /** System messages ("X joined the room") are shown apart. */
  system: boolean;
}

export interface ChatUser {
  name: string;
  role: string | null;
  self: boolean;
  incognito: boolean;
}

/** What one answer of /chat/{room} holds. */
export interface ChatAnswer {
  messages: readonly ChatMessage[];
  users: readonly ChatUser[];
}

/** The legacy chat's warnings for a failed call (chat.js chatErrorMap); after one, the chat stops. */
export function chatProblem(status: number): string {
  switch (status) {
    case 0:
      return 'Chat server has been closed or there was a problem with your internet connection.';
    case 401:
      return 'You are not logged in. ';
    case 403:
      return 'You have no permission to participate in this chat.';
    case 404:
      return 'Chat room is closed.';
    case 410:
      return 'You have been inactive for too long and were removed from the chat room. Refresh the page if you wish to re-enter.';
    default:
      return `There was a problem communicating with the chat server (${status}).`;
  }
}

/** A `<chat>` answer; users written the old way ("* name", "+ name") get their role from the mark. */
export function parseChat(doc: Document): ChatAnswer | null {
  const root = doc.documentElement;
  if (root.tagName !== 'chat') return null;
  const messages = childElements(root, 'message').map((message) => {
    const from = attr(message, 'from') ?? '';
    const date = attr(message, 'date');
    return {
      from,
      html: message.textContent,
      date: date === null ? null : Number(date),
      system: from === 'System',
    };
  });
  const users = childElements(root, 'user').map((user) => {
    let name = user.textContent;
    let role = attr(user, 'role');
    const marked = /^([*+]) (.+)$/.exec(name);
    if (marked) {
      name = marked[2] ?? name;
      role ??= marked[1] === '*' ? 'admin' : 'leagueAdmin';
    }
    return { name, role, self: attr(user, 'self') === 'true', incognito: attr(user, 'incognito') === 'true' };
  });
  return { messages, users };
}

/** How long the game page's chat waits between asking for news (the legacy game page's chatUpdateInterval). */
export const GAME_CHAT_INTERVAL_MS = 3000;

/**
 * One chat room as a page takes part in it (the legacy ChatBoxUI without its markup): joining, then asking for news
 * every `interval`, and sending messages one after another in the order they were typed. Page-scoped.
 */
@Injectable()
export class ChatRoom {
  private readonly http = inject(GempHttp);
  private readonly lifetime = inject(PageLifetime);

  readonly messages = signal<readonly ChatMessage[]>([]);
  readonly users = signal<readonly ChatUser[]>([]);
  /** The warning after which the chat stopped, or null while it works. */
  readonly problem = signal<string | null>(null);

  private room: string | null = null;
  private interval = GAME_CHAT_INTERVAL_MS;
  private sending: string[] = [];
  private busy = false;

  join(room: string, interval = GAME_CHAT_INTERVAL_MS): void {
    this.room = room;
    this.interval = interval;
    this.lifetime.read(this.http.getXml(this.path()), {
      next: (doc) => this.receive(doc),
      error: (error: unknown) => this.fail(error),
    });
  }

  /** Sends a message; several go one at a time, in the order they were typed. */
  send(text: string): void {
    if (!this.room || this.problem() !== null) return;
    this.sending.push(text);
    this.sendNext();
  }

  private sendNext(): void {
    const text = this.sending[0];
    if (this.busy || text === undefined) return;
    this.busy = true;
    const done = () => {
      this.busy = false;
      this.sending.shift();
      this.sendNext();
    };
    this.lifetime.write(this.http.postXml(this.path(), { message: text }), {
      next: done,
      error: (error: unknown) => {
        this.fail(error);
        done();
      },
    });
  }

  private path(): string {
    return `/chat/${encodeURIComponent(this.room ?? '')}`;
  }

  private receive(doc: Document): void {
    const answer = parseChat(doc);
    if (!answer) return;
    if (answer.messages.length > 0) this.messages.update((messages) => [...messages, ...answer.messages]);
    this.users.set(answer.users);
    this.lifetime.later(() => this.poll(), this.interval);
  }

  private poll(): void {
    if (this.problem() !== null) return;
    this.lifetime.read(this.http.postXml(this.path()), {
      next: (doc) => this.receive(doc),
      error: (error: unknown) => this.fail(error),
    });
  }

  private fail(error: unknown): void {
    this.problem.set(chatProblem(ApiError.from(error).status));
  }
}
