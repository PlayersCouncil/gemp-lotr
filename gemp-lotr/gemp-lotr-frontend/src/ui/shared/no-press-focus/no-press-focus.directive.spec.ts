import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { NoPressFocusDirective } from './no-press-focus.directive';

@Component({
  imports: [NoPressFocusDirective],
  template: `<input /><button gdNoPressFocus>A button</button>`,
})
class Host {}

describe('NoPressFocusDirective', () => {
  function render() {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    document.body.appendChild(fixture.nativeElement as HTMLElement);
    const el = fixture.nativeElement as HTMLElement;
    return { input: el.querySelector('input')!, button: el.querySelector('button')! };
  }

  it('takes the focus away from what had it, and does not take it', () => {
    const { input, button } = render();
    input.focus();
    const press = new MouseEvent('mousedown', { bubbles: true, cancelable: true });
    button.dispatchEvent(press);
    expect(press.defaultPrevented).toBe(true);
    expect(document.activeElement).toBe(document.body);
  });

  it('leaves the keyboard able to reach it', () => {
    const { button } = render();
    button.focus();
    expect(document.activeElement).toBe(button);
  });
});
