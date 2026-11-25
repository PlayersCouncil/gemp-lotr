import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { AuthoringState, DraftState } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { decision, draftGameXml, draftState, position, result } from '../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { PAGE_RIGHT_PROPERTY } from '../../shared/page-room';
import { AuthoringPanelComponent } from './authoring-panel.component';
import { sentForm } from '../../../testing/sent-form';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

describe('AuthoringPanelComponent', () => {
  let fixture: ComponentFixture<AuthoringPanelComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let hall: { go: ReturnType<typeof vi.fn>; elsewhere: ReturnType<typeof signal> };
  let cues: { go: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    hall = { go: vi.fn(), elsewhere: signal(null) };
    cues = { go: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: cues },
        { provide: HallTab, useValue: hall },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    fixture = TestBed.createComponent(AuthoringPanelComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    document.documentElement.style.removeProperty(PAGE_RIGHT_PROPERTY);
    vi.useRealTimers();
  });

  async function open(state: AuthoringState = draftState(), info: Parameters<typeof draftGameXml>[0] = {}) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml(info));
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
    await fixture.whenStable();
  }

  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;
  const panel = () => el.querySelector<HTMLElement>('section.gd-author')!;
  const notices = () => [...el.querySelectorAll('.gd-author-notice')].map((n) => n.getAttribute('data-notice'));
  const posted = (what: string) => backend.match((r) => r.url === AUTHOR + what && r.method === 'POST');

  it('shows its head and the timeline for the draft being written, and takes its room at the right', async () => {
    await open();
    expect(panel().getAttribute('data-view')).toBe('author');
    expect(panel().hasAttribute('data-own-keys')).toBe(true);
    expect(text('.gd-author__title')).toBe('A Test');
    expect(text('.gd-author__where')).toBe('Step 1 of 3 · turn 1, fellowship');
    expect(el.querySelector('gd-game-author-timeline')).not.toBeNull();
    expect(el.querySelector('.gd-author__tab[aria-selected=true]')?.getAttribute('data-tab')).toBe('timeline');
    expect(document.documentElement.style.getPropertyValue(PAGE_RIGHT_PROPERTY)).toBe('430px');
    expect(TestBed.inject(ScenarioRoom).reservedRight()).toBe(430);
    await click('.gd-author__fold');
    expect(panel().hasAttribute('data-folded')).toBe(true);
    expect(text('.gd-author__fold')).toBe('«');
    expect(document.documentElement.style.getPropertyValue(PAGE_RIGHT_PROPERTY)).toBe('30px');
    fixture.destroy();
    expect(document.documentElement.style.getPropertyValue(PAGE_RIGHT_PROPERTY)).toBe('');
    expect(TestBed.inject(ScenarioRoom).reservedRight()).toBe(0);
  });

  it('marks a draft with changes in no file, and saves it', async () => {
    await open(draftState({ dirty: true }));
    expect(text('.gd-author__title')).toBe('* A Test');
    expect(panel().hasAttribute('data-unsaved')).toBe(true);
    expect(el.querySelector('.gd-author__save')?.hasAttribute('data-dirty')).toBe(true);
    await click('.gd-author__save');
    expect(posted('save')).toHaveLength(1);
    expect(el.querySelector('.gd-author__busy')).not.toBeNull();
  });

  it('turns to each tab, the Opponent tab calling while the opponent waits for the author', async () => {
    await open();
    for (const tab of ['step', 'opponent', 'board', 'scenario', 'timeline']) {
      await click(`.gd-author__tab[data-tab=${tab}]`);
      expect(el.querySelector('.gd-author__tab[aria-selected=true]')?.getAttribute('data-tab')).toBe(tab);
      expect(el.querySelector(`gd-game-author-${tab}`)).not.toBeNull();
    }
    await update(draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow', held: true }) } }));
    expect(el.querySelector('.gd-author__tab[data-tab=opponent]')?.hasAttribute('data-attention')).toBe(true);
    expect(panel().hasAttribute('data-wanted')).toBe(true);
  });

  it('keeps the keys typed in it to itself', async () => {
    await open();
    const heard = vi.fn();
    document.addEventListener('keydown', heard);
    el.querySelector('.gd-author__tab')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    document.removeEventListener('keydown', heard);
    expect(heard).not.toHaveBeenCalled();
  });

  it('floats in a test game, with only the way back and the test again', async () => {
    await open(draftState({ mode: 'test', testFrom: 3 }), { author: false, draftTest: true });
    expect(panel().getAttribute('data-view')).toBe('test');
    expect(panel().hasAttribute('data-floating')).toBe(true);
    expect(el.querySelector('.gd-author__fold')).toBeNull();
    expect(el.querySelector('.gd-author__save')).toBeNull();
    expect(text('.gd-author__where')).toBe("A player's game of the draft, from step 3");
    expect(document.documentElement.style.getPropertyValue(PAGE_RIGHT_PROPERTY)).toBe('');
    await click('[data-author=resume]');
    expect(posted('resume')).toHaveLength(1);
    await click('[data-author=test-again]');
    expect(sentForm(posted('test')[0]!.request).get('step')).toBe('3');
    await update(draftState({ mode: 'test' }));
    expect(text('.gd-author__where')).toBe("A player's game of the draft, from step 1");
  });

  it('says when the draft has moved on to another game, and goes to it', async () => {
    await open(draftState({ gameId: 9 }));
    expect(panel().getAttribute('data-view')).toBe('elsewhere');
    expect(text('.gd-author__title')).toBe('Scenario authoring');
    expect(text('.gd-author__where')).toBe('');
    await click('[data-author=go]');
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
  });

  it('says when no draft is open', async () => {
    await open({ session: false });
    expect(text('.gd-author__body')).toBe('No draft is open. Open one for editing from the Game Hall.');
    expect(notices()).toEqual([]);
  });

  it('shows nothing but its head before the state has come', () => {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    fixture.detectChanges();
    expect(panel().getAttribute('data-view')).toBe('none');
    expect(el.querySelector('.gd-author__body')).toBeNull();
    expect(text('.gd-author__title')).toBe('Scenario authoring');
  });

  describe('the notices', () => {
    it('says what went wrong, a line each, until put away', async () => {
      await open();
      authoring.problem.set('First.\nSecond.');
      fixture.detectChanges();
      expect(
        [...el.querySelectorAll('[data-notice=problem] .gd-author-notice__text')].map((n) => n.textContent),
      ).toEqual(['First.', 'Second.']);
      await click('[data-notice=problem] .gd-author-notice__dismiss');
      expect(notices()).toEqual([]);
    });

    it('says what was left out of the file, with the way to its step, until put away for good', async () => {
      await open(
        draftState({
          leftOut: [
            { where: 'step 2.allow', message: 'bad', was: 'allow: x', step: 2, uid: 's2' },
            { where: 'cards', message: 'odd', was: 'x', step: 0, uid: null },
          ],
        }),
      );
      expect(text('[data-notice=left-out] .gd-author-notice__head')).toContain('2 parts of the file were mistaken');
      expect(el.querySelectorAll('[data-notice=left-out] [data-author=show]')).toHaveLength(1);
      expect(text('.gd-author-notice__was')).toBe('allow: x');
      await click('[data-notice=left-out] [data-author=show]');
      expect(authoring.ui()).toMatchObject({ tab: 'step', selected: 2 });
      await click('[data-notice=left-out] .gd-author-notice__dismiss');
      expect(sentForm(posted('settings')[0]!.request).get('seenLeftOut')).toBe('true');
    });

    it('says what no step covers, and writes the step for it; or why it cannot', async () => {
      const open1 = {
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
      await open(
        draftState({
          uncovered: open1,
          position: position({ inHand: false }),
          stopped: 'uncovered',
          notice: 'Stopped.',
        }),
      );
      expect(notices()).toEqual(['uncovered']);
      expect(text('[data-notice=uncovered] .gd-author-notice__how')).toBe(
        'Add a step for this decision It goes in after step 1.',
      );
      expect(el.querySelector('.gd-author-notice__or')?.getAttribute('title')).toContain('That writes the same step');
      await click('[data-notice=uncovered] [data-author=add-here]');
      expect(posted('here')).toHaveLength(1);
      await update(draftState({ uncovered: { ...open1, problem: 'No.' }, position: position({ inHand: false }) }));
      expect(text('[data-notice=uncovered] .gd-author-notice__how')).toBe(
        'A step cannot be added for it as things stand: No.',
      );
      expect(el.querySelector('[data-notice=uncovered] [data-author=add-here]')).toBeNull();
    });

    it("says the server's notice, offering to answer the step again, until put away", async () => {
      await open(draftState({ notice: 'Stopped at step 1.', stopped: 'step' }));
      expect(text('[data-notice=notice] .gd-author-notice__text')).toBe('Stopped at step 1.');
      expect(el.querySelector('[data-author=retake]')?.getAttribute('title')).toContain('Step 1 forgets');
      await click('[data-author=retake]');
      expect(posted('retake')).toHaveLength(1);
      await click('[data-notice=notice] .gd-author-notice__dismiss');
      expect(notices()).toEqual([]);
      await update(draftState({ notice: 'Something else.' }));
      expect(notices()).toEqual(['notice']);
      expect(el.querySelector('[data-author=retake]')).toBeNull();
    });

    it("says the latest of the script's problems until put away, and how a save went", async () => {
      await open(draftState({ problems: ['a', 'b'] }));
      expect(text('[data-notice=problems]')).toBe('b (and 1 more before it) ×');
      await click('[data-notice=problems] .gd-author-notice__dismiss');
      expect(notices()).toEqual([]);
      authoring.saved.set({ good: false, why: true, stays: false, text: 'Saved. It does not play through yet.' });
      fixture.detectChanges();
      expect(el.querySelector('[data-notice=saved]')?.getAttribute('data-tone')).toBe('warning');
      await click('[data-author=see-why]');
      expect(authoring.ui().tab).toBe('scenario');
      expect(notices()).toEqual([]);
      authoring.saved.set({ good: true, why: true, stays: false, text: 'Saved.' });
      fixture.detectChanges();
      expect(el.querySelector('[data-notice=saved]')?.getAttribute('data-tone')).toBe('good');
      expect(el.querySelector('[data-author=see-why]')).toBeNull();
      await click('[data-notice=saved] .gd-author-notice__dismiss');
      authoring.startPicking('Click what the message is about.');
      fixture.detectChanges();
      expect(notices()).toEqual(['told']);
    });
  });

  describe('leaving with unsaved changes', () => {
    it('asks, and saves and goes, discards and goes, or stays', async () => {
      await open(draftState({ dirty: true, path: 'users/asdf/tutorials/t.hjson' }));
      authoring.askBeforeLeaving();
      fixture.detectChanges();
      const dialog = () => el.querySelector('dialog[data-dialog=author-leave]');
      expect(dialog()?.hasAttribute('open')).toBe(true);
      expect(text('.gd-author-leave__text')).toBe(
        'A Test has changes that are not in its file (users/asdf/tutorials/t.hjson).',
      );
      await click('[data-action=stay]');
      expect(authoring.leaveQuestion()).toBeNull();
      authoring.askBeforeLeaving();
      fixture.detectChanges();
      await click('[data-action=save]');
      expect(
        [...el.querySelectorAll('dialog[data-dialog=author-leave] button.gd-button')].every(
          (b) => (b as HTMLButtonElement).disabled,
        ),
      ).toBe(true);
      posted('save')[0]!.flush('{"error":"Disk full."}', { status: 500, statusText: 'x' });
      await settle();
      fixture.detectChanges();
      expect(text('.gd-author-leave__said')).toBe('Disk full.');
      await click('[data-action=discard]');
      posted('close')[0]!.flush('{"ok":true}');
      await settle();
      expect(hall.go).toHaveBeenCalled();
    });

    it('says so without a path, and with no draft says nothing', async () => {
      await open(draftState({ dirty: true, path: null }));
      authoring.askBeforeLeaving();
      fixture.detectChanges();
      expect(text('.gd-author-leave__text')).toBe('A Test has changes that are not in its file.');
      await update(draftState({ dirty: true, draft: { id: 'untitled' }, path: null }));
      expect(text('.gd-author-leave__text')).toBe('untitled has changes that are not in its file.');
      authoring.setState({ session: false });
      fixture.detectChanges();
      expect(text('.gd-author-leave__text')).toBe('');
    });
  });

  it('answers a change without a draft title with its id, and stays as it is for a change the game can take', async () => {
    await open(draftState({ draft: { id: 'plain' } }));
    expect(text('.gd-author__title')).toBe('plain');
    void authoring.settings({ recording: false });
    posted('settings')[0]!.flush(JSON.stringify(result(draftState({ recording: false, draft: { id: 'plain' } }))));
    await settle();
    fixture.detectChanges();
    expect(text('.gd-author__title')).toBe('plain');
  });

  it('turns the table’s own edit controls on and off with Edit table, remembered with the panel', async () => {
    await open();
    const toggle = () => el.querySelector<HTMLButtonElement>('button[data-author=edit-table]')!;
    expect(toggle().getAttribute('aria-pressed')).toBe('false');
    expect(toggle().textContent?.trim()).toBe('Edit table');
    await click('button[data-author=edit-table]');
    expect(authoring.ui().editTable).toBe(true);
    expect(toggle().getAttribute('aria-pressed')).toBe('true');
    expect(toggle().getAttribute('data-tone')).toBe('primary');
    await click('button[data-author=edit-table]');
    expect(authoring.ui().editTable).toBe(false);
  });
});
