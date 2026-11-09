import { TestBed } from '@angular/core/testing';
import { CardInfoService } from '../cards/card-info.service';
import { CardRef } from '../cards/card-ref';
import { DeckEditing } from '../deck/deck-editing.service';
import { PacksService } from '../packs/packs.service';
import { DeckbuilderActions } from './deckbuilder-actions.service';

describe('DeckbuilderActions', () => {
  let actions: DeckbuilderActions;
  let deck: {
    addFromCollection: ReturnType<typeof vi.fn>;
    removeSlotCard: ReturnType<typeof vi.fn>;
    removeDrawCard: ReturnType<typeof vi.fn>;
  };
  let packs: { packClicked: ReturnType<typeof vi.fn>; choose: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    deck = { addFromCollection: vi.fn(), removeSlotCard: vi.fn(), removeDrawCard: vi.fn() };
    packs = { packClicked: vi.fn(), choose: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        { provide: DeckEditing, useValue: deck },
        { provide: PacksService, useValue: packs },
      ],
    });
    actions = TestBed.inject(DeckbuilderActions);
  });

  const drawCopy = { id: 7, blueprintId: '1_12', side: 'FREE_PEOPLE' as const };

  it('adds a collection card to the deck, by its side', () => {
    actions.activate({
      kind: 'collection',
      role: 'card',
      blueprintId: '1_12',
      item: { kind: 'card', blueprintId: '1_12', count: 4, side: 'FREE_PEOPLE', group: 'fp' },
    });
    expect(deck.addFromCollection).toHaveBeenCalledWith('1_12', 'FREE_PEOPLE');
  });

  it('opens packs and selection packs from the collection', () => {
    actions.activate({
      kind: 'collection',
      role: 'selection',
      blueprintId: '(S)FotR - Starter',
      item: { kind: 'pack', blueprintId: '(S)FotR - Starter', count: 1, selection: ['1_1'] },
    });
    expect(packs.packClicked).toHaveBeenCalledWith('(S)FotR - Starter', ['1_1']);
  });

  it('takes a selection pack choice; other shown cards do nothing', () => {
    actions.activate({ kind: 'shown', blueprintId: '1_1', zone: 'selection', choosable: true });
    actions.activate({ kind: 'shown', blueprintId: '1_2', zone: 'player', choosable: false });
    expect(packs.choose).toHaveBeenCalledExactlyOnceWith('1_1');
  });

  it('removes deck cards; copies behind a card and the Race card do nothing', () => {
    actions.activate({ kind: 'slot', slot: 'sites', blueprintId: '1_319', card: { id: 3, blueprintId: '1_319' } });
    actions.activate({ kind: 'draw', blueprintId: '1_12', card: drawCopy });
    actions.activate({ kind: 'behind', blueprintId: '1_12', card: drawCopy });
    actions.activate({ kind: 'race', blueprintId: '90_7', overlayBlueprintId: null });
    expect(deck.removeSlotCard).toHaveBeenCalledExactlyOnceWith('sites', 3);
    expect(deck.removeDrawCard).toHaveBeenCalledExactlyOnceWith(7);
    expect(deck.addFromCollection).not.toHaveBeenCalled();
  });

  it('shows a card in the card information', () => {
    const ref: CardRef = { kind: 'race', blueprintId: '90_7', overlayBlueprintId: null };
    actions.showInfo(ref);
    expect(TestBed.inject(CardInfoService).card()).toBe(ref);
  });
});
