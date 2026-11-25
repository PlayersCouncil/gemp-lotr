import { DestroyRef, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { TimerHandle, Timers } from '../platform/timers';
import { ChatApi } from './chat-api.service';
import { ChatSnapshot } from './chat-models';

/** What a chat connection tells the page's chat service. */
export interface ChatConnectionHandler {
  /** An answer of the join or of a poll: the new messages and the whole user list. */
  received(snapshot: ChatSnapshot): void;
  /**
   * A call failed (an ApiError, or another error). `polling`: the join or a poll, after which the updates have
   * stopped; otherwise a message that was not sent (the updates go on unless the handler closes the connection).
   */
  failed(error: unknown, polling: boolean): void;
}

/**
 * Taking part in one chat room, as chat.js ChatBoxUI and communication.js did it on every page: join (GET
 * /chat/{room}), then after each answer wait `delayMs` and long-poll (POST), until close() or a failed join or poll;
 * messages go out one at a time, in the order typed, each after the previous one's answer. What the answers mean is
 * the handler's (the hall's ChatService, the game page's ChatRoom); this is the part they share.
 *
 * Not a service of its own: a page's chat service creates it in its constructor (`new ChatConnection()`, an
 * injection context), so it lives and dies with that service. When the service's injector goes, the updates stop and
 * a message being sent is left to finish, its answer ignored (as PageLifetime.write).
 */
export class ChatConnection {
  private readonly api = new ChatApi();
  private readonly timers = inject(Timers);

  private room: string | null = null;
  private delayMs = 0;
  private handler: ChatConnectionHandler | null = null;
  private running = false;
  private destroyed = false;
  private timer: TimerHandle | null = null;
  private request: Subscription | null = null;
  private readonly outgoing: string[] = [];
  private sending = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.close();
      this.destroyed = true;
    });
  }

  /** Joined and updating: open() was called, and neither close() nor a failed join or poll has ended it. */
  get open(): boolean {
    return this.running;
  }

  /** Joins `room` and keeps asking for its news, `delayMs` after each answer. Nothing while it is open. */
  connect(room: string, delayMs: number, handler: ChatConnectionHandler): void {
    if (this.running || this.destroyed) {
      return;
    }
    this.room = room;
    this.delayMs = delayMs;
    this.handler = handler;
    this.running = true;
    this.listen(this.api.join(room).subscribe(this.answers()));
  }

  /** Stops the updates: the request in flight is dropped and the next poll is not made. Messages being sent finish. */
  close(): void {
    this.running = false;
    this.timers.clear(this.timer);
    this.timer = null;
    this.request?.unsubscribe();
    this.request = null;
  }

  /** Sends a message to the room joined; several go one at a time, in the order given. */
  send(text: string): void {
    if (this.room === null || this.destroyed) {
      return;
    }
    this.outgoing.push(text);
    this.sendNext();
  }

  private sendNext(): void {
    const text = this.outgoing[0];
    if (this.sending || text === undefined || this.room === null) {
      return;
    }
    this.sending = true;
    const done = () => {
      this.sending = false;
      this.outgoing.shift();
      if (!this.destroyed) {
        this.sendNext();
      }
    };
    this.api.send(this.room, text).subscribe({
      next: () => undefined,
      complete: done,
      error: (error: unknown) => {
        if (!this.destroyed) {
          this.handler?.failed(error, false);
        }
        done();
      },
    });
  }

  private answers() {
    return {
      next: (snapshot: ChatSnapshot) => this.received(snapshot),
      error: (error: unknown) => {
        this.request = null;
        this.close();
        this.handler?.failed(error, true);
      },
    };
  }

  private listen(request: Subscription): void {
    // A synchronous answer (a spec's fake) has already been handled: keep only one still in flight
    this.request = request.closed ? null : request;
  }

  private received(snapshot: ChatSnapshot): void {
    this.request = null;
    try {
      this.handler?.received(snapshot);
    } finally {
      if (this.running) {
        this.timer = this.timers.after(this.delayMs, () => this.poll());
      }
    }
  }

  private poll(): void {
    this.timer = null;
    if (this.running && this.room !== null) {
      this.listen(this.api.poll(this.room).subscribe(this.answers()));
    }
  }
}
