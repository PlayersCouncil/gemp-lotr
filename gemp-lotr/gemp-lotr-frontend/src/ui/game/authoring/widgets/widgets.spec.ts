import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Notifier } from '../../../../core/notifier/notifier';
import { FakeNotifier, provideFakeNotifier } from '../../../../testing/fake-notifier';
import { AuthorButtonDirective } from './author-button.directive';
import { AuthorCheckComponent } from './author-check.component';
import { askAuthor } from './author-confirm';
import { AuthorFieldComponent } from './author-field.component';
import { AuthorOption, AuthorSelectComponent } from './author-select.component';
import { AuthorSendDirective, TYPING_SEND_MS } from './author-send.directive';
import { HjsonAreaDirective } from './hjson-area.directive';

@Component({
  imports: [
    AuthorButtonDirective,
    AuthorCheckComponent,
    AuthorFieldComponent,
    AuthorSelectComponent,
    AuthorSendDirective,
    HjsonAreaDirective,
  ],
  template: `
    <button gdAuthorButton id="plain" (click)="pressed.set(pressed() + 1)">Plain</button>
    <button gdAuthorButton="danger" id="danger">Danger</button>
    <gd-author-field label="When" hint="What it waits for">
      <input id="when" gdAuthorSend (sent)="sent.push(['when', $event])" />
    </gd-author-field>
    <textarea id="text" gdAuthorSend="typing" (sent)="sent.push(['text', $event])"></textarea>
    <gd-author-check label="once" hint="Just once" [checked]="checked()" (changed)="checked.set($event)" />
    <gd-author-select label="Side" [options]="options" [value]="side()" (changed)="side.set($event)" />
    <textarea id="code" rows="2" gdHjsonArea [max]="4" [text]="code()"></textarea>
    <textarea id="always" gdHjsonArea="always"></textarea>
  `,
})
class HostComponent {
  readonly pressed = signal(0);
  readonly sent: [string, string][] = [];
  readonly checked = signal(true);
  readonly side = signal<string | null>('left');
  readonly code = signal('a');
  readonly options: readonly AuthorOption[] = [['', 'wherever fits best'], 'left', ['right', 'Right']];
  readonly send = viewChild.required(AuthorSendDirective);
}

