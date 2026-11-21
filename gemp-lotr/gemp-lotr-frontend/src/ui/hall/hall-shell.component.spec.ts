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
import { HallChannel } from '../../core/platform/hall-tab.service';
import { LocalSettings } from '../../core/platform/local-settings.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
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

  it("shows admins the Admin tab, the legacy hall's", async () => {
    await open('/hall');
    await answerPlayer({ name: 'boss', type: 'ua' });
    const admin = el().querySelector<HTMLAnchorElement>('.gd-hall__tab[data-tab="admin"]');
    expect(admin?.getAttribute('href')).toBe('/gemp-lotr/hall-legacy.html?admin=1#admin');
    expect(admin?.target).toBe('');
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
});
