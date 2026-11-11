import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DrawerComponent } from './drawer.component';

@Component({
  imports: [DrawerComponent],
  template: `
    <gd-drawer name="test" label="Test menu">
      <button type="button" class="item" (click)="ran = ran + 1">Item</button>
      <select class="list">
        <option>One</option>
      </select>
    </gd-drawer>
  `,
})
class HostComponent {
  ran = 0;
}

describe('DrawerComponent', () => {
  async function render() {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const burger = el.querySelector<HTMLButtonElement>('button.gd-burger')!;
    const panel = el.querySelector<HTMLDialogElement>('dialog.gd-drawer')!;
    return { fixture, el, burger, panel };
  }

  it('is a burger button and a named panel holding the items (DOM contract)', async () => {
    const { burger, panel } = await render();
    expect(burger.getAttribute('aria-label')).toBe('Test menu');
    expect(burger.title).toBe('Test menu');
    expect(burger.getAttribute('aria-haspopup')).toBe('dialog');
    expect(burger.getAttribute('aria-expanded')).toBe('false');
    expect(panel.dataset['drawer']).toBe('test');
    expect(panel.getAttribute('aria-label')).toBe('Test menu');
    expect(panel.open).toBe(false);
    expect(panel.querySelector('button.item')).not.toBeNull();
  });

  it('opens with the burger, and closes after an item did its job', async () => {
    const { fixture, burger, panel } = await render();
    burger.click();
    await fixture.whenStable();
    expect(panel.open).toBe(true);
    expect(burger.getAttribute('aria-expanded')).toBe('true');
    panel.querySelector<HTMLButtonElement>('button.item')!.click();
    await fixture.whenStable();
    expect(fixture.componentInstance.ran).toBe(1);
    expect(panel.open).toBe(false);
    expect(burger.getAttribute('aria-expanded')).toBe('false');
  });

  it('stays open for a list in it, and for a press inside the panel; closes for a press on the backdrop', async () => {
    const { fixture, burger, panel } = await render();
    burger.click();
    await fixture.whenStable();
    vi.spyOn(panel, 'getBoundingClientRect').mockReturnValue({ left: 0, right: 320, top: 0, bottom: 800 } as DOMRect);
    panel.querySelector<HTMLSelectElement>('select.list')!.click();
    panel.dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 100, clientY: 100, detail: 1 }));
    // A key press on the panel itself clicks at 0,0
    panel.dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 0, clientY: 0, detail: 0 }));
    expect(panel.open).toBe(true);
    panel.dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 360, clientY: 100, detail: 1 }));
    await fixture.whenStable();
    expect(panel.open).toBe(false);
  });

  it('notices when it was closed another way (Escape)', async () => {
    const { fixture, burger, panel } = await render();
    burger.click();
    await fixture.whenStable();
    panel.removeAttribute('open');
    panel.dispatchEvent(new Event('close'));
    await fixture.whenStable();
    expect(burger.getAttribute('aria-expanded')).toBe('false');
  });
});
