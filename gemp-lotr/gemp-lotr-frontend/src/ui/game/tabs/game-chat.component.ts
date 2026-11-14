import {
  afterRenderEffect,
  Component,
  computed,
  ElementRef,
  inject,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ChatRoom } from '../../../core/chat/chat-room.service';
import { GameLog, GameLogEntry } from '../../../core/game/session/game-log';
import { PlayerInfo } from '../../../core/session/player-info.service';

/** A line that names the player is marked: "@name" calls them (ping), their name or "@everyone" mentions them. */
export function mentionOf(html: string, name: string | null): 'ping' | 'mention' | null {
  if (name !== null) {
    const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    if (new RegExp(`@${escaped}\\b`).test(html)) return 'ping';
    if (new RegExp(`(?<!<b>)\\b${escaped}\\b`).test(html)) return 'mention';
  }
  return /(@everyone|@anyone)/.test(html) ? 'mention' : null;
}

interface LineView {
  kind: 'chat' | 'system' | 'message' | 'warning';
  from: string | null;
  html: string;
  mention: 'ping' | 'mention' | null;
  /**
   * The text is in a box of its own: a chat line's, and a game message's unless it names the player (the legacy page
   * left those bare, so their parts, e.g. card names, are laid out one by one and wrap as wholes).
   */
  boxed: boolean;
}

function lineOf(entry: GameLogEntry, name: string | null): LineView {
  if (entry.kind === 'chat') {
    const { from, html, system } = entry.message;
    return { kind: system ? 'system' : 'chat', from, html, mention: mentionOf(html, name), boxed: true };
  }
  const mention = mentionOf(entry.html, name);
  return { kind: entry.kind, from: null, html: entry.html, mention, boxed: entry.kind === 'message' && !mention };
}

/**
 * The game's chat box: the room's chat and the game's messages in one list, which stays at its end as lines come
 * unless the player scrolled up, and the field to say something (Enter sends it).
 *
 * DOM contract: `gd-game-chat` holding `.gd-game-chat__lines` with one `.gd-game-chat__line` per line (`data-kind`
 * chat, system, message, warning; `data-mention` ping or mention when it names the player; the game page shows no
 * times), and `input[data-chat-input]`.
 */
@Component({
  selector: 'gd-game-chat',
  styleUrl: './game-chat.component.css',
  // Not scoped: it also styles what the server's messages hold (card names, links); its rules all start at gd-game-chat
  encapsulation: ViewEncapsulation.None,
  template: `
    <div #lines class="gd-game-chat__lines" (scroll)="onScroll()">
      @for (line of views(); track $index) {
        @if (line.from !== null || line.boxed) {
          <div class="gd-game-chat__line" [attr.data-kind]="line.kind" [attr.data-mention]="line.mention">
            @if (line.from !== null) {
              <div class="gd-game-chat__who">
                <span>
                  <b>{{ line.from }}: </b></span
                >
              </div>
            }
            <div class="gd-game-chat__text" [innerHTML]="line.html"></div>
          </div>
        } @else {
          <div
            class="gd-game-chat__line"
            [attr.data-kind]="line.kind"
            [attr.data-mention]="line.mention"
            [innerHTML]="line.html"
          ></div>
        }
      }
    </div>
    <input
      type="text"
      class="gd-game-chat__talk"
      data-chat-input
      aria-label="Say something"
      (keydown.enter)="send($event)"
    />
  `,
})
export class GameChatComponent {
  private readonly log = inject(GameLog);
  private readonly chat = inject(ChatRoom);
  private readonly player = inject(PlayerInfo);
  private readonly lines = viewChild.required<ElementRef<HTMLElement>>('lines');

  protected readonly views = computed<LineView[]>(() => {
    const name = this.player.name();
    return this.log.entries().map((entry) => lineOf(entry, name));
  });

  /** The list follows its end until the player scrolls up from it. */
  private atEnd = true;

  constructor() {
    afterRenderEffect(() => {
      this.views();
      const box = this.lines().nativeElement;
      if (this.atEnd) box.scrollTop = box.scrollHeight;
    });
  }

  protected onScroll(): void {
    const box = this.lines().nativeElement;
    const most = box.scrollHeight - box.clientHeight;
    this.atEnd = most <= 30 || box.scrollTop / most >= 0.999;
  }

  protected send(event: Event): void {
    const field = event.target as HTMLInputElement;
    if (field.value !== '') this.chat.send(field.value);
    field.value = '';
  }
}
