import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { Draft, DraftState } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { draftGameXml, draftState, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { ScenarioTabComponent } from './scenario-tab.component';
import { sentForm } from '../../../testing/sent-form';
import { AUTHOR, authorCall, authorCalls, sentPatch } from '../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';

describe('ScenarioTabComponent', () => {
  let fixture: ComponentFixture<ScenarioTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let notifier: FakeNotifier;
  let hall: { go: ReturnType<typeof vi.fn>; elsewhere: ReturnType<typeof signal> };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    hall = { go: vi.fn(), elsewhere: signal<string | null>(null) };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: hall },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    notifier = TestBed.inject(FakeNotifier);
    fixture = TestBed.createComponent(ScenarioTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  const withDraft = (draft: Partial<Draft>, extra: Partial<DraftState> = {}) =>
    draftState({ ...extra, draft: { ...draftState().draft, ...draft } });

  async function open(state: DraftState = draftState()) {
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

  async function redraw() {
    await settle();
    fixture.detectChanges();
    await fixture.whenStable();
  }

  function find<T extends HTMLElement = HTMLElement>(selector: string): T {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  }

  async function click(selector: string) {
    find(selector).click();
    await redraw();
  }

  /** Types into a field and leaves it (it sends on change). */
  async function type(selector: string, value: string) {
    const field = find<HTMLInputElement | HTMLTextAreaElement>(selector);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    await redraw();
  }

  async function choose(part: string, value: string) {
    const select = find<HTMLSelectElement>(`gd-author-select[data-part="${part}"] select`);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await redraw();
  }

  async function tick(selector: string, on: boolean) {
    const box = find<HTMLInputElement>(`${selector} input`);
    box.checked = on;
    box.dispatchEvent(new Event('change'));
    await redraw();
  }

  const posted = (what: string) => authorCalls(backend, what);

  /** The one edit sent: its patch. */
  function patch(): { top?: Record<string, unknown>; topText?: Record<string, string>; opponent?: unknown } {
    return sentPatch(authorCall(backend, 'edit')) as { top?: Record<string, unknown> };
  }

  const top = () => patch().top;
  const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? null;

  it('shows its sections in order', async () => {
    await open();
    expect([...el.querySelectorAll('section[data-section]')].map((s) => s.getAttribute('data-section'))).toEqual([
      'file',
      'about',
      'plays',
      'rules',
      'outcome',
      'parts',
    ]);
  });

  it('shows nothing without a draft', async () => {
    await open({ session: false } as unknown as DraftState);
    expect(el.querySelectorAll('section').length).toBe(0);
  });

  describe('the file', () => {
    it('says where it is, what saving would drop or move, and offers Submit only for an own scenario', async () => {
      await open(draftState({ commentsLost: 2, fileOnSave: 'puzzles/test.hjson', kindFolder: 'puzzles' }));
      expect(text('.gd-author-scenario__path')).toBe('tutorials/test.hjson (public) — saved');
      expect(text('[data-note=comments]')).toBe('Saving drops 2 lines of comment.');
      expect(text('[data-note=moves]')).toBe('Saving moves it to puzzles/test.hjson.');
      expect(el.querySelector('button[data-author=submit]')).toBeNull();
      await update(draftState({ own: true, dirty: true }));
      expect(el.querySelector('[data-note]')).toBeNull();
      expect(find('button[data-author=submit]').title).toBe(
        'Submit it for review, to become a public scenario. It is saved first.',
      );
      await update(draftState({ own: true, revision: 3 }));
      expect(find('button[data-author=submit]').title).toBe('Submit it for review, to become a public scenario.');
    });

    it('saves, and shows the report until it is put away', async () => {
      await open(draftState({ dirty: true }));
      authoring.showTab('scenario');
      await click('button[data-author=save]');
      const saved = { path: 'p', id: 'test', playable: true, verified: false, errors: [], warnings: [], report: 'X' };
      posted('save')[0]!.flush(JSON.stringify(result(draftState(), { saved })));
      await redraw();
      expect(text('pre.gd-author-scenario__report')).toBe('Saved to p. It does not play through yet: X');
      await click('button[data-author=put-away]');
      expect(el.querySelector('pre.gd-author-scenario__report')).toBeNull();
    });

    it('checks, tries other answers and shows the file', async () => {
      await open();
      await click('button[data-author=check]');
      posted('check')[0]!.flush(JSON.stringify({ passed: true, report: 'Fine.', warnings: [] }));
      await redraw();
      expect(text('pre.gd-author-scenario__report')).toBe('Scenario validated. Fine.');
      await click('button[data-author=other-answers]');
      expect(text('pre.gd-author-scenario__report')).toBe('Trying other answers...');
      posted('offscript')[0]!.flush(JSON.stringify({ tries: 2, handled: true, report: '' }));
      await redraw();
      await click('button[data-author=see-file]');
      backend.expectOne((r) => r.url === `${AUTHOR}source`).flush(JSON.stringify({ source: 'id: test' }));
      await redraw();
      expect(text('pre.gd-author-scenario__report')).toBe('id: test');
    });

    it('saves as a copy through the form: the name tidied, where it goes said as it is typed', async () => {
      await open(draftState({ canonical: false, file: 'tutorials/basics/b-02.hjson' }));
      await click('button[data-author=save-as]');
      expect(find('button[data-author=save-as]').hasAttribute('data-on')).toBe(true);
      const path = find<HTMLInputElement>('input[data-filing-path]');
      expect(path.value).toBe('basics/b-02-copy');
      expect(path.maxLength).toBe(120 - 'tutorials/'.length - '.hjson'.length);
      expect(text('[data-filing-note]')).toBe('A new scenario, with an id of its own.');
      expect(text('[data-filing-preview]')).toBe('your own folder: tutorials/basics/b-02-copy.hjson (id: b-02-copy)');
      path.value = ' My Copy!.hjson';
      path.dispatchEvent(new Event('input'));
      await redraw();
      expect(text('[data-filing-preview]')).toBe('your own folder: tutorials/My Copy.hjson (id: my-copy)');
      path.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      await redraw();
      const [request] = posted('save') as TestRequest[];
      expect([sentForm(request!.request).get('as'), sentForm(request!.request).get('path')]).toEqual([
        'copy',
        'My Copy.hjson',
      ]);
      expect(sentForm(request!.request).get('canonical')).toBe('false');
      // (while it is on its way, it is not sent again)
      await click('button[data-author=file]');
      expect(posted('save')).toEqual([]);
      const saved = {
        path: 'p',
        id: 'my-copy',
        playable: true,
        verified: true,
        errors: [],
        warnings: [],
        report: null,
      };
      request!.flush(JSON.stringify(result(draftState(), { saved })));
      await redraw();
      expect(el.querySelector('gd-game-author-filing')).toBeNull();
    });

    it('marks a name with nothing usable in it, and sends nothing', async () => {
      await open();
      await click('button[data-author=rename]');
      expect(text('[data-filing-note]')).toBe('The same scenario, its file moved.');
      expect(find('[data-filing-note]').title).not.toContain('stays unsaved');
      const path = find<HTMLInputElement>('input[data-filing-path]');
      expect(path.value).toBe('test');
      path.value = ' !! ';
      path.dispatchEvent(new Event('input'));
      await click('button[data-author=file]');
      expect(path.hasAttribute('data-bad')).toBe(true);
      expect(text('[data-filing-preview]')).toBe('');
      expect(posted('save')).toEqual([]);
    });

    it('keeps the form open when filing is refused; Cancel and the button close it', async () => {
      await open(draftState({ dirty: true }));
      await click('button[data-author=rename]');
      expect(find('[data-filing-note]').title).toContain('What has not been saved stays unsaved.');
      await click('button[data-author=file]');
      posted('save')[0]!.flush(JSON.stringify({ error: 'There is already one.' }), { status: 400, statusText: 'No' });
      await redraw();
      expect(el.querySelector('gd-game-author-filing')).not.toBeNull();
      await click('button[data-author=cancel-filing]');
      expect(el.querySelector('gd-game-author-filing')).toBeNull();
      await click('button[data-author=save-as]');
      await click('button[data-author=save-as]');
      expect(el.querySelector('gd-game-author-filing')).toBeNull();
      await click('button[data-author=save-as]');
      await click('button[data-author=rename]');
      expect(find('gd-game-author-filing .gd-author-scenario__filing').getAttribute('data-filing')).toBe('rename');
    });

    it('asks before submitting; Cancel submits nothing, Submit for review does', async () => {
      await open(draftState({ own: true }));
      await click('button[data-author=submit]');
      const dialog = () => el.querySelector('dialog[data-dialog=author-submit]');
      expect(dialog()?.hasAttribute('open')).toBe(true);
      expect(text('dialog[data-dialog=author-submit] h2')).toBe('Submit “A Test” for review?');
      expect(el.querySelectorAll('.gd-author-submit__guidelines li').length).toBe(4);
      await click('dialog[data-dialog=author-submit] button[data-action=cancel]');
      expect(dialog()?.hasAttribute('open')).toBe(false);
      expect(posted('submit')).toEqual([]);
      // (closed, it answers no too; a draft with no title is called by its id)
      await update(withDraft({ title: ' ' }, { own: true, revision: 2 }));
      await click('button[data-author=submit]');
      expect(text('dialog[data-dialog=author-submit] h2')).toBe('Submit “test” for review?');
      await click('dialog[data-dialog=author-submit] button.gd-dialog__close');
      expect(dialog()?.hasAttribute('open')).toBe(false);
      expect(posted('submit')).toEqual([]);
      await click('button[data-author=submit]');
      await click('dialog[data-dialog=author-submit] button[data-action=submit]');
      posted('submit')[0]!.flush(JSON.stringify({ ok: true, submitted: 'x' }));
      await redraw();
      expect(hall.go).toHaveBeenCalledWith('#account/scenarios');
    });

    it('closes the draft, asking first when it has changes', async () => {
      await open(draftState({ dirty: true }));
      notifier.answers.push(false);
      await click('button[data-author=close]');
      expect(notifier.dialogs).toEqual(['confirm: The draft has changes that have not been saved. Close it anyway?']);
      expect(posted('close')).toEqual([]);
      notifier.answers.push(true);
      await click('button[data-author=close]');
      posted('close')[0]!.flush(JSON.stringify({ ok: true }));
      await redraw();
      expect(hall.go).toHaveBeenCalled();
      await update(draftState({ revision: 9 }));
      await click('button[data-author=close]');
      expect(notifier.dialogs).toHaveLength(2);
      expect(posted('close')).toHaveLength(1);
    });
  });

  describe('about it', () => {
    it('sends each field as it is left, blank as nothing, the version as a number', async () => {
      await open();
      expect(find<HTMLInputElement>('input[data-part=title]').value).toBe('A Test');
      await type('input[data-part=title]', 'New');
      expect(top()).toEqual({ title: 'New' });
      await type('textarea[data-part=summary]', 'What it is');
      expect(top()).toEqual({ summary: 'What it is' });
      await type('input[data-part=tags]', 'a');
      backend.match(() => true);
      await type('input[data-part=tags]', '');
      expect(top()).toEqual({ tags: null });
      await type('input[data-part=version]', 'two');
      expect(top()).toEqual({ version: 1 });
      expect(find<HTMLInputElement>('input[data-part=version]').value).toBe('1');
      await type('textarea[data-part=note]', 'For later');
      expect(top()).toEqual({ note: 'For later' });
      await choose('kind', 'puzzle');
      expect(top()).toEqual({ kind: 'puzzle' });
    });

    it('leaves alone a field being typed in when a fresh state comes', async () => {
      await open();
      const summary = find<HTMLTextAreaElement>('textarea[data-part=summary]');
      summary.value = 'Half typed';
      summary.dispatchEvent(new Event('input'));
      await update(withDraft({ title: 'Changed elsewhere' }, { revision: 2 }));
      expect(summary.value).toBe('Half typed');
      expect(el.querySelector('textarea[data-part=summary]')).toBe(summary);
      expect(find<HTMLInputElement>('input[data-part=title]').value).toBe('Changed elsewhere');
    });
  });

  describe('how it plays', () => {
    it('names the opponent and says what it and the player do unscripted, and where the player sits', async () => {
      await open(withDraft({ opponent: { fallback: 'pass' }, coverage: 'guided' }));
      // (the opponent's own parts go by the opponent patch, which leaves its standing orders alone)
      await type('input[data-part=opponent-name]', 'Sauron');
      expect(patch()).toEqual({ opponent: { set: { name: 'Sauron' } } });
      await type('input[data-part=opponent-name]', ' ');
      expect(patch()).toEqual({ opponent: { unset: ['name'] } });
      await choose('fallback', 'fail');
      expect(patch()).toEqual({ opponent: { unset: ['fallback', 'plays'] } });
      expect(find<HTMLSelectElement>('gd-author-select[data-part=coverage] select').value).toBe('guided');
      await choose('coverage', 'strict');
      expect(top()).toEqual({ coverage: 'strict' });
      await choose('human', 'shadow');
      expect(top()).toEqual({ human: 'shadow' });
    });

    it('ticks the phases the player is passed through', async () => {
      await open(withDraft({ options: { autoPass: 'fellowship' } }));
      const ticked = [...el.querySelectorAll<HTMLInputElement>('[data-part=auto-pass] input')].map(
        (box) => box.checked,
      );
      expect(ticked).toEqual([true, false, false, false, false, false, false]);
      await tick('[data-part=auto-pass] gd-author-check[data-phase=regroup]', true);
      expect(top()).toEqual({ options: { autoPass: 'fellowship, regroup' } });
      await tick('[data-part=auto-pass] gd-author-check[data-phase=fellowship]', false);
      expect(top()).toEqual({ options: null });
    });

    it("shows the player's settings as the game has them, and writes only what differs from the default", async () => {
      await open(withDraft({ kind: 'puzzle', options: { autoAccept: false, animationSpeed: 2 } }));
      expect(find<HTMLInputElement>('gd-author-check[data-part=auto-accept] input').checked).toBe(false);
      expect(find<HTMLInputElement>('gd-author-check[data-part=auto-dismiss-deck-reveal] input').checked).toBe(false);
      expect(find<HTMLSelectElement>('gd-author-select[data-part=animation-speed] select').value).toBe('2');
      await tick('gd-author-check[data-part=auto-accept]', true);
      expect(top()).toEqual({ options: { animationSpeed: 2 } });
      await tick('gd-author-check[data-part=auto-dismiss-deck-reveal]', true);
      expect(top()).toEqual({ options: { autoAccept: false, animationSpeed: 2, autoDismissDeckReveal: true } });
      await choose('animation-speed', '');
      expect(top()).toEqual({ options: { autoAccept: false } });
      await choose('animation-speed', '-4');
      expect(top()).toEqual({ options: { autoAccept: false, animationSpeed: -4 } });
    });

    it("dismisses a tutorial's deck reveals unless it says not to", async () => {
      await open();
      expect(find<HTMLSelectElement>('gd-author-select[data-part=animation-speed] select').value).toBe('');
      const reveal = find<HTMLInputElement>('gd-author-check[data-part=auto-dismiss-deck-reveal] input');
      expect(reveal.checked).toBe(true);
      await tick('gd-author-check[data-part=auto-dismiss-deck-reveal]', false);
      expect(top()).toEqual({ options: { autoDismissDeckReveal: false } });
      await tick('gd-author-check[data-part=auto-accept]', false);
      expect(top()).toEqual({ options: { autoAccept: false } });
    });

    it('sets the bubble gap, the stock one when blank', async () => {
      await open(withDraft({ options: { bubbleGap: '3' } }));
      const gap = find<HTMLInputElement>('input[data-part=bubble-gap]');
      expect(gap.placeholder).toBe('1.8');
      expect(gap.value).toBe('3');
      await type('input[data-part=bubble-gap]', ' ');
      expect(top()).toEqual({ options: null });
    });
  });

  describe('rules', () => {
    const skip = (phase: string) => ({ type: 'modifier', modifier: { type: 'skipPhase', phase } });
    const other = { type: 'modifier', modifier: { type: 'modifyStrength', filter: 'frodo', amount: 1 } };

    it("shows each seat's usual rules, counting the others, and sends the whole of the rules as one changes", async () => {
      await open(withDraft({ rules: { oneSided: true, shadow: [skip('MANEUVER'), other] } }));
      const seat = '[data-seat=shadow]';
      expect(find<HTMLInputElement>('gd-author-check[data-part=one-sided] input').checked).toBe(true);
      expect(find<HTMLInputElement>(`${seat} gd-author-check[data-phase=maneuver] input`).checked).toBe(true);
      expect(text(`${seat} [data-part=others]`)).toBe('1 other effect, kept.');
      expect(el.querySelector('[data-seat=freeps] [data-part=others]')).toBeNull();
      await tick(`${seat} gd-author-check[data-phase=maneuver]`, false);
      expect(top()).toEqual({ rules: { oneSided: true, shadow: [other] } });
      await tick(`${seat} gd-author-check[data-part=ring]`, true);
      expect(top()).toEqual({
        rules: {
          oneSided: true,
          shadow: [
            { type: 'modifier', modifier: { type: 'disableGameText', filter: 'name(The One Ring)' } },
            skip('maneuver'),
            other,
          ],
        },
      });
      await type('[data-seat=freeps] input[data-part=hand]', '-3');
      expect(top()).toEqual({
        rules: {
          oneSided: true,
          shadow: [skip('MANEUVER'), other],
          freeps: [{ type: 'modifier', modifier: { type: 'modifyHandSize', amount: -3 } }],
        },
      });
      await type('[data-seat=freeps] input[data-part=roaming]', '2');
      expect(JSON.stringify(top())).toContain('"modifyRoamingPenalty","filter":"minion","amount":2');
    });

    it('takes the rules out when nothing is left', async () => {
      await open(withDraft({ rules: { oneSided: true } }));
      await tick('gd-author-check[data-part=one-sided]', false);
      expect(top()).toEqual({ rules: null });
      await update(withDraft({ rules: { shadow: [skip('regroup'), other, other] } }, { revision: 2 }));
      expect(text('[data-seat=shadow] [data-part=others]')).toBe('2 other effects, kept.');
      await tick('gd-author-check[data-part=one-sided]', true);
      expect(top()).toEqual({ rules: { shadow: [skip('regroup'), other, other], oneSided: true } });
    });
  });

  describe('how it is lost', () => {
    it('loses also when a companion is killed, and says what the player is told', async () => {
      await open();
      const lose = find<HTMLSelectElement>('gd-author-select[data-part=lose] select');
      expect([...lose.options].map((option) => option.value)).toEqual(['', 'companion']);
      await choose('lose', 'companion');
      expect(top()).toEqual({ outcome: { lose: { type: 'hasCardInDeadPile', filter: 'companion' } } });
      await type('textarea[data-part=lose-message]', 'Alas.');
      expect(top()).toEqual({ outcome: { loseMessage: 'Alas.' } });
    });

    it('shows a way of losing only the file has, and choosing it changes nothing', async () => {
      await open(withDraft({ outcome: { lose: [{ type: 'x' }], loseMessage: 'Alas.' } }));
      expect(find<HTMLSelectElement>('gd-author-select[data-part=lose] select').value).toBe('other');
      expect(find<HTMLTextAreaElement>('textarea[data-part=lose-message]').value).toBe('Alas.');
      await choose('lose', 'companion');
      backend.match(() => true);
      await choose('lose', 'other');
      expect(posted('edit')).toEqual([]);
      await choose('lose', '');
      expect(top()).toEqual({ outcome: { loseMessage: 'Alas.' } });
    });
  });

  describe('the rest of the file', () => {
    it('shows each part as HJSON, and applies it as it is', async () => {
      await open(draftState({ parts: { cards: '{\n\tfreeps: {}\n}' }, draft: { id: 'test', cards: {}, steps: [] } }));
      const names = [...el.querySelectorAll('details[data-part] > summary')].map((s) => s.textContent);
      expect(names).toEqual([
        'cards',
        'ringBearer (none)',
        'ring (none)',
        'sites (none)',
        'setup (none)',
        'rules (none)',
        'rng (none)',
        'opponent (none)',
        'options (none)',
        'outcome (none)',
      ]);
      const area = find<HTMLTextAreaElement>('details[data-part=cards] textarea');
      expect(area.value).toBe('{\n\tfreeps: {}\n}');
      expect(area.rows).toBe(4);
      area.value = '{ freeps: { frodo: 1_290 } }';
      await click('details[data-part=cards] button[data-author=apply]');
      expect(patch()).toEqual({ topText: { cards: '{ freeps: { frodo: 1_290 } }' } });
    });

    it('keeps the text the server refused', async () => {
      await open();
      const area = find<HTMLTextAreaElement>('details[data-part=options] textarea');
      area.value = '{ nope';
      await click('details[data-part=options] button[data-author=apply]');
      posted('edit')[0]!.flush(JSON.stringify({ error: 'options: bad' }), { status: 400, statusText: 'No' });
      await redraw();
      expect(authoring.problem()).toBe('options: bad');
      expect(find<HTMLTextAreaElement>('details[data-part=options] textarea').value).toBe('{ nope');
    });
  });

  describe('two sides (A3)', () => {
    const shadowRow = '[data-part=auto-pass][data-role=shadow]';
    const ticks = (selector: string) =>
      [...el.querySelectorAll<HTMLInputElement>(`${selector} gd-author-check[data-phase] input`)].map(
        (box) => box.checked,
      );

    it('offers the phases for each role in a game of two sides, sending the object form', async () => {
      await open(withDraft({ options: { autoPass: 'fellowship' } }, { twoSided: true }));
      expect(find<HTMLInputElement>('gd-author-check[data-part=by-role] input').checked).toBe(false);
      await tick('gd-author-check[data-part=by-role]', true);
      expect(top()).toEqual({ options: { autoPass: { freeps: 'fellowship', shadow: 'fellowship' } } });
      await update(withDraft({ options: { autoPass: { freeps: 'fellowship', shadow: false } } }, { twoSided: true }));
      expect(ticks('[data-part=auto-pass][data-role=freeps]')).toEqual([
        true,
        false,
        false,
        false,
        false,
        false,
        false,
      ]);
      expect(ticks(shadowRow)).toEqual([false, false, false, false, false, false, false]);
      await tick(`${shadowRow} gd-author-check[data-phase=maneuver]`, true);
      expect(top()).toEqual({ options: { autoPass: { freeps: 'fellowship', shadow: 'maneuver' } } });
    });

    it('shows a file’s object form whatever the draft’s sidedness, and keeps it when a role’s phase is ticked', async () => {
      await open(
        withDraft({ options: { autoPass: { freeps: 'regroup', shadow: 'maneuver, regroup' }, autoAccept: false } }),
      );
      expect(find<HTMLInputElement>('gd-author-check[data-part=by-role] input').checked).toBe(true);
      expect(ticks(shadowRow)).toEqual([false, false, true, false, false, false, true]);
      await tick('[data-part=auto-pass][data-role=freeps] gd-author-check[data-phase=fellowship]', true);
      expect(top()).toEqual({
        options: { autoPass: { freeps: 'fellowship, regroup', shadow: 'maneuver, regroup' }, autoAccept: false },
      });
    });

    it('asks before one list takes the place of two that differ; kept, the tick goes back', async () => {
      await open(withDraft({ options: { autoPass: { freeps: 'regroup', shadow: false } } }, { twoSided: true }));
      notifier.answers.push(false);
      await tick('gd-author-check[data-part=by-role]', false);
      expect(notifier.dialogs[0]).toBe(
        'confirm: One list for both roles: regroup. (Free Peoples had regroup; Shadow had none.)',
      );
      expect(posted('edit')).toEqual([]);
      expect(find<HTMLInputElement>('gd-author-check[data-part=by-role] input').checked).toBe(true);
      notifier.answers.push(true);
      await tick('gd-author-check[data-part=by-role]', false);
      expect(top()).toEqual({ options: { autoPass: 'regroup' } });
    });

    it('takes out leftovers a change of sides would leave the opponent unable to give, after asking', async () => {
      const draft = {
        human: 'shadow' as const,
        rules: { oneSided: true },
        cards: { freeps: { frodo: '10_121' }, shadow: { oppFrodo: '10_121' } },
        opponent: { leftovers: 'frodo' },
      };
      await open(withDraft(draft));
      notifier.answers.push(false);
      await tick('gd-author-check[data-part=one-sided]', false);
      expect(notifier.dialogs[0]).toBe(
        "confirm: The opponent's leftovers to frodo go: in a two-sided game they go to the player's characters.",
      );
      expect(posted('edit')).toEqual([]);
      expect(find<HTMLInputElement>('gd-author-check[data-part=one-sided] input').checked).toBe(true);
      notifier.answers.push(true);
      await tick('gd-author-check[data-part=one-sided]', false);
      expect(patch()).toEqual({ top: { rules: null }, opponent: { unset: ['leftovers'] } });
    });
  });
});
