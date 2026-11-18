import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { SessionStore } from './session-store';

function storeWith(sessionStorage: Partial<Storage> | null | 'throws'): SessionStore {
  const defaultView =
    sessionStorage === 'throws'
      ? {
          get sessionStorage(): Storage {
            throw new Error('blocked');
          },
        }
      : { sessionStorage };
  TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: { defaultView } }] });
  return TestBed.inject(SessionStore);
}

describe('SessionStore', () => {
  afterEach(() => sessionStorage.clear());

  it("keeps JSON values in the tab's session storage", () => {
    const store = TestBed.inject(SessionStore);
    store.write('a', { x: [1, 'two'] });
    expect(store.read('a')).toEqual({ x: [1, 'two'] });
    expect(sessionStorage.getItem('a')).toBe('{"x":[1,"two"]}');
    store.remove('a');
    expect(store.read('a')).toBeNull();
  });

  it('reads nothing for a value that is not JSON', () => {
    sessionStorage.setItem('b', '{nope');
    expect(TestBed.inject(SessionStore).read('b')).toBeNull();
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
