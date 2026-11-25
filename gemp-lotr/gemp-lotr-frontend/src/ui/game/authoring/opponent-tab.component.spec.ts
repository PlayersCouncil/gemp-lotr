import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { AuthoringDecision, DraftState } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { decision, draftGameXml, draftState } from '../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { OpponentTabComponent } from './opponent-tab.component';
import { sentForm } from '../../../testing/sent-form';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

const held = (extra: Partial<AuthoringDecision> = {}): AuthoringDecision =>
  decision({
    seat: 'shadow',
    text: 'Play Shadow action or Pass',
    where: 'turn 1, shadow',
    step: null,
    held: true,
    ...extra,
  });

const ACTION = held({
  options: [
    { term: 'play(orc)', label: 'Goblin Runner: Play', alias: 'orc', id: '11' },
    { term: 'play(troll)', label: 'Cave Troll: Play', alias: 'troll', id: '12' },
  ],
});

describe('OpponentTabComponent', () => {
  let fixture: ComponentFixture<OpponentTabComponent>;
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
    fixture = TestBed.createComponent(OpponentTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

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
  }

  async function update(state: DraftState) {
    authoring.setState(state);
    await render();
  }

  async function click(selector: string) {
    const target = el.querySelector<HTMLElement>(selector);
    expect(target, selector).not.toBeNull();
    target!.click();
    await settle();
    await render();
  }

  /** The calls to `what` sent: their forms. */
  function sent(what: string): HttpParams[] {
    return backend.match((r) => r.url === AUTHOR + what && r.method === 'POST').map((r) => sentForm(r.request));
  }
  const answers = () => sent('answer').map((body) => `${body.get('term')}|${body.get('record')}`);
  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;

  async function setValue(selector: string, value: string, event: 'input' | 'change') {
    const field = el.querySelector<HTMLInputElement | HTMLSelectElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event(event));
    await render();
  }

  it('says the opponent is asked nothing, what the player is asked, and how it is answered elsewhere', async () => {
    await open(draftState({ askOpponent: false, draft: { id: 'x', opponent: { fallback: 'pass' } } }));
    expect(text('[data-author=not-asked]')).toBe('The opponent is not being asked anything.');
    expect(text('[data-part=yours] .gd-author-opponent__asked')).toBe(
      'Play Fellowship action or Pass (turn 1, fellowship)',
    );
    expect(text('[data-part=yours] .gd-author-note')).toBe('Step 1 applies to it.');
    expect(el.querySelector<HTMLInputElement>('[data-ask=me]')?.checked).toBe(false);
    expect(el.querySelector<HTMLInputElement>('[data-ask=orders]')?.checked).toBe(true);
    expect(text('[data-part=how] label:last-of-type')).toBe('it plays by its orders, then fallback: pass');
    await click('[data-ask=me]');
    expect(sent('settings').map((body) => body.get('askOpponent'))).toEqual(['true']);
    await update(draftState({ askOpponent: true, waiting: { human: null, opponent: null } }));
    expect(text('[data-part=yours] .gd-author-note')).toBe('Nothing, for now.');
    expect(text('[data-part=how] label:last-of-type')).toBe('it plays by its orders, then fallback: fail');
    await click('[data-ask=orders]');
    expect(sent('settings').map((body) => body.get('askOpponent'))).toEqual(['false']);
  });

  it("says no step applies to the player's decision, and that the answer becomes one while written down", async () => {
    await open(draftState({ recording: true, waiting: { human: decision({ step: null }), opponent: null } }));
    expect(text('[data-part=yours] .gd-author-note')).toBe('No step applies to it. Your answer becomes one.');
    await update(draftState({ recording: false, waiting: { human: decision({ step: null }), opponent: null } }));
    expect(text('[data-part=yours] .gd-author-note')).toBe('No step applies to it.');
  });

  it('says what answers a decision that is not held', async () => {
    await open(draftState({ waiting: { human: null, opponent: held({ held: false, step: 2, sub: 1 }) } }));
    expect(text('.gd-author-opponent__asked')).toBe('Play Shadow action or Pass (turn 1, shadow)');
    expect(text('[data-author=answered-by]')).toBe('Step 2.1 answers it.');
    expect(el.querySelector('.gd-author-opponent__answers')).toBeNull();
    await update(draftState({ waiting: { human: null, opponent: held({ held: false }) } }));
    expect(text('[data-author=answered-by]')).toBe('It answers by itself in a moment.');
  });

  it('answers an action with its button or Pass, written into the script or not', async () => {
    await open(draftState({ waiting: { human: null, opponent: ACTION } }));
    expect(el.querySelector('.gd-author-opponent__answers')?.getAttribute('data-kind')).toBe('action');
    const buttons = [...el.querySelectorAll<HTMLButtonElement>('.gd-author-opponent__answer')];
    expect(buttons.map((button) => [button.textContent?.trim(), button.title])).toEqual([
      ['Goblin Runner: Play', 'play(orc)'],
      ['Cave Troll: Play', 'play(troll)'],
      ['Pass', 'pass'],
    ]);
    await click('[data-term="play(troll)"]');
    const record = el.querySelector<HTMLInputElement>('[data-option=record] input')!;
    expect(record.checked).toBe(true);
    record.click();
    await render();
    await click('[data-term=pass]');
    expect(answers()).toEqual(['play(troll)|true', 'pass|false']);
  });

  it('answers in the script’s words, and with the least it can do', async () => {
    await open(draftState({ waiting: { human: null, opponent: ACTION } }));
    await click('[data-author=send]');
    expect(answers()).toEqual([]);
    await setValue('.gd-author-opponent__raw input', '  use(orc) ', 'input');
    await click('[data-author=send]');
    const raw = el.querySelector<HTMLInputElement>('.gd-author-opponent__raw input')!;
    raw.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    expect(answers()).toEqual(['use(orc)|true', 'use(orc)|true']);
    await click('[data-author=least]');
    expect(sent('answer').map((body) => `${body.get('least')}|${body.get('record')}`)).toEqual(['true|false']);
  });

  it('keeps what was typed and ticked while the state comes and goes, and starts afresh for another decision', async () => {
    await open(draftState({ waiting: { human: null, opponent: ACTION } }));
    await setValue('.gd-author-opponent__raw input', 'use(orc)', 'input');
    el.querySelector<HTMLInputElement>('[data-option=record] input')!.click();
    await render();
    await update(draftState({ waiting: { human: decision(), opponent: ACTION } }));
    expect(el.querySelector<HTMLInputElement>('.gd-author-opponent__raw input')?.value).toBe('use(orc)');
    expect(el.querySelector<HTMLInputElement>('[data-option=record] input')?.checked).toBe(false);
    await update(draftState({ waiting: { human: null, opponent: held({ text: 'Another' }) } }));
    expect(el.querySelector<HTMLInputElement>('.gd-author-opponent__raw input')?.value).toBe('');
    expect(el.querySelector<HTMLInputElement>('[data-option=record] input')?.checked).toBe(true);
  });

  it('chooses the cards ticked: by name, by id, or passes with none', async () => {
    const cards = held({
      type: 'cards',
      pass: undefined,
      min: 0,
      max: 2,
      options: [
        { term: null, label: 'Goblin Runner', alias: 'orc', id: '11' },
        { term: null, label: 'Cave Troll', alias: null, id: '12' },
      ],
    });
    await open(draftState({ waiting: { human: null, opponent: cards } }));
    expect(text('.gd-author-opponent__answers .gd-author-note')).toBe('Choose 0 to 2:');
    expect([...el.querySelectorAll('.gd-author-opponent__card')].map((label) => label.textContent?.trim())).toEqual([
      'Goblin Runner (orc)',
      'Cave Troll',
    ]);
    expect(el.querySelector('[data-term=pass]')).toBeNull();
    await click('[data-author=choose]');
    await click('[data-key=orc] input');
    await click('[data-author=choose]');
    await click('[data-key="#12"] input');
    await click('[data-author=choose]');
    await click('[data-key=orc] input');
    await click('[data-author=choose]');
    expect(answers()).toEqual(['pass|true', 'choose(orc)|true', 'raw(11,12)|true', 'raw(12)|true']);
  });

  it('assigns each minion to the character chosen for it', async () => {
    const assign = held({
      type: 'assign',
      text: 'Assign minions',
      characters: [
        { term: null, label: 'Frodo', alias: 'frodo', id: '1' },
        { term: null, label: 'Sam', alias: 'sam', id: '2' },
      ],
      options: [
        { term: null, label: 'Goblin Runner', alias: 'orc', id: '11' },
        { term: null, label: 'Cave Troll', alias: null, id: '12' },
      ],
    });
    await open(draftState({ waiting: { human: null, opponent: assign } }));
    expect(el.querySelector('[data-term=pass]')).toBeNull();
    expect([...el.querySelectorAll('.gd-author-opponent__minion')].map((span) => span.textContent)).toEqual([
      'orc',
      'Cave Troll',
    ]);
    expect([...el.querySelectorAll('[data-minion=orc] option')].map((option) => option.textContent?.trim())).toEqual([
      'nobody',
      'Frodo (frodo)',
      'Sam (sam)',
    ]);
    await click('[data-author=assign]');
    await setValue('[data-minion=orc]', 'sam', 'change');
    expect(el.querySelector<HTMLSelectElement>('[data-minion=orc]')?.value).toBe('sam');
    await click('[data-author=assign]');
    await setValue('[data-minion="#12"]', 'frodo', 'change');
    await click('[data-author=assign]');
    expect(answers()).toEqual(['pass|true', 'assign(sam: orc)|true', 'raw(2 11, 1 12)|true']);
  });

  it('assigns nobody where the decision names no characters', async () => {
    const assign = held({ type: 'assign', options: [{ term: null, label: 'Goblin Runner', alias: 'orc', id: '11' }] });
    await open(draftState({ waiting: { human: null, opponent: assign } }));
    expect(el.querySelectorAll('[data-minion=orc] option')).toHaveLength(1);
    await click('[data-author=assign]');
    expect(answers()).toEqual(['pass|true']);
  });

  it('answers a number, only a whole one', async () => {
    await open(
      draftState({ waiting: { human: null, opponent: held({ type: 'number', pass: undefined, min: 1, max: 3 }) } }),
    );
    const number = el.querySelector<HTMLInputElement>('.gd-author-opponent__number')!;
    expect(number.value).toBe('1');
    expect(number.getAttribute('min')).toBe('1');
    expect(number.getAttribute('max')).toBe('3');
    await setValue('.gd-author-opponent__number', '', 'input');
    expect(el.querySelector<HTMLButtonElement>('[data-author=number]')?.disabled).toBe(true);
    await setValue('.gd-author-opponent__number', '2', 'input');
    await click('[data-author=number]');
    expect(answers()).toEqual(['number(2)|true']);
    await update(draftState({ waiting: { human: null, opponent: held({ type: 'number', pass: undefined }) } }));
    expect(el.querySelector<HTMLInputElement>('.gd-author-opponent__number')?.value).toBe('0');
    expect(el.querySelector('.gd-author-opponent__number')?.hasAttribute('min')).toBe(false);
  });

  it('answers a choice by its options; an option without a term does nothing', async () => {
    await open(
      draftState({
        waiting: {
          human: null,
          opponent: held({
            type: 'choice',
            pass: undefined,
            options: [
              { term: 'option(Yes)', label: 'Yes', alias: null, id: null },
              { term: null, label: 'Odd', alias: null, id: null },
            ],
          }),
        },
      }),
    );
    await click('[data-term="option(Yes)"]');
    await click('.gd-author-opponent__answer:not([data-term])');
    expect(answers()).toEqual(['option(Yes)|true']);
  });

  it('shows nothing without a draft', async () => {
    await open(draftState());
    authoring.setState({ session: false });
    await render();
    expect(el.querySelector('section')).toBeNull();
  });

  it('says the role of each seat’s decision in a game of two sides', async () => {
    await open(
      draftState({
        twoSided: true,
        waiting: {
          human: decision({ role: 'shadow', where: 'turn 2, regroup' }),
          opponent: decision({ seat: 'shadow', role: 'freeps', where: 'turn 2, regroup', held: true, step: null }),
        },
      }),
    );
    expect(text('[data-part=asked] .gd-author-opponent__asked')).toBe(
      'Play Fellowship action or Pass (turn 2, regroup · as Free Peoples)',
    );
    expect(text('[data-part=yours] .gd-author-opponent__asked')).toBe(
      'Play Fellowship action or Pass (turn 2, regroup · as Shadow)',
    );
  });
});
