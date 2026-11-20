import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HallPlayDialogComponent } from './hall-play-dialog.component';

@Component({
  imports: [HallPlayDialogComponent],
  template: `<gd-hall-play-dialog [open]="open()" (closed)="open.set(false)" />`,
})
class Host {
  readonly open = signal(false);
}

describe('HallPlayDialogComponent', () => {
  it('opens as the frame says, and closes with its button', async () => {
    const fixture = TestBed.createComponent(Host);
    document.body.appendChild(fixture.nativeElement);
    await fixture.whenStable();
    const dialog = fixture.nativeElement.querySelector('dialog[data-dialog="play"]') as HTMLDialogElement;
    expect(dialog.open).toBe(false);
    fixture.componentInstance.open.set(true);
    await fixture.whenStable();
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('.gd-dialog__title')?.textContent).toBe('Play');
    dialog.querySelector<HTMLButtonElement>('[data-action="close"]')?.click();
    await fixture.whenStable();
    expect(fixture.componentInstance.open()).toBe(false);
    fixture.nativeElement.remove();
  });
});
