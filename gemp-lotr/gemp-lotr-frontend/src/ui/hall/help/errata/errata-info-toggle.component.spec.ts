import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ErrataInfoToggleComponent } from './errata-info-toggle.component';

@Component({
  imports: [ErrataInfoToggleComponent],
  template: `<gd-errata-info-toggle [(open)]="open" controls="help" />`,
})
class Host {
  readonly open = signal(false);
}

describe('ErrataInfoToggleComponent', () => {
  it('toggles its text, saying so (TableFlow.bindInfoToggles)', () => {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect([button.textContent?.trim(), button.title, button.getAttribute('aria-controls')]).toEqual([
      'i',
      'What is this?',
      'help',
    ]);
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    fixture.detectChanges();
    expect(fixture.componentInstance.open()).toBe(true);
    expect(button.getAttribute('aria-expanded')).toBe('true');
  });
});
