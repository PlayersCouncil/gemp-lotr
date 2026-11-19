import { SELECTION_DEBOUNCE_MS } from '../../../core/game/decision/game-decisions.service';
import { GameSettings } from '../../../core/game/settings/game-settings';
import { PhoneSettings } from '../../../core/game/phone/phone-settings';
import { GameSession } from '../../../core/game/session/game-session.service';
import { ThemeService } from '../../../core/platform/theme.service';
import { signal } from '@angular/core';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { GamePageComponent } from '../game-page.component';

const GAME = '/gemp-lotr-server/game/7';
const param = (name: string, ...values: string[]) =>
  values.map((value) => `<parameter name="${name}" value="${value}"/>`).join('');
const card = (id: string, zone: string, extra = '', owner = 'alice', blueprint = '1_7') =>
  `<ge type="PCIP" cardId="${id}" blueprintId="${blueprint}" participantId="${owner}" zone="${zone}" ${extra}/>`;
const decision = (type: string, params: string, text = 'Do it') =>
  `<ge type="D" id="3" decisionType="${type}" participantId="alice" text="${text}">${params}</ge>`;
const TABLE =
  '<ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>' +
  card('1', 'FREE_CHARACTERS') +
  card('2', 'ATTACHED', 'targetCardId="1" targetType="attached"') +
  card('3', 'SUPPORT', '', 'alice', '1_121') +
  card('4', 'SUPPORT') +
  card('5', 'SHADOW_CHARACTERS', '', 'bob') +
  card('6', 'HAND') +
  card('7', 'HAND') +
  card('8', 'ADVENTURE_PATH', 'index="1"') +
  card('9', 'DEAD') +
  '<ge type="PP" participantId="alice" index="1"/><ge type="PP" participantId="bob" index="1"/>' +
  '<ge type="TC" participantId="alice" otherCardIds=""/><ge type="GPC" phase="Fellowship"/>';
const game = (events = '') => `<gameState cn="1">${TABLE}${events}</gameState>`;

