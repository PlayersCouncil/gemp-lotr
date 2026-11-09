import { provideDeckbuilderState } from '../../../core/deckbuilder-state';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckChanges } from '../../../core/deck/deck-changes.service';
import { DeckDraft } from '../../../core/deck/deck-draft.service';
import { DeckSaver } from '../../../core/deck/deck-saver.service';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { ThemeService } from '../../../core/platform/theme.service';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { DeckToolbarComponent } from './deck-toolbar.component';

describe('DeckToolbarComponent', () => {
  let draft: DeckDraft;
  let changes: DeckChanges;
  let saver: DeckSaver;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    draft = TestBed.inject(DeckDraft);
    changes = TestBed.inject(DeckChanges);
    saver = TestBed.inject(DeckSaver);
    // jsdom takes touch events, which counts as a touch device: these tests are on a computer unless they say not
    Object.defineProperty(TestBed.inject(AutoZoomSetting), 'available', { value: true });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(DeckToolbarComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const button = (action: string) => el.querySelector<HTMLButtonElement>(`button[data-action="${action}"]`)!;
    return { fixture, el, button };
  }

  it('has the deck buttons, the format, the notes, the name, the zoom, the collection and the theme (DOM contract)', async () => {
    const { el } = await render();
    expect(el.classList).toContain('gd-toolbar');
    expect(el.getAttribute('role')).toBe('toolbar');
    const actions = Array.from(el.querySelectorAll<HTMLButtonElement>('.gd-toolbar__deck-actions > button.gd-button'));
    expect(actions.map((b) => [b.dataset['action'], b.title])).toEqual([
      ['new', 'New deck'],
      ['save', 'Save deck'],
      ['rename', 'Rename deck'],
      ['copy', 'Copy deck to new'],
      ['import', 'Import Deck'],
      ['library', 'Open Library'],
      ['deck-list', 'Deck list'],
      ['convert-errata', 'Convert to PC Errata'],
    ]);
    expect(actions.every((b) => b.getAttribute('aria-label') === b.title)).toBe(true);
    expect(
      Array.from(el.children)
        .slice(1)
        .map((child) => child.className || (child as HTMLElement).dataset['action']),
    ).toEqual([
      'gd-toolbar__format',
      'gd-button',
      'gd-toolbar__deck-name',
      'gd-button',
      'gd-toolbar__collection',
      'gd-toolbar__theme',
    ]);
    expect(el.querySelector('.gd-toolbar__deck-name')!.textContent).toBe('New deck');
    const theme = el.querySelector<HTMLSelectElement>('select.gd-toolbar__theme')!;
    expect(Array.from(theme.options).map((o) => o.value)).toEqual(['classic', 'modern']);
  });

  it('runs the deck actions', async () => {
    const { button } = await render();
    const library = TestBed.inject(DeckLibraryService);
    const calls = {
      newDeck: vi.spyOn(saver, 'newDeck').mockResolvedValue(),
      save: vi.spyOn(saver, 'saveCurrentDeck').mockResolvedValue(true),
      rename: vi.spyOn(saver, 'renameCurrentDeck').mockResolvedValue(),
      copy: vi.spyOn(saver, 'copyDeck').mockReturnValue(),
      importDeck: vi.spyOn(library, 'openImport').mockResolvedValue(),
      libraryList: vi.spyOn(library, 'openLibrary').mockResolvedValue(),
      deckList: vi.spyOn(library, 'openDeckList').mockResolvedValue(),
      notes: vi.spyOn(library, 'openNotes').mockReturnValue(),
    };
    for (const action of ['new', 'save', 'rename', 'copy', 'import', 'library', 'deck-list', 'notes']) {
      button(action).click();
    }
    for (const spy of Object.values(calls)) {
      expect(spy).toHaveBeenCalledTimes(1);
    }
  });

  it('enables Convert to PC Errata when the deck can be converted', async () => {
    const { fixture, button } = await render();
    expect(button('convert-errata').disabled).toBe(true);
    changes.convertErrataEnabled.set(true);
    await fixture.whenStable();
    expect(button('convert-errata').disabled).toBe(false);
  });

  it('shows a modified deck as the legacy page did', async () => {
    const { fixture, el } = await render();
    draft.label.set({ text: 'Mine', modified: true });
    await fixture.whenStable();
    const name = el.querySelector<HTMLElement>('.gd-toolbar__deck-name')!;
    expect(name.textContent).toBe('*Mine - modified');
    expect(name.hasAttribute('data-modified')).toBe(true);
  });

  it('toggles the hover zoom', async () => {
    const { fixture, button } = await render();
    const zoom = TestBed.inject(AutoZoomSetting);
    const initial = zoom.enabled();
    expect(button('auto-zoom').getAttribute('aria-pressed')).toBe(`${initial}`);
    button('auto-zoom').click();
    await fixture.whenStable();
    expect(zoom.enabled()).toBe(!initial);
    expect(button('auto-zoom').getAttribute('aria-pressed')).toBe(`${!initial}`);
    zoom.toggle();
  });

  it('has no hover zoom toggle on touch devices', async () => {
    Object.defineProperty(TestBed.inject(AutoZoomSetting), 'available', { value: false });
    const { el } = await render();
    expect(el.querySelector('[data-action="auto-zoom"]')).toBeNull();
  });

  it('selects the format, the collection and the theme', async () => {
    const { el } = await render();
    const selectFormat = vi.spyOn(changes, 'selectFormat');
    const format = el.querySelector<HTMLSelectElement>('.gd-toolbar__format')!;
    format.selectedIndex = 0;
    format.dispatchEvent(new Event('change'));
    expect(selectFormat).toHaveBeenCalledWith(0);

    const selectCollection = vi.spyOn(TestBed.inject(CollectionBrowser), 'selectCollectionType').mockReturnValue();
    const collection = el.querySelector<HTMLSelectElement>('.gd-toolbar__collection')!;
    collection.value = collection.options[1].value;
    collection.dispatchEvent(new Event('change'));
    expect(selectCollection).toHaveBeenCalledWith(collection.options[1].value);

    const select = vi.spyOn(TestBed.inject(ThemeService), 'select').mockResolvedValue();
    const theme = el.querySelector<HTMLSelectElement>('.gd-toolbar__theme')!;
    theme.value = 'modern';
    theme.dispatchEvent(new Event('change'));
    expect(select).toHaveBeenCalledWith('modern');
  });
});
