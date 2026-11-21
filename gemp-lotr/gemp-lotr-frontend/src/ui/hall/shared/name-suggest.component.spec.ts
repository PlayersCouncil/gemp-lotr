import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { NameSuggestComponent, NameSuggestion, SUGGEST_DELAY_MS } from './name-suggest.component';

@Component({
  imports: [NameSuggestComponent],
  template: `<gd-name-suggest
    [value]="value()"
    [suggest]="suggest"
    [disabled]="disabled()"
    toggleLabel="Show the players in the hall"
    (valueChange)="value.set($event)"
  />`,
})
class HostComponent {
  readonly value = signal('');
  readonly disabled = signal(false);
  readonly asked: string[] = [];
  readonly suggest = (term: string): Promise<NameSuggestion[]> => {
    this.asked.push(term);
    return Promise.resolve(
      term === ''
        ? [
            { name: 'bob', heading: 'In the hall' },
            { name: 'zed', heading: 'Other players' },
          ]
        : [{ note: `No player's name starts with "${term}".` }],
    );
  };
}

describe('NameSuggestComponent', () => {
  async function render() {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const settle = async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
      fixture.detectChanges();
      await fixture.whenStable();
    };
    return { fixture, el, host: fixture.componentInstance, settle };
  }

  it('opens the whole list from its button, under headings, and picks a name', async () => {
    const { el, host, settle } = await render();
    el.querySelector<HTMLButtonElement>('.gd-name-suggest__toggle')!.click();
    await settle();
    expect(Array.from(el.querySelectorAll('li')).map((li) => [li.className, li.textContent?.trim()])).toEqual([
      ['gd-name-suggest__heading', 'In the hall'],
      ['gd-name-suggest__option', 'bob'],
      ['gd-name-suggest__heading', 'Other players'],
      ['gd-name-suggest__option', 'zed'],
    ]);
    el.querySelectorAll<HTMLElement>('.gd-name-suggest__option')[1]!.click();
    await settle();
    expect(host.value()).toBe('zed');
    expect(el.querySelector('.gd-name-suggest__list')).toBeNull();
  });

  it('reports what is typed, asks for suggestions after a pause, and takes the keys', async () => {
    vi.useFakeTimers();
    try {
      const { fixture, el, host } = await render();
      const input = el.querySelector<HTMLInputElement>('input')!;
      input.value = 'qq';
      input.dispatchEvent(new Event('input'));
      expect(host.value()).toBe('qq');
      expect(host.asked).toEqual([]);
      await vi.advanceTimersByTimeAsync(SUGGEST_DELAY_MS);
      fixture.detectChanges();
      expect(host.asked).toEqual(['qq']);
      expect(el.querySelector('.gd-name-suggest__note')?.textContent).toContain('No player');
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
      fixture.detectChanges();
      expect(el.querySelector('.gd-name-suggest__list')).toBeNull();
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
      await vi.advanceTimersByTimeAsync(0);
      fixture.detectChanges();
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
      fixture.detectChanges();
      expect(el.querySelector('[aria-selected=true]')?.textContent?.trim()).toBe('bob');
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      fixture.detectChanges();
      expect(host.value()).toBe('bob');
    } finally {
      vi.useRealTimers();
    }
  });

  it('is off while disabled', async () => {
    const { fixture, el, host } = await render();
    host.disabled.set(true);
    fixture.detectChanges();
    expect(el.querySelector('input')?.disabled).toBe(true);
    expect(el.querySelector<HTMLButtonElement>('.gd-name-suggest__toggle')?.disabled).toBe(true);
  });
});
