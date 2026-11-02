import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiButtonDirective } from './ui-button.directive';

@Component({
  imports: [UiButtonDirective],
  template: `
    <button id="text" gempUiButton [disabled]="disabled()"><span class="ui-button-text">x</span></button>
    <button id="icon" gempUiButton="icon"></button>
  `,
})
class Host {
  readonly disabled = signal(false);
}

describe('UiButtonDirective', () => {
  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, text: el.querySelector<HTMLButtonElement>('#text')!, icon: el.querySelector('#icon')! };
  }

  it('adds the jQuery UI button classes', async () => {
    const { text, icon } = await render();
    expect(text.className).toBe('ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only');
    expect(icon.classList).toContain('ui-button-icon-only');
    expect(text.getAttribute('role')).toBe('button');
    expect(text.getAttribute('aria-disabled')).toBe('false');
  });

  it('shows hover, pressed and focus states like jQuery UI', async () => {
    const { fixture, text } = await render();
    text.dispatchEvent(new MouseEvent('mouseenter'));
    text.dispatchEvent(new MouseEvent('mousedown'));
    await fixture.whenStable();
    expect(text.classList).toContain('ui-state-hover');
    expect(text.classList).toContain('ui-state-active');

    document.dispatchEvent(new MouseEvent('mouseup'));
    text.dispatchEvent(new FocusEvent('focus'));
    await fixture.whenStable();
    expect(text.classList).not.toContain('ui-state-active');
    expect(text.classList).toContain('ui-state-focus');

    text.dispatchEvent(new MouseEvent('mouseleave'));
    await fixture.whenStable();
    expect(text.classList).not.toContain('ui-state-hover');
  });

  it('shows the disabled state and ignores the mouse', async () => {
    const { fixture, text } = await render();
    fixture.componentInstance.disabled.set(true);
    await fixture.whenStable();
    expect(text.disabled).toBe(true);
    expect(text.classList).toContain('ui-button-disabled');
    expect(text.classList).toContain('ui-state-disabled');
    expect(text.getAttribute('aria-disabled')).toBe('true');
    text.dispatchEvent(new MouseEvent('mouseenter'));
    text.dispatchEvent(new MouseEvent('mousedown'));
    await fixture.whenStable();
    expect(text.classList).not.toContain('ui-state-hover');
    expect(text.classList).not.toContain('ui-state-active');
  });
});
