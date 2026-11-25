import { TestBed } from '@angular/core/testing';
import { parseJson } from '../../api/json';
import { LocalStore } from '../../platform/local-store';
import { readRecent, RECENT_KEY, RECENT_MAX, RecentCards, used } from './recent-cards';

const legolas = { blueprintId: '1_50', name: 'Legolas, Greenleaf', type: 'COMPANION' };
const gimli = { blueprintId: '1_13', name: 'Gimli, Son of Glóin', type: 'COMPANION' };

describe('RecentCards', () => {
  afterEach(() => localStorage.clear());

  it('keeps the card used last at the head, once, a dozen at most', () => {
    expect(used([gimli, legolas], legolas)).toEqual([legolas, gimli]);
    const many = Array.from({ length: 20 }, (_, at) => ({ blueprintId: `1_${at}`, name: `${at}`, type: '' }));
    expect(many.reduce(used, [])).toHaveLength(RECENT_MAX);
    expect(many.reduce(used, [])[0]?.blueprintId).toBe('1_19');
  });

  it('reads what storage held, leaving out what is not a card', () => {
    expect(readRecent(null)).toEqual([]);
    expect(readRecent([legolas, { name: 'nameless' }, 'x', null, { blueprintId: '1_2', name: 'Ring' }])).toEqual([
      legolas,
      { blueprintId: '1_2', name: 'Ring', type: '' },
    ]);
  });

  it("remembers them in this viewer's local storage, and works without it", () => {
    TestBed.configureTestingModule({ providers: [RecentCards] });
    const recent = TestBed.inject(RecentCards);
    expect(recent.list()).toEqual([]);
    recent.use(legolas);
    recent.use(gimli);
    expect(recent.list()).toEqual([gimli, legolas]);
    expect(parseJson(localStorage.getItem(RECENT_KEY)!)).toEqual([gimli, legolas]);

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [RecentCards, { provide: LocalStore, useValue: { read: () => null, write: () => undefined } }],
    });
    const without = TestBed.inject(RecentCards);
    without.use(legolas);
    expect(without.list()).toEqual([legolas]);
  });
});
