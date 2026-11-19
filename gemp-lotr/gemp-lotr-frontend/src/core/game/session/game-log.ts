import { inject, Injectable, signal } from '@angular/core';
import { ChatMessage, ChatRoom } from '../../chat/chat-room.service';
import { SessionStore } from '../../platform/session-store';
import { GameMessage } from '../state/game-model';

/** A line of the game page's chat box: what a player said, or what the game reports. `html` is the server's. */
export type GameLogEntry =
  /** `said`: a line the page itself put in a player's mouth (a scripted game's message), by what it is a line of. */
  | { kind: 'chat'; message: ChatMessage; said?: string }
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
  /** Whether the game has opened (its messages are taken as they come). */
  private started = false;
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

  /** The game's new messages, as the game got them (before the game has opened, restart takes them). */
  game(messages: readonly GameMessage[]): void {
    if (!this.started) return;
    this.add(messages.map((message) => ({ kind: message.kind, html: message.text, game: true })));
  }

  /**
   * The game opened (again): the list as the page showed it before a reload, with the game's messages it hadn't shown
   * yet; or, the first time, the game's messages so far (the chat's lines it already had stay after them). The
   * feedback dialog is as it was left, or opens for the server's thanks among the new messages. `gameId` null (a
   * replay): nothing is kept.
   */
  restart(gameId: string | null, messages: readonly GameMessage[]): void {
    this.gameId = gameId;
    this.started = true;
    const saved = gameId === null ? null : (this.store.read(key(gameId)) as SavedLog | GameLogEntry[] | null);
    const asGame = (m: GameMessage): GameLogEntry => ({ kind: m.kind, html: m.text, game: true });
    const lines = Array.isArray(saved) ? saved : saved?.lines;
    if (lines) {
      const shown = lines.filter((line) => line.kind !== 'chat' && line.game).length;
      const fresh = messages.slice(shown).map(asGame);
      this.lines.set([...lines, ...fresh]);
      this.feedback.set(Array.isArray(saved) ? null : (saved?.feedback ?? null));
      this.thank(fresh);
      this.rejoining = true;
    } else {
      const fresh = messages.map(asGame);
      this.lines.update((kept) => [...fresh, ...kept.filter((line) => line.kind === 'chat')]);
      this.thank(fresh);
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

  /**
   * A line as if a player had said it, though no chat room did: what a scripted game's message says, under its
   * opponent's name. `what` names the line (a step's message): said again (after a reload, or rewritten by the
   * scenario's author), it is the same line, with the newer words.
   */
  say(what: string, from: string, html: string): void {
    const line: GameLogEntry = { kind: 'chat', message: { from, html, date: null, system: false }, said: what };
    if (this.lines().some((other) => other.kind === 'chat' && other.said === what)) {
      this.lines.update((lines) => lines.map((other) => (other.kind === 'chat' && other.said === what ? line : other)));
      this.save();
    } else {
      this.add([line]);
    }
  }

  private save(): void {
    if (this.gameId !== null) {
      const saved: SavedLog = { lines: this.lines(), feedback: this.feedback() };
      this.store.write(key(this.gameId), saved);
    }
  }

  /** The player closed the feedback dialog. */
  closeFeedback(): void {
    this.feedback.set(null);
    this.save();
  }

  /** The server's thanks among new lines opens the feedback dialog (legacy checkForEnd). */
  private thank(entries: readonly GameLogEntry[]): void {
    for (const entry of entries) {
      const html = entry.kind === 'chat' ? entry.message.html : entry.html;
      if (html.includes(FEEDBACK_MARK)) this.feedback.set(html);
    }
  }

  private add(entries: readonly GameLogEntry[]): void {
    if (entries.length === 0) return;
    this.lines.update((lines) => [...lines, ...entries]);
    // Any line that thanks the player is the end of the game (legacy checkForEnd)
    this.thank(entries);
    this.save();
  }
}

const key = (gameId: string) => `gemp.game.${gameId}.log`;

/** What is kept of the chat box per game: its lines and the feedback dialog, if open. */
interface SavedLog {
  lines: readonly GameLogEntry[];
  feedback: string | null;
}
