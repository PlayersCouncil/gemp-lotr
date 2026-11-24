import { Component, computed, DestroyRef, effect, ElementRef, inject, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { ChatService } from '../../core/chat/chat.service';
import { HallComms } from '../../core/hall/hall-comms.service';
import { hallAddressOfHash, LEGACY_HALL_URL } from '../../core/hall/hall-hash-routes';
import { LOGIN_REQUIRED, LOGIN_REQUIRED_TITLE } from '../../core/hall/hall-messages';
import { HallSession } from '../../core/hall/hall-session.service';
import { HALL_TAB_STORAGE_KEY, HallTab, hallTabAt, visibleHallTabs } from '../../core/hall/hall-tabs';
import { HallChannel } from '../../core/platform/hall-tab.service';
import { LocalSettings } from '../../core/platform/local-settings.service';
import { loginPageAddress } from '../../core/platform/login-page.service';
import { SettingCookies } from '../../core/platform/setting-cookies';
import { LOG_OUT } from '../../core/session/pc-account-messages';
import { PcSessionService } from '../../core/session/pc-session.service';
import { PlayerInfo } from '../../core/session/player-info.service';
import { ChatComponent } from '../shared/chat/chat.component';
import { DialogComponent } from '../shared/dialog/dialog.component';
import { HallConnectionComponent } from './hall-connection.component';
import { DECKBUILDER_HREF, HALL_DISCLAIMER } from './hall-links';
import { injectGameHall } from '../../core/hall/hall-services';
import { PlayOverlay } from '../../core/hall/play/play-overlay.service';
import { GameHallDialogsComponent } from './game-hall/game-hall-dialogs.component';
import { JoinDialogComponent } from './game-hall/play/join-dialog.component';
import { PlayOverlayComponent } from './game-hall/play/play-overlay.component';

/** The chat's height (px) as the legacy hall keeps it (hallUi.js HallChatResizer: the chatResize cookie). */
export const CHAT_HEIGHT_COOKIE = 'chatResize';
export const CHAT_DEFAULT_HEIGHT = 300;
export const CHAT_MIN_HEIGHT = 100;
/** The room the rest of the page keeps when the chat is as tall as it goes (px). */
export const CHAT_ROOM_LEFT = 160;
/** Up and Down on the resize bar change the chat's height by this much (px). */
export const CHAT_RESIZE_STEP = 20;

/** The Admin tab is not ported: it opens the legacy hall's (HALL-PORT.md, "Later"). */
export const ADMIN_TAB_HREF = `${LEGACY_HALL_URL}#admin`;

/**
 * The Game Hall's frame (hall.html #main), around every tab: the tab bar, the tab shown (the routes under the hall,
 * each tab its own: /hall, /help/..., /events/..., /info/..., /account/...), the main bar (the connection readout, the
 * Deck-builder link in a new tab, Play, the server clock), the chat with its user list and resize bar, the disclaimer,
 * and the "Not logged in" and Play dialogs. Owns the hall's services (HALL_SERVICES, its route's providers).
 *
 * Logged in or not (GempHallSession, HallSession): opened on a tab that needs a logged-in player the hall's poll and
 * chat start at once; on Help or Server Info the player is asked for first, and a visitor reads on, not asked to log
 * in until opening a tab that needs it. The tab shown is remembered (localStorage gemp.hall.activeTab, as the legacy
 * hall: its legacy link), and the hall's address with no tab opens it again (hall.routes.ts). Another tab of the site
 * asking whether a hall is open, or asking it to show a legacy hall link, is answered (HallChannel, hallUi.js).
 * The Admin tab (admins and league admins) is a link to the legacy hall's, in a tab of its own: this hall has to stay
 * open and polling, or the server takes the admin out of their tables and queues after 20 s (HallServer). The Users
 * tab is not drawn (hidden in the legacy hall too). Play opens the "Not logged in" dialog for a visitor and the Play overlay for a player
 * (`gd-play-overlay`, part 4.2); the chat's "Invite to table" opens its Casual form for that player. The Join dialog and
 * the Game Hall's dialogs (ready checks, ready prompts, messages, deliveries) are here too, so they show over every tab.
 *
 * Log Out (PC-ACCOUNT.md "Two log-outs"; in every mode, while an alias is played) ends the alias, keeping a PC
 * account signed in, and goes where the server says: the alias list on the login page, or for a password login the
 * login page. Other tabs are told (PcSessionService).
 *
 * The chat's height is set by dragging its resize bar or with Up and Down on it (hallUi.js HallChatResizer): 100 px
 * to the window's height less 160, 300 by default, kept in the legacy chatResize cookie.
 *
 * DOM contract: `gd-hall-shell.gd-hall` (`data-tab` = the tab shown, `data-chat` = builtin | discord,
 * `--gd-chat-height` in px) holding `nav.gd-hall__tabs` (`aria-label`) with an `a.gd-hall__tab` per tab shown
 * (`data-tab` = its legacy slug, for its icon; `aria-current=page` on the tab shown; router links, Admin a plain link
 * to the legacy hall); `main.gd-hall__panel` (`data-tab`) holding the route's component; `div.gd-hall__bar` with
 * `div.gd-hall__presence` (`gd-hall-connection`, then `button.gd-hall__logout[data-action=logout]` while an alias is
 * played), `div.gd-hall__actions` (`a.gd-hall__action[data-action=deckbuilder]` (`target=_blank`),
 * `button.gd-hall__action[data-action=play]`, each with a `span.gd-hall__action-label`) and `p.gd-hall__clock` (the
 * date and time on two lines, empty before the first update); `div.gd-hall__resizer` (`role=separator`,
 * `aria-valuenow` the chat's height, `data-dragging` while dragged); `gd-chat`; `p.gd-hall__disclaimer`;
 * `gd-dialog[data-dialog=login-required]` (`p.gd-hall__login-required`, `a.gd-button[data-action=login]`,
 * `button.gd-button[data-action=close]`); `gd-play-overlay`, `gd-join-dialog` and `gd-game-hall-dialogs`.
 */
@Component({
  selector: 'gd-hall-shell',
  styleUrl: './hall-shell.component.css',
  imports: [
    RouterOutlet,
    RouterLink,
    ChatComponent,
    DialogComponent,
    HallConnectionComponent,
    PlayOverlayComponent,
    JoinDialogComponent,
    GameHallDialogsComponent,
  ],
  host: {
    class: 'gd-hall',
    '[attr.data-tab]': 'active()?.slug ?? null',
    '[attr.data-chat]': "chat.discord() ? 'discord' : 'builtin'",
    '[style.--gd-chat-height]': "chatHeight() + 'px'",
    '(window:resize)': 'fitChat()',
  },
  template: `
    <nav class="gd-hall__tabs" aria-label="Game Hall">
      @for (tab of tabs(); track tab.slug) {
        @if (tab.path; as path) {
          <a
            class="gd-hall__tab"
            [routerLink]="'/' + path"
            [attr.data-tab]="tab.slug"
            [attr.aria-current]="tab === active() ? 'page' : null"
            >{{ tab.label }}</a
          >
        } @else {
          <a class="gd-hall__tab" [href]="adminHref" target="_blank" rel="noopener" [attr.data-tab]="tab.slug">{{
            tab.label
          }}</a>
        }
      }
    </nav>
    <main class="gd-hall__panel" [attr.data-tab]="active()?.slug ?? null">
      <router-outlet />
    </main>
    <div class="gd-hall__bar">
      <div class="gd-hall__presence">
        <gd-hall-connection [loginUrl]="loginUrl()" />
        @if (logoutShown()) {
          <button
            type="button"
            class="gd-hall__logout"
            data-action="logout"
            [disabled]="loggingOut()"
            (click)="logout()"
          >
            {{ logoutLabel }}
          </button>
        }
      </div>
      <div class="gd-hall__actions">
        <a class="gd-hall__action" data-action="deckbuilder" [href]="deckbuilder" target="_blank"
          ><span class="gd-hall__action-label">Deck-builder</span></a
        >
        <button type="button" class="gd-hall__action" data-action="play" (click)="play()">
          <span class="gd-hall__action-label">Play</span>
        </button>
      </div>
      <p class="gd-hall__clock" aria-label="Server time">
        @if (clock(); as clock) {
          {{ clock.date }}<br />{{ clock.time }}
        }
      </p>
    </div>
    <div
      class="gd-hall__resizer"
      role="separator"
      aria-orientation="horizontal"
      aria-label="Resize the chat"
      tabindex="0"
      [attr.aria-valuenow]="chatHeight()"
      [attr.aria-valuemin]="minHeight"
      [attr.data-dragging]="dragging() ? '' : null"
      (pointerdown)="startResize($event)"
      (keydown)="resizeWithKeys($event)"
    ></div>
    <gd-chat
      label="Game Hall chat"
      [lines]="chat.lines()"
      [users]="chat.users()"
      [ignored]="chat.ignored()"
      [status]="chat.userListStatus()"
      [canSend]="chat.canSend()"
      [stopped]="chat.stopped()"
      [loggedOut]="chat.loggedOut()"
      [discord]="chat.discord()"
      [userName]="player.name()"
      [loginUrl]="loginUrl()"
      (send)="chat.send($event)"
      (ignoreToggle)="chat.toggleIgnore($event)"
      (incognitoChange)="chat.setIncognito($event)"
      (discordChange)="chat.setDiscord($event)"
      (invite)="playOverlay.inviteToTable($event)"
    />
    <p class="gd-hall__disclaimer">{{ disclaimer }}</p>
    <gd-dialog
      name="login-required"
      [heading]="loginRequiredTitle"
      [open]="session.loginRequired()"
      (closed)="session.dismissLoginRequired()"
    >
      <p class="gd-hall__login-required">{{ loginRequired }}</p>
      <div class="gd-dialog__actions" gdDialogActions>
        <a class="gd-button" data-action="login" [href]="loginUrl()">Register or log in</a>
        <button type="button" class="gd-button" data-action="close" (click)="session.dismissLoginRequired()">
          Close
        </button>
      </div>
    </gd-dialog>
    <gd-play-overlay />
    <gd-join-dialog />
    <gd-game-hall-dialogs />
  `,
})
export class HallShellComponent {
  protected readonly chat = inject(ChatService);
  protected readonly comms = inject(HallComms);
  protected readonly session = inject(HallSession);
  protected readonly player = inject(PlayerInfo);
  private readonly router = inject(Router);
  private readonly settings = inject(LocalSettings);
  private readonly cookies = inject(SettingCookies);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly deckbuilder = DECKBUILDER_HREF;
  protected readonly adminHref = ADMIN_TAB_HREF;
  protected readonly disclaimer = HALL_DISCLAIMER;
  protected readonly loginRequiredTitle = LOGIN_REQUIRED_TITLE;
  protected readonly loginRequired = LOGIN_REQUIRED;
  protected readonly minHeight = CHAT_MIN_HEIGHT;
  protected readonly logoutLabel = LOG_OUT;

  private readonly pc = inject(PcSessionService);
  /** Log Out: while an alias is played (POST /argonauth/logout answers in every mode). */
  protected readonly logoutShown = computed(() => (this.pc.session()?.player ?? null) !== null);
  protected readonly loggingOut = signal(false);

  protected readonly tabs = computed(() => visibleHallTabs(this.session.adminTabShown()));
  /** The address shown (the router's), for the login page to come back to. */
  private readonly url = signal('/');
  /** The tab shown: the one whose route the address is in. */
  protected readonly active = signal<HallTab | null>(null);
  protected readonly loginUrl = computed(() => loginPageAddress(this.url()));

  /** The server clock, its date over its time. */
  protected readonly clock = computed(() => {
    const time = this.comms.serverTime();
    if (!time) {
      return null;
    }
    const [date, clock] = time.split(' ');
    return { date: date ?? '', time: clock ?? '' };
  });

  /** The Play overlay and its flows (part 4.2). */
  protected readonly playOverlay = inject(PlayOverlay);

  /** The chat's height (px) as resized, within its limits. */
  protected readonly chatHeight = signal(CHAT_DEFAULT_HEIGHT);
  protected readonly dragging = signal(false);

  constructor() {
    // the Game Hall's services that follow the poll, before its first answer (whatever tab the hall opens on)
    injectGameHall();
    const route = inject(ActivatedRoute);
    const first = this.tabOfRoute(route.snapshot.firstChild?.routeConfig?.path ?? null);
    this.show(first, this.addressOf(route), false);
    void this.session.begin(first?.login ?? true);
    void this.pc.load();

    this.router.events.pipe(takeUntilDestroyed()).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const tree = this.router.parseUrl(event.urlAfterRedirects);
        const segment = tree.root.children['primary']?.segments[0]?.path ?? null;
        this.show(this.tabOfRoute(segment), event.urlAfterRedirects, true);
      }
    });

    // Another tab of the site asks the hall to show a legacy hall link (hallUi.js: only hall links)
    const channel = inject(HallChannel);
    channel.listen();
    channel.goes$.pipe(takeUntilDestroyed()).subscribe((hash) => {
      const address = hallAddressOfHash(hash);
      if (address !== null && 'route' in address) {
        void this.router.navigateByUrl(address.route);
      }
    });

    // The hall's services belong to the route, whose injector outlives the page: leaving it stops the poll and chat
    inject(DestroyRef).onDestroy(() => this.session.stop());
    // The poll found the viewer not logged in: on a tab that needs logging in, the "Not logged in" dialog says so
    effect(() => {
      if (this.session.started() && this.comms.connection().state === 'loggedout') {
        untracked(() => this.session.requireLogin(this.active()?.login ?? true));
      }
    });

    this.chatHeight.set(this.clampChat(Number(this.cookies.read(CHAT_HEIGHT_COOKIE)) || CHAT_DEFAULT_HEIGHT));
  }

  /** Log Out: the alias only. */
  protected async logout(): Promise<void> {
    this.loggingOut.set(true);
    await this.pc.logout('alias');
  }

  /** Play: the Play dialog for a player; a visitor is asked to log in instead (hallUi.js #open-table-button). */
  protected play(): void {
    this.playOverlay.open();
  }

  /** Dragging the resize bar: the chat's top edge follows the pointer. */
  protected startResize(event: PointerEvent): void {
    if (event.button !== 0) {
      return;
    }
    event.preventDefault();
    const bar = event.currentTarget as HTMLElement;
    const chat = this.host.querySelector<HTMLElement>('gd-chat');
    if (!chat) {
      return;
    }
    // (a DOM without pointer capture, as the unit tests', just follows the pointer)
    if ('setPointerCapture' in bar) bar.setPointerCapture(event.pointerId);
    this.dragging.set(true);
    const bottom = chat.getBoundingClientRect().bottom;
    const move = (e: PointerEvent) => this.chatHeight.set(this.clampChat(Math.round(bottom - e.clientY)));
    const stop = () => {
      bar.removeEventListener('pointermove', move);
      bar.removeEventListener('pointerup', stop);
      bar.removeEventListener('pointercancel', stop);
      this.dragging.set(false);
      this.saveChatHeight();
    };
    bar.addEventListener('pointermove', move);
    bar.addEventListener('pointerup', stop);
    bar.addEventListener('pointercancel', stop);
  }

  /** Up makes the chat taller, Down shorter. */
  protected resizeWithKeys(event: KeyboardEvent): void {
    const step = event.key === 'ArrowUp' ? CHAT_RESIZE_STEP : event.key === 'ArrowDown' ? -CHAT_RESIZE_STEP : 0;
    if (!step) {
      return;
    }
    event.preventDefault();
    this.chatHeight.set(this.clampChat(this.chatHeight() + step));
    this.saveChatHeight();
  }

  /** The window changed size: the chat keeps within its limits. */
  protected fitChat(): void {
    this.chatHeight.set(this.clampChat(this.chatHeight()));
  }

  private clampChat(height: number): number {
    const window = this.host.ownerDocument.defaultView;
    const max = Math.max(
      CHAT_MIN_HEIGHT,
      (window?.innerHeight ?? CHAT_DEFAULT_HEIGHT + CHAT_ROOM_LEFT) - CHAT_ROOM_LEFT,
    );
    return Math.min(max, Math.max(CHAT_MIN_HEIGHT, height));
  }

  private saveChatHeight(): void {
    this.cookies.write(CHAT_HEIGHT_COOKIE, String(this.chatHeight()));
  }

  /** The tab at a route's first segment; null for none (the hall's own address, before it opens a tab). */
  private tabOfRoute(path: string | null): HallTab | null {
    return path === null ? null : hallTabAt(path);
  }

  /** The address of the route being opened (the router's url is the previous one until the navigation ends). */
  private addressOf(route: ActivatedRoute): string {
    const segments: string[] = [];
    for (let r = route.snapshot.firstChild; r; r = r.firstChild) {
      segments.push(...r.url.map((segment) => segment.path));
    }
    return '/' + segments.join('/');
  }

  /** A tab is shown: it is remembered, and a tab that needs a logged-in player starts the hall or asks to log in. */
  private show(tab: HallTab | null, url: string, opened: boolean): void {
    this.url.set(url);
    if (tab === null) {
      return;
    }
    const before = this.active();
    this.active.set(tab);
    this.settings.write(HALL_TAB_STORAGE_KEY, tab.href);
    if (opened && tab !== before && tab.login) {
      this.session.loginTabOpened();
    }
  }
}
