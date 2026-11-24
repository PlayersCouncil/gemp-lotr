import { HallTablesApi } from './hall-tables-api.service';
import { ChatApi } from '../../chat/chat-api.service';
import { HallApi } from '../hall-api.service';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { settle } from '../../../testing/fake-notifier';
import { LegacyCookies } from '../../platform/legacy-cookies';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { PageNavigation } from '../../platform/page-navigation';
import { SoundPlayer } from '../../platform/sound-player';
import { PlayerInfo } from '../../session/player-info.service';
import { HallComms, NEXT_POLL_DELAY_MS } from '../hall-comms.service';
import { HALL_SERVICES } from '../hall-services';
import { HallSession } from '../hall-session.service';
import { HallReadyOffers } from '../ready/hall-ready-offers.service';
import { CasualTableForm } from './hall-table-models';
import {
  AGE_REFRESH_MS,
  BOT_DECK_FLOW,
  JOIN_DECK_FLOW,
  CASUAL_FORMAT_COOKIE,
  CASUAL_TIMER_COOKIE,
  GAME_START_SOUND,
  HallTablesService,
} from './hall-tables.service';
import {
  CANNOT_INVITE_SELF,
  MUST_NAME_INVITEE,
  MUST_SELECT_DECK,
  MUST_SELECT_FORMAT,
  TABLE_CREATED_KEPT_OPEN,
} from './hall-tables-messages';

const SERVER = '/gemp-lotr-server';
const SERVER_NOW = 1794232985000;

class FakeCookies {
  readonly values = new Map<string, string>();
  read(name: string): string | null {
    return this.values.get(name) ?? null;
  }
  write(name: string, value: string): void {
    this.values.set(name, value);
  }
}

