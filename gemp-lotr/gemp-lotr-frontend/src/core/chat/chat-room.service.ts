import { Injectable, signal } from '@angular/core';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { ChatConnection } from './chat-connection';
import { chatWarning } from './chat-messages';
import { ChatMessage, ChatSnapshot, ChatUser } from './chat-models';

/** The chat's models are the shared ones (chat-models.ts); re-exported for the game page's code. */
export type { ChatMessage, ChatUser } from './chat-models';

/** Who wants to hear of the room's news the moment it comes (the signals say the same, to the view). */
export interface ChatListener {
  messages(messages: readonly ChatMessage[]): void;
  problem(text: string): void;
}

/** How long the game page's chat waits between asking for news (the legacy game page's chatUpdateInterval). */
export const GAME_CHAT_INTERVAL_MS = 3000;

/**
 * One chat room as the game page takes part in it (the legacy ChatBoxUI without its markup): joining, then asking for
 * news every `interval`, and sending messages one after another in the order they were typed (ChatConnection, shared
 * with the hall's ChatService). Its answers are the shared ChatMessage and ChatUser (parseChat); the game page keeps
 * them as they came, with no timestamps, ignore list or Discord (see README.md). A failed call stops the chat with its
 * warning (chatWarning); an answer that is not a <chat> stops it quietly, as before. Page-scoped.
 */
@Injectable()
export class ChatRoom {
  private readonly connection = new ChatConnection();

  readonly messages = signal<readonly ChatMessage[]>([]);
  readonly users = signal<readonly ChatUser[]>([]);
  /** True once the room has answered: the users are known. */
  readonly joined = signal(false);
  /** The warning after which the chat stopped, or null while it works. */
  readonly problem = signal<string | null>(null);

  private listener: ChatListener | null = null;
  private joinedRoom = false;

  /** Tells a page of each batch of messages and of the problem that stops the chat, as they come. */
  listen(listener: ChatListener): void {
    this.listener = listener;
  }

  join(room: string, interval = GAME_CHAT_INTERVAL_MS): void {
    this.joinedRoom = true;
    this.connection.connect(room, interval, {
      received: (snapshot) => this.receive(snapshot),
      failed: (error) => this.fail(error),
    });
  }

  /** Sends a message; several go one at a time, in the order they were typed. */
  send(text: string): void {
    if (!this.joinedRoom || this.problem() !== null) return;
    this.connection.send(text);
  }

  private receive(answer: ChatSnapshot): void {
    if (answer.messages.length > 0) {
      this.messages.update((messages) => [...messages, ...answer.messages]);
      this.listener?.messages(answer.messages);
    }
    this.users.set(answer.users);
    this.joined.set(true);
  }

  private fail(error: unknown): void {
    this.connection.close();
    const status = ApiError.from(error).status;
    // Not a <chat> (the legacy page's chat ignored it): the chat stops, saying nothing
    if (status === INVALID_RESPONSE_STATUS) return;
    const problem = chatWarning(status).text;
    this.problem.set(problem);
    this.listener?.problem(problem);
  }
}
