import { inject, Injectable, signal } from '@angular/core';
import { ChatMessage, ChatRoom } from '../../chat/chat-room.service';
import { SessionStore } from '../../platform/session-store';
import { GameMessage } from '../state/game-model';

/** A line of the game page's chat box: what a player said, or what the game reports. `html` is the server's. */
export type GameLogEntry =
  | { kind: 'chat'; message: ChatMessage }
  /** `game`: the game's own message (not the page's warning about a problem). */
  | { kind: 'message' | 'warning'; html: string; game?: boolean };

/** The words the server's last message of a game starts with. */
const FEEDBACK_MARK = 'Thank you for playing!';

/**
 * The game page's chat box shows two things in one list, in the order they came: the room's chat and the game's own
 * messages and warnings (and the chat's and the game's problems, as warnings). The room and the game hand their lines
 * over as they get them, so two answers arriving together still keep their order. The list is kept per game in the
 * tab's session storage, so a reload shows it as it was (D10 of trymarz/gemp-lotr-private#27): the room's lines it
 * sends again on joining are the ones already shown. Page-scoped.
 */
@Injectable()
export class GameLog {
  private readonly store = inject(SessionStore);
  private readonly lines = signal<readonly GameLogEntry[]>([]);
  private gameId: string | null = null;
  /** After a reload: the room's first answer repeats what was shown. */
  private rejoining = false;
  readonly entries = this.lines.asReadonly();

  /** The server's thanks at the end of the game (HTML, with the feedback form's link), until the player closes it. */
  readonly feedback = signal<string | null>(null);

  constructor() {
    inject(ChatRoom).listen({
      messages: (messages) => this.add(this.newChat(messages).map((message) => ({ kind: 'chat', message }))),
      problem: (text) => this.warn([text]),
    });
  }

  /** The game's new messages, as the game got them. */
  game(messages: readonly GameMessage[]): void {
    this.add(messages.map((message) => ({ kind: message.kind, html: message.text, game: true })));
  }

  /**
   * The game opened (again): the list as the page showed it before a reload, with the game's messages it hadn't shown
   * yet; or, the first time, the game's messages so far (the chat's lines it already had stay after them).
   */
  restart(gameId: string, messages: readonly GameMessage[]): void {
    this.gameId = gameId;
    const saved = this.store.read(key(gameId));
    const asGame = (m: GameMessage): GameLogEntry => ({ kind: m.kind, html: m.text, game: true });
    if (Array.isArray(saved)) {
      const lines = saved as GameLogEntry[];
      const shown = lines.filter((line) => line.kind !== 'chat' && line.game).length;
      this.lines.set([...lines, ...messages.slice(shown).map(asGame)]);
      this.rejoining = true;
    } else {
      this.lines.update((lines) => [...messages.map(asGame), ...lines.filter((line) => line.kind === 'chat')]);
    }
    this.save();
  }

  /** The room's messages not shown yet: on rejoining, what it says again (the same lines, its greetings) is left out. */
  private newChat(messages: readonly ChatMessage[]): ChatMessage[] {
    const shown = this.lines().flatMap((line) => (line.kind === 'chat' ? [line.message] : []));
    const same = (a: ChatMessage, b: ChatMessage) => a.from === b.from && a.html === b.html && a.date === b.date;
    const fresh = messages.filter(
      (message) =>
        !shown.some((other) => same(message, other)) &&
        !(this.rejoining && message.system && shown.some((other) => other.system && other.html === message.html)),
    );
    this.rejoining = false;
    return fresh;
  }

  warn(lines: readonly string[]): void {
    this.add(lines.map((html) => ({ kind: 'warning', html })));
  }

  private save(): void {
    if (this.gameId !== null) this.store.write(key(this.gameId), this.lines());
  }

  private add(entries: readonly GameLogEntry[]): void {
    if (entries.length === 0) return;
    this.lines.update((lines) => [...lines, ...entries]);
    this.save();
    // Any line that thanks the player is the end of the game (legacy checkForEnd)
    for (const entry of entries) {
      const html = entry.kind === 'chat' ? entry.message.html : entry.html;
      if (html.includes(FEEDBACK_MARK)) this.feedback.set(html);
    }
  }
}

const key = (gameId: string) => `gemp.game.${gameId}.log`;
