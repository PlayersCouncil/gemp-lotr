import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MenuKeysDirective } from './menu-keys.directive';

@Component({
  imports: [MenuKeysDirective],
  template: `
    <div
      gdMenuKeys
      [gdMenuKeysFocus]="focus"
      (forward)="forwards.push($event.id)"
      (back)="backs = backs + 1"
      (escape)="escapes = escapes + 1"
    >
      <button id="a" role="menuitem">A</button>
      <button id="b" role="menuitem" disabled>B</button>
      <input id="field" />
      <button id="c" role="menuitem">C</button>
    </div>
  `,
})
class Host {
  focus = true;
  forwards: string[] = [];
  backs = 0;
  escapes = 0;
}

describe('MenuKeysDirective', () => {
  function render(focus = true) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.focus = focus;
    document.body.append(fixture.nativeElement as HTMLElement);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const key = (target: Element, key: string) => {
      const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
      target.dispatchEvent(event);
      return event;
    };
    return { fixture, el, key, host: fixture.componentInstance };
  }

  it('focuses the first item, and moves between the items that can be used, round and round', () => {
    const { el, key } = render();
    const at = () => document.activeElement?.id;
    expect(at()).toBe('a');
    key(document.activeElement!, 'ArrowDown');
    expect(at()).toBe('c');
    key(document.activeElement!, 'ArrowDown');
    expect(at()).toBe('a');
    key(document.activeElement!, 'ArrowUp');
    expect(at()).toBe('c');
    key(document.activeElement!, 'Home');
    expect(at()).toBe('a');
    key(document.activeElement!, 'End');
    expect(at()).toBe('c');
    // From nowhere in the menu: Up is the last, Down the first
    key(el.querySelector('div')!, 'ArrowUp');
    expect(at()).toBe('c');
    key(el.querySelector('div')!, 'ArrowDown');
    expect(at()).toBe('a');
    el.remove();
  });

  it("says Right on an item, Left and Escape; a field's keys are its own", () => {
    const { el, key, host } = render(false);
    expect(document.activeElement?.id).not.toBe('a');
    key(el.querySelector('#c')!, 'ArrowRight');
    key(el.querySelector('div')!, 'ArrowRight');
    expect(host.forwards).toEqual(['c']);
    key(el.querySelector('#a')!, 'ArrowLeft');
    expect(host.backs).toBe(1);
    const typed = key(el.querySelector('#field')!, 'ArrowLeft');
    expect(typed.defaultPrevented).toBe(false);
    expect(host.backs).toBe(1);
    key(el.querySelector('#field')!, 'Escape');
    expect(host.escapes).toBe(1);
    key(el.querySelector('#a')!, 'x');
    el.remove();
  });
});
