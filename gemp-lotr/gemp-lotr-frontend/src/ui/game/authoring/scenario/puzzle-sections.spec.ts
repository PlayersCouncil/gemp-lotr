import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal, Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardHover } from '../../../../core/cards/card-hover.service';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { CardViews } from '../../../../core/cards/card-views.service';
import { Draft, DraftState, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { PuzzleState } from '../../../../core/game/authoring/puzzle-state';
import { provideGameState } from '../../../../core/game/game-state';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import { draftState, puzzleSlot, puzzleState, result } from '../../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { AuthoringNoticesComponent } from '../authoring-notices.component';
import { SEARCH_MS } from '../widgets/card-search.component';
import { solutionVerdicts } from '../../../../core/game/authoring/puzzle-solutions';
import { AboutSectionComponent } from './about-section.component';
import { JudgingComponent } from './judging.component';
import { PlaysSectionComponent } from './plays-section.component';
import { PuzzleRulesComponent } from './puzzle-rules.component';
import { SolutionsSectionComponent } from './solutions-section.component';
import { parseJson } from '../../../../core/api/json';
import { sentForm } from '../../../../testing/sent-form';
import { AUTHOR, authorCall, authorCalls, authorForm, sentPatch } from '../../../../testing/authoring-requests';

const BOUND = '/gemp-lotr-server/scenarios/author/bound';

const WIN = [{ type: 'canSpot', filter: 'alias(frodo)' }];

/** Puzzle 10, as far as these sections care. */
const PUZZLE_DRAFT: Draft = {
  id: 'test',
  kind: 'puzzle',
  human: 'freeps',
  steps: [],
  cards: { freeps: { frodo: '2_102', eomer: '4_267' }, shadow: { troop1: '4_182' } },
  options: { autoPass: 'fellowship, archery', opponentDelayMs: 0 },
  rules: { oneSided: true, forbid: 'shuffle' },
  pool: 'set:1-6',
  slots: {
    condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', label: 'a Rohan condition' },
    ally: { zone: 'support', filter: 'culture:ROHAN cardtype:ALLY', label: 'a Rohan ally' },
  },
  outcome: { winMessages: ['Every companion came through.'], judge: { at: 'endOf(archery)', win: WIN } },
  solutions: [
    {
      name: 'published',
      note: 'Parapet, then Aldor.',
      picks: { condition: '5_87', ally: '4_262' },
      answers: ['use(condition)', 'choose(ally)'],
    },
  ],
};

const SLOTS = [
  puzzleSlot('condition'),
  puzzleSlot('ally', {
    label: 'a Rohan ally',
    filter: 'culture:ROHAN cardtype:ALLY',
    bound: 'set:1-6 culture:ROHAN cardtype:ALLY',
    pick: '4_262',
    pickName: 'Aldor, Soldier of Edoras',
  }),
];

const PUZZLE: PuzzleState = puzzleState({
  undo: false,
  judge: {
    at: 'endOf(archery)',
    moment: 'endOf',
    phase: 'archery',
    turn: null,
    win: '[\n\t{\n\t\ttype: canSpot\n\t}\n]',
  },
  forbid: ['shuffle'],
  pool: 'set:1-6',
  slots: SLOTS,
  solutions: [
    {
      name: 'published',
      note: 'Parapet, then Aldor.',
      picks: { condition: '5_87', ally: '4_262' },
      pickNames: { condition: 'Parapet', ally: 'Aldor, Soldier of Edoras' },
      answers: ['use(condition)', 'choose(ally)'],
      text: '',
    },
  ],
  picks: { condition: '5_87', ally: '4_262' },
  playingWith: 'published',
});

const puzzle = (extra: Partial<DraftState> = {}, draft: Partial<Draft> = {}): DraftState =>
  draftState({ draft: { ...PUZZLE_DRAFT, ...draft }, puzzle: PUZZLE, ...extra });

const ALDOR: FoundCard = {
  id: '4_262',
  name: 'Aldor, Soldier of Edoras',
  type: 'ally',
  culture: 'rohan',
  side: 'free_people',
  cost: 1,
};

describe('Scenario tab: what puzzles and sandboxes brought', () => {
  let fixture: ComponentFixture<unknown>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let notifier: FakeNotifier;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        CardHover,
        CardViews,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    notifier = TestBed.inject(FakeNotifier);
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  async function show<T>(component: Type<T>, state: DraftState, input = true) {
    authoring.setState(state);
    fixture = TestBed.createComponent(component);
    el = fixture.nativeElement as HTMLElement;
    if (input) fixture.componentRef.setInput('state', state);
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
  const all = (selector: string) => [...el.querySelectorAll<HTMLElement>(selector)];

  async function type(selector: string, value: string) {
    const field = find<HTMLInputElement | HTMLTextAreaElement>(selector);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    await settle();
    await render();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await settle();
    await render();
  }

  async function tick(selector: string, on: boolean) {
    const box = find<HTMLInputElement>(`${selector} input`);
    box.checked = on;
    box.dispatchEvent(new Event('change'));
    await settle();
    await render();
  }

  async function click(selector: string) {
    find(selector).click();
    await settle();
    await render();
  }

  const posted = (what: string) => authorCalls(backend, what);

  /** The one edit sent: its patch. */
  function patch(): Record<string, unknown> {
    return sentPatch(authorCall(backend, 'edit')) as Record<string, unknown>;
  }

  /** The one call of /author/solution: its form. */
  function solutionCall(): Record<string, string | null> {
    const body = authorForm(backend, 'solution');
    return { op: body.get('op'), name: body.get('name'), picks: body.get('picks') };
  }

  /** Answers the searches asked (the filter field's count), each with this total. */
  async function searched(total: number): Promise<string[]> {
    const requests = backend.match((r) => r.url === BOUND);
    for (const request of requests) request.flush(JSON.stringify({ ok: true, why: null, total }));
    await settle();
    await render();
    // (a slot's bound is counted with the pool, each searched by itself: `filter | pool`)
    return requests.map((request: TestRequest) =>
      [request.request.params.get('filter') ?? '', request.request.params.get('pool')].filter(Boolean).join(' | '),
    );
  }

  /* ---------------------------------------------------------------- kind and tools (es 4) */

  it('offers the kinds the server names, and a sandbox’s tools under it, each tick sending the options', async () => {
    const tools = {
      names: ['add', 'copy', 'move', 'modify'],
      notBuilt: ['modify'],
      said: false,
      allowed: ['add', 'copy', 'move', 'modify'],
    };
    const state = draftState({
      kinds: ['tutorial', 'sandbox'],
      sandboxTools: tools,
      draft: { id: 'test', kind: 'sandbox', steps: [], options: { autoPass: 'fellowship' } },
    });
    await show(AboutSectionComponent, state);
    expect(all('gd-author-select[data-part=kind] option').map((one) => one.getAttribute('value'))).toEqual([
      'tutorial',
      'sandbox',
    ]);
    expect(all('[data-part=tools] gd-author-check').map((one) => one.getAttribute('data-tool'))).toEqual(tools.names);
    expect(find('[data-part=all-tools]').textContent?.trim()).toBe('(all)');
    expect(find<HTMLInputElement>('[data-tool=modify] input').disabled).toBe(true);
    await tick('[data-tool=copy]', false);
    expect(patch()).toEqual({ top: { options: { autoPass: 'fellowship', tools: 'add, move, modify' } } });
  });

  it('sends every tool as the field left out, and shows no tools for another kind', async () => {
    const tools = { names: ['add', 'move'], notBuilt: [], said: true, allowed: ['add'] };
    const state = draftState({
      sandboxTools: tools,
      draft: { id: 'test', kind: 'sandbox', steps: [], options: { tools: 'add' } },
    });
    await show(AboutSectionComponent, state);
    expect(el.querySelector('[data-part=all-tools]')).toBeNull();
    await tick('[data-tool=move]', true);
    expect(patch()).toEqual({ top: { options: null } });
    await show(AboutSectionComponent, draftState({ sandboxTools: tools }));
    expect(el.querySelector('[data-part=tools]')).toBeNull();
  });

  it("offers who plays a sandbox's other seat from the server's choices, sending the whole options", async () => {
    const state = draftState({
      sandboxTools: { names: ['add'], notBuilt: [], said: false, allowed: ['add'] },
      sandboxOtherSeat: { choices: ['script', 'bot', 'person'], said: false, value: 'script' },
      draft: { id: 'test', kind: 'sandbox', steps: [], options: { autoPass: 'fellowship' } },
    });
    await show(AboutSectionComponent, state);
    expect(all('gd-author-select[data-part=other-seat] option').map((one) => one.textContent)).toEqual([
      'script',
      'bot',
      'you',
    ]);
    expect(find('[data-part=seat-unsaid]').textContent?.trim()).toBe('(script)');
    await choose('gd-author-select[data-part=other-seat] select', 'person');
    expect(patch()).toEqual({ top: { options: { autoPass: 'fellowship', otherSeat: 'person' } } });
    const said = draftState({
      ...state,
      sandboxOtherSeat: { choices: ['script', 'person'], said: true, value: 'person' },
      draft: { ...state.draft, options: { otherSeat: 'person' } },
    });
    await show(AboutSectionComponent, said);
    expect(find<HTMLSelectElement>('gd-author-select[data-part=other-seat] select').value).toBe('person');
    expect(el.querySelector('[data-part=seat-unsaid]')).toBeNull();
    await choose('gd-author-select[data-part=other-seat] select', 'script');
    expect(patch()).toEqual({ top: { options: null } });
    // (only a sandbox's)
    await show(AboutSectionComponent, draftState({ ...said, draft: { ...said.draft, kind: 'tutorial' } }));
    expect(el.querySelector('[data-part=other-seat]')).toBeNull();
  });

  /* ---------------------------------------------------------------- take-backs (P6) */

  it('chooses take-backs, showing what the kind gives when the file says nothing', async () => {
    await show(PlaysSectionComponent, puzzle());
    const select = find<HTMLSelectElement>('gd-author-select[data-part=undo] select');
    expect(select.value).toBe('');
    expect(select.options[0]!.textContent?.trim()).toBe('default (off)');
    await choose('gd-author-select[data-part=undo] select', 'on');
    expect(patch()).toEqual({ top: { options: { autoPass: 'fellowship, archery', opponentDelayMs: 0, undo: 'on' } } });
    await show(PlaysSectionComponent, puzzle({}, { options: { undo: 'off' } }));
    expect(find<HTMLSelectElement>('gd-author-select[data-part=undo] select').value).toBe('off');
    await choose('gd-author-select[data-part=undo] select', '');
    expect(patch()).toEqual({ top: { options: null } });
  });

  /* ---------------------------------------------------------------- the judge (P1) */

  it('shows the judge: its moment, phase, turn and requirements', async () => {
    await show(JudgingComponent, puzzle());
    expect(find<HTMLSelectElement>('[data-part=judge-moment] select').value).toBe('endOf');
    expect(find<HTMLSelectElement>('[data-part=judge-phase] select').value).toBe('archery');
    expect(find<HTMLInputElement>('[data-part=judge-turn]').value).toBe('');
    expect(find<HTMLTextAreaElement>('[data-part=judge-win]').value).toBe('[\n\t{\n\t\ttype: canSpot\n\t}\n]');
    expect(
      all('[data-part=judge-moment] option').map((one) => [
        one.getAttribute('value'),
        one.getAttribute('title') !== null,
      ]),
    ).toEqual([
      ['', true],
      ['startOf', true],
      ['endOf', true],
      ['endOfTurn', true],
      ['skirmishResolution', true],
    ]);
    expect(el.querySelector('[data-part=verdict]')).toBeNull();
  });

  it('sends a new moment with the requirements as the draft has them (the notes’ patch)', async () => {
    await show(JudgingComponent, puzzle());
    await choose('[data-part=judge-moment] select', 'startOf');
    expect(patch()).toEqual({
      top: { outcome: { winMessages: ['Every companion came through.'], judge: { at: 'startOf(archery)', win: WIN } } },
    });
    await choose('[data-part=judge-phase] select', 'regroup');
    expect(patch()).toEqual({
      top: { outcome: { winMessages: ['Every companion came through.'], judge: { at: 'endOf(regroup)', win: WIN } } },
    });
    await type('[data-part=judge-turn]', '2');
    expect((patch()['top'] as { outcome: { judge: unknown } }).outcome.judge).toEqual({
      at: 'endOf(archery), turn(2)',
      win: WIN,
    });
  });

  it('sends requirements as HJSON text, none as the moment alone, and never takes the judge out', async () => {
    await show(JudgingComponent, puzzle());
    await type('[data-part=judge-win]', '{\n type: canSpot\n filter: alias(frodo)\n}');
    expect(patch()).toEqual({
      top: {
        outcome: {
          winMessages: ['Every companion came through.'],
          judge: { at: 'endOf(archery)', win: '{\n type: canSpot\n filter: alias(frodo)\n}' },
        },
      },
    });
    await type('[data-part=judge-win]', '');
    expect((patch()['top'] as { outcome: { judge: unknown } }).outcome.judge).toBe('endOf(archery)');
    await choose('[data-part=judge-moment] select', '');
    expect(patch()).toEqual({ top: { outcome: { winMessages: ['Every companion came through.'] } } });
  });

  it('judges a draft at a moment that has none, from the first phase, and says the game’s verdict', async () => {
    const state = draftState({
      draft: { id: 'test', steps: [] },
      puzzle: puzzleState({ verdict: { won: true, score: 1160, moment: 'skirmishResolution' } }),
    });
    await show(JudgingComponent, state);
    expect(el.querySelector('[data-part=judge-phase]')).toBeNull();
    expect(el.querySelector('[data-part=judge-win]')).toBeNull();
    expect(find('[data-part=verdict]').textContent?.trim()).toBe('This game: won at skirmishResolution, scoring 1160.');
    await choose('[data-part=judge-moment] select', 'endOf');
    expect(patch()).toEqual({ top: { outcome: { judge: 'endOf(fellowship)' } } });
    await choose('[data-part=judge-moment] select', 'endOfTurn');
    expect(patch()).toEqual({ top: { outcome: { judge: 'endOfTurn' } } });
  });

  /* ---------------------------------------------------------------- the score (P7) */

  it('scores by a total at once, and by a card’s value once its card is chosen', async () => {
    await show(JudgingComponent, puzzle());
    expect(find<HTMLSelectElement>('[data-part=score-value] select').value).toBe('');
    expect(el.querySelector('[data-part=score-better]')).toBeNull();
    await choose('[data-part=score-value] select', 'burdens');
    expect(patch()).toEqual({
      top: {
        outcome: {
          winMessages: ['Every companion came through.'],
          judge: { at: 'endOf(archery)', win: WIN },
          score: { value: 'burdens' },
        },
      },
    });
    await choose('[data-part=score-value] select', 'strength');
    expect(posted('edit')).toHaveLength(0);
    expect(all('[data-part=score-of] option').map((one) => one.getAttribute('value'))).toEqual([
      '',
      'frodo',
      'eomer',
      'troop1',
      'condition',
      'ally',
    ]);
    await choose('[data-part=score-of] select', 'eomer');
    expect((patch()['top'] as { outcome: { score: unknown } }).outcome.score).toEqual({ value: 'strength:eomer' });
  });

  it('changes a score’s better and label, and a hand’s seat (the notes’ patch)', async () => {
    const state = puzzle(
      { puzzle: { ...PUZZLE, score: { value: 'wounds:eomer', valueText: null, better: 'higher', label: null } } },
      { outcome: { score: { value: 'wounds:eomer' } } },
    );
    await show(JudgingComponent, state);
    expect(find<HTMLSelectElement>('[data-part=score-of] select').value).toBe('eomer');
    await choose('[data-part=score-better] select', 'lower');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'wounds:eomer', better: 'lower' } } } });
    await type('[data-part=score-label]', 'Wounds');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'wounds:eomer', label: 'Wounds' } } } });
    await choose('[data-part=score-value] select', 'vitality');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'vitality:eomer' } } } });
    await choose('[data-part=score-value] select', 'hand');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'hand' } } } });
    await choose('[data-part=score-value] select', '');
    expect(patch()).toEqual({ top: { outcome: null } });
  });

  it('sends a custom value typed as HJSON as the outcome’s text', async () => {
    await show(JudgingComponent, puzzle({}, { outcome: { judge: 'endOfTurn' } }));
    await choose('[data-part=score-value] select', 'custom');
    expect(posted('edit')).toHaveLength(0);
    expect(find<HTMLTextAreaElement>('[data-part=score-text]').placeholder).toBe(
      '{\n\ttype: forEachWound\n\tfilter: companion\n}',
    );
    await type('[data-part=score-text]', '{\n\ttype: forEachWound\n\tfilter: companion\n}');
    expect(patch()).toEqual({
      topText: {
        outcome: '{\n"judge": "endOfTurn"\n"score": {\n"value": {\n\ttype: forEachWound\n\tfilter: companion\n}\n}\n}',
      },
    });
  });

  it('keeps a custom value the draft has when only its label changes', async () => {
    const value = { type: 'forEachWound', filter: 'companion' };
    const state = puzzle(
      {
        puzzle: {
          ...PUZZLE,
          score: { value: null, valueText: '{\n\ttype: forEachWound\n}', better: 'higher', label: null },
        },
      },
      { outcome: { score: { value } } },
    );
    await show(JudgingComponent, state);
    expect(find<HTMLTextAreaElement>('[data-part=score-text]').value).toBe('{\n\ttype: forEachWound\n}');
    await type('[data-part=score-label]', 'Wounds');
    expect(patch()).toEqual({ top: { outcome: { score: { value, label: 'Wounds' } } } });
  });

  /* ---------------------------------------------------------------- forbid and the pool (P10, P2) */

  it('ticks what the player may not do, sending the rules', async () => {
    await show(PuzzleRulesComponent, puzzle());
    expect(all('[data-part=forbid] gd-author-check').map((one) => one.getAttribute('data-forbid'))).toEqual([
      'shuffle',
      'reveal',
    ]);
    expect(find<HTMLInputElement>('[data-forbid=shuffle] input').checked).toBe(true);
    await tick('[data-forbid=reveal]', true);
    expect(patch()).toEqual({ top: { rules: { oneSided: true, forbid: 'shuffle, reveal' } } });
    await searched(1251);
  });

  it('shows the pool with how many cards it finds, sends a new one, and says why one is refused', async () => {
    await show(PuzzleRulesComponent, puzzle());
    expect(find<HTMLInputElement>('input[data-part=pool]').value).toBe('set:1-6');
    expect(await searched(1251)).toEqual(['set:1-6']);
    expect(find('.gd-author-filter__verdict').textContent?.trim()).toBe('1251 cards');
    await type('input[data-part=pool]', ' set:1-3 ');
    const sent = posted('edit');
    expect(sent).toHaveLength(1);
    expect(parseJson(sentForm(sent[0]!.request).get('patch') as string)).toEqual({ top: { pool: 'set:1-3' } });
    sent[0]!.flush(JSON.stringify({ error: "pool: 'colour:red': the deck builder's search has no 'colour'" }), {
      status: 400,
      statusText: 'No',
    });
    await settle();
    await render();
    expect(find('.gd-author-filter__verdict').textContent?.trim()).toBe('refused');
    expect(find('.gd-author-filter__why').textContent).toMatch(/has no 'colour'/);
    await type('input[data-part=pool]', '');
    const blank = posted('edit');
    expect(parseJson(sentForm(blank[0]!.request).get('patch') as string)).toEqual({ top: { pool: null } });
    blank[0]!.flush(JSON.stringify(result(puzzle())));
    await settle();
    await render();
    expect(el.querySelector('.gd-author-filter__why')).toBeNull();
  });

  it('offers no pool to a draft that is no puzzle', async () => {
    await show(PuzzleRulesComponent, draftState({ puzzle: puzzleState() }));
    expect(el.querySelector('input[data-part=pool]')).toBeNull();
    expect(el.querySelector('[data-part=forbid]')).not.toBeNull();
    await show(PuzzleRulesComponent, draftState());
    expect(el.querySelector('[data-part=forbid]')).toBeNull();
  });

  /* ---------------------------------------------------------------- solutions (P4) */

  it('lists the solutions: the one in play, its picks by name, its answers, what the last Check said', async () => {
    authoring.solutionVerdicts.set(solutionVerdicts({ published: null }));
    await show(SolutionsSectionComponent, puzzle());
    const row = find('.gd-author-solution[data-name=published]');
    expect(row.hasAttribute('data-playing')).toBe(true);
    expect(row.getAttribute('data-solved')).toBe('yes');
    expect(find('.gd-author-solution__tag').textContent?.trim()).toBe('in play');
    expect(find('.gd-author-solution__picks').textContent?.trim()).toBe(
      'condition: Parapet, ally: Aldor, Soldier of Edoras',
    );
    expect(find('.gd-author-solution__answers').textContent?.trim()).toBe('2 answers');
    expect(find('.gd-author-solution__verdict').textContent?.trim()).toBe('solved');
    expect(find<HTMLButtonElement>('[data-author=use]').disabled).toBe(true);
    expect(find<HTMLInputElement>('[data-part=solution-name]').value).toBe('solution2');
  });

  it('plays a solution, uses its cards, and deletes it after the page’s own question', async () => {
    await show(SolutionsSectionComponent, puzzle({ puzzle: { ...PUZZLE, playingWith: null } }));
    await click('[data-author=play]');
    expect(solutionCall()).toEqual({ op: 'play', name: 'published', picks: null });
    await click('[data-author=use]');
    expect(solutionCall()).toEqual({ op: 'use', name: 'published', picks: null });
    notifier.answers.push(false);
    await click('[data-author=delete]');
    expect(posted('solution')).toHaveLength(0);
    expect(notifier.dialogs.at(-1)).toBe('confirm: Delete the solution published?');
    notifier.answers.push(true);
    await click('[data-author=delete]');
    expect(solutionCall()).toEqual({ op: 'delete', name: 'published', picks: null });
  });

  it('records a solution with the game’s cards, or with cards chosen for its slots', async () => {
    vi.useFakeTimers();
    await show(SolutionsSectionComponent, puzzle());
    await click('[data-author=record]');
    expect(solutionCall()).toEqual({ op: 'record', name: 'solution2', picks: null });
    await type('[data-part=solution-name]', 'second-try');
    await click('.gd-author-solutions__record [data-slot=ally] [data-action=change]');
    const field = find<HTMLInputElement>('.gd-author-solutions__record gd-author-card-search input');
    field.value = 'aldor';
    field.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(SEARCH_MS);
    backend
      .match((r) => r.url === `${AUTHOR}cards`)
      .forEach((request) => request.flush(JSON.stringify({ cards: [{ ...ALDOR, id: '4_263' }] })));
    await settle();
    await render();
    await click('.gd-author-search__result[data-id="4_263"]');
    expect(find('.gd-author-solutions__record [data-slot=ally] .gd-author-picks__card').textContent?.trim()).toBe(
      'Aldor, Soldier of Edoras',
    );
    await click('[data-author=record]');
    expect(solutionCall()).toEqual({
      op: 'record',
      name: 'second-try',
      picks: JSON.stringify({ condition: '5_87', ally: '4_263' }),
    });
    // (a name the server would refuse is not sent)
    await type('[data-part=solution-name]', '2nd');
    expect(find<HTMLButtonElement>('[data-author=record]').disabled).toBe(true);
  });

  it('renames a solution, and changes its note, a card and its answers, sending the solutions as the file has them', async () => {
    vi.useFakeTimers();
    await show(SolutionsSectionComponent, puzzle());
    const SOLUTION = PUZZLE_DRAFT.solutions![0]!;
    await click('[data-author=rename]');
    const rename = find<HTMLInputElement>('[data-part=solution-rename]');
    rename.value = 'key';
    rename.dispatchEvent(new Event('input'));
    rename.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    await render();
    expect(patch()).toEqual({ top: { solutions: [{ ...SOLUTION, name: 'key' }] } });
    await click('[data-author=details]');
    await type('[data-part=solution-note]', 'The key.');
    expect(patch()).toEqual({ top: { solutions: [{ ...SOLUTION, note: 'The key.' }] } });
    await type('[data-part=solution-answers]', 'use(condition)\noption(Make the minion archery total -3)\n');
    expect(patch()).toEqual({
      top: { solutions: [{ ...SOLUTION, answers: ['use(condition)', 'option(Make the minion archery total -3)'] }] },
    });
    await click('.gd-author-solution__details [data-slot=condition] [data-action=change]');
    const field = find<HTMLInputElement>('.gd-author-solution__details gd-author-card-search input');
    field.value = 'parapet';
    field.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(SEARCH_MS);
    backend
      .match((r) => r.url === `${AUTHOR}cards`)
      .forEach((request) => request.flush(JSON.stringify({ cards: [{ ...ALDOR, id: '5_79', name: 'Armory' }] })));
    await settle();
    await render();
    await click('.gd-author-search__result[data-id="5_79"]');
    expect(patch()).toEqual({
      top: { solutions: [{ ...SOLUTION, picks: { condition: '5_79', ally: '4_262' } }] },
    });
  });

  it('refuses a solution’s new name the rule or another solution has, and leaves one unchanged alone', async () => {
    await show(SolutionsSectionComponent, puzzle());
    await click('[data-author=rename]');
    const rename = find<HTMLInputElement>('[data-part=solution-rename]');
    rename.value = '1st';
    rename.dispatchEvent(new Event('input'));
    rename.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    await render();
    expect(posted('edit')).toHaveLength(0);
    expect(find('[data-part=solution-rename]').hasAttribute('data-bad')).toBe(true);
    rename.value = 'published';
    rename.dispatchEvent(new Event('input'));
    rename.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    await render();
    expect(posted('edit')).toHaveLength(0);
    expect(el.querySelector('[data-part=solution-rename]')).toBeNull();
  });

  it('says it is recording, with Stop, and says the game’s verdict on the solution in play', async () => {
    const state = puzzle({
      puzzle: { ...PUZZLE, recording: 'published', verdict: { won: false, score: null, moment: 'endOf(archery)' } },
    });
    await show(SolutionsSectionComponent, state);
    expect(find('.gd-author-solutions__recording').textContent).toMatch(/Recording published/);
    expect(find('.gd-author-solution').getAttribute('data-solved')).toBe('no');
    expect(find('.gd-author-solution__verdict').textContent?.trim()).toBe('failed at endOf(archery)');
    await click('[data-author=stop]');
    expect(solutionCall()).toEqual({ op: 'stop', name: null, picks: null });
  });

  it('shows no solutions for a draft that is no puzzle, and says when a puzzle has none', async () => {
    await show(SolutionsSectionComponent, draftState({ puzzle: puzzleState() }));
    expect(el.querySelector('[data-section=solutions]')).toBeNull();
    await show(SolutionsSectionComponent, puzzle({ puzzle: puzzleState() }, { solutions: [], slots: {} }));
    expect(find('[data-section=solutions] [data-empty]').textContent?.trim()).toBe('None yet.');
    expect(el.querySelector('gd-author-slot-picks')).toBeNull();
  });

  it('puts the recording above the tabs too, with Stop', async () => {
    await show(AuthoringNoticesComponent, puzzle({ puzzle: { ...PUZZLE, recording: 'published' } }), false);
    expect(find('[data-notice=recording]').textContent).toMatch(/Recording solution published/);
    await click('[data-author=stop-recording]');
    expect(solutionCall()).toEqual({ op: 'stop', name: null, picks: null });
  });

  /* ---------------------------------------------------------------- the edges */

  it('shows a tool or a forbidden thing the panel does not know as itself, and no tools without the server’s list', async () => {
    const tools = { names: ['add', 'paint'], notBuilt: [], said: true, allowed: ['paint'] };
    await show(
      AboutSectionComponent,
      draftState({ sandboxTools: tools, draft: { id: 'test', kind: 'sandbox', steps: [] } }),
    );
    expect(find('[data-tool=paint] label').title).toBe('paint');
    await show(AboutSectionComponent, draftState({ draft: { id: 'test', kind: 'sandbox', steps: [] } }));
    expect(el.querySelector('[data-part=tools]')).toBeNull();
    await show(PuzzleRulesComponent, puzzle({ puzzle: { ...PUZZLE, forbidChoices: ['shuffle', 'draw'] } }));
    expect(find('[data-forbid=draw] label').textContent?.trim()).toBe('draw');
    await tick('[data-forbid=shuffle]', false);
    expect(patch()).toEqual({ top: { rules: { oneSided: true } } });
  });

  it('offers a hand’s seats and keeps a card the file names that the scenario does not have', async () => {
    const state = puzzle(
      { puzzle: { ...PUZZLE, score: { value: 'hand', valueText: null, better: 'lower', label: null } } },
      { outcome: { score: { value: 'hand', better: 'lower' } } },
    );
    await show(JudgingComponent, state);
    expect(all('[data-part=score-of] option').map((one) => [one.getAttribute('value'), one.textContent])).toEqual([
      ['', 'both'],
      ['human', 'human'],
      ['opponent', 'opponent'],
      ['freeps', 'freeps'],
      ['shadow', 'shadow'],
    ]);
    await choose('[data-part=score-of] select', 'human');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'hand:human', better: 'lower' } } } });
    await choose('[data-part=score-better] select', 'higher');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'hand' } } } });
    const gone = puzzle(
      { puzzle: { ...PUZZLE, score: { value: 'strength:gollum', valueText: null, better: 'higher', label: null } } },
      { outcome: { score: { value: 'strength:gollum' } } },
    );
    await show(JudgingComponent, gone);
    expect(find<HTMLSelectElement>('[data-part=score-of] select').value).toBe('gollum');
    // (another card's value keeps the card; a total has none to choose)
    await choose('[data-part=score-value] select', 'vitality');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'vitality:gollum' } } } });
    await choose('[data-part=score-value] select', 'twilight');
    expect(patch()).toEqual({ top: { outcome: { score: { value: 'twilight' } } } });
  });

  it('says a judged game with no score, and a judge on a turn', async () => {
    const state = puzzle({
      puzzle: {
        ...PUZZLE,
        judge: { at: 'endOfTurn, turn(2)', moment: 'endOfTurn', phase: null, turn: 2, win: null },
        verdict: { won: false, score: null, moment: 'endOfTurn, turn(2)' },
      },
    });
    await show(JudgingComponent, state);
    expect(find<HTMLInputElement>('[data-part=judge-turn]').value).toBe('2');
    expect(find<HTMLTextAreaElement>('[data-part=judge-win]').value).toBe('');
    expect(find('[data-part=verdict]').textContent?.trim()).toBe('This game: failed at endOfTurn, turn(2).');
    await type('[data-part=judge-turn]', '0');
    expect((patch()['top'] as { outcome: { judge: unknown } }).outcome.judge).toEqual({ at: 'endOfTurn', win: WIN });
  });

  it('says a refusal of the pool even when the server gave no words, and shows an empty slot to Record', async () => {
    await show(PuzzleRulesComponent, puzzle());
    await searched(3);
    await type('input[data-part=pool]', 'set:2');
    const sent = posted('edit');
    sent[0]!.error(new ProgressEvent('error'));
    await settle();
    await render();
    expect(find('.gd-author-filter__verdict').textContent?.trim()).toBe('refused');
    const empty = { ...PUZZLE, slots: [puzzleSlot('condition', { pick: null, pickName: null, bound: '' })], picks: {} };
    await show(SolutionsSectionComponent, puzzle({ puzzle: empty }));
    expect(find('.gd-author-solutions__record .gd-author-picks__card').hasAttribute('data-empty')).toBe(true);
    await click('.gd-author-solutions__record [data-action=change]');
    await click('.gd-author-solutions__record gd-author-card-search [data-action=cancel]');
    expect(el.querySelector('.gd-author-solutions__record gd-author-card-search')).toBeNull();
    // (Record with the slot empty: no picks, which the server refuses in its words)
    await click('[data-author=record]');
    expect(solutionCall()).toEqual({ op: 'record', name: 'solution2', picks: '{}' });
  });

  it('shows a solution with no note or picks, and closes its details', async () => {
    const bare = {
      ...PUZZLE,
      playingWith: null,
      solutions: [{ name: 'bare', note: null, picks: {}, pickNames: {}, answers: [], text: '' }],
    };
    await show(SolutionsSectionComponent, puzzle({ puzzle: bare }, { solutions: [{ name: 'bare' }] }));
    expect(el.querySelector('.gd-author-solution__picks')).toBeNull();
    expect(find('.gd-author-solution__answers').textContent?.trim()).toBe('0 answers');
    expect(find('.gd-author-solution').hasAttribute('data-solved')).toBe(false);
    await click('[data-author=details]');
    expect(find<HTMLTextAreaElement>('[data-part=solution-note]').value).toBe('');
    await click('[data-author=details]');
    expect(el.querySelector('.gd-author-solution__details')).toBeNull();
    await click('[data-author=rename]');
    find<HTMLInputElement>('[data-part=solution-rename]').dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape' }),
    );
    await render();
    expect(el.querySelector('[data-part=solution-rename]')).toBeNull();
  });
});
