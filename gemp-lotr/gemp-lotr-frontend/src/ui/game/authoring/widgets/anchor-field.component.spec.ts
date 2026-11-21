import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TablePicker } from '../table-picker.service';
import { AnchorFieldComponent, AnchorPicked } from './anchor-field.component';

@Component({
  imports: [AnchorFieldComponent],
  template: `
    @if (shown()) {
      <gd-author-anchor
        [value]="value()"
        label="Points at"
        hint="What the bubble points at"
        placeholder="card:alias"
        [many]="many()"
        [addOnly]="addOnly()"
        (typed)="typed.push($event)"
        (committed)="committed.push($event)"
        (picked)="picked.push($event)"
      />
    }
  `,
})
class HostComponent {
  readonly shown = signal(true);
  readonly value = signal('');
  readonly many = signal(false);
  readonly addOnly = signal(false);
  readonly typed: string[] = [];
  readonly committed: string[] = [];
  readonly picked: AnchorPicked[] = [];
}

describe('AnchorFieldComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let el: HTMLElement;
  let answer: (anchor: string | null) => void;
  const picker = {
    pick: vi.fn(() => new Promise<string | null>((resolve) => (answer = resolve))),
    cancel: vi.fn(),
    fightingSide: vi.fn<(anchor: string) => 'freeps' | 'shadow' | null>(() => null),
  };

  beforeEach(async () => {
    picker.pick.mockClear();
    picker.cancel.mockClear();
    picker.fightingSide.mockReset().mockReturnValue(null);
    TestBed.configureTestingModule({ providers: [{ provide: TablePicker, useValue: picker }] });
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    el = fixture.nativeElement as HTMLElement;
    await render();
  });

  async function render() {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  const input = () => el.querySelector<HTMLInputElement>('input.gd-author-anchor__input')!;
  const button = (pick: string) => el.querySelector<HTMLButtonElement>(`button[data-pick="${pick}"]`);
  const part = () => el.querySelector<HTMLSelectElement>('select.gd-author-anchor__part');

  async function pick(which: string, anchor: string | null) {
    button(which)!.click();
    await render();
    answer(anchor);
    await Promise.resolve();
    await render();
  }

  function type(text: string) {
    input().value = text;
    input().dispatchEvent(new Event('input'));
  }

  it('shows its value, label and hint, and Pick alone for one anchor', async () => {
    host.value.set('zone:minions');
    await render();
    expect(input().value).toBe('zone:minions');
    expect(input().placeholder).toBe('card:alias');
    expect(el.querySelector('.gd-author-anchor__label')?.textContent?.trim()).toBe('Points at');
    expect(el.querySelector('.gd-author-anchor__label')?.getAttribute('title')).toBe('What the bubble points at');
    expect(button('replace')).not.toBeNull();
    expect(button('add')).toBeNull();
    expect(part()).toBeNull();
  });

  it('says what is typed as it is, and commits it when left or on Enter, once', async () => {
    type('card:fro');
    type('card:frodo');
    expect(host.typed).toEqual(['card:fro', 'card:frodo']);
    expect(host.committed).toEqual([]);
    input().dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    input().dispatchEvent(new Event('change'));
    expect(host.committed).toEqual(['card:frodo']);
    // (left without typing: nothing new)
    input().dispatchEvent(new Event('change'));
    expect(host.committed).toEqual(['card:frodo']);
  });

  it('keeps the caret when given the value it shows, and takes a new one', async () => {
    document.body.appendChild(el);
    type('card:frodo');
    input().focus();
    input().setSelectionRange(3, 3);
    host.value.set('card:frodo');
    await render();
    expect(document.activeElement).toBe(input());
    expect(input().selectionStart).toBe(3);
    host.value.set('ui:phase');
    await render();
    expect(input().value).toBe('ui:phase');
    el.remove();
  });

  it('puts what is picked in its place, and says so', async () => {
    host.value.set('zone:minions');
    await render();
    await pick('replace', 'card:frodo');
    expect(picker.pick).toHaveBeenCalledTimes(1);
    expect(input().value).toBe('card:frodo');
    expect(host.committed).toEqual(['card:frodo']);
    expect(host.picked).toEqual([{ anchor: 'card:frodo', replacing: null }]);
    expect(picker.fightingSide).toHaveBeenCalledWith('card:frodo');
    expect(el.querySelector('.gd-author-anchor__either')).toBeNull();
  });

  it('shows it is picking while its own pick is under way; nothing picked changes nothing', async () => {
    button('replace')!.click();
    await render();
    expect(el.querySelector('gd-author-anchor')?.hasAttribute('data-picking')).toBe(true);
    expect(button('replace')?.getAttribute('aria-pressed')).toBe('true');
    answer(null);
    await Promise.resolve();
    await render();
    expect(el.querySelector('gd-author-anchor')?.hasAttribute('data-picking')).toBe(false);
    expect(host.committed).toEqual([]);
    expect(host.picked).toEqual([]);
  });

  it('adds to a list with + Pick, once each', async () => {
    host.many.set(true);
    host.value.set('card:frodo');
    await render();
    await pick('add', 'ui:phase');
    expect(input().value).toBe('card:frodo, ui:phase');
    await pick('add', 'ui:phase');
    expect(host.committed).toEqual(['card:frodo, ui:phase', 'card:frodo, ui:phase']);
    expect(host.picked).toEqual([]);
    expect(button('add')?.getAttribute('aria-pressed')).toBe('false');
  });

  it('offers only + Pick, and no parts, when only added to', async () => {
    host.many.set(true);
    host.addOnly.set(true);
    host.value.set('card:frodo');
    await render();
    expect(button('replace')).toBeNull();
    expect(button('add')?.textContent?.trim()).toBe('+ Pick');
    expect(part()).toBeNull();
  });

  it("offers a card's parts while it holds one card", async () => {
    host.value.set('card:frodo:vitality');
    await render();
    expect(part()?.value).toBe('vitality');
    expect(part()?.options[0]?.textContent).toBe('the whole card');
    part()!.value = 'strength';
    part()!.dispatchEvent(new Event('change'));
    await render();
    expect(input().value).toBe('card:frodo:strength');
    part()!.value = '';
    part()!.dispatchEvent(new Event('change'));
    await render();
    expect(host.committed).toEqual(['card:frodo:strength', 'card:frodo']);
    type('card:frodo, ui:phase');
    await render();
    expect(part()).toBeNull();
  });

  it('offers whichever card fights on that side, for a card picked in the skirmish', async () => {
    picker.fightingSide.mockReturnValue('shadow');
    await pick('replace', 'card:orc');
    const either = el.querySelector('.gd-author-anchor__either');
    expect(either?.textContent).toContain('orc is a minion in this skirmish.');
    expect(button('either')?.textContent?.trim()).toBe('Whichever minion is fighting');
    expect(button('either')?.title).toContain('ui:skirmish:shadow');
    button('either')!.click();
    await render();
    expect(input().value).toBe('ui:skirmish:shadow');
    expect(host.committed).toEqual(['card:orc', 'ui:skirmish:shadow']);
    expect(host.picked).toEqual([
      { anchor: 'card:orc', replacing: null },
      { anchor: 'ui:skirmish:shadow', replacing: 'card:orc' },
    ]);
    expect(el.querySelector('.gd-author-anchor__either')).toBeNull();

    picker.fightingSide.mockReturnValue('freeps');
    await pick('replace', 'card:frodo');
    expect(el.querySelector('.gd-author-anchor__either')?.textContent).toContain('frodo is the character');
    expect(button('either')?.textContent?.trim()).toBe('Whichever character is fighting');
    // Typing puts the offer away
    type('card:sam');
    await render();
    expect(el.querySelector('.gd-author-anchor__either')).toBeNull();
  });

  it('gives up its pick when it goes, and says nothing after', async () => {
    button('replace')!.click();
    await render();
    host.shown.set(false);
    await render();
    expect(picker.cancel).toHaveBeenCalledTimes(1);
    answer('card:frodo');
    await Promise.resolve();
    expect(host.committed).toEqual([]);
  });

  it('leaves a pick that is not its own alone when it goes', async () => {
    host.shown.set(false);
    await render();
    expect(picker.cancel).not.toHaveBeenCalled();
  });
});
