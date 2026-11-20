import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { provideGameState } from '../game-state';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { GameUndo } from '../undo/game-undo.service';
import { GameDecisions, SELECTION_DEBOUNCE_MS, UNASSIGNED_QUESTION } from './game-decisions.service';

const GAME = '/gemp-lotr-server/game/7';
const param = (name: string, ...values: string[]) =>
  values.map((value) => `<parameter name="${name}" value="${value}"/>`).join('');
const decision = (type: string, params: string) =>
  `<gameState cn="1"><ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>` +
  `<ge type="PCIP" cardId="5" blueprintId="1_7" participantId="bob" zone="SHADOW_CHARACTERS"/>` +
  `<ge type="D" id="3" decisionType="${type}" participantId="alice" text="Do it">${params}</ge></gameState>`;

describe('GameDecisions', () => {
  let decisions: GameDecisions;
  let session: GameSession;
  let settings: GameSettings;
  let backend: HttpTestingController;
  let notifier: FakeNotifier;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    decisions = TestBed.inject(GameDecisions);
    session = TestBed.inject(GameSession);
    settings = TestBed.inject(GameSettings);
    backend = TestBed.inject(HttpTestingController);
    notifier = TestBed.inject(FakeNotifier);
  });

  afterEach(() => backend.match(() => true));

  function open(type: string, params: string) {
    session.open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(decision(type, params));
    TestBed.tick();
  }

  /** The answer sent, or null if none. */
  function answer(): string | null {
    const sent = backend.match((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'));
    return sent.length ? (sent[0]!.request.body?.get('decisionValue') ?? null) : null;
  }

  const at = { x: 1, y: 2 };

  it('chooses cards up to the most allowed; with auto-accept the last one answers', () => {
    open('CARD_SELECTION', param('cardId', '1', '2', '3') + param('min', '1') + param('max', '2'));
    decisions.cardClicked('1', at, 1000);
    expect(decisions.view().marks['1']).toBe('selected');
    expect(answer()).toBeNull();
    // Not offered: nothing happens
    decisions.cardClicked('9', at, 2000);
    decisions.cardClicked('2', at, 3000);
    expect(answer()).toBe('1,2');
  });

  it('ignores a second click on the same card within the debounce time', () => {
    settings.setAutoAccept(false);
    open('CARD_SELECTION', param('cardId', '1', '2') + param('min', '0') + param('max', '2'));
    decisions.cardClicked('1', at, 1000);
    decisions.press('Reset choice');
    decisions.cardClicked('1', at, 1000 + SELECTION_DEBOUNCE_MS - 1);
    expect(session.model().local.selectedCardIds).toEqual([]);
    decisions.cardClicked('1', at, 1000 + SELECTION_DEBOUNCE_MS);
    expect(session.model().local.selectedCardIds).toEqual(['1']);
  });

  it('without auto-accept waits for Done, which needs the least number chosen', async () => {
    settings.setAutoAccept(false);
    open('CARD_SELECTION', param('cardId', '1', '2') + param('min', '2') + param('max', '2'));
    decisions.cardClicked('1', at, 1000);
    await decisions.press('Done');
    expect(answer()).toBeNull();
    decisions.cardClicked('2', at, 2000);
    expect(answer()).toBeNull();
    await decisions.press('Done');
    expect(answer()).toBe('1,2');
  });

  it("answers a card's only action at once, and shows the menu of a card with several", () => {
    open(
      'CARD_ACTION_CHOICE',
      param('cardId', '5', '5', '6') +
        param('blueprintId', 'inPlay', 'inPlay', 'inPlay') +
        param('actionId', '10', '11', '12') +
        param('actionText', 'Use', 'Exert', 'Play'),
    );
    decisions.cardClicked('5', at, 1000);
    expect(decisions.menu()).toEqual({
      cardId: '5',
      x: 1,
      y: 2,
      actions: [
        { actionId: '10', text: 'Use', noEffect: null },
        { actionId: '11', text: 'Exert', noEffect: null },
      ],
    });
    decisions.closeMenu();
    expect(decisions.menu()).toBeNull();
    decisions.chooseAction('11');
    expect(answer()).toBeNull();
    decisions.cardClicked('5', at, 2000);
    decisions.chooseAction('11');
    expect(answer()).toBe('11');
  });

  it("passes, or without auto-accept marks the action's card until Done", async () => {
    settings.setAutoAccept(false);
    open(
      'CARD_ACTION_CHOICE',
      param('cardId', '6') + param('blueprintId', 'inPlay') + param('actionId', '12') + param('actionText', 'Play'),
    );
    decisions.cardClicked('6', at, 1000);
    expect(decisions.view().marks).toEqual({ '6': 'selected' });
    await decisions.press('Done');
    expect(answer()).toBe('12');
  });

  it('passes', async () => {
    open('CARD_ACTION_CHOICE', '');
    await decisions.press('Pass');
    expect(answer()).toBe('');
  });

  it('answers a choice of actions shown as cards with the action of the card clicked', () => {
    open('ACTION_CHOICE', param('blueprintId', '1_1', '1_2') + param('actionId', '7', '8'));
    decisions.cardClicked('temp1', at, 1000);
    expect(answer()).toBe('8');
  });

  it('finishes a choice of cards in a dialog with Escape once enough are chosen', () => {
    settings.setAutoAccept(false);
    open(
      'ARBITRARY_CARDS',
      param('cardId', 'a', 'b') + param('selectable', 'true', 'true') + param('min', '1') + param('max', '2'),
    );
    decisions.escape();
    expect(answer()).toBeNull();
    decisions.cardClicked('b', at, 1000);
    decisions.escape();
    expect(answer()).toBe('b');
  });

  it("answers looking through one's own deck at once, if the player asked not to see it", () => {
    settings.setAutoDismissDeckReveal(true);
    open('ARBITRARY_CARDS', param('deckInspection', 'own') + param('min', '0') + param('max', '0'));
    expect(answer()).toBe('');
  });

  it('answers a number and a choice', () => {
    open('INTEGER', param('min', '0'));
    decisions.answerChoice(1);
    decisions.answerNumber('2');
    expect(answer()).toBe('2');
  });

  it('answers a multiple choice by its place', () => {
    open('MULTIPLE_CHOICE', param('results', 'a', 'b'));
    decisions.answerNumber('9');
    decisions.answerChoice(1);
    expect(answer()).toBe('1');
  });

  it('assigns a minion to the character clicked next, takes it back by clicking it again, and asks before Done', async () => {
    open('ASSIGN_MINIONS', param('freeCharacters', '1', '2') + param('minions', '5'));
    decisions.cardClicked('5', at, 1000);
    expect(decisions.view().marks).toEqual({ '1': 'selectable', '2': 'selectable', '5': 'selectable' });
    decisions.cardClicked('5', at, 2000);
    expect(session.model().local).toMatchObject({ minion: null, assignments: {} });
    // Done with the minion left out: the player is asked, and says no
    notifier.answers.push(false);
    await decisions.press('Done');
    await settle();
    expect(notifier.dialogs).toEqual([`confirm: ${UNASSIGNED_QUESTION}`]);
    expect(answer()).toBeNull();
    decisions.cardClicked('5', at, 3000);
    decisions.cardClicked('2', at, 4000);
    expect(session.model().local.assignments).toEqual({ '5': '2' });
    await decisions.press('Done');
    expect(answer()).toBe('1,2 5');
  });

  it('answers a decision that only asks to be confirmed, if the player asked to skip those (not otherwise)', () => {
    open('CARD_SELECTION', param('min', '0') + param('max', '0') + param('confirmOnly', 'true'));
    expect(answer()).toBeNull();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    decisions = TestBed.inject(GameDecisions);
    session = TestBed.inject(GameSession);
    backend = TestBed.inject(HttpTestingController);
    TestBed.inject(GameSettings).setSkipConfirmations(true);
    open('CARD_SELECTION', param('min', '0') + param('max', '0') + param('confirmOnly', 'true'));
    expect(answer()).toBe('');
  });

  it('while a revert request waits, no gesture answers or chooses anything', async () => {
    settings.setAutoAccept(false);
    open('CARD_SELECTION', param('cardId', '1', '2') + param('min', '0') + param('max', '2'));
    const undo = TestBed.inject(GameUndo);
    undo.pending.set({
      requestId: 1,
      requester: 'bob',
      label: 'x',
      turn: 1,
      phase: '',
      tier: 'RED',
      reasons: [],
      secondsLeft: 60,
    });
    decisions.cardClicked('1', at, 1000);
    decisions.chooseAction('1');
    decisions.answerNumber('1');
    decisions.answerChoice(0);
    decisions.escape();
    await decisions.press('Done');
    expect(session.model().local.selectedCardIds).toEqual([]);
    expect(answer()).toBeNull();
    // Resolved: the decision is answered as ever
    undo.pending.set(null);
    decisions.cardClicked('1', at, 2000);
    await decisions.press('Done');
    expect(answer()).toBe('1');
  });

  it("closes a card's menu when the game was taken back and read again", () => {
    const menu = param('cardId', '5', '5') + param('blueprintId', 'inPlay', 'inPlay') + param('actionId', 'a', 'b');
    open('CARD_ACTION_CHOICE', menu + param('actionText', 'A', 'B'));
    decisions.cardClicked('5', at, 1000);
    expect(decisions.menu()).not.toBeNull();
    session.reopen(null);
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(decision('CARD_ACTION_CHOICE', menu + param('actionText', 'A', 'B')));
    TestBed.tick();
    expect(decisions.menu()).toBeNull();
  });

  it('does nothing without a waiting decision', async () => {
    decisions.cardClicked('1', at);
    await decisions.press('Done');
    await decisions.press('OK');
    decisions.escape();
    expect(answer()).toBeNull();
  });
});
