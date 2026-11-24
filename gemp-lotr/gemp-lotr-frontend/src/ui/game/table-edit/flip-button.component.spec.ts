import { TestBed } from '@angular/core/testing';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { fakeTableFlip, FakeTableFlip } from '../../../testing/table-flip';
import { FlipButtonComponent } from './flip-button.component';

describe('FlipButtonComponent', () => {
  let flip: FakeTableFlip;

  function render() {
    flip = fakeTableFlip();
    TestBed.configureTestingModule({ providers: [{ provide: TableFlip, useValue: flip }] });
    const fixture = TestBed.createComponent(FlipButtonComponent);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button[data-flip]')!;
    return { fixture, button };
  }

  it('flips the table, its title saying where to; marked while flipped; off while a call is on its way', () => {
    const { fixture, button } = render();
    expect(button.textContent?.trim()).toBe('Flip');
    expect(button.title).toBe("Look from Shadow's side");
    expect(button.hasAttribute('data-flipped')).toBe(false);
    button.click();
    expect(flip.flip).toHaveBeenCalled();
    flip.seat.set('shadow');
    flip.busy.set(true);
    fixture.detectChanges();
    expect(button.hasAttribute('data-flipped')).toBe(true);
    expect(button.title).toBe('Back to your side (Free Peoples)');
    expect(button.disabled).toBe(true);
  });
});
