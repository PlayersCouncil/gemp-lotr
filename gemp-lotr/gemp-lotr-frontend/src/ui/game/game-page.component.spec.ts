import { GameSettings } from '../../core/game/settings/game-settings';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { POLL_DELAY_MS } from '../../core/game/session/game-session.service';
import { PageCues } from '../../core/platform/page-cues';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { GAME_THEME_PICKER, ThemeService } from '../../core/platform/theme.service';
import { By } from '@angular/platform-browser';
import { AutoZoomComponent } from '../shared/auto-zoom/auto-zoom.component';
import { PlayerInfo } from '../../core/session/player-info.service';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { PUZZLE_SLOTS } from '../../testing/table-edit';
import { GamePageComponent } from './game-page.component';
import { mentionOf } from './tabs/game-chat.component';
import { sentForm } from '../../testing/sent-form';

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
  'fellowshipDamageBonus="0" shadowDamageBonus="1" fpOverwhelmed="true" shadowOverwhelmed="true">' +
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

  async function open(first: string, query = '?gameId=7', themePicker = true, theme = 'classic') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: query },
        { provide: GAME_THEME_PICKER, useValue: themePicker },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    // The legacy page's look and behaviour (the page's default is modern: tests of it say so)
    TestBed.inject(ThemeService).select(theme);
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

  it("names a scripted game's opponent on the pre-game information as its scenario does", async () => {
    const info = JSON.stringify({ type: 'info', id: 'opening', opponent: '~Scenario', opponentName: 'Tutorial' });
    const { text } = await open(
      state(
        '<ge type="PGS" participantId="alice" allParticipantIds="~Scenario,alice" summary="Scenario"/>' +
          `<ge type="S" scenario="${info.replace(/"/g, '&quot;')}"/>`,
      ),
    );
    expect(text('[data-side="left"] .gd-game-pregame__name')).toBe('alice');
    expect(text('[data-side="right"] .gd-game-pregame__name')).toBe('Tutorial');
  });

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

  it('shows the red theme button in the table, unless GAME_THEME_PICKER is off', async () => {
    const shown = await open(state(TABLE));
    expect(shown.el.querySelector('.gd-game__table .gd-game__theme .gd-game-theme')).not.toBeNull();
    TestBed.resetTestingModule();
    const hidden = await open(state(TABLE), '?gameId=7', false);
    expect(hidden.el.querySelector('.gd-game-theme')).toBeNull();
  });

  it('lets Shift flip the zoomed card in the modern view only, as the legacy game page never did', async () => {
    const { fixture, stable } = await open(state(TABLE));
    const zoom = () =>
      fixture.debugElement.query(By.directive(AutoZoomComponent)).componentInstance as AutoZoomComponent;
    const themes = TestBed.inject(ThemeService);
    themes.select('classic');
    await stable();
    expect(zoom().flipOnShift()).toBe(false);
    themes.select('modern');
    await stable();
    expect(zoom().flipOnShift()).toBe(true);
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
    expect(el.querySelector('.gd-game__strength[data-side="free-peoples"]')?.hasAttribute('data-overwhelmed')).toBe(
      true,
    );
    // The Shadow's total turns red too when the minions are about to be overwhelmed (the new master)
    expect(el.querySelector('.gd-game__strength[data-side="shadow"]')?.hasAttribute('data-overwhelmed')).toBe(true);
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

  it('lists the newest chat lines and game messages first in the modern theme, the field above them', async () => {
    const { el, stable } = await open(state(TABLE), '?gameId=7', true, 'modern');
    backend
      .expectOne((r) => r.url === CHAT && r.method === 'GET')
      .flush('<chat><message from="bob" date="2">hi</message><user>alice</user></chat>');
    await stable();
    const lines = Array.from(el.querySelectorAll<HTMLElement>('.gd-game-chat__line'), (line) =>
      line.textContent.replace(/\s+/g, ' ').trim(),
    );
    expect(lines).toEqual(['bob: hi', 'Careful', 'Plain message', 'Hello Gimli alice']);
    expect(el.querySelector('gd-game-chat')?.hasAttribute('data-newest-first')).toBe(true);
  });

  it("jumps to the newest line at the top with every new one, and the modern settings switch the chat's order", async () => {
    const { el, stable, news, fixture } = await open(state(TABLE), '?gameId=7', true, 'modern');
    backend.expectOne((r) => r.url === CHAT && r.method === 'GET').flush('<chat><user>alice</user></chat>');
    await stable();
    // jsdom lays nothing out: the list as a browser would have it, 100px high, 400px of lines
    const box = el.querySelector<HTMLElement>('.gd-game-chat__lines')!;
    let top = 0;
    Object.defineProperty(box, 'clientHeight', { configurable: true, get: () => 100 });
    Object.defineProperty(box, 'scrollHeight', { configurable: true, get: () => 400 });
    Object.defineProperty(box, 'scrollTop', { configurable: true, get: () => top, set: (v: number) => (top = v) });
    const lines = () =>
      Array.from(el.querySelectorAll<HTMLElement>('.gd-game-chat__line'), (line) =>
        line.textContent.replace(/\s+/g, ' ').trim(),
      );
    // Read down the list, then a new line: back at the top, where it is
    top = 250;
    await news('<ge type="M" message="Newest news"/>');
    expect(lines()[0]).toBe('Newest news');
    expect(top).toBe(0);

    // The settings tab (modern only): oldest first, at the end
    (el.querySelector('[data-tab="settings"]') as HTMLElement).click();
    await stable();
    const order = el.querySelector<HTMLInputElement>('[data-setting="chatNewestFirst"]')!;
    expect(order.checked).toBe(true);
    order.checked = false;
    order.dispatchEvent(new Event('change'));
    await stable();
    expect(el.querySelector('gd-game-chat')?.hasAttribute('data-newest-first')).toBe(false);
    expect(lines().at(-1)).toBe('Newest news');
    expect(top).toBe(400);
    expect(fixture.debugElement.injector.get(GameSettings).chatNewestFirst()).toBe(false);
    order.checked = true;
    order.dispatchEvent(new Event('change'));
    await stable();
    expect(lines()[0]).toBe('Newest news');
    expect(top).toBe(0);
    document.cookie = 'gempChatNewestFirst=; path=/gemp-lotr; max-age=0';
    document.cookie = 'gempChatNewestFirst=; path=/; max-age=0';
  });

  it("offers no chat order in classic's settings: game.html's order, the newest last", async () => {
    const { el, stable } = await open(state(TABLE));
    (el.querySelector('[data-tab="settings"]') as HTMLElement).click();
    await stable();
    expect(el.querySelector('[data-panel="settings"]')).not.toBeNull();
    expect(el.querySelector('[data-setting="chatNewestFirst"]')).toBeNull();
    expect(el.querySelector('gd-game-chat')?.hasAttribute('data-newest-first')).toBe(false);
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
    expect(sentForm(sent.request).get('message')).toBe('hello');
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

  it('keeps the timer alerts as typed on the settings tab: a rule needs both numbers above 0', async () => {
    const { el, fixture, stable } = await open(state(TABLE + STATS));
    const settings = fixture.debugElement.injector.get(GameSettings);
    (el.querySelector('[data-tab="settings"]') as HTMLElement).click();
    await stable();
    const fields = (name: string) => [...el.querySelectorAll<HTMLInputElement>(`[data-timer-alert="${name}"]`)];
    const below = fields('timerAlertBelow');
    const every = fields('timerAlertEvery');
    expect(below.length).toBeGreaterThan(0);
    const type = async (field: HTMLInputElement | undefined, value: string) => {
      if (!field) throw new Error('no field');
      field.value = value;
      field.dispatchEvent(new Event('change'));
      await stable();
    };
    await type(below[0], '5');
    await type(every[0], '20');
    expect(settings.timerAlertRules()[0]).toEqual({ belowSeconds: 300, everySeconds: 20 });
    // A rule with a 0 is dropped
    const before = settings.timerAlertRules().length;
    await type(every[0], '0');
    expect(settings.timerAlertRules().length).toBe(before - 1);
    document.cookie = 'timerAlertRules=; path=/; max-age=0';
    document.cookie = 'timerAlertRules=; path=/gemp-lotr; max-age=0';
  });

  it("opens a pile's dialog from its counter with a click or Enter, only where the player may look", async () => {
    const { el, stable, news } = await open(state(TABLE + STATS));
    for (const toggle of ['0', '1']) (el.querySelector(`[data-stats-toggle="${toggle}"]`) as HTMLElement).click();
    await stable();
    const dialogs = () => el.querySelectorAll('gd-game-piles gd-dialog').length;
    const press = async (stat: string, how: 'click' | 'enter') => {
      const counter = el.querySelector<HTMLElement>(`[data-stat="${stat}"]`);
      if (!counter) throw new Error(`no counter ${stat}`);
      if (how === 'click') counter.click();
      else counter.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
      await stable();
    };
    // Not one's to look into: nothing opens
    await press('adventureDeck1', 'click');
    await press('adventureDeck1', 'enter');
    await press('deck0', 'click');
    expect(dialogs()).toBe(0);
    await press('deadPile1', 'click');
    expect(dialogs()).toBe(1);
    await press('removedPile1', 'enter');
    await press('adventureDeck0', 'enter');
    await press('deadPile0', 'enter');
    expect(dialogs()).toBe(4);
    // The opponent's revealed hand (STATS: bob's handRevealed), by click and by Enter
    await press('hand1', 'click');
    expect(dialogs()).toBe(5);
    await press('hand1', 'enter');
    // One's own deck, once the game has ended
    await news('<ge type="EG"/>');
    await press('deck0', 'enter');
    expect(dialogs()).toBe(6);
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

  it('opens a pile in a dialog of its own from its counter, and a revealed hand, until closed', async () => {
    const { el, stable } = await open(
      state(
        TABLE + card('11', 'DISCARD') + card('12', 'DISCARD') + STATS + card('13', 'HAND', '').replace('alice', 'bob'),
      ),
    );
    (el.querySelector('[data-stats-toggle="0"]') as HTMLElement).click();
    await stable();
    const discard = el.querySelector<HTMLElement>('[data-stat="discard0"]')!;
    discard.click();
    discard.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    (el.querySelector('[data-stat="hand1"]') as HTMLElement).click();
    await stable();
    const dialogs = () => Array.from(el.querySelectorAll<HTMLDialogElement>('dialog[data-dialog="pile"]'));
    expect(dialogs().map((d) => d.querySelector('[data-dialog-title]')?.textContent)).toEqual([
      'Discard Pile - alice',
      'Revealed Hand - bob',
    ]);
    expect(
      Array.from(dialogs()[0]!.querySelectorAll<HTMLElement>('[data-card-id]'), (c) => c.dataset['cardId']),
    ).toEqual(['11', '12']);
    (dialogs()[0]!.querySelector('.gd-dialog__close') as HTMLElement).click();
    await stable();
    expect(dialogs()).toHaveLength(1);
  });

  it('shows a card named in a text when its name is clicked', async () => {
    const { el, stable } = await open(state(TABLE));
    // Not a name: nothing
    (el.querySelector('.gd-game-chat__line') as HTMLElement).click();
    await stable();
    expect(el.querySelector('gd-card-info [data-dialog-title]')).toBeNull();
    (el.querySelector('.gd-game-chat__line .cardHint') as HTMLElement).click();
    await stable();
    expect(el.querySelector('gd-card-info [data-dialog-title]')?.textContent).toBe('Card Information');
    expect(el.querySelector('gd-card-info .gd-card-info__details')).toBeNull();
  });

  it("shows a card named in a card's details inline as a card link, and its information when clicked", async () => {
    const { el, stable } = await open(state(TABLE));
    const card1 = el.querySelector<HTMLElement>('[data-card-id="1"]')!;
    card1.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true }));
    await stable();
    // The new master (2026-11-17) names the card a bonus comes from, as a card link inside the bold label
    backend
      .expectOne((r) => r.url.endsWith('/cardInfo'))
      .flush(
        "<b>Affecting card:</b><br><b><div class='cardHint' value='1_2'>·The One Ring, The Ruling Ring</div>:</b> " +
          'Strength +1<br>',
      );
    await stable();
    const hint = el.querySelector<HTMLElement>('gd-card-info .gd-card-info__details .cardHint');
    expect(hint?.textContent).toBe('·The One Ring, The Ruling Ring');
    hint!.click();
    await stable();
    // That card's information, a card's name: no game details
    expect(el.querySelector('gd-card-info [data-dialog-title]')?.textContent).toBe('Card Information');
    expect(el.querySelector('gd-card-info .gd-card-info__details')).toBeNull();
  });

  it('moves a card of the hand by dragging it sideways, and that drag is not a click on the card', async () => {
    const { el, stable } = await open(state(TABLE + card('9', 'HAND') + card('10', 'HAND')));
    const hand = () =>
      Array.from(el.querySelectorAll<HTMLElement>('[data-row="hand"] [data-card-id]'), (c) => c.dataset['cardId']);
    expect(hand()).toEqual(['4', '9', '10']);
    const box = el.querySelector<HTMLElement>('[data-row="hand"] .gd-game-cards__box')!;
    const card4 = el.querySelector<HTMLElement>('[data-row="hand"] [data-card-id="4"]')!;
    card4.getBoundingClientRect = () => ({ width: 50 }) as DOMRect;
    const pointer = (target: Element, type: string, clientX: number, clientY = 100) => {
      const event = new MouseEvent(type, { bubbles: true, clientX, clientY, button: 0 });
      Object.defineProperty(event, 'pointerType', { value: 'mouse' });
      Object.defineProperty(event, 'pointerId', { value: 1 });
      target.dispatchEvent(event);
    };
    pointer(card4, 'pointerdown', 100);
    pointer(card4, 'pointermove', 110);
    expect(box.hasAttribute('data-dragging')).toBe(false);
    pointer(card4, 'pointermove', 205);
    expect(box.hasAttribute('data-dragging')).toBe(true);
    // Dragged up too: a card being moved shows no card information
    pointer(card4, 'pointerup', 205, 50);
    expect(box.hasAttribute('data-dragging')).toBe(false);
    card4.click();
    await stable();
    expect(hand()).toEqual(['9', '10', '4']);
    expect(el.querySelector('gd-card-info [data-dialog-title]')).toBeNull();
    // A press of something else, or of another button, moves nothing
    pointer(box, 'pointerdown', 0);
    pointer(box, 'pointermove', 100);
    card4.click();
    expect(hand()).toEqual(['9', '10', '4']);
  });

  it("writes out a replay's Slower and Faster in the modern theme, with how fast it plays, each off at its limit", async () => {
    const { el, stable } = await open('', '?replayId=alice$abc', true, 'modern');
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$abc')
      .flush(`<gameReplay>${PLAYERS}</gameReplay>`);
    await stable();
    const replay = el.querySelector('gd-game-replay')!;
    const button = (which: string) => replay.querySelector<HTMLButtonElement>(`[data-replay="${which}"]`)!;
    const speed = () => replay.querySelector('[data-replay="speed"]')?.textContent.trim();
    expect(replay.hasAttribute('data-words')).toBe(true);
    expect(button('slower').textContent.trim()).toBe('Slower');
    expect(button('faster').textContent.trim()).toBe('Faster');
    expect(speed()).toBe('×1');
    button('faster').click();
    await stable();
    expect(speed()).toBe('×2');
    for (let i = 0; i < 5; i++) button('faster').click();
    await stable();
    expect(speed()).toBe('×16');
    expect(button('faster').disabled).toBe(true);
    for (let i = 0; i < 9; i++) button('slower').click();
    await stable();
    expect(speed()).toBe('×1/16');
    expect(button('slower').disabled).toBe(true);
    expect(button('faster').disabled).toBe(false);
  });

  it('plays a replay with its controls, and shows only the chat and the settings', async () => {
    document.cookie = 'animation-speed=4; path=/';
    try {
      const { el, stable } = await open('', '?replayId=alice$abc');
      backend
        .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$abc')
        .flush(
          `<gameReplay>${PLAYERS}${card('1', 'FREE_CHARACTERS')}<ge type="TP" count="3"/><ge type="EG"/></gameReplay>`,
        );
      await stable();
      expect(el.querySelector('[data-tab="options"]')).toBeNull();
      const replay = el.querySelector('gd-game-replay')!;
      (replay.querySelector('[data-replay="slower"]') as HTMLElement).click();
      (replay.querySelector('[data-replay="faster"]') as HTMLElement).click();
      (replay.querySelector('[data-replay="play"]') as HTMLElement).click();
      await stable();
      expect(replay.querySelector('[data-replay="play"]')?.getAttribute('aria-pressed')).toBe('true');
      for (let i = 0; i < 100 && !replay.hasAttribute('data-replay-done'); i++) {
        await new Promise((resolve) => setTimeout(resolve, 50));
        await stable();
      }
      expect(replay.hasAttribute('data-replay-done')).toBe(true);
      expect(el.querySelector('[data-game="twilight"]')?.textContent).toBe('3');
      expect(el.querySelector('[data-card-id="1"]')).not.toBeNull();
      expect(el.hasAttribute('data-animating')).toBe(false);
    } finally {
      document.cookie = 'animation-speed=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    }
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

  describe("the game's end with the replay (modern theme, the owner 2026-11-20)", () => {
    const HISTORY = '/gemp-lotr-server/gameHistory';
    const entry = (opponent: string, recording: string | null, endMs = Date.now()) =>
      `<gameHistory><historyEntry opponent="${opponent}" result="W" endMs="${endMs}"` +
      `${recording === null ? '' : ` gameRecordingId="${recording}"`}/></gameHistory>`;
    const END =
      '<ge type="M" message="alice is the winner due to: Surviving to Sanctuary"/>' +
      '<ge type="M" message="Thank you for playing! &lt;a href=\'x\'&gt;using this form.&lt;/a&gt;"/><ge type="EG"/>';

    it('says who won and why, with the replay to watch or copy, and the thanks; classic keeps its feedback', async () => {
      const writeText = vi.fn(() => Promise.resolve());
      Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });
      const { el, news, stable, text } = await open(state(TABLE), '?gameId=7', true, 'modern');
      expect(el.querySelector('dialog[data-dialog="game-ended"][open]')).toBeNull();
      await news(END);
      const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="game-ended"]')!;
      expect(dialog.open).toBe(true);
      expect(el.querySelector('dialog[data-dialog="feedback"]')).toBeNull();
      expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('You won!');
      expect(text('[data-ended="result"]')).toBe('You won the game.');
      expect(dialog.querySelector('[data-ended="result"]')?.hasAttribute('data-won')).toBe(true);
      expect(text('[data-ended="reason"]')).toBe('Surviving to Sanctuary');
      expect(dialog.querySelector('[data-ended="replay"]')?.getAttribute('data-state')).toBe('looking');
      expect(dialog.querySelector('[data-ended="feedback"] a')?.textContent).toBe('using this form.');

      backend.expectOne((r) => r.url === HISTORY).flush(entry('bob', 'r42'));
      await stable();
      expect(dialog.querySelector('[data-ended="replay"]')?.getAttribute('data-state')).toBe('found');
      const link = `${location.origin}/gemp-lotr/ng/game?replayId=alice%24r42`;
      expect(dialog.querySelector<HTMLInputElement>('[data-ended="link"]')?.value).toBe(link);
      (dialog.querySelector('[data-ended="copy"]') as HTMLElement).click();
      await stable();
      await stable();
      expect(writeText).toHaveBeenCalledWith(link);
      expect(text('[data-ended="copy"]')).toBe('Link copied');

      (dialog.querySelector('[data-ended="stay"]') as HTMLElement).click();
      await stable();
      expect(dialog.open).toBe(false);
      // The alert box's button shows it again
      (el.querySelector('gd-game-alert [data-game="ended"]') as HTMLElement).click();
      await stable();
      expect(dialog.open).toBe(true);
    });

    it("leaves classic as game.html: its feedback dialog, no game's end and no button for it", async () => {
      const { el, news } = await open(state(TABLE));
      await news(END);
      expect(el.querySelector('dialog[data-dialog="feedback"]')?.hasAttribute('open')).toBe(true);
      expect(el.querySelector('dialog[data-dialog="game-ended"]')).toBeNull();
      expect(el.querySelector('[data-game="ended"]')).toBeNull();
      backend.expectNone((r) => r.url === HISTORY);
    });

    it('opens the replay, and selects its link for the player to copy where the browser cannot copy it', async () => {
      let page: HTMLElement | null = null;
      for (const clipboard of [undefined, { writeText: () => Promise.reject(new Error('denied')) }]) {
        TestBed.resetTestingModule();
        Object.defineProperty(navigator, 'clipboard', { value: clipboard, configurable: true });
        const { el, news, stable } = await open(state(TABLE), '?gameId=7', true, 'modern');
        page = el;
        await news(END);
        backend.expectOne((r) => r.url === HISTORY).flush(entry('bob', 'r42'));
        await stable();
        const field = el.querySelector<HTMLInputElement>('[data-ended="link"]')!;
        const select = vi.spyOn(field, 'select');
        (el.querySelector('[data-ended="copy"]') as HTMLElement).click();
        await stable();
        await stable();
        expect(select).toHaveBeenCalledTimes(1);
        expect(el.querySelector('[data-ended="copy"]')?.hasAttribute('data-copied')).toBe(false);
        field.dispatchEvent(new Event('focus'));
        expect(select).toHaveBeenCalledTimes(2);
      }
      const go = vi.spyOn(TestBed.inject(PageCues), 'go').mockImplementation(() => undefined);
      (page?.querySelector('[data-ended="watch"]') as HTMLElement).click();
      expect(go).toHaveBeenCalledWith(`${location.origin}/gemp-lotr/ng/game?replayId=alice%24r42`);
    });

    it("tells a player who lost, and that the replay isn't found when the history hasn't got it", async () => {
      vi.useFakeTimers({ toFake: ['setTimeout'] });
      try {
        const { el, stable, text, fixture } = await open(state(TABLE), '?gameId=7', true, 'modern');
        await vi.advanceTimersByTimeAsync(POLL_DELAY_MS + 10);
        backend
          .expectOne((r) => r.url === GAME && r.method === 'POST')
          .flush(update('<ge type="M" message="bob is the winner due to: Ring-bearer corrupted"/><ge type="EG"/>'));
        await stable();
        expect(text('dialog[data-dialog="game-ended"] [data-dialog-title]')).toBe('You lost');
        expect(text('[data-ended="result"]')).toBe('bob won the game.');
        // An earlier game against someone else, then one too old, then nothing, then a game without a recording
        backend.expectOne((r) => r.url === HISTORY).flush(entry('carol', 'r1'));
        await vi.advanceTimersByTimeAsync(1500);
        backend.expectOne((r) => r.url === HISTORY).flush(entry('bob', 'r2', Date.now() - 60 * 60 * 1000));
        await vi.advanceTimersByTimeAsync(1500);
        backend.expectOne((r) => r.url === HISTORY).flush('<gameHistory/>');
        await vi.advanceTimersByTimeAsync(1500);
        backend.expectOne((r) => r.url === HISTORY).flush(entry('bob', null));
        await stable();
        fixture.detectChanges();
        expect(el.querySelector('[data-ended="replay"]')?.getAttribute('data-state')).toBe('missing');
        backend.expectNone((r) => r.url === HISTORY);
      } finally {
        vi.useRealTimers();
      }
    });
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

describe('GamePageComponent in a scripted game', () => {
  let backend: HttpTestingController;
  const saved = { frame: window.requestAnimationFrame, cancel: window.cancelAnimationFrame };

  beforeEach(() => {
    sessionStorage.clear();
    // A window large enough for a scenario; the bubble's measuring is the layer's own spec's business
    Object.defineProperty(document.documentElement, 'clientWidth', { configurable: true, get: () => 1280 });
    Object.defineProperty(document.documentElement, 'clientHeight', { configurable: true, get: () => 800 });
    window.requestAnimationFrame = () => 1;
    window.cancelAnimationFrame = () => undefined;
  });

  afterEach(() => {
    backend.match(() => true);
    delete (document.documentElement as { clientWidth?: number }).clientWidth;
    delete (document.documentElement as { clientHeight?: number }).clientHeight;
    window.requestAnimationFrame = saved.frame;
    window.cancelAnimationFrame = saved.cancel;
  });

  const json = (value: unknown) =>
    JSON.stringify(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  const S = (event: unknown) => `<ge type="S" scenario="${json(event)}"/>`;
  const INFO = {
    type: 'info',
    id: 'basics-01',
    kind: 'tutorial',
    title: 'The Basics',
    humanSeat: 'freeps',
    opponent: 'bob',
    opponentName: 'Basic Shadow',
    autoAccept: true,
    autoDismissDeckReveal: true,
    animationSpeed: null,
    campaign: null,
  };
  const STEP = {
    type: 'step',
    step: 1,
    sub: 0,
    seat: 'human',
    pause: false,
    messages: [{ header: 'Welcome', html: '<p>Play Gimli.</p>', anchors: ['card:gimli'], advance: 'next' }],
    allow: { answers: ['10'], pass: false, otherwise: null, hints: { other: '<p>Not that one.</p>' } },
    cards: { gimli: 1, other: 6 },
  };
  const ACTIONS =
    '<ge type="D" id="3" decisionType="CARD_ACTION_CHOICE" participantId="alice" text="Play">' +
    '<parameter name="cardId" value="1"/><parameter name="cardId" value="6"/>' +
    '<parameter name="blueprintId" value="inPlay"/><parameter name="blueprintId" value="inPlay"/>' +
    '<parameter name="actionId" value="10"/><parameter name="actionId" value="11"/>' +
    '<parameter name="actionText" value="Use"/><parameter name="actionText" value="Use"/></ge>';

  async function open(events: string) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        { provide: GAME_THEME_PICKER, useValue: false },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    TestBed.inject(ThemeService).select('classic');
    TestBed.inject(PlayerInfo).load().subscribe();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('{"name":"alice"}');
    const fixture = TestBed.createComponent(GamePageComponent);
    const el = fixture.nativeElement as HTMLElement;
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(state(events));
    const stable = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
    };
    await stable();
    return { el, stable };
  }

  it("draws the scenario over the page: its bubble, its menu, the cards as the step takes them, the opponent's name", async () => {
    const { el, stable } = await open(TABLE + STATS + S(INFO) + S(STEP) + ACTIONS);
    expect(el.getAttribute('data-scenario')).toBe('tutorial');
    expect(el.querySelector('gd-game-scenario .gd-scenario-bubble__title')?.textContent).toBe('Welcome');
    expect(el.querySelector('nav.gd-scenario-menu .gd-scenario-menu__title')?.textContent).toBe('The Basics');
    expect(el.querySelector('section.gd-scenario-panel')).toBeNull();
    const card = (id: string) => el.querySelector<HTMLElement>(`gd-game-cards [data-card-id="${id}"]`);
    expect(card('1')?.dataset['scenario']).toBe('live');
    expect(card('6')?.dataset['scenario']).toBe('notnow');
    expect(card('5')?.hasAttribute('data-scenario')).toBe(false);
    // The scripted opponent under its shown name: in the state panel, the messages and the chat box
    expect(el.querySelector('gd-game-state')?.textContent).toContain('Basic Shadow');
    expect(el.querySelector('gd-game-state')?.textContent).not.toContain('bob');
    const said = el.querySelector<HTMLElement>('.gd-game-chat__line[data-said]');
    expect(said?.textContent.replace(/\s+/g, ' ').trim()).toBe('Basic Shadow: Play Gimli.');
    expect(el.querySelectorAll('.gd-game-chat__line[data-said]')).toHaveLength(1);

    // Read first, then Gimli's action; the other card gets its hint
    el.querySelector<HTMLButtonElement>('[data-scenario="next"]')?.click();
    await stable();
    expect(el.querySelector('.gd-scenario-bubble')).toBeNull();
    card('6')?.click();
    await stable();
    expect(el.querySelector('.gd-scenario-hint')?.innerHTML).toBe('<p>Not that one.</p>');
    backend.expectNone((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'));
    await new Promise((resolve) => setTimeout(resolve, 250));
    card('1')?.click();
    await stable();
    const answer = backend.expectOne((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'));
    expect(answer.request.body?.get('decisionValue')).toBe('10');
  });

  it('names a pile of the scripted opponent by its shown name, and has no feedback dialog at the end', async () => {
    const { el, stable } = await open(TABLE + STATS + S(INFO));
    (el.querySelector('[data-stats-toggle="1"]') as HTMLElement | null)?.click();
    await stable();
    (el.querySelector('[data-stat="hand1"]') as HTMLElement).click();
    await stable();
    expect(el.querySelector('dialog[data-dialog="pile"] [data-dialog-title]')?.textContent).toBe(
      'Revealed Hand - Basic Shadow',
    );
    await new Promise((resolve) => setTimeout(resolve, POLL_DELAY_MS + 10));
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(
        update(
          '<ge type="M" message="Thank you for playing! &lt;a href=\'x\'&gt;using this form.&lt;/a&gt;"/><ge type="EG"/>' +
            S({ type: 'end', result: 'complete', messages: [], message: null, checkpoint: null }),
        ),
      );
    await stable();
    expect(el.querySelector('dialog[data-dialog="feedback"][open]')).toBeNull();
    expect(el.querySelector('section.gd-scenario-panel')?.getAttribute('data-result')).toBe('complete');
    expect(el.querySelector('.gd-scenario-panel__report a')?.getAttribute('href')).toBe('x');
  });

  it("docks the authoring panel at the right in a draft's game, the page leaving it the room", async () => {
    const { el, stable } = await open(TABLE + STATS + S({ ...INFO, author: true }) + ACTIONS);
    expect(el.querySelector('gd-game-author section.gd-author')).not.toBeNull();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/author/state').flush('{"session":false}');
    await stable();
    expect(el.querySelector('.gd-author')?.getAttribute('data-view')).toBe('elsewhere');
    expect(document.documentElement.style.getPropertyValue('--gd-page-right')).toBe('430px');
    document.documentElement.style.removeProperty('--gd-page-right');
  });

  it('has no authoring panel in a scenario played as a player', async () => {
    const { el } = await open(TABLE + STATS + S(INFO));
    expect(el.querySelector('gd-game-author')).toBeNull();
  });

  it('shows a stopped sandbox in edit mode: no decision, the edit bar, an open spot in each row, the card menu', async () => {
    const SANDBOX = {
      ...INFO,
      kind: 'sandbox',
      mode: 'edit',
      level: 'sandbox',
      tools: ['add', 'delete'],
      canStart: true,
    };
    const { el, stable } = await open(TABLE + STATS + S(SANDBOX) + ACTIONS);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/state')
      .flush({ mode: 'edit', level: 'sandbox', tools: ['add', 'delete'], cards: [] });
    await stable();
    expect(el.getAttribute('data-edit')).toBe('edit');
    expect(el.querySelector('[data-game="alert"]')?.textContent).toBe('');
    expect(el.querySelectorAll('button[data-decision-button]')).toHaveLength(0);
    expect(el.querySelector('nav.gd-scenario-menu gd-edit-bar')?.getAttribute('data-mode')).toBe('edit');
    expect(el.querySelector('gd-table-edit')).not.toBeNull();
    const spots = [...el.querySelectorAll<HTMLElement>('gd-open-spot button')].map(
      (spot) => `${spot.dataset['seat']}.${spot.dataset['zone']}`,
    );
    // (the person plays the freeps seat; the minions' row is the other seat's on the person's own turn)
    expect(spots).toEqual([
      'shadow.support',
      'shadow.characters',
      'shadow.minions',
      'freeps.characters',
      'freeps.support',
      'freeps.hand',
    ]);
    const card = el.querySelector<HTMLElement>('gd-game-cards [data-card-id="1"]')!;
    card.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true }));
    await stable();
    expect(el.querySelector('.gd-edit-menu [data-menu="info"]')).not.toBeNull();
    // Start: play mode, the bar and nothing else of the controls; the game's news asked for at once
    el.querySelector<HTMLElement>('.gd-edit-backdrop')?.dispatchEvent(new Event('pointerdown'));
    el.querySelector<HTMLButtonElement>('[data-edit-bar="start"]')?.click();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/start')
      .flush({ ok: true, state: { mode: 'play', level: 'sandbox', tools: ['add'], canStop: true, cards: [] } });
    await stable();
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(update(S({ ...SANDBOX, mode: 'play', canStop: true })));
    await stable();
    expect(el.getAttribute('data-edit')).toBe('play');
    expect(el.querySelectorAll('gd-open-spot')).toHaveLength(0);
    expect(el.querySelector('[data-game="alert"]')?.textContent).not.toBe('');
  });

  it("flips a sandbox's table: the seat looked from below, its open spots its own; the other seat's to choose", async () => {
    const FLIPPED = {
      ...INFO,
      kind: 'sandbox',
      mode: 'edit',
      level: 'sandbox',
      tools: ['add'],
      canStart: true,
      seat: 'shadow',
      otherSeat: 'person',
      otherSeats: ['script', 'person'],
      waitingOn: 'freeps',
      canFlip: true,
    };
    const { el, stable } = await open(TABLE + STATS + S(FLIPPED) + ACTIONS);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/state')
      .flush({ mode: 'edit', level: 'sandbox', tools: ['add'], cards: [] });
    await stable();
    const spots = [...el.querySelectorAll<HTMLElement>('gd-open-spot button')].map(
      (spot) => `${spot.dataset['seat']}.${spot.dataset['zone']}`,
    );
    // (looking from the shadow seat: its rows and hand at the bottom, the person's own seat at the top)
    expect(spots).toEqual([
      'freeps.support',
      'freeps.characters',
      'freeps.minions',
      'shadow.characters',
      'shadow.support',
      'shadow.hand',
    ]);
    const bar = el.querySelector('gd-edit-bar gd-seat-controls');
    expect(bar?.querySelector('.gd-seat-controls__looking')?.textContent?.trim()).toBe('As Shadow');
    expect(bar?.querySelector<HTMLSelectElement>('select[data-seat-control="other"]')?.value).toBe('person');
    // In play, the person's own seat waits: said where the decision would be, with Flip; nothing to answer here
    el.querySelector<HTMLButtonElement>('[data-edit-bar="start"]')?.click();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/start')
      .flush({ ok: true, state: { mode: 'play', level: 'sandbox', tools: ['add'], canStop: true, cards: [] } });
    await stable();
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(update(S({ ...FLIPPED, mode: 'play', canStop: true, otherSeat: 'script' })));
    await stable();
    expect(el.querySelector('gd-game-alert .gd-seat-waiting__text')?.textContent).toBe('Free Peoples to choose');
    expect(el.querySelector('gd-game-alert gd-seat-waiting button[data-flip]')).not.toBeNull();
    // (flipped onto a seat the script plays: its decision is not offered)
    expect(el.querySelector('[data-game="alert"]')?.textContent).toBe('');
    expect(el.querySelectorAll('button[data-decision-button]')).toHaveLength(0);
  });

  it('shows a puzzle held for its picks: no decision, its slots in their rows and the strip, Choosing and Submit', async () => {
    const PUZZLE = {
      ...INFO,
      kind: 'puzzle',
      mode: 'edit',
      level: 'puzzle',
      undo: false,
      slots: [...PUZZLE_SLOTS, { id: 'tale', label: 'a tale', zone: 'deck', position: 2 }],
    };
    const { el, stable } = await open(TABLE + STATS + S(PUZZLE) + ACTIONS);
    await stable();
    expect(el.getAttribute('data-edit')).toBe('edit');
    expect(el.querySelector('[data-game="alert"]')?.textContent).toBe('');
    expect(el.querySelectorAll('button[data-decision-button]')).toHaveLength(0);
    expect(el.querySelectorAll('gd-open-spot')).toHaveLength(0);
    expect(el.querySelector('nav.gd-scenario-menu gd-puzzle-bar')?.getAttribute('data-mode')).toBe('edit');
    const inRows = [...el.querySelectorAll<HTMLElement>('.gd-game__row .gd-puzzle-slot')].map(
      (slot) => `${slot.closest<HTMLElement>('[data-row]')?.dataset['row']}:${slot.dataset['slot']}`,
    );
    expect(inRows).toEqual(['support-player:condition', 'support-player:ally']);
    expect(el.querySelector('section.gd-puzzle-strip .gd-puzzle-slot')?.getAttribute('data-slot')).toBe('tale');
    expect(el.querySelector('section.gd-puzzle-strip .gd-puzzle-slot__place')?.textContent).toBe('Deck, 2nd');
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
