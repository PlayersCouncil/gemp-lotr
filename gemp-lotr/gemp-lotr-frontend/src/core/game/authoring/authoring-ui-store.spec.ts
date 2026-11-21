import { TestBed } from '@angular/core/testing';
import { AUTHOR_UI_KEY, AuthoringUiStore, recallUi } from './authoring-ui-store';

describe("the authoring panel's remembered view", () => {
  afterEach(() => sessionStorage.clear());

  it('starts on the timeline, following the game, unfolded, with nothing selected', () => {
    for (const nothing of [null, 'text', 3]) {
      expect(recallUi(nothing)).toEqual({
        tab: 'timeline',
        selected: null,
        selectedUid: null,
        sub: null,
        subAt: null,
        follow: true,
        atUid: null,
        hold: false,
        jump: false,
        folded: false,
        where: 'now',
        board: null,
        sitesApart: null,
        newFights: [],
        carried: null,
      });
    }
  });

  it('reads back what it wrote, and nothing it does not understand', () => {
    const board = { draft: 'x', ops: [{ name: 'wound', args: ['a', 1] }], added: [], removed: ['b'] };
    expect(
      recallUi({
        tab: 'board',
        selected: 3,
        selectedUid: 's3',
        sub: 's3k1',
        subAt: 1,
        follow: false,
        atUid: 's2',
        hold: true,
        jump: true,
        folded: true,
        where: 'setup',
        board,
        sitesApart: true,
        newFights: ['frodo', 3],
        carried: 'It went wrong.',
      }),
    ).toEqual({
      tab: 'board',
      selected: 3,
      selectedUid: 's3',
      sub: 's3k1',
      subAt: 1,
      follow: false,
      atUid: 's2',
      hold: true,
      jump: true,
      folded: true,
      where: 'setup',
      board,
      sitesApart: true,
      newFights: ['frodo'],
      carried: 'It went wrong.',
    });
    expect(recallUi({ tab: 'nowhere', selected: Number.NaN, where: 'there', newFights: 'x' })).toMatchObject({
      tab: 'timeline',
      selected: null,
      where: 'now',
      newFights: [],
    });
  });

  it("keeps it in the tab's session storage as it changes", () => {
    sessionStorage.setItem(AUTHOR_UI_KEY, JSON.stringify({ tab: 'step', selected: 2 }));
    TestBed.configureTestingModule({ providers: [AuthoringUiStore] });
    const store = TestBed.inject(AuthoringUiStore);
    expect(store.ui()).toMatchObject({ tab: 'step', selected: 2 });
    store.update({ folded: true });
    expect(store.ui().folded).toBe(true);
    expect(JSON.parse(sessionStorage.getItem(AUTHOR_UI_KEY)!)).toMatchObject({
      tab: 'step',
      selected: 2,
      folded: true,
    });
  });
});
