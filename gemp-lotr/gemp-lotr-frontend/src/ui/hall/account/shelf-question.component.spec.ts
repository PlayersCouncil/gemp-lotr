import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ShelfQuestion } from '../../../core/hall/account/my-scenarios.service';
import { ShelfQuestionComponent } from './shelf-question.component';

@Component({
  imports: [ShelfQuestionComponent],
  template: `<gd-shelf-question [question]="question()" (answered)="answers.push($event)" />`,
})
class Host {
  readonly question = signal<ShelfQuestion | null>(null);
  readonly answers: boolean[] = [];
}

describe('ShelfQuestionComponent', () => {
  async function render(question: ShelfQuestion) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.question.set(question);
    await fixture.whenStable();
    return { fixture, box: fixture.nativeElement.querySelector('dialog') as HTMLDialogElement };
  }

  it('asks a question in the shelf’s box, yes first and drawn as dangerous', async () => {
    const { fixture, box } = await render({
      kind: 'confirm',
      title: 'Delete “x”?',
      lines: ['Are you sure you want to delete “x”?'],
      yes: 'Delete',
      no: 'Cancel',
      danger: true,
    });
    expect(box.hasAttribute('open')).toBe(true);
    expect(box.dataset['kind']).toBe('confirm');
    expect(box.querySelector('h2')?.textContent).toBe('Delete “x”?');
    const buttons = Array.from(box.querySelectorAll('button'));
    expect(buttons.map((b) => [b.textContent?.trim(), b.dataset['action'], b.hasAttribute('data-danger')])).toEqual([
      ['Delete', 'yes', true],
      ['Cancel', 'no', false],
    ]);
    buttons[0]?.click();
    buttons[1]?.click();
    box.dispatchEvent(new Event('cancel', { cancelable: true }));
    expect(fixture.componentInstance.answers).toEqual([true, false, false]);
    fixture.componentInstance.question.set(null);
    await fixture.whenStable();
    expect(box.hasAttribute('open')).toBe(false);
  });

  it('says what to check before submitting, in the authoring panel’s words', async () => {
    const { fixture, box } = await render({ kind: 'submit', title: null });
    expect(box.querySelector('h2')?.textContent).toBe('Submit “this scenario” for review?');
    expect(box.querySelectorAll('.gd-shelf-question__guidelines li')).toHaveLength(4);
    expect(box.querySelectorAll('.gd-shelf-question__consequences li')).toHaveLength(4);
    expect(box.querySelector('.gd-shelf-question__ask')?.textContent).toBe('Are you sure you want to submit it?');
    (box.querySelector('[data-action=yes]') as HTMLButtonElement).click();
    expect(fixture.componentInstance.answers).toEqual([true]);
    expect((box.querySelector('[data-action=yes]') as HTMLElement).textContent?.trim()).toBe('Submit for review');
  });

  it('answers no to Cancel in the submit box, and to a press on the shade but not inside the box', async () => {
    const { fixture, box } = await render({ kind: 'submit', title: 'Mine' });
    (box.querySelector('[data-action=no]') as HTMLButtonElement).click();
    vi.spyOn(box, 'getBoundingClientRect').mockReturnValue(new DOMRect(100, 100, 400, 300));
    const press = (x: number, y: number) =>
      box.dispatchEvent(new MouseEvent('pointerdown', { clientX: x, clientY: y, bubbles: true }));
    press(200, 200);
    press(20, 20);
    (box.querySelector('h2') as HTMLElement).dispatchEvent(
      new MouseEvent('pointerdown', { clientX: 20, clientY: 20, bubbles: true }),
    );
    expect(fixture.componentInstance.answers).toEqual([false, false]);
  });
});
