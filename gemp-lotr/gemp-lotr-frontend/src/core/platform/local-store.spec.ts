import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { LocalStore } from './local-store';

function storeWith(localStorage: Partial<Storage> | null | 'throws'): LocalStore {
  const defaultView =
    localStorage === 'throws'
      ? {
          get localStorage(): Storage {
            throw new Error('blocked');
          },
        }
      : { localStorage };
  TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: { defaultView } }] });
  return TestBed.inject(LocalStore);
}

describe('LocalStore', () => {
  afterEach(() => localStorage.clear());

  it('keeps JSON values in local storage', () => {
    const store = TestBed.inject(LocalStore);
    store.write('a', { x: [1, 'two'] });
    expect(store.read('a')).toEqual({ x: [1, 'two'] });
    expect(localStorage.getItem('a')).toBe('{"x":[1,"two"]}');
    store.remove('a');
    expect(store.read('a')).toBeNull();
  });

  it('reads nothing for a value that is not JSON', () => {
    localStorage.setItem('b', '{nope');
    expect(TestBed.inject(LocalStore).read('b')).toBeNull();
  });

  it('forgets quietly when the browser has no storage, blocks it or is full', () => {
    for (const storage of [null, 'throws'] as const) {
      TestBed.resetTestingModule();
      const store = storeWith(storage);
      store.write('a', 1);
      expect(store.read('a')).toBeNull();
      store.remove('a');
    }
    TestBed.resetTestingModule();
    const full = storeWith({
      setItem: () => {
        throw new Error('full');
      },
      getItem: () => {
        throw new Error('blocked');
      },
      removeItem: () => {
        throw new Error('blocked');
      },
    });
    full.write('a', 1);
    expect(full.read('a')).toBeNull();
    full.remove('a');
  });
});