describe('the authoring panel widgets', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let el: HTMLElement;

  beforeEach(() => {
    vi.useFakeTimers();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    el = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  afterEach(() => vi.useRealTimers());

  const type = (selector: string, value: string) => {
    const field = el.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    return field;
  };

  it('draws a button that never submits, with its tone', () => {
    const plain = el.querySelector<HTMLButtonElement>('#plain')!;
    expect(plain.classList.contains('gd-author-button')).toBe(true);
    expect(plain.type).toBe('button');
    expect(plain.hasAttribute('data-tone')).toBe(false);
    expect(el.querySelector('#danger')?.getAttribute('data-tone')).toBe('danger');
    plain.click();
    expect(host.pressed()).toBe(1);
  });

  it('labels a control, its explanation for a tooltip', () => {
    const label = el.querySelector('label.gd-author-field')!;
    expect(label.querySelector('.gd-author-field__label')?.textContent).toBe('When');
    expect(label.querySelector('.gd-author-field__label')?.getAttribute('title')).toBe('What it waits for');
    expect(label.querySelector('.gd-author-field__control #when')).not.toBeNull();
  });

  it('ticks a box with its words, and says each change', () => {
    const box = el.querySelector<HTMLInputElement>('.gd-author-check input')!;
    expect(box.checked).toBe(true);
    expect(el.querySelector('.gd-author-check')?.getAttribute('title')).toBe('Just once');
    box.checked = false;
    box.dispatchEvent(new Event('change'));
    expect(host.checked()).toBe(false);
  });

  it('chooses from a list of options, values alone or with their words', () => {
    const select = el.querySelector<HTMLSelectElement>('select.gd-author-input')!;
    expect([...select.options].map((option) => [option.value, option.text])).toEqual([
      ['', 'wherever fits best'],
      ['left', 'left'],
      ['right', 'Right'],
    ]);
    expect(select.value).toBe('left');
    expect(select.getAttribute('aria-label')).toBe('Side');
    select.value = 'right';
    select.dispatchEvent(new Event('change'));
    expect(host.side()).toBe('right');
  });

  it('sends a one-line field when it is left, or on Enter, and the same value once', () => {
    const field = type('#when', 'freeps');
    vi.advanceTimersByTime(TYPING_SEND_MS * 2);
    expect(host.sent).toEqual([]);
    expect(host.send().pending()).toBe(true);
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(host.sent).toEqual([['when', 'freeps']]);
    expect(host.send().pending()).toBe(false);
    field.dispatchEvent(new Event('change'));
    expect(host.sent).toHaveLength(1);
    type('#when', 'shadow').dispatchEvent(new Event('change'));
    expect(host.sent).toEqual([
      ['when', 'freeps'],
      ['when', 'shadow'],
    ]);
  });

  it('sends a field that sends as it is typed in once typing stops, and at once when it is left', () => {
    const field = type('#text', 'Hel');
    vi.advanceTimersByTime(TYPING_SEND_MS - 1);
    type('#text', 'Hello');
    vi.advanceTimersByTime(TYPING_SEND_MS - 1);
    expect(host.sent).toEqual([]);
    vi.advanceTimersByTime(1);
    expect(host.sent).toEqual([['text', 'Hello']]);
    type('#text', 'Hello there');
    field.dispatchEvent(new Event('change'));
    vi.advanceTimersByTime(TYPING_SEND_MS);
    expect(host.sent).toEqual([
      ['text', 'Hello'],
      ['text', 'Hello there'],
    ]);
    // (Enter in a text area is a new line, not a send)
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(host.sent).toHaveLength(2);
    type('#text', 'gone');
    fixture.destroy();
    vi.advanceTimersByTime(TYPING_SEND_MS);
    expect(host.sent).toHaveLength(2);
  });

  it('grows an HJSON field with its lines, from its own rows to the most', () => {
    const code = el.querySelector<HTMLTextAreaElement>('#code')!;
    expect(code.classList.contains('gd-author-code')).toBe(true);
    expect(code.rows).toBe(2);
    host.code.set('a\nb\nc');
    code.value = 'a\nb\nc';
    fixture.detectChanges();
    TestBed.tick();
    expect(code.rows).toBe(3);
    type('#code', 'a\nb\nc\nd\ne\nf');
    expect(code.rows).toBe(4);
    type('#code', '');
    expect(code.rows).toBe(2);
  });

  it('types a tab with Tab: in a field of several lines, or always where asked', () => {
    const press = (field: HTMLTextAreaElement, init: KeyboardEventInit = {}) => {
      const event = new KeyboardEvent('keydown', { key: 'Tab', cancelable: true, ...init });
      field.dispatchEvent(event);
      return event.defaultPrevented;
    };
    const code = type('#code', 'one line') as HTMLTextAreaElement;
    expect(press(code)).toBe(false);
    code.value = '{\nx}';
    code.selectionStart = code.selectionEnd = 2;
    const heard = vi.fn();
    code.addEventListener('input', heard);
    expect(press(code)).toBe(true);
    expect(code.value).toBe('{\n\tx}');
    expect(code.selectionStart).toBe(3);
    expect(heard).toHaveBeenCalled();
    expect(press(code, { shiftKey: true })).toBe(false);
    const always = el.querySelector<HTMLTextAreaElement>('#always')!;
    expect(press(always)).toBe(true);
    expect(always.value).toBe('\t');
  });
});

describe('askAuthor', () => {
  it("asks in the page's dialog, OK saying what it does", async () => {
    TestBed.configureTestingModule({ providers: provideFakeNotifier() });
    const notifier = TestBed.inject(Notifier) as FakeNotifier;
    notifier.answers.push(true);
    expect(await askAuthor(notifier, 'Delete step 3?', 'Delete')).toBe(true);
    expect(notifier.dialogs).toEqual(['confirm: Delete step 3?']);
    expect(notifier.words).toEqual([{ title: 'Please confirm', cancel: 'Cancel', ok: 'Delete' }]);
    expect(await askAuthor(notifier, 'Close it?', 'Close', { title: 'Close the draft' })).toBe(false);
    expect(notifier.words[1]).toEqual({ title: 'Close the draft', cancel: 'Cancel', ok: 'Close' });
  });
});