describe('the game on a phone (G7)', () => {
  let backend: HttpTestingController;
  let cues: {
    vibrate: ReturnType<typeof vi.fn>;
    play: ReturnType<typeof vi.fn>;
    toggleFullscreen: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => sessionStorage.clear());
  afterEach(() => {
    backend.match(() => true);
    document.cookie = 'gempGameSplit=; path=/; max-age=0';
  });

  /** The page with the compact layout on (the modern theme on a phone: CompactLayout) or off. */
  async function open(body: string, { compact = true } = {}) {
    cues = { vibrate: vi.fn(), play: vi.fn(), toggleFullscreen: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        {
          provide: PageCues,
          useValue: {
            ...cues,
            setTitle: vi.fn(),
            away: () => false,
            reload: vi.fn(),
            go: vi.fn(),
            fullscreen: signal(false),
            fullscreenAvailable: () => true,
            fontsReady: () => Promise.resolve(),
          },
        },
        {
          provide: CompactLayout,
          useValue: {
            active: signal(compact),
            sideBySide: signal(false),
            device: signal(compact ? 'phone' : null),
            gameActive: signal(compact),
            gameDevice: signal(compact ? 'phone' : null),
            tallSideways: signal(false),
          },
        },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(GamePageComponent);
    const el = fixture.nativeElement as HTMLElement;
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(body);
    const stable = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();
    };
    await stable();
    const click = async (selector: string) => {
      const target = el.querySelector<HTMLElement>(selector);
      if (!target) throw new Error(`nothing at ${selector}`);
      target.click();
      await stable();
    };
    const longPress = async (selector: string) => {
      const target = el.querySelector<HTMLElement>(selector);
      if (!target) throw new Error(`nothing at ${selector}`);
      target.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true }));
      await stable();
    };
    const sheet = () => el.querySelector('.gd-phone-sheet:not([data-sheet="actions"])');
    const answer = () =>
      backend
        .match((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'))[0]
        ?.request.body?.get('decisionValue') ?? null;
    return { fixture, el, stable, click, longPress, sheet, answer };
  }

  it('shows the phone layout in the modern theme on a phone, and the computer layout otherwise', async () => {
    const { el } = await open(game());
    expect(el.getAttribute('data-layout')).toBe('phone');
    expect(el.querySelector('gd-game-phone')).not.toBeNull();
    expect(el.querySelector('.gd-game__table')).toBeNull();
    expect(el.querySelector('gd-game-tabs')).toBeNull();
    TestBed.resetTestingModule();
    const computer = await open(game(), { compact: false });
    expect(computer.el.querySelector('gd-game-phone')).toBeNull();
    expect(computer.el.querySelector('.gd-game__table')).not.toBeNull();
  });

  it('opens the cards under a card below it with its ▾ and puts them back with ▴; the setting can remove the button', async () => {
    const { el, click } = await open(game());
    const toggle = '[data-zone="characters-player"] .gd-phone-slot__toggle';
    const peek = () =>
      el.querySelector('[data-zone="characters-player"] .gd-phone-slot > [data-under][data-card-id="2"]');
    const opened = () =>
      el.querySelector('[data-zone="characters-player"] .gd-phone-slot__opened > [data-card-id="2"]');
    expect(el.querySelector(toggle)?.textContent?.trim()).toBe('▾');
    expect(el.querySelector(toggle)?.getAttribute('aria-expanded')).toBe('false');
    expect(peek()).not.toBeNull();
    expect(opened()).toBeNull();

    await click(toggle);
    expect(el.querySelector(toggle)?.textContent?.trim()).toBe('▴');
    expect(peek()).toBeNull();
    expect(opened()).not.toBeNull();

    await click(toggle);
    expect(peek()).not.toBeNull();
    expect(opened()).toBeNull();

    // "Nothing": no button, the cards only peek out
    TestBed.resetTestingModule();
    document.cookie = 'gempPhoneUnderButton=none; path=/';
    try {
      const without = await open(game());
      expect(without.el.querySelector(toggle)).toBeNull();
      expect(
        without.el.querySelector('[data-zone="characters-player"] .gd-phone-slot > [data-under][data-card-id="2"]'),
      ).not.toBeNull();
    } finally {
      document.cookie = 'gempPhoneUnderButton=; path=/; max-age=0';
    }
  });

  it("layers a fight's minions on the first; its ▾ opens the others in a row under it", async () => {
    // Bob's minions 5 and 10 assigned to alice's companion 1
    const { el, click } = await open(
      game(card('10', 'SHADOW_CHARACTERS', '', 'bob') + '<ge type="AA" cardId="1" otherCardIds="5,10"/>'),
    );
    const pair = '.gd-phone-pair[data-pair="assignment"]';
    const toggle = `${pair} .gd-phone-layered__toggle`;
    const peeking = () => el.querySelector(`${pair} .gd-phone-layered > [data-layered][data-card-id="10"]`);
    const opened = () => el.querySelector(`${pair} .gd-phone-layered__rest [data-card-id="10"]`);
    // The first on top, in its own place; the other under it, peeking out
    expect(el.querySelector(`${pair} .gd-phone-layered > .gd-phone-slot > [data-card-id="5"]`)).not.toBeNull();
    expect(peeking()).not.toBeNull();
    expect(el.querySelector(toggle)?.textContent?.trim()).toBe('▾ 1');

    await click(toggle);
    expect(el.querySelector(toggle)?.textContent?.trim()).toBe('▴');
    expect(peeking()).toBeNull();
    expect(opened()).not.toBeNull();
    // The first stays where it was
    expect(el.querySelector(`${pair} .gd-phone-layered > .gd-phone-slot > [data-card-id="5"]`)).not.toBeNull();

    await click(toggle);
    expect(peeking()).not.toBeNull();
    // One minion: no stack
    TestBed.resetTestingModule();
    const single = await open(game('<ge type="AA" cardId="1" otherCardIds="5"/>'));
    expect(single.el.querySelector('.gd-phone-layered')).toBeNull();
  });

  it('turns the fights over for the Shadow player: the companions on top, the minions near the player', async () => {
    const pair = '.gd-phone-pair[data-pair="assignment"]';
    // Alice's turn: she is the Free Peoples player, her companion at the bottom
    const own = await open(game('<ge type="AA" cardId="1" otherCardIds="5"/>'));
    expect(own.el.querySelector(pair)?.hasAttribute('data-reversed')).toBe(false);
    // Bob's turn: alice plays the Shadow
    TestBed.resetTestingModule();
    const shadow = await open(
      game('<ge type="TC" participantId="bob" otherCardIds=""/><ge type="AA" cardId="1" otherCardIds="5"/>'),
    );
    expect(shadow.el.querySelector(pair)?.hasAttribute('data-reversed')).toBe(true);
  });

  it('shows the pre-game information and asks the bid in the divider before the players are known', async () => {
    // The player keeps the hand hidden on this device
    document.cookie = 'gempGameSplit=phone:hidden; path=/';
    const { el, click, answer } = await open(
      '<gameState cn="1"><ge type="PGS" participantId="alice" allParticipantIds="alice,bob" summary="Casual game" ' +
        'notes="" maps="" metaSites=""/>' +
        decision(
          'INTEGER',
          param('min', '0') + param('max', '9') + param('defaultValue', '0'),
          'Choose a number of burdens to bid',
        ) +
        '</gameState>',
    );
    expect(el.querySelector('gd-game-phone .gd-phone-pregame gd-game-pregame')).not.toBeNull();
    expect(el.querySelector('gd-phone-table')).toBeNull();
    expect(el.querySelector('gd-phone-hand')).toBeNull();
    // No hand yet, so no hint to drag it up, even where the player keeps it hidden
    expect(el.querySelector('.gd-phone-divider__hand-hint')).toBeNull();
    expect(el.querySelector('[data-decision="text"]')?.textContent).toBe('Choose a number of burdens to bid');
    await click('[data-decision="integer-up"]');
    await click('[data-decision="integer-up"]');
    await click('[data-decision-button="OK"]');
    expect(answer()).toBe('2');
  });

  it('lays out the path first, the rows from the opponent to the player, the support Free Peoples first', async () => {
    const { el } = await open(game());
    const zones = Array.from(el.querySelectorAll('gd-phone-table .gd-phone-zone'), (z) => z.getAttribute('data-zone'));
    expect(zones).toEqual([
      'path',
      'support-opponent',
      'characters-opponent',
      'shadow',
      'characters-player',
      'support-player',
    ]);
    expect(el.querySelectorAll('.gd-phone-site')).toHaveLength(9);
    expect(el.querySelector('.gd-phone-site[data-site="1"] .gd-card')?.getAttribute('data-card-id')).toBe('8');
    const support = Array.from(el.querySelectorAll('[data-zone="support-player"] .gd-phone-slot > .gd-card'), (c) =>
      c.getAttribute('data-card-id'),
    );
    expect(support).toEqual(['4', '3']);
    expect(el.querySelector('[data-zone="support-player"] .gd-phone-cards__split')).not.toBeNull();
    // The attached card: under its card, peeking out on its left
    expect(
      el.querySelector('[data-zone="characters-player"] .gd-phone-slot > [data-under][data-card-id="2"]'),
    ).not.toBeNull();
    expect(Array.from(el.querySelectorAll('gd-phone-hand .gd-card'), (c) => c.getAttribute('data-card-id'))).toEqual([
      '6',
      '7',
    ]);
    expect(el.querySelectorAll('.gd-phone-player')).toHaveLength(2);
  });

  it('asks in the divider and answers with its buttons; the answer closes every sheet', async () => {
    const { el, click, answer, stable } = await open(
      game(
        decision(
          'CARD_ACTION_CHOICE',
          param('cardId', '6') + param('blueprintId', 'inPlay') + param('actionId', '10') + param('actionText', 'Play'),
          'Play a Fellowship action',
        ),
      ),
    );
    expect(el.querySelector('[data-decision="text"]')?.textContent).toBe('Play a Fellowship action');
    expect(el.querySelector('gd-phone-divider')?.hasAttribute('data-deciding')).toBe(true);
    expect(el.querySelector('[data-phone="your-move"]')).not.toBeNull();
    await click('[data-phone="menu"]');
    expect(el.querySelector('.gd-phone-sheet[data-sheet="menu"]')).not.toBeNull();
    await click('[data-decision-button="Pass"]');
    expect(answer()).toBe('');
    await stable();
    expect(el.querySelector('.gd-phone-sheet')).toBeNull();
  });

  it("changes every setting from the settings sheet: the game's (legacy cookies) and the phone's own", async () => {
    const { el, click, fixture, stable } = await open(game());
    const settings = fixture.debugElement.injector.get(GameSettings);
    const phone = fixture.debugElement.injector.get(PhoneSettings);
    const toggle = async (setting: string) => {
      const input = el.querySelector<HTMLInputElement>(`[data-setting="${setting}"]`);
      if (!input) throw new Error(`no setting ${setting}`);
      input.checked = !input.checked;
      input.dispatchEvent(new Event('change'));
      await stable();
    };
    const slide = async (setting: string, value: string) => {
      const input = el.querySelector<HTMLInputElement>(`[data-setting="${setting}"]`);
      if (!input) throw new Error(`no setting ${setting}`);
      input.value = value;
      input.dispatchEvent(new Event('input'));
      await stable();
    };
    await click('[data-phone="menu"]');
    await click('[data-menu="settings"]');
    // The game tab
    const shadowPass = settings.autoPassPhases().includes('SHADOW');
    await toggle('autoPassSHADOW');
    expect(settings.autoPassPhases().includes('SHADOW')).toBe(!shadowPass);
    const autoAccept = settings.autoAccept();
    await toggle('autoAccept');
    expect(settings.autoAccept()).toBe(!autoAccept);
    const dropDown = settings.alwaysDropDown();
    await toggle('alwaysDropDown');
    expect(settings.alwaysDropDown()).toBe(!dropDown);
    const reveal = settings.autoDismissDeckReveal();
    await toggle('autoDismissDeckReveal');
    expect(settings.autoDismissDeckReveal()).toBe(!reveal);
    const vibrate = phone.moveVibrate();
    await toggle('moveVibrate');
    expect(phone.moveVibrate()).toBe(!vibrate);
    const sound = phone.moveSound();
    await toggle('moveSound');
    expect(phone.moveSound()).toBe(!sound);
    const muted = settings.timerAudioMuted();
    await toggle('timerSound');
    expect(settings.timerAudioMuted()).toBe(!muted);
    // The display tab
    await click('[data-settings-tab="display"]');
    await slide('card-size', '120');
    expect(phone.cardSize()).toBe(120);
    await slide('animation-slider', '-2');
    expect(settings.animationSpeed()).toBe(-2);
    const oldStack = settings.oldStackVisuals();
    await toggle('use-old-stack-checkbox');
    expect(settings.oldStackVisuals()).toBe(!oldStack);
    const space = el.querySelector<HTMLInputElement>('[data-setting="underButton"][value="space"]');
    space?.dispatchEvent(new Event('change'));
    await stable();
    expect(settings.underButton()).toBe('space');
    // The cookies these wrote, gone again for the tests after this one
    for (const name of [
      'autoPassPhases',
      'autoAccept',
      'alwaysDropDown',
      'autoDismissDeckReveal',
      'timerAudioMuted',
      'animation-speed',
      'use-old-stack',
      'gempPhoneUnderButton',
      'gempGameMoveVibrate',
      'gempGameMoveSound',
      'gempGameCardSize',
    ]) {
      for (const path of ['/', '/gemp-lotr', '/gemp-lotr-server'])
        document.cookie = `${name}=; path=${path}; max-age=0`;
    }
    expect(document.cookie).not.toContain('autoAccept=');
  });

  it('asks before conceding or cancelling: Back returns to the menu, the button does it', async () => {
    const { el, click, fixture, sheet } = await open(game());
    const session = fixture.debugElement.injector.get(GameSession);
    const concede = vi.spyOn(session, 'concede').mockImplementation(() => undefined);
    const cancel = vi.spyOn(session, 'cancel').mockImplementation(() => undefined);
    await click('[data-phone="menu"]');
    await click('[data-menu="concede"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('confirm');
    expect(el.querySelector('.gd-phone-confirm')?.textContent).toContain('You lose this game');
    await click('[data-phone="confirm-no"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('menu');
    expect(concede).not.toHaveBeenCalled();
    await click('[data-menu="concede"]');
    await click('[data-phone="confirm-yes"]');
    expect(concede).toHaveBeenCalledOnce();
    expect(sheet()).toBeNull();
    await click('[data-phone="menu"]');
    await click('[data-menu="cancel"]');
    expect(el.querySelector('.gd-phone-confirm')?.textContent).toContain('cancelled, with no winner');
    expect(el.querySelector('[data-phone="confirm-yes"]')?.textContent?.trim()).toBe('Request cancel');
    await click('[data-phone="confirm-yes"]');
    expect(cancel).toHaveBeenCalledOnce();
  });

  it("opens a card's actions in a sheet, which closes with ✕ or the backdrop, and a choice answers", async () => {
    const { el, click, answer } = await open(
      game(
        decision(
          'CARD_ACTION_CHOICE',
          param('cardId', '4', '4') +
            param('blueprintId', 'inPlay', 'inPlay') +
            param('actionId', '10', '11') +
            param('actionText', 'First ability', 'Second ability') +
            param('noEffect', 'false', 'limit'),
          'Play a Fellowship action',
        ),
      ),
    );
    const actions = () => el.querySelector('.gd-phone-sheet[data-sheet="actions"]');
    // A tap again after the bounce guard (SELECTION_DEBOUNCE_MS: a second tap within it is the finger bouncing)
    const tapCard = async () => {
      await new Promise((resolve) => setTimeout(resolve, SELECTION_DEBOUNCE_MS + 20));
      await click('[data-zone="support-player"] .gd-phone-slot > [data-card-id="4"]');
    };
    await tapCard();
    expect(actions()).not.toBeNull();
    // The action that would do nothing says why
    expect(el.querySelector('[data-action-id="11"] .gd-phone-menu__reason')?.textContent).toContain('limit reached');
    await click('.gd-phone-sheet[data-sheet="actions"] [data-phone="sheet-close"]');
    expect(actions()).toBeNull();
    await tapCard();
    await click('.gd-phone-sheet[data-sheet="actions"] .gd-phone-sheet__backdrop');
    expect(actions()).toBeNull();
    await tapCard();
    await click('[data-action-id="10"]');
    expect(answer()).toBe('10');
  });

  it('steps a number with the arrows only within its bounds, then answers it', async () => {
    const { el, click, answer } = await open(
      game(decision('INTEGER', param('min', '1') + param('max', '2') + param('defaultValue', '1'), 'How many?')),
    );
    const shown = () => el.querySelector('[data-decision="integer"]')?.textContent?.trim();
    await click('[data-decision="integer-down"]');
    expect(shown()).toBe('1');
    await click('[data-decision="integer-up"]');
    await click('[data-decision="integer-up"]');
    expect(shown()).toBe('2');
    await click('[data-decision-button="OK"]');
    expect(answer()).toBe('2');
  });

  it('answers a choice with its button, or from a list with OK when the player wants lists', async () => {
    const choice = () => game(decision('MULTIPLE_CHOICE', param('results', 'Yes', 'No'), 'Go on?'));
    const buttons = await open(choice());
    await buttons.click('[data-decision-choice="1"]');
    expect(buttons.answer()).toBe('1');
    TestBed.resetTestingModule();
    document.cookie = 'alwaysDropDown=true; path=/';
    try {
      const list = await open(choice());
      const select = list.el.querySelector<HTMLSelectElement>('select[data-decision="choice"]');
      expect(select).not.toBeNull();
      if (select) {
        select.selectedIndex = 1;
        select.dispatchEvent(new Event('change'));
      }
      await list.click('[data-decision-button="OK"]');
      expect(list.answer()).toBe('1');
    } finally {
      document.cookie = 'alwaysDropDown=; path=/; max-age=0';
    }
  });

  it('brings a hidden hand back from the keyboard on the divider, and hides it again', async () => {
    document.cookie = 'gempGameSplit=phone:hidden; path=/';
    const { el, fixture, stable } = await open(game());
    const phone = fixture.debugElement.injector.get(PhoneSettings);
    // Hidden in a game: the divider says the hand is there to drag up
    expect(el.querySelector('.gd-phone-divider__hand-hint')?.textContent).toContain('Hand');
    const divider = el.querySelector<HTMLElement>('gd-phone-divider');
    const key = async (key: string) => {
      divider?.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true }));
      await stable();
    };
    await key('Enter');
    expect(phone.handHidden()).toBe(true);
    await key('ArrowUp');
    expect(phone.handHidden()).toBe(false);
    await key('ArrowDown');
    expect(phone.handHidden()).toBe(true);
  });

  it("shows a skirmish's strengths between its sides, with a damage bonus and who is overwhelmed", async () => {
    const stats =
      '<ge type="GS" moveCount="1" moveLimit="2" initiative="FREE_PEOPLE" ruleof4="-1" fellowshipStrength="7" ' +
      'shadowStrength="15" fellowshipDamageBonus="1" shadowDamageBonus="0" fpOverwhelmed="true" ' +
      'shadowOverwhelmed="false"/>';
    const { el } = await open(game('<ge type="SS" cardId="1" otherCardIds="5"/>' + stats));
    const strength = (side: string) => el.querySelector(`.gd-phone-pair .gd-phone-strength[data-side="${side}"]`);
    expect(strength('free-peoples')?.textContent?.trim()).toBe('7 +1');
    expect(strength('free-peoples')?.hasAttribute('data-overwhelmed')).toBe(true);
    // No bonus of 0 shown
    expect(strength('shadow')?.textContent?.trim()).toBe('15');
    expect(strength('shadow')?.hasAttribute('data-overwhelmed')).toBe(false);
    expect(el.querySelector('[data-zone="pairs"] .gd-phone-zone__label')?.textContent).toContain('Skirmish');
  });

  it("opens the player's adventure deck from an empty place on the road", async () => {
    const { el, click, sheet } = await open(game());
    await click('.gd-phone-site[data-site="4"] .gd-phone-site__empty');
    expect(sheet()?.getAttribute('data-sheet')).toBe('piles');
    expect(el.querySelector('.gd-phone-sheet[data-sheet="piles"] [data-pile="adventureDeck"]')).not.toBeNull();
  });

  it("shows a revealed hand among a player's piles, and one's own draw deck once the game has ended", async () => {
    const zones =
      '<ge type="GS" moveCount="1" moveLimit="2" initiative="FREE_PEOPLE" ruleof4="-1">' +
      '<playerZones name="alice" HAND="2" DISCARD="0" ADVENTURE_DECK="8" DEAD="1" DECK="50" REMOVED="0"/>' +
      '<playerZones name="bob" HAND="8" DISCARD="0" ADVENTURE_DECK="9" DEAD="0" DECK="52" REMOVED="0" ' +
      'handRevealed="true"/></ge>';
    const piles = (el: HTMLElement) =>
      Array.from(el.querySelectorAll('.gd-phone-sheet[data-sheet="piles"] [data-pile]'), (pile) =>
        pile.getAttribute('data-pile'),
      );
    const { el, click } = await open(game(zones));
    await click('.gd-phone-player[data-player="bob"]');
    expect(piles(el)).toContain('revealedHand');
    expect(piles(el)).not.toContain('deck');
    TestBed.resetTestingModule();
    const ended = await open(game(zones + '<ge type="EG"/>'));
    await ended.click('.gd-phone-player[data-player="alice"]');
    expect(piles(ended.el)).toContain('deck');
  });

  it('shows the archery totals the server gives, first among the pills; none outside archery', async () => {
    const stats = (fp: string, shadow: string) =>
      `<ge type="GS" fellowshipArchery="${fp}" shadowArchery="${shadow}" moveCount="1" moveLimit="2" ` +
      'initiative="FREE_PEOPLE" ruleof4="-1"/>';
    const { el } = await open(game(stats('2', '3')));
    const pills = () =>
      Array.from(el.querySelectorAll('.gd-phone-status__pills:not([hidden]) .gd-phone-pill'), (pill) => [
        pill.getAttribute('data-pill'),
        pill.textContent?.trim(),
        pill.querySelector('.gd-phone-pill__icon') !== null,
      ]);
    expect(pills().slice(0, 2)).toEqual([
      ['fp-archery', 'FP 2', true],
      ['shadow-archery', 'Sh 3', true],
    ]);
    TestBed.resetTestingModule();
    const outside = await open(game(stats('null', 'null')));
    expect(outside.el.querySelector('[data-pill$="-archery"]')).toBeNull();
  });

  it("shows the game's summary in Game info rendered, not as HTML tags", async () => {
    const pgs =
      '<ge type="PGS" participantId="alice" allParticipantIds="alice,bob" ' +
      'summary="&lt;b&gt;Format:&lt;/b&gt; Fellowship block&lt;br&gt;Timer: 80 minutes" notes="" maps="" metaSites=""/>';
    const { el, click } = await open(game(pgs));
    await click('[data-phone="menu"]');
    await click('[data-menu="info"]');
    const facts = Array.from(el.querySelectorAll('.gd-phone-sheet[data-sheet="info"] .gd-phone-facts dt'));
    const summary = facts.find((dt) => dt.textContent === 'Game')?.nextElementSibling;
    expect(summary?.querySelector('b')?.textContent).toBe('Format:');
    expect(summary?.querySelector('br')).not.toBeNull();
    expect(summary?.textContent).toBe('Format: Fellowship blockTimer: 80 minutes');
  });

  it("switches the theme from the menu's red first item, as the deck builder does; the piles are on the chips", async () => {
    const { el, click } = await open(game());
    // No red button in the status bar
    expect(el.querySelector('gd-phone-status [data-icon="theme"]')).toBeNull();
    await click('[data-phone="menu"]');
    const items = Array.from(el.querySelectorAll('.gd-phone-menu__item'), (item) => item.getAttribute('data-menu'));
    expect(items[0]).toBe('theme');
    expect(items).not.toContain('own-piles');
    expect(items).not.toContain('opponent-piles');
    expect(el.querySelector('[data-menu="theme"]')?.textContent?.trim()).toBe('Switch to the Classic theme');
    const select = vi.spyOn(TestBed.inject(ThemeService), 'select').mockImplementation(() => undefined);
    await click('[data-menu="theme"]');
    expect(select).toHaveBeenCalledWith('classic');
  });

  it('puts the game in full screen from the menu, which closes', async () => {
    const { el, click, sheet } = await open(game());
    await click('[data-phone="menu"]');
    expect(el.querySelector('[data-menu="fullscreen"]')?.textContent.trim()).toBe('Full screen');
    await click('[data-menu="fullscreen"]');
    expect(cues.toggleFullscreen).toHaveBeenCalled();
    expect(sheet()).toBeNull();
  });

  it('opens a card on a long press; sheets stack and go back one at a time, by the button and Escape', async () => {
    const { el, click, longPress, sheet, stable } = await open(game());
    await click('.gd-phone-player[data-player="alice"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('piles');
    expect(el.querySelector('[data-pile="deadPile"] [data-card-id="9"]')).not.toBeNull();
    await longPress('[data-pile="deadPile"] [data-card-id="9"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('card');
    expect(sheet()?.getAttribute('data-depth')).toBe('2');
    expect(el.querySelector('[data-phone="sheet-close"]')?.hasAttribute('data-back')).toBe(true);
    await click('[data-phone="sheet-close"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('piles');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await stable();
    expect(sheet()).toBeNull();
  });

  it("closes a card's details with a swipe down, as their corner button does: back, then closed", async () => {
    const { el, click, longPress, sheet, stable } = await open(game());
    /** A finger on the details, `down` px further down when it lifts (`sideways` px across). */
    const swipe = async (down: number, sideways = 0) => {
      const panel = el.querySelector('.gd-phone-sheet[data-sheet="card"] .gd-phone-sheet__panel');
      if (!panel) throw new Error('no details open');
      const touch = (y: number, x = 100) => ({ clientX: x, clientY: y });
      const event = (type: string, touches: object[], changed: object[]) =>
        Object.assign(new Event(type, { bubbles: true }), { touches, changedTouches: changed });
      panel.dispatchEvent(event('touchstart', [touch(200)], [touch(200)]));
      panel.dispatchEvent(event('touchend', [], [touch(200 + down, 100 + sideways)]));
      await stable();
    };
    // Over the piles: the swipe goes back to them, as ‹
    await click('.gd-phone-player[data-player="alice"]');
    await longPress('[data-pile="deadPile"] [data-card-id="9"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('card');
    // Too short, or more sideways than down: nothing
    await swipe(30);
    await swipe(90, 120);
    expect(sheet()?.getAttribute('data-sheet')).toBe('card');
    await swipe(120);
    expect(sheet()?.getAttribute('data-sheet')).toBe('piles');
    // Alone: the swipe closes them, as ✕; on another sheet it does nothing
    await click('[data-phone="sheet-close"]');
    await longPress('[data-zone="characters-player"] .gd-phone-slot > [data-card-id="1"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('card');
    await swipe(120);
    expect(sheet()).toBeNull();
  });

  it('goes back from the settings to the menu with a swipe down, and closes the menu with another', async () => {
    const { el, click, sheet, stable } = await open(game());
    const swipe = async () => {
      const panel = el.querySelector('.gd-phone-sheet .gd-phone-sheet__panel');
      if (!panel) throw new Error('no sheet open');
      const event = (type: string, y: number) =>
        Object.assign(new Event(type, { bubbles: true }), {
          touches: type === 'touchstart' ? [{ clientX: 100, clientY: y }] : [],
          changedTouches: [{ clientX: 100, clientY: y }],
        });
      panel.dispatchEvent(event('touchstart', 200));
      panel.dispatchEvent(event('touchend', 320));
      await stable();
    };
    await click('[data-phone="menu"]');
    await click('[data-menu="settings"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('settings');
    await swipe();
    expect(sheet()?.getAttribute('data-sheet')).toBe('menu');
    await swipe();
    expect(sheet()).toBeNull();
    // The chat keeps its touches
    await click('[data-phone="chat"]');
    await swipe();
    expect(sheet()?.getAttribute('data-sheet')).toBe('chat');
  });

  it('closes the piles and the game info with a swipe down too', async () => {
    const { el, click, sheet, stable } = await open(game());
    const swipe = async () => {
      const panel = el.querySelector('.gd-phone-sheet .gd-phone-sheet__panel');
      if (!panel) throw new Error('no sheet open');
      const event = (type: string, y: number) =>
        Object.assign(new Event(type, { bubbles: true }), {
          touches: type === 'touchstart' ? [{ clientX: 100, clientY: y }] : [],
          changedTouches: [{ clientX: 100, clientY: y }],
        });
      panel.dispatchEvent(event('touchstart', 200));
      panel.dispatchEvent(event('touchend', 320));
      await stable();
    };
    await click('.gd-phone-player[data-player="alice"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('piles');
    await swipe();
    expect(sheet()).toBeNull();
    await click('[data-phone="menu"]');
    await click('[data-menu="info"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('info');
    await swipe();
    expect(sheet()?.getAttribute('data-sheet')).toBe('menu');
  });

  it("opens no menu of the phone's on a long press over a card's picture in a sheet; text keeps it", async () => {
    const { el, longPress } = await open(game());
    await longPress('[data-zone="characters-player"] .gd-phone-slot > [data-card-id="1"]');
    const menu = (target: Element) => {
      const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
      target.dispatchEvent(event);
      return event.defaultPrevented;
    };
    expect(menu(el.querySelector('.gd-phone-detail__image')!)).toBe(true);
    expect(menu(el.querySelector('.gd-phone-sheet__head h2')!)).toBe(false);
  });

  it("shows a card's details and the cards under it, whose details open over it", async () => {
    const { el, longPress, sheet, click } = await open(game());
    await longPress('[data-zone="characters-player"] .gd-phone-slot > [data-card-id="1"]');
    expect(sheet()?.getAttribute('data-sheet')).toBe('card');
    // What affects the card, asked of the server
    backend.match((r) => r.url.includes('/cardInfo'));
    expect(el.querySelector('.gd-phone-sheet [data-card-id="2"]')).not.toBeNull();
    await longPress('.gd-phone-sheet [data-card-id="2"]');
    expect(sheet()?.getAttribute('data-depth')).toBe('2');
    await click('.gd-phone-sheet__backdrop');
    expect(sheet()?.getAttribute('data-depth')).toBe('1');
  });

  it("shows cards offered from off the table in the hand's place; ✕ goes back to the hand", async () => {
    const { el, click } = await open(
      game(
        decision(
          'ARBITRARY_CARDS',
          param('cardId', 'a', 'b') +
            param('blueprintId', '1_1', '1_2') +
            param('selectable', 'true', 'true') +
            param('min', '1') +
            param('max', '1'),
          'Choose a card from your deck',
        ),
      ),
    );
    const hand = () => el.querySelector('gd-phone-hand');
    expect(hand()?.getAttribute('data-place')).toBe('choices');
    expect(hand()?.querySelector('.gd-phone-hand__label')?.textContent).toContain('Choose a card from your deck');
    expect(hand()?.querySelectorAll('.gd-card')).toHaveLength(2);
    await click('[data-phone="close-choices"]');
    expect(hand()?.getAttribute('data-place')).toBe('hand');
    await click('[data-phone="show-choices"]');
    expect(hand()?.getAttribute('data-place')).toBe('choices');
  });

  it('scrolls offered cards down in rows when the player chooses it in the settings; the hand stays sideways', async () => {
    const { el, click, fixture, stable } = await open(
      game(
        decision(
          'ARBITRARY_CARDS',
          param('cardId', 'a', 'b') +
            param('blueprintId', '1_1', '1_2') +
            param('selectable', 'true', 'true') +
            param('min', '1') +
            param('max', '1'),
          'Choose a card from your deck',
        ),
      ),
    );
    const phone = fixture.debugElement.injector.get(PhoneSettings);
    const hand = () => el.querySelector('gd-phone-hand');
    // Sideways by default
    expect(hand()?.hasAttribute('data-scroll')).toBe(false);
    await click('[data-phone="menu"]');
    await click('[data-menu="settings"]');
    await click('[data-settings-tab="display"]');
    try {
      el.querySelector('[data-setting="choicesScroll"][value="down"]')?.dispatchEvent(new Event('change'));
      await stable();
      expect(phone.choicesScroll()).toBe('down');
      expect(hand()?.getAttribute('data-scroll')).toBe('down');
      // The player's own hand keeps scrolling sideways
      await click('[data-phone="sheet-close"]');
      await click('[data-phone="close-choices"]');
      expect(hand()?.getAttribute('data-place')).toBe('hand');
      expect(hand()?.hasAttribute('data-scroll')).toBe(false);
    } finally {
      for (const path of ['/', '/gemp-lotr']) document.cookie = `gempGameChoicesScroll=; path=${path}; max-age=0`;
    }
  });

  it('a tap acts only in a decision: an offered card is chosen, another does nothing', async () => {
    const { el, stable, answer } = await open(
      game(
        decision(
          'CARD_SELECTION',
          param('cardId', '6') + param('min', '1') + param('max', '1'),
          'Choose a card to discard',
        ),
      ),
    );
    el.querySelector<HTMLElement>('[data-card-id="7"]')!.click();
    await stable();
    expect(answer()).toBeNull();
    expect(el.querySelector('.gd-phone-sheet')).toBeNull();
    el.querySelector<HTMLElement>('gd-phone-hand [data-card-id="6"]')!.click();
    await stable();
    expect(answer()).toBe('6');
  });

  it('shows offered cards under a card smaller, before the hand, after a copy of that card; a tap chooses', async () => {
    // A minion stacked on a possession (11 on 4) and a card attached to a companion (2 on 1), both offered
    const { el, stable, answer } = await open(
      game(
        card('11', 'STACKED', 'targetCardId="4" targetType="stacked"', 'alice', '1_121') +
          decision(
            'CARD_SELECTION',
            param('cardId', '11', '2') + param('min', '1') + param('max', '1'),
            'Choose a card',
          ),
      ),
    );
    const hand = el.querySelector('gd-phone-hand')!;
    const hosts = Array.from(hand.querySelectorAll('.gd-phone-cards__host'), (host) => [
      host.getAttribute('data-host'),
      host.querySelector('.gd-phone-cards__host-label')?.textContent?.trim(),
    ]);
    expect(hosts).toEqual([
      ['1', 'Attached to'],
      ['4', 'Stacked on'],
    ]);
    const offered = Array.from(hand.querySelectorAll('[data-from-under] > .gd-card'), (c) => [
      c.getAttribute('data-card-id'),
      c.getAttribute('data-mark'),
    ]);
    expect(offered).toEqual([
      ['2', 'selectable'],
      ['11', 'selectable'],
    ]);
    // First, then the hand's own cards after a split; the small copies on the table stay lit
    const order = Array.from(
      hand.querySelectorAll('.gd-phone-cards__host, .gd-phone-slot > .gd-card, [data-offers-end]'),
      (el) => el.getAttribute('data-host') ?? el.getAttribute('data-card-id') ?? 'split',
    );
    expect(order).toEqual(['1', '2', '4', '11', 'split', '6', '7']);
    expect(
      el.querySelector('gd-phone-table .gd-phone-slot > [data-under][data-card-id="11"]')?.getAttribute('data-mark'),
    ).toBe('selectable');
    hand.querySelector<HTMLElement>('[data-from-under] > [data-card-id="11"]')!.click();
    await stable();
    expect(answer()).toBe('11');
  });

  it('keeps the hand where the player scrolled it when a new decision offers cards from under a card', async () => {
    const { el, stable } = await open(game(card('11', 'STACKED', 'targetCardId="4" targetType="stacked"')));
    const scroller = el.querySelector<HTMLElement>('gd-phone-hand .gd-phone-cards__scroll')!;
    // The player scrolled the hand right
    let left = 300;
    Object.defineProperty(scroller, 'scrollLeft', { get: () => left, set: (value: number) => (left = value) });
    await new Promise((resolve) => setTimeout(resolve, 150));
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(
        `<update cn="2">${decision('CARD_ACTION_CHOICE', param('actionId', '1') + param('cardId', '11') + param('blueprintId', 'inPlay') + param('actionText', 'Play'))}</update>`,
      );
    await stable();
    expect(el.querySelector('gd-phone-hand [data-from-under] > [data-card-id="11"]')).not.toBeNull();
    // Never scrolled for them (the owner, 2026-11-17: after a card was played the hand jumped to its start)
    expect(left).toBe(300);
  });

  it('shows no cards from under a card in the hand when the decision offers none of them', async () => {
    const { el } = await open(game());
    expect(el.querySelector('gd-phone-hand .gd-phone-cards__host')).toBeNull();
    expect(el.querySelector('gd-phone-hand [data-from-under]')).toBeNull();
  });

  it('keeps the sheets and the hand place with the game, so a reload shows them again (D10)', async () => {
    const first = await open(game());
    await first.click('[data-phone="menu"]');
    await first.click('[data-menu="settings"]');
    await first.click('[data-settings-tab="display"]');
    first.fixture.destroy();
    TestBed.resetTestingModule();
    const again = await open(game());
    expect(again.sheet()?.getAttribute('data-sheet')).toBe('settings');
    expect(again.sheet()?.getAttribute('data-depth')).toBe('2');
    expect(again.el.querySelector('[data-settings-tab="display"]')?.getAttribute('aria-selected')).toBe('true');
    await again.click('[data-phone="sheet-close"]');
    expect(again.sheet()?.getAttribute('data-sheet')).toBe('menu');
  });

  describe("who may see a player's piles (the server's rules: dead and removed public, the adventure deck its owner's, the discard pile its owner's unless the format makes it public)", () => {
    /** Cards of both players in every pile; the server sends an opponent's discard only in a public format, a page gets it anyway here. */
    const PILES = (discardPublic: boolean, viewer = 'alice') =>
      `<gameState cn="1"><ge type="P" participantId="${viewer}" allParticipantIds="alice,bob" discardPublic="${discardPublic}"/>` +
      ['alice', 'bob']
        .flatMap((owner, o) =>
          ['DISCARD', 'DEAD', 'REMOVED', 'ADVENTURE_DECK'].map((zone, z) => card(`${o}${z}`, zone, '', owner)),
        )
        .join('') +
      '<ge type="GPC" phase="Fellowship"/></gameState>';
    const shown = (el: HTMLElement, pile: string) => {
      const section = el.querySelector(`[data-pile="${pile}"]`);
      return section?.querySelector('gd-phone-cards') ? section.querySelectorAll('.gd-card').length : 'hidden';
    };
    const piles = async (body: string, player: string) => {
      const page = await open(body);
      await page.click(`.gd-phone-player[data-player="${player}"]`);
      const el = page.el;
      return {
        discard: shown(el, 'discard'),
        dead: shown(el, 'deadPile'),
        removed: shown(el, 'removedPile'),
        adventure: shown(el, 'adventureDeck'),
      };
    };

    it("hides the opponent's discard pile and adventure deck where the format keeps the discard private", async () => {
      expect(await piles(PILES(false), 'bob')).toEqual({ discard: 'hidden', dead: 1, removed: 1, adventure: 'hidden' });
    });

    it("shows the opponent's discard pile where the format makes it public (the PC formats), never their adventure deck", async () => {
      expect(await piles(PILES(true), 'bob')).toEqual({ discard: 1, dead: 1, removed: 1, adventure: 'hidden' });
    });

    it("shows a spectator no player's discard pile or adventure deck unless the format makes the discard public", async () => {
      for (const player of ['alice', 'bob']) {
        TestBed.resetTestingModule();
        expect(await piles(PILES(false, 'carol'), player)).toEqual({
          discard: 'hidden',
          dead: 1,
          removed: 1,
          adventure: 'hidden',
        });
        TestBed.resetTestingModule();
        expect((await piles(PILES(true, 'carol'), player)).discard).toBe(1);
      }
    });

    it('shows the player all their own piles, whatever the format', async () => {
      expect(await piles(PILES(false), 'alice')).toEqual({ discard: 1, dead: 1, removed: 1, adventure: 1 });
    });
  });

  it("folds the cards not active this turn: the player's shrink, the opponent's hide; the next turn folds again", async () => {
    // Alice's Free Peoples turn: her Shadow condition (3) and bob's companion (10) are not active
    const { el, click, stable } = await open(
      game(card('10', 'FREE_CHARACTERS', '', 'bob') + '<ge type="TC" participantId="alice" otherCardIds="3,10"/>'),
    );
    const slot = (id: string) => el.querySelector(`.gd-phone-slot:has(> [data-card-id="${id}"])`);
    const button = (row: string) => el.querySelector<HTMLElement>(`[data-zone="${row}"] button[data-fold]`);
    expect(slot('3')?.hasAttribute('data-shrunk')).toBe(true);
    expect(slot('4')?.hasAttribute('data-shrunk')).toBe(false);
    expect(button('support-player')?.getAttribute('data-fold')).toBe('shrink');
    expect(button('support-player')?.getAttribute('aria-expanded')).toBe('false');
    expect(slot('10')).toBeNull();
    expect(button('characters-opponent')?.textContent?.trim()).toBe('Show 1 ▸');
    // Hidden, the row is one line: its title, with the button on it, and no row of cards
    expect(button('characters-opponent')?.closest('.gd-phone-zone__label')).not.toBeNull();
    expect(el.querySelector('[data-zone="characters-opponent"] gd-phone-cards')).toBeNull();
    // The minions never fold, and rows of active cards have no button
    expect(button('shadow')).toBeNull();
    expect(button('characters-player')).toBeNull();

    await click('[data-zone="characters-opponent"] button[data-fold]');
    expect(slot('10')).not.toBeNull();
    expect(button('characters-opponent')?.textContent?.trim()).toBe('◂ Hide');
    expect(button('characters-opponent')?.closest('.gd-phone-zone__label')).not.toBeNull();
    await click('[data-zone="characters-opponent"] button[data-fold]');
    expect(slot('10')).toBeNull();
    await click('[data-zone="support-player"] .gd-phone-cards__fold');
    expect(slot('3')?.hasAttribute('data-shrunk')).toBe(false);
    expect(button('support-player')?.getAttribute('aria-expanded')).toBe('true');

    // Bob's Free Peoples turn: alice's Free Peoples cards shrink, the row opened before folds again
    await new Promise((resolve) => setTimeout(resolve, 150));
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush('<update cn="2"><ge type="TC" participantId="bob" otherCardIds="1,4,5"/></update>');
    await stable();
    expect(slot('4')?.hasAttribute('data-shrunk')).toBe(true);
    expect(slot('1')?.hasAttribute('data-shrunk')).toBe(true);
    expect(slot('3')?.hasAttribute('data-shrunk')).toBe(false);
    expect(button('support-player')?.getAttribute('aria-expanded')).toBe('false');
    expect(slot('10')).not.toBeNull();
  });

  it('buzzes and plays a sound when a decision comes, unless turned off', async () => {
    const { stable } = await open(game());
    await new Promise((resolve) => setTimeout(resolve, 150));
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(
        `<update cn="2">${decision('CARD_SELECTION', param('cardId', '6') + param('min', '1') + param('max', '1'))}</update>`,
      );
    await stable();
    expect(cues.vibrate).toHaveBeenCalled();
    expect(cues.play).toHaveBeenCalledWith('awaitAction');
  });
});
