import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { AuthoringState, DraftState } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { decision, draftGameXml, draftState, position } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { TimelineTabComponent } from './timeline-tab.component';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

describe('TimelineTabComponent', () => {
  let fixture: ComponentFixture<TimelineTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;

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
        CardInfoService,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    fixture = TestBed.createComponent(TimelineTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  async function open(state: AuthoringState = draftState()) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(state));
    await settle();
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function update(state: DraftState) {
    authoring.setState(state);
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function click(selector: string) {
    const target = el.querySelector<HTMLElement>(selector);
    expect(target, selector).not.toBeNull();
    target!.click();
    await settle();
    fixture.detectChanges();
  }

  /** The one call to `what` sent: its form. */
  function sent(what: string): TestRequest['request']['body'] {
    const requests = backend.match((r) => r.url === AUTHOR + what && r.method === 'POST');
    expect(requests).toHaveLength(1);
    return requests[0]!.request.body;
  }

  const patch = () => JSON.parse(sent('edit')!.get('patch')!) as { steps: unknown[] };
  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;
  const rows = () =>
    [...el.querySelectorAll<HTMLElement>('.gd-author-timeline__row')].map((row) => ({
      step: row.getAttribute('data-step'),
      phase: row.getAttribute('data-phase'),
      current: row.hasAttribute('data-current'),
      selected: row.hasAttribute('data-selected'),
      sub: row.hasAttribute('data-sub'),
      who: row.querySelector('.gd-author-timeline__who')?.textContent,
      what: row.querySelector('.gd-author-timeline__what')?.textContent?.replace(/\s+/g, ' ').trim(),
      tags: [...row.querySelectorAll('.gd-author-timeline__tag')].map((tag) => tag.textContent),
    }));

  it('lists the steps in groups, the current one marked, the selected one too', async () => {
    await open();
    expect([...el.querySelectorAll('.gd-author-timeline__group')].map((group) => group.textContent?.trim())).toEqual([
      'Turn 1 · Fellowship',
      'Turn 1 · Shadow',
      'Turn 1 · Several phases, in any order',
    ]);
    expect(rows()).toEqual([
      {
        step: '1',
        phase: 'fellowship',
        current: true,
        selected: true,
        sub: false,
        who: 'You',
        what: 'action fellowship t1pass',
        tags: ['▬2'],
      },
      {
        step: '2',
        phase: 'shadow',
        current: false,
        selected: false,
        sub: false,
        who: 'Opp',
        what: 'action shadow t1play(runner)',
        tags: [],
      },
      {
        step: '3',
        phase: 'block',
        current: false,
        selected: false,
        sub: false,
        who: 'Block',
        what: 't1any order, until phase(regroup)',
        tags: [],
      },
      {
        step: '3.1',
        phase: 'block',
        current: false,
        selected: false,
        sub: true,
        who: 'You',
        what: 'assignpass',
        tags: ['once'],
      },
      {
        step: '3.2',
        phase: 'block',
        current: false,
        selected: false,
        sub: true,
        who: 'You',
        what: 'action inSkirmish(frodo)play(sword)',
        tags: [],
      },
    ]);
  });

  it('marks a silent pass and a step with a name of its own', async () => {
    await open(draftState({ draft: { id: 'x', steps: [{ when: 'shadow, action', allow: 'pass', id: 'quiet' }] } }));
    const row = el.querySelector('.gd-author-timeline__row')!;
    expect(row.hasAttribute('data-silent')).toBe(true);
    expect(row.getAttribute('title')).toContain('A pass the game makes by itself');
    expect(text('.gd-author-timeline__id')).toBe('quiet');
  });

  it("turns to a step in the Step tab, or one of a block's", async () => {
    await open();
    await click('.gd-author-timeline__row[data-step="3.2"] .gd-author-timeline__pick');
    expect(authoring.ui()).toMatchObject({ tab: 'step', selected: 3, sub: 's3k2' });
    await click('.gd-author-timeline__row[data-step="2"] .gd-author-timeline__pick');
    expect(authoring.ui()).toMatchObject({ selected: 2, sub: null });
  });

  it("plays to a step, or to one of a block's", async () => {
    await open();
    await click('.gd-author-timeline__row[data-step="2"] .gd-author-timeline__go');
    expect(sent('goto')?.toString()).toBe('step=2');
    await click('.gd-author-timeline__row[data-step="3.2"] .gd-author-timeline__go');
    expect(sent('goto')?.toString()).toBe('step=3&sub=2');
  });

  it('goes to the start, the last checkpoint, back one, this step and the end', async () => {
    const steps = [{ when: 'a' }, { when: 'b', checkpoint: true }, { when: 'c' }, { when: 'd' }];
    await open(draftState({ draft: { id: 'x', steps }, position: position({ step: 4 }) }));
    for (const [go, step] of [
      ['start', '1'],
      ['checkpoint', '2'],
      ['back', '3'],
      ['this', '4'],
      ['end', '5'],
    ]) {
      await click(`[data-go=${go}]`);
      expect(sent('goto')?.get('step')).toBe(step);
    }
  });

  it('changes the options, and the forks taken', async () => {
    await open(draftState({ flags: ['plan', 'help'], way: ['plan'] }));
    const tick = async (selector: string, checked: boolean) => {
      const box = el.querySelector<HTMLInputElement>(`${selector} input`)!;
      box.checked = checked;
      box.dispatchEvent(new Event('change'));
      await settle();
      fixture.detectChanges();
    };
    await tick('[data-option=recording]', false);
    expect(sent('settings')?.toString()).toBe('recording=false');
    await tick('[data-option=ask-opponent]', false);
    expect(sent('settings')?.toString()).toBe('askOpponent=false');
    await tick('[data-flag=help]', true);
    expect(sent('settings')?.get('way')).toBe('plan,help');
    await tick('[data-flag=plan]', false);
    expect(sent('settings')?.get('way')).toBe('');
    await tick('[data-option=follow]', false);
    expect(authoring.ui().follow).toBe(false);
  });

  it('puts steps in: an opening, a pause before the selected step, a block or the skirmishes after it', async () => {
    await open(draftState({ position: position({ step: 2, turn: 2 }) }));
    authoring.select(1);
    fixture.detectChanges();
    await click('[data-add=opening]');
    expect(patch().steps).toEqual([
      { op: 'insert', before: 's1', step: { when: 'opening', messages: [{ text: '...' }] } },
    ]);
    authoring.select(1);
    await click('[data-add=pause]');
    expect(patch().steps).toEqual([{ op: 'insert', before: 's2', step: { pause: true, messages: [{ text: '...' }] } }]);
    authoring.select(1);
    await click('[data-add=block]');
    expect(patch().steps).toEqual([
      { op: 'insert', before: 's3', step: { until: 'phase(regroup)', anyOrder: [], when: 'turn(2)' } },
    ]);
    authoring.select(1);
    await click('[data-add=skirmishes]');
    expect(patch().steps).toEqual([
      {
        op: 'insert',
        before: 's3',
        step: { skirmishes: true, until: 'phase(regroup)', anyOrder: [], when: 'turn(2)' },
      },
    ]);
    authoring.select(1);
    await click('[data-add=step]');
    expect(patch().steps).toEqual([{ op: 'insert', before: 's3', step: { when: 'freeps, action' }, awaiting: true }]);
  });

  it('puts a step in at the end with nothing selected, and offers no opening where there is one', async () => {
    await open(draftState({ draft: { id: 'x', steps: [{ when: 'opening' }] }, uids: ['s1'], kids: [[]] }));
    authoring.remember({ selected: null });
    fixture.detectChanges();
    expect(el.querySelector('[data-add=opening]')).toBeNull();
    await click('[data-add=pause]');
    expect(patch().steps[0]).toMatchObject({ before: null });
    await click('[data-add=block]');
    expect(patch().steps[0]).toMatchObject({
      before: null,
      step: { until: 'phase(regroup)', anyOrder: [], when: 'turn(1)' },
    });
  });

  it('writes the step for the decision no step covers when "+ Step after" would put it just there', async () => {
    const free = position({ inHand: false });
    const uncovered = {
      seat: 'human' as const,
      after: 1,
      when: 'x',
      repeating: null,
      until: null,
      becomes: null,
      problem: null,
      block: null,
      sub: 0,
      reuses: null,
    };
    await open(draftState({ position: free, uncovered }));
    authoring.select(0);
    await click('[data-add=step]');
    expect(sent('here')).toBeDefined();
  });

  it('says what is under the last row: the end of the game, the end of the script, the decision no step has', async () => {
    await open(draftState({ position: position({ over: true, result: 'complete' }) }));
    expect(text('.gd-author-timeline__end')).toBe('The game is over (complete). Go back to a step to carry on.');
    expect(el.querySelector<HTMLButtonElement>('[data-author=rewrite]')!.disabled).toBe(true);
    await update(draftState({ position: position({ frontier: true, inHand: false, step: 4 }) }));
    const end = el.querySelector('.gd-author-timeline__end')!;
    expect(end.hasAttribute('data-current')).toBe(true);
    expect(text('.gd-author-timeline__end')).toBe(
      'The script ends here. What you do from here on is written down.You are being asked: Play Fellowship action or Pass (turn 1, fellowship) Add a step for this decision',
    );
    await click('.gd-author-timeline__end [data-author=add-here]');
    expect(sent('here')).toBeDefined();
    await update(
      draftState({
        position: position({ inHand: false }),
        waiting: {
          human: null,
          opponent: decision({ seat: 'shadow', held: true, text: 'Play Shadow action', where: 'w' }),
        },
      }),
    );
    expect(text('.gd-author-timeline__asked')).toBe('The opponent is being asked: Play Shadow action (w)');
  });

  it('rewrites from here, once the author has said so', async () => {
    await open(draftState({ position: position({ step: 3 }) }));
    TestBed.inject(FakeNotifier).answers.push(true);
    await click('[data-author=rewrite]');
    await settle();
    expect(patch().steps).toEqual([{ op: 'delete', uid: 's3' }]);
  });

  it('tests from the selected step, or from the start', async () => {
    await open();
    authoring.select(2);
    await click('[data-author=test-here]');
    expect(sent('test')?.get('step')).toBe('3');
    authoring.remember({ selected: null });
    await click('[data-author=test-here]');
    expect(sent('test')?.get('step')).toBe('1');
    await click('[data-author=test-start]');
    expect(sent('test')?.get('step')).toBe('1');
  });

  it('brings the list to where the game is when the game has moved, if it follows the game', async () => {
    const steps = Array.from({ length: 30 }, (_, i) => ({ when: `freeps, turn(${i + 1})` }));
    await open(draftState({ draft: { id: 'x', steps } }));
    const list = el.querySelector<HTMLElement>('.gd-author-timeline__list')!;
    const rowsOf = [...el.querySelectorAll<HTMLElement>('.gd-author-timeline__row')];
    rowsOf.forEach((row, i) => Object.defineProperty(row, 'offsetTop', { configurable: true, value: i * 20 }));
    await update(draftState({ draft: { id: 'x', steps }, position: position({ step: 20 }) }));
    expect(list.scrollTop).toBe(19 * 20 - 60);
    list.scrollTop = 5;
    // (the same place: the list stays where it was scrolled)
    await update(draftState({ draft: { id: 'x', steps }, position: position({ step: 20 }), dirty: true }));
    expect(list.scrollTop).toBe(5);
    authoring.setFollow(false);
    await update(draftState({ draft: { id: 'x', steps }, position: position({ step: 3 }) }));
    expect(list.scrollTop).toBe(5);
  });

  it('shows nothing without a draft', () => {
    fixture.detectChanges();
    expect(el.children).toHaveLength(0);
  });

  it('does nothing with its buttons once the draft is gone', async () => {
    await open();
    const instance = fixture.componentInstance as unknown as {
      addPause(): void;
      addStep(): void;
      addBlock(s: boolean): void;
      choose(f: string, on: boolean): void;
    };
    authoring.setState({ session: false });
    instance.addPause();
    instance.addStep();
    instance.addBlock(true);
    instance.choose('plan', true);
    expect(backend.match((r) => r.url === `${AUTHOR}edit`)).toEqual([]);
    expect(sent('settings')?.get('way')).toBe('plan');
  });
});
