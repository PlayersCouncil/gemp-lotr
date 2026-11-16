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
  let cues: { vibrate: ReturnType<typeof vi.fn>; play: ReturnType<typeof vi.fn> };

  beforeEach(() => sessionStorage.clear());
  afterEach(() => backend.match(() => true));

  /** The page with the compact layout on (the modern theme on a phone: CompactLayout) or off. */
  async function open(body: string, { compact = true } = {}) {
    cues = { vibrate: vi.fn(), play: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        {
          provide: PageCues,
          useValue: { ...cues, setTitle: vi.fn(), away: () => false, reload: vi.fn(), go: vi.fn() },
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
