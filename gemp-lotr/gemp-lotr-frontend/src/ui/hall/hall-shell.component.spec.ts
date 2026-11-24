import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { HallTablesService } from '../../core/hall/tables/hall-tables.service';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Subject } from 'rxjs';
import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { CHAT_CHOICE_KEY } from '../../core/chat/chat-messages';
import { HALL_TAB_STORAGE_KEY } from '../../core/hall/hall-tabs';
import { HallChannel, SessionChannel } from '../../core/platform/hall-tab.service';
import { LocalSettings } from '../../core/platform/local-settings.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { PageNavigation } from '../../core/platform/page-navigation';
import { SettingCookies } from '../../core/platform/setting-cookies';
import { settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { APP_ROUTES } from '../app/app.routes';
import { CHAT_HEIGHT_COOKIE, CHAT_RESIZE_STEP, HallShellComponent } from './hall-shell.component';
import { provideFakeNotifier } from '../../testing/fake-notifier';

const SERVER = '/gemp-lotr-server';
/** A request's path on the server, without the cache-buster the interceptors add. */
const at = (path: string) => (r: { url: string }) => r.url.split('?')[0] === SERVER + path;
const HALL = at('/hall');
const CHAT = at('/chat/Game%20Hall');
const PLAYER = at('/player');

class MemoryStore {
  readonly values = new Map<string, string>();
  read(key: string): string | null {
    return this.values.get(key) ?? null;
  }
  write(key: string, value: string): void {
    this.values.set(key, value);
  }
}

describe('HallShellComponent', () => {
  let backend: HttpTestingController;
  let settings: MemoryStore;
  let cookies: MemoryStore;
  let harness: RouterTestingHarness;

  beforeEach(async () => {
    settings = new MemoryStore();
    settings.write(CHAT_CHOICE_KEY, 'builtin');
    cookies = new MemoryStore();
    TestBed.configureTestingModule({
      providers: [
        provideRouter(APP_ROUTES),
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: LocalSettings, useValue: settings },
        { provide: SettingCookies, useValue: cookies },
        // the app's (app.config.ts): the Game Hall tab's queues ask their questions through it
        ...provideFakeNotifier(),
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.match(() => true));

  async function open(url: string) {
    await harness.navigateByUrl(url);
    await update();
    return harness.routeNativeElement?.closest('.gd-hall') ?? document.querySelector('.gd-hall');
  }

  function el(): HTMLElement {
    return harness.fixture.nativeElement.querySelector('.gd-hall') as HTMLElement;
  }

  async function update() {
    await settle();
    await harness.fixture.whenStable();
  }

  const all = (selector: string) => Array.from(el().querySelectorAll<HTMLElement>(selector));
  const text = (selector: string) => el().querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;
  const url = () => TestBed.inject(Router).url;

  /** The hall's first calls for a player: who they are, the hall's snapshot, the chat. */
  async function answerPlayer(player: object = { name: 'alice', type: 'u' }) {
    backend.match(PLAYER).forEach((r) => r.flush(player));
    await update();
    backend.match((r) => HALL(r) && r.method === 'GET').forEach((r) => r.flush(readServerResponse('hall.xml')));
    backend.match((r) => CHAT(r) && r.method === 'GET').forEach((r) => r.flush(readServerResponse('chat.xml')));
    await update();
    backend.match(at('/player/ignores')).forEach((r) => r.flush({ ignored: [] }));
    await update();
  }

  it('draws the frame: the tabs, the tab shown, the main bar, the chat and the disclaimer', async () => {
    await open('/hall');
    await answerPlayer();
    expect(el().dataset['tab']).toBe('game-hall');
    // Users isn't drawn, and Admin is for admins
    expect(
      all('.gd-hall__tab').map((tab) => [tab.dataset['tab'], tab.textContent?.trim(), tab.getAttribute('href')]),
    ).toEqual([
      ['game-hall', 'Game Hall', '/hall'],
      ['help', 'Help', '/help'],
      ['events', 'Events', '/events'],
      ['info', 'Server Info', '/info'],
      ['account', 'My Account', '/account'],
    ]);
    expect(all('.gd-hall__tab[aria-current="page"]').map((tab) => tab.dataset['tab'])).toEqual(['game-hall']);
    expect(el().querySelector('main.gd-hall__panel gd-game-hall-tab')).not.toBeNull();
    expect(el().querySelector('gd-game-hall-tab gd-game-hall-sections')).not.toBeNull();
    expect(text('.gd-game-hall__motd')).toBe('MOTD: Welcome back!');
    expect(el().querySelector('.gd-game-hall__motd b:not(.gd-game-hall__motd-label)')?.textContent).toBe('back');

    const deckbuilder = el().querySelector<HTMLAnchorElement>('.gd-hall__action[data-action="deckbuilder"]');
    expect([deckbuilder?.getAttribute('href'), deckbuilder?.target, deckbuilder?.textContent?.trim()]).toEqual([
      '/gemp-lotr/ng/deckbuilder/',
      '_blank',
      'Deck-builder',
    ]);
    expect(text('.gd-hall__action[data-action="play"]')).toBe('Play');
    expect(
      el()
        .querySelector('.gd-hall__clock')
        ?.innerHTML.replace(/<!--.*?-->| _ngcontent-[\w-]+=""/g, '')
        .trim(),
    ).toBe('2026-11-09<br>14:03:05');
    expect(el().querySelector('gd-hall-connection')?.getAttribute('data-state')).toBe('connected');
    expect(all('.gd-chat__message').length).toBeGreaterThan(0);
    expect(all('.gd-chat__user').map((user) => user.querySelector('.gd-chat__name')?.textContent)).toContain('alice');
    expect(text('.gd-hall__disclaimer')).toContain('copyrighted by Decipher Inc.');
    expect(el().style.getPropertyValue('--gd-chat-height')).toBe('300px');
  });

  it('opens the tab last opened (as either hall remembers it) at its address alone, else the Game Hall', async () => {
    settings.write(HALL_TAB_STORAGE_KEY, 'includes/info.html');
    await open('/');
    expect(url()).toBe('/info/stats');
    expect(el().dataset['tab']).toBe('info');

    settings.write(HALL_TAB_STORAGE_KEY, 'includes/admin.html');
    await open('/');
    expect(url()).toBe('/hall');
  });

  it('remembers the tab shown as the legacy hall does (its legacy link); unknown addresses go to the Game Hall', async () => {
    await open('/events/leagues/42');
    expect(el().dataset['tab']).toBe('events');
    expect(settings.read(HALL_TAB_STORAGE_KEY)).toBe('includes/events.html');
    await open('/no/such/page?theme=classic');
    expect(url()).toBe('/hall?theme=classic');
    expect(settings.read(HALL_TAB_STORAGE_KEY)).toBe('#gameHall');
  });

  it('shows the sub-tabs of a tab that has them, each an address of its own', async () => {
    await open('/help');
    expect(url()).toBe('/help/how-to-play');
    const subTabs = all('.gd-hall-sub-tabs__tab');
    expect(
      subTabs.map((tab) => [tab.textContent?.trim(), tab.getAttribute('href'), tab.getAttribute('aria-selected')]),
    ).toEqual([
      ['How Do I Play?', '/help/how-to-play', 'true'],
      ['Code of Conduct', '/help/conduct', 'false'],
      ['Format Definitions', '/help/formats', 'false'],
      ['What Are Leagues?', '/help/leagues', 'false'],
      ['PC Errata', '/help/errata', 'false'],
    ]);
    await open('/help/formats/pc_movie');
    expect(all('.gd-hall-sub-tabs__tab[aria-selected="true"]').map((tab) => tab.dataset['subTab'])).toEqual([
      'formats',
    ]);
  });

  it('lets a visitor read Help without asking them to log in; a tab that needs logging in starts the hall', async () => {
    await open('/help');
    backend.expectOne(PLAYER).flush('', { status: 401, statusText: 'Unauthorized' });
    await update();
    backend.expectNone(HALL);
    backend.expectNone(CHAT);
    expect(el().querySelector('gd-hall-connection')?.getAttribute('data-state')).toBe('loggedout');
    expect(el().querySelector('.gd-hall-connection__login')?.getAttribute('href')).toBe(
      '/gemp-lotr/ng/login?return=%2Fhelp%2Fhow-to-play',
    );
    expect(el().querySelector('gd-chat')?.hasAttribute('data-logged-out')).toBe(true);
    expect(el().querySelector('dialog[data-dialog="login-required"]')?.hasAttribute('open')).toBe(false);

    // Play asks the visitor to log in
    el().querySelector<HTMLButtonElement>('.gd-hall__action[data-action="play"]')?.click();
    await update();
    expect(el().querySelector('dialog[data-dialog="login-required"]')?.hasAttribute('open')).toBe(true);
    expect(el().querySelector('[data-action="login"]')?.getAttribute('href')).toBe(
      '/gemp-lotr/ng/login?return=%2Fhelp%2Fhow-to-play',
    );
    el().querySelector<HTMLButtonElement>('[data-dialog="login-required"] [data-action="close"]')?.click();
    await update();
    expect(el().querySelector('dialog[data-dialog="login-required"]')?.hasAttribute('open')).toBe(false);

    // Opening the Game Hall starts the hall: its poll and chat
    el().querySelector<HTMLAnchorElement>('.gd-hall__tab[data-tab="game-hall"]')?.click();
    await update();
    expect(url()).toBe('/hall');
    expect(backend.match((r) => HALL(r) && r.method === 'GET')).toHaveLength(1);
    expect(backend.match((r) => CHAT(r) && r.method === 'GET')).toHaveLength(1);
  });

  it('starts at once on a tab that needs logging in, and the Play dialog opens for a player', async () => {
    await open('/account/history');
    expect(backend.match((r) => HALL(r) && r.method === 'GET')).toHaveLength(1);
    await answerPlayer();
    el().querySelector<HTMLButtonElement>('.gd-hall__action[data-action="play"]')?.click();
    await update();
    expect(el().querySelector('dialog[data-dialog="play"]')?.hasAttribute('open')).toBe(true);
    expect(el().querySelector('dialog[data-dialog="login-required"]')?.hasAttribute('open')).toBe(false);
  });

  it('opens the Play overlay\'s Casual form from the chat\'s "Invite to table"', async () => {
    await open('/hall');
    await answerPlayer();
    harness.fixture.debugElement.query(By.css('gd-chat')).triggerEventHandler('invite', '+ bob');
    await update();
    expect(el().querySelector('dialog[data-dialog="play"] [data-dialog-title]')?.textContent).toBe(
      'Create Table ▸ Casual',
    );
    const shell = harness.fixture.debugElement.query(By.directive(HallShellComponent));
    expect(shell.injector.get(HallTablesService).invitee()).toBe('bob');
  });

  it("shows admins the Admin tab, the legacy hall's in a tab of its own (this hall goes on polling)", async () => {
    await open('/hall');
    await answerPlayer({ name: 'boss', type: 'ua' });
    const admin = el().querySelector<HTMLAnchorElement>('.gd-hall__tab[data-tab="admin"]');
    expect(admin?.getAttribute('href')).toBe('/gemp-lotr/hall-legacy.html?admin=1#admin');
    expect(admin?.target).toBe('_blank');
  });

  it('goes where another tab of the site asks it to (a legacy hall link)', async () => {
    await open('/hall');
    const channel = harness.fixture.debugElement
      .query((node) => node.componentInstance instanceof HallShellComponent)
      .injector.get(HallChannel) as unknown as { goes: Subject<string> };
    channel.goes.next('#account/scenarios');
    await update();
    expect(url()).toBe('/account/scenarios');
    channel.goes.next('#admin');
    await update();
    expect(url()).toBe('/account/scenarios');
  });

  it('resizes the chat with Up and Down, within its limits, kept in the legacy chatResize cookie', async () => {
    cookies.write(CHAT_HEIGHT_COOKIE, '250');
    await open('/help');
    expect(el().style.getPropertyValue('--gd-chat-height')).toBe('250px');
    const bar = el().querySelector<HTMLElement>('.gd-hall__resizer');
    bar?.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowUp' }));
    await update();
    expect(el().style.getPropertyValue('--gd-chat-height')).toBe(`${250 + CHAT_RESIZE_STEP}px`);
    expect(cookies.read(CHAT_HEIGHT_COOKIE)).toBe(String(250 + CHAT_RESIZE_STEP));
    for (let i = 0; i < 20; i++) bar?.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    await update();
    expect(el().style.getPropertyValue('--gd-chat-height')).toBe('100px');
    expect(bar?.getAttribute('aria-valuenow')).toBe('100');
  });

  it("passes the chat's messages, ignores, incognito and Discord choice on to the hall's chat", async () => {
    await open('/hall');
    await answerPlayer();
    const chat = harness.fixture.debugElement.query(By.css('gd-chat'));
    chat.triggerEventHandler('send', 'hello all');
    await update();
    const sent = backend.match((r) => CHAT(r) && r.method === 'POST' && r.body?.toString().includes('message='));
    expect(sent.map((r) => r.request.body?.toString())).toEqual([expect.stringContaining('message=hello%20all')]);
    chat.triggerEventHandler('ignoreToggle', 'bob');
    await update();
    expect(backend.match(at('/player/ignores/add')).map((r) => r.request.body?.toString())).toEqual(['name=bob']);
    chat.triggerEventHandler('incognitoChange', true);
    await update();
    expect(backend.match(at('/player/incognito')).map((r) => r.request.body?.toString())).toEqual(['incognito=true']);
    chat.triggerEventHandler('discordChange', true);
    await update();
    expect(settings.read(CHAT_CHOICE_KEY)).toBe('discord');
    expect(el().dataset['chat']).toBe('discord');
  });

  it('asks to log in when the poll finds the session ended, on a tab that needs it; Esc closes the dialog', async () => {
    await open('/hall');
    await answerPlayer();
    backend.match(at('/hall/update')).forEach((r) => r.flush('', { status: 401, statusText: 'Unauthorized' }));
    await new Promise((resolve) => setTimeout(resolve, 150));
    await update();
    backend.match(at('/hall/update')).forEach((r) => r.flush('', { status: 401, statusText: 'Unauthorized' }));
    await update();
    const dialog = el().querySelector<HTMLDialogElement>('dialog[data-dialog="login-required"]')!;
    expect(dialog.hasAttribute('open')).toBe(true);
    dialog.dispatchEvent(new Event('close'));
    await update();
    expect(dialog.hasAttribute('open')).toBe(false);
  });

  describe('dragging the chat taller or shorter (HallChatResizer)', () => {
    afterEach(() => vi.restoreAllMocks());

    const press = (type: string, init: { button?: number; clientY?: number }) =>
      Object.assign(new MouseEvent(type, { bubbles: true, cancelable: true, ...init }), { pointerId: 1 });

    it('makes the chat follow the pointer while the bar is dragged, within its limits, and keeps the height', async () => {
      await open('/help');
      const bar = el().querySelector<HTMLElement>('.gd-hall__resizer')!;
      const chat = el().querySelector<HTMLElement>('gd-chat')!;
      vi.spyOn(chat, 'getBoundingClientRect').mockReturnValue({ bottom: 700 } as DOMRect);
      bar.dispatchEvent(press('pointerdown', { button: 2, clientY: 400 }));
      await update();
      expect(bar.hasAttribute('data-dragging')).toBe(false);
      bar.dispatchEvent(press('pointerdown', { button: 0, clientY: 400 }));
      await update();
      expect(bar.hasAttribute('data-dragging')).toBe(true);
      bar.dispatchEvent(press('pointermove', { clientY: 350 }));
      await update();
      expect(el().style.getPropertyValue('--gd-chat-height')).toBe('350px');
      bar.dispatchEvent(press('pointermove', { clientY: 650 }));
      await update();
      expect(el().style.getPropertyValue('--gd-chat-height')).toBe('100px');
      expect(cookies.read(CHAT_HEIGHT_COOKIE)).toBeNull();
      bar.dispatchEvent(press('pointerup', {}));
      await update();
      expect(bar.hasAttribute('data-dragging')).toBe(false);
      expect(cookies.read(CHAT_HEIGHT_COOKIE)).toBe('100');
      // let go: moving no longer resizes
      bar.dispatchEvent(press('pointermove', { clientY: 300 }));
      await update();
      expect(el().style.getPropertyValue('--gd-chat-height')).toBe('100px');
    });

    it('keeps the chat within the window when the window gets smaller', async () => {
      cookies.write(CHAT_HEIGHT_COOKIE, '500');
      await open('/help');
      expect(el().style.getPropertyValue('--gd-chat-height')).toBe('500px');
      vi.spyOn(window, 'innerHeight', 'get').mockReturnValue(560);
      window.dispatchEvent(new Event('resize'));
      await update();
      expect(el().style.getPropertyValue('--gd-chat-height')).toBe('400px');
    });
  });

  describe('Log Out', () => {
    const SESSION = at('/argonauth/session');

    async function answerSession(fixture: string) {
      backend.match(SESSION).forEach((r) => r.flush(readServerResponse(fixture)));
      await update();
    }

    it('is beside the connection readout, and logs the alias out', async () => {
      const go = vi.spyOn(TestBed.inject(PageNavigation), 'go').mockImplementation(() => undefined);
      const announce = vi.spyOn(TestBed.inject(SessionChannel), 'announce').mockImplementation(() => undefined);
      await open('/hall');
      await answerPlayer();
      await answerSession('argonauth-session.json');
      const presence = el().querySelector('.gd-hall__bar > .gd-hall__presence')!;
      expect(Array.from(presence.children).map((child) => child.localName)).toEqual(['gd-hall-connection', 'button']);
      const logout = presence.querySelector<HTMLButtonElement>('button.gd-hall__logout[data-action="logout"]')!;
      expect(logout.textContent!.trim()).toBe('Log Out');
      logout.click();
      await update();
      expect(logout.disabled).toBe(true);
      const request = backend.expectOne((r) => at('/argonauth/logout')(r) && r.method === 'POST');
      expect(request.request.body.toString()).toBe('scope=alias');
      request.flush(readServerResponse('argonauth-logout-alias.json'));
      await update();
      expect(announce).toHaveBeenCalledTimes(1);
      expect(go).toHaveBeenCalledWith('/gemp-lotr/ng/login');
    });

    it('is there in test mode too', async () => {
      await open('/hall');
      await answerPlayer();
      await answerSession('argonauth-session-test.json');
      expect(el().querySelector('.gd-hall__presence .gd-hall__logout')).not.toBeNull();
    });

    it('is not there with no alias played', async () => {
      await open('/hall');
      await answerPlayer();
      await answerSession('argonauth-session-aliases.json');
      expect(el().querySelector('.gd-hall__presence gd-hall-connection')).not.toBeNull();
      expect(el().querySelector('.gd-hall__logout')).toBeNull();
    });
  });
});
