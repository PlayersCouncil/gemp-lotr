import { TestBed } from '@angular/core/testing';
import { PuzzlePopups } from './puzzle-popups';

describe('PuzzlePopups', () => {
  it("opens a slot's chooser where the slot is, gives the focus back on closing, and opens its search", () => {
    TestBed.configureTestingModule({ providers: [PuzzlePopups] });
    const popups = TestBed.inject(PuzzlePopups);
    const spot = document.createElement('button');
    document.body.append(spot);
    popups.openChooser('ally', spot);
    expect(popups.chooser()).toEqual(expect.objectContaining({ slot: 'ally', element: spot }));
    popups.closeChooser(true);
    expect(document.activeElement).toBe(spot);
    popups.openChooser('ally', spot);
    popups.openSearch('ally');
    expect(popups.chooser()).toBeNull();
    expect(popups.search()).toBe('ally');
    popups.closeSearch();
    expect(popups.search()).toBeNull();
    popups.closeChooser();
    spot.remove();
  });
});
