import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DeckbuilderActions } from '../../../core/actions/deckbuilder-actions.service';
import { CollectionService } from '../../../core/collection/collection.service';
import { DeckService } from '../../../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CollectionGridComponent } from './collection-grid.component';

describe('CollectionGridComponent', () => {
  let collection: CollectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    collection = TestBed.inject(CollectionService);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(CollectionGridComponent);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('shows packs first, each item with its role and the copies still available', async () => {
    TestBed.inject(DeckService).addFromCollection('1_1', 'FREE_PEOPLE');
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

  it('uses a card through DeckbuilderActions, with the legacy role on its CardRef', async () => {
    collection.page.set({
      total: 1,
      items: [{ kind: 'card', blueprintId: '1_1', count: 3, side: 'FREE_PEOPLE', group: 'fp' }],
    });
    const activate = vi.spyOn(TestBed.inject(DeckbuilderActions), 'activate');
    const { el } = await render();
    el.querySelector<HTMLButtonElement>('.gd-card')!.click();
    expect(activate).toHaveBeenCalledWith(
      expect.objectContaining({ kind: 'collection', role: 'cardInCollection', blueprintId: '1_1' }),
    );
  });

  it('shows nothing before the first page has loaded', async () => {
    const { el } = await render();
    expect(el.querySelectorAll('.gd-card').length).toBe(0);
  });
});
