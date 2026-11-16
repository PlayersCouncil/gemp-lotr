import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SWIPE_MAX_MS, SwipeDirective } from './swipe.directive';

@Component({
  imports: [SwipeDirective],
  template: `
    <div
      class="target"
      gdSwipe
      [gdSwipeEnabled]="enabled()"
      (swipeLeft)="swipes.push('left')"
      (swipeRight)="swipes.push('right')"
    >
      <span class="inner"></span><input class="field" type="range" />
      <dialog open><span class="in-dialog"></span></dialog>
    </div>
  `,
})
class Host {
  readonly enabled = signal(true);
  readonly swipes: string[] = [];
}

describe('SwipeDirective', () => {
  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const host = fixture.componentInstance;
    /** A pointer's event (jsdom has no PointerEvent): a finger, unless said otherwise. */
    const send = (on: string, type: string, x: number, y: number, init: Record<string, unknown> = {}) => {
      const event = new MouseEvent(type, { clientX: x, clientY: y, bubbles: true, cancelable: true });
      Object.defineProperties(event, {
        pointerType: { value: init['pointerType'] ?? 'touch' },
        pointerId: { value: init['pointerId'] ?? 1 },
        timeStamp: { value: init['time'] ?? 0 },
      });
      el.querySelector(on)!.dispatchEvent(event);
      return event;
    };
    const swipe = (from: number, to: number, init: Record<string, unknown> = {}, on = '.inner', dy = 0) => {
      send(on, 'pointerdown', from, 100, { ...init, time: 0 });
      return send(on, 'pointerup', to, 100 + dy, { ...init, time: init['time'] ?? 200 });
    };
    return { host, send, swipe };
  }

  it('tells a swipe to the left from one to the right, and cancels nothing', async () => {
    const { host, swipe } = await render();
    expect(swipe(200, 100).defaultPrevented).toBe(false);
    swipe(100, 200);
    expect(host.swipes).toEqual(['left', 'right']);
  });

  it('takes a pen too, but not the mouse', async () => {
    const { host, swipe } = await render();
    swipe(200, 100, { pointerType: 'pen' });
    swipe(200, 100, { pointerType: 'mouse' });
    expect(host.swipes).toEqual(['left']);
  });

  it('ignores a short move, a mostly vertical one (a scroll) and a slow one', async () => {
    const { host, swipe } = await render();
    swipe(200, 160);
    swipe(200, 100, {}, '.inner', 60);
    swipe(200, 100, { time: SWIPE_MAX_MS + 1 });
    expect(host.swipes).toEqual([]);
    swipe(200, 100, {}, '.inner', 40);
    expect(host.swipes).toEqual(['left']);
  });

  it('ignores a swipe from a field or a slider, or in a dialog', async () => {
    const { host, swipe } = await render();
    swipe(200, 100, {}, '.field');
    swipe(200, 100, {}, '.in-dialog');
    expect(host.swipes).toEqual([]);
  });

  it('ignores a move the browser took over (a scroll), and another finger', async () => {
    const { host, send, swipe } = await render();
    send('.inner', 'pointerdown', 200, 100);
    send('.inner', 'pointercancel', 200, 100);
    send('.inner', 'pointerup', 100, 100);
    send('.inner', 'pointerdown', 200, 100);
    send('.inner', 'pointerup', 100, 100, { pointerId: 2 });
    expect(host.swipes).toEqual([]);
    swipe(200, 100);
    expect(host.swipes).toEqual(['left']);
  });

  it('does nothing while it is off', async () => {
    const { host, swipe } = await render();
    host.enabled.set(false);
    TestBed.tick();
    swipe(200, 100);
    expect(host.swipes).toEqual([]);
  });
});
