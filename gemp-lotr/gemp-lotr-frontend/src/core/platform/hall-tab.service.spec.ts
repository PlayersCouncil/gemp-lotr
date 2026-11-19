import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { HALL_PAGE_URL, HallTab } from './hall-tab.service';

/** A BroadcastChannel that keeps what is posted on it; the test answers for the hall. */
class FakeChannel {
  static made: FakeChannel[] = [];
  readonly posted: unknown[] = [];
  closed = false;
  onmessage: ((event: MessageEvent) => void) | null = null;

  constructor(readonly name: string) {
    FakeChannel.made.push(this);
  }

  postMessage(message: unknown): void {
    this.posted.push(message);
  }

  close(): void {
    this.closed = true;
  }

  /** What the hall says back. */
  hear(data: unknown): void {
    this.onmessage?.({ data } as MessageEvent);
  }
}

describe('HallTab', () => {
  let view: {
    location: { href: string };
    close: ReturnType<typeof vi.fn>;
    setTimeout: typeof setTimeout;
    BroadcastChannel?: unknown;
  };

  function hall(withChannels = true, withView = true): HallTab {
    view = {
      location: { href: '/gemp-lotr/ng/game?gameId=7' },
      close: vi.fn(),
      setTimeout: ((action: () => void, ms: number) => setTimeout(action, ms)) as typeof setTimeout,
      ...(withChannels ? { BroadcastChannel: FakeChannel } : {}),
    };
    TestBed.configureTestingModule({
      providers: [{ provide: DOCUMENT, useValue: { defaultView: withView ? view : null } }],
    });
    return TestBed.inject(HallTab);
  }

  beforeEach(() => {
    vi.useFakeTimers();
    FakeChannel.made = [];
  });

  afterEach(() => vi.useRealTimers());

  it('closes this tab when the hall is open in another, telling it where to go; says so if the tab stays', () => {
    const tab = hall();
    tab.go('#account/scenarios');
    const [channel] = FakeChannel.made;
    expect(channel?.name).toBe('gemp-lotr-hall');
    expect(channel?.posted).toEqual([{ type: 'anyone' }]);
    // Something else on the channel is no answer
    channel?.hear({ type: 'go' });
    channel?.hear(null);
    expect(view.close).not.toHaveBeenCalled();
    channel?.hear({ type: 'here' });
    expect(channel?.posted).toEqual([{ type: 'anyone' }, { type: 'go', hash: '#account/scenarios' }]);
    expect(channel?.closed).toBe(true);
    expect(view.close).toHaveBeenCalledTimes(1);
    // A second hall answering changes nothing
    channel?.hear({ type: 'here' });
    expect(view.close).toHaveBeenCalledTimes(1);
    // The browser wouldn't close the tab: still here a moment later
    vi.advanceTimersByTime(399);
    expect(tab.elsewhere()).toBeNull();
    vi.advanceTimersByTime(1);
    expect(tab.elsewhere()).toBe('#account/scenarios');
    // (and the hall isn't opened here as well)
    vi.advanceTimersByTime(1000);
    expect(view.location.href).toBe('/gemp-lotr/ng/game?gameId=7');
  });

  it('tells the hall nothing of where to go when nowhere was asked for', () => {
    const tab = hall();
    tab.go();
    FakeChannel.made[0]?.hear({ type: 'here' });
    expect(FakeChannel.made[0]?.posted).toEqual([{ type: 'anyone' }]);
    vi.advanceTimersByTime(400);
    expect(tab.elsewhere()).toBe('');
  });

  it('opens the hall here when no hall answers in time', () => {
    const tab = hall();
    tab.go('#x');
    vi.advanceTimersByTime(349);
    expect(view.location.href).not.toContain('hall');
    vi.advanceTimersByTime(1);
    expect(FakeChannel.made[0]?.closed).toBe(true);
    expect(view.location.href).toBe(`${HALL_PAGE_URL}#x`);
    expect(tab.elsewhere()).toBeNull();
  });

  it('opens the hall here in a browser without channels, and does nothing without a window', () => {
    hall(false).go('#y');
    expect(view.location.href).toBe(`${HALL_PAGE_URL}#y`);
    TestBed.resetTestingModule();
    const nowhere = hall(true, false);
    nowhere.go();
    nowhere.openHere();
    expect(FakeChannel.made).toEqual([]);
  });
});
