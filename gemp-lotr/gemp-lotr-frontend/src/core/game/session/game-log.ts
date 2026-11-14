import { inject, Injectable, signal } from '@angular/core';
import { ChatMessage, ChatRoom } from '../../chat/chat-room.service';
import { GameMessage } from '../state/game-model';

/** A line of the game page's chat box: what a player said, or what the game reports. `html` is the server's. */
export type GameLogEntry = { kind: 'chat'; message: ChatMessage } | { kind: 'message' | 'warning'; html: string };

/** The words the server's last message of a game starts with. */
const FEEDBACK_MARK = 'Thank you for playing!';

/**
 * The game page's chat box shows two things in one list, in the order they came: the room's chat and the game's own
 * messages and warnings (and the chat's and the game's problems, as warnings). The room and the game hand their lines
 * over as they get them, so two answers arriving together still keep their order. Page-scoped.
 */
@Injectable()
export class GameLog {
  private readonly lines = signal<readonly GameLogEntry[]>([]);
  readonly entries = this.lines.asReadonly();

  /** The server's thanks at the end of the game (HTML, with the feedback form's link), until the player closes it. */
  readonly feedback = signal<string | null>(null);

  constructor() {
    inject(ChatRoom).listen({
      messages: (messages) => this.add(messages.map((message) => ({ kind: 'chat', message }))),
      problem: (text) => this.warn([text]),
    });
  }

  /** The game's new messages, as the game got them. */
  game(messages: readonly GameMessage[]): void {
    this.add(messages.map((message) => ({ kind: message.kind, html: message.text })));
  }

  /** The list starts again with these (a reload puts the game's earlier messages back in front). */
  restart(messages: readonly GameMessage[]): void {
    this.lines.update((lines) => lines.filter((line) => line.kind === 'chat'));
    this.lines.update((lines) => [...messages.map((m) => ({ kind: m.kind, html: m.text })), ...lines]);
  }

  warn(lines: readonly string[]): void {
    this.add(lines.map((html) => ({ kind: 'warning', html })));
  }

  private add(entries: readonly GameLogEntry[]): void {
    if (entries.length === 0) return;
    this.lines.update((lines) => [...lines, ...entries]);
    // Any line that thanks the player is the end of the game (legacy checkForEnd)
    for (const entry of entries) {
      const html = entry.kind === 'chat' ? entry.message.html : entry.html;
      if (html.includes(FEEDBACK_MARK)) this.feedback.set(html);
    }
  }
}
