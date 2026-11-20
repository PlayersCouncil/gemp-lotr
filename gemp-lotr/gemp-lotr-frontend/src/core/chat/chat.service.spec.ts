import { ChatApi } from './chat-api.service';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { settle } from '../../testing/fake-notifier';
import { LocalSettings } from '../platform/local-settings.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { Player } from '../session/player';
import { PlayerInfo } from '../session/player-info.service';
import {
  CHAT_CHOICE_KEY,
  CHAT_CONNECTION_LOST,
  CHAT_INACTIVE,
  CHAT_NOT_LOGGED_IN,
  STATUS_INCOGNITO,
  STATUS_NOT_IN_CHAT,
} from './chat-messages';
import { TournamentAnnouncement } from './chat-models';
import { CHAT_POLL_DELAY_MS, ChatService, MAX_CHAT_LINES, USER_LIST_STATUS_MS } from './chat.service';

const ROOM = '/gemp-lotr-server/chat/Game%20Hall';
const IGNORES = '/gemp-lotr-server/player/ignores';

class MemorySettings {
  readonly values = new Map<string, string>();
  read(key: string) {
    return this.values.get(key) ?? null;
  }
  write(key: string, value: string) {
    this.values.set(key, value);
  }
}

describe('ChatService', () => {
  let backend: HttpTestingController;
  let chat: ChatService;
  let settings: MemorySettings;
  let announcements: TournamentAnnouncement[];
  const player = signal<Player | null>({ name: 'alice', type: 'u', hasTrophies: false });

  function create(stored?: string): void {
    settings = new MemorySettings();
    if (stored) settings.values.set(CHAT_CHOICE_KEY, stored);
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ChatApi,
        ChatService,
        { provide: PlayerInfo, useValue: { player } },
        { provide: LocalSettings, useValue: settings },
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    chat = TestBed.inject(ChatService);
    announcements = [];
    chat.tournamentAnnouncements$.subscribe((a) => announcements.push(a));
  }

  beforeEach(() => vi.useFakeTimers());
  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  const join = (): TestRequest => backend.expectOne((r) => r.url === ROOM && r.method === 'GET');
  const poll = (): TestRequest =>
    backend.expectOne((r) => r.url === ROOM && r.method === 'POST' && !r.body.has('message'));
  const sent = (): TestRequest[] =>
    backend.match((r) => r.url === ROOM && r.method === 'POST' && r.body.has('message'));
  const ignores = (): TestRequest => backend.expectOne((r) => r.url === IGNORES);

  /** Starts, answers the join and the ignore list, and waits for the first poll. */
  function joined(): void {
    chat.start();
    join().flush(readServerResponse('chat.xml'));
    ignores().flush(readServerResponse('ignores.json'));
    vi.advanceTimersByTime(CHAT_POLL_DELAY_MS);
  }

  it('joins the room and shows its history, users and highlights', () => {
    create();
    expect(chat.canSend()).toBe(false);
    joined();
    expect(chat.canSend()).toBe(true);
    expect(chat.lines().map((l) => [l.kind, l.from, l.highlight])).toEqual([
      ['system', 'System', null],
      ['chat', 'bob', 'ping'],
      ['chat', 'carol', 'mention'],
      ['chat', 'TournamentSystem', null],
      ['system', 'System', null],
    ]);
    expect(chat.lines()[1].date).toBe(1794232860000);
    expect(announcements).toEqual([{ message: 'Your <b>round 2</b> game is ready', recipients: ['alice', 'dave'] }]);
    expect(chat.users().map((u) => u.name)).toEqual(['alice', 'bob', 'carol', 'dave']);
    expect(chat.players()).toEqual(['alice', 'bob', 'carol', 'dave']);
    expect(chat.ignored()).toEqual(['Carol']);
    expect(chat.isIgnored('carol')).toBe(true);

    poll().flush(readServerResponse('chat-update.xml'));
    expect(
      chat
        .lines()
        .slice(-2)
        .map((l) => l.html),
    ).toEqual(['erin joined the room', 'The tournament starts in 1 minute']);
    expect(announcements[1]).toEqual({ message: 'The tournament starts in 1 minute', recipients: null });
    expect(chat.users().map((u) => [u.name, u.incognito])).toEqual([
      ['alice', true],
      ['bob', false],
      ['erin', false],
    ]);
    // the ignore list is asked once
    vi.advanceTimersByTime(CHAT_POLL_DELAY_MS);
    poll();
    chat.stop();
  });

  it('keeps at most 500 lines', () => {
    create();
    joined();
    for (let i = 0; i < MAX_CHAT_LINES; i++) chat.appendWarning(`w${i}`);
    expect(chat.lines().length).toBe(MAX_CHAT_LINES);
    expect(chat.lines()[0].html).toBe('w0');
    expect(chat.lines().at(-1)!.html).toBe(`w${MAX_CHAT_LINES - 1}`);
    poll();
    chat.stop();
  });

  it('sends messages one at a time, in order, and never blank ones', () => {
    create();
    joined();
    chat.send('first');
    chat.send('   ');
    chat.send('second\nline');
    let out = sent();
    expect(out.map((r) => r.request.body.get('message'))).toEqual(['first']);
    out[0].flush('');
    out = sent();
    expect(out.map((r) => r.request.body.get('message'))).toEqual(['second\nline']);
    out[0].flush('');
    poll();
    chat.stop();
  });

  it('says when a command failed, and goes on', () => {
    create();
    joined();
    chat.send('/ban bob');
    chat.send('sorry');
    sent()[0].flush('', { status: 400, statusText: 'Bad Request' });
    expect(chat.lines().at(-1)!.html).toBe('Unknown chat problem occurred (error=400)');
    expect(chat.canSend()).toBe(true);
    sent()[0].flush('');
    poll();
    chat.stop();
  });

  it.each([
    [0, CHAT_CONNECTION_LOST],
    [410, CHAT_INACTIVE],
  ])('stops with a warning when a poll fails with %s', (status, warning) => {
    create();
    joined();
    const req = poll();
    if (status === 0) req.error(new ProgressEvent('error'));
    else req.flush('', { status, statusText: 'Error' });
    expect(chat.lines().at(-1)).toMatchObject({ kind: 'warning', html: warning, from: null, loginLink: false });
    expect(chat.stopped()).toBe(true);
    expect(chat.canSend()).toBe(false);
    vi.advanceTimersByTime(60_000);
    backend.expectNone((r) => r.url === ROOM);
  });

  it('stops after an unknown poll failure too, saying so', () => {
    create();
    joined();
    poll().flush('', { status: 500, statusText: 'Error' });
    expect(chat.lines().at(-1)!.html).toBe('Unknown chat problem occurred (error=500)');
    expect(chat.stopped()).toBe(true);
    vi.advanceTimersByTime(60_000);
    backend.expectNone((r) => r.url === ROOM);
  });

  it('times out a poll after 20 s', () => {
    create();
    joined();
    const req = poll();
    vi.advanceTimersByTime(20_000);
    expect(req.cancelled).toBe(true);
    expect(chat.lines().at(-1)!.html).toBe(CHAT_CONNECTION_LOST);
  });

  it('shows the "not logged in" warning once, with its link', () => {
    create();
    chat.showLoggedOut();
    expect(chat.canSend()).toBe(false);
    chat.start();
    join().flush('', { status: 401, statusText: 'Unauthorized' });
    expect(chat.lines().map((l) => [l.html, l.loginLink])).toEqual([[CHAT_NOT_LOGGED_IN, true]]);
  });

  it('ignores and unignores players, with a status line that goes away', async () => {
    create();
    joined();
    const done = chat.toggleIgnore('erin');
    const req = backend.expectOne((r) => r.url === '/gemp-lotr-server/player/ignores/add');
    expect(req.request.body.get('name')).toBe('erin');
    req.flush(readServerResponse('ignore-added.json'));
    await done;
    expect(chat.ignored()).toEqual(['Carol', 'erin']);
    expect(chat.userListStatus()).toBe('erin is now ignored.');

    const undo = chat.toggleIgnore('carol');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player/ignores/remove').flush('{}');
    await undo;
    expect(chat.userListStatus()).toBe('Unignored carol.');
    vi.advanceTimersByTime(USER_LIST_STATUS_MS);
    expect(chat.userListStatus()).toBeNull();
    poll();
    chat.stop();
  });

  it('switches incognito at once, and says why it could not', async () => {
    create();
    joined();
    let done = chat.setIncognito(true);
    const req = backend.expectOne((r) => r.url === '/gemp-lotr-server/player/incognito');
    expect(req.request.body.get('incognito')).toBe('true');
    req.flush('{"inRoom":true,"incognito":true}');
    await done;
    expect(chat.users()[0]).toMatchObject({ name: 'alice', incognito: true });
    expect(chat.userListStatus()).toBe(STATUS_INCOGNITO);

    done = chat.setIncognito(false);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/player/incognito')
      .flush('', { status: 409, statusText: 'Conflict' });
    await done;
    await settle();
    expect(chat.userListStatus()).toBe(STATUS_NOT_IN_CHAT);
    expect(chat.users()[0].incognito).toBe(true);
    poll();
    chat.stop();
  });

  it('remembers the Discord / built-in choice (Discord until chosen)', () => {
    create();
    expect(chat.discord()).toBe(true);
    chat.setDiscord(false);
    expect(settings.values.get(CHAT_CHOICE_KEY)).toBe('builtin');
    TestBed.resetTestingModule();
    create('builtin');
    expect(chat.discord()).toBe(false);
  });
});
