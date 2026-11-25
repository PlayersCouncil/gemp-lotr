import {
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
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameSettings } from '../../../core/game/settings/game-settings';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { ThemeService } from '../../../core/platform/theme.service';
import { CardHintsDirective } from '../table/card-hints.directive';
import { LogPin } from './log-pin';

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
  /** A line the page put in a player's mouth (a scripted game's message). */
  said: boolean;
  /**
   * The text is in a box of its own: a chat line's, and a game message's unless it names the player (the legacy page
   * left those bare, so their parts, e.g. card names, are laid out one by one and wrap as wholes).
   */
  boxed: boolean;
}

function lineOf(entry: GameLogEntry, name: string | null): LineView {
  if (entry.kind === 'chat') {
    const { from, html, system } = entry.message;
    return {
      kind: system ? 'system' : 'chat',
      from,
      html,
      mention: mentionOf(html, name),
      said: entry.said !== undefined,
      boxed: true,
    };
  }
  const mention = mentionOf(entry.html, name);
  return {
    kind: entry.kind,
    from: null,
    html: entry.html,
    mention,
    said: false,
    boxed: entry.kind === 'message' && !mention,
  };
}

/**
 * The game's chat box: the room's chat and the game's messages in one list, and the field to say something (Enter
 * sends it). Oldest first (classic, and modern by its setting) it stays at its newest lines as they come unless the
 * player scrolled away from them (LogPin); newest first (modern's default) it jumps to its top with every new line
 * (the owner, 2026-11-20).
 *
 * DOM contract: `gd-game-chat` holding `.gd-game-chat__lines` with one `.gd-game-chat__line` per line (`data-kind`
 * chat, system, message, warning; `data-mention` ping or mention when it names the player; `data-said` on a line the
 * page put in a player's mouth, a scripted game's message; the game page shows no times), and
 * `input[data-chat-input]`.
 */
@Component({
  selector: 'gd-game-chat',
  styleUrl: './game-chat.component.css',
  // Not scoped: it also styles what the server's messages hold (card names, links); its rules all start at gd-game-chat
  encapsulation: ViewEncapsulation.None,
  imports: [CardHintsDirective],
  host: { '[attr.data-newest-first]': "newestFirst() ? '' : null" },
  template: `
    <div #lines class="gd-game-chat__lines" gdCardHints [attr.data-replay]="replaying() ? '' : null">
      @for (line of shownLines(); track $index) {
        @if (line.from !== null || line.boxed) {
          <div
            class="gd-game-chat__line"
            [attr.data-kind]="line.kind"
            [attr.data-mention]="line.mention"
            [attr.data-said]="line.said ? '' : null"
          >
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
    @if (!replaying()) {
      <input
        type="text"
        class="gd-game-chat__talk"
        data-chat-input
        aria-label="Say something"
        (keydown.enter)="send($event)"
      />
    }
  `,
})
export class GameChatComponent {
  private readonly log = inject(GameLog);
  private readonly chat = inject(ChatRoom);
  private readonly player = inject(PlayerInfo);
  private readonly viewState = inject(GameViewState);
  /** A replay has no room to say anything in. */
  protected readonly replaying = inject(GameSession).replaying;
  private readonly settings = inject(GameSettings);
  /**
   * The modern theme lists the newest lines first, the field to say something above them (the owner, 2026-11-16), unless
   * its setting says otherwise (Settings › Display, 2026-11-20); classic keeps the legacy page's order, the newest last.
   */
  protected readonly newestFirst = computed(() => this.theme.theme() === 'modern' && this.settings.chatNewestFirst());
  private readonly theme = inject(ThemeService);
  private readonly lines = viewChild<ElementRef<HTMLElement>>('lines');

  protected readonly views = computed<LineView[]>(() => {
    const name = this.player.name();
    return this.log.entries().map((entry) => lineOf(entry, name));
  });

  /** The lines in the order shown. */
  protected readonly shownLines = computed(() => (this.newestFirst() ? [...this.views()].reverse() : this.views()));

  private pin: LogPin | null = null;

  constructor() {
    const destroyRef = inject(DestroyRef);
    // Before the new lines are drawn: whether they are followed was decided by where the player left the list (LogPin)
    let shown = 0;
    let warned = false;
    effect(() => {
      const views = this.views();
      warned ||= views.slice(shown).some((line) => line.kind === 'warning');
      shown = views.length;
      untracked(() => this.pin?.drawing());
    });
    // After a reload: where the list was (null: at its newest lines), taken up once its first lines are drawn
    let restoring = true;
    let order: boolean | null = null;
    afterRenderEffect(() => {
      const count = this.views().length;
      // (the order switched, by the theme or the setting: the list goes to its newest lines, at their other end)
      const newestFirst = this.newestFirst();
      const switched = order !== null && order !== newestFirst;
      order = newestFirst;
      const box = this.lines()?.nativeElement;
      if (!box) return;
      const pin = (this.pin ??= this.pinFor(box, destroyRef));
      if (restoring) {
        if (count === 0) return;
        restoring = false;
        warned = false;
        pin.restore(untracked(this.viewState.chatScroll));
        return;
      }
      // A warning always shows, the list then follows its newest lines again; and newest first, every new line is
      // shown, wherever the player was (the owner, 2026-11-20)
      pin.drawn(warned || newestFirst || switched);
      warned = false;
    });
  }

  /** The list's pin: told of the player's scrolling, of images loading in it, and of its size changing. */
  private pinFor(box: HTMLElement, destroyRef: DestroyRef): LogPin {
    const pin = new LogPin(
      box,
      () => (untracked(this.newestFirst) ? 'start' : 'end'),
      (scroll) => this.viewState.chatScroll.set(scroll),
    );
    box.addEventListener('scroll', () => pin.scrolled());
    // (load doesn't bubble: caught on its way down)
    box.addEventListener('load', () => pin.repin(), true);
    const Observer = box.ownerDocument.defaultView?.ResizeObserver;
    const observer = Observer ? new Observer(() => pin.resized()) : null;
    observer?.observe(box);
    destroyRef.onDestroy(() => {
      observer?.disconnect();
      pin.dispose();
    });
    return pin;
  }

  protected send(event: Event): void {
    const field = event.target as HTMLInputElement;
    if (field.value !== '') this.chat.send(field.value);
    field.value = '';
  }
}
