import { TestBed } from '@angular/core/testing';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { fakeTableFlip, FakeTableFlip } from '../../../testing/table-flip';
import { AuthorFlipComponent } from './author-flip.component';

describe('AuthorFlipComponent', () => {
  let flip: FakeTableFlip;

  it("flips an author's table to the opponent's seat and back, saying the seat looked from; none where it can't", () => {
    flip = fakeTableFlip();
    TestBed.configureTestingModule({ providers: [{ provide: TableFlip, useValue: flip }] });
    const fixture = TestBed.createComponent(AuthorFlipComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const button = () => el.querySelector<HTMLButtonElement>('button[data-author="flip"]');
    expect(button()?.textContent?.trim()).toBe('Flip');
    expect(button()?.title).toBe("Look from Shadow's side");
    button()!.click();
    expect(flip.flip).toHaveBeenCalled();
    flip.seat.set('shadow');
    fixture.detectChanges();
    expect(button()?.textContent?.trim()).toBe('As Shadow');
    expect(button()?.hasAttribute('data-flipped')).toBe(true);
    flip.canFlip.set(false);
    fixture.detectChanges();
    expect(button()).toBeNull();
  });
});
