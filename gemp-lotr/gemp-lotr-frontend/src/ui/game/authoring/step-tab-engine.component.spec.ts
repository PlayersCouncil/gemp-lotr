import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { DraftState, DraftStep } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { decision, draftGameXml, draftState, result } from '../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { StepTabComponent } from './step-tab.component';
import { TablePicker } from './table-picker.service';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

/** A draft whose steps are these (named s1, s2, ...; a block's steps s3k1, ...). */
const withSteps = (steps: DraftStep[], extra: Partial<DraftState> = {}) =>
  draftState({ ...extra, draft: { ...draftState().draft, steps } });

const xmlJson = (value: unknown) =>
  JSON.stringify(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

/** The draft's game with step `number`'s event on the table, saying these messages. */
const withStepShown = (messages: readonly string[], number = 1) =>
  draftGameXml().replace(
    '</gameState>',
    `<ge type="S" scenario="${xmlJson({
      type: 'step',
      step: number,
      sub: 0,
      name: null,
      seat: 'human',
      acknowledge: false,
      pause: false,
      checkpoint: false,
      messages: messages.map((text) => ({ text, html: `<p>${text}</p>` })),
      parts: {},
      allow: null,
      cards: { frodo: 7 },
    })}"/></gameState>`,
  );

describe('StepTabComponent and the engine’s newer parts', () => {
  let fixture: ComponentFixture<StepTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let state: DraftState;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        // (the panel's, for the message cards' anchor fields)
        TablePicker,
        CardInfoService,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
  });

  afterEach(() => {
    fixture?.destroy();
    backend.match(() => true);
    vi.useRealTimers();
    document.body.innerHTML = '';
  });

  /** The page on game 7 of the draft, the Step tab on the step selected (the one the game is at, unless `select`). */
  async function open(
    start: DraftState = draftState(),
    select?: { index: number; sub?: string },
    xml = draftGameXml(),
  ) {
    state = start;
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(xml);
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(start));
    await settle();
    if (select) authoring.select(select.index, select.sub ?? null);
    fixture = TestBed.createComponent(StepTabComponent);
    el = fixture.nativeElement as HTMLElement;
    document.body.appendChild(el);
    await render();
  }

  async function render() {
    await settle();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  /** A new state of the draft, as a refresh brings it. */
  async function update(next: DraftState) {
    state = next;
    authoring.setState(next);
    await render();
  }

  const find = <T extends HTMLElement = HTMLElement>(selector: string): T => {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  };
  const field = <T extends HTMLInputElement | HTMLTextAreaElement = HTMLInputElement>(path: string) =>
    find<T>(`[data-field="${path}"]`);
  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;

  async function click(selector: string) {
    find(selector).click();
    await render();
  }

  /** Types into a field (the input event), and leaves it (`change`) when asked. */
  async function type(path: string, value: string, leave = false) {
    const input = field(path);
    input.focus();
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    if (leave) input.dispatchEvent(new Event('change', { bubbles: true }));
    await render();
  }

  async function tick(selector: string, checked: boolean) {
    const box = find<HTMLInputElement>(selector);
    box.checked = checked;
    box.dispatchEvent(new Event('change', { bubbles: true }));
    await render();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(`${selector} select`);
    select.value = value;
    select.dispatchEvent(new Event('change', { bubbles: true }));
    await render();
  }

  async function wait(ms: number) {
    vi.advanceTimersByTime(ms);
    await render();
  }

  /** The edits on their way. */
  const edits = (): TestRequest[] => backend.match((r) => r.url === `${AUTHOR}edit` && r.method === 'POST');
  const patchOf = (request: TestRequest) =>
    JSON.parse(request.request.body.get('patch') as string) as { steps: Record<string, unknown>[] };
  /** The one edit on its way: its ops. */
  function sentOps(): Record<string, unknown>[] {
    const requests = edits();
    expect(requests).toHaveLength(1);
    pending = requests[0]!;
    return patchOf(pending).steps;
  }
  let pending: TestRequest | null = null;

  /** Answers the edit last looked at with the state, or refuses it. */
  async function answer(next: DraftState | string) {
    if (typeof next === 'string') pending!.flush({ error: next }, { status: 400, statusText: 'Bad Request' });
    else {
      state = next;
      pending!.flush(JSON.stringify(result(next)));
    }
    pending = null;
    await render();
  }

  /** The state with step 1 changed as given. */
  const withFirst = (change: Partial<DraftStep>, extra: Partial<DraftState> = {}) => {
    const steps = [...(state.draft.steps ?? [])];
    steps[0] = { ...steps[0], ...change };
    return { ...state, ...extra, draft: { ...state.draft, steps } };
  };

  /** Opens the folded part of message `index`. */
  async function more(index: number) {
    find<HTMLDetailsElement>(`.gd-author-message[data-index="${index}"] details.gd-author-message__more`).open = true;
    await render();
  }

  describe('what is being asked', () => {
    it('puts the term into the When where the server writes it, and its not(…), and takes it out', async () => {
      await open();
      await choose('gd-author-when [data-picker=asked]', 'nextSkirmish');
      await wait(0);
      expect(sentOps()).toEqual([
        {
          op: 'merge',
          uid: 's1',
          set: { when: 'freeps, action, nextSkirmish, phase(fellowship), turn(1)' },
          unset: [],
        },
      ]);
      await answer(withFirst({ when: 'freeps, action, nextSkirmish, phase(fellowship), turn(1)' }));
      expect(find<HTMLInputElement>('gd-author-when input[data-picker=not]').disabled).toBe(false);
      await tick('gd-author-when input[data-picker=not]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { when: 'freeps, action, not(nextSkirmish), phase(fellowship), turn(1)' },
      });
      await answer(withFirst({ when: 'freeps, action, not(nextSkirmish), phase(fellowship), turn(1)' }));
      expect(field('when').value).toBe('freeps, action, not(nextSkirmish), phase(fellowship), turn(1)');
      expect(find<HTMLSelectElement>('gd-author-when [data-picker=asked] select').value).toBe('nextSkirmish');
      await choose('gd-author-when [data-picker=asked]', '');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { when: 'freeps, action, phase(fellowship), turn(1)' } });
      await answer(withFirst({ when: 'freeps, action, phase(fellowship), turn(1)' }));
      expect(find<HTMLInputElement>('gd-author-when input[data-picker=not]').disabled).toBe(true);
    });

    it('serves Until and Skip when too, once they say something, and not one written in full', async () => {
      await open(
        withSteps([
          { when: 'freeps, action, phase(regroup)', until: 'turn(2)', skipWhen: 'phase(shadow)', allow: 'pass' },
          { when: 'freeps', until: '{ seat: freeps }', allow: 'pass' },
        ]),
      );
      await choose('[data-path=until] [data-picker=asked]', 'reconcile');
      await choose('[data-path=skipWhen] [data-picker=asked]', 'anotherMove');
      await tick('[data-path=skipWhen] input[data-picker=not]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { until: 'reconcile, turn(2)', skipWhen: 'not(anotherMove), phase(shadow)' },
      });
      await answer(withFirst({ until: 'reconcile, turn(2)', skipWhen: 'not(anotherMove), phase(shadow)' }));
      await click('[data-go=next]');
      expect(el.querySelector('[data-path=until]')).toBeNull();
      expect(el.querySelector('[data-path=skipWhen]')).toBeNull();
      expect(el.querySelector('[data-path=when]')).not.toBeNull();
    });

    it("serves a block's Until and Skip when", async () => {
      await open(withSteps([{ when: 'turn(1)', until: 'phase(regroup)', skipWhen: 'turn(2)', anyOrder: [] }]));
      expect(el.querySelector('[data-path=until] [data-picker=asked]')).not.toBeNull();
      expect(el.querySelector('[data-path=skipWhen] [data-picker=asked]')).not.toBeNull();
    });

    it('shows what the decision waiting is for on the step the game is at, and nowhere else', async () => {
      await open(
        draftState({
          waiting: {
            human: decision({ kind: 'nextSkirmish', text: 'Choose next skirmish to resolve' }),
            opponent: null,
          },
        }),
      );
      expect(text('.gd-author-step-head__asked')).toBe('next skirmish');
      expect(find('.gd-author-step-head__asked').getAttribute('data-kind')).toBe('nextSkirmish');
      expect(find('.gd-author-step-head__asked').title).toContain('("Choose next skirmish to resolve")');
      await click('[data-go=next]');
      expect(el.querySelector('.gd-author-step-head__asked')).toBeNull();
      await click('[data-go=previous]');
      await update(draftState({ waiting: { human: null, opponent: decision({ kind: 'oddity' }) } }));
      expect(el.querySelector('.gd-author-step-head__asked')).toBeNull();
      await update(draftState({ waiting: { human: null, opponent: decision({ kind: 'phaseActions' }) } }));
      expect(text('.gd-author-step-head__asked')).toBe('play or pass');
    });
  });

  describe('what the step must refuse', () => {
    it('sends Refuses with the steps’ merge: one line as a line, more as a list', async () => {
      await open();
      await type('refuses', 'choose(boromir)', true);
      await wait(0);
      expect(sentOps()).toEqual([{ op: 'merge', uid: 's1', set: { refuses: 'choose(boromir)' }, unset: [] }]);
      await answer(withFirst({ refuses: 'choose(boromir)' }));
      await type('refuses', '2: choose(boromir)\npass', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { refuses: ['2: choose(boromir)', 'pass'] } });
      await answer(withFirst({ refuses: ['2: choose(boromir)', 'pass'] }));
      expect(field<HTMLTextAreaElement>('refuses').value).toBe('2: choose(boromir)\npass');
      await type('refuses', '', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: {}, unset: ['refuses'] });
    });

    it("is for the player's steps that allow something", async () => {
      await open(
        withSteps([
          { when: 'freeps, action', answer: 'pass' },
          { when: 'shadow, action', answer: 'pass' },
        ]),
      );
      expect(field<HTMLTextAreaElement>('refuses').disabled).toBe(true);
      await click('[data-go=next]');
      expect(el.querySelector('[data-field=refuses]')).toBeNull();
    });

    it('says how each refusal stands at the decision in hand, a word each and why in its title', async () => {
      const refuses = [
        { refusal: '2: choose(boromir)', at: 2, now: 'refused' as const },
        { refusal: 'pass', at: 0, now: 'allowed' as const },
      ];
      await open(draftState({ waiting: { human: decision({ refuses }), opponent: null } }));
      const badges = [...el.querySelectorAll<HTMLElement>('.gd-author-allow__refusal')];
      expect(badges.map((badge) => [badge.dataset['now'], badge.textContent?.replace(/\s+/g, ' ').trim()])).toEqual([
        ['refused', '2: choose(boromir) refused'],
        ['allowed', 'pass allowed!'],
      ]);
      expect(badges[1]?.title).toBe('pass: Offered here, and Allow lets it through: Check would fail on this.');
      await click('[data-go=next]');
      expect(el.querySelector('.gd-author-allow__refusal')).toBeNull();
    });

    it('offers a click on the table the step did not take as a refusal, once', async () => {
      await open(withSteps([{ when: 'freeps, action', allow: 'play(sword)' }]));
      const guide = TestBed.inject(ScenarioGuide);
      expect(el.querySelector('[data-act=add-refusal]')).toBeNull();
      guide.hint.set({ html: 'Not now.', cardId: null, button: 'Pass', option: null });
      await render();
      expect(text('[data-act=add-refusal]')).toBe('+ refuses pass');
      await click('[data-act=add-refusal]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { refuses: 'pass' } });
      await answer(withFirst({ refuses: 'pass' }));
      expect(el.querySelector('[data-act=add-refusal]')).toBeNull();
    });
  });

  describe('assignments by rule', () => {
    it('offers every minion still to be given out to the Ring-bearer, beside what the assignment offers', async () => {
      await open(draftState({ waiting: { human: decision({ type: 'assign', pass: true }), opponent: null } }));
      expect(text('.gd-author-allow__offered')).toBe('+ assign(ringBearer: rest)');
      await click('[data-term="assign(ringBearer: rest)"]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { allow: 'pass, assign(ringBearer: rest)' } });
      expect(
        find('[data-field="allow.answers"]').closest('label')?.querySelector<HTMLElement>('.gd-author-field__label')
          ?.title,
      ).toContain('ringBearer: rest');
    });
  });

  describe("a message's condition", () => {
    it('is typed in Say only if, sent with the messages when it is left, and shown as the file writes it', async () => {
      await open();
      await more(0);
      await type('messages.0.require', '{type: canSpot, filter: name(Aragorn)}', true);
      await wait(0);
      expect(sentOps()).toEqual([
        {
          op: 'merge',
          uid: 's1',
          set: { messages: [{ text: 'Hello', require: '{type: canSpot, filter: name(Aragorn)}' }, { text: 'Two' }] },
          unset: [],
        },
      ]);
      const require = [{ type: 'canSpot', filter: 'name(Aragorn)' }];
      field('messages.0.require').blur();
      await answer(
        withFirst(
          { messages: [{ text: 'Hello', require }, 'Two'] },
          { texts: { s1: { 'messages.0.require': '[\n\t{\n\t\ttype: canSpot\n\t}\n]' } } },
        ),
      );
      expect(field<HTMLTextAreaElement>('messages.0.require').value).toBe('[\n\t{\n\t\ttype: canSpot\n\t}\n]');
      expect(find<HTMLDetailsElement>('.gd-author-message[data-index="0"] details').open).toBe(true);
    });

    it('goes with the message when it is moved within the step', async () => {
      await open(
        withSteps([
          { when: 'freeps, action', allow: 'pass', messages: ['One', { text: 'Two', require: { type: 'x' } }] },
        ]),
      );
      await click('.gd-author-message[data-index="1"] [data-act=up]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Two', require: '{\n\t"type": "x"\n}' }, { text: 'One' }] },
      });
    });

    it("refused, is kept as typed and the server's words said, while the message's other changes go", async () => {
      await open();
      await more(0);
      await type('messages.0.require', '{ type: canSpot, filter: ');
      await type('messages.0.text', 'Hello there');
      await wait(700);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Hello there', require: '{ type: canSpot, filter: ' }, { text: 'Two' }] },
      });
      await answer('step 1.messages[0].require: End of input while parsing an object');
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Hello there' }, { text: 'Two' }] } });
      await answer(withFirst({ messages: [{ text: 'Hello there' }, 'Two'] }));
      expect(authoring.problem()).toBe('step 1.messages[0].require: End of input while parsing an object');
      expect(field<HTMLTextAreaElement>('messages.0.require').value).toBe('{ type: canSpot, filter: ');
      expect(field<HTMLTextAreaElement>('messages.0.text').value).toBe('Hello there');
    });

    it('is not for a message that ends in a choice', async () => {
      await open(
        withSteps([{ when: 'freeps', pause: true, messages: [{ text: 'Which?', choices: { a: 'A', b: 'B' } }] }]),
      );
      await more(0);
      expect(field<HTMLTextAreaElement>('messages.0.require').disabled).toBe(true);
    });

    it('can be given to a closing message', async () => {
      await open(withSteps([{ when: 'freeps', allow: 'pass', end: { result: 'complete', messages: ['Bye'] } }]));
      await type('end.messages.0.require', '{ type: x }', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { end: { result: 'complete', messages: [{ text: 'Bye', require: '{ type: x }' }] } },
      });
    });

    it('says when the step on the table left the message out, its condition failing', async () => {
      const step: DraftStep = {
        when: 'freeps, action',
        allow: 'pass',
        messages: ['Always', { text: 'If', require: { type: 'x' } }],
      };
      await open(withSteps([step]), undefined, withStepShown(['Always']));
      expect(find('.gd-author-message[data-index="1"]').getAttribute('data-said')).toBe('no');
      expect(text('.gd-author-message[data-index="1"] .gd-author-message__left-out')).toBe(
        'Not said here: its condition fails.',
      );
      expect(find('.gd-author-message[data-index="0"]').getAttribute('data-said')).toBeNull();
    });

    it('says only that it may have been left out when the count cannot tell which', async () => {
      const step: DraftStep = {
        when: 'freeps, action',
        allow: 'pass',
        messages: [
          { text: 'A', require: { type: 'x' } },
          { text: 'B', require: { type: 'y' } },
        ],
      };
      await open(withSteps([step, { when: 'shadow', answer: 'pass' }]), undefined, withStepShown(['A']));
      expect(find('.gd-author-message[data-index="0"]').getAttribute('data-said')).toBe('maybe');
      expect(text('.gd-author-message[data-index="1"] .gd-author-message__left-out')).toBe('Maybe not said here.');
      // (another step on the table: nothing is said of this one's)
      await click('[data-go=next]');
      expect(el.querySelector('[data-said]')).toBeNull();
    });
  });

  describe('numbers from the game in a message', () => {
    const cards = { freeps: { frodo: '1_290' }, shadow: { orc: '1_191' } };

    async function menu(index = 0) {
      await click(`.gd-author-message[data-index="${index}"] [data-act=templates]`);
    }

    it('puts one in at the caret as if typed: the focus and caret kept, sent once when typing stops', async () => {
      await open();
      await type('messages.0.text', 'Pool:  now');
      const area = field<HTMLTextAreaElement>('messages.0.text');
      area.setSelectionRange(6, 6);
      // (its button does not take the focus from the text)
      const down = new MouseEvent('mousedown', { bubbles: true, cancelable: true });
      find('.gd-author-message[data-index="0"] [data-act=templates]').dispatchEvent(down);
      expect(down.defaultPrevented).toBe(true);
      await menu();
      expect(find('[data-act=templates]').getAttribute('aria-expanded')).toBe('true');
      await click('[data-template=twilight]');
      expect(area.value).toBe('Pool: {twilight} now');
      expect(document.activeElement).toBe(area);
      expect(area.selectionStart).toBe(16);
      expect(el.querySelector('.gd-author-templates__menu')).toBeNull();
      await wait(700);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Pool: {twilight} now' }, { text: 'Two' }] } });
      await answer(withFirst({ messages: [{ text: 'Pool: {twilight} now' }, 'Two'] }));
      await wait(700);
      expect(edits()).toEqual([]);
    });

    it("asks which card or seat for one that is a card's or a seat's, from the state's list", async () => {
      await open(
        draftState({
          draft: { ...draftState().draft, cards },
          templates: { totals: [], perCard: ['strength'], perSeat: ['hand'] },
        }),
      );
      await menu();
      expect([...el.querySelectorAll('[data-template]')].map((one) => one.textContent?.trim())).toEqual([
        'strength:…',
        'hand:…',
      ]);
      await click('[data-template=strength]');
      expect(find('.gd-author-templates__menu').getAttribute('data-asking')).toBe('card');
      expect([...el.querySelectorAll('[data-of]')].map((one) => one.getAttribute('data-of'))).toEqual(['frodo', 'orc']);
      await click('[data-act=back]');
      await click('[data-template=hand]');
      expect([...el.querySelectorAll('[data-of]')].map((one) => one.getAttribute('data-of'))).toEqual([
        'freeps',
        'shadow',
        'human',
        'opponent',
      ]);
      await click('[data-of=shadow]');
      expect(field<HTMLTextAreaElement>('messages.0.text').value).toBe('Hello{hand:shadow}');
      await menu();
      await click('[data-template=strength]');
      await click('[data-of=orc]');
      expect(field<HTMLTextAreaElement>('messages.0.text').value).toBe('Hello{hand:shadow}{strength:orc}');
      await menu();
      await menu();
      expect(el.querySelector('.gd-author-templates__menu')).toBeNull();
    });

    it("serves a closing message's text, sent when the field is left", async () => {
      await open(withSteps([{ when: 'freeps', allow: 'pass', end: { result: 'fail', messages: ['Lost'] } }]));
      const area = field<HTMLTextAreaElement>('end.messages.0.text');
      area.focus();
      area.setSelectionRange(4, 4);
      await click('.gd-author-message[data-closing] [data-act=templates]');
      await click('.gd-author-message[data-closing] [data-template=turn]');
      expect(area.value).toBe('Lost{turn}');
      await wait(700);
      expect(edits()).toEqual([]);
      area.dispatchEvent(new Event('change', { bubbles: true }));
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { end: { result: 'fail', messages: [{ text: 'Lost{turn}' }] } } });
    });
  });
});
