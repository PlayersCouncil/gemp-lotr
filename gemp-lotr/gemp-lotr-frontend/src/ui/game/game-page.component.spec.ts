import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { POLL_DELAY_MS } from '../../core/game/session/game-session.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { PlayerInfo } from '../../core/session/player-info.service';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { GamePageComponent } from './game-page.component';
import { mentionOf } from './tabs/game-chat.component';

const GAME = '/gemp-lotr-server/game/7';
const CHAT = '/gemp-lotr-server/chat/Game7';

const state = (events: string, clocks = '') => `<gameState cn="1">${events}${clocks}</gameState>`;
const update = (events: string) => `<update cn="1">${events}</update>`;
const card = (id: string, zone: string, extra = '') =>
  `<ge type="PCIP" cardId="${id}" blueprintId="1_7" participantId="alice" zone="${zone}" ${extra}/>`;
const PLAYERS = '<ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>';
const STATS =
  '<ge type="GS" charStats="1=7|3|RF10,5=9|2|4,6=5|1|R6" fellowshipArchery="null" shadowArchery="2" moveCount="1" ' +
  'moveLimit="2" initiative="FREE_PEOPLE" ruleof4="2" fellowshipStrength="7" shadowStrength="9" ' +
  'fellowshipDamageBonus="0" shadowDamageBonus="1" fpOverwhelmed="true">' +
  '<playerZones name="alice" HAND="1" DISCARD="3" ADVENTURE_DECK="8" DEAD="0" DECK="50" REMOVED="0"/>' +
  '<playerZones name="bob" HAND="8" DISCARD="0" ADVENTURE_DECK="9" DEAD="0" DECK="52" REMOVED="0" handRevealed="true"/>' +
  '<threats name="alice" value="1"/><threats name="bob" value="0"/>' +
  '<threatTotals name="alice" value="3"/><threatTotals name="bob" value="-1"/></ge>';
const TABLE =
  PLAYERS +
  card('1', 'FREE_CHARACTERS') +
  card('2', 'ATTACHED', 'targetCardId="1" targetType="attached"') +
  card('3', 'STACKED', 'targetCardId="1" targetType="stacked"') +
  card('4', 'HAND') +
  card('5', 'SHADOW_CHARACTERS') +
  card('6', 'FREE_CHARACTERS') +
  card('7', 'SHADOW_CHARACTERS') +
  card('8', 'ADVENTURE_PATH', 'index="1"') +
  '<ge type="PP" participantId="alice" index="1"/>' +
  '<ge type="TC" participantId="alice" otherCardIds="5"/>' +
  '<ge type="GPC" phase="Skirmish"/><ge type="TP" count="4"/>' +
  '<ge type="AT" cardId="1" token="WOUND" count="12"/>' +
  '<ge type="M" message="Hello &lt;div class=\'cardHint\' value=\'1_7\'&gt;Gimli&lt;/div&gt; alice"/>' +
  '<ge type="M" message="Plain message"/><ge type="W" message="Careful"/>';
const CLOCKS =
  '<clocks><clock participantId="alice">250</clock><clock participantId="bob">2700</clock>' +
  '<clock participantId="decisionClock">5</clock><clock participantId="decisionLimit">360</clock></clocks>';