describe('HallTablesService', () => {
  let backend: HttpTestingController;
  let comms: HallComms;
  let tables: HallTablesService;
  let cookies: FakeCookies;
  let opened: string[];
  let blockPopups: boolean;
  let sounds: string[];

  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(SERVER_NOW - 2000);
    cookies = new FakeCookies();
    opened = [];
    sounds = [];
    blockPopups = false;
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        HallApi,
        ChatApi,
        HallTablesApi,
        ...HALL_SERVICES,
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
        { provide: LegacyCookies, useValue: cookies },
        {
          provide: PageNavigation,
          useValue: {
            go: () => undefined,
            openInNewTab: (url: string) => {
              opened.push(url);
              return !blockPopups;
            },
          },
        },
        { provide: SoundPlayer, useValue: { play: (url: string) => sounds.push(url) } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    comms = TestBed.inject(HallComms);
    tables = TestBed.inject(HallTablesService);
  });

  afterEach(() => {
    comms.stop();
    vi.useRealTimers();
    // the long-poll's last request is left open by design
    backend.match((r) => r.url === `${SERVER}/hall/update`);
    backend.verify();
  });

  const request = (path: string, method = 'POST'): TestRequest =>
    backend.expectOne((r) => r.url === SERVER + path && r.method === method);
  const body = (req: TestRequest) => {
    const params = req.request.body as HttpParams;
    return Object.fromEntries(params.keys().map((key) => [key, params.get(key)]));
  };
  const offers = () => TestBed.inject(HallReadyOffers);
  const ids = (rows: readonly { table: { id: string } }[]) => rows.map((row) => row.table.id);

  /** Signs up with hall.xml and leaves the first update poll open. */
  function connect(): void {
    comms.start();
    request('/hall', 'GET').flush(readServerResponse('hall.xml'));
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
  }

  function poll(file: string): void {
    request('/hall/update').flush(readServerResponse(file));
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
  }

  async function loadOptions(decks = readServerResponse('deck-list.xml')): Promise<void> {
    const loading = tables.loadOptions();
    request('/deck/formats').flush(readServerResponse('hall-formats.json'));
    request('/deck/list', 'GET').flush(decks);
    request('/deck/libraryList', 'GET').flush(
      '<decks><deck targetFormat="Fellowship Block">FotR Starter - Gandalf</deck></decks>',
    );
    await loading;
  }

  const casual = (form: Partial<CasualTableForm> = {}): CasualTableForm => ({
    format: 'movie',
    deck: { name: 'Elves', source: 'player' },
    timer: 'blitz',
    description: 'go easy',
    isPrivate: false,
    isInviteOnly: false,
    invitee: '',
    keepOpen: false,
    ...form,
  });

  it('lists the tables of the snapshot and follows the incremental updates', () => {
    connect();
    expect(ids(tables.sections().waiting)).toEqual(['11']);
    expect(ids(tables.sections().playing)).toEqual(['12']);
    expect(tables.counts()).toEqual({ waiting: 1, playing: 1, finished: 0 });

    poll('hall-tables-update.xml');
    const { waiting, playing, finished } = tables.sections();
    expect(ids(waiting)).toEqual(['11', '13', '14']);
    expect(waiting.map((row) => row.action)).toEqual(['leave', 'join', null]);
    expect(waiting[1].note).toBe("Private match for user 'erin'.");
    expect(playing).toEqual([]);
    expect(finished[0].table).toMatchObject({ id: '12', winner: 'carol' });

    poll('hall-update.xml');
    expect(ids(tables.sections().waiting)).toEqual(['13', '14']);
    expect(tables.sections().playing[0]).toMatchObject({
      action: 'play',
      gameUrl: '/gemp-lotr/ng/game?gameId=g-11',
    });
    expect(tables.sections().finished).toEqual([]);
  });

  it('starts over when the channel signs up again', () => {
    connect();
    poll('hall-tables-update.xml');
    request('/hall/update').flush('', { status: 410, statusText: 'Gone' });
    vi.advanceTimersByTime(1000);
    request('/hall', 'GET').flush(readServerResponse('hall.xml'));
    expect(ids(tables.sections().waiting)).toEqual(['11']);
    expect(tables.sections().finished).toEqual([]);
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
  });

  it('ticks the waiting timers on the server clock', () => {
    connect();
    const table = tables.table('11')!;
    // created 85 s before the server's clock, which runs 2 s ahead of ours
    expect(tables.ageOf(table)).toBe('01:25');
    vi.advanceTimersByTime(AGE_REFRESH_MS * 5);
    expect(tables.ageOf(table)).toBe('01:30');
    expect(tables.ageOf(tables.table('12')!)).toBeNull();
  });

  it('opens a started game in a new tab once, with the fanfare', () => {
    connect();
    expect(opened).toEqual(['/gemp-lotr/ng/game?gameId=g-9']);
    expect(sounds).toEqual([GAME_START_SOUND]);
    poll('hall-update.xml');
    expect(opened.length).toBe(2);
    expect(opened[1]).toContain('gameId=g-11');
    expect(offers().offers()).toEqual([]);
  });

  it('offers a blocked game in a prompt until its table goes', () => {
    blockPopups = true;
    comms.start();
    request('/hall', 'GET').flush(
      readServerResponse('hall.xml').replace('<newGame id="g-9"/>', '<newGame id="g-12"/>'),
    );
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    expect(offers().offers()).toEqual([
      {
        key: 'game:g-12',
        kind: 'game',
        url: '/gemp-lotr/ng/game?gameId=g-12',
        title: 'Your game is ready',
        text: 'Your game has started, but your browser blocked the new window.',
        label: 'Open game',
      },
    ]);
    poll('hall-update-empty.xml');
    expect(offers().offers().length).toBe(1);
    // table 12 is removed; g-11 and g-9 (not announced by this snapshot) start
    poll('hall-update.xml');
    expect(
      offers()
        .offers()
        .map((g) => g.key),
    ).toEqual(['game:g-11', 'game:g-9']);
    offers().dismiss('game:g-11');
    expect(
      offers()
        .offers()
        .map((g) => g.key),
    ).toEqual(['game:g-9']);
  });

  it('loads the hall formats in order, the timers and both deck lists', async () => {
    await loadOptions();
    expect(tables.formats()).toEqual([
      { code: 'fotr_block', name: 'Fellowship Block' },
      { code: 'pc_fotr_block', name: 'PC-Fellowship Block' },
      { code: 'movie', name: 'Movie Block' },
    ]);
    expect(tables.timersOffered().map((t) => t.code)).toEqual(['default', 'blitz', 'WC', 'slow', 'glacial']);
    expect(tables.timerDescription('wc')).toContain('20 minutes');
    expect(tables.playerDecks()).toEqual([
      { name: 'Test + deck & co', formatName: 'Fellowship Block', source: 'player' },
    ]);
    expect(tables.libraryDecks()[0].source).toBe('library');
    expect(tables.optionsLoaded()).toBe(true);
    expect(tables.formatOfDeck({ name: 'Test + deck & co', source: 'player' })).toBe('fotr_block');
    expect(tables.formatOfDeck({ name: 'nope', source: 'player' })).toBeNull();
  });

  it('reports formats that could not be loaded, and keeps the decks it had when a list fails', async () => {
    await loadOptions();
    const loading = tables.loadOptions();
    request('/deck/formats').flush('', { status: 500, statusText: 'Error' });
    request('/deck/list', 'GET').flush('', { status: 0, statusText: 'Unknown' });
    request('/deck/libraryList', 'GET').flush('<decks/>');
    await loading;
    expect(tables.optionsError()).toBe(
      'Could not load the list of formats: The server ran into an error. Please try again, and report a bug if it ' +
        'keeps happening.',
    );
    expect(tables.playerDecks().length).toBe(1);
    expect(tables.libraryDecks()).toEqual([]);
  });

  it('starts each flow with the deck last used, else the first one', async () => {
    await loadOptions();
    expect(tables.initialDeck(BOT_DECK_FLOW, 'fotr_block')).toEqual({ name: 'Test + deck & co', source: 'player' });
    cookies.write('bot-table-decks', JSON.stringify({ fotr_block: ['FotR Starter - Gandalf', 'library'] }));
    expect(tables.rememberedDeck(BOT_DECK_FLOW, 'fotr_block')).toEqual({
      name: 'FotR Starter - Gandalf',
      source: 'library',
    });
    expect(tables.rememberedDeck(BOT_DECK_FLOW, 'movie')).toBeNull();
    cookies.write('bot-table-decks', JSON.stringify({ _: ['Deleted deck', 'player'] }));
    expect(tables.initialDeck(BOT_DECK_FLOW, null)).toEqual({ name: 'Test + deck & co', source: 'player' });
  });

  it('gives a player with no decks a library deck', async () => {
    await loadOptions('<decks/>');
    expect(tables.initialDeck(JOIN_DECK_FLOW, null)).toEqual({ name: 'FotR Starter - Gandalf', source: 'library' });
  });

  it('creates a Casual table and remembers its choices', async () => {
    const creating = tables.createCasual(casual());
    expect(tables.results().casual).toEqual({ kind: 'pending', message: 'Working…' });
    expect(tables.busy()).toBe(true);
    const req = request('/hall');
    expect(body(req)).toEqual({
      format: 'movie',
      deckName: 'Elves',
      deckSource: 'player',
      timer: 'blitz',
      desc: 'go easy',
      isPrivate: 'false',
      isInviteOnly: 'false',
    });
    req.flush(readServerResponse('hall-table-ok.xml'));
    expect(await creating).toBe(true);
    expect(tables.results().casual).toEqual({ kind: 'success', message: 'OK' });
    expect(tables.busy()).toBe(false);
    expect(cookies.read(CASUAL_FORMAT_COOKIE)).toBe('movie');
    expect(cookies.read(CASUAL_TIMER_COOKIE)).toBe('blitz');
    expect(JSON.parse(cookies.read('unranked-table-decks')!)).toEqual({
      movie: ['Elves', 'player'],
      _: ['Elves', 'player'],
    });
    expect(tables.casualDefaults()).toEqual({ format: 'movie', timer: 'blitz', openByDefault: false });
  });

  it('keeps the popup open when asked, and shows a refusal', async () => {
    let creating = tables.createCasual(casual({ keepOpen: true }));
    request('/hall').flush('<result>OK</result>');
    expect(await creating).toBe(false);
    expect(tables.results().casual).toEqual({ kind: 'success', message: TABLE_CREATED_KEPT_OPEN });

    creating = tables.createCasual(casual({ isInviteOnly: true, invitee: 'nobody' }));
    const req = request('/hall');
    expect(body(req)).toMatchObject({ desc: 'nobody', isInviteOnly: 'true' });
    req.flush(readServerResponse('hall-table-error.xml'));
    expect(await creating).toBe(false);
    expect(tables.results().casual).toEqual({
      kind: 'error',
      message: "Cannot find player 'nobody'. Check your spelling and ensure the name is exact.",
    });
  });

  it('checks the Casual form before sending anything', async () => {
    const player = TestBed.inject(PlayerInfo);
    const loading = player.whoAmI();
    request('/player', 'GET').flush(readServerResponse('player.json'));
    await loading;

    expect(await tables.createCasual(casual({ format: '' }))).toBe(false);
    expect(tables.results().casual.message).toBe(MUST_SELECT_FORMAT);
    expect(await tables.createCasual(casual({ isInviteOnly: true, invitee: ' ' }))).toBe(false);
    expect(tables.results().casual.message).toBe(MUST_NAME_INVITEE);
    expect(await tables.createCasual(casual({ isInviteOnly: true, invitee: '* visual TESTER' }))).toBe(false);
    expect(tables.results().casual).toEqual({ kind: 'error', message: CANNOT_INVITE_SELF });
    expect(await tables.createCasual(casual({ deck: null }))).toBe(false);
    expect(tables.results().casual.message).toBe(MUST_SELECT_DECK);
    expect(cookies.values.size).toBe(0);
  });

  it('creates a bot table', async () => {
    const creating = tables.createBot({
      format: 'fotr_block',
      deck: { name: 'FotR Starter - Gandalf', source: 'library' },
      botDeck: '',
    });
    const req = request('/hall/solo');
    expect(body(req)).toEqual({
      format: 'fotr_block',
      deckName: 'FotR Starter - Gandalf',
      deckSource: 'library',
      botDeckName: '',
      isPrivate: 'false',
    });
    req.flush('<result>OK</result>');
    expect(await creating).toBe(true);
    expect(JSON.parse(cookies.read('bot-table-decks')!).fotr_block).toEqual(['FotR Starter - Gandalf', 'library']);
  });

  it('leaves a waiting table', async () => {
    const leaving = tables.leave('11');
    const req = request('/hall/11/leave');
    expect(body(req)).toEqual({});
    req.flush('<result>OK</result>');
    expect(await leaving).toBe(true);

    const failing = tables.leave('11');
    request('/hall/11/leave').flush('', { status: 0, statusText: 'Unknown' });
    expect(await failing).toBe(false);
    expect(tables.results().leave.message).toContain('Could not reach the server');
  });

  it('opens the Play popup for a player, and the login dialog for a visitor', async () => {
    tables.resetResult('casual');
    expect(tables.openPlay()).toBe(true);
    request('/deck/formats').flush(readServerResponse('hall-formats.json'));
    request('/deck/list', 'GET').flush('<decks/>');
    request('/deck/libraryList', 'GET').flush('<decks/>');
    await settle();

    const session = TestBed.inject(HallSession);
    session.requireLogin(false);
    expect(tables.openPlay()).toBe(false);
    expect(session.loginRequired()).toBe(true);
  });

  it('takes an invitation from the chat list', () => {
    tables.requestInvite('+ bob');
    expect(tables.invitee()).toBe('bob');
    tables.clearInvite();
    expect(tables.invitee()).toBeNull();
    tables.setCasualOpenByDefault(true);
    expect(tables.casualDefaults().openByDefault).toBe(true);
  });
});
