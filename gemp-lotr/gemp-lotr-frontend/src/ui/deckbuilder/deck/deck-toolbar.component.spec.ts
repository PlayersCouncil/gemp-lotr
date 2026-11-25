import { provideDeckbuilderState } from '../../../core/deckbuilder-state';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckChanges } from '../../../core/deck/deck-changes.service';
import { DeckDraft } from '../../../core/deck/deck-draft.service';
import { DeckSaver } from '../../../core/deck/deck-saver.service';
import { signal } from '@angular/core';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { THEME_ATTRIBUTE, THEME_PICKER, ThemeService } from '../../../core/platform/theme.service';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { DeckToolbarComponent } from './deck-toolbar.component';

describe('DeckToolbarComponent', () => {
  let draft: DeckDraft;
  let changes: DeckChanges;
  let saver: DeckSaver;
  /** THEME_PICKER, read when the toolbar is created */
  let themePicker: boolean;

  beforeEach(() => {
    themePicker = true;
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: THEME_PICKER, useFactory: () => themePicker },
      ],
    });
    draft = TestBed.inject(DeckDraft);
    changes = TestBed.inject(DeckChanges);
    saver = TestBed.inject(DeckSaver);
    // jsdom takes touch events, which counts as a touch device: these tests are on a computer unless they say not
    Object.defineProperty(TestBed.inject(AutoZoomSetting), 'available', { value: true });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  afterEach(() => document.documentElement.removeAttribute(THEME_ATTRIBUTE));

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
      'gd-button gd-toolbar__theme',
    ]);
    expect(el.querySelector('.gd-toolbar__deck-name')!.textContent).toBe('New deck');
  });

  it('has no theme picker when THEME_PICKER is off', async () => {
    themePicker = false;
    const { el } = await render();
    expect(el.querySelector('.gd-toolbar__theme')).toBeNull();
    expect(el.lastElementChild!.className).toBe('gd-toolbar__collection');
  });

  it('lets players switch themes at any time, keeping the deck being edited', async () => {
    const themes = TestBed.inject(ThemeService);
    themes.init();
    draft.deckName.set('My deck');
    draft.markModified(true);
    const { fixture, el } = await render();
    const picker = el.querySelector<HTMLButtonElement>('button.gd-button.gd-toolbar__theme')!;
    expect(el.lastElementChild).toBe(picker);
    expect(picker.dataset['action']).toBe('theme');
    expect(picker.title).toBe('Switch to the Classic theme');
    expect(picker.getAttribute('aria-label')).toBe(picker.title);

    picker.click();
    await fixture.whenStable();
    expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
    expect(themes.behaviour().cardInfoModal).toBe(false);
    expect(picker.title).toBe('Switch to the Modern theme');
    expect(el.querySelector('.gd-toolbar__deck-name')!.textContent).toBe('*My deck - modified');

    picker.click();
    await fixture.whenStable();
    expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
    expect(themes.behaviour().cardInfoModal).toBe(true);
    expect(picker.title).toBe('Switch to the Classic theme');
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

  it('selects the format and the collection', async () => {
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
  });

  describe('in the compact layout', () => {
    // The modern theme on a portrait or narrow screen
    beforeEach(() => Object.defineProperty(TestBed.inject(CompactLayout), 'active', { value: signal(true) }));

    it('has only the deck menu and the name; the menu has every control, named, the theme first', async () => {
      draft.deckName.set('My deck');
      draft.markModified(true);
      const { el } = await render();
      expect(Array.from(el.children).map((child) => child.localName + '.' + child.className)).toEqual([
        'gd-drawer.',
        'span.gd-toolbar__deck-name',
      ]);
      expect(el.querySelector('.gd-toolbar__deck-name')!.textContent).toBe('*My deck - modified');
      const burger = el.querySelector<HTMLButtonElement>('button.gd-burger')!;
      expect(burger.getAttribute('aria-label')).toBe('Deck menu');
      const menu = el.querySelector<HTMLElement>('dialog.gd-drawer[data-drawer="deck"]')!;
      const items = Array.from(menu.querySelectorAll<HTMLButtonElement>('button.gd-button.gd-menu__item'));
      expect(items.map((b) => [b.dataset['action'], b.textContent!.trim()])).toEqual([
        ['theme', 'Switch to the Classic theme'],
        ['new', 'New deck'],
        ['save', 'Save deck'],
        ['rename', 'Rename deck'],
        ['copy', 'Copy deck to new'],
        ['import', 'Import Deck'],
        ['library', 'Open Library'],
        ['deck-list', 'Deck list'],
        ['convert-errata', 'Convert to PC Errata'],
        ['notes', 'Add/Edit Notes'],
        ['auto-zoom', 'Auto-zoom cards on hover'],
      ]);
      expect(items[0].classList).toContain('gd-toolbar__theme');
      expect(menu.querySelector('label.gd-menu__field select.gd-toolbar__format')).not.toBeNull();
      expect(el.querySelector('select.gd-toolbar__collection')).toBeNull();
    });

    it('runs a menu item and closes the menu', async () => {
      const newDeck = vi.spyOn(saver, 'newDeck').mockResolvedValue(undefined);
      const { fixture, el } = await render();
      el.querySelector<HTMLButtonElement>('button.gd-burger')!.click();
      await fixture.whenStable();
      const menu = el.querySelector<HTMLDialogElement>('dialog.gd-drawer')!;
      expect(menu.open).toBe(true);
      expect(el.querySelector('button.gd-burger')!.getAttribute('aria-expanded')).toBe('true');
      menu.querySelector<HTMLButtonElement>('[data-action="new"]')!.click();
      await fixture.whenStable();
      expect(newDeck).toHaveBeenCalledOnce();
      expect(menu.open).toBe(false);
      expect(el.querySelector('button.gd-burger')!.getAttribute('aria-expanded')).toBe('false');
    });
  });
});
