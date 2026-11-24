import { TestBed } from '@angular/core/testing';
import { PathPopups } from './path-popups';

describe('PathPopups', () => {
  it('opens the site chooser by what opened it, then its search at the same number, one at a time', () => {
    TestBed.configureTestingModule({ providers: [PathPopups] });
    const popups = TestBed.inject(PathPopups);
    const spot = document.createElement('button');
    document.body.append(spot);
    popups.openChooser(4, spot);
    expect(popups.chooser()).toMatchObject({ number: 4, element: spot, replacing: false });
    popups.openSearch();
    expect(popups.chooser()).toBeNull();
    expect(popups.search()).toMatchObject({ number: 4 });
    // (no chooser open: no search)
    popups.closeSearch();
    popups.openSearch();
    expect(popups.search()).toBeNull();
    // A chooser opened closes the search; closed, it gives the focus back
    popups.openChooser(5, spot, true);
    popups.openSearch();
    popups.openChooserAt(6, { left: 1, top: 2, right: 3, bottom: 4 }, spot, true);
    expect(popups.search()).toBeNull();
    expect(popups.chooser()).toEqual({
      number: 6,
      anchor: { left: 1, top: 2, right: 3, bottom: 4 },
      element: spot,
      replacing: true,
    });
    popups.closeChooser(true);
    expect(document.activeElement).toBe(spot);
    popups.closeChooser(true);
    spot.remove();
  });
});
