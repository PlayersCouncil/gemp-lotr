import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PickerItem } from '../../../core/hall/play/player-picker';
import { Timers } from '../../../core/platform/timers';
import { PICKER_DELAY_MS, PlayerPickerComponent } from './player-picker.component';

const ITEMS: PickerItem[] = [
  { kind: 'player', name: 'bob', heading: 'In the hall' },
  { kind: 'player', name: 'carol', heading: 'In the hall' },
  { kind: 'player', name: 'erin', heading: 'Other players' },
];

@Component({
  imports: [PlayerPickerComponent],
  template: `<gd-player-picker
    fieldId="who"
    placeholder="Choose"
    [maxlength]="30"
    [suggest]="suggest"
    [(value)]="value"
    (picked)="picked.push($event)"
  />`,
})
class Host {
  readonly value = signal('');
  readonly picked: string[] = [];
  readonly asked: string[] = [];
  items: PickerItem[] = ITEMS;
  readonly suggest = (term: string) => {
    this.asked.push(term);
    return Promise.resolve(this.items);
  };
}

describe('PlayerPickerComponent', () => {
  let timers: { after: (ms: number, fn: () => void) => number; clear: () => void; now: () => number; run: () => void };

  beforeEach(() => {
    let pending: (() => void) | null = null;
    timers = {
      after: (_ms, fn) => {
        pending = fn;
        return 1;
      },
      clear: () => (pending = null),
      now: () => 0,
      run: () => {
        const fn = pending;
        pending = null;
        fn?.();
      },
    };
    TestBed.configureTestingModule({ providers: [{ provide: Timers, useValue: timers }] });
  });

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const input = element.querySelector('input') as HTMLInputElement;
    const settle = async () => {
      await Promise.resolve();
      await fixture.whenStable();
    };
    return { fixture, element, input, settle };
  }

  it('opens the whole list from ▾, with its headings, and takes a name clicked', async () => {
    const { fixture, element, input, settle } = await render();
    expect([input.id, input.placeholder, input.maxLength, input.getAttribute('role')]).toEqual([
      'who',
      'Choose',
      30,
      'combobox',
    ]);
    const toggle = element.querySelector('.gd-player-picker__toggle') as HTMLButtonElement;
    expect(toggle.getAttribute('aria-label')).toBe('Show the players in the hall');
    toggle.click();
    await settle();
    expect(fixture.componentInstance.asked).toEqual(['']);
    expect(input.getAttribute('aria-expanded')).toBe('true');
    const lines = Array.from(element.querySelectorAll('.gd-player-picker__menu li')).map((li) =>
      li.textContent?.trim(),
    );
    expect(lines).toEqual(['In the hall', 'bob', 'carol', 'Other players', 'erin']);
    (element.querySelector('[data-name="carol"]') as HTMLElement).click();
    await settle();
    expect(fixture.componentInstance.value()).toBe('carol');
    expect(fixture.componentInstance.picked).toEqual(['carol']);
    expect(element.querySelector('.gd-player-picker__menu')).toBeNull();
  });

  it('asks again after a pause in typing, and moves along the list with the keys', async () => {
    const { fixture, element, input, settle } = await render();
    input.value = 'er';
    input.dispatchEvent(new Event('input'));
    expect(fixture.componentInstance.value()).toBe('er');
    expect(fixture.componentInstance.asked).toEqual([]);
    timers.run();
    await settle();
    expect(fixture.componentInstance.asked).toEqual(['er']);
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowUp' }));
    await settle();
    expect(element.querySelector('[aria-selected="true"]')?.getAttribute('data-name')).toBe('bob');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    expect(fixture.componentInstance.picked).toEqual(['bob']);
    expect(PICKER_DELAY_MS).toBe(250);
  });

  it('shows notes as no choice, and closes on Escape', async () => {
    const { fixture, element, input, settle } = await render();
    fixture.componentInstance.items = [{ kind: 'note', text: 'Nobody else is in the hall.' }];
    input.click();
    await settle();
    expect(element.querySelector('.gd-player-picker__note')?.textContent?.trim()).toBe('Nobody else is in the hall.');
    expect(element.querySelector('[role="option"]')).toBeNull();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await settle();
    expect(input.getAttribute('aria-expanded')).toBe('false');
  });
});
