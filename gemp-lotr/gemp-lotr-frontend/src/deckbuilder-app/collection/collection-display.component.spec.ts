import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DeckService } from '../../core/deck/deck.service';
import { CollectionService } from '../../core/collection/collection.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { PageLayoutService } from '../layout/page-layout.service';
import { CollectionDisplayComponent } from './collection-display.component';
import { provideFakeNotifier } from '../../testing/fake-notifier';

@Component({
  imports: [CollectionDisplayComponent],
  template: `<div id="collection-display" gempCollectionDisplay></div>`,
})
class Host {}

describe('CollectionDisplayComponent', () => {
  let collection: CollectionService;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    // The 1280x800 page: the collection pane's content box is 388x778
    const layout = TestBed.inject(PageLayoutService);
    layout.viewportWidth.set(1280);
    layout.viewportHeight.set(800);
    collection = TestBed.inject(CollectionService);
  });

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('draws the group box at the legacy size', async () => {
    const { el } = await render();
    const box = el.querySelector<HTMLElement>('.ui-widget-content')!;
    expect(box.style.width).toBe('378px');
    expect(box.style.height).toBe('378px');
    expect(box.style.borderRadius).toBe('7px');
  });

  it('shows packs first, each item with its role class and count', async () => {
    // One copy in the deck is subtracted from the collection's count
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
    const cards = Array.from(el.querySelectorAll<HTMLElement>('.card'));
    expect(cards.map((c) => c.className)).toEqual([
      'card packInCollection',
      'card selectionInCollection',
      'card cardInCollection',
      'card placeholderInCollection',
    ]);
    expect(cards.map((c) => c.querySelector('.cardCount')!.textContent)).toEqual(['2', '1', '2', '1']);
    expect(cards[0].querySelector('.borderOverlay')!.classList).toContain('noBorder');
    expect(cards[2].querySelector('.borderOverlay')!.classList).not.toContain('noBorder');
    expect(cards[0].style.left).toBe('3px');
    expect(cards[0].style.top).toBe('3px');
  });
});
