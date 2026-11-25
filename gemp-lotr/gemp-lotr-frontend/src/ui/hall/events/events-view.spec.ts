import { revealRow } from './events-view';

/**
 * EventDrawer.scrollIntoView: the row a link opened is scrolled to the top of its scroller, smoothly unless reduced
 * motion is asked for, once per reveal; while the page grows in the next 2 s the row is brought up again.
 */
describe('revealRow', () => {
  let host: HTMLElement;
  let scroller: HTMLElement;
  let rows: Record<string, HTMLElement>;
  let scrolls: { key: string; options: ScrollIntoViewOptions }[];
  let reduced: boolean;
  let observed: { callback: () => void; target: Element | null; disconnected: boolean }[];
  let rowTop: number;

  beforeEach(() => {
    vi.useFakeTimers();
    host = document.createElement('div');
    scroller = document.createElement('div');
    scroller.style.overflowY = 'auto';
    scroller.append(host);
    document.body.append(scroller);
    rows = {};
    for (const key of ['a', 'b']) {
      const row = document.createElement('gd-event-row');
      row.dataset['key'] = key;
      row.scrollIntoView = (options?: boolean | ScrollIntoViewOptions) =>
        scrolls.push({ key, options: options as ScrollIntoViewOptions });
      host.append(row);
      rows[key] = row;
    }
    scrolls = [];
    reduced = false;
    observed = [];
    rowTop = 0;
    vi.spyOn(rows['a'], 'getBoundingClientRect').mockImplementation(() => ({ top: rowTop }) as DOMRect);
    vi.spyOn(scroller, 'getBoundingClientRect').mockReturnValue({ top: 0 } as DOMRect);
    vi.stubGlobal('matchMedia', (query: string) => ({ matches: reduced && query.includes('reduce') }));
    vi.stubGlobal(
      'ResizeObserver',
      class {
        private readonly entry;
        constructor(callback: () => void) {
          this.entry = { callback, target: null as Element | null, disconnected: false };
          observed.push(this.entry);
        }
        observe(target: Element) {
          this.entry.target = target;
        }
        disconnect() {
          this.entry.disconnected = true;
        }
      },
    );
  });

  afterEach(() => {
    scroller.remove();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('scrolls the revealed row to the top smoothly, once per reveal', () => {
    revealRow(host, { key: 'a', seq: 1 });
    revealRow(host, { key: 'a', seq: 1 });
    expect(scrolls).toEqual([{ key: 'a', options: { block: 'start', behavior: 'smooth' } }]);

    revealRow(host, { key: 'a', seq: 2 });
    expect(scrolls).toHaveLength(2);
  });

  it('jumps without motion when reduced motion is asked for', () => {
    reduced = true;
    revealRow(host, { key: 'b', seq: 1 });
    expect(scrolls).toEqual([{ key: 'b', options: { block: 'start', behavior: 'auto' } }]);
  });

  it('does nothing with no reveal, or a row that is not drawn (yet: a later call with it scrolls)', () => {
    revealRow(host, null);
    revealRow(host, { key: 'nope', seq: 1 });
    expect(scrolls).toEqual([]);
    expect(observed).toEqual([]);
  });

  it('brings the row up again while the page grows, until it is at the top, for 2 s', () => {
    revealRow(host, { key: 'a', seq: 1 });
    expect(observed).toHaveLength(1);
    const [growing] = observed;
    expect(growing.target).toBe(host);

    // the page was too short: the row stopped below the top
    rowTop = 120;
    growing.callback();
    expect(scrolls).toHaveLength(2);
    // at the top (within a pixel): left alone
    rowTop = 0.5;
    growing.callback();
    expect(scrolls).toHaveLength(2);

    vi.advanceTimersByTime(1999);
    expect(growing.disconnected).toBe(false);
    vi.advanceTimersByTime(1);
    expect(growing.disconnected).toBe(true);
  });

  it('leaves the row alone while it is in nothing that scrolls', () => {
    scroller.style.overflowY = 'visible';
    revealRow(host, { key: 'a', seq: 1 });
    rowTop = 120;
    observed[0].callback();
    expect(scrolls).toHaveLength(1);
  });

  it('scrolls once where the page has no layout to scroll, or no resize observer', () => {
    rows['a'].scrollIntoView = () => {
      throw new Error('no layout');
    };
    vi.stubGlobal('ResizeObserver', undefined);
    expect(() => revealRow(host, { key: 'a', seq: 1 })).not.toThrow();
    expect(observed).toEqual([]);
  });
});
