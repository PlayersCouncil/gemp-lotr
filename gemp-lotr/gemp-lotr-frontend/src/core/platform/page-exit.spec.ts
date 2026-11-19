import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { PageExit } from './page-exit';

function exitWith(defaultView: unknown): PageExit {
  TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: { defaultView } }] });
  return TestBed.inject(PageExit);
}

describe('PageExit', () => {
  it('hears the page being hidden until told to stop', () => {
    const exit = TestBed.inject(PageExit);
    const leaving = vi.fn();
    const stop = exit.onLeave(leaving);
    window.dispatchEvent(new Event('pagehide'));
    expect(leaving).toHaveBeenCalledTimes(1);
    stop();
    window.dispatchEvent(new Event('pagehide'));
    expect(leaving).toHaveBeenCalledTimes(1);
  });

  it('sends a beacon of the fields, form-encoded', () => {
    const sendBeacon = vi.fn(() => true);
    const exit = exitWith({ navigator: { sendBeacon } });
    expect(exit.beacon('/x/left', { left: 'true' })).toBe(true);
    const [url, body] = sendBeacon.mock.calls[0] as unknown as [string, URLSearchParams];
    expect(url).toBe('/x/left');
    expect(body.toString()).toBe('left=true');
  });

  it('says so when the browser has no beacons, or refuses one', () => {
    expect(exitWith({ navigator: {} }).beacon('/x', {})).toBe(false);
    TestBed.resetTestingModule();
    const refusing = exitWith({
      navigator: {
        sendBeacon: () => {
          throw new Error('no');
        },
      },
    });
    expect(refusing.beacon('/x', {})).toBe(false);
    TestBed.resetTestingModule();
    const none = exitWith(null);
    expect(none.beacon('/x', {})).toBe(false);
    expect(none.onLeave(() => undefined)()).toBeUndefined();
  });
});
