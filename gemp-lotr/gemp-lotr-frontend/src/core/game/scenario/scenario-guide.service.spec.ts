import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { CardInfoService } from '../../cards/card-info.service';
import { CardView } from '../../cards/card-view';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { GameDecisions } from '../decision/game-decisions.service';
import { provideGameState } from '../game-state';
import { readingTime } from '../session/event-pacing';
import { GameLog } from '../session/game-log';
import { GameSession, POLL_DELAY_MS } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import {
  ACKNOWLEDGE_RETRY_MS,
  HINT_MS,
  ScenarioGuide,
  STOCK_NOT_NOW,
  STOCK_OFF_SCRIPT,
} from './scenario-guide.service';

const GAME = '/gemp-lotr-server/game/7';
const ACK = `${GAME}/scenarioAck`;
const START = '/gemp-lotr-server/scenarios/start';

/** The first tutorial's info, as the server sends it. */
const INFO = {
  type: 'info',
  id: 'basics-01',
  version: 1,
  kind: 'tutorial',
  title: 'The Basics: Companions and Skirmishes',
  coverage: 'strict',
  human: 'asdf',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Basic Shadow',
  autoAccept: true,
  autoDismissDeckReveal: true,
  animationSpeed: null,
  bubbleGap: null,
  resumedAt: null,
  campaign: {
    id: 'basics',
    title: 'Learn to Play',
    position: 1,
    count: 2,
    next: { id: 'basics-02-items-damage', title: 'The Basics: Items and Damage' },
  },
  author: false,
  draftTest: false,
};

const message = (header: string, extra: Record<string, unknown> = {}) => ({
  header,
  html: `<p>${header}</p>`,
  anchor: null,
  anchors: [],
  gap: null,
  highlight: [],
  highlightStyle: 'gold',
  avoid: [],
  side: 'auto',
  advance: 'next',
  advanceCard: null,
  choices: [],
  on: null,
  zoom: null,
  ...extra,
});

const step = (extra: Record<string, unknown> = {}) => ({
  type: 'step',
  step: 1,
  sub: 0,
  name: null,
  seat: 'human',
  acknowledge: false,
  pause: false,
  checkpoint: false,
  messages: [],
  parts: {},
  allow: null,
  cards: { frodo: 7, sam: 8, orc: 20, troll: 21 },
  ...extra,
});
const allow = (answers: string[], extra: Record<string, unknown> = {}) => ({
  answers,
  pass: false,
  otherwise: null,
  hints: {},
  ...extra,
});

