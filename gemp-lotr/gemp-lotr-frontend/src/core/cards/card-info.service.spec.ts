import { provideDeckbuilderState } from '../deckbuilder-state';
import { TestBed } from '@angular/core/testing';
import { CardInfoService } from './card-info.service';
import { CardRef } from './card-ref';

const CARD: CardRef = { kind: 'shown', blueprintId: '1_2', zone: 'COLLECTION', choosable: false };

describe('CardInfoService', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: provideDeckbuilderState() }));

  let info: CardInfoService;

  beforeEach(() => (info = TestBed.inject(CardInfoService)));

  it('shows and closes a card', () => {
    expect(info.isOpen()).toBe(false);
    info.show(CARD);
    expect(info.card()).toBe(CARD);
    info.close();
    expect(info.isOpen()).toBe(false);
  });

  it('after a drag up, lets the one click that ends the drag through', () => {
    info.showDragged(CARD);
    expect(info.takeDragRelease()).toBe(true);
    expect(info.takeDragRelease()).toBe(false);
  });

  it('forgets the drag when a new press starts', () => {
    info.showDragged(CARD);
    info.forgetDragRelease();
    expect(info.takeDragRelease()).toBe(false);
  });

  it('has no drag release after an ordinary show', () => {
    info.show(CARD);
    expect(info.takeDragRelease()).toBe(false);
  });
});
