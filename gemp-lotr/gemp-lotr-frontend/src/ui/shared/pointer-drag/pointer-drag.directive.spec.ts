import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DragHandlers, PointerDragDirective } from './pointer-drag.directive';

@Component({
  imports: [PointerDragDirective],
  template: `@if (shown()) {
    <div class="target" [gdPointerDrag]="start"><span class="inner"></span></div>
  }`,
})
class Host {
  readonly shown = signal(true);
  handlers: DragHandlers | null = null;
  readonly presses: PointerEvent[] = [];
  readonly start = (event: PointerEvent) => {
    this.presses.push(event);
    return this.handlers;
  };
}

describe('PointerDragDirective', () => {
  function pointer(type: string, init: MouseEventInit = {}): PointerEvent {
    return new MouseEvent(type, { bubbles: true, cancelable: true, ...init }) as PointerEvent;
  }

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const host = fixture.componentInstance;
    const target = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.target')!;
    const handlers = { move: vi.fn(), end: vi.fn(), cancel: vi.fn() };
    host.handlers = handlers;
    return { fixture, host, target, handlers };
  }

  it('ignores other mouse buttons', async () => {
    const { host, target } = await render();
    target.dispatchEvent(pointer('pointerdown', { button: 2 }));
    expect(host.presses).toEqual([]);
  });

  it('asks whether this press starts a drag, and does nothing when it does not', async () => {
    const { host, target, handlers } = await render();
    host.handlers = null;
    const press = pointer('pointerdown');
    target.dispatchEvent(press);
    expect(host.presses.length).toBe(1);
    expect(press.defaultPrevented).toBe(false);
    target.dispatchEvent(pointer('pointermove'));
    expect(handlers.move).not.toHaveBeenCalled();
  });

  it('captures the pointer and stops the browser acting on the press', async () => {
    const { target } = await render();
    const capture = vi.fn();
    target.setPointerCapture = capture;
    const press = new MouseEvent('pointerdown', { bubbles: true, cancelable: true }) as PointerEvent;
    Object.defineProperty(press, 'pointerId', { value: 7 });
    target.dispatchEvent(press);
    expect(press.defaultPrevented).toBe(true);
    expect(capture).toHaveBeenCalledWith(7);
  });

  it('reports moves until the release', async () => {
    const { target, handlers } = await render();
    target.dispatchEvent(pointer('pointerdown'));
    target.dispatchEvent(pointer('pointermove', { clientX: 5 }));
    target.dispatchEvent(pointer('pointermove', { clientX: 9 }));
    target.dispatchEvent(pointer('pointerup'));
    target.dispatchEvent(pointer('pointermove', { clientX: 20 }));
    expect(handlers.move.mock.calls.map(([event]) => event.clientX)).toEqual([5, 9]);
    expect(handlers.end).toHaveBeenCalledOnce();
    expect(handlers.cancel).not.toHaveBeenCalled();
  });

  it('reports a cancelled drag as cancelled, not as a release', async () => {
    const { target, handlers } = await render();
    target.dispatchEvent(pointer('pointerdown'));
    target.dispatchEvent(pointer('pointercancel'));
    expect(handlers.cancel).toHaveBeenCalledOnce();
    expect(handlers.end).not.toHaveBeenCalled();
  });

  it('treats a cancelled drag as ended when there is no cancel handler', async () => {
    const { host, target } = await render();
    const end = vi.fn();
    host.handlers = { move: vi.fn(), end };
    target.dispatchEvent(pointer('pointerdown'));
    target.dispatchEvent(pointer('pointercancel'));
    expect(end).toHaveBeenCalledOnce();
  });

  it('starts from presses on what is inside the element', async () => {
    const { host, target } = await render();
    target.querySelector('.inner')!.dispatchEvent(pointer('pointerdown'));
    expect(host.presses.length).toBe(1);
  });

  it('can run a second drag after the first', async () => {
    const { target, handlers } = await render();
    for (let drag = 0; drag < 2; drag++) {
      target.dispatchEvent(pointer('pointerdown'));
      target.dispatchEvent(pointer('pointermove'));
      target.dispatchEvent(pointer('pointerup'));
    }
    expect(handlers.move).toHaveBeenCalledTimes(2);
    expect(handlers.end).toHaveBeenCalledTimes(2);
  });

  it('stops listening when the element goes away in the middle of a drag', async () => {
    const { fixture, host, target, handlers } = await render();
    target.dispatchEvent(pointer('pointerdown'));
    host.shown.set(false);
    await fixture.whenStable();
    target.dispatchEvent(pointer('pointermove'));
    expect(handlers.move).not.toHaveBeenCalled();
  });
});
