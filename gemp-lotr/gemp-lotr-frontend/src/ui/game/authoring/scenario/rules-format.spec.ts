import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { Draft, DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../../core/game/game-state';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import { draftGameXml, draftState } from '../../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { ScenarioTabComponent } from '../scenario-tab.component';
import { AUTHOR, authorCalls, sentPatch } from '../../../../testing/authoring-requests';

/** The Scenario tab's rules the scenario engine added: the format, and the markers keeping together. */
describe('the Scenario tab: format and markers', () => {
  let fixture: ComponentFixture<ScenarioTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;

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
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal<string | null>(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ScenarioTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  const withDraft = (draft: Partial<Draft>): DraftState => draftState({ draft: { ...draftState().draft, ...draft } });

  async function open(state: DraftState) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/game/7' && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(state));
    await redraw();
  }

  async function redraw() {
    await settle();
    fixture.detectChanges();
    await fixture.whenStable();
  }

  const find = <T extends HTMLElement = HTMLElement>(selector: string): T => {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  };

  /** The edits sent: their patches' top parts. */
  const sent = () => authorCalls(backend, 'edit').map((request) => (sentPatch(request) as { top: unknown }).top);

  async function tick(selector: string, on: boolean) {
    const box = find<HTMLInputElement>(`${selector} input`);
    box.checked = on;
    box.dispatchEvent(new Event('change'));
    await redraw();
  }

  async function choose(value: string) {
    const select = find<HTMLSelectElement>('gd-author-select[data-part=format] select');
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await redraw();
  }

  it("offers the scenarios' formats (and keeps another the file names), and sends the one chosen", async () => {
    await open(withDraft({ format: 'scenario' }));
    const options = () => [...el.querySelectorAll<HTMLOptionElement>('gd-author-select[data-part=format] option')];
    expect(options().map((option) => option.value)).toEqual(['scenario', 'scenario_shadows']);
    expect(find<HTMLSelectElement>('gd-author-select[data-part=format] select').value).toBe('scenario');
    await choose('scenario_shadows');
    expect(sent()).toEqual([{ format: 'scenario_shadows' }]);
    await choose('scenario');
    expect(sent()).toEqual([]);
  });

  it('keeps a format of its own in the list', async () => {
    await open(withDraft({ format: 'expanded' }));
    const select = find<HTMLSelectElement>('gd-author-select[data-part=format] select');
    expect(select.value).toBe('expanded');
    expect([...select.options].map((option) => option.value)).toEqual(['scenario', 'scenario_shadows', 'expanded']);
  });

  it('ticks markers keeping together in a one-sided game only, and takes it out with one-sided', async () => {
    await open(withDraft({ rules: { oneSided: true } }));
    expect(find<HTMLInputElement>('gd-author-check[data-part=markers] input').disabled).toBe(false);
    await tick('gd-author-check[data-part=markers]', true);
    expect(sent()).toEqual([{ rules: { oneSided: true, markers: 'together' } }]);
    await tick('gd-author-check[data-part=markers]', false);
    expect(sent()).toEqual([{ rules: { oneSided: true } }]);
    await update(withDraft({ rules: { oneSided: true, markers: 'together' } }));
    expect(find<HTMLInputElement>('gd-author-check[data-part=markers] input').checked).toBe(true);
    await tick('gd-author-check[data-part=one-sided]', false);
    expect(sent()).toEqual([{ rules: null }]);
  });

  it('cannot tick markers keeping together in a game of two sides', async () => {
    await open(withDraft({}));
    expect(find<HTMLInputElement>('gd-author-check[data-part=markers] input').disabled).toBe(true);
  });

  /** A fresh state for the same page. */
  async function update(state: DraftState) {
    TestBed.inject(Authoring).setState(state);
    await redraw();
  }
});
