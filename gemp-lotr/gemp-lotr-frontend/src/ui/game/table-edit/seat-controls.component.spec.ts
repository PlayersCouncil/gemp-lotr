import { TestBed } from '@angular/core/testing';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { fakeTableFlip, FakeTableFlip } from '../../../testing/table-flip';
import { SeatControlsComponent } from './seat-controls.component';

describe('SeatControlsComponent', () => {
  let flip: FakeTableFlip;

  function render() {
    flip = fakeTableFlip();
    TestBed.configureTestingModule({ providers: [{ provide: TableFlip, useValue: flip }] });
    const fixture = TestBed.createComponent(SeatControlsComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const select = () => el.querySelector<HTMLSelectElement>('select[data-seat-control="other"]');
    return { fixture, el, select };
  }

  it('offers who plays the other seat, labelled by that seat, each choice a word with a sentence as its title', () => {
    const { el, select } = render();
    expect(el.querySelector('.gd-seat-controls__other')?.textContent?.trim()).toMatch(/^Shadow:/);
    const options = [...select()!.options];
    expect(options.map((option) => [option.value, option.textContent?.trim()])).toEqual([
      ['script', 'Script'],
      ['bot', 'Bot'],
      ['person', 'Me'],
    ]);
    expect(options.every((option) => option.title.length > 20)).toBe(true);
    expect(select()!.value).toBe('script');
    select()!.value = 'person';
    select()!.dispatchEvent(new Event('change'));
    expect(flip.setOtherSeat).toHaveBeenCalledWith('person');
    expect(el.querySelector('gd-flip-button')).not.toBeNull();
    expect(el.querySelector('.gd-seat-controls__looking')).toBeNull();
  });

  it('says the seat looked from while flipped; the choice follows the server; a refusal under it', () => {
    const { fixture, el, select } = render();
    flip.seat.set('shadow');
    flip.otherSeat.set('person');
    flip.problem.set('This server has no bot to play the other seat.');
    fixture.detectChanges();
    expect(el.dataset['flipped']).toBe('');
    expect(el.querySelector('.gd-seat-controls__looking')?.textContent?.trim()).toBe('As Shadow');
    expect(select()!.value).toBe('person');
    expect(el.querySelector('[role=alert]')?.textContent).toContain('no bot');
    flip.busy.set(true);
    fixture.detectChanges();
    expect(el.dataset['busy']).toBe('');
    expect(select()!.disabled).toBe(true);
  });

  it('offers no choice where there is none (the game over), nor Flip where the table cannot be flipped', () => {
    const { fixture, el, select } = render();
    flip.otherSeats.set([]);
    flip.canFlip.set(false);
    fixture.detectChanges();
    expect(select()).toBeNull();
    expect(el.querySelector('gd-flip-button')).toBeNull();
  });

  it('names the seat the person does not sit in, whichever it is', () => {
    const { fixture, el } = render();
    flip.humanSeat.set('shadow');
    fixture.detectChanges();
    expect(el.querySelector('.gd-seat-controls__other')?.textContent?.trim()).toMatch(/^Free Peoples:/);
  });
});
