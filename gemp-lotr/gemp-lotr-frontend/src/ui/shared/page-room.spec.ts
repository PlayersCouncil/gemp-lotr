import { pageRight, PAGE_RIGHT_PROPERTY } from './page-room';

describe('pageRight', () => {
  afterEach(() => document.documentElement.style.removeProperty(PAGE_RIGHT_PROPERTY));

  it('is nothing until a page gives up room at the right, and then that room', () => {
    expect(pageRight(document)).toBe(0);
    document.documentElement.style.setProperty(PAGE_RIGHT_PROPERTY, '430px');
    expect(pageRight(document)).toBe(430);
    document.documentElement.style.setProperty(PAGE_RIGHT_PROPERTY, 'nonsense');
    expect(pageRight(document)).toBe(0);
  });

  it('is nothing without a window', () => {
    expect(pageRight({ documentElement: document.documentElement, defaultView: null } as unknown as Document)).toBe(0);
  });
});
