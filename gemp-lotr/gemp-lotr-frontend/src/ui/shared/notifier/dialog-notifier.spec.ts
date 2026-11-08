import { TestBed } from '@angular/core/testing';
import { Notifier } from '../../../core/notifier/notifier';
import { settle } from '../../../testing/fake-notifier';
import { DialogNotifier } from './dialog-notifier';
import { NotifierDialogComponent } from './notifier-dialog.component';

describe('DialogNotifier', () => {
  let notifier: DialogNotifier;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DialogNotifier, { provide: Notifier, useExisting: DialogNotifier }],
    });
    notifier = TestBed.inject(DialogNotifier);
  });

  it('asks one thing at a time, in order', async () => {
    const answers: unknown[] = [];
    void notifier.alert('first').then(() => answers.push('alert'));
    void notifier.confirm('second').then((ok) => answers.push(ok));
    void notifier.askText('third', 'default').then((text) => answers.push(text));
    expect(notifier.current()?.message).toBe('first');
    notifier.answer(true);
    expect(notifier.current()?.message).toBe('second');
    notifier.answer(false);
    expect(notifier.current()).toMatchObject({
      kind: 'prompt',
      message: 'third',
      defaultValue: 'default',
    });
    notifier.answer(true, 'typed');
    await settle();
    expect(answers).toEqual(['alert', false, 'typed']);
    expect(notifier.current()).toBeNull();
  });

  it('answers a cancelled prompt with null, and ignores answers when nothing is asked', async () => {
    const text = notifier.askText('name?');
    notifier.answer(false, 'ignored');
    expect(await text).toBeNull();
    expect(() => notifier.answer(true)).not.toThrow();
  });

  describe('gd-notifier', () => {
    async function render() {
      const fixture = TestBed.createComponent(NotifierDialogComponent);
      await fixture.whenStable();
      const el = fixture.nativeElement as HTMLElement;
      const dialog = el.querySelector<HTMLDialogElement>('dialog.gd-dialog')!;
      const stable = () => fixture.whenStable();
      return { el, dialog, stable };
    }

    it('shows nothing until asked', async () => {
      const { dialog } = await render();
      expect(dialog.open).toBe(false);
    });

    it('shows a message with OK (DOM contract)', async () => {
      const { dialog, stable } = await render();
      const done = notifier.alert('Line one\nline two');
      await stable();
      expect(dialog.open).toBe(true);
      expect(dialog.dataset['dialog']).toBe('alert');
      expect(dialog.querySelector('p.gd-notifier__message')!.textContent).toBe(
        'Line one\nline two',
      );
      expect(dialog.querySelector('.gd-notifier__input')).toBeNull();
      const buttons = dialog.querySelectorAll<HTMLButtonElement>(
        '.gd-dialog__actions > button.gd-button',
      );
      expect(Array.from(buttons).map((b) => b.dataset['action'])).toEqual(['ok']);
      expect(document.activeElement).toBe(buttons[0]);
      buttons[0].click();
      await done;
      await stable();
      expect(dialog.open).toBe(false);
    });

    it('asks a question with OK and Cancel', async () => {
      const { dialog, stable } = await render();
      const answer = notifier.confirm('Sure?');
      await stable();
      expect(dialog.dataset['dialog']).toBe('confirm');
      dialog.querySelector<HTMLButtonElement>('[data-action="cancel"]')!.click();
      expect(await answer).toBe(false);
      const again = notifier.confirm('Really?');
      await stable();
      dialog.querySelector<HTMLButtonElement>('[data-action="ok"]')!.click();
      expect(await again).toBe(true);
    });

    it('asks for text: Enter is OK, the close button (or Escape, which closes it) is Cancel', async () => {
      const { dialog, stable } = await render();
      const name = notifier.askText('Deck name:', 'My deck');
      await stable();
      expect(dialog.dataset['dialog']).toBe('prompt');
      const input = dialog.querySelector<HTMLInputElement>('input.gd-notifier__input')!;
      expect(input.value).toBe('My deck');
      expect(input.hasAttribute('autofocus')).toBe(true);
      input.value = 'Renamed';
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
      expect(await name).toBe('Renamed');
      const other = notifier.askText('Again:');
      await stable();
      dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
      expect(await other).toBeNull();
    });
  });
});
