import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { GameSettings } from '../../../core/game/settings/game-settings';
import { PageCues } from '../../../core/platform/page-cues';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { GamePageComponent } from '../game-page.component';
import { sentForm } from '../../../testing/sent-form';

const GAME = '/gemp-lotr-server/game/7';
const P = '<ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>';
const CARD_DECISION =
  '<ge type="D" id="3" decisionType="CARD_SELECTION" participantId="alice" text="Choose">' +
  '<parameter name="cardId" value="1"/><parameter name="min" value="1"/><parameter name="max" value="1"/></ge>';
const ARBITRARY =
  '<ge type="D" id="3" decisionType="ARBITRARY_CARDS" participantId="alice" text="Starting fellowship">' +
  '<parameter name="cardId" value="temp0"/><parameter name="blueprintId" value="1_7"/>' +
  '<parameter name="selectable" value="true"/><parameter name="min" value="0"/><parameter name="max" value="1"/></ge>';
const game = (decision = CARD_DECISION) =>
  `<gameState cn="1">${P}<ge type="PCIP" cardId="1" blueprintId="1_7" participantId="alice" zone="FREE_CHARACTERS"/>${decision}</gameState>`;

const policy = (asks = true, allowed = true) => ({
  green: allowed,
  yellow: allowed,
  red: allowed,
  asks,
  timeoutSeconds: 60,
  greenPerPhase: 3,
});
const green = { entryCount: 11, label: 'Played Gimli', turn: 0, phase: 'Play starting fellowship', tier: 'GREEN' };
const yellow = {
  entryCount: 10,
  label: 'Bid 0',
  turn: 0,
  phase: 'Play starting fellowship',
  tier: 'YELLOW',
  reasons: ['Opponent replays 1 choice'],
  unavailable: null,
};
const red = {
  entryCount: 9,
  label: 'Pressed DONE',
  turn: 1,
  phase: 'Fellowship',
  tier: 'RED',
  reasons: ['Hidden information revealed', 'A shuffle'],
  unavailable: 'bob is auto-declining reverts',
};
const points = (list: unknown[], extra: Record<string, unknown> = {}) => ({
  points: list,
  policy: policy(),
  autoDecline: false,
  declining: [],
  pending: null,
  ...extra,
});
const request = (requester: string) => ({
  notices: [
    {
      number: 4,
      type: 'undoRequested',
      requestId: '8',
      requester,
      label: 'Pressed DONE',
      turn: '1',
      phase: 'Fellowship',
      tier: 'RED',
      reasons: 'Hidden information revealed; A shuffle',
      seconds: '57',
    },
  ],
  latest: 4,
});

