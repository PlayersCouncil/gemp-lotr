import { TestBed } from '@angular/core/testing';
import { COMPACT_QUERY, CompactLayout } from './compact-layout.service';
import { PAGE_QUERY_STRING } from './page-location';
import { ThemeService } from './theme.service';

describe('CompactLayout', () => {
  /** A screen that matches COMPACT_QUERY or not; `turn` changes it, as turning the device does. */
  function screen(matches: boolean) {
    const query = { matches, onchange: null as ((event: { matches: boolean }) => void) | null };
    const matchMedia = vi.fn(() => query);
    Object.defineProperty(window, 'matchMedia', { value: matchMedia, configurable: true, writable: true });
    return { matchMedia, turn: (now: boolean) => query.onchange?.({ matches: now }) };
  }

  function layout(theme: 'classic' | 'modern') {
    TestBed.configureTestingModule({ providers: [{ provide: PAGE_QUERY_STRING, useValue: `?theme=${theme}` }] });
    vi.spyOn(document, 'cookie', 'set').mockImplementation(() => undefined);
    TestBed.inject(ThemeService).init();
    return TestBed.inject(CompactLayout);
  }

  afterEach(() => {
    vi.restoreAllMocks();
    Reflect.deleteProperty(window, 'matchMedia');
    document.documentElement.removeAttribute('data-theme');
  });

  it('is compact in the modern theme on a portrait or narrow screen, and follows the screen', () => {
    const { matchMedia, turn } = screen(true);
    const compact = layout('modern');
    expect(matchMedia).toHaveBeenCalledWith(COMPACT_QUERY);
    expect(compact.active()).toBe(true);
    turn(false);
    expect(compact.active()).toBe(false);
    turn(true);
    expect(compact.active()).toBe(true);
  });

  it('is never compact in the classic theme, and follows the theme', () => {
    screen(true);
    const compact = layout('classic');
    expect(compact.active()).toBe(false);
    TestBed.inject(ThemeService).select('modern');
    expect(compact.active()).toBe(true);
  });

  it('is not compact on a wide screen, or without matchMedia', () => {
    screen(false);
    expect(layout('modern').active()).toBe(false);
  });
});
