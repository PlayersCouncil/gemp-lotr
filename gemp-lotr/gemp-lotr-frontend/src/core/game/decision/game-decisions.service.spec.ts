import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { provideGameState } from '../game-state';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { DecisionGate, DecisionGates, DecisionGesture } from './decision-gate';
import { GameDecisions, SELECTION_DEBOUNCE_MS, UNASSIGNED_QUESTION, UNASSIGNED_WORDS } from './game-decisions.service';

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
    // In the page's own dialog, with its own words
    expect(notifier.words).toEqual([UNASSIGNED_WORDS]);
    expect(UNASSIGNED_WORDS).toEqual({ title: 'Unassigned minions', ok: 'Proceed', cancel: 'Go back' });
    expect(answer()).toBeNull();
    decisions.cardClicked('5', at, 3000);
    decisions.cardClicked('2', at, 4000);
    expect(session.model().local.assignments).toEqual({ '5': '2' });
    await decisions.press('Done');
    expect(answer()).toBe('1,2 5');
  });

  it('asks without a word when no more minions could have been assigned (the decision says how many could)', async () => {
    open('ASSIGN_MINIONS', param('freeCharacters', '1') + param('minions', '5', '6') + param('maxAssignable', '1'));
    session.assign('5', '1');
    await decisions.press('Done');
    expect(notifier.dialogs).toEqual([]);
    expect(answer()).toBe('1 5');
  });

  describe('with a gate', () => {
    let refused: DecisionGesture['kind'][];
    let asked: DecisionGesture[];
    let done: DecisionGesture[];

    beforeEach(() => {
      refused = [];
      asked = [];
      done = [];
      const gate: DecisionGate = {
        allows: (gesture) => {
          asked.push(gesture);
          return !refused.includes(gesture.kind);
        },
        after: (gesture) => done.push(gesture),
      };
      TestBed.inject(DecisionGates).add(gate);
    });

    it('puts every gesture to it first, does nothing it refuses, and tells it what went ahead', async () => {
      settings.setAutoAccept(false);
      open('CARD_SELECTION', param('cardId', '1', '2') + param('min', '1') + param('max', '2'));
      refused = ['card'];
      decisions.cardClicked('1', at, 1000);
      expect(session.model().local.selectedCardIds).toEqual([]);
      expect(asked).toEqual([{ kind: 'card', cardId: '1' }]);
      expect(done).toEqual([]);
      refused = [];
      decisions.cardClicked('1', at, 2000);
      expect(session.model().local.selectedCardIds).toEqual(['1']);
      expect(done).toEqual([{ kind: 'card', cardId: '1' }]);
      refused = ['button'];
      await decisions.press('Done');
      expect(answer()).toBeNull();
      refused = [];
      await decisions.press('Reset choice');
      expect(done.at(-1)).toEqual({ kind: 'button', button: 'Reset choice' });
      expect(session.model().local.selectedCardIds).toEqual([]);
    });

    it("leaves a card's menu open for another of its actions when one is refused", () => {
      open(
        'CARD_ACTION_CHOICE',
        param('cardId', '5', '5') +
          param('blueprintId', 'inPlay', 'inPlay') +
          param('actionId', '10', '11') +
          param('actionText', 'Use', 'Exert'),
      );
      decisions.cardClicked('5', at, 1000);
      refused = ['action'];
      decisions.chooseAction('10');
      expect(decisions.menu()?.cardId).toBe('5');
      expect(answer()).toBeNull();
      refused = [];
      decisions.chooseAction('11');
      expect(decisions.menu()).toBeNull();
      expect(answer()).toBe('11');
      expect(done.at(-1)).toEqual({ kind: 'action', actionId: '11', cardId: '5' });
    });

    it('puts a number, a choice and Escape to it', () => {
      open('INTEGER', param('min', '0'));
      refused = ['number'];
      decisions.answerNumber('2');
      expect(answer()).toBeNull();
      refused = [];
      decisions.answerNumber('2');
      expect(answer()).toBe('2');

      open('MULTIPLE_CHOICE', param('results', 'a', 'b'));
      refused = ['choice'];
      decisions.answerChoice(1);
      expect(answer()).toBeNull();
      refused = [];
      decisions.answerChoice(1);
      expect(answer()).toBe('1');

      settings.setAutoAccept(false);
      open(
        'ARBITRARY_CARDS',
        param('cardId', 'a') + param('selectable', 'true') + param('min', '0') + param('max', '1'),
      );
      refused = ['escape'];
      decisions.escape();
      expect(answer()).toBeNull();
      refused = [];
      decisions.escape();
      expect(answer()).toBe('');
      expect(done.map((gesture) => gesture.kind)).toEqual(['number', 'choice', 'escape']);
    });
  });

  it('does nothing without a waiting decision', async () => {
    decisions.cardClicked('1', at);
    await decisions.press('Done');
    await decisions.press('OK');
    decisions.escape();
    expect(answer()).toBeNull();
  });
});
