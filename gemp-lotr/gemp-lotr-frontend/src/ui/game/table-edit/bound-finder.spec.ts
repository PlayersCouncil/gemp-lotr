import { TestBed } from '@angular/core/testing';
import { BoundFinder } from './bound-finder';

describe('BoundFinder', () => {
  it('holds what the search is opened for, and hands the filter found there once', () => {
    TestBed.configureTestingModule({ providers: [BoundFinder] });
    const finder = TestBed.inject(BoundFinder);
    const used: string[] = [];
    finder.open('pool', (filter) => used.push(filter));
    expect(finder.finding()?.what).toBe('pool');
    finder.use('set:1-6');
    expect(used).toEqual(['set:1-6']);
    expect(finder.finding()).toBeNull();
    finder.use('again');
    expect(used).toEqual(['set:1-6']);
    finder.open('pool', (filter) => used.push(filter));
    finder.close();
    expect(finder.finding()).toBeNull();
  });
});
