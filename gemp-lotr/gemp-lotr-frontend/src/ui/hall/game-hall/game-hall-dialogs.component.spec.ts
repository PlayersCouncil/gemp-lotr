import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallDialogs, plainTextMarkup } from '../../../core/hall/hall-dialogs.service';
import { HallReadyOffers } from '../../../core/hall/ready/hall-ready-offers.service';
import { HallDeliveries } from '../../../core/hall/deliveries/hall-deliveries.service';
import { GameHallDialogsComponent } from './game-hall-dialogs.component';

describe('GameHallDialogsComponent', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  function render() {
    const fixture = TestBed.createComponent(GameHallDialogsComponent);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('shows the hall messages with OK, and information dialogs without', () => {
    const { fixture, el } = render();
    const dialogs = TestBed.inject(HallDialogs);
    dialogs.showText('Error', 'Queue is full');
    dialogs.showInfo('Prizes details', plainTextMarkup('No prizes'));
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=hall-message]')?.textContent).toContain('Queue is full');
    expect(el.querySelector('[data-dialog=hall-info] [data-action=ok]')).toBeNull();
    el.querySelector<HTMLButtonElement>('[data-dialog=hall-message] [data-action=ok]')!.click();
    fixture.detectChanges();
    expect(dialogs.open().map((d) => d.title)).toEqual(['Prizes details']);
  });

  it('offers a blocked game behind a link, with no close button', () => {
    const { fixture, el } = render();
    hall.navigation.blockPopups = true;
    TestBed.inject(HallReadyOffers).openOrOffer('game:9', 'game', '/gemp-lotr/ng/game?gameId=9');
    fixture.detectChanges();
    const dialog = el.querySelector('[data-dialog=ready-offer]')!;
    expect(dialog.querySelector('.gd-dialog__close')).toBeNull();
    const link = dialog.querySelector<HTMLAnchorElement>('.gd-game-hall-dialogs__open')!;
    expect([link.textContent?.trim(), link.getAttribute('href'), link.target]).toEqual([
      'Open game',
      '/gemp-lotr/ng/game?gameId=9',
      '_blank',
    ]);
  });

  it('shows an announcement; a link into the hall dismisses it and goes there', () => {
    const { fixture, el } = render();
    const deliveries = TestBed.inject(HallDeliveries);
    deliveries.announcement.set({
      id: 1,
      title: 'Yuletide',
      content: [{ kind: 'hall-link', hash: '#patch-notes/yuletide', children: [{ kind: 'text', text: 'Read' }] }],
    });
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=announcement] [data-dialog-title]')?.textContent).toBe(
      'ANNOUNCEMENT - Yuletide',
    );
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    el.querySelector<HTMLAnchorElement>('.gd-markup__hall-link')!.click();
    hall.request('/delivery/announcements/dismiss').flush('');
    expect(deliveries.announcement()).toBeNull();
    expect(navigate).toHaveBeenCalled();
  });
});
