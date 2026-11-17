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
          },
        },
        {
          provide: CompactLayout,
          useValue: {
            active: signal(compact),
            sideBySide: signal(false),
            device: signal(compact ? 'phone' : null),
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
    // The attached card: a small copy under its card
    expect(el.querySelector('[data-zone="characters-player"] .gd-phone-slot__under [data-card-id="2"]')).not.toBeNull();
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

  it('shows the archery totals the server gives, first among the pills; none outside archery', async () => {
    const stats = (fp: string, shadow: string) =>
      `<ge type="GS" fellowshipArchery="${fp}" shadowArchery="${shadow}" moveCount="1" moveLimit="2" ` +
      'initiative="FREE_PEOPLE" ruleof4="-1"/>';
    const { el } = await open(game(stats('2', '3')));
    const pills = () =>
      Array.from(el.querySelectorAll('.gd-phone-pill'), (pill) => [
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
      el.querySelector('gd-phone-table .gd-phone-slot__under [data-card-id="11"]')?.getAttribute('data-mark'),
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
    const button = (row: string) => el.querySelector<HTMLElement>(`[data-zone="${row}"] .gd-phone-cards__fold`);
    expect(slot('3')?.hasAttribute('data-shrunk')).toBe(true);
    expect(slot('4')?.hasAttribute('data-shrunk')).toBe(false);
    expect(button('support-player')?.getAttribute('data-fold')).toBe('shrink');
    expect(button('support-player')?.getAttribute('aria-expanded')).toBe('false');
    expect(slot('10')).toBeNull();
    expect(button('characters-opponent')?.textContent?.trim()).toBe('Show 1 ▸');
    // The minions never fold, and rows of active cards have no button
    expect(button('shadow')).toBeNull();
    expect(button('characters-player')).toBeNull();

    await click('[data-zone="characters-opponent"] .gd-phone-cards__fold');
    expect(slot('10')).not.toBeNull();
    expect(button('characters-opponent')?.textContent?.trim()).toBe('◂ Hide');
    await click('[data-zone="characters-opponent"] .gd-phone-cards__fold');
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
