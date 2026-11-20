import {
  afterNextRender,
  afterRenderEffect,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { formatChatTimestamp, isIgnored } from '../../../core/chat/chat-format';
import {
  CHAT_LOGIN_LINK,
  CHAT_PLACEHOLDER,
  CHAT_PLACEHOLDER_LOGGED_OUT,
  INCOGNITO_TITLE,
  ONLINE_TITLE,
  SWITCH_TO_DISCORD,
  SWITCH_TO_LEGACY,
} from '../../../core/chat/chat-messages';
import { ChatLine, ChatRole, ChatUser } from '../../../core/chat/chat-models';
import { LOGIN_PAGE_URL } from '../../../core/platform/app-paths';
import { ServerMarkupPipe } from './chat-markup.pipe';
import { DiscordEmbedComponent } from './discord-embed.component';
import { MarkupComponent } from './markup.component';

/** The list is "at the bottom" (and so stays there as lines come) within this many px of it (chat.js appendMessage). */
const STUCK_WITHIN_PX = 30;

const ROLE_LABELS: Readonly<Record<ChatRole, { text: string; title: string }>> = {
  admin: { text: 'admin', title: 'Administrator' },
  leagueAdmin: { text: 'league admin', title: 'League administrator' },
};

let nextChatId = 0;

/**
 * A chat room (chat.js ChatBoxUI), shared by the hall, the table draft and the game: the messages, the message box,
 * and optionally the user list (with each player's menu and the viewer's own online / incognito chip) and the switch
 * to the Discord chat. It shows what it is given and says what the player did; the page's chat service talks to the
 * server. Messages are drawn from their markup's nodes (`gd-markup`), warnings as plain text. Enter sends,
 * Shift+Enter starts a new line. The message list stays at the bottom as lines come, while it is there (or for a
 * warning).
 *
 * DOM contract: `gd-chat.gd-chat` (`data-mode` = builtin | discord, `data-stopped` once the chat stopped,
 * `data-logged-out` for a visitor, `data-users` when it has a user list) holding `section.gd-chat__room` (the
 * built-in chat, `aria-label`; `hidden` while Discord shows) with `div.gd-chat__messages` (`role=log`) of
 * `div.gd-chat__message` (`data-kind` = chat | system | warning, `data-highlight` = ping | mention) each holding,
 * for messages, `span.gd-chat__sender` (`time.gd-chat__time` "[Nov 9 08:05:03]" when timestamps show, then
 * `b.gd-chat__from` "bob:") and `div.gd-chat__text` (`gd-markup`), and for warnings `div.gd-chat__text` with the
 * text and maybe `a.gd-chat__login`; then `form.gd-chat__form` with `label.gd-chat__label` (to be hidden visually) and
 * `textarea.gd-chat__input`. Then, when Discord was chosen once, `gd-discord-embed` (`hidden` while the built-in chat
 * shows). With a user list, `div.gd-chat__people` holding `ul.gd-chat__users` of `li.gd-chat__user` (`data-self`,
 * `data-role` = admin | leagueAdmin, `data-ignored`) — the own entry: `span.gd-chat__name`, maybe
 * `span.gd-chat__role`, `button.gd-chat__visibility` (`data-incognito`, `aria-pressed`); the others:
 * `button.gd-chat__player` (`aria-haspopup=menu`, `aria-expanded`) holding `span.gd-chat__name`, maybe
 * `span.gd-chat__role` and `span.gd-chat__ignored` (" (ignored)", to be hidden visually), then while open
 * `div.gd-chat__menu` (`role=menu`) with `p.gd-chat__menu-title` and `button.gd-chat__menu-item` (`role=menuitem`,
 * `data-action` = invite | ignore | unignore); then `p.gd-chat__status` (`role=status`, empty when none) and, with
 * Discord, `button.gd-button.gd-chat__switch`.
 */
@Component({
  selector: 'gd-chat',
  styleUrl: './chat.component.css',
  imports: [MarkupComponent, ServerMarkupPipe, DiscordEmbedComponent],
  host: {
    class: 'gd-chat',
    '[attr.data-mode]': "discord() ? 'discord' : 'builtin'",
    '[attr.data-stopped]': "stopped() ? '' : null",
    '[attr.data-logged-out]': "loggedOut() ? '' : null",
    '[attr.data-users]': "users() !== null ? '' : null",
    '(document:pointerdown)': 'closeMenuOutside($event)',
  },
  template: `
    <section class="gd-chat__room" [attr.aria-label]="label()" [hidden]="discord()">
      <div
        #messages
        class="gd-chat__messages"
        role="log"
        aria-live="polite"
        [attr.aria-label]="label() + ' messages'"
        tabindex="0"
        (scroll)="onScroll()"
      >
        @for (line of lines(); track line.id) {
          <div class="gd-chat__message" [attr.data-kind]="line.kind" [attr.data-highlight]="line.highlight">
            @if (line.kind === 'warning') {
              <div class="gd-chat__text">
                {{ line.html }}
                @if (line.loginLink) {
                  <a class="gd-chat__login" [href]="loginUrl()">{{ loginText }}</a>
                }
              </div>
            } @else {
              <span class="gd-chat__sender">
                @if (timestamps() && line.date !== null) {
                  <time class="gd-chat__time" [attr.datetime]="isoDate(line.date)">[{{ timestamp(line.date) }}]</time
                  >&ngsp;
                }
                <b class="gd-chat__from">{{ line.from }}:</b></span
              >
              <div class="gd-chat__text"><gd-markup [nodes]="line.html | serverMarkup" /></div>
            }
          </div>
        }
      </div>
      <form class="gd-chat__form" (submit)="$event.preventDefault()">
        <label class="gd-chat__label" [for]="inputId">{{ inputLabel() }}</label>
        <textarea
          #talk
          class="gd-chat__input"
          rows="1"
          [id]="inputId"
          [placeholder]="loggedOut() ? loggedOutPlaceholder : placeholder()"
          [disabled]="!canSend()"
          (keydown.enter)="onEnter($event)"
        ></textarea>
      </form>
    </section>
    @if (discordLoaded()) {
      <gd-discord-embed [hidden]="!discord()" [username]="userName() ?? ''" />
    }
    @if (users(); as users) {
      <div class="gd-chat__people">
        <ul class="gd-chat__users" [attr.aria-label]="'Players in ' + label()">
          @for (user of users; track user.name) {
            <li
              class="gd-chat__user"
              [attr.data-self]="user.self ? '' : null"
              [attr.data-role]="user.role"
              [attr.data-ignored]="!user.self && ignoredName(user.name) ? '' : null"
            >
              @if (user.self) {
                <span class="gd-chat__name" [title]="user.name">{{ user.name }}</span
                >&ngsp;
                @if (user.role; as role) {
                  <span class="gd-chat__role" [title]="roles[role].title">{{ roles[role].text }}</span
                  >&ngsp;
                }
                <button
                  type="button"
                  class="gd-chat__visibility"
                  [attr.data-incognito]="user.incognito ? '' : null"
                  [attr.aria-pressed]="user.incognito"
                  [title]="user.incognito ? incognitoTitle : onlineTitle"
                  (click)="incognitoChange.emit(!user.incognito)"
                >
                  {{ user.incognito ? 'incognito' : 'online' }}
                </button>
              } @else {
                <button
                  type="button"
                  class="gd-chat__player"
                  aria-haspopup="menu"
                  [attr.aria-expanded]="menuFor() === user.name"
                  [title]="'Actions for ' + user.name"
                  (click)="toggleMenu(user.name)"
                  (keydown)="playerKey($event)"
                >
                  <span class="gd-chat__name">{{ user.name }}</span>
                  @if (user.role; as role) {
                    &ngsp;<span class="gd-chat__role" [title]="roles[role].title">{{ roles[role].text }}</span>
                  }
                  @if (ignoredName(user.name)) {
                    <span class="gd-chat__ignored"> (ignored)</span>
                  }
                </button>
                @if (menuFor() === user.name) {
                  <div
                    class="gd-chat__menu"
                    tabindex="-1"
                    role="menu"
                    [attr.aria-label]="'Actions for ' + user.name"
                    (keydown)="menuKey($event)"
                  >
                    <p class="gd-chat__menu-title" role="presentation">{{ user.name }}</p>
                    @if (invitable()) {
                      <button
                        type="button"
                        role="menuitem"
                        class="gd-chat__menu-item"
                        data-action="invite"
                        (click)="choose(user.name, 'invite')"
                      >
                        Invite to table
                      </button>
                    }
                    <button
                      type="button"
                      role="menuitem"
                      class="gd-chat__menu-item"
                      [attr.data-action]="ignoredName(user.name) ? 'unignore' : 'ignore'"
                      (click)="choose(user.name, 'ignore')"
                    >
                      {{ ignoredName(user.name) ? 'Unignore' : 'Ignore' }}
                    </button>
                  </div>
                }
              }
            </li>
          }
        </ul>
        <p class="gd-chat__status" role="status">{{ status() ?? '' }}</p>
        @if (discord() !== null) {
          <button type="button" class="gd-button gd-chat__switch" (click)="discordChange.emit(!discord())">
            {{ discord() ? switchToLegacy : switchToDiscord }}
          </button>
        }
      </div>
    }
  `,
})
export class ChatComponent {
  /** The room's name for assistive technology ("Game Hall chat"). */
  readonly label = input('Chat');
  readonly lines = input.required<readonly ChatLine[]>();
  /** The user list, the viewer's own entry first; null: no user list (and no Discord switch). */
  readonly users = input<readonly ChatUser[] | null>(null);
  /** The players the viewer ignores (struck through in the list). */
  readonly ignored = input<readonly string[] | null>(null);
  /** The user list's status line. */
  readonly status = input<string | null>(null);
  /** Whether the message box takes messages. */
  readonly canSend = input(false);
  readonly stopped = input(false);
  readonly loggedOut = input(false);
  /** The message box's placeholder; its label is the same without the "...". */
  readonly placeholder = input(CHAT_PLACEHOLDER);
  /** Whether messages show their time before the sender. */
  readonly timestamps = input(true);
  /** The Discord chat shows instead (true) or not (false); null: no Discord switch. */
  readonly discord = input<boolean | null>(null);
  /** The viewer's name (for the Discord chat). */
  readonly userName = input<string | null>(null);
  /** Where the "not logged in" warning's link leads. */
  readonly loginUrl = input(LOGIN_PAGE_URL);
  /** Players' menus offer "Invite to table". */
  readonly invitable = input(false);

  readonly send = output<string>();
  readonly ignoreToggle = output<string>();
  readonly incognitoChange = output<boolean>();
  readonly discordChange = output<boolean>();
  readonly invite = output<string>();

  protected readonly roles = ROLE_LABELS;
  protected readonly loginText = CHAT_LOGIN_LINK;
  protected readonly loggedOutPlaceholder = CHAT_PLACEHOLDER_LOGGED_OUT;
  protected readonly incognitoTitle = INCOGNITO_TITLE;
  protected readonly onlineTitle = ONLINE_TITLE;
  protected readonly switchToDiscord = SWITCH_TO_DISCORD;
  protected readonly switchToLegacy = SWITCH_TO_LEGACY;
  protected readonly inputId = `gd-chat-input-${++nextChatId}`;

  protected readonly inputLabel = computed(() => this.placeholder().replace(/\.+$/, ''));
  /** The player whose menu is open. */
  protected readonly menuFor = signal<string | null>(null);
  /** The Discord chat was shown once (it stays, hidden, after that). */
  protected readonly discordLoaded = signal(false);

  private readonly messages = viewChild.required<ElementRef<HTMLElement>>('messages');
  private readonly talk = viewChild.required<ElementRef<HTMLTextAreaElement>>('talk');
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly injector = inject(Injector);
  private stuck = true;

  constructor() {
    effect(() => {
      if (this.discord()) {
        this.discordLoaded.set(true);
      }
    });
    afterRenderEffect(() => {
      const lines = this.lines();
      const list = this.messages().nativeElement;
      if (this.stuck || lines.at(-1)?.kind === 'warning') {
        list.scrollTop = list.scrollHeight;
        this.stuck = true;
      }
    });
  }

  protected timestamp(date: number): string {
    return formatChatTimestamp(date);
  }

  protected isoDate(date: number): string {
    return new Date(date).toISOString();
  }

  protected ignoredName(name: string): boolean {
    return isIgnored(this.ignored() ?? [], name);
  }

  protected onScroll(): void {
    const list = this.messages().nativeElement;
    this.stuck = list.scrollHeight - list.scrollTop - list.clientHeight <= STUCK_WITHIN_PX;
  }

  /** Enter sends what was typed; Shift+Enter is a new line. */
  protected onEnter(event: Event): void {
    if ((event as KeyboardEvent).shiftKey) {
      return;
    }
    event.preventDefault();
    const box = this.talk().nativeElement;
    const text = box.value;
    box.value = '';
    if (text !== '') {
      this.send.emit(text);
    }
    this.stuck = true;
    const list = this.messages().nativeElement;
    list.scrollTop = list.scrollHeight;
  }

  protected toggleMenu(name: string): void {
    if (this.menuFor() === name) {
      this.menuFor.set(null);
      return;
    }
    this.menuFor.set(name);
    afterNextRender(() => this.host.querySelector<HTMLElement>('.gd-chat__menu-item')?.focus(), {
      injector: this.injector,
    });
  }

  protected choose(name: string, action: 'invite' | 'ignore'): void {
    this.closeMenu(true);
    if (action === 'invite') {
      this.invite.emit(name);
    } else {
      this.ignoreToggle.emit(name);
    }
  }

  /** Up and Down move between the other players' entries (as the legacy list did). */
  protected playerKey(event: KeyboardEvent): void {
    if (event.key !== 'ArrowDown' && event.key !== 'ArrowUp') {
      return;
    }
    event.preventDefault();
    const all = Array.from(this.host.querySelectorAll<HTMLElement>('.gd-chat__player'));
    const index = all.indexOf(event.currentTarget as HTMLElement) + (event.key === 'ArrowDown' ? 1 : -1);
    all[index]?.focus();
  }

  /** In a menu: Up and Down move between its items, Escape and Tab close it. */
  protected menuKey(event: KeyboardEvent): void {
    const items = Array.from(this.host.querySelectorAll<HTMLElement>('.gd-chat__menu-item'));
    const at = items.indexOf(event.target as HTMLElement);
    if (event.key === 'Escape') {
      event.preventDefault();
      this.closeMenu(true);
    } else if (event.key === 'Tab') {
      this.closeMenu(false);
    } else if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      const step = event.key === 'ArrowDown' ? 1 : -1;
      items[(at + step + items.length) % items.length]?.focus();
    }
  }

  protected closeMenuOutside(event: Event): void {
    if (this.menuFor() === null) {
      return;
    }
    const target = event.target;
    if (!(target instanceof Element) || !target.closest('.gd-chat__menu, .gd-chat__player[aria-expanded="true"]')) {
      this.closeMenu(false);
    }
  }

  /** Closes the open menu; `refocus` puts the focus back on its player. */
  private closeMenu(refocus: boolean): void {
    const name = this.menuFor();
    this.menuFor.set(null);
    if (refocus && name !== null) {
      const players = Array.from(this.host.querySelectorAll<HTMLElement>('.gd-chat__player'));
      players.find((player) => player.querySelector('.gd-chat__name')?.textContent === name)?.focus();
    }
  }
}
