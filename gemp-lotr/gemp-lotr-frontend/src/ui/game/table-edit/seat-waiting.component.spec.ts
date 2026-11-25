import { TestBed } from '@angular/core/testing';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { fakeTableFlip, FakeTableFlip } from '../../../testing/table-flip';
import { SeatWaitingComponent } from './seat-waiting.component';

describe('SeatWaitingComponent', () => {
  let flip: FakeTableFlip;

  it('says which seat out of sight is to choose, with Flip beside it; nothing otherwise', () => {
    flip = fakeTableFlip();
    TestBed.configureTestingModule({ providers: [{ provide: TableFlip, useValue: flip }] });
    const fixture = TestBed.createComponent(SeatWaitingComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.gd-seat-waiting')).toBeNull();
    flip.elsewhere.set('shadow');
    fixture.detectChanges();
    expect(el.querySelector('.gd-seat-waiting')?.getAttribute('data-seat')).toBe('shadow');
    expect(el.querySelector('.gd-seat-waiting__text')?.textContent).toBe('Shadow to choose');
    el.querySelector<HTMLButtonElement>('button[data-flip]')!.click();
    expect(flip.flip).toHaveBeenCalled();
    flip.elsewhere.set('freeps');
    fixture.detectChanges();
    expect(el.querySelector('.gd-seat-waiting__text')?.textContent).toBe('Free Peoples to choose');
  });
});
