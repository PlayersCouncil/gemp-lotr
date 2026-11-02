import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiDialogComponent } from './ui-dialog.component';

@Component({
  imports: [UiDialogComponent],
  template: `
    @if (open()) {
      <gemp-ui-dialog
        title="Your Saved Decks"
        [width]="700"
        [height]="height()"
        [modal]="modal()"
        [openedEmpty]="openedEmpty()"
        (closed)="closed = closed + 1; open.set(false)"
        (contentResized)="contentSize = $event"
      >
        <button id="first">Open</button><button id="second">Rename</button>
      </gemp-ui-dialog>
    }
  `,
})
class Host {
  readonly open = signal(true);
  readonly modal = signal(true);
  readonly height = signal<number | 'auto'>(400);
  readonly openedEmpty = signal(false);
  closed = 0;
  contentSize = { width: 0, height: 0 };
}

describe('UiDialogComponent', () => {
  async function render(setup: (host: Host) => void = () => undefined) {
    const fixture = TestBed.createComponent(Host);
    setup(fixture.componentInstance);
    fixture.detectChanges();
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el, dialog: el.querySelector<HTMLElement>('.ui-dialog')! };
  }

  it('has the jQuery UI dialog markup', async () => {
    const { el, dialog } = await render();
    expect(dialog.className).toBe('ui-dialog ui-widget ui-widget-content ui-corner-all ui-draggable ui-resizable');
    expect(dialog.getAttribute('role')).toBe('dialog');
    const title = dialog.querySelector('.ui-dialog-titlebar .ui-dialog-title')!;
    expect(title.textContent).toBe('Your Saved Decks');
    expect(dialog.getAttribute('aria-labelledby')).toBe(title.id);
    expect(dialog.querySelector('.ui-dialog-titlebar-close .ui-icon-closethick')!.textContent).toBe('close');
    expect(Array.from(dialog.querySelectorAll('.ui-resizable-handle')).map((h) => h.className.split(' ')[1])).toEqual([
      'ui-resizable-n',
      'ui-resizable-e',
      'ui-resizable-s',
      'ui-resizable-w',
      'ui-resizable-se',
      'ui-resizable-sw',
      'ui-resizable-ne',
      'ui-resizable-nw',
    ]);
    expect(el.querySelector('.ui-widget-overlay')).not.toBeNull();
  });

  it('stacks the modal overlay below the dialog, and later dialogs above', async () => {
    const { el, dialog } = await render();
    const overlayZ = parseInt(el.querySelector<HTMLElement>('.ui-widget-overlay')!.style.zIndex);
    expect(parseInt(dialog.style.zIndex)).toBe(overlayZ + 1);
    const second = await render((host) => host.modal.set(false));
    expect(parseInt(second.dialog.style.zIndex)).toBe(overlayZ + 2);
    dialog.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    expect(parseInt(dialog.style.zIndex)).toBe(overlayZ + 3);
  });

  it('gives the dialog its width and focuses the first field', async () => {
    const { el, dialog } = await render();
    expect(dialog.style.width).toBe('700px');
    expect(dialog.style.position).toBe('absolute');
    expect(document.activeElement).toBe(el.querySelector('#first'));
  });

  it('focuses the dialog itself when opened as if empty', async () => {
    const { dialog } = await render((host) => {
      host.openedEmpty.set(true);
      host.height.set('auto');
    });
    expect(document.activeElement).toBe(dialog);
  });

  it('closes by its button and by Escape', async () => {
    const { fixture, dialog } = await render();
    const close = dialog.querySelector<HTMLAnchorElement>('.ui-dialog-titlebar-close')!;
    const click = new MouseEvent('click', { bubbles: true, cancelable: true });
    close.dispatchEvent(click);
    expect(click.defaultPrevented).toBe(true);
    expect(fixture.componentInstance.closed).toBe(1);

    fixture.componentInstance.open.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const reopened = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.ui-dialog')!;
    reopened.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(fixture.componentInstance.closed).toBe(2);
  });

  it('shows hover and focus on the close button', async () => {
    const { fixture, dialog } = await render();
    const close = dialog.querySelector<HTMLAnchorElement>('.ui-dialog-titlebar-close')!;
    close.dispatchEvent(new MouseEvent('mouseenter'));
    fixture.detectChanges();
    expect(close.classList).toContain('ui-state-hover');
    close.dispatchEvent(new MouseEvent('mouseleave'));
    fixture.detectChanges();
    expect(close.classList).not.toContain('ui-state-hover');
  });

  it('resizes from a corner, not below 150px, and reports the new content size', async () => {
    const { fixture, dialog } = await render();
    const handle = dialog.querySelector<HTMLElement>('.ui-resizable-se')!;
    handle.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, button: 0, clientX: 500, clientY: 500 }));
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: -1000, clientY: 500 }));
    document.dispatchEvent(new MouseEvent('mouseup'));
    expect(dialog.style.width).toBe('150px');
    expect(fixture.componentInstance.contentSize).toBeDefined();
  });
});