const xmlJson = (value: unknown) =>
  JSON.stringify(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
/** A scenario event as the server sends it. */
const S = (event: unknown) => `<ge type="S" scenario="${xmlJson(event)}"/>`;

const param = (name: string, ...values: string[]) =>
  values.map((value) => `<parameter name="${name}" value="${value}"/>`).join('');
const D = (type: string, params: string) =>
  `<ge type="D" id="3" decisionType="${type}" participantId="asdf" text="Do it">${params}</ge>`;
const TABLE =
  '<ge type="P" participantId="asdf" allParticipantIds="asdf,~Scenario" discardPublic="false"/>' +
  '<ge type="PCIP" cardId="7" blueprintId="1_290" participantId="asdf" zone="FREE_CHARACTERS"/>' +
  '<ge type="PCIP" cardId="8" blueprintId="1_311" participantId="asdf" zone="FREE_CHARACTERS"/>' +
  '<ge type="PCIP" cardId="20" blueprintId="1_191" participantId="~Scenario" zone="SHADOW_CHARACTERS"/>' +
  '<ge type="PCIP" cardId="21" blueprintId="1_165" participantId="~Scenario" zone="SHADOW_CHARACTERS"/>';
/** Frodo (7) and Sam (8) each have an action; Sam's card is offered from off the table. */
const CARD_ACTIONS = D(
  'CARD_ACTION_CHOICE',
  param('cardId', '7', '8') +
    param('blueprintId', 'inPlay', '1_311') +
    param('actionId', '12', '13') +
    param('actionText', 'Use', 'Play'),
);
const CARD_SELECTION = D('CARD_SELECTION', param('cardId', '7', '8') + param('min', '1') + param('max', '2'));
const ASSIGNMENT = D('ASSIGN_MINIONS', param('freeCharacters', '7', '8') + param('minions', '20', '21'));

const at = { x: 1, y: 2 };

describe('ScenarioGuide', () => {
  let guide: ScenarioGuide;
  let session: GameSession;
  let decisions: GameDecisions;
  let settings: GameSettings;
  let backend: HttpTestingController;
  let notifier: FakeNotifier;
  let cardInfo: CardInfoService;
  let log: GameLog;
  let cues: { go: ReturnType<typeof vi.fn> };
  let hall: { go: ReturnType<typeof vi.fn>; openHere: ReturnType<typeof vi.fn>; elsewhere: ReturnType<typeof signal> };
  let now = 1000;

  function setUp(query = '') {
    cues = { go: vi.fn() };
    hall = { go: vi.fn(), openHere: vi.fn(), elsewhere: signal<string | null>(null) };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: query },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: cues },
        { provide: HallTab, useValue: hall },
      ],
    });
    guide = TestBed.inject(ScenarioGuide);
    session = TestBed.inject(GameSession);
    decisions = TestBed.inject(GameDecisions);
    settings = TestBed.inject(GameSettings);
    backend = TestBed.inject(HttpTestingController);
    notifier = TestBed.inject(FakeNotifier);
    cardInfo = TestBed.inject(CardInfoService);
    log = TestBed.inject(GameLog);
  }

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    now = 1000;
    setUp();
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  /** Opens game 7 with these events (the players and four cards first). */
  function open(events: string) {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(`<gameState cn="1">${TABLE}${events}</gameState>`);
    TestBed.tick();
  }

  /** The answer sent to the decision, or null if none. */
  function answer(): string | null {
    const sent = backend.match((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'));
    return sent.length ? (sent[0]!.request.body?.get('decisionValue') ?? null) : null;
  }

  /** The "I have read it" sent, or null. */
  const acknowledged = () => {
    const sent = backend.match((r) => r.url === ACK);
    expect(sent.length).toBeLessThanOrEqual(1);
    return sent[0] ?? null;
  };

  /** The poll that asks for news, answered with these events. */
  function news(events: string) {
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST' && !r.body?.has('decisionId'))
      .flush(`<update cn="2">${events}</update>`);
    TestBed.tick();
  }

  const click = (cardId: string) => {
    now += 1000;
    decisions.cardClicked(cardId, at, now);
    TestBed.tick();
  };
  const header = () => guide.message()?.header ?? null;

  it('stays out of an ordinary game: nothing up, every gesture allowed, the settings the player chose', () => {
    settings.setAutoAccept(false);
    open(CARD_ACTIONS);
    expect(guide.info()).toBeNull();
    expect(guide.playing()).toBe(false);
    expect(guide.phase()).toBe('idle');
    expect(guide.message()).toBeNull();
    expect(guide.nextLabel()).toBeNull();
    expect(guide.cardMarks()).toEqual({});
    expect(guide.nextClicks()).toEqual({ cardIds: [], button: null, options: [] });
    expect(guide.allows({ kind: 'card', cardId: '8' })).toBe(true);
    expect(settings.fixed()).toEqual({});
    expect(settings.autoAccept()).toBe(false);
  });

  it("fixes the scenario's settings while it is played, over the player's own", () => {
    settings.setAutoAccept(false);
    open(S(INFO) + CARD_ACTIONS);
    expect(guide.info()?.title).toBe('The Basics: Companions and Skirmishes');
    expect(guide.playing()).toBe(true);
    expect(settings.fixed()).toEqual({ autoAccept: true, autoDismissDeckReveal: true, animationSpeed: null });
    expect(settings.autoAccept()).toBe(true);
    expect(settings.autoDismissDeckReveal()).toBe(true);
    // Any click goes through while nothing of the script is up
    expect(guide.phase()).toBe('idle');
    click('extra8');
    expect(answer()).toBe('13');
  });

  it('holds the decision while the opening messages are read, then takes only what the step allows', () => {
    open(
      S(INFO) +
        S(
          step({
            messages: [message('Welcome'), message('Frodo')],
            allow: allow(['12'], { hints: { sam: '<p>Not Sam.</p>' } }),
          }),
        ) +
        CARD_ACTIONS,
    );
    expect(guide.phase()).toBe('reading');
    expect(header()).toBe('Welcome');
    expect(guide.count()).toEqual({ at: 1, of: 2 });
    expect(guide.nextLabel()).toBe('Next');
    // The cards stand as the step will take them, though nothing may be clicked yet
    expect(guide.cardMarks()).toEqual({ '7': 'live', extra8: 'notnow' });
    expect(guide.nextClicks().cardIds).toEqual([]);
    click('7');
    expect(answer()).toBeNull();
    expect(guide.nudges()).toBe(1);

    guide.next();
    expect(header()).toBe('Frodo');
    expect(guide.count()).toEqual({ at: 2, of: 2 });
    guide.next();
    expect(guide.phase()).toBe('answering');
    expect(guide.message()).toBeNull();
    expect(guide.count()).toBeNull();
    // (nothing more to read)
    guide.next();
    expect(guide.phase()).toBe('answering');
    expect(guide.nextClicks()).toEqual({ cardIds: ['7'], button: null, options: [] });

    // A card the step doesn't take: its own hint, at the card clicked, for a while
    click('extra8');
    expect(answer()).toBeNull();
    expect(guide.hint()).toEqual({ html: '<p>Not Sam.</p>', cardId: 'extra8', button: null, option: null });
    vi.advanceTimersByTime(HINT_MS);
    expect(guide.hint()).toBeNull();

    click('7');
    expect(answer()).toBe('12');
    // Answered: the step has had its say
    expect(guide.step()).toBeNull();
    expect(guide.phase()).toBe('idle');
  });

  it('lets the decision be answered under an opening message that goes when it is answered', () => {
    open(
      S(INFO) +
        S(step({ messages: [message('Play Frodo', { advance: 'decision' })], allow: allow(['12']) })) +
        CARD_ACTIONS,
    );
    expect(guide.phase()).toBe('answering');
    expect(header()).toBe('Play Frodo');
    expect(guide.nextLabel()).toBeNull();
    expect(guide.count()).toBeNull();
    click('7');
    expect(answer()).toBe('12');
    expect(guide.message()).toBeNull();
  });

  it('says of any other click what the step says, or the stock line; the latest hint stays its full time', async () => {
    open(S(INFO) + S(step({ allow: allow(['12'], { otherwise: '<p>Frodo first.</p>' }) })) + CARD_ACTIONS);
    click('extra8');
    expect(guide.hint()?.html).toBe('<p>Frodo first.</p>');
    await decisions.press('Pass');
    expect(guide.hint()).toEqual({ html: '<p>Frodo first.</p>', cardId: null, button: 'Pass', option: null });
    vi.advanceTimersByTime(HINT_MS - 1000);
    click('extra8');
    vi.advanceTimersByTime(1000);
    // The first hint's time is up, the second's isn't
    expect(guide.hint()?.cardId).toBe('extra8');
    guide.dismissHint();
    expect(guide.hint()).toBeNull();
    expect(answer()).toBeNull();
  });

  it('says the stock line of a choice the step does not take, and takes the one it does', () => {
    open(S(INFO) + S(step({ allow: allow(['1']) })) + D('MULTIPLE_CHOICE', param('results', 'Yes', 'No')));
    expect(guide.nextClicks()).toEqual({ cardIds: [], button: null, options: [1] });
    decisions.answerChoice(0);
    expect(guide.hint()).toEqual({ html: STOCK_NOT_NOW, cardId: null, button: null, option: 0 });
    expect(answer()).toBeNull();
    decisions.answerChoice(1);
    expect(answer()).toBe('1');
  });

  it('takes only the allowed number', () => {
    open(S(INFO) + S(step({ allow: allow(['3']) })) + D('INTEGER', param('min', '0') + param('max', '5')));
    decisions.answerNumber('2');
    expect(answer()).toBeNull();
    expect(guide.hint()?.html).toBe(STOCK_NOT_NOW);
    decisions.answerNumber('3');
    expect(answer()).toBe('3');
  });

  it('points at Pass when passing is all the step takes, and passes', async () => {
    open(S(INFO) + S(step({ allow: allow([], { pass: true }) })) + CARD_ACTIONS);
    expect(guide.nextClicks()).toEqual({ cardIds: [], button: 'Pass', options: [] });
    expect(guide.cardMarks()).toEqual({ '7': 'notnow', extra8: 'notnow' });
    await decisions.press('Pass');
    expect(answer()).toBe('');
  });

  it('takes anything for a step that allows anything', () => {
    open(S(INFO) + S(step({ messages: [message('Anything', { advance: 'decision' })] })) + CARD_ACTIONS);
    expect(guide.plan()).toBeNull();
    expect(guide.cardMarks()).toEqual({});
    expect(guide.nextClicks().cardIds).toEqual([]);
    click('extra8');
    expect(answer()).toBe('13');
  });

  it('puts up the message of a moment: a card picked, the choice ready; read, or taken back, it goes', async () => {
    open(
      S({ ...INFO, autoAccept: false }) +
        S(
          step({
            messages: [
              message('Pick both', { advance: 'decision' }),
              message('Frodo picked', { on: { moment: 'picked', card: 'frodo' } }),
              message('Ready', { on: { moment: 'ready', card: null } }),
            ],
            allow: allow(['7,8']),
          }),
        ) +
        CARD_SELECTION,
    );
    expect(header()).toBe('Pick both');
    expect(guide.count()).toBeNull();
    // Done isn't taken until both are chosen
    await decisions.press('Done');
    expect(guide.hint()?.button).toBe('Done');
    click('7');
    expect(header()).toBe('Frodo picked');
    expect(guide.nextLabel()).toBe('Next');
    guide.next();
    expect(header()).toBe('Pick both');
    click('8');
    expect(header()).toBe('Ready');
    expect(guide.nextClicks()).toEqual({ cardIds: [], button: 'Done', options: [] });
    // Taken back: the moment's message goes with it
    await decisions.press('Reset choice');
    TestBed.tick();
    expect(header()).toBe('Pick both');
    expect(session.model().local.selectedCardIds).toEqual([]);
    click('8');
    expect(header()).toBe('Pick both');
    // Picked and ready at once: the later moment has the bubble
    click('7');
    expect(header()).toBe('Ready');
    await decisions.press('Done');
    expect(answer()).toBe('8,7');
  });

  it('puts up the message of a minion picked up and of a character given every minion it is to have', async () => {
    open(
      S(INFO) +
        S(
          step({
            messages: [
              message('Assign', { advance: 'decision' }),
              message('The orc', { on: { moment: 'picked', card: 'orc' } }),
              message('Frodo has his', { on: { moment: 'assigned', card: 'frodo' } }),
            ],
            allow: allow(['7 20,8 21']),
          }),
        ) +
        ASSIGNMENT,
    );
    expect(guide.nextClicks().cardIds).toEqual(['20']);
    click('21');
    expect(guide.hint()?.html).toBe(STOCK_NOT_NOW);
    click('20');
    expect(header()).toBe('The orc');
    expect(guide.nextClicks().cardIds).toEqual(['7']);
    click('7');
    expect(header()).toBe('Frodo has his');
    click('21');
    click('8');
    // Nothing to say of the troll or of Sam: Frodo's message stays until read
    expect(header()).toBe('Frodo has his');
    expect(guide.nextClicks().button).toBe('Done');
    await decisions.press('Done');
    await settle();
    expect(notifier.dialogs).toEqual([]);
    expect(answer()).toBe('7 20,8 21');
  });

  it("reads the opponent's step and tells the server, once, that it has been read", () => {
    open(S(INFO) + S(step({ seat: 'opponent', messages: [message('Shadow'), message('Orcs')] })));
    expect(guide.phase()).toBe('waiting');
    expect(guide.nextLabel()).toBe('Next');
    expect(guide.allows({ kind: 'escape' })).toBe(false);
    expect(guide.nudges()).toBe(1);
    guide.next();
    expect(header()).toBe('Orcs');
    expect(guide.nextLabel()).toBe('Continue');
    guide.next();
    guide.next();
    const read = acknowledged();
    expect(read?.request.body?.get('step')).toBe('1');
    expect(read?.request.body?.get('choice')).toBe('');
    read?.flush('');
    TestBed.tick();
    expect(guide.step()).toBeNull();
    expect(guide.phase()).toBe('idle');
    // (the page was asking for news already: no second question)
    vi.advanceTimersByTime(POLL_DELAY_MS);
    expect(backend.match((r) => r.url === GAME && r.method === 'POST')).toHaveLength(1);
  });

  it('tells the server which button a step ended in', () => {
    open(
      S(INFO) +
        S(
          step({
            seat: 'opponent',
            messages: [message('Ready?', { choices: [{ name: 'yes', label: 'Yes' }, { name: 'no' }] })],
          }),
        ),
    );
    expect(guide.message()?.choices).toEqual([
      { name: 'yes', label: 'Yes' },
      { name: 'no', label: 'no' },
    ]);
    expect(guide.nextLabel()).toBeNull();
    guide.choose('yes');
    expect(acknowledged()?.request.body?.get('choice')).toBe('yes');
  });

  it('offers to try again when telling the server failed, and stops offering once it worked', () => {
    open(S(INFO) + S(step({ seat: 'opponent', messages: [message('Shadow')] })));
    guide.next();
    acknowledged()?.flush('', { status: 500, statusText: 'x' });
    TestBed.tick();
    expect(guide.acknowledgeFailed()).toBe(true);
    expect(guide.nextLabel()).toBe('Try again');
    expect(guide.nudges()).toBe(1);
    expect(header()).toBe('Shadow');
    guide.next();
    acknowledged()?.flush('');
    TestBed.tick();
    expect(guide.acknowledgeFailed()).toBe(false);
    expect(guide.step()).toBeNull();
  });

  it('tells the server at once of a step with nothing to read, and again a moment later if that failed', () => {
    open(S(INFO) + S(step({ seat: 'opponent' })));
    acknowledged()?.flush('', { status: 503, statusText: 'x' });
    TestBed.tick();
    // Nothing on the screen to press again
    expect(guide.acknowledgeFailed()).toBe(false);
    expect(guide.message()).toBeNull();
    vi.advanceTimersByTime(ACKNOWLEDGE_RETRY_MS - 1);
    expect(acknowledged()).toBeNull();
    vi.advanceTimersByTime(1);
    acknowledged()?.flush('');
    TestBed.tick();
    expect(guide.step()).toBeNull();
  });

  it('forgets telling the server about a step another has replaced', () => {
    open(S(INFO) + S(step({ seat: 'opponent' })));
    const first = acknowledged();
    vi.advanceTimersByTime(POLL_DELAY_MS);
    news(S(step({ step: 2, messages: [message('Yours')], allow: allow(['12']) })) + CARD_ACTIONS);
    first?.flush('', { status: 500, statusText: 'x' });
    TestBed.tick();
    vi.advanceTimersByTime(ACKNOWLEDGE_RETRY_MS);
    expect(acknowledged()).toBeNull();
    expect(guide.acknowledgeFailed()).toBe(false);
    expect(guide.step()?.step).toBe(2);
  });

  it('keeps a step that has been replaced while the server was told the last one had been read', () => {
    open(S(INFO) + S(step({ seat: 'opponent' })));
    const first = acknowledged();
    vi.advanceTimersByTime(POLL_DELAY_MS);
    news(S(step({ step: 2, messages: [message('Yours')], allow: allow(['12']) })) + CARD_ACTIONS);
    first?.flush('');
    TestBed.tick();
    expect(guide.step()?.step).toBe(2);
  });

  it("reads a step put before the player's own decision, then asks once for the news that follows", () => {
    open(S(INFO) + S(step({ pause: true, messages: [message('Before you play')] })) + CARD_ACTIONS);
    expect(guide.phase()).toBe('waiting');
    expect(guide.nextLabel()).toBe('Continue');
    click('7');
    expect(answer()).toBeNull();
    expect(guide.nudges()).toBe(1);
    guide.next();
    acknowledged()?.flush('');
    TestBed.tick();
    expect(guide.step()).toBeNull();
    // No news was being asked for over the waiting decision: asked for once
    news('');
    vi.advanceTimersByTime(POLL_DELAY_MS * 10);
    expect(backend.match((r) => r.url === GAME && r.method === 'POST')).toEqual([]);
    click('7');
    expect(answer()).toBe('12');
  });

  it('says once that the game has left its script, in its own words or the stock ones', () => {
    open(S(INFO) + S({ type: 'offscript', message: null }) + CARD_ACTIONS);
    expect(guide.phase()).toBe('notice');
    expect(guide.message()?.html).toBe(STOCK_OFF_SCRIPT);
    expect(guide.nextLabel()).toBe('OK');
    // An ordinary game from here on
    expect(guide.allows({ kind: 'card', cardId: '7' })).toBe(true);
    guide.next();
    expect(guide.phase()).toBe('idle');
    expect(guide.message()).toBeNull();

    TestBed.resetTestingModule();
    sessionStorage.clear();
    setUp();
    open(S(INFO) + S({ type: 'offscript', message: '<p>Off you go.</p>' }) + CARD_ACTIONS);
    expect(guide.message()?.html).toBe('<p>Off you go.</p>');
  });

  it('takes nothing once the scenario has ended', () => {
    open(
      S(INFO) + CARD_ACTIONS + S({ type: 'end', result: 'complete', messages: [], message: null, checkpoint: null }),
    );
    expect(guide.phase()).toBe('ended');
    expect(guide.end()?.result).toBe('complete');
    expect(guide.message()).toBeNull();
    click('7');
    expect(answer()).toBeNull();
    expect(guide.nudges()).toBe(1);
  });

  it("puts each message into the game log under the opponent's shown name, once however often it is shown", () => {
    open(
      S(INFO) +
        S(step({ seat: 'opponent', messages: [message('One'), message('Two'), message('Blank', { html: '' })] })),
    );
    guide.next();
    TestBed.tick();
    guide.next();
    TestBed.tick();
    const said = () =>
      log
        .entries()
        .flatMap((entry) =>
          entry.kind === 'chat' && entry.said ? [`${entry.said} ${entry.message.from}: ${entry.message.html}`] : [],
        );
    expect(said()).toEqual(['scenario 1.0:0 Basic Shadow: <p>One</p>', 'scenario 1.0:1 Basic Shadow: <p>Two</p>']);
    // The same step put up again (its author rewrote it): from its first message, the same lines
    vi.advanceTimersByTime(POLL_DELAY_MS);
    news(S(step({ seat: 'opponent', messages: [message('One', { html: '<p>One, again</p>' })] })));
    expect(header()).toBe('One');
    expect(said()).toEqual([
      'scenario 1.0:0 Basic Shadow: <p>One, again</p>',
      'scenario 1.0:1 Basic Shadow: <p>Two</p>',
    ]);
  });

  it('logs the notice too, and a step before the scenario said who it is under a stock name', () => {
    open(S(step({ messages: [message('Early')] })));
    expect(guide.playing()).toBe(false);
    expect(log.entries().at(-1)).toMatchObject({ kind: 'chat', said: 'scenario 1.0:0', message: { from: 'Scenario' } });
    vi.advanceTimersByTime(POLL_DELAY_MS);
    news(S({ type: 'offscript', message: null }));
    expect(log.entries().at(-1)).toMatchObject({ said: 'scenario notice', message: { html: STOCK_OFF_SCRIPT } });
  });

  describe('card information', () => {
    const shown = (cardId: string | null) => ({ ref: cardId === null ? null : { cardId } }) as unknown as CardView;

    it('goes on when the card the message names has its information opened, and when it is closed again', () => {
      open(
        S(INFO) +
          S(
            step({
              messages: [
                message('Look at Frodo', { advance: 'inspect', advanceCard: 'frodo' }),
                message('Close it', { advance: 'close' }),
                message('Look at anything', { advance: 'inspect' }),
                message('Done'),
              ],
            }),
          ) +
          CARD_ACTIONS,
      );
      expect(guide.nextLabel()).toBeNull();
      cardInfo.show(shown('8'));
      TestBed.tick();
      expect(header()).toBe('Look at Frodo');
      cardInfo.close();
      TestBed.tick();
      // Frodo, drawn under a stand-in id
      cardInfo.show(shown('extra7'));
      TestBed.tick();
      expect(header()).toBe('Close it');
      cardInfo.close();
      TestBed.tick();
      expect(header()).toBe('Look at anything');
      cardInfo.show(shown(null));
      TestBed.tick();
      expect(header()).toBe('Done');
      // Nothing waits for it now
      cardInfo.close();
      TestBed.tick();
      expect(header()).toBe('Done');
    });

    it('does not go on for a card without an id; one that waits for it and failed to be told offers to try again', () => {
      open(
        S(INFO) +
          S(
            step({
              seat: 'opponent',
              messages: [message('Look at Frodo', { advance: 'inspect', advanceCard: 'frodo' })],
            }),
          ),
      );
      cardInfo.show(shown(null));
      TestBed.tick();
      expect(acknowledged()).toBeNull();
      cardInfo.close();
      TestBed.tick();
      cardInfo.show(shown('7'));
      TestBed.tick();
      acknowledged()?.flush('', { status: 500, statusText: 'x' });
      TestBed.tick();
      expect(guide.acknowledgeFailed()).toBe(true);
      // Opening the card again isn't trying again; the button is
      cardInfo.close();
      TestBed.tick();
      cardInfo.show(shown('7'));
      TestBed.tick();
      expect(acknowledged()).toBeNull();
      expect(guide.nextLabel()).toBe('Try again');
      guide.next();
      acknowledged()?.flush('');
      TestBed.tick();
      expect(guide.step()).toBeNull();
    });
  });

  describe('starting again, the next scenario, leaving', () => {
    const start = () => backend.expectOne((r) => r.url === START && r.method === 'POST');

    it('starts the scenario again and goes to its new game; once, however often asked', () => {
      open(S(INFO) + CARD_ACTIONS);
      guide.restart();
      guide.restart();
      expect(guide.leaving()).toBe(true);
      const asked = start();
      expect(asked.request.body?.get('id')).toBe('basics-01');
      expect(asked.request.body?.has('from')).toBe(false);
      asked.flush('{"gameId":42}');
      expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=42');
    });

    it('starts from the last checkpoint, or the next of the campaign, for an admin acting as the player', () => {
      TestBed.resetTestingModule();
      setUp('?participantId=bob');
      open(S(INFO) + CARD_ACTIONS);
      guide.restart(true);
      const checkpoint = start();
      expect(checkpoint.request.body?.get('from')).toBe('checkpoint');
      checkpoint.flush('{"gameId":43}');
      expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=43&participantId=bob');
    });

    it('starts the next scenario of the campaign, if there is one', () => {
      open(S(INFO) + CARD_ACTIONS);
      guide.startNext();
      expect(start().request.body?.get('id')).toBe('basics-02-items-damage');
      TestBed.resetTestingModule();
      setUp();
      open(S({ ...INFO, campaign: null }) + CARD_ACTIONS);
      guide.startNext();
      expect(backend.match((r) => r.url === START)).toEqual([]);
    });

    it('says so when the scenario could not be started, and can be asked again', () => {
      open(S(INFO) + CARD_ACTIONS);
      guide.restart();
      start().flush('', { status: 500, statusText: 'x' });
      expect(guide.leaving()).toBe(false);
      expect(notifier.dialogs).toEqual([
        'alert: The scenario could not be started. Go back to the Game Hall and try from there.',
      ]);
      guide.restart();
      start();
    });

    it('starts nothing in an ordinary game', () => {
      open(CARD_ACTIONS);
      guide.restart();
      guide.startNext();
      expect(backend.match((r) => r.url === START)).toEqual([]);
    });

    it('asks before starting again in the middle of a game, and not once it is over', async () => {
      open(S(INFO) + CARD_ACTIONS);
      notifier.answers.push(false);
      await guide.askRestart();
      expect(notifier.dialogs).toEqual(['confirm: Start this scenario again from the beginning?']);
      expect(backend.match((r) => r.url === START)).toEqual([]);
      notifier.answers.push(true);
      await guide.askRestart();
      start();

      TestBed.resetTestingModule();
      setUp();
      open(S(INFO) + '<ge type="EG"/>');
      await guide.askRestart();
      expect(notifier.dialogs).toEqual([]);
      start();
    });

    it('asks before leaving in the middle of a game, then concedes and goes back to the hall a moment later', async () => {
      open(S(INFO) + CARD_ACTIONS);
      notifier.answers.push(false);
      await guide.leave();
      expect(notifier.dialogs).toEqual(['confirm: Leave this scenario? You can start it again from the Game Hall.']);
      expect(guide.leaving()).toBe(false);
      notifier.answers.push(true);
      await guide.leave();
      expect(guide.leaving()).toBe(true);
      backend.expectOne((r) => r.url === `${GAME}/concede`).flush('');
      expect(hall.go).not.toHaveBeenCalled();
      vi.advanceTimersByTime(300);
      expect(hall.go).toHaveBeenCalledWith('');
    });

    it('goes straight back to the hall from a scenario that is over, to a place in it if asked', async () => {
      open(S(INFO) + S({ type: 'end', result: 'failed', checkpoint: 2 }));
      await guide.leave();
      expect(notifier.dialogs).toEqual([]);
      expect(hall.go).toHaveBeenCalledWith('');
      guide.backToHall('#account/scenarios');
      expect(hall.go).toHaveBeenLastCalledWith('#account/scenarios');
      // Starting again from the end panel isn't asked about either
      await guide.askRestart();
      start();
    });
  });

  it('shows each opening message of a replay for as long as it takes to read, and fixes no settings there', () => {
    const messages = [message('First words here'), message('Then these')];
    session.openReplay('r');
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/r')
      .flush(`<gameReplay>${TABLE}${S(INFO)}${S(step({ messages }))}<ge type="TP" count="1"/></gameReplay>`);
    session.togglePlay();
    TestBed.tick();
    for (let i = 0; i < 1000 && !guide.step(); i++) {
      vi.advanceTimersByTime(50);
      TestBed.tick();
    }
    expect(guide.playing()).toBe(false);
    expect(settings.fixed()).toEqual({});
    expect(header()).toBe('First words here');
    // Nothing to press in a replay: it goes by itself
    expect(guide.nextLabel()).toBeNull();
    expect(guide.allows({ kind: 'card', cardId: '7' })).toBe(true);
    vi.advanceTimersByTime(readingTime('<p>First words here</p>') * session.speed());
    TestBed.tick();
    expect(header()).toBe('Then these');
    expect(acknowledged()).toBeNull();
  });
});
