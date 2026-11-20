import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { GameSettings } from '../../../core/game/settings/game-settings';
import { PageCues } from '../../../core/platform/page-cues';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { GamePageComponent } from '../game-page.component';
import { choiceLabel } from './decision-dialogs.component';

const GAME = '/gemp-lotr-server/game/7';
const param = (name: string, ...values: string[]) =>
  values.map((value) => `<parameter name="${name}" value="${value}"/>`).join('');
const card = (id: string, zone = 'FREE_CHARACTERS') =>
  `<ge type="PCIP" cardId="${id}" blueprintId="1_7" participantId="alice" zone="${zone}"/>`;
const game = (decision: string, events = '') =>
  `<gameState cn="1"><ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>` +
  `${card('1')}${card('2')}${events}${decision}</gameState>`;
const d = (type: string, params: string, text = 'Do it') =>
  `<ge type="D" id="3" decisionType="${type}" participantId="alice" text="${text}">${params}</ge>`;

describe('the decisions on the game page', () => {
  let backend: HttpTestingController;
  let cues: { reload: ReturnType<typeof vi.fn>; go: ReturnType<typeof vi.fn> };

  beforeEach(() => sessionStorage.clear());
  afterEach(() => backend.match(() => true));

  async function open(body: string, status = 200) {
    cues = { reload: vi.fn(), go: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        { provide: PageCues, useValue: { ...cues, setTitle: vi.fn(), away: () => false, play: vi.fn() } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(GamePageComponent);
    const el = fixture.nativeElement as HTMLElement;
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(body, { status, statusText: status === 200 ? 'OK' : 'Error' });
    const stable = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
    };
    await stable();
    const click = async (selector: string) => {
      el.querySelector<HTMLElement>(selector)!.click();
      await stable();
    };
    const answer = () =>
      backend
        .match((r) => r.url === GAME && r.method === 'POST' && r.body?.has('decisionId'))[0]
        ?.request.body?.get('decisionValue') ?? null;
    return { fixture, el, stable, click, answer };
  }

  it('asks for a number with the spinner, between its limits, and sends it with OK', async () => {
    const { el, click, answer } = await open(game(d('INTEGER', param('min', '0') + param('max', '2'))));
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="decision"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('.gd-game-decision__text')?.textContent).toBe('Do it');
    expect(dialog.querySelector('.gd-dialog__close')).toBeNull();
    const value = () => dialog.querySelector('[data-decision="integer"]')?.textContent;
    expect(value()).toBe('0');
    await click('[data-decision="integer-down"]');
    expect(value()).toBe('0');
    for (let i = 0; i < 3; i++) await click('[data-decision="integer-up"]');
    expect(value()).toBe('2');
    await click('[data-decision="integer-down"]');
    await click('[data-decision-button="OK"]');
    expect(answer()).toBe('1');
  });

  it('starts the spinner at its least when the default is out of its range', async () => {
    const { el } = await open(game(d('INTEGER', param('min', '2') + param('max', '4') + param('defaultValue', '9'))));
    expect(el.querySelector('[data-decision="integer"]')?.textContent).toBe('2');
  });

  it('answers one of two with its button, without the card names acting as links', async () => {
    const { el, click, answer } = await open(
      game(
        d('MULTIPLE_CHOICE', param('results', 'Go first', "&lt;div class='cardHint' value='1_1'&gt;Gimli&lt;/div&gt;")),
      ),
    );
    expect(el.querySelectorAll('[data-decision-choice]')).toHaveLength(2);
    expect(el.querySelector('[data-decision-choice="1"] .cardHint')).toBeNull();
    expect(el.querySelector('[data-decision-button="OK"]')).toBeNull();
    await click('[data-decision-choice="1"]');
    expect(answer()).toBe('1');
  });

  it('answers one of more from the list with OK', async () => {
    const { el, click, answer } = await open(game(d('MULTIPLE_CHOICE', param('results', 'a', 'b', 'c'))));
    const list = el.querySelector<HTMLSelectElement>('select[data-decision="choice"]')!;
    list.selectedIndex = 2;
    list.dispatchEvent(new Event('change'));
    await click('[data-decision-button="OK"]');
    expect(answer()).toBe('2');
  });

  it("shows the cards of a choice in a dialog, with the actions' texts, and its buttons as the choice grows", async () => {
    TestBed.resetTestingModule();
    const { el, click, answer, fixture } = await open(
      game(
        d(
          'ACTION_CHOICE',
          param('blueprintId', '1_1', '1_2') + param('actionId', '7', '8') + param('actionText', 'One', 'Two'),
          'Choose &lt;b&gt;one&lt;/b&gt;',
        ),
      ),
    );
    fixture.debugElement.injector.get(GameSettings).setAutoAccept(false);
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="decision-cards"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[data-dialog-title] b')?.textContent).toBe('one');
    expect(Array.from(dialog.querySelectorAll('.gd-game-decision__card-text'), (t) => t.textContent)).toEqual([
      'One',
      'Two',
    ]);
    expect(dialog.querySelector('[data-decision-button]')).toBeNull();
    await click('[data-card-id="temp1"]');
    expect(dialog.querySelector('[data-card-id="temp1"]')?.getAttribute('data-mark')).toBe('selected');
    await click('[data-decision-button="Clear selection"]');
    expect(dialog.querySelector('[data-card-id="temp1"]')?.getAttribute('data-mark')).toBe('selectable');
    await click('[data-card-id="temp0"]');
    await click('[data-decision-button="Done"]');
    expect(answer()).toBe('7');
  });

  it('frames the alert box for a choice of actions, with Pass, and opens the menu of a card with several', async () => {
    const { el, click, answer, stable } = await open(
      game(
        d(
          'CARD_ACTION_CHOICE',
          param('cardId', '1', '1', '9') +
            param('blueprintId', 'inPlay', 'inPlay', '1_9') +
            param('actionId', '10', '11', '12') +
            param('actionText', 'Use', 'Exert', 'Play'),
          'Play a &lt;b&gt;Fellowship&lt;/b&gt; action',
        ),
      ),
    );
    expect(el.querySelector('gd-game-alert')?.getAttribute('data-frame')).toBe('action');
    expect(el.querySelector('[data-game="alert"] b')?.textContent).toBe('Fellowship');
    expect(el.querySelector('[data-card-id="1"]')?.getAttribute('data-mark')).toBe('actionable');
    // The card with an action that isn't on the table: in the hand's row
    expect(el.querySelector('[data-row="hand"] [data-card-id="extra9"]')?.getAttribute('data-zone')).toBe('EXTRA');
    expect(el.querySelector('[data-decision-button="Pass"]')?.getAttribute('data-button-id')).toBe('Pass');
    el.querySelector<HTMLElement>('[data-card-id="1"]')!.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await stable();
    const menu = el.querySelector<HTMLElement>('.gd-game-menu');
    expect(menu).not.toBeNull();
    expect(Array.from(menu!.querySelectorAll('[data-action-id]'), (a) => a.textContent)).toEqual(['Use', 'Exert']);
    // A click elsewhere closes it
    document.body.click();
    await click('gd-game-alert');
    expect(el.querySelector('.gd-game-menu')).toBeNull();
    // A second click on the same card soon after the first is ignored (a bounce)
    await new Promise((resolve) => setTimeout(resolve, 250));
    el.querySelector<HTMLElement>('[data-card-id="1"]')!.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await stable();
    await click('[data-action-id="11"]');
    expect(answer()).toBe('11');
  });

  it('marks a card whose every action would do nothing, and says why in its menu (the new master)', async () => {
    const { el, stable } = await open(
      game(
        d(
          'CARD_ACTION_CHOICE',
          param('cardId', '1', '1', '2') +
            param('blueprintId', 'inPlay', 'inPlay', 'inPlay') +
            param('actionId', '10', '11', '12') +
            param('actionText', 'Use', 'Exert', 'Pump') +
            param('noEffect', 'limit', 'skirmish', 'false'),
          'Play a Skirmish action',
        ),
      ),
    );
    expect(el.querySelector('[data-card-id="1"]')?.hasAttribute('data-no-effect')).toBe(true);
    expect(el.querySelector('[data-card-id="2"]')?.hasAttribute('data-no-effect')).toBe(false);
    el.querySelector<HTMLElement>('[data-card-id="1"]')!.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await stable();
    const entries = Array.from(el.querySelectorAll<HTMLElement>('.gd-game-menu [data-action-id]'));
    // As game.html: the action's text, then why it would do nothing
    expect(entries.map((entry) => entry.textContent?.trim())).toEqual([
      'Use (limit reached)',
      'Exert (no one in this skirmish)',
    ]);
    expect(entries.map((entry) => entry.getAttribute('data-no-effect'))).toEqual(['limit', 'skirmish']);
  });

  it('shows the information of a card the decision offers on a right click, then on a shift click', async () => {
    const { el, stable } = await open(
      game(
        d(
          'CARD_ACTION_CHOICE',
          param('cardId', '4') + param('blueprintId', 'inPlay') + param('actionId', '10') + param('actionText', 'Play'),
          'Play',
        ),
        card('4', 'HAND'),
      ),
    );
    const hand = el.querySelector<HTMLElement>('[data-row="hand"] [data-card-id="4"]')!;
    const title = () => el.querySelector('gd-card-info [data-dialog-title]');
    hand.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true }));
    await stable();
    expect(title()).not.toBeNull();
    document.body.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    document.body.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    document.body.click();
    await stable();
    expect(title()).toBeNull();
    const press = (target: Element, button: number, shiftKey = false) => {
      for (const type of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', button === 0 ? 'click' : 'contextmenu']) {
        const event = new MouseEvent(type, {
          bubbles: true,
          cancelable: true,
          button,
          shiftKey,
          clientX: 5,
          clientY: 5,
        });
        Object.defineProperty(event, 'pointerType', { value: 'mouse' });
        Object.defineProperty(event, 'pointerId', { value: 1 });
        target.dispatchEvent(event);
      }
    };
    press(hand, 2);
    await stable();
    expect(title()).not.toBeNull();
    press(el.querySelector('[data-card-id="1"]')!, 0);
    await stable();
    expect(title()).toBeNull();
    press(hand, 0, true);
    await stable();
    expect(title()).not.toBeNull();
  });

  it("comes back after a reload with a card's open menu and the cards chosen but not sent", async () => {
    const state = game(
      d(
        'CARD_ACTION_CHOICE',
        param('cardId', '1', '1') +
          param('blueprintId', 'inPlay', 'inPlay') +
          param('actionId', '10', '11') +
          param('actionText', 'Use', 'Exert'),
      ),
    );
    const first = await open(state);
    first.el
      .querySelector<HTMLElement>('[data-card-id="1"]')!
      .dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await first.stable();
    expect(first.el.querySelector('.gd-game-menu')).not.toBeNull();
    first.fixture.destroy();
    backend.match(() => true);
    TestBed.resetTestingModule();

    const again = await open(state);
    expect(Array.from(again.el.querySelectorAll('[data-action-id]'), (a) => a.textContent)).toEqual(['Use', 'Exert']);
    // Another decision: the menu is gone
    again.fixture.destroy();
    backend.match(() => true);
    TestBed.resetTestingModule();
    const other = await open(
      game(d('CARD_SELECTION', param('cardId', '1', '2') + param('min', '2') + param('max', '2'))),
    );
    expect(other.el.querySelector('.gd-game-menu')).toBeNull();
    other.fixture.debugElement.injector.get(GameSettings).setAutoAccept(false);
    await other.click('[data-card-id="2"]');
    other.fixture.destroy();
    backend.match(() => true);
    TestBed.resetTestingModule();
    const chosen = await open(
      game(d('CARD_SELECTION', param('cardId', '1', '2') + param('min', '2') + param('max', '2'))),
    );
    expect(chosen.el.querySelector('[data-card-id="2"]')?.getAttribute('data-mark')).toBe('selected');
    expect(chosen.el.querySelector('[data-decision-button="Reset choice"]')).not.toBeNull();
  });

  it('assigns minions with the alert box framed for a choice, Done on its left', async () => {
    const { el, click } = await open(
      game(
        d('ASSIGN_MINIONS', param('freeCharacters', '1') + param('minions', '5')),
        '<ge type="PCIP" cardId="5" blueprintId="1_9" participantId="bob" zone="SHADOW_CHARACTERS"/>',
      ),
    );
    expect(el.querySelector('gd-game-alert')?.getAttribute('data-frame')).toBe('selection');
    expect(el.querySelector('[data-decision-button="Done"]')?.hasAttribute('data-right')).toBe(false);
    await click('[data-card-id="5"]');
    await click('[data-card-id="1"]');
    expect(el.querySelector('[data-card-id="5"]')?.getAttribute('data-assign')).toBe('1');
  });

  it('finishes a choice of cards with Escape', async () => {
    const { el, stable, answer } = await open(
      game(
        d(
          'ARBITRARY_CARDS',
          param('cardId', 'a') +
            param('blueprintId', '1_1') +
            param('selectable', 'true') +
            param('min', '0') +
            param('max', '1'),
        ),
      ),
    );
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await stable();
    expect(answer()).toBe('');
    expect(el).toBeTruthy();
  });

  it('shows what stops the game in a dialog with the way on', async () => {
    const { el, click, stable } = await open('', 409);
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="game-problem"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('Concurrent access error');
    await click('[data-problem-action="reload"]');
    expect(cues.reload).toHaveBeenCalled();
    dialog.querySelector<HTMLElement>('.gd-dialog__close')!.click();
    await stable();
    expect(dialog.open).toBe(false);
  });

  it('leads to the main page or the hall', async () => {
    const first = await open('', 401);
    await first.click('[data-problem-action="mainPage"]');
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/login');
    first.fixture.destroy();
    backend.match(() => true);
    TestBed.resetTestingModule();
    const second = await open('', 403);
    await second.click('[data-problem-action="hall"]');
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/');
  });
});

describe('choiceLabel', () => {
  it("takes the card names' link away and keeps them in the line", () => {
    expect(choiceLabel("a <div class='cardHint' value='1'>X</div>")).toBe(
      "a <div style='display:inline' class='' value='1'>X</div>",
    );
  });
});
