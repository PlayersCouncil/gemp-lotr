import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PickBoxComponent, PICKING_ATTRIBUTE } from './pick-box.component';
import { TablePick, PickHover } from '../table-edit/table-pick';

describe('PickBoxComponent', () => {
  it('draws the box and the label of what the pointer is on, and the crosshair while picking', async () => {
    const picking = signal(false);
    const hover = signal<PickHover | null>(null);
    TestBed.configureTestingModule({ providers: [{ provide: TablePick, useValue: { picking, hover } }] });
    const fixture = TestBed.createComponent(PickBoxComponent);
    const el = fixture.nativeElement as HTMLElement;
    const root = document.documentElement;
    const render = async () => {
      fixture.detectChanges();
      await fixture.whenStable();
    };
    await render();
    expect(el.querySelector('.gd-author-pickbox')).toBeNull();
    expect(root.hasAttribute(PICKING_ATTRIBUTE)).toBe(false);

    picking.set(true);
    hover.set({
      anchor: 'card:frodo',
      box: { left: 10.4, top: 20, width: 100, height: 140 },
      label: { left: 10, top: 0 },
    });
    await render();
    expect(root.hasAttribute(PICKING_ATTRIBUTE)).toBe(true);
    const box = el.querySelector<HTMLElement>('.gd-author-pickbox')!;
    expect(box.style.getPropertyValue('--gd-left')).toBe('10px');
    expect(box.style.getPropertyValue('--gd-height')).toBe('140px');
    const label = el.querySelector<HTMLElement>('.gd-author-picklabel')!;
    expect(label.textContent?.trim()).toBe('card:frodo');
    expect(label.style.getPropertyValue('--gd-top')).toBe('0px');

    picking.set(false);
    hover.set(null);
    await render();
    expect(root.hasAttribute(PICKING_ATTRIBUTE)).toBe(false);
    expect(el.querySelector('.gd-author-picklabel')).toBeNull();
    picking.set(true);
    await render();
    fixture.destroy();
    expect(root.hasAttribute(PICKING_ATTRIBUTE)).toBe(false);
  });
});
