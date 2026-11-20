import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { Draft, DraftState } from '../../../../core/game/authoring/authoring-state';
import { provideGameState } from '../../../../core/game/game-state';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import { draftState } from '../../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { KEEP_KEY } from '../widgets/author-keep.directive';
import { OutcomeSectionComponent } from './outcome-section.component';

const AUTHOR = '/gemp-lotr-server/scenarios/author/';

/** An outcome with every part set. */
const FULL: NonNullable<Draft['outcome']> = {
  win: [{ type: 'canSpot', filter: 'name(Aragorn)' }],
  lose: { type: 'hasCardInDeadPile', filter: 'companion' },
  loseMessage: 'Alas.',
  offScript: { message: 'Off you go.', result: 'fail' },
  winMessages: ['Won.'],
  loseMessages: [{ header: 'Lost', text: 'Lost.' }],
  gameWon: { result: 'complete', messages: ['Hurrah.', { header: 'Well done', text: 'Again?' }] },
  gameLost: 'complete',
};

const withOutcome = (outcome?: Draft['outcome']): DraftState =>
  draftState({ draft: { id: 'test', steps: [], ...(outcome ? { outcome } : {}) } });

describe('OutcomeSectionComponent: endings', () => {
  let fixture: ComponentFixture<OutcomeSectionComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;

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
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.match(() => true));

  async function show(state: DraftState) {
    fixture = TestBed.createComponent(OutcomeSectionComponent);
    el = fixture.nativeElement as HTMLElement;
    fixture.componentRef.setInput('state', state);
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

  /** Types into a field and leaves it (it sends on change). */
  async function type(selector: string, value: string) {
    const field = find<HTMLInputElement | HTMLTextAreaElement>(selector);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    await settle();
    await render();
  }

  async function click(selector: string) {
    find(selector).click();
    await settle();
    await render();
  }

  /** The one outcome sent (`top: {outcome}`). */
  function outcome(): unknown {
    const requests: TestRequest[] = backend.match((r) => r.url === `${AUTHOR}edit` && r.method === 'POST');
    expect(requests).toHaveLength(1);
    const patch = JSON.parse(requests[0]!.request.body.get('patch') as string) as { top: { outcome: unknown } };
    expect(Object.keys(patch)).toEqual(['top']);
    return patch.top.outcome;
  }

  const ending = (kind: string) => `.gd-author-ending[data-ending=${kind}]`;
  const texts = (kind: string) =>
    [...el.querySelectorAll<HTMLTextAreaElement>(`${ending(kind)} textarea[data-part=text]`)].map((one) => one.value);

  it('shows each ending, its result where it has one, and its messages', async () => {
    await show(withOutcome(FULL));
    expect([...el.querySelectorAll('.gd-author-ending')].map((one) => one.getAttribute('data-ending'))).toEqual([
      'winMessages',
      'loseMessages',
      'gameWon',
      'gameLost',
    ]);
    expect(el.querySelector(`${ending('winMessages')} gd-author-select`)).toBeNull();
    expect(find<HTMLSelectElement>(`${ending('gameWon')} select`).value).toBe('complete');
    expect(find<HTMLSelectElement>(`${ending('gameLost')} select`).value).toBe('complete');
    expect(texts('gameWon')).toEqual(['Hurrah.', 'Again?']);
    expect(find<HTMLInputElement>(`${ending('gameWon')} li[data-index="1"] input[data-part=header]`).value).toBe(
      'Well done',
    );
    await show(withOutcome());
    expect(find<HTMLSelectElement>(`${ending('gameLost')} select`).value).toBe('fail');
    expect(texts('gameLost')).toEqual([]);
  });

  it('keeps every other part of an outcome with every part set when one message changes', async () => {
    await show(withOutcome(FULL));
    await type(`${ending('loseMessages')} li[data-index="0"] textarea[data-part=text]`, 'Lost, alas.');
    expect(outcome()).toEqual({ ...FULL, loseMessages: [{ header: 'Lost', text: 'Lost, alas.' }] });
  });

  it("writes the game lost's messages with its result, and changes the result alone", async () => {
    await show(withOutcome());
    await click(`${ending('gameLost')} [data-author=add-message]`);
    expect(backend.match((r) => r.url === `${AUTHOR}edit`)).toEqual([]);
    await type(`${ending('gameLost')} li[data-index="0"] input[data-part=header]`, 'The end');
    // (no text yet: nothing to say, and the heading stays here)
    expect(backend.match((r) => r.url === `${AUTHOR}edit`)).toEqual([]);
    await type(`${ending('gameLost')} li[data-index="0"] textarea[data-part=text]`, 'Frodo has fallen.');
    expect(outcome()).toEqual({
      gameLost: { result: 'fail', messages: [{ header: 'The end', text: 'Frodo has fallen.' }] },
    });
    const select = find<HTMLSelectElement>(`${ending('gameWon')} select`);
    select.value = 'fail';
    select.dispatchEvent(new Event('change'));
    await settle();
    expect(outcome()).toEqual({ gameWon: 'fail' });
  });

  it('reorders and takes out messages, a header cleared going with it', async () => {
    await show(withOutcome({ winMessages: ['One', { header: 'H', text: 'Two' }] }));
    await click(`${ending('winMessages')} li[data-index="1"] [data-author=up]`);
    expect(outcome()).toEqual({ winMessages: [{ header: 'H', text: 'Two' }, 'One'] });
    await click(`${ending('winMessages')} li[data-index="1"] [data-author=down]`);
    expect(backend.match((r) => r.url === `${AUTHOR}edit`)).toEqual([]);
    await type(`${ending('winMessages')} li[data-index="0"] input[data-part=header]`, ' ');
    expect(outcome()).toEqual({ winMessages: ['Two', 'One'] });
    await click(`${ending('winMessages')} li[data-index="0"] [data-author=delete]`);
    expect(outcome()).toEqual({ winMessages: ['One'] });
  });

  it('leaves a message being typed in alone when a fresh state says the same', async () => {
    await show(withOutcome(FULL));
    const field = find<HTMLTextAreaElement>(`${ending('gameWon')} li[data-index="0"] textarea`);
    field.value = 'Half';
    field.dispatchEvent(new Event('input'));
    fixture.componentRef.setInput('state', { ...withOutcome(FULL), revision: 4 });
    await render();
    expect(find(`${ending('gameWon')} li[data-index="0"] textarea`)).toBe(field);
    expect(field.value).toBe('Half');
  });

  it('takes back the field being typed in across a fresh game, and sends it when it is left', async () => {
    sessionStorage.setItem(
      KEEP_KEY,
      JSON.stringify({ key: 'outcome.gameWon.0.text', value: 'Hurrah!!', start: 3, end: 3 }),
    );
    await show(withOutcome(FULL));
    const field = find<HTMLTextAreaElement>(`${ending('gameWon')} li[data-index="0"] textarea`);
    expect(field.value).toBe('Hurrah!!');
    // (still being typed in: kept for the next fresh game too)
    expect(JSON.parse(sessionStorage.getItem(KEEP_KEY)!)).toMatchObject({ value: 'Hurrah!!' });
    field.dispatchEvent(new Event('blur'));
    await settle();
    expect(outcome()).toEqual({
      ...FULL,
      gameWon: { result: 'complete', messages: ['Hurrah!!', { header: 'Well done', text: 'Again?' }] },
    });
    // Typing keeps it; leaving forgets it
    field.dispatchEvent(new Event('focus'));
    expect(JSON.parse(sessionStorage.getItem(KEEP_KEY)!)).toMatchObject({
      key: 'outcome.gameWon.0.text',
      value: 'Hurrah!!',
    });
    field.dispatchEvent(new Event('blur'));
    expect(sessionStorage.getItem(KEEP_KEY)).toBeNull();
  });

  it('takes nothing back that was kept for another field, or that is what the field already says', async () => {
    sessionStorage.setItem(KEEP_KEY, JSON.stringify({ key: 'elsewhere', value: 'x' }));
    await show(withOutcome(FULL));
    expect(sessionStorage.getItem(KEEP_KEY)).not.toBeNull();
    sessionStorage.setItem(KEEP_KEY, JSON.stringify({ key: 'outcome.gameWon.0.text', value: 'Hurrah.' }));
    await show(withOutcome(FULL));
    const field = find<HTMLTextAreaElement>(`${ending('gameWon')} li[data-index="0"] textarea`);
    field.dispatchEvent(new Event('blur'));
    await settle();
    expect(backend.match((r) => r.url === `${AUTHOR}edit`)).toEqual([]);
  });
});
