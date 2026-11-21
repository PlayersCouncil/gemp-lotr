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
    [maxLength]="30"
    [disabled]="disabled()"
    [suggest]="suggest"
    [(value)]="value"
    (picked)="picked.push($event)"
  />`,
})
class Host {
  readonly value = signal('');
  readonly disabled = signal(false);
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
    const chosen = () => element.querySelector('[aria-selected="true"]')?.getAttribute('data-name') ?? null;
    const press = async (key: string) => {
      input.dispatchEvent(new KeyboardEvent('keydown', { key }));
      await settle();
    };
    // the menu's next and previous: past either end back to the field
    await press('ArrowDown');
    expect(chosen()).toBe('bob');
    await press('ArrowUp');
    expect(chosen()).toBeNull();
    await press('ArrowUp');
    expect(chosen()).toBe('erin');
    await press('ArrowDown');
    expect(chosen()).toBeNull();
    await press('ArrowDown');
    await press('Enter');
    expect(fixture.componentInstance.picked).toEqual(['bob']);
    // Down on a field with a name in it does not open the list; on an empty one, or with Alt, it opens the whole list
    await press('ArrowDown');
    expect(input.getAttribute('aria-expanded')).toBe('false');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', altKey: true }));
    await settle();
    expect(fixture.componentInstance.asked).toEqual(['er', '']);
    expect(PICKER_DELAY_MS).toBe(250);
  });

  it('is off while disabled, and closes its list then', async () => {
    const { fixture, element, input, settle } = await render();
    (element.querySelector('.gd-player-picker__toggle') as HTMLButtonElement).click();
    await settle();
    fixture.componentInstance.disabled.set(true);
    await settle();
    expect(input.disabled).toBe(true);
    expect((element.querySelector('.gd-player-picker__toggle') as HTMLButtonElement).disabled).toBe(true);
    expect(element.querySelector('.gd-player-picker__menu')).toBeNull();
    expect(element.querySelector('gd-player-picker')?.getAttribute('data-disabled')).toBe('');
  });

  it('closes when the focus leaves it, and Escape goes no further', async () => {
    const { element, input, settle } = await render();
    input.click();
    await settle();
    const escape = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true });
    let bubbled = false;
    element.addEventListener('keydown', () => (bubbled = true));
    input.dispatchEvent(escape);
    await settle();
    expect(bubbled).toBe(false);
    input.click();
    await settle();
    input.dispatchEvent(new FocusEvent('focusout', { relatedTarget: null, bubbles: true }));
    await settle();
    expect(input.getAttribute('aria-expanded')).toBe('false');
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
