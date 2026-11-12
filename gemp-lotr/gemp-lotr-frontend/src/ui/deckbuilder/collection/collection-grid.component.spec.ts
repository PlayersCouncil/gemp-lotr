import { provideDeckbuilderState } from '../../../core/deckbuilder-state';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CardSize } from '../../../core/collection/card-size';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { DeckbuilderActions } from '../../../core/actions/deckbuilder-actions.service';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { DeckEditing } from '../../../core/deck/deck-editing.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CollectionGridComponent } from './collection-grid.component';

describe('CollectionGridComponent', () => {
  let collection: CollectionBrowser;

  const providers = () => [
    ...provideDeckbuilderState(),
    provideGempHttp(),
    provideHttpClientTesting(),
    provideFakeNotifier(),
    { provide: PAGE_QUERY_STRING, useValue: '' },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: providers() });
    collection = TestBed.inject(CollectionBrowser);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(CollectionGridComponent);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('shows packs first, each item with its role and the copies still available', async () => {
    TestBed.inject(DeckEditing).addFromCollection('1_1', 'FREE_PEOPLE');
    collection.page.set({
      total: 4,
      items: [
        { kind: 'card', blueprintId: '1_1', count: 3, side: 'FREE_PEOPLE', group: 'fp' },
        { kind: 'pack', blueprintId: 'FotR - League Starter', count: 2, selection: null },
        { kind: 'card', blueprintId: '404_1', count: 1, side: null, group: null },
        { kind: 'pack', blueprintId: '(S)FotR - Starter', count: 1, selection: ['1_1', '1_2'] },
      ],
    });
    const { el } = await render();
    expect(el.classList).toContain('gd-collection');
    const box = el.querySelector<HTMLElement>('.gd-collection__cards')!;
    expect(box.dataset['count']).toBe('4');
    const cards = Array.from(box.querySelectorAll<HTMLElement>('button.gd-card'));
    expect(cards.map((c) => c.dataset['role'])).toEqual(['pack', 'selection', 'card', 'placeholder']);
    expect(cards.map((c) => c.querySelector('.gd-card__count')!.textContent)).toEqual(['2', '1', '2', '1']);
    expect(cards.map((c) => c.hasAttribute('data-borderless'))).toEqual([true, true, false, false]);
    expect(cards[2].querySelector('img')!.src).toBe('https://i.lotrtcgpc.net/decipher/LOTR01001.jpg');
  });

  it("scales the cards by the player's card size in the compact layout only", async () => {
    const compact = signal(false);
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [...providers(), { provide: CompactLayout, useValue: { active: compact } }],
    });
    TestBed.inject(CardSize).set(150);
    const { fixture } = await render();
    const fit = fixture.debugElement.query(By.directive(FitCardsDirective)).injector.get(FitCardsDirective);
    expect(fit.gdFitCardsScale()).toBe(1);
    compact.set(true);
    await fixture.whenStable();
    expect(fit.gdFitCardsScale()).toBe(1.5);
  });

  it('uses a card through DeckbuilderActions, with the legacy role on its CardRef', async () => {
    collection.page.set({
      total: 1,
      items: [{ kind: 'card', blueprintId: '1_1', count: 3, side: 'FREE_PEOPLE', group: 'fp' }],
    });
    const activate = vi.spyOn(TestBed.inject(DeckbuilderActions), 'activate');
    const { el } = await render();
    el.querySelector<HTMLButtonElement>('.gd-card')!.click();
    expect(activate).toHaveBeenCalledWith(
      expect.objectContaining({ kind: 'collection', role: 'card', blueprintId: '1_1' }),
    );
  });

  it('shows nothing before the first page has loaded', async () => {
    const { el } = await render();
    expect(el.querySelectorAll('.gd-card').length).toBe(0);
  });
});