describe('taking a game back on the game page', () => {
  let backend: HttpTestingController;

  beforeEach(() => sessionStorage.clear());

  /** Answers every question about what can be taken back now open (the list's, and a refresh's that came meanwhile). */
  const flushPoints = (answer: object) =>
    backend.match((r) => r.url === `${GAME}/restorePoints`).forEach((r) => r.flush(answer));
  afterEach(() => backend.match(() => true));

  /** The page opened with a decision waiting, undo started: `restore` is the answer about what can be taken back. */
  async function open(restore: object, decision = CARD_DECISION) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        {
          provide: PageCues,
          useValue: { reload: vi.fn(), go: vi.fn(), setTitle: vi.fn(), away: () => false, play: vi.fn() },
        },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(GamePageComponent);
    const el = fixture.nativeElement as HTMLElement;
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(game(decision));
    const stable = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
      await fixture.whenStable();
    };
    await stable();
    backend.expectOne((r) => r.url === `${GAME}/notices`).flush({ notices: [], latest: 3 });
    backend.match((r) => r.url === `${GAME}/restorePoints`).forEach((r) => r.flush(restore));
    await stable();
    const notices = (): TestRequest => backend.expectOne((r) => r.url === `${GAME}/notices`);
    const click = async (selector: string) => {
      const target = el.querySelector<HTMLElement>(selector);
      if (!target) throw new Error(`nothing at ${selector}`);
      target.click();
      await stable();
    };
    const sent = (end: string) => backend.match((r) => r.url === `${GAME}/${end}`).map((r) => sentForm(r.request));
    return { fixture, el, stable, click, notices, sent };
  }

  it("shows the Undo button in the alert box's lower right while the last choice is the player's own", async () => {
    const { el, click, sent } = await open(points([green]));
    const button = el.querySelector<HTMLButtonElement>('gd-game-alert button[data-undo="last"]')!;
    expect(button.textContent?.trim()).toBe('✓ Undo');
    expect(button.title).toBe('Undo: Played Gimli');
    expect(button.disabled).toBe(false);
    expect(el.querySelector('gd-game-alert')?.hasAttribute('data-undo')).toBe(true);
    await click('[data-undo="last"]');
    const [body] = sent('undo');
    expect(body?.get('entryCount')).toBe('11');
    expect(body?.get('ownOnly')).toBe('true');
  });

  it('has no Undo button for a revert', async () => {
    const { el } = await open(points([yellow]));
    expect(el.querySelector('[data-undo="last"]')).toBeNull();
    expect(el.querySelector('gd-game-alert')?.hasAttribute('data-undo')).toBe(false);
  });

  it('lists the places from Revert... in the Options tab: headings, tiers, what was done, why, and what is off', async () => {
    const { el, click, sent, stable } = await open(points([green]));
    await click('[data-tab="options"]');
    expect(el.querySelector('[data-setting="undoAutoDecline"]')).not.toBeNull();
    await click('[data-button="undoGame"]');
    flushPoints(points([green, yellow, red]));
    await stable();
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="undo-list"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('Revert');
    expect(Array.from(dialog.querySelectorAll('[data-undo="when"]'), (h) => h.textContent)).toEqual([
      'Setup · Play starting fellowship',
      'Turn 1 · Fellowship',
    ]);
    const rows = Array.from(dialog.querySelectorAll<HTMLElement>('[data-undo="point"]'));
    expect(rows.map((row) => row.dataset['tier'])).toEqual(['GREEN', 'YELLOW', 'RED']);
    expect(rows.map((row) => row.querySelector('[data-undo="go"]')?.textContent?.trim())).toEqual([
      '✓ Undo',
      '~ Revert',
      '✕ Revert',
    ]);
    expect(rows.map((row) => row.querySelector('[data-undo="go"]')?.getAttribute('data-look'))).toEqual([
      'own',
      'replays',
      'revealed',
    ]);
    expect(rows[0]?.querySelector('[data-undo="label"]')?.textContent).toBe('Played Gimli');
    expect(rows[0]?.querySelector('[data-undo="why"]')).toBeNull();
    expect(Array.from(rows[2]!.querySelectorAll('[data-undo="why"] > div > b'), (b) => b.textContent)).toEqual([
      'Hidden information revealed',
      'A shuffle',
    ]);
    expect(rows[2]?.querySelector('[data-undo="off"]')?.textContent).toBe('bob is auto-declining reverts');
    expect(rows[2]?.querySelector<HTMLButtonElement>('[data-undo="go"]')?.disabled).toBe(true);
    expect(dialog.querySelector('[data-undo="note"]')).toBeNull();

    // The "i" button explains the tiers
    const help = dialog.querySelector<HTMLElement>('[data-undo="help"]')!;
    expect(help.hidden).toBe(true);
    await click('dialog[data-dialog="undo-list"] [data-undo="info"]');
    expect(help.hidden).toBe(false);
    expect(help.textContent?.replace(/\s+/g, ' ')).toContain(
      'Revert. Your opponent replays choices. Needs their approval.',
    );

    await click('dialog[data-dialog="undo-list"] [data-undo="point"]:nth-of-type(3) [data-undo="go"]');
    expect(sent('undo').map((body) => [body?.get('entryCount'), body?.get('ownOnly')])).toEqual([['10', 'false']]);
  });

  it('says in the list when there is nothing to go back to, or a request is pending; it closes with its button', async () => {
    const { el, click, stable } = await open(points([]));
    await click('[data-tab="options"]');
    await click('[data-button="undoGame"]');
    flushPoints(points([]));
    await stable();
    expect(el.querySelector('[data-undo="note"]')?.textContent).toBe('Nothing to go back to.');
    expect(el.querySelector('[data-undo="points"]')).toBeNull();
    await click('dialog[data-dialog="undo-list"] .gd-dialog__close');
    expect(el.querySelector<HTMLDialogElement>('dialog[data-dialog="undo-list"]')?.open).toBe(false);
    await click('[data-button="undoGame"]');
    flushPoints(points([yellow], { pending: { requestId: 1, requester: 'bob', tier: 'RED', secondsLeft: 9 } }));
    await stable();
    expect(el.querySelector('[data-undo="note"]')?.textContent).toBe('A revert request is pending.');
    expect(el.querySelector('[data-undo="points"]')).toBeNull();
  });

  it('has the switch to decline every request beside Revert only where anybody is asked; it says so to the server', async () => {
    const { el, click, sent } = await open(points([], { policy: policy(false) }));
    await click('[data-tab="options"]');
    expect(el.querySelector('[data-button="undoGame"]')).not.toBeNull();
    expect(el.querySelector('[data-setting="undoAutoDecline"]')).toBeNull();
    TestBed.resetTestingModule();
    const page = await open(points([], { autoDecline: true }));
    await page.click('[data-tab="options"]');
    const toggle = page.el.querySelector<HTMLInputElement>('[data-setting="undoAutoDecline"]')!;
    expect(toggle.checked).toBe(true);
    toggle.checked = false;
    toggle.dispatchEvent(new Event('change'));
    await page.stable();
    expect(page.sent('autoDecline').map((body) => body?.get('on'))).toEqual(['false']);
    expect(sent('autoDecline')).toEqual([]);
  });

  it('has no Revert... where nothing can be taken back', async () => {
    const { el, click } = await open(points([], { policy: policy(false, false) }));
    await click('[data-tab="options"]');
    expect(el.querySelector('[data-button="undoGame"]')).toBeNull();
    expect(el.querySelector('[data-button="concedeGame"]')).not.toBeNull();
  });

  it("shows the opponent's request with no default: the dialog has the focus, Approve first, the i after them", async () => {
    const { el, notices, stable, click, sent } = await open(points([green]));
    notices().flush(request('bob'));
    await stable();
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="undo-request"]')!;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('.gd-dialog__close')).toBeNull();
    expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('Revert request');
    expect(dialog.querySelector('[data-undo="head"]')?.textContent?.trim()).toBe('bob asks to revert to before:');
    expect(dialog.querySelector('[data-undo="request"]')?.getAttribute('data-tier')).toBe('RED');
    expect(dialog.querySelector('[data-undo="target"] [data-undo="mark"]')?.textContent).toBe('✕');
    expect(dialog.querySelector('[data-undo="target"] [data-undo="label"]')?.textContent).toBe('Pressed DONE');
    expect(dialog.querySelector('[data-undo="target"] [data-undo="when"]')?.textContent).toBe('Turn 1 · Fellowship');
    expect(Array.from(dialog.querySelectorAll('[data-undo="why"] b'), (b) => b.textContent)).toEqual([
      'Hidden information revealed',
      'A shuffle',
    ]);
    expect(dialog.querySelector('[data-undo="clock"]')?.textContent).toBe('Game paused · auto-declines in 57s');
    const buttons = Array.from(dialog.querySelectorAll<HTMLButtonElement>('button'));
    expect(buttons.map((b) => b.textContent?.trim())).toEqual(['Approve', 'Decline', 'Decline all', 'i']);
    expect(buttons[2]?.title).toBe('Decline this and every later revert request in this game');
    expect(document.activeElement).toBe(dialog);
    // While it waits, nothing in the game can be answered; the Undo button is off
    expect(el.querySelector('[data-game="alert-buttons"]')?.hasAttribute('inert')).toBe(true);
    expect(el.querySelector<HTMLButtonElement>('[data-undo="last"]')?.disabled).toBe(true);
    await click('[data-undo-reply="approve"]');
    expect(sent('undoReply').map((body) => [body?.get('requestId'), body?.get('reply')])).toEqual([['8', 'approve']]);
  });

  it("dims a decision's dialog while a request waits; the asker may withdraw", async () => {
    const { el, notices, stable, click, sent } = await open(points([]), ARBITRARY);
    const decision = el.querySelector<HTMLDialogElement>('dialog[data-dialog="decision-cards"]')!;
    expect(decision.hasAttribute('inert')).toBe(false);
    notices().flush(request('alice'));
    await stable();
    expect(decision.hasAttribute('inert')).toBe(true);
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog="undo-request"]')!;
    expect(dialog.querySelector('[data-dialog-title]')?.textContent).toBe('Revert requested');
    expect(dialog.querySelector('[data-undo="head"]')?.textContent?.trim()).toBe('You asked to revert to before:');
    expect(Array.from(dialog.querySelectorAll('button[data-undo-reply]'), (b) => b.textContent?.trim())).toEqual([
      'Withdraw',
    ]);
    await click('[data-undo-reply="cancel"]');
    expect(sent('undoReply').map((body) => body?.get('reply'))).toEqual(['cancel']);
    // Resolved: it goes, and the decision can be answered again
    notices().flush({ notices: [{ number: 5, type: 'undoResolved', outcome: 'cancelled', message: 'Withdrawn' }] });
    await stable();
    expect(dialog.open).toBe(false);
    expect(decision.hasAttribute('inert')).toBe(false);
    // ...and the banner says what happened; pressed, it goes
    expect(el.querySelector('[data-undo="banner"]')?.textContent?.trim()).toBe('Withdrawn');
    await click('[data-undo="banner"]');
    expect(el.querySelector('[data-undo="banner"]')).toBeNull();
  });

  it('after the game was taken back, reads it again in place and says so with the tier', async () => {
    const { el, notices, stable } = await open(points([green]));
    notices().flush({
      notices: [{ number: 5, type: 'resync', reason: 'revert', message: 'alice undid: Played Gimli', tier: 'GREEN' }],
      latest: 5,
    });
    await stable();
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(game());
    await stable();
    const banner = el.querySelector('[data-undo="banner"]')!;
    expect(banner.querySelector('[data-undo="mark"]')?.getAttribute('data-tier')).toBe('GREEN');
    expect(banner.textContent?.replace(/\s+/g, ' ').trim()).toBe('✓ alice undid: Played Gimli');
  });

  it('has the "Skip extra confirmations" setting, off unless turned on', async () => {
    const { el, click, fixture, stable } = await open(points([]));
    await click('[data-tab="settings"]');
    const box = el.querySelector<HTMLInputElement>('[data-setting="skipConfirmations"]')!;
    expect(box.checked).toBe(false);
    box.checked = true;
    box.dispatchEvent(new Event('change'));
    await stable();
    expect(fixture.debugElement.injector.get(GameSettings).skipConfirmations()).toBe(true);
  });
});
