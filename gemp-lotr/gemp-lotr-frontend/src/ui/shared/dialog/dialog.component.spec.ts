import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DialogComponent } from './dialog.component';

@Component({
  imports: [DialogComponent],
  template: `
    <gd-dialog
      name="test"
      heading="A title"
      [open]="open()"
      [modal]="modal()"
      [focusSelf]="focusSelf()"
      (closed)="closed = closed + 1; open.set(false)"
    >
      <input class="first" />
      <div class="actions" gdDialogActions><button type="button">OK</button></div>
    </gd-dialog>
  `,
})
class HostComponent {
  readonly open = signal(false);
  readonly modal = signal(true);
  readonly focusSelf = signal(false);
  closed = 0;
}

describe('DialogComponent', () => {
  async function render() {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const host = fixture.componentInstance;
    const el = fixture.nativeElement as HTMLElement;
    const dialog = el.querySelector<HTMLDialogElement>('dialog.gd-dialog')!;
    const show = async () => {
      host.open.set(true);
      await fixture.whenStable();
    };
    return { fixture, host, el, dialog, show };
  }

  it('is a native dialog with a title bar, the content and the actions (DOM contract)', async () => {
    const { dialog } = await render();
    expect(dialog.dataset['dialog']).toBe('test');
    expect(Array.from(dialog.children).map((child) => child.className)).toEqual([
      'gd-dialog__header',
      'gd-dialog__body',
      'actions',
    ]);
    const title = dialog.querySelector('header.gd-dialog__header > h2.gd-dialog__title')!;
    expect(title.textContent).toBe('A title');
    expect(dialog.getAttribute('aria-labelledby')).toBe(title.id);
    expect(dialog.querySelector('header > button.gd-dialog__close')!.getAttribute('aria-label')).toBe('Close');
    expect(dialog.querySelector('.gd-dialog__body > input.first')).not.toBeNull();
  });

  it('opens and closes with `open`, without reporting a close by the player', async () => {
    const { fixture, host, dialog, show } = await render();
    expect(dialog.open).toBe(false);
    await show();
    expect(dialog.open).toBe(true);
    expect(dialog.style.getPropertyValue('--gd-dialog-layer')).not.toBe('');
    host.open.set(false);
    await fixture.whenStable();
    expect(dialog.open).toBe(false);
    expect(host.closed).toBe(0);
  });

  it('reports a close by the close button', async () => {
    const { fixture, host, dialog, show } = await render();
    await show();
    dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    await fixture.whenStable();
    expect(host.closed).toBe(1);
    expect(dialog.open).toBe(false);
  });

  it('closes a non-modal dialog on Escape inside it', async () => {
    const { fixture, host, dialog, show } = await render();
    host.modal.set(false);
    await show();
    dialog.querySelector('input')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await fixture.whenStable();
    expect(host.closed).toBe(1);
  });

  it("focuses the content's first control, or the dialog itself when asked", async () => {
    const { fixture, host, dialog, show } = await render();
    await show();
    expect(document.activeElement).toBe(dialog.querySelector('input.first'));
    host.open.set(false);
    host.focusSelf.set(true);
    await fixture.whenStable();
    await show();
    expect(document.activeElement).toBe(dialog);
    expect(dialog.getAttribute('tabindex')).toBe('-1');
  });

  it('moves with its title bar, and is placed afresh when opened again', async () => {
    const { fixture, host, dialog, show } = await render();
    await show();
    const header = dialog.querySelector<HTMLElement>('.gd-dialog__header')!;
    vi.spyOn(dialog, 'getBoundingClientRect').mockReturnValue(new DOMRect(100, 50, 300, 200));
    header.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, button: 0, clientX: 150, clientY: 60 }));
    header.dispatchEvent(new PointerEvent('pointermove', { bubbles: true, clientX: 170, clientY: 90 }));
    header.dispatchEvent(new PointerEvent('pointerup', { bubbles: true }));
    await fixture.whenStable();
    expect(dialog.hasAttribute('data-placed')).toBe(true);
    expect(dialog.style.getPropertyValue('--gd-dialog-left')).toBe('120px');
    expect(dialog.style.getPropertyValue('--gd-dialog-top')).toBe('80px');
    host.open.set(false);
    await fixture.whenStable();
    await show();
    expect(dialog.hasAttribute('data-placed')).toBe(false);
  });

  it('is pinned where it is when pressed on its own edge (so a resize grows it right and down)', async () => {
    const { fixture, dialog, show } = await render();
    await show();
    vi.spyOn(dialog, 'getBoundingClientRect').mockReturnValue(new DOMRect(10.4, 20.6, 300, 200));
    dialog.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true }));
    await fixture.whenStable();
    expect(dialog.style.getPropertyValue('--gd-dialog-left')).toBe('10px');
    expect(dialog.style.getPropertyValue('--gd-dialog-top')).toBe('21px');
  });
});
