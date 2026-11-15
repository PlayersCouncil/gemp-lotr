import {
  afterNextRender,
  afterRenderEffect,
  DestroyRef,
  Component,
  effect,
  computed,
  ElementRef,
  inject,
  untracked,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ChatRoom } from '../../../core/chat/chat-room.service';
import { GameLog, GameLogEntry } from '../../../core/game/session/game-log';
import { GameViewState } from '../../../core/game/session/game-view-state';
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
    <div #lines class="gd-game-chat__lines">
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
  private readonly viewState = inject(GameViewState);
  private readonly lines = viewChild<ElementRef<HTMLElement>>('lines');

  protected readonly views = computed<LineView[]>(() => {
    const name = this.player.name();
    return this.log.entries().map((entry) => lineOf(entry, name));
  });

  /**
   * Whether the list goes to its end when lines come: decided before they are drawn, from where the list stands (legacy
   * appendMessage): at its end or within 30px of scrolling at all, or not scrollable (also while its tab is closed),
   * and always for a warning.
   */
  private follow = true;
  private shown = 0;

  constructor() {
    // Before the new lines are drawn
    effect(() => {
      const views = this.views();
      const box = this.lines()?.nativeElement;
      const most = box ? box.scrollHeight - box.offsetHeight : 0;
      const warned = views.slice(this.shown).some((line) => line.kind === 'warning');
      this.shown = views.length;
      this.follow = warned || most <= 30 || (box?.scrollTop ?? 0) / most >= 0.999;
    });
    // Only when lines were added, as the legacy page did. While its tab is closed nothing moves it: it opens again where
    // it was (the browser kept an element's place over a closed tab; here it is kept explicitly)
    let drawn = 0;
    let kept: number | null = null;
    // After a reload: where the list was (null: at its end)
    let restoring = true;
    afterRenderEffect(() => {
      const count = this.views().length;
      const box = this.lines()?.nativeElement;
      if (box && count > drawn && this.follow) {
        // Not while its tab is closed: it opens again where it was
        if (box.clientHeight > 0) box.scrollTop = box.scrollHeight;
      }
      if (box && restoring && count > 0) {
        restoring = false;
        const saved = untracked(this.viewState.chatScroll);
        if (saved !== null) {
          if (box.clientHeight > 0) box.scrollTop = saved;
          else kept = saved;
        }
      }
      drawn = count;
    });
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const box = this.lines()?.nativeElement;
      const Observer = box?.ownerDocument.defaultView?.ResizeObserver;
      if (!box || !Observer) return;
      let last = box.scrollTop;
      box.addEventListener('scroll', () => {
        if (restoring) return;
        last = box.scrollTop;
        const atEnd = box.scrollHeight - box.clientHeight - box.scrollTop < 1;
        this.viewState.chatScroll.set(atEnd ? null : box.scrollTop);
      });
      const observer = new Observer(() => {
        if (box.clientHeight === 0) {
          // (not before the place kept over a reload is taken up)
          if (restoring) return;
          kept ??= last;
          this.viewState.chatScroll.set(kept);
        } else if (kept !== null) {
          box.scrollTop = kept;
          kept = null;
          last = box.scrollTop;
        }
      });
      observer.observe(box);
      destroyRef.onDestroy(() => observer.disconnect());
    });
  }

  protected send(event: Event): void {
    const field = event.target as HTMLInputElement;
    if (field.value !== '') this.chat.send(field.value);
    field.value = '';
  }
}
