import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { AuthoringDecision, DraftState, OpponentState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { ORDERS_MEMORY_KEY } from '../../../../core/game/authoring/opponent-orders';
import { provideGameState } from '../../../../core/game/game-state';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { Notifier } from '../../../../core/notifier/notifier';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import {
  decision,
  draftGameXml,
  draftState,
  opponentOrder,
  opponentState,
  result,
} from '../../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { OpponentTabComponent } from '../opponent-tab.component';
import { sentForm } from '../../../../testing/sent-form';
import { AUTHOR, authorCall, authorCalls, sentPatch } from '../../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';

const ORDERS = [
  opponentOrder(0, { id: 'first', note: 'Only once.', once: true, spent: true }),
  opponentOrder(1, { when: 'shadow, assign', answers: ['assign(frodo: rest)'] }),
  opponentOrder(2, { when: 'shadow, action, phase(maneuver)', answers: ['pass'] }),
];

const held = (extra: Partial<AuthoringDecision> = {}): AuthoringDecision =>
  decision({ seat: 'shadow', text: 'Assign minions', where: 'turn 1, assignment', step: null, held: true, ...extra });

const ASSIGN = held({
  type: 'assign',
  pass: true,
  characters: [
    { term: null, label: 'Frodo', alias: 'frodo', id: '1' },
    { term: null, label: 'Sam', alias: 'sam', id: '2' },
  ],
  options: [
    { term: null, label: 'Goblin Runner', alias: 'orc', id: '11' },
    { term: null, label: 'Goblin Runner', alias: 'orc2', id: '12' },
  ],
});

const withOpponent = (opponent: Partial<OpponentState> = {}, extra: Partial<DraftState> = {}): DraftState =>
  draftState({ opponent: opponentState({ orders: ORDERS, ...opponent }), ...extra });

describe('OpponentTabComponent: standing orders and rules', () => {
  let fixture: ComponentFixture<OpponentTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let notifier: FakeNotifier;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    configure();
  });

  function configure() {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    notifier = TestBed.inject(Notifier) as FakeNotifier;
    fixture = TestBed.createComponent(OpponentTabComponent);
    el = fixture.nativeElement as HTMLElement;
  }

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  async function open(state: DraftState) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(state));
    await settle();
    await render();
  }

  async function render() {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function find<T extends HTMLElement>(selector: string): T {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  }

  async function click(selector: string) {
    find(selector).click();
    await settle();
    await render();
  }

  async function typeIn(selector: string, value: string) {
    const field = find<HTMLInputElement | HTMLTextAreaElement>(selector);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    await render();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await settle();
    await render();
  }

  const edits = (): TestRequest[] => authorCalls(backend, 'edit');
  /** The one edit sent: its patch. */
  function patch(): unknown {
    return sentPatch(authorCall(backend, 'edit'));
  }
  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;
  const card = (index: number) => `li.gd-author-order[data-index="${index}"]`;
  const kept = () => JSON.parse(sessionStorage.getItem(ORDERS_MEMORY_KEY) ?? 'null') as Record<string, unknown>;

  it('lists the orders in the order they are tried, with their tags', async () => {
    await open(withOpponent());
    expect(find('ol.gd-author-orders').getAttribute('data-count')).toBe('3');
    expect(
      [...el.querySelectorAll(`${card(0)} .gd-author-order__summary span`)].map((part) => part.textContent?.trim()),
    ).toEqual(['1', 'shadow, action, phase(shadow)', 'play(orc)']);
    expect(find(`${card(0)} .gd-author-order__summary`).title).toBe('Only once.');
    expect([...el.querySelectorAll(`${card(0)} .gd-author-order__tag`)].map((tag) => tag.textContent?.trim())).toEqual([
      'first',
      'used up',
    ]);
    expect(find(card(0)).hasAttribute('data-spent')).toBe(true);
    expect(find<HTMLButtonElement>(`${card(0)} [data-author=up]`).disabled).toBe(true);
    expect(find<HTMLButtonElement>(`${card(2)} [data-author=down]`).disabled).toBe(true);
    await authoring.setState(withOpponent({ orders: [opponentOrder(0, { once: true })] }));
    await render();
    expect(text(`${card(0)} .gd-author-order__tag`)).toBe('once');
    await authoring.setState(withOpponent({ orders: [] }));
    await render();
    expect(text('[data-author=no-orders]')).toBe('None.');
  });

  it('shows nothing of orders from a server that does not describe the opponent', async () => {
    await open(draftState());
    expect(el.querySelector('section[data-part=orders]')).toBeNull();
    expect(el.querySelector('gd-author-select[data-part=leftovers]')).toBeNull();
  });

  it('adds an order at the end, sent with Add, the others left alone', async () => {
    await open(withOpponent());
    await click('[data-author=add-order]');
    expect(find('li.gd-author-order[data-new]')).toBe(el.querySelectorAll('li.gd-author-order')[3]);
    expect(find<HTMLButtonElement>('[data-new] [data-author=apply]').disabled).toBe(true);
    await typeIn('[data-new] [data-part=when]', 'shadow, action, phase(regroup)');
    await typeIn('[data-new] [data-part=answer]', 'pass');
    await click('[data-new] [data-part=once]');
    await typeIn('[data-new] [data-part=note]', 'At the end');
    await click('[data-new] [data-author=apply]');
    expect(patch()).toEqual({
      opponent: {
        orders: [
          {
            op: 'insert',
            order: { note: 'At the end', once: true, when: 'shadow, action, phase(regroup)', answer: 'pass' },
          },
        ],
      },
    });
    // (the page of the fresh game opens the new order)
    expect(kept()).toMatchObject({ open: 3, form: null, refusal: null });
  });

  it('changes an open order with Apply: one merge of what changed, by its name', async () => {
    await open(withOpponent());
    await click(`${card(0)} .gd-author-order__summary`);
    expect(find(card(0)).hasAttribute('data-open')).toBe(true);
    expect(find<HTMLInputElement>(`${card(0)} [data-part=id]`).value).toBe('first');
    expect(find<HTMLButtonElement>(`${card(0)} [data-author=apply]`).disabled).toBe(true);
    await typeIn(`${card(0)} [data-part=note]`, '');
    await click(`${card(0)} [data-part=once]`);
    await typeIn(`${card(0)} [data-part=answer]`, 'play(orc2)');
    // Enter in a one-line field applies
    find(`${card(0)} [data-part=answer]`).dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    expect(patch()).toEqual({
      opponent: { orders: [{ op: 'merge', id: 'first', set: { answer: 'play(orc2)' }, unset: ['note', 'once'] }] },
    });
    // Unnamed, an order goes by its place
    await click(`${card(1)} .gd-author-order__summary`);
    expect(find(card(0)).hasAttribute('data-open')).toBe(false);
    await typeIn(`${card(1)} [data-part=when]`, 'shadow, assign, turn(2)');
    await click(`${card(1)} [data-author=apply]`);
    expect(patch()).toEqual({
      opponent: { orders: [{ op: 'merge', index: 1, set: { when: 'shadow, assign, turn(2)' } }] },
    });
  });

  it('puts the form back with Cancel, and closes an order clicked again', async () => {
    await open(withOpponent());
    await click(`${card(1)} .gd-author-order__summary`);
    await typeIn(`${card(1)} [data-part=answer]`, 'pass');
    await click(`${card(1)} [data-author=cancel]`);
    expect(find<HTMLInputElement>(`${card(1)} [data-part=answer]`).value).toBe('assign(frodo: rest)');
    await click(`${card(1)} .gd-author-order__summary`);
    expect(el.querySelector('.gd-author-order__form')).toBeNull();
    await click('[data-author=add-order]');
    await click('[data-new] [data-author=cancel]');
    expect(el.querySelector('[data-new]')).toBeNull();
    expect(edits()).toEqual([]);
  });

  it('moves an order up and down, and deletes it once the author says so', async () => {
    await open(withOpponent());
    await click(`${card(1)} [data-author=up]`);
    expect(patch()).toEqual({ opponent: { orders: [{ op: 'move', index: 1, to: 0 }] } });
    await click(`${card(0)} [data-author=down]`);
    expect(patch()).toEqual({ opponent: { orders: [{ op: 'move', id: 'first', to: 1 }] } });
    await click(`${card(2)} [data-author=delete]`);
    expect(notifier.dialogs).toEqual(['confirm: Delete order 3?']);
    expect(edits()).toEqual([]);
    notifier.answers.push(true);
    await click(`${card(0)} [data-author=delete]`);
    expect(notifier.dialogs.at(-1)).toBe('confirm: Delete order 1 (first)?');
    expect(patch()).toEqual({ opponent: { orders: [{ op: 'delete', id: 'first' }] } });
  });

  it('keeps the open order where it goes as others move or go', async () => {
    await open(withOpponent());
    await click(`${card(2)} .gd-author-order__summary`);
    await click(`${card(2)} [data-author=up]`);
    expect(kept()['open']).toBe(1);
    edits()[0]!.flush(JSON.stringify(result(withOpponent())));
    await settle();
    notifier.answers.push(true);
    await click(`${card(0)} [data-author=delete]`);
    expect(kept()['open']).toBe(0);
  });

  it("says a refusal on the order in the server's words, and keeps what was typed", async () => {
    await open(withOpponent());
    await click(`${card(1)} .gd-author-order__summary`);
    await typeIn(`${card(1)} [data-part=answer]`, 'assign(nobody: rest)');
    await click(`${card(1)} [data-author=apply]`);
    const error = "opponent.orders[2].answer: there is no card called 'nobody'";
    edits()[0]!.flush(JSON.stringify({ error }), { status: 400, statusText: 'No' });
    await settle();
    await render();
    expect(text(`${card(1)} .gd-author-order__refusal`)).toBe(error);
    expect(find<HTMLInputElement>(`${card(1)} [data-part=answer]`).value).toBe('assign(nobody: rest)');
    expect(kept()).toMatchObject({ open: 1, refusal: error, form: { answer: 'assign(nobody: rest)' } });
  });

  it('comes back after a fresh game as it was: the order open, what was typed, the scroll', async () => {
    sessionStorage.setItem(
      ORDERS_MEMORY_KEY,
      JSON.stringify({
        draft: 'test',
        open: 'new',
        before: 1,
        form: { id: '', note: '', once: false, when: 'shadow, assign', answer: 'pass' },
        refusal: null,
        scroll: 0,
      }),
    );
    await open(withOpponent());
    const cards = [...el.querySelectorAll('li.gd-author-order')];
    expect(cards[1]?.hasAttribute('data-new')).toBe(true);
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('pass');
    expect(find<HTMLButtonElement>('[data-new] [data-author=apply]').title).toBe(
      'Into the file, as order 2 (a fresh game)',
    );
    await click('[data-new] [data-author=apply]');
    expect(patch()).toEqual({
      opponent: { orders: [{ op: 'insert', order: { when: 'shadow, assign', answer: 'pass' }, before: 1 }] },
    });
  });

  it('says what answers the waiting decision, and starts an order for it from what is given in the tab', async () => {
    const answering = {
      order: 1,
      id: null,
      answer: 'assign(frodo: rest)',
      gives: 'assign(frodo: orc + orc2)',
      rule: null,
      when: 'shadow, assign, phase(assignment), turn(1)',
    } as const;
    await open(withOpponent({ answering }, { waiting: { human: null, opponent: ASSIGN } }));
    expect(find('.gd-author-opponent__answering').getAttribute('data-rule')).toBe('order');
    expect(text('.gd-author-opponent__rule')).toBe('Order 2');
    expect(text('.gd-author-opponent__gives')).toBe('assign(frodo: orc + orc2)');
    await click('[data-author=order-for]');
    expect(el.querySelectorAll('li.gd-author-order')[1]?.hasAttribute('data-new')).toBe(true);
    expect(find<HTMLTextAreaElement>('[data-new] [data-part=when]').value).toBe(answering.when);
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('assign(frodo: orc + orc2)');
    // What the author gives in the tab: who the minions fight, the rest included
    await choose('select[data-rest]', 'ringBearer');
    await choose('select[data-minion=orc]', 'sam');
    await click('[data-author=order-for]');
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('assign(sam: orc, ringBearer: rest)');
    const raw = find<HTMLInputElement>('.gd-author-opponent__raw input');
    raw.value = 'pass';
    raw.dispatchEvent(new Event('input'));
    await click('[data-author=order-for]');
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('pass');
    const suggested = [...el.querySelectorAll('#gd-author-order-answers option')].map((one) =>
      one.getAttribute('value'),
    );
    expect(suggested).toEqual(['pass', 'assign(frodo: orc + orc2)', 'assign(frodo: rest)']);
  });

  it('starts an order from the cards ticked, else from what answers it now, else blank', async () => {
    const cards = held({
      type: 'cards',
      pass: undefined,
      min: 1,
      max: 1,
      options: [{ term: null, label: 'Goblin Runner', alias: 'orc', id: '11' }],
    });
    const answering = { order: null, id: null, answer: null, gives: null, rule: null, when: 'shadow, cards' } as const;
    await open(withOpponent({ answering }, { waiting: { human: null, opponent: cards } }));
    expect(find('.gd-author-opponent__answering').getAttribute('data-rule')).toBe('nothing');
    expect(el.querySelector('.gd-author-opponent__gives')).toBeNull();
    await click('[data-author=order-for]');
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('');
    expect(el.querySelectorAll('li.gd-author-order')[3]?.hasAttribute('data-new')).toBe(true);
    await click('[data-key=orc] input');
    await click('[data-author=order-for]');
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('choose(orc)');
    await authoring.setState(
      withOpponent({ answering: { ...answering, rule: 'leftovers' } }, { waiting: { human: null, opponent: ASSIGN } }),
    );
    await render();
    expect(find('.gd-author-opponent__answering').getAttribute('data-rule')).toBe('leftovers');
  });

  it('assigns the rest of the minions by rule, named minions first', async () => {
    await open(withOpponent({}, { waiting: { human: null, opponent: ASSIGN } }));
    expect([...el.querySelectorAll('select[data-rest] option')].map((one) => one.textContent?.trim())).toEqual([
      'nobody',
      'the Ring-bearer',
      'Frodo (frodo)',
      'Sam (sam)',
    ]);
    await choose('select[data-rest]', 'frodo');
    expect(find('select[data-minion=orc] option').textContent?.trim()).toBe('the rest');
    await choose('select[data-minion=orc2]', 'sam');
    await click('[data-author=assign]');
    const answers = backend
      .match((r) => r.url === `${AUTHOR}answer`)
      .map((request) => sentForm(request.request).get('term') as string);
    expect(answers).toEqual(['assign(sam: orc2, frodo: rest)']);
  });

  it('chooses the fallback, the sandbox’s cards and the leftovers by the opponent patch', async () => {
    await open(withOpponent());
    const fallback = 'section[data-part=how] gd-author-select[data-part=fallback] select';
    expect([...el.querySelectorAll(`${fallback} option`)].map((one) => one.getAttribute('value'))).toEqual([
      'fail',
      'pass',
      'bot',
      'sandbox',
    ]);
    expect(el.querySelector('.gd-author-plays')).toBeNull();
    await choose(fallback, 'sandbox');
    expect(patch()).toEqual({ opponent: { set: { fallback: 'sandbox' } } });
    await authoring.setState(withOpponent({ fallback: 'sandbox', plays: ['orc2'] }));
    await render();
    expect(text('.gd-author-plays')).toBe('plays orc orc2');
    await click('.gd-author-plays gd-author-check[data-card=orc] input');
    expect(patch()).toEqual({ opponent: { set: { plays: 'orc2, orc' } } });
    await click('.gd-author-plays gd-author-check[data-card=orc2] input');
    expect(patch()).toEqual({ opponent: { unset: ['plays'] } });
    await authoring.setState(withOpponent({ fallback: 'sandbox', plays: [] }));
    await render();
    expect(text('.gd-author-plays .gd-author-note')).toBe('(all)');
    await choose(fallback, 'fail');
    expect(patch()).toEqual({ opponent: { unset: ['fallback', 'plays'] } });
    const leftovers = 'gd-author-select[data-part=leftovers] select';
    expect(find<HTMLSelectElement>(leftovers).value).toBe('');
    await choose(leftovers, 'ringBearer');
    expect(patch()).toEqual({ opponent: { set: { leftovers: 'ringBearer' } } });
    await authoring.setState(withOpponent({ leftovers: 'sam' }));
    await render();
    expect(find<HTMLSelectElement>(leftovers).value).toBe('sam');
    await choose(leftovers, '');
    expect(patch()).toEqual({ opponent: { unset: ['leftovers'] } });
  });

  it('keeps a form being typed in while the state comes and goes', async () => {
    await open(withOpponent());
    await click(`${card(1)} .gd-author-order__summary`);
    const answer = find<HTMLInputElement>(`${card(1)} [data-part=answer]`);
    await typeIn(`${card(1)} [data-part=answer]`, 'half typ');
    await authoring.setState(withOpponent({}, { revision: 5, position: { ...withOpponent().position, turn: 2 } }));
    await render();
    expect(find(`${card(1)} [data-part=answer]`)).toBe(answer);
    expect(answer.value).toBe('half typ');
  });

  it('edits an order’s When with the When composer: the form takes it as typing does, and Apply sends it', async () => {
    await open(withOpponent({}, { twoSided: true }));
    await click(`${card(2)} .gd-author-order__summary`);
    const pickers = `${card(2)} gd-author-when-pickers`;
    expect(find<HTMLSelectElement>(`${pickers} [data-picker=phase] select`).value).toBe('maneuver');
    await choose(`${pickers} [data-picker=phase] select`, 'archery');
    await choose(`${pickers} gd-author-select[data-picker=role] select`, 'shadow');
    await choose(`${pickers} [data-picker=asked] select`, 'phaseActions');
    const when = find<HTMLTextAreaElement>(`${card(2)} [data-part=when]`);
    expect(when.value).toBe('shadow, action, phaseActions, phase(archery), as(shadow)');
    // (nothing is sent until Apply: the form's own model)
    expect(edits()).toEqual([]);
    const turn = find<HTMLInputElement>(`${pickers} input[data-picker=turn]`);
    turn.value = '2';
    turn.dispatchEvent(new Event('change'));
    await render();
    await click(`${card(2)} [data-author=apply]`);
    expect(patch()).toEqual({
      opponent: {
        orders: [
          { op: 'merge', index: 2, set: { when: 'shadow, action, phaseActions, phase(archery), turn(2), as(shadow)' } },
        ],
      },
    });
  });

  it('has no pickers for a When written out in full, and no role in a one-sided game', async () => {
    await open(
      withOpponent({ orders: [opponentOrder(0, { when: '{\n\tterms: "shadow"\n}' }), { ...ORDERS[2]!, index: 1 }] }),
    );
    await click(`${card(0)} .gd-author-order__summary`);
    expect(el.querySelector(`${card(0)} .gd-author-when__pickers`)).toBeNull();
    await click(`${card(1)} .gd-author-order__summary`);
    expect(el.querySelector(`${card(1)} gd-author-select[data-picker=role]`)).toBeNull();
  });

  it('offers the rest to the Ring-bearer for an order about an assignment, as a step’s Answer does', async () => {
    await open(withOpponent());
    await click(`${card(2)} .gd-author-order__summary`);
    expect(el.querySelector(`${card(2)} [data-author=answer-rest]`)).toBeNull();
    await click(`${card(1)} .gd-author-order__summary`);
    expect(text(`${card(1)} [data-author=answer-rest]`)).toBe('+ assign(ringBearer: rest)');
    await click(`${card(1)} [data-author=answer-rest]`);
    expect(find<HTMLInputElement>(`${card(1)} [data-part=answer]`).value).toBe(
      'assign(frodo: rest), assign(ringBearer: rest)',
    );
    // (said already)
    expect(el.querySelector(`${card(1)} [data-author=answer-rest]`)).toBeNull();
  });

  it('offers it for a new order while the opponent’s waiting decision is an assignment', async () => {
    await open(withOpponent({}, { waiting: { human: null, opponent: ASSIGN } }));
    await click('[data-author=add-order]');
    await click('[data-new] [data-author=answer-rest]');
    expect(find<HTMLInputElement>('[data-new] [data-part=answer]').value).toBe('assign(ringBearer: rest)');
  });

  it('offers the eager and bot fallbacks, each saying what it does, and sets them by the opponent patch', async () => {
    await open(withOpponent({ fallbacks: ['fail', 'pass', 'bot', 'sandbox', 'eager'] }));
    const fallback = 'section[data-part=how] gd-author-select[data-part=fallback] select';
    const options = [...el.querySelectorAll<HTMLOptionElement>(`${fallback} option`)];
    expect(options.map((one) => [one.value, one.textContent?.trim()])).toEqual([
      ['fail', 'fail: off the script'],
      ['pass', 'pass: does least'],
      ['bot', 'bot: Gemp’s bot'],
      ['sandbox', 'sandbox: plays minions'],
      ['eager', 'eager: takes all'],
    ]);
    expect(options.every((one) => (one.getAttribute('title') ?? '').length > 20)).toBe(true);
    await choose(fallback, 'eager');
    expect(patch()).toEqual({ opponent: { set: { fallback: 'eager' }, unset: ['plays'] } });
    await choose(fallback, 'bot');
    expect(patch()).toEqual({ opponent: { set: { fallback: 'bot' }, unset: ['plays'] } });
    await authoring.setState(
      withOpponent({
        fallback: 'eager',
        fallbacks: ['fail', 'pass', 'bot', 'sandbox', 'eager'],
        answering: { order: null, id: null, answer: null, gives: null, rule: 'eager', when: 'shadow, action' },
      }),
    );
    await render();
    expect(find<HTMLSelectElement>(fallback).value).toBe('eager');
  });
});
