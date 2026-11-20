import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { DraftState, DraftStep, UncoveredDecision } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { decision, draftGameXml, draftState, position, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { STEP_CARRY_KEY } from './step/step-editor';
import { CATCH_UP_MS, StepTabComponent } from './step-tab.component';
import { TablePicker } from './table-picker.service';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

/** A draft whose steps are these (named s1, s2, ...; a block's steps s3k1, ...). */
const withSteps = (steps: DraftStep[], extra: Partial<DraftState> = {}) =>
  draftState({ ...extra, draft: { ...draftState().draft, steps } });

const UNCOVERED: UncoveredDecision = {
  seat: 'human',
  after: 1,
  when: 'freeps, action',
  repeating: null,
  until: null,
  becomes: null,
  problem: null,
  block: null,
  sub: 0,
  reuses: null,
};

describe('StepTabComponent', () => {
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
  async function open(start: DraftState = draftState(), select?: { index: number; sub?: string }) {
    state = start;
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
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

  /** The one call of `what` on its way. */
  function sent(what: string): TestRequest['request']['body'] {
    const requests = backend.match((r) => r.url === AUTHOR + what && r.method === 'POST');
    expect(requests).toHaveLength(1);
    return requests[0]!.request.body;
  }

  describe('its head', () => {
    it('says which step this is of how many, and what became of it; turns to the steps either side', async () => {
      await open();
      expect(text('.gd-author-step-head__name')).toBe('Step 1 of 3');
      expect(text('.gd-author-step-head__where')).toBe('Current Step');
      expect(find<HTMLButtonElement>('[data-go=previous]').disabled).toBe(true);
      await click('[data-go=next]');
      expect(authoring.ui()).toMatchObject({ selected: 2, selectedUid: 's2', sub: null });
      expect(text('.gd-author-step-head__name')).toBe('Step 2 of 3');
      expect(text('.gd-author-step-head__where')).toBe('Future Step');
      await click('[data-go=current]');
      expect(authoring.ui().selected).toBe(3);
      expect(find<HTMLButtonElement>('[data-go=next]').disabled).toBe(true);
      await click('[data-go=previous]');
      expect(authoring.ui().selected).toBe(2);
    });

    it('goes to the step the game is at from before it; + adds the step for the decision no step covers', async () => {
      await open(draftState({ position: position({ step: 3, inHand: false }) }), { index: 0 });
      expect(find<HTMLButtonElement>('[data-author=add-here]').disabled).toBe(false);
      await click('[data-go=current]');
      expect(authoring.ui().selected).toBe(3);
      await click('[data-author=add-here]');
      expect(sent('here')).toBeDefined();
    });

    it('says when there is no step to show', async () => {
      await open(withSteps([], { uids: [], kids: [], statuses: [], substatuses: [] }));
      expect(text('[data-empty]')).toBe('No steps yet: play, and what you do becomes steps.');
      await update(draftState());
      authoring.remember({ selected: 9 });
      await render();
      expect(text('[data-empty]')).toBe('Select a step in the timeline.');
    });
  });

  describe('its form', () => {
    it('sends what is typed in a field when it is left, and only that part', async () => {
      await open();
      await type('answer', 'pass', true);
      await wait(0);
      expect(sentOps()).toEqual([{ op: 'merge', uid: 's1', set: { answer: 'pass' }, unset: [] }]);
      await answer(withFirst({ answer: 'pass' }));
      expect(authoring.busy()).toBe(0);
      expect(field('answer').value).toBe('pass');
    });

    it("sends a message's text a moment after typing stops, and keeps the focus, the caret and what is typed since", async () => {
      await open();
      await type('messages.0.text', 'Hello there');
      await wait(500);
      expect(edits()).toEqual([]);
      await wait(300);
      expect(sentOps()).toEqual([
        { op: 'merge', uid: 's1', set: { messages: [{ text: 'Hello there' }, { text: 'Two' }] }, unset: [] },
      ]);
      const area = field<HTMLTextAreaElement>('messages.0.text');
      area.value = 'Hello there, you';
      area.setSelectionRange(16, 16);
      area.dispatchEvent(new Event('input', { bubbles: true }));
      await answer(withFirst({ messages: [{ text: 'Hello there' }, 'Two'] }));
      expect(document.activeElement).toBe(area);
      expect(area.value).toBe('Hello there, you');
      expect(area.selectionStart).toBe(16);
      await wait(700);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Hello there, you' }, { text: 'Two' }] } });
    });

    it('takes the step as changed elsewhere, where the author has not changed it since', async () => {
      await open();
      await type('messages.0.text', 'Typed');
      await update(withFirst({ answer: 'play(sword)' }));
      expect(field('answer').value).toBe('play(sword)');
      expect(field('messages.0.text').value).toBe('Typed');
    });

    it('says a refusal, keeps what was refused, and does not send it again as it is', async () => {
      await open();
      await type('answer', 'nonsense(', true);
      await wait(0);
      sentOps();
      await answer('No such term.');
      expect(authoring.problem()).toBe('No such term.');
      expect(field('answer').value).toBe('nonsense(');
      // Going elsewhere does not happen while it stands
      void authoring.goTo(2);
      await render();
      expect(backend.match((r) => r.url === `${AUTHOR}goto`)).toEqual([]);
      expect(edits()).toEqual([]);
    });

    it("takes back the server's way of writing what was typed in full, unless the author is in that field", async () => {
      await open(withSteps([{ when: 'freeps, action' }, { when: 'b' }]));
      await type('skipWhen', '{seat:freeps}', true);
      field('skipWhen').blur();
      await wait(0);
      sentOps();
      await answer(withFirst({ skipWhen: { seat: 'freeps' } }, { texts: { s1: { skipWhen: 'seat: freeps' } } }));
      expect(field('skipWhen').value).toBe('seat: freeps');
      await type('skipWhen', '{seat:shadow}', true);
      await wait(0);
      sentOps();
      await answer(withFirst({ skipWhen: { seat: 'shadow' } }, { texts: { s1: { skipWhen: 'seat: shadow' } } }));
      expect(field('skipWhen').value).toBe('{seat:shadow}');
    });

    it('sends what is unsent before going elsewhere, and says why not when the server would not take it', async () => {
      await open();
      await type('do', 'draw(1)');
      void authoring.goTo(2);
      await render();
      expect(sentOps()[0]).toMatchObject({ set: { do: 'draw(1)' } });
      await answer(withFirst({ do: 'draw(1)' }));
      expect(sent('goto')?.get('step')).toBe('2');
      await type('do', 'draw(x)');
      void authoring.save();
      await render();
      sentOps();
      await answer('draw(x) is not a number');
      expect(sent('save')).toBeDefined();
    });

    it('sends what is unsent of a step turned away from', async () => {
      await open();
      await type('note', 'Remember');
      await click('[data-go=next]');
      expect(sentOps()[0]).toMatchObject({ uid: 's1', set: { note: 'Remember' } });
      expect(text('.gd-author-step-head__name')).toBe('Step 2 of 3');
    });

    it('says whether the author is typing in it, and catches up with the game once a field is left', async () => {
      await open();
      authoring.showTab('step');
      expect(authoring.engaged()).toBe(false);
      field('answer').focus();
      expect(authoring.engaged()).toBe(true);
      find<HTMLInputElement>('[data-flag=pause]').focus();
      expect(authoring.engaged()).toBe(false);
      const catchUp = vi.spyOn(authoring, 'catchUp');
      field('answer').dispatchEvent(new FocusEvent('focusout', { bubbles: true }));
      vi.advanceTimersByTime(CATCH_UP_MS);
      expect(catchUp).toHaveBeenCalled();
    });

    it('sends what is unsent when the tab goes', async () => {
      await open();
      await type('id', 'greeting');
      fixture.destroy();
      expect(sentOps()[0]).toMatchObject({ set: { id: 'greeting' } });
    });
  });

  describe('what it says at its top', () => {
    it('answers the step in hand again', async () => {
      await open();
      await click('[data-act=retake]');
      expect(sent('retake')).toBeDefined();
    });

    it('says a step has no answer yet: to be done now, or when the game comes to it', async () => {
      await open(draftState({ awaiting: ['s1', 's2'] }));
      expect(text('[data-note=awaiting]')).toBe('No answer yet: do it in the game now.');
      await click('[data-go=next]');
      expect(text('[data-note=awaiting]')).toBe('No answer yet: what you do there is written in.');
      expect(el.querySelector('[data-act=retake]')).toBeNull();
    });

    it('says a step is a pass the game makes by itself', async () => {
      await open(withSteps([{ when: 'shadow, action', allow: 'pass' }, { when: 'b' }]));
      expect(text('[data-note=silent]')).toBe('A pass the game makes by itself.');
    });

    it('sends a line when Enter is pressed in it, and shows a part that is not text as it is', async () => {
      await open(withSteps([{ when: 'a', messages: [{ text: 'Big', zoom: 'card:x', magnify: 2 }] }, { when: 'b' }]));
      expect(field('messages.0.magnify').value).toBe('2');
      await type('id', 'named');
      field('id').dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { id: 'named' } });
      await answer(withFirst({ id: 'named' }));
      await type('messages.0.text', 'Bigger');
      field('messages.0.text').dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      await wait(0);
      expect(edits()).toEqual([]);
    });
  });

  describe('when the form is refused', () => {
    it('tries each part by itself, says the last refusal, and does not go on to what was asked', async () => {
      await open(draftState(), { index: 2, sub: 's3k2' });
      await type('answer', 'nonsense(');
      await type('id', 'named', true);
      await wait(0);
      sentOps();
      await answer('Two things wrong.');
      expect(Object.keys(sentOps()[0]!['set'] as object)).toEqual(['id']);
      await answer(state);
      expect(Object.keys(sentOps()[0]!['set'] as object)).toEqual(['answer']);
      await answer('No such term.');
      expect(authoring.problem()).toBe('No such term.');
      for (const selector of [
        '[data-act=later]',
        '[data-act=join]',
        '.gd-author-panel[data-sub="1"] [data-act=up]',
        '.gd-author-panel[data-sub="1"] [data-act=out]',
      ]) {
        if (el.querySelector(selector)) await click(selector);
      }
      expect(edits()).toEqual([]);
      (document.activeElement as HTMLElement).blur();
      expect(authoring.engaged()).toBe(false);
      field('answer').focus();
      authoring.showTab('step');
      expect(authoring.engaged()).toBe(true);
      field('allow.otherwise').focus();
      expect(authoring.engaged()).toBe(true);
    });
  });

  describe('the small print', () => {
    it("names a block's sections for what they wait for, and counts their messages", async () => {
      const notifier = TestBed.inject(FakeNotifier);
      await open(
        withSteps([{ when: 'a' }, { until: 'x', anyOrder: [{ id: 'named', messages: ['m'] }] }], {
          leftOut: [{ where: 'step 2', message: 'gone', was: 'x', step: 2, uid: 's2' }],
        }),
        {
          index: 1,
        },
      );
      expect(text('.gd-author-panel[data-sub="-1"] .gd-author-panel__when')).toBe('every decision');
      expect(text('.gd-author-panel[data-sub="0"] .gd-author-panel__when')).toBe('any decision');
      expect(text('.gd-author-panel[data-sub="0"] .gd-author-panel__id')).toBe('named');
      expect(find('.gd-author-panel[data-sub="0"] [data-tag=messages]').title).toBe('1 message');
      expect(text('[data-kids]')).toBe('Its 1 step, in any order');
      expect(text('.gd-author-step-actions__left-out div')).toBe('Left out: gone');
      await click('.gd-author-panel[data-sub="-1"] .gd-author-panel__title');
      expect(authoring.ui().sub).toBeNull();
      notifier.answers.push(false);
      await click('[data-act=delete]');
      expect(notifier.dialogs).toEqual(['confirm: Take step 2 out of the script, with the 1 step in it?']);
      await click('.gd-author-panel[data-sub="0"] [data-act=delete]');
      expect(edits()).toEqual([]);
    });
  });

  describe('across a fresh game', () => {
    it('carries what was typed since the send on its way, and the focus, to the next page', async () => {
      await open();
      await type('answer', 'pass', true);
      await wait(0);
      sentOps();
      await type('messages.0.text', 'Typed meanwhile');
      field<HTMLTextAreaElement>('messages.0.text').setSelectionRange(5, 6);
      pending!.flush(JSON.stringify(result({ ...state, gameId: '8' }, { rebuilt: true, gameId: '8' })));
      await render();
      expect(TestBed.inject(PageCues).go).toHaveBeenCalled();
      const carried = JSON.parse(sessionStorage.getItem(STEP_CARRY_KEY)!) as Record<string, unknown>;
      expect(carried).toMatchObject({ uid: 's1', focus: { field: 'messages.0.text', start: 5, end: 6 } });
      expect(carried['form']).toMatchObject({ parts: ['messages'] });
      // (what is left unsent then goes from the next page, not this one)
      await wait(700);
      expect(edits()).toEqual([]);
    });

    it('takes back on the next page what the last one carried, sends it soon, and gives the field the focus', async () => {
      sessionStorage.setItem(
        STEP_CARRY_KEY,
        JSON.stringify({
          uid: 's1',
          form: {
            uid: 's1',
            parts: ['messages'],
            work: { messages: [{ text: 'Carried' }], allow: { hints: {} }, end: { result: '', messages: [] } },
          },
          focus: { field: 'messages.0.text', start: 2, end: 3 },
        }),
      );
      await open();
      expect(sessionStorage.getItem(STEP_CARRY_KEY)).toBeNull();
      const area = field<HTMLTextAreaElement>('messages.0.text');
      expect(area.value).toBe('Carried');
      expect(document.activeElement).toBe(area);
      expect([area.selectionStart, area.selectionEnd]).toEqual([2, 3]);
      await wait(700);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Carried' }] } });
    });

    it('takes back only the focus where nothing was unsent, and nothing for another step', async () => {
      sessionStorage.setItem(
        STEP_CARRY_KEY,
        JSON.stringify({ uid: 's1', form: null, focus: { field: 'note', start: 0, end: 0 } }),
      );
      await open();
      expect(document.activeElement).toBe(field('note'));
      fixture.destroy();
      sessionStorage.setItem(
        STEP_CARRY_KEY,
        JSON.stringify({ uid: 's9', form: null, focus: { field: 'note', start: 0, end: 0 } }),
      );
      fixture = TestBed.createComponent(StepTabComponent);
      el = fixture.nativeElement as HTMLElement;
      await render();
      expect(document.activeElement).not.toBe(field('note'));
    });
  });

  describe('what it waits for', () => {
    it('puts a picked seat, kind, phase or turn into the When, in its place', async () => {
      await open(withSteps([{ when: 'text(Play), freeps' }, { when: 'b' }]));
      await choose('[data-picker=phase]', 'shadow');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { when: 'freeps, phase(shadow), text(Play)' } });
      await answer(withFirst({ when: 'freeps, phase(shadow), text(Play)' }));
      expect(field('when').value).toBe('freeps, phase(shadow), text(Play)');
      const turn = find<HTMLInputElement>('[data-picker=turn]');
      turn.value = '3';
      turn.dispatchEvent(new Event('change'));
      await choose('[data-picker=seat]', 'shadow');
      await choose('[data-picker=kind]', 'cards');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { when: 'shadow, cards, phase(shadow), turn(3), text(Play)' } });
    });

    it('has no pickers for a When written in full, nor Only while for one in braces', async () => {
      await open(withSteps([{ when: { seat: 'freeps', odd: true } as never }, { when: 'b' }]));
      expect(el.querySelector('[data-picker]')).toBeNull();
      expect(el.querySelector('[data-field=whenRequire]')).toBeNull();
    });

    it('edits a When with requirements as its terms and its requirements', async () => {
      await open(withSteps([{ when: { seat: 'freeps', require: { type: 'canSpot' } } }, { when: 'b' }]));
      expect(field('when').value).toBe('freeps');
      expect(field('whenRequire').value).toContain('canSpot');
      await type('whenRequire', '', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { when: 'freeps' } });
    });

    it('sends its flags at once; names it; If and Unless where the pauses have choices', async () => {
      await open(draftState({ flags: ['brave'] }));
      expect(el.querySelector('.gd-author-when__flags [data-flag=once]')).toBeNull();
      await tick('[data-flag=checkpoint]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { checkpoint: true } });
      await answer(withFirst({ checkpoint: true }));
      expect(field('if')).toBeDefined();
      expect(find('[data-field=if]').closest('label')?.querySelector('span')?.title).toContain('brave');
      await tick('[data-flag=pause]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { pause: true } });
      // (a pause has no "what is done", and its heading says it is read)
      expect(el.querySelector('gd-author-allow')).toBeNull();
      expect(text('gd-author-when .gd-author-step__heading')).toBe('Read over the decision that is');
    });

    it('repeats until what it guesses, or what is written; and offers the cards it plays as the end of it', async () => {
      await open(
        withSteps([
          { when: 'freeps, action, phase(fellowship)', allow: 'play(sword)' },
          { when: 'freeps, action, phase(fellowship)' },
          { when: 'shadow, action' },
        ]),
      );
      expect(el.querySelector('.gd-author-repeat__more')).toBeNull();
      await tick('[data-flag=repeat]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { until: 'shadow, action' } });
      await answer(withFirst({ until: 'shadow, action' }));
      expect(text('[data-act=pass-once]')).toBe('Pass only once sword is in play');
      await click('[data-act=pass-once]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { allow: { answers: 'play(sword)', passWhen: expect.stringContaining('alias(sword)') } },
      });
      expect(find<HTMLDetailsElement>('.gd-author-allow__more').open).toBe(true);
      await answer(
        withFirst({ until: 'shadow, action', allow: { answers: 'play(sword)', passWhen: [{ type: 'canSpot' }] } }),
      );
      expect(el.querySelector('[data-act=pass-once]')).toBeNull();
      await click('[data-act=until-in-play]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { until: expect.stringContaining('decision: action') } });
      await answer(withFirst({ until: { seat: 'freeps' } }));
      await tick('[data-flag=repeat]', false);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ unset: ['until'] });
    });

    it('leaves Until to be written when nothing can be guessed', async () => {
      await open(withSteps([{ when: 'freeps, action' }]), { index: 0 });
      await tick('[data-flag=repeat]', true);
      expect(document.activeElement).toBe(field('until'));
      expect(find<HTMLInputElement>('[data-flag=repeat]').checked).toBe(true);
      expect(text('[data-act=pass-once]')).toBe('Pass only once they are in play');
      expect(find<HTMLButtonElement>('[data-act=pass-once]').disabled).toBe(true);
    });

    it('is the opening: a name and what is done once it has been read', async () => {
      await open(withSteps([{ when: 'opening', messages: ['Hi'] }, { when: 'b' }]));
      expect(text('gd-author-when .gd-author-step__heading')).toBe('The opening');
      expect(el.querySelector('.gd-author-when__flags')).toBeNull();
      await type('do', 'draw(5)', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { do: 'draw(5)' } });
    });
  });

  describe('what is done', () => {
    it('lets the player do anything, and back', async () => {
      await open(withSteps([{ when: 'freeps, action', allow: 'pass' }, { when: 'b' }]));
      await tick('[data-flag=free]', true);
      await wait(0);
      expect(sentOps()[0]).toEqual({ op: 'merge', uid: 's1', set: { answer: 'pass' }, unset: ['allow'] });
      await answer(withFirst({ allow: undefined, answer: 'pass' }));
      expect(field('allow.answers').disabled).toBe(true);
      await tick('[data-flag=free]', false);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { allow: 'pass' } });
    });

    it('offers what else the decision in hand offers, and the opponent has no "anything here"', async () => {
      const asked = decision({ options: [{ term: 'play(sword)', label: 'Sword', alias: 'sword', id: '9' }] });
      await open(
        withSteps([{ when: 'freeps, action', allow: 'pass' }, { when: 'shadow, action' }], {
          waiting: { human: asked, opponent: null },
        }),
      );
      expect([...el.querySelectorAll('[data-term]')].map((chip) => chip.getAttribute('data-term'))).toEqual([
        'play(sword)',
      ]);
      await click('[data-term="play(sword)"]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { allow: 'pass, play(sword)' } });
      await click('[data-go=next]');
      expect(el.querySelector('[data-flag=free]')).toBeNull();
    });

    it('says what a click on one particular card is told; a hint is sent with its text, and taken out at once', async () => {
      const board = {
        cards: [
          {
            alias: 'sword',
            blueprintId: '1_1',
            name: 'Sword',
            type: 'POSSESSION',
            owner: 'freeps' as const,
            cardId: 1,
            zone: 'hand',
            at: 0,
            wounds: 0,
            tokens: 0,
            on: null,
          },
        ],
        twilight: 0,
        burdens: 0,
        threats: 0,
        sites: {
          freeps: [{ number: 1, id: '1_319', alias: 'site1', name: null }],
          shadow: [{ number: 1, id: '1_319', alias: null, name: null }],
        },
      };
      await open(draftState({ board }));
      await choose('[data-act=add-hint]', 'sword');
      expect(edits()).toEqual([]);
      await type('allow.hints.sword', 'Not yet', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { allow: { answers: 'pass', hints: { sword: 'Not yet' } } } });
      await answer(withFirst({ allow: { answers: 'pass', hints: { sword: 'Not yet' } } }));
      await click('[data-alias=sword] button');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { allow: 'pass' } });
      expect([...el.querySelectorAll('[data-act=add-hint] option')].map((option) => option.textContent)).toEqual([
        '+ a hint for…',
        'sword',
        'site1',
      ]);
    });
  });

  describe('its messages', () => {
    it('puts a message in, empty and with the focus, to go with what is written in it', async () => {
      await open();
      await click('gd-author-message[ng-reflect-index], .gd-author-message[data-index="1"] [data-act=insert]');
      expect(field('messages.1.text').value).toBe('');
      expect(document.activeElement).toBe(field('messages.1.text'));
      expect(edits()).toEqual([]);
      await click('[data-act=add-message]');
      expect(document.activeElement).toBe(field('messages.3.text'));
      await type('messages.3.text', 'Last');
      await wait(700);
      expect((sentOps()[0]!['set'] as { messages: unknown[] }).messages).toEqual([
        { text: 'Hello' },
        { text: '' },
        { text: 'Two' },
        { text: 'Last' },
      ]);
    });

    it('moves one up and down in the step, and takes one out, at once', async () => {
      await open();
      await click('.gd-author-message[data-index="1"] [data-act=up]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Two' }, { text: 'Hello' }] } });
      await answer(withFirst({ messages: ['Two', 'Hello'] }));
      await click('.gd-author-message[data-index="0"] [data-act=down]');
      await wait(0);
      sentOps();
      await answer(withFirst({ messages: ['Hello', 'Two'] }));
      await click('.gd-author-message[data-index="0"] [data-act=delete]');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Two' }] } });
    });

    it('says a message every time, and where its bubble sits, tried on the table at once', async () => {
      await open();
      const tried = vi.spyOn(authoring, 'tryPlace');
      await tick('.gd-author-message[data-index="0"] [data-flag=once]', false);
      await choose('.gd-author-message[data-index="0"] [data-pick=side]', 'left');
      expect(tried).toHaveBeenLastCalledWith({ step: 1, sub: 0, index: 0 }, { side: 'left' });
      await choose('.gd-author-message[data-index="0"] [data-pick=side]', '');
      expect(tried).toHaveBeenLastCalledWith({ step: 1, sub: 0, index: 0 }, { side: 'auto' });
      await choose('.gd-author-message[data-index="0"] [data-pick=style]', 'red');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Hello', once: false, highlightStyle: 'red' }, { text: 'Two' }] },
      });
    });

    it('tries the gap on the table as it is typed, and changes it a little with the arrows', async () => {
      await open(draftState({ draft: { ...draftState().draft, options: { bubbleGap: '2' } } }));
      const tried = vi.spyOn(authoring, 'tryPlace');
      const gap = field('messages.0.gap');
      expect(gap.placeholder).toBe('2');
      await type('messages.0.gap', '20px');
      expect(tried).toHaveBeenLastCalledWith({ step: 1, sub: 0, index: 0 }, { gap: '20px' });
      gap.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowUp' }));
      await render();
      expect(gap.value).toBe('22px');
      await type('messages.0.gap', '');
      expect(tried).toHaveBeenLastCalledWith({ step: 1, sub: 0, index: 0 }, { gap: null });
      gap.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
      await render();
      expect(gap.value).toBe('1.75');
      await type('messages.0.gap', 'far');
      gap.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
      expect(gap.value).toBe('far');
      await wait(700);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Hello', gap: 'far' }, { text: 'Two' }] } });
    });

    it('puts the message whose card has the focus on the table', async () => {
      await open();
      const focusOn = vi.spyOn(authoring, 'focusOn');
      field('messages.1.text').dispatchEvent(new FocusEvent('focusin', { bubbles: true }));
      expect(focusOn).toHaveBeenCalledWith({ step: 1, sub: 0, index: 1 });
    });

    it('moves a message to the step before or after, with the rest unsent, and turns to it', async () => {
      await open(withSteps([{ when: 'a', messages: ['x'] }, { when: 'b', messages: ['y', 'z'] }, { when: 'c' }]), {
        index: 1,
      });
      expect(find<HTMLButtonElement>('.gd-author-message[data-index="0"] [data-act=later]').disabled).toBe(false);
      await type('note', 'unsent');
      await click('.gd-author-message[data-index="0"] [data-act=earlier]');
      expect(sentOps()).toEqual([
        { op: 'merge', uid: 's2', set: { note: 'unsent', messages: [{ text: 'z' }] }, unset: [] },
        { op: 'merge', uid: 's1', set: { messages: [{ text: 'x' }, { text: 'y' }] }, unset: [] },
      ]);
      expect(authoring.focusMessage()).toEqual({ step: 1, sub: 0, index: 1 });
      expect(authoring.ui()).toMatchObject({ selected: 1, hold: true });
    });

    it("makes a message and the ones before it a pause before the step (in a block, one of the block's)", async () => {
      await open();
      await click('.gd-author-message[data-index="0"] [data-act=pause]');
      expect(sentOps()).toEqual([
        { op: 'merge', uid: 's1', set: { messages: [{ text: 'Two' }] }, unset: [] },
        {
          op: 'insert',
          before: 's1',
          step: { pause: true, messages: [{ text: 'Hello' }], when: 'freeps, action, phase(fellowship), turn(1)' },
        },
      ]);
      await answer(state);
      const block = withSteps([{ when: 'a' }, { until: 'x', anyOrder: [{ when: 'b', messages: ['m'] }] }]);
      await update(block);
      authoring.select(1, 's2k1');
      await render();
      expect(el.querySelector('.gd-author-message [data-act=earlier]')).toBeNull();
      await click('.gd-author-message[data-index="0"] [data-act=pause]');
      expect(sentOps()[1]).toEqual({
        op: 'insert',
        before: 's2k1',
        step: { pause: true, messages: [{ text: 'm' }], when: 'b' },
      });
    });

    it("ends a pause's last message in a choice: buttons with names that will do, sent at once", async () => {
      await open(withSteps([{ pause: true, messages: ['Pick'] }, { when: 'b' }]));
      const name = find<HTMLInputElement>('[data-choice-name]');
      name.value = '1st';
      name.dispatchEvent(new Event('input'));
      await click('[data-act=add-choice]');
      expect(name.hasAttribute('data-bad')).toBe(true);
      name.value = 'brave';
      name.dispatchEvent(new Event('input'));
      name.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Pick', choices: { brave: 'brave' } }] } });
      await answer(withFirst({ messages: [{ text: 'Pick', choices: { brave: 'brave' } }] }));
      expect(find<HTMLDetailsElement>('.gd-author-message__more').open).toBe(true);
      expect(el.querySelector('[data-field="messages.0.on"]')).toBeNull();
      await click('[data-choice=brave] button');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { messages: [{ text: 'Pick' }] } });
    });

    it('lets its parts and how magnified be said only with something shown large', async () => {
      await open();
      expect(field('messages.0.focus').disabled).toBe(true);
      expect(field('messages.0.magnify').placeholder).toBe('(nothing)');
      await type('messages.0.zoom', 'card:frodo');
      expect(field('messages.0.focus').disabled).toBe(false);
      expect(field('messages.0.magnify').placeholder).toBe('1');
    });

    it('takes what a message points at as it is typed, or picked on the table; Outlines follows a pick while it is empty', async () => {
      await open();
      const first = '.gd-author-message[data-index="0"] gd-author-anchor';
      const input = find<HTMLInputElement>(`${first}[data-field=anchor] input`);
      input.focus();
      input.value = 'ui:phase';
      input.dispatchEvent(new Event('input', { bubbles: true }));
      await wait(500);
      expect(edits()).toEqual([]);
      await wait(300);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Hello', anchor: 'ui:phase' }, { text: 'Two' }] },
      });
      await answer(withFirst({ messages: [{ text: 'Hello', anchor: 'ui:phase' }, 'Two'] }));
      expect(document.activeElement).toBe(input);

      const picker = TestBed.inject(TablePicker);
      vi.spyOn(picker, 'fightingSide').mockReturnValue(null);
      vi.spyOn(picker, 'pick').mockResolvedValue('card:frodo');
      await click(`${first}[data-field=anchor] button[data-pick=replace]`);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Hello', anchor: 'card:frodo', highlight: 'card:frodo' }, { text: 'Two' }] },
      });
      await answer(withFirst({ messages: [{ text: 'Hello', anchor: 'card:frodo', highlight: 'card:frodo' }, 'Two'] }));

      // Outlines that says something of its own stays as it is; + Pick adds to it, and to what is kept clear of
      vi.spyOn(picker, 'pick').mockResolvedValue('ui:twilightPool');
      await click(`${first}[data-field=anchor] button[data-pick=replace]`);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ anchor: 'ui:twilightPool', highlight: 'card:frodo' }, { text: 'Two' }] },
      });
      await answer(
        withFirst({ messages: [{ text: 'Hello', anchor: 'ui:twilightPool', highlight: 'card:frodo' }, 'Two'] }),
      );
      await click(`${first}[data-field=highlight] button[data-pick=add]`);
      await click(`${first}[data-field=avoid] button[data-pick=add]`);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: {
          messages: [{ highlight: 'card:frodo, ui:twilightPool', avoid: 'ui:twilightPool' }, { text: 'Two' }],
        },
      });
    });

    it("chooses what a message shows large from the server's pictures", async () => {
      await open();
      const card = '.gd-author-message[data-index="0"]';
      expect(el.querySelector('gd-author-pictures')).toBeNull();
      await click(`${card} [data-act=pictures]`);
      backend
        .expectOne((r) => r.url === `${AUTHOR}images`)
        .flush(JSON.stringify({ images: [{ name: 'a.png', url: '/gemp-lotr/images/scenarios/a.png' }] }));
      await render();
      await click(`${card} gd-author-pictures button[data-name="a.png"]`);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { messages: [{ text: 'Hello', zoom: 'image:a.png' }, { text: 'Two' }] },
      });
      expect(el.querySelector('gd-author-pictures')).toBeNull();
      expect(field('messages.0.zoom').value).toBe('image:a.png');
      // (and Picture… again puts the chooser away)
      await click(`${card} [data-act=pictures]`);
      backend.match((r) => r.url === `${AUTHOR}images`);
      await click(`${card} [data-act=pictures]`);
      expect(el.querySelector('gd-author-pictures')).toBeNull();
    });

    it('ends the scenario, with messages of its own that point at nothing and go when a field is left', async () => {
      await open();
      expect(el.querySelector('gd-author-messages[data-closing], .gd-author-messages[data-closing]')).toBeNull();
      await choose('[data-pick=end]', 'complete');
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { end: 'complete' } });
      await answer(withFirst({ end: 'complete' }));
      await click('.gd-author-messages[data-closing] [data-act=add-message]');
      const closing = find('.gd-author-messages[data-closing]');
      expect(closing.querySelector('[data-act=earlier], [data-flag=once]')).toBeNull();
      await type('end.messages.0.text', 'Well done');
      await wait(700);
      expect(edits()).toEqual([]);
      await type('end.messages.0.header', 'Done', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({
        set: { end: { result: 'complete', messages: [{ text: 'Well done', header: 'Done' }] } },
      });
    });
  });

  describe('what is done with the step as a whole', () => {
    it('plays to it, moves it, puts a step in either side of it', async () => {
      await open(draftState({ position: position({ step: 3 }) }), { index: 1 });
      await click('[data-act=play]');
      expect(sent('goto')?.get('step')).toBe('2');
      await click('[data-act=earlier]');
      expect(sentOps()).toEqual([{ op: 'move', uid: 's2', before: 's1' }]);
      await answer(state);
      authoring.select(1);
      await render();
      await click('[data-act=later]');
      expect(sentOps()).toEqual([{ op: 'move', uid: 's2', before: null }]);
      await answer(state);
      authoring.select(1);
      await render();
      await click('[data-act=before]');
      expect(sentOps()).toEqual([{ op: 'insert', before: 's2', step: { when: 'freeps, action' }, awaiting: true }]);
      await answer(state);
      authoring.select(1);
      await render();
      await update({ ...state, uncovered: { ...UNCOVERED, after: 2 }, position: position({ step: 3, inHand: false }) });
      authoring.select(1);
      await render();
      await click('[data-act=after]');
      expect(sent('here')).toBeDefined();
    });

    it('takes it out once asked, a block with what is in it', async () => {
      await open(draftState(), { index: 2 });
      const notifier = TestBed.inject(FakeNotifier);
      notifier.answers.push(false);
      await click('[data-act=delete]');
      expect(notifier.dialogs).toEqual(['confirm: Take step 3 out of the script, with the 2 steps in it?']);
      expect(edits()).toEqual([]);
      notifier.answers.push(true);
      authoring.select(1);
      await render();
      await click('[data-act=delete]');
      await render();
      expect(sentOps()).toEqual([{ op: 'delete', uid: 's2' }]);
      expect(authoring.ui().selected).toBe(1);
    });

    it('joins the steps after it that wait for the same', async () => {
      await open(
        withSteps([
          { when: 'freeps, action', allow: 'heal(a)' },
          { when: 'freeps, action', allow: 'heal(b)' },
        ]),
      );
      expect(text('[data-act=join]')).toBe('Join next');
      await click('[data-act=join]');
      expect(sentOps()).toEqual([
        {
          op: 'merge',
          uid: 's1',
          set: { until: '', allow: 'heal(a), heal(b)', answer: 'heal(a), heal(b)' },
          unset: [],
        },
        { op: 'delete', uid: 's2' },
      ]);
    });

    it('makes it a block, or near the skirmishes a block or the skirmishes; a block there the skirmishes', async () => {
      await open(withSteps([{ when: 'freeps, action, phase(fellowship)' }, { when: 'b' }, { when: 'freeps, assign' }]));
      await click('[data-act=convert]');
      expect(sentOps()[0]).toMatchObject({ op: 'set', uid: 's1' });
      await answer(state);
      await update(withSteps([{ when: 'freeps, action, phase(maneuver)' }, { when: 'freeps, assign' }]));
      authoring.select(0);
      await render();
      await choose('[data-act=convert]', 'skirmishes');
      expect(sentOps()[0]).toMatchObject({ op: 'set', step: { skirmishes: true } });
      await answer(state);
      await update(withSteps([{ when: 'a' }, { until: 'phase(regroup)', anyOrder: [] }, { when: 'freeps, assign' }]));
      authoring.select(1);
      await render();
      expect(el.querySelector('[data-act=into-before], [data-act=into-after]')).toBeNull();
      await click('[data-act=skirmishes]');
      expect(sentOps()).toEqual([{ op: 'merge', uid: 's2', set: { skirmishes: true } }]);
    });

    it('makes it one of the steps of the block beside it, and says what of it was left out when the file was opened', async () => {
      const leftOut = [{ where: 'step 2.allow', message: 'no such card', was: 'allow: x', step: 2, uid: 's2' }];
      await open(
        withSteps([{ until: 'x', anyOrder: [{ when: 'a' }] }, { when: 'b' }, { until: 'y', anyOrder: [] }], {
          leftOut,
        }),
        {
          index: 1,
        },
      );
      expect(text('.gd-author-step-actions__left-out div')).toBe("Left out of 'allow': no such card");
      expect(text('.gd-author-step-actions__left-out pre')).toBe('allow: x');
      await click('[data-act=into-before]');
      expect(sentOps()).toEqual([{ op: 'move', uid: 's2', into: 's1' }]);
      expect(authoring.ui()).toMatchObject({ selected: 1, subAt: 1, hold: true });
      await answer(state);
      authoring.select(1);
      await render();
      await click('[data-act=into-after]');
      expect(sentOps()).toEqual([{ op: 'move', uid: 's2', into: 's3' }]);
    });
  });

  describe("a block's page", () => {
    it('has a section for the block and one for each of its steps; one is open, with the form', async () => {
      await open(draftState(), { index: 2 });
      expect([...el.querySelectorAll('.gd-author-panel')].map((panel) => panel.getAttribute('data-sub'))).toEqual([
        '-1',
        '0',
        '1',
      ]);
      expect(find('.gd-author-panel[data-sub="-1"]').hasAttribute('data-open')).toBe(true);
      const title = [...find('.gd-author-panel[data-sub="0"] .gd-author-panel__title').children].map((part) =>
        part.textContent?.trim(),
      );
      expect(title).toEqual(['▸', '3.1', 'You', 'assignpass', 'once']);
      expect(text('.gd-author-panel[data-sub="0"] .gd-author-panel__act')).toBe('pass');
      expect(text('[data-kids]')).toBe('Its 2 steps, in any order');
      expect(text('gd-author-when .gd-author-step__heading')).toBe('Covers the decisions that are');
      await click('.gd-author-panel[data-sub="1"] .gd-author-panel__title');
      expect(authoring.ui().sub).toBe('s3k2');
      expect(find('.gd-author-panel[data-sub="1"]').hasAttribute('data-open')).toBe(true);
      expect(text('gd-author-when .gd-author-step__heading')).toBe('Applies to each decision that is');
      expect(text('gd-author-allow summary')).toBe('Wrong clicks');
    });

    it("shows what a closed step says, and opens it from there; the block's own section from its head", async () => {
      await open(
        withSteps([
          { when: 'a' },
          {
            until: 'x',
            anyOrder: [{ when: 'b', messages: ['Hi there'], do: 'draw(1)', checkpoint: true, end: 'fail' }],
          },
        ]),
        {
          index: 1,
          sub: 's2k1',
        },
      );
      await click('.gd-author-panel[data-sub="-1"] .gd-author-panel__title');
      expect(authoring.ui().sub).toBeNull();
      expect(
        [...el.querySelectorAll('.gd-author-panel[data-sub="0"] [data-tag]')].map((tag) =>
          tag.getAttribute('data-tag'),
        ),
      ).toEqual(['do', 'messages', 'checkpoint', 'end']);
      await click('.gd-author-panel__says');
      expect(authoring.ui().sub).toBe('s2k1');
    });

    it('plays to a step of it, moves it about, out of the block, and takes it out', async () => {
      await open(draftState({ position: position({ step: 4, inHand: false }) }), { index: 2, sub: 's3k1' });
      const panel = '.gd-author-panel[data-sub="0"]';
      expect(find<HTMLButtonElement>(`${panel} [data-act=up]`).disabled).toBe(true);
      await click(`${panel} [data-act=play]`);
      expect(sent('goto')?.toString()).toBe('step=3&sub=1');
      await click(`${panel} [data-act=down]`);
      expect(sentOps()).toEqual([{ op: 'move', uid: 's3k1', into: 's3' }]);
      await answer(state);
      await click('.gd-author-panel[data-sub="1"] [data-act=up]');
      expect(sentOps()).toEqual([{ op: 'move', uid: 's3k2', before: 's3k1' }]);
      await answer(state);
      await click(`${panel} [data-act=out]`);
      expect(sentOps()).toEqual([{ op: 'move', uid: 's3k1', before: null }]);
      await answer(state);
      authoring.select(2, 's3k1');
      await render();
      const notifier = TestBed.inject(FakeNotifier);
      notifier.answers.push(true);
      await click(`${panel} [data-act=delete]`);
      await render();
      expect(notifier.dialogs).toEqual(['confirm: Take step 3.1 out of the script?']);
      expect(sentOps()).toEqual([{ op: 'delete', uid: 's3k1' }]);
    });

    it('puts steps in: before one (for the decision it holds), after the others, a pause', async () => {
      await open(draftState({ position: position({ step: 3, sub: 2 }) }), { index: 2 });
      await click('.gd-author-panel[data-sub="0"] [data-act=insert]');
      expect(sentOps()).toEqual([
        { op: 'insert', step: { once: true, when: 'freeps, action' }, before: 's3k1', awaiting: true },
      ]);
      await answer(state);
      await click('.gd-author-panel[data-sub="1"] [data-act=insert]');
      expect(sent('before')).toBeDefined();
      expect(authoring.ui()).toMatchObject({ jump: true, tab: 'step' });
      await click('gd-author-block-page [data-add=pause]');
      expect(sentOps()).toEqual([
        { op: 'insert', step: { pause: true, when: 'freeps, action', messages: [{ text: '...' }] }, into: 's3' },
      ]);
      await answer(state);
      await click('gd-author-block-page [data-add=step]');
      expect(sentOps()[0]).toMatchObject({ op: 'insert', into: 's3', awaiting: true });
      await answer(state);
      await update({ ...state, uncovered: { ...UNCOVERED, block: 3 }, position: position({ step: 3, inHand: false }) });
      await click('gd-author-block-page [data-add=step]');
      expect(sent('here')).toBeDefined();
    });

    it('says a block with no steps has none', async () => {
      await open(withSteps([{ until: 'x', anyOrder: [] }]));
      expect(text('[data-kids]')).toBe('It has no steps yet');
      await tick('[data-flag=skirmishes]', true);
      await wait(0);
      expect(sentOps()[0]).toMatchObject({ set: { skirmishes: true } });
      await answer(withFirst({ skirmishes: true }));
      expect(el.querySelector('gd-author-skirmish-page')).not.toBeNull();
    });
  });

  describe('the skirmishes page', () => {
    const board = {
      cards: [
        {
          alias: 'frodo',
          blueprintId: '1_290',
          name: 'Frodo, Son of Drogo',
          type: 'COMPANION',
          owner: 'freeps' as const,
          cardId: 1,
          zone: 'characters',
          at: null,
          wounds: 0,
          tokens: 0,
          on: null,
        },
        {
          alias: 'orc',
          blueprintId: '1_1',
          name: 'Goblin Runner',
          type: 'MINION',
          owner: 'shadow' as const,
          cardId: 2,
          zone: 'minions',
          at: null,
          wounds: 0,
          tokens: 0,
          on: null,
        },
      ],
      twilight: 0,
      burdens: 0,
      threats: 0,
      sites: { freeps: [], shadow: [] },
    };
    const block: DraftStep = {
      when: 'turn(1)',
      until: 'phase(regroup)',
      skirmishes: true,
      anyOrder: [{ when: 'freeps, assign', once: true }, { when: 'freeps, action, inSkirmish(frodo)' }],
    };

    it("lays the block's steps out by skirmish, and adds one where it goes", async () => {
      await open(withSteps([block], { board }));
      expect(
        [...el.querySelectorAll('.gd-author-skirmish__group')].map((group) => group.getAttribute('data-kind')),
      ).toEqual(['before', 'first', 'fight', 'after']);
      expect(text('.gd-author-skirmish__group[data-kind=fight] .gd-author-step__heading')).toBe("Frodo's skirmish");
      await click('.gd-author-skirmish__group[data-kind=first] [data-add=step]');
      expect(sentOps()).toEqual([
        { op: 'insert', step: { once: true, when: 'freeps, cards, nextSkirmish' }, into: 's1', awaiting: true },
      ]);
      await answer(state);
      await click('.gd-author-skirmish__group[data-kind=before] .gd-author-panel[data-sub="0"] [data-act=down]');
      expect(edits()).toEqual([]);
    });

    it('gives a minion a skirmish of its own, and writes the step for the decision waiting where it is that one', async () => {
      const open1 = { ...UNCOVERED, block: 1, when: 'freeps, cards, text(next skirmish)' };
      await open(withSteps([block], { board, uncovered: open1, position: position({ inHand: false }) }));
      await choose('[data-add=fight]', 'orc');
      expect(authoring.ui().newFights).toEqual(['orc']);
      expect(text('.gd-author-skirmish__group[data-who=orc] .gd-author-step__heading')).toBe(
        'Any skirmish Goblin Runner (orc) is in',
      );
      await click('.gd-author-skirmish__group[data-kind=first] [data-add=step]');
      expect(sent('here')).toBeDefined();
      await click('gd-author-skirmish-page > .gd-author-bar [data-add=pause]');
      expect(sentOps()[0]).toMatchObject({ op: 'insert', into: 's1', step: { pause: true } });
    });
  });

  it('starts at the top of a step turned to', async () => {
    await open();
    const scroller = document.createElement('div');
    scroller.style.overflowY = 'auto';
    document.body.appendChild(scroller);
    scroller.appendChild(el);
    const scrollTo = vi.fn();
    scroller.scrollTo = scrollTo;
    await click('[data-go=next]');
    expect(scrollTo).toHaveBeenCalledWith({ top: 0 });
  });
});