describe('GamePageComponent', () => {
  let backend: HttpTestingController;

  beforeEach(() => sessionStorage.clear());

  afterEach(() => backend.match(() => true));

  /** The page asks for news a moment after an answer. */
  const pollDelay = () => new Promise((resolve) => setTimeout(resolve, POLL_DELAY_MS + 10));

  async function open(first: string, query = '?gameId=7') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: query },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    TestBed.inject(PlayerInfo).load().subscribe();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('{"name":"alice"}');
    const fixture = TestBed.createComponent(GamePageComponent);
    const el = fixture.nativeElement as HTMLElement;
    const stable = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
    };
    if (query.includes('gameId')) {
      backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(first);
    }
    await stable();
    const news = async (events: string) => {
      await pollDelay();
      backend.expectOne((r) => r.url === GAME && r.method === 'POST').flush(update(events));
      await stable();
    };
    const cardOf = (id: string) => el.querySelector<HTMLElement>(`[data-card-id="${id}"]`);
    const text = (selector: string) => el.querySelector(selector)?.textContent.replace(/\s+/g, ' ').trim();
    return { fixture, el, stable, news, cardOf, text };
  }

  it('shows the pre-game information and the tab box until the players are known', async () => {
    const { el, text } = await open(
      state(
        '<ge type="PGS" participantId="alice" allParticipantIds="bob,alice" summary="Casual game" notes="My deck" ' +
          'maps="alice:100_1" metaSites="bob:91_1|92_1"/>',
      ),
    );
    expect(el.hasAttribute('data-started')).toBe(false);
    expect(text('.gd-game-pregame__summary')).toContain('Casual gamePlease be courteous');
    expect(text('[data-side="left"] .gd-game-pregame__name')).toBe('alice');
    expect(text('[data-side="right"] .gd-game-pregame__name')).toBe('bob');
    expect(text('[data-side="left"] .gd-game-pregame__content')).toBe('My deck');
    expect(el.querySelectorAll('gd-game-pregame .gd-card')).toHaveLength(2);
    expect(el.querySelector('.gd-game__table')).toBeNull();
    // The timer's switch as the legacy page shows it before the game: a plain button
    expect(el.querySelector('.gd-game-tabs__switch:not(.gd-button)')).not.toBeNull();
    // The hover zoom's switch is there only without a touch screen
    const zoom = el.querySelector('[data-toggle="auto-zoom"]') ? 1 : 0;
    expect(el.querySelector('gd-game-tabs')?.getAttribute('data-extras')).toBe(String(1 + zoom));
  });

  it('opens nothing without a game in the address', async () => {
    const { el } = await open('', '');
    expect(el.querySelector('gd-game-tabs')).not.toBeNull();
    expect(el.querySelector('.gd-game__table')).toBeNull();
  });

  it('lays the cards out on the table, with what a game draws on them', async () => {
    const { el, cardOf, text } = await open(state(TABLE + STATS, CLOCKS));
    expect(el.hasAttribute('data-started')).toBe(true);
    expect(el.getAttribute('data-turn')).toBe('bottom');
    const row = (name: string) =>
      Array.from(
        el.querySelectorAll(`[data-row="${name}"] [data-card-id]`),
        (c) => (c as HTMLElement).dataset['cardId'],
      );
    // The card with what lies under it before it: the stacked card, then the attached one
    expect(row('characters-player')).toEqual(['3', '2', '1', '6']);
    expect(row('shadow')).toEqual(['5', '7']);
    expect(row('hand')).toEqual(['4']);
    expect(cardOf('2')?.dataset['under']).toBe('attached');
    expect(cardOf('3')?.dataset['under']).toBe('stacked');
    expect(cardOf('1')?.dataset['zone']).toBe('FREE_CHARACTERS');
    expect(cardOf('1')?.dataset['blueprintId']).toBe('1_7');
    expect(cardOf('5')?.hasAttribute('data-inactive')).toBe(true);
    // 12 wounds: two columns, every second token a step to the right
    const tokens = cardOf('1')?.querySelectorAll<HTMLElement>('.gd-game-token') ?? [];
    expect(tokens).toHaveLength(12);
    expect(
      cardOf('1')?.querySelector<HTMLElement>('gd-game-card-layers')?.style.getPropertyValue('--gd-token-columns'),
    ).toBe('2');
    expect(tokens[11]?.style.getPropertyValue('--gd-token-column')).toBe('2');
    expect(tokens[11]?.style.getPropertyValue('--gd-token-row')).toBe('1');
    expect(tokens[3]?.style.getPropertyValue('--gd-token-shift')).toBe('2');
    expect(tokens[0]?.getAttribute('src')).toContain('tokens/wound.png');
    // A character's numbers: Frodo's resistance has no icon, a minion shows its site number
    const numbers = (id: string) =>
      Array.from(
        cardOf(id)?.querySelectorAll('.gd-game-number') ?? [],
        (n) => `${(n as HTMLElement).dataset['number']}=${n.textContent.trim()}`,
      );
    expect(numbers('1')).toEqual(['strength=7', 'vitality=3', 'resistance=10']);
    expect(cardOf('1')?.querySelector('[data-number="resistance"]')?.getAttribute('data-owner')).toBe('frodo');
    expect(cardOf('1')?.querySelector('img[data-number="resistance"]')).toBeNull();
    expect(numbers('5')).toEqual(['strength=9', 'vitality=2', 'site=4']);
    expect(numbers('6')).toEqual(['strength=5', 'vitality=1', 'resistance=6']);
    expect(cardOf('6')?.querySelector('img[data-number="resistance"]')).not.toBeNull();
    expect(numbers('7')).toEqual([]);
    // The site, with the player standing on it on their turn
    expect(cardOf('8')?.querySelector('.gd-game-token')?.getAttribute('src')).toContain('tokens/1-a.png');
    expect(el.querySelector<HTMLElement>('gd-game-path')?.style.getPropertyValue('--gd-path-count')).toBe('1');

    expect(text('[data-game="twilight"]')).toBe('4');
    expect(text('[data-game="phase"]')).toBe('Skirmish');
    expect(text('[data-game="initiative"]')).toBe('Initiative: Free Peoples');
    expect(text('[data-game="ruleof4"]')).toBe('Rule of 4: 2 card');
    expect(text('[data-game="fp-archery"]')).toBe('');
    expect(text('[data-game="shadow-archery"]')).toBe('2');
    expect(text('[data-game="move"]')).toBe('1/2');
    expect(text('[data-stat="deck0"]')).toBe('50');
    expect(text('[data-stat="hand1"]')).toBe('8');
    expect(el.querySelector('[data-stat="hand1"]')?.hasAttribute('data-revealed')).toBe(true);
    expect(text('[data-stat="threats0"]')).toBe('1/3');
    expect(text('[data-stat="threats1"]')).toBe('0');
    const clocks = Array.from(el.querySelectorAll('[data-clock]'), (c) => c.textContent.trim());
    expect(clocks).toEqual(['4:10', '45:00', '0:05 / 6:00']);
    expect(el.querySelector('[data-clock][data-own]')?.getAttribute('data-band')).toBe('danger');
    expect(el.querySelector('.gd-game-player[data-current]')?.textContent).toContain('1. alice');
  });

  it('shows assignments and the skirmish in boxes of their own, and follows the game', async () => {
    const { el, news, text } = await open(state(TABLE + STATS));
    expect(el.style.getPropertyValue('--gd-game-fights')).toBe('0');
    await news('<ge type="AA" cardId="6" otherCardIds="7"/><ge type="SS" cardId="1" otherCardIds="5"/>' + STATS);
    expect(el.style.getPropertyValue('--gd-game-fights')).toBe('2');
    const fight = (kind: string, side: string) =>
      Array.from(
        el.querySelectorAll(`[data-fight="${kind}"] [data-side="${side}"] [data-card-id]`),
        (c) => (c as HTMLElement).dataset['cardId'],
      );
    expect(fight('skirmish', 'free-peoples')).toEqual(['3', '2', '1']);
    expect(fight('skirmish', 'shadow')).toEqual(['5']);
    expect(fight('assignment', 'free-peoples')).toEqual(['6']);
    expect(fight('assignment', 'shadow')).toEqual(['7']);
    expect(el.querySelector('[data-card-id="7"]')?.getAttribute('data-assign')).toBe('6');
    expect(text('.gd-game__strength[data-side="free-peoples"]')).toBe('7');
    expect(el.querySelector('.gd-game__strength[data-overwhelmed]')).not.toBeNull();
    expect(el.querySelector('.gd-game__damage[data-side="free-peoples"]')?.hasAttribute('data-none')).toBe(true);
    expect(text('.gd-game__damage[data-side="shadow"]')).toBe('+1');
    // The rule no longer applies: its line is hidden, with the count it last showed
    await news('<ge type="ES"/><ge type="GS" ruleof4="-1" initiative="SHADOW"/>');
    expect(el.querySelector('[data-fight="skirmish"]')).toBeNull();
    expect(el.querySelector<HTMLElement>('[data-game="ruleof4"]')?.hidden).toBe(true);
    expect(text('[data-game="ruleof4"]')).toBe('Rule of 4: 2 card');
    expect(text('[data-game="initiative"]')).toBe('Initiative: Shadow');
    await news('<ge type="GS" ruleof4="4"/>');
    expect(text('[data-game="ruleof4"]')).toBe('Rule of 4: 4 cards.(Rule of 4 limit reached!)');
  });

  it('gives a spectator no hand row and no timer switch, but the options tab', async () => {
    const { el } = await open(
      state('<ge type="P" participantId="carol" allParticipantIds="alice,bob" discardPublic="true"/>' + STATS),
    );
    expect(el.hasAttribute('data-spectator')).toBe(true);
    expect(el.querySelector('[data-row="hand"]')).toBeNull();
    expect(el.querySelector('[data-toggle="timer-mute"]')).toBeNull();
    expect(el.querySelector('[data-tab="options"]')).not.toBeNull();
    expect(el.querySelector('[data-clock][data-own]')).toBeNull();
  });

  it('lists the chat and the game messages in the order they came, marking the ones that name the player', async () => {
    const { el, stable } = await open(state(TABLE));
    // The chat is joined once the game is open
    backend
      .expectOne((r) => r.url === CHAT && r.method === 'GET')
      .flush(
        '<chat><message from="System" date="1">Welcome</message><message from="bob" date="2">hi @alice</message>' +
          '<user>alice</user><user role="admin">bob</user></chat>',
      );
    await stable();
    const lines = Array.from(el.querySelectorAll<HTMLElement>('.gd-game-chat__line'), (line) => [
      line.dataset['kind'],
      line.dataset['mention'] ?? '',
      line.textContent.replace(/\s+/g, ' ').trim(),
    ]);
    expect(lines).toEqual([
      ['message', 'mention', 'Hello Gimli alice'],
      ['message', '', 'Plain message'],
      ['warning', '', 'Careful'],
      ['system', '', 'System: Welcome'],
      ['chat', 'ping', 'bob: hi @alice'],
    ]);
    // A message that names the player is left bare; the others have their text in a box
    expect(el.querySelectorAll('.gd-game-chat__line')[0]?.querySelector('.gd-game-chat__text')).toBeNull();
    expect(el.querySelector('.gd-game-chat__line .cardHint')?.textContent).toBe('Gimli');
    expect(el.querySelector('[data-tab="players"]')?.textContent.trim()).toBe('Players(2)');

    const field = el.querySelector<HTMLInputElement>('[data-chat-input]')!;
    field.value = 'hello';
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    const sent = backend.expectOne((r) => r.url === CHAT && r.method === 'POST');
    expect(sent.request.body?.get('message')).toBe('hello');
    expect(field.value).toBe('');
    // Nothing typed: nothing sent
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    backend.expectNone((r) => r.url === CHAT && r.method === 'POST' && r !== sent.request);

    (el.querySelector('[data-tab="players"]') as HTMLElement).click();
    await stable();
    expect(el.querySelector('[data-panel="players"]')?.textContent.replace(/\s+/g, ' ').trim()).toBe('alice * bob');
  });

  it('switches tabs, changes settings, and asks to concede or cancel', async () => {
    const { el, stable } = await open(state(TABLE));
    const click = async (selector: string) => {
      (el.querySelector(selector) as HTMLElement).click();
      await stable();
    };
    expect(el.querySelector('[data-tab="chat"]')?.getAttribute('aria-selected')).toBe('true');
    await click('[data-tab="settings"]');
    expect(el.querySelector('[data-tab="settings"]')?.getAttribute('aria-selected')).toBe('true');
    expect(el.querySelector<HTMLElement>('[data-panel="chat"]')?.hidden).toBe(true);
    const box = (name: string) => el.querySelector<HTMLInputElement>(`[data-setting="${name}"]`)!;
    expect(box('autoAccept').checked).toBe(true);
    expect(box('autoPassSHADOW').checked).toBe(false);
    for (const name of [
      'autoAccept',
      'autoDismissDeckReveal',
      'use-old-stack-checkbox',
      'alwaysDropDown',
      'autoPassSHADOW',
    ]) {
      box(name).click();
    }
    await stable();
    expect(box('autoAccept').checked).toBe(false);
    expect(box('autoPassSHADOW').checked).toBe(true);
    expect(el.hasAttribute('data-old-stack')).toBe(true);
    const slider = box('animation-slider');
    slider.value = '2';
    slider.dispatchEvent(new Event('input'));
    await stable();
    expect(el.querySelector<HTMLElement>('.gd-game-slider')?.style.getPropertyValue('--gd-slider-share')).toBe('0.75');

    await click('[data-toggle="timer-mute"]');
    expect(el.querySelector('[data-toggle="timer-mute"]')?.getAttribute('data-icon')).toBe('muted');
    if (el.querySelector('[data-toggle="auto-zoom"]')) {
      await click('[data-toggle="auto-zoom"]');
      expect(el.querySelector('[data-toggle="auto-zoom"]')?.getAttribute('data-icon')).toBe('no-zoom');
    }

    await click('[data-tab="options"]');
    await click('[data-button="concedeGame"]');
    backend.expectOne((r) => r.url === `${GAME}/concede`).flush('');
    await click('[data-button="cancelGame"]');
    backend.expectOne((r) => r.url === `${GAME}/cancel`).flush('');
  });

  it("opens a player's other piles, marking the ones the player may look into", async () => {
    const { el, stable, news } = await open(state(TABLE + STATS));
    expect(el.querySelector('[data-stat="discard0"]')).toBeNull();
    (el.querySelector('[data-stats-toggle="0"]') as HTMLElement).click();
    (el.querySelector('[data-stats-toggle="1"]') as HTMLElement).click();
    await stable();
    expect(el.querySelector('.gd-game-player')?.hasAttribute('data-open')).toBe(true);
    const clickable = (stat: string) => el.querySelector(`[data-stat="${stat}"]`)?.hasAttribute('data-clickable');
    expect(el.querySelector('[data-stat="discard0"]')?.textContent.trim()).toBe('3');
    // One's own discard pile and adventure deck, everyone's dead and removed piles
    expect(['discard0', 'adventureDeck0', 'deadPile0', 'removedPile0'].map(clickable)).toEqual([
      true,
      true,
      true,
      true,
    ]);
    expect(['discard1', 'adventureDeck1', 'deadPile1', 'removedPile1'].map(clickable)).toEqual([
      false,
      false,
      true,
      true,
    ]);
    // One's own deck once the game has ended
    expect(clickable('deck0')).toBe(false);
    await news('<ge type="EG"/>');
    expect(clickable('deck0')).toBe(true);
    expect(clickable('deck1')).toBe(false);
    (el.querySelector('[data-stats-toggle="0"]') as HTMLElement).click();
    await stable();
    expect(el.querySelector('[data-stat="discard0"]')).toBeNull();
    expect(el.querySelector('[data-stat="discard1"]')).not.toBeNull();
  });

  it('comes back after a reload with the tab and the piles the player had open', async () => {
    const first = await open(state(TABLE + STATS));
    (first.el.querySelector('[data-tab="settings"]') as HTMLElement).click();
    (first.el.querySelector('[data-stats-toggle="1"]') as HTMLElement).click();
    await first.stable();
    first.fixture.destroy();
    backend.match(() => true);
    TestBed.resetTestingModule();

    const { el } = await open(state(TABLE + STATS));
    expect(el.querySelector('[data-tab="settings"]')?.getAttribute('aria-selected')).toBe('true');
    expect(el.querySelector('[data-stat="discard1"]')).not.toBeNull();
    expect(el.querySelector('[data-stat="discard0"]')).toBeNull();
  });

  it("shows the server's thanks at the end of the game in a dialog too, until it is closed", async () => {
    const { el, news, stable } = await open(state(TABLE));
    expect(el.querySelector('dialog[data-dialog="feedback"][open]')).toBeNull();
    await news(
      '<ge type="M" message="Thank you for playing! &lt;a href=\'x\'&gt;using this form.&lt;/a&gt;"/><ge type="EG"/>',
    );
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="feedback"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('Give us feedback!');
    expect(dialog.querySelector('a')?.textContent).toBe('using this form.');
    (dialog.querySelector('.gd-dialog__close') as HTMLElement).click();
    await stable();
    expect(dialog.open).toBe(false);
  });

  it("writes a game problem without a dialog and the chat's problem in the chat box, as warnings", async () => {
    const { el, stable } = await open(state(TABLE));
    backend.expectOne((r) => r.url === CHAT).flush('', { status: 404, statusText: 'x' });
    await pollDelay();
    backend.expectOne((r) => r.url === GAME && r.method === 'POST').flush('', { status: 500, statusText: 'x' });
    await stable();
    const warnings = Array.from(el.querySelectorAll('.gd-game-chat__line[data-kind="warning"]'), (w) =>
      w.textContent.trim(),
    );
    expect(warnings[1]).toBe('Chat room is closed.');
    expect(warnings[2]).toContain('There was a problem communicating with the server (500)');
    expect(warnings).toHaveLength(4);
  });
});

describe('mentionOf', () => {
  it('tells a call ("@name") from a mention (the name, or everyone)', () => {
    expect(mentionOf('hi @alice!', 'alice')).toBe('ping');
    expect(mentionOf('alice plays', 'alice')).toBe('mention');
    expect(mentionOf('for @everyone', 'alice')).toBe('mention');
    expect(mentionOf('for @anyone', null)).toBe('mention');
    expect(mentionOf('alicia plays', 'alice')).toBeNull();
    expect(mentionOf('alice plays', null)).toBeNull();
    // The speaker's own name in bold in front of a message isn't a mention
    expect(mentionOf('<b>alice: </b>', 'alice')).toBeNull();
    // A name with characters that mean something in a pattern is taken as it is
    expect(mentionOf('a.b plays', 'a.b')).toBe('mention');
    expect(mentionOf('axb plays', 'a.b')).toBeNull();
  });
});
