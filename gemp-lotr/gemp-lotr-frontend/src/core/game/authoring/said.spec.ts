import { indexesOf, leftOutOf } from './said';

describe('messages left out on the table', () => {
  it('knows exactly which were left out when each message said carries its place', () => {
    const left = leftOutOf([false, true, true], { indexes: [0, 2], count: 2 });
    expect([...left.places]).toEqual([1]);
    expect(left.sure).toBe(true);
  });

  it('tells from how many were said only what can be told', () => {
    // (as many missing as have a condition: those)
    expect(leftOutOf([false, true], { indexes: null, count: 1 })).toEqual({ places: new Set([1]), sure: true });
    // (fewer: any of them may be)
    expect(leftOutOf([true, true, false], { indexes: null, count: 2 })).toEqual({
      places: new Set([0, 1]),
      sure: false,
    });
    // (none missing, or more than have one: not their conditions)
    expect(leftOutOf([true, false], { indexes: null, count: 2 }).places.size).toBe(0);
    expect(leftOutOf([true, false, false], { indexes: null, count: 0 }).places.size).toBe(0);
    expect(leftOutOf([true], null).places.size).toBe(0);
  });

  it("reads the places off a step event's messages, if every one has one", () => {
    expect(indexesOf([{ index: 0 }, { index: 2 }])).toEqual([0, 2]);
    expect(indexesOf([{ index: 0 }, { html: 'x' }])).toBeNull();
    expect(indexesOf([null])).toBeNull();
    expect(indexesOf([])).toBeNull();
  });
});
