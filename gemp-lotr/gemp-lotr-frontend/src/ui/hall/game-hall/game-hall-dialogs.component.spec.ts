import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallDialogs, plainTextMarkup } from '../../../core/hall/hall-dialogs.service';
import { HallReadyOffers } from '../../../core/hall/ready/hall-ready-offers.service';
import { HallDeliveries } from '../../../core/hall/deliveries/hall-deliveries.service';
import { CardImageService } from '../../../core/cards/card-image.service';
import { READY_CHECK_TICK_MS } from '../../../core/hall/queues/hall-queues.service';
import { GameHallDialogsComponent } from './game-hall-dialogs.component';

describe('GameHallDialogsComponent', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

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
  const words = (el: Element | null) => el?.textContent?.replace(/\s+/g, ' ').trim();

  it('shows the ready check counting down; Ready confirms it and closes it', async () => {
    vi.useFakeTimers();
    const { fixture, el } = render();
    hall.connect();
    fixture.detectChanges();
    const dialog = () => el.querySelector('[data-dialog=ready-check]');
    expect(words(dialog()!.querySelector('[data-dialog-title]'))).toBe('Ready Check');
    expect(
      dialog()!
        .querySelector('.gd-game-hall-dialogs__ready-check')!
        .innerHTML.replace(/ _ng[\w-]+="[^"]*"/g, ''),
    ).toContain('Ready Check started for the <b>Casual FotR Live Draft</b> tournament.');
    expect(words(dialog()!.querySelector('.gd-game-hall-dialogs__ready-check'))).toContain(
      'Confirm you are present within 45 seconds.',
    );
    vi.advanceTimersByTime(READY_CHECK_TICK_MS * 3);
    fixture.detectChanges();
    expect(words(dialog()!.querySelector('.gd-game-hall-dialogs__ready-check'))).toContain('within 42 seconds.');

    dialog()!.querySelector<HTMLButtonElement>('[data-action=ready]')!.click();
    hall.request('/hall/queue/pm1/ready').flush('<result>OK</result>');
    await vi.advanceTimersByTimeAsync(0);
    fixture.detectChanges();
    expect(dialog()).toBeNull();
  });

  it('closes the ready check with OK and leaves the check going (nothing is sent)', () => {
    vi.useFakeTimers();
    const { fixture, el } = render();
    hall.connect();
    fixture.detectChanges();
    el.querySelector<HTMLButtonElement>('[data-dialog=ready-check] [data-action=ok]')!.click();
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=ready-check]')).toBeNull();
    hall.backend.expectNone((r) => r.url.endsWith('/ready'));
  });

  it('closes a ready prompt once its link is followed', () => {
    vi.useFakeTimers();
    const { fixture, el } = render();
    hall.navigation.blockPopups = true;
    TestBed.inject(HallReadyOffers).openOrOffer('draft:t2', 'draft', '/gemp-lotr/soloDraft.html?eventId=t2');
    fixture.detectChanges();
    const link = el.querySelector<HTMLAnchorElement>('[data-dialog=ready-offer] .gd-game-hall-dialogs__open')!;
    link.addEventListener('click', (event) => event.preventDefault());
    link.click();
    fixture.detectChanges();
    // it stays until the browser has followed the link
    expect(el.querySelector('[data-dialog=ready-offer]')).not.toBeNull();
    vi.advanceTimersByTime(0);
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=ready-offer]')).toBeNull();
  });

  it('shows new items, packs then cards, with a count past one; closing takes them away', () => {
    const { fixture, el } = render();
    const images = TestBed.inject(CardImageService);
    const deliveries = TestBed.inject(HallDeliveries);
    deliveries.newItems.set([
      {
        collectionName: 'My cards',
        packs: [{ blueprintId: '(S)FotR - Booster', count: 2 }],
        cards: [{ blueprintId: '1_1', count: 1 }],
      },
    ]);
    fixture.detectChanges();
    const dialog = el.querySelector('[data-dialog=new-items]')!;
    expect(words(dialog.querySelector('[data-dialog-title]'))).toBe('New items - My cards');
    const items = Array.from(dialog.querySelectorAll('.gd-game-hall-dialogs__item'));
    expect(
      items.map((i) => [
        i.querySelector('img')!.getAttribute('src'),
        words(i.querySelector('.gd-game-hall-dialogs__count')),
      ]),
    ).toEqual([
      [images.imageUrl('(S)FotR - Booster'), '2'],
      [images.imageUrl('1_1'), undefined],
    ]);
    dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    fixture.detectChanges();
    expect(deliveries.newItems()).toEqual([]);
    expect(el.querySelector('[data-dialog=new-items]')).toBeNull();
  });

  it('dismisses the announcement or snoozes it (Remind Me Later, or closing it)', () => {
    const { fixture, el } = render();
    const deliveries = TestBed.inject(HallDeliveries);
    const announce = () => {
      deliveries.announcement.set({ id: 1, title: 'Yuletide', content: [{ kind: 'text', text: 'News' }] });
      fixture.detectChanges();
    };
    const button = (action: string) =>
      el.querySelector<HTMLButtonElement>(`[data-dialog=announcement] [data-action=${action}]`)!;

    announce();
    expect([words(button('dismiss')), words(button('snooze'))]).toEqual(['Dismiss', 'Remind Me Later']);
    button('dismiss').click();
    hall.request('/delivery/announcements/dismiss').flush('');
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=announcement]')).toBeNull();

    announce();
    button('snooze').click();
    hall.request('/delivery/announcements/snooze').flush('');
    fixture.detectChanges();
    expect(el.querySelector('[data-dialog=announcement]')).toBeNull();

    announce();
    el.querySelector<HTMLButtonElement>('[data-dialog=announcement] .gd-dialog__close')!.click();
    hall.request('/delivery/announcements/snooze').flush('');
    expect(deliveries.announcement()).toBeNull();
  });

  it('shows the Race to Mount Doom moves one at a time: old site, arrow, new site, with their modifiers', () => {
    const { fixture, el } = render();
    const images = TestBed.inject(CardImageService);
    const deliveries = TestBed.inject(HallDeliveries);
    deliveries.leagueNotifications.set([
      {
        message: 'You have advanced!',
        old: { visual: '1_319', modifier: '4_1' },
        next: { visual: '1_320', modifier: null },
      },
      { message: 'Again', old: null, next: { visual: '1_321', modifier: '4_2' } },
    ]);
    fixture.detectChanges();
    const sites = () =>
      Array.from(el.querySelectorAll<HTMLElement>('[data-dialog=rtmd] .gd-game-hall-dialogs__site')).map((s) => [
        s.dataset['site'],
        ...Array.from(s.querySelectorAll('img')).map((i) => i.getAttribute('src')),
      ]);
    expect(words(el.querySelector('[data-dialog=rtmd] [data-dialog-title]'))).toBe('Race to Mount Doom');
    expect(words(el.querySelector('.gd-game-hall-dialogs__rtmd-message'))).toBe('You have advanced!');
    expect(sites()).toEqual([
      ['old', images.imageUrl('1_319'), images.imageUrl('4_1')],
      ['new', images.imageUrl('1_320')],
    ]);
    expect(el.querySelectorAll('.gd-game-hall-dialogs__arrow')).toHaveLength(1);

    el.querySelector<HTMLButtonElement>('[data-dialog=rtmd] .gd-dialog__close')!.click();
    fixture.detectChanges();
    expect(words(el.querySelector('.gd-game-hall-dialogs__rtmd-message'))).toBe('Again');
    expect(sites()).toEqual([['new', images.imageUrl('1_321'), images.imageUrl('4_2')]]);
    expect(el.querySelector('.gd-game-hall-dialogs__arrow')).toBeNull();
  });
  it('closes a hall message or a ready check from its title bar as well', () => {
    vi.useFakeTimers();
    const { fixture, el } = render();
    TestBed.inject(HallDialogs).showInfo('Prizes details', plainTextMarkup('No prizes'));
    hall.connect();
    fixture.detectChanges();
    for (const name of ['hall-info', 'ready-check']) {
      el.querySelector<HTMLButtonElement>(`[data-dialog=${name}] .gd-dialog__close`)!.click();
      fixture.detectChanges();
      expect(el.querySelector(`[data-dialog=${name}]`)).toBeNull();
    }
    hall.backend.expectNone((r) => r.url.endsWith('/ready'));
  });
});
