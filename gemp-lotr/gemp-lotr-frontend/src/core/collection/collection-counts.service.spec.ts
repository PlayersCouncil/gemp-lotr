import { provideDeckbuilderState } from '../deckbuilder-state';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DeckDraft } from '../deck/deck-draft.service';
import { DeckFormatSelection, RaceCard } from '../deck/deck-format-selection.service';
import { addDrawCopy } from '../deck/deck-model';
import { CollectionCounts } from './collection-counts.service';

describe('CollectionCounts', () => {
  const raceCard = signal<RaceCard | null>(null);
  let counts: CollectionCounts;
  let draft: DeckDraft;

  beforeEach(() => {
    raceCard.set(null);
    TestBed.configureTestingModule({
      providers: [...provideDeckbuilderState(), { provide: DeckFormatSelection, useValue: { raceCard } }],
    });
    counts = TestBed.inject(CollectionCounts);
    draft = TestBed.inject(DeckDraft);
  });

  it('shows all copies of a card that is not in the deck', () => {
    expect(counts.available('1_12', 4)).toBe(4);
  });

  it('follows the deck at once', () => {
    draft.deck.update((deck) => addDrawCopy(deck, '1_12', 'FREE_PEOPLE'));
    expect(counts.available('1_12', 4)).toBe(3);
    draft.deck.update((deck) => addDrawCopy(deck, '1_12', 'FREE_PEOPLE'));
    expect(counts.available('1_12', 4)).toBe(2);
  });

  it('counts the Race card as on the deck side', () => {
    raceCard.set({ blueprintId: '90_7', overlayBlueprintId: null });
    expect(counts.available('90_7', 1)).toBe(0);
  });
});
