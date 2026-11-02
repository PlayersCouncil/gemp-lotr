import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiSliderComponent } from './ui-slider.component';

@Component({
  imports: [UiSliderComponent],
  template: `<div
    gempUiSlider
    [min]="4"
    [max]="40"
    [value]="18"
    [disabled]="disabled()"
    (slide)="slid.push($event)"
  ></div>`,
})
class Host {
  readonly disabled = signal(false);
  readonly slid: number[] = [];
}

describe('UiSliderComponent', () => {
  async function render(disabled = false) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.disabled.set(disabled);
    await fixture.whenStable();
    const slider = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('[gempUiSlider]')!;
    const handle = slider.querySelector<HTMLAnchorElement>('a')!;
    return { fixture, slider, handle, slid: fixture.componentInstance.slid };
  }

  const key = (handle: HTMLElement, name: string) => {
    handle.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true }));
    handle.dispatchEvent(new KeyboardEvent('keyup', { key: name, bubbles: true }));
  };

  it('has the jQuery UI slider markup, with the handle at the value', async () => {
    const { slider, handle } = await render();
    expect(slider.className).toBe('ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all');
    expect(slider.getAttribute('aria-disabled')).toBe('false');
    expect(handle.className).toBe('ui-slider-handle ui-state-default ui-corner-all');
    expect(handle.getAttribute('href')).toBe('#');
    expect(parseFloat(handle.style.left)).toBeCloseTo(38.8889, 3);
  });

  it('is marked as a disabled slider without aria-disabled, as jQuery UI created it', async () => {
    const { slider } = await render(true);
    expect(slider.classList).toContain('ui-slider-disabled');
    expect(slider.classList).toContain('ui-disabled');
    expect(slider.hasAttribute('aria-disabled')).toBe(false);
  });

  it('steps with the keyboard and stops at the limits', async () => {
    const { fixture, handle, slid } = await render();
    key(handle, 'ArrowRight');
    key(handle, 'ArrowUp');
    key(handle, 'ArrowLeft');
    key(handle, 'PageUp');
    key(handle, 'End');
    key(handle, 'ArrowRight');
    key(handle, 'Home');
    key(handle, 'PageDown');
    await fixture.whenStable();
    // PageUp moves a fifth of the range (7.2), rounded to the step
    expect(slid).toEqual([19, 20, 19, 26, 40, 4]);
    expect(handle.style.left).toBe('0%');
  });

  it('jumps to a clicked point of the track', async () => {
    const { fixture, slider, slid } = await render();
    vi.spyOn(slider, 'getBoundingClientRect').mockReturnValue(new DOMRect(100, 0, 360, 10));
    slider.dispatchEvent(new MouseEvent('mousedown', { clientX: 190, button: 0, bubbles: true }));
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 460 }));
    document.dispatchEvent(new MouseEvent('mouseup'));
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 100 }));
    await fixture.whenStable();
    // 90 of 360px = 25% of 4..40 = 13; then dragged to the end; moves after release are ignored
    expect(slid).toEqual([13, 40]);
  });

  it('ignores the mouse and keys while disabled', async () => {
    const { slider, handle, slid } = await render(true);
    slider.dispatchEvent(new MouseEvent('mousedown', { clientX: 10, button: 0, bubbles: true }));
    key(handle, 'End');
    expect(slid).toEqual([]);
  });

  it('shows hover and the active state while sliding', async () => {
    const { fixture, slider, handle } = await render();
    handle.dispatchEvent(new MouseEvent('mouseenter'));
    slider.dispatchEvent(new MouseEvent('mousedown', { clientX: 0, button: 0, bubbles: true }));
    await fixture.whenStable();
    expect(handle.classList).toContain('ui-state-hover');
    expect(handle.classList).toContain('ui-state-active');
    expect(handle.classList).toContain('ui-state-focus');
    document.dispatchEvent(new MouseEvent('mouseup'));
    await fixture.whenStable();
    expect(handle.classList).not.toContain('ui-state-active');
  });
});
