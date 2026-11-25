import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { CardInfoService } from '../../cards/card-info.service';
import { provideCardInteractions } from '../../cards/card-interactions';
import { CardViews } from '../../cards/card-views.service';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { GameCardActions } from '../table/game-card-actions';
import { GameCardInfo, RETRIEVING } from '../table/game-card-info';
import { GameCardViews } from '../table/game-card-views';
import { GameTableState } from '../table/game-table.service';
import { GameSession } from './game-session.service';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { GameViewState } from './game-view-state';

const GAME = '/gemp-lotr-server/game/7';
const card = (id: string, zone = 'HAND', blueprint = '1_7') =>
  `<ge type="PCIP" cardId="${id}" blueprintId="${blueprint}" participantId="alice" zone="${zone}"/>`;
const players = '<ge type="P" participantId="alice" allParticipantIds="alice,bob"/>';

describe('GameViewState and the card information of a game', () => {
  let session: GameSession;
  let view: GameViewState;
  let info: CardInfoService;
  let backend: HttpTestingController;

  function setUp() {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        provideFakeNotifier(),
        ...provideCardInteractions({ actions: GameCardActions, autoZoomCookie: 'test-zoom' }),
      ],
    });
    session = TestBed.inject(GameSession);
    view = TestBed.inject(GameViewState);
    info = TestBed.inject(CardInfoService);
    TestBed.inject(GameCardInfo);
    backend = TestBed.inject(HttpTestingController);
  }

  /** Opens the game: the server sends it, with nothing for the player to decide (one question for news follows). */
  function open(events: string) {
    view.restore('7');
    session.open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(`<gameState cn="1">${events}</gameState>`);
    TestBed.tick();
  }

  const showCard = (id: string) => {
    info.show(TestBed.inject(GameCardViews).of(session.model().cards[id]!));
    TestBed.tick();
  };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    setUp();
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  it('opens pile dialogs, the last one in front, and closes them; shows and hides the other piles', () => {
    view.openPile('discard', 'alice');
    view.openPile('deck', 'bob');
    view.openPile('discard', 'alice');
    expect(view.pileDialogs()).toEqual([
      { pile: 'deck', player: 'bob' },
      { pile: 'discard', player: 'alice' },
    ]);
    view.closePile('deck', 'bob');
    expect(view.pileDialogs()).toEqual([{ pile: 'discard', player: 'alice' }]);
    view.togglePiles(1);
    view.togglePiles(0);
    view.togglePiles(1);
    expect(view.openPiles()).toEqual([0]);
  });

  it('a card shown gets what affects it from the server, and is shown again after a reload', () => {
    open(players + card('5') + card('6'));
    showCard('5');
    expect(info.details()).toBe(RETRIEVING);
    const asked = backend.expectOne((r) => r.url === `${GAME}/cardInfo`);
    // Another card is shown before the answer comes: the answer is not for it
    showCard('6');
    asked.flush('<b>old</b>');
    expect(info.details()).toBe(RETRIEVING);
    backend.expectOne((r) => r.url === `${GAME}/cardInfo`).flush('<b>Strength +1</b>');
    expect(info.details()).toBe('<b>Strength +1</b>');
    expect(view.cardInfo()).toEqual({ cardId: '6', blueprintId: '1_7' });

    view.tab.set('settings');
    view.openPile('discard', 'bob');
    view.togglePiles(1);
    view.chatScroll.set(40);
    view.panelScroll.set(12);
    view.menu.set({ cardId: '6', x: 1, y: 2, decision: 'd' });
    TestBed.inject(GameTableState).move(['5', '6'], '6', 0);
    TestBed.tick();

    TestBed.resetTestingModule();
    setUp();
    open(players + card('5') + card('6'));
    expect(info.card()?.blueprintId).toBe('1_7');
    backend.expectOne((r) => r.url === `${GAME}/cardInfo`).flush('<b>again</b>');
    expect(info.details()).toBe('<b>again</b>');
    expect({
      tab: view.tab(),
      piles: view.pileDialogs(),
      open: view.openPiles(),
      chat: view.chatScroll(),
      panel: view.panelScroll(),
      menu: view.menu(),
      order: view.cardOrder(),
    }).toEqual({
      tab: 'settings',
      piles: [{ pile: 'discard', player: 'bob' }],
      open: [1],
      chat: 40,
      panel: 12,
      // A menu for a decision that no longer waits is not opened again
      menu: null,
      order: ['6', '5'],
    });
  });

  it('a card named in a text comes back after a reload too; a card gone since does not', () => {
    open(players + card('5'));
    info.show(TestBed.inject(CardViews).create({ ref: { hint: true }, blueprintId: '2_3', zone: 'SPECIAL' }));
    TestBed.tick();
    // Not a card of the game: nothing to ask the server
    backend.expectNone((r) => r.url === `${GAME}/cardInfo`);
    expect(view.cardInfo()).toEqual({ cardId: null, blueprintId: '2_3' });
    TestBed.resetTestingModule();
    setUp();
    open(players + card('5'));
    expect(info.card()?.blueprintId).toBe('2_3');

    showCard('5');
    backend.expectOne((r) => r.url === `${GAME}/cardInfo`).flush('');
    TestBed.resetTestingModule();
    setUp();
    open(players);
    expect(info.card()).toBeNull();
    expect(view.cardInfo()).toBeNull();
  });

  it('asks nothing for a card shown only for a decision, nor in a replay; a closed card is forgotten', () => {
    open(players + card('temp1'));
    showCard('temp1');
    backend.expectNone((r) => r.url === `${GAME}/cardInfo`);
    info.close();
    TestBed.tick();
    expect(view.cardInfo()).toBeNull();
  });

  it('ignores what is not its own in what was kept', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(`<gameState cn="1">${players}</gameState>`);
    session.keepView({
      cardInfo: { cardId: 5, blueprintId: '1_1' },
      cardOrder: ['1', 2],
      pileDialogs: [{ pile: 'deck', player: 'bob' }, { pile: 1 }],
      openPiles: [0, 'x'],
      menu: { cardId: '1' },
    });
    TestBed.resetTestingModule();
    setUp();
    view.restore('7');
    expect(view.cardInfo()).toEqual({ cardId: null, blueprintId: '1_1' });
    expect(view.cardOrder()).toEqual(['1']);
    expect(view.pileDialogs()).toEqual([{ pile: 'deck', player: 'bob' }]);
    expect(view.openPiles()).toEqual([0]);
    expect(view.menu()).toBeNull();
  });
});
