import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PcFormComponent, PcFormField } from './pc-form.component';

const FIELDS: PcFormField[] = [
  { name: 'login', label: 'Username', type: 'text', autocomplete: 'username', maxLength: 30 },
  { name: 'password', label: 'Password', type: 'password', autocomplete: 'current-password' },
];

@Component({
  imports: [PcFormComponent],
  template: `<gd-pc-form
    place="login"
    form="link"
    label="Link it"
    [fields]="fields"
    [values]="values()"
    [note]="note()"
    [error]="error()"
    submitLabel="Link"
    cancelLabel="Cancel"
    [busy]="busy()"
    (valueChange)="changes.push($event)"
    (submitted)="submits = submits + 1"
    (cancelled)="cancels = cancels + 1"
  />`,
})
class HostComponent {
  readonly fields = FIELDS;
  readonly values = signal<Record<string, string>>({ login: 'Sam' });
  readonly note = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly busy = signal(false);
  readonly changes: { name: string; value: string }[] = [];
  submits = 0;
  cancels = 0;
}

describe('PcFormComponent', () => {
  async function render() {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-pc-form') as HTMLElement;
    return { fixture, host: fixture.componentInstance, el };
  }

  it('draws its fields in a line, named by placeholders, and its buttons to their right (DOM contract)', async () => {
    const { el } = await render();
    expect(el.classList).toContain('gd-pc-form');
    expect(el.dataset['place']).toBe('login');
    const form = el.querySelector<HTMLFormElement>('form.gd-pc-form__form')!;
    expect([form.dataset['form'], form.getAttribute('aria-label')]).toEqual(['link', 'Link it']);
    const fields = Array.from(el.querySelectorAll<HTMLElement>('label.gd-pc-form__field'));
    // No visible label: the field's placeholder and aria-label name it
    expect(fields.map((f) => [f.dataset['field'], f.textContent!.trim()])).toEqual([
      ['login', ''],
      ['password', ''],
    ]);
    const line = el.querySelector('form > .gd-pc-form__line')!;
    expect(Array.from(line.children).map((child) => child.className)).toEqual([
      'gd-pc-form__field',
      'gd-pc-form__field',
      'gd-pc-form__actions',
    ]);
    const [login, password] = fields.map((f) => f.querySelector<HTMLInputElement>('input.gd-pc-form__input')!);
    expect([login.type, login.name, login.autocomplete, login.maxLength, login.value]).toEqual([
      'text',
      'login',
      'username',
      30,
      'Sam',
    ]);
    expect([login.placeholder, login.getAttribute('aria-label')]).toEqual(['Username', 'Username']);
    expect([password.placeholder, password.getAttribute('aria-label')]).toEqual(['Password', 'Password']);
    // The first field takes the focus
    expect(document.activeElement).toBe(login);
    expect(login.getAttribute('autocapitalize')).toBe('none');
    expect([password.type, password.value, password.hasAttribute('maxlength')]).toEqual(['password', '', false]);
    expect(password.hasAttribute('autocapitalize')).toBe(false);
    expect(
      Array.from(el.querySelectorAll('.gd-pc-form__actions .gd-button')).map((b) => [
        (b as HTMLElement).dataset['action'],
        b.textContent!.trim(),
      ]),
    ).toEqual([
      ['submit', 'Link'],
      ['cancel', 'Cancel'],
    ]);
    expect(el.querySelector('.gd-pc-form__error')).toBeNull();
    expect(el.querySelector('.gd-pc-form__note')).toBeNull();
  });

  it('says its note under the line, the refusal after it', async () => {
    const { fixture, host, el } = await render();
    host.note.set('Mind this.');
    await fixture.whenStable();
    const note = el.querySelector('.gd-pc-form__note')!;
    expect(note.textContent).toBe('Mind this.');
    expect(note.previousElementSibling!.classList).toContain('gd-pc-form__line');
    host.error.set('No.');
    await fixture.whenStable();
    expect(note.nextElementSibling!.classList).toContain('gd-pc-form__error');
  });

  it('cancels on Escape', async () => {
    const { host, el } = await render();
    el.querySelector('input')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(host.cancels).toBe(1);
  });

  it('passes on what is typed, the submit and Cancel; shows a refusal; waits while busy', async () => {
    const { fixture, host, el } = await render();
    const password = el.querySelector<HTMLInputElement>('input[name="password"]')!;
    password.value = 'secret';
    password.dispatchEvent(new Event('input'));
    expect(host.changes).toEqual([{ name: 'password', value: 'secret' }]);
    el.querySelector<HTMLFormElement>('form')!.requestSubmit();
    el.querySelector<HTMLButtonElement>('[data-action="cancel"]')!.click();
    expect([host.submits, host.cancels]).toEqual([1, 1]);

    host.error.set('Wrong password.');
    host.busy.set(true);
    await fixture.whenStable();
    const error = el.querySelector('.gd-pc-form__error')!;
    expect([error.textContent, error.getAttribute('role')]).toEqual(['Wrong password.', 'alert']);
    expect(Array.from(el.querySelectorAll('button')).every((b) => b.disabled)).toBe(true);
  });
});
