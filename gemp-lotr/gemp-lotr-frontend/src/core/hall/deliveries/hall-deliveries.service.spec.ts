import { TestBed } from '@angular/core/testing';
import { DeliveryAnnouncements } from '../../api/delivery-announcements';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallDeliveries, parseLeagueNotification } from './hall-deliveries.service';

describe('HallDeliveries', () => {
  let hall: HallTestBed;
  let deliveries: HallDeliveries;
  let announce: (kind: 'package' | 'announcement' | 'league') => void;

  beforeEach(() => {
    hall = setUpHall();
    deliveries = TestBed.inject(HallDeliveries);
    const announcements = TestBed.inject(DeliveryAnnouncements);
    announce = (kind) => announcements.announce(kind);
  });

  afterEach(() => hall.finish());

  it('shows delivered items per collection, added to a dialog still open', async () => {
    announce('package');
    hall.request('/delivery/misc', 'GET').flush(readServerResponse('pack-delivery.xml'));
    await settle();
    const first = deliveries.newItems();
    expect(first.length).toBeGreaterThan(0);
    announce('package');
    hall.request('/delivery/misc', 'GET').flush(readServerResponse('pack-delivery.xml'));
    await settle();
    expect(deliveries.newItems().length).toBe(first.length);
    expect(deliveries.newItems()[0].packs.length + deliveries.newItems()[0].cards.length).toBe(
      2 * (first[0].packs.length + first[0].cards.length),
    );
    deliveries.closeNewItems(first[0].collectionName);
    expect(deliveries.newItems().length).toBe(first.length - 1);
    announce('package');
    hall.request('/delivery/misc', 'GET').flush('', { status: 404, statusText: 'Not Found' });
    await settle();
  });

  it('shows one announcement, and dismisses or snoozes it', async () => {
    announce('announcement');
    hall.request('/delivery/announcements', 'GET').flush(readServerResponse('delivery-announcement.json'));
    await settle();
    const shown = deliveries.announcement();
    expect(shown?.title).toBe('Yuletide update');
    expect(shown?.content[0]).toMatchObject({ kind: 'element', tag: 'p' });
    expect(JSON.stringify(shown?.content)).toContain('"kind":"hall-link"');
    // asked again while it shows: no request
    announce('announcement');
    deliveries.dismissAnnouncement();
    hall.request('/delivery/announcements/dismiss').flush('');
    expect(deliveries.announcement()).toBeNull();

    announce('announcement');
    hall.request('/delivery/announcements', 'GET').flush(readServerResponse('delivery-announcement.json'));
    await settle();
    deliveries.snoozeAnnouncement();
    hall.request('/delivery/announcements/snooze').flush('');
  });

  it('shows the Race to Mount Doom moves one after the other', async () => {
    announce('league');
    hall.request('/delivery/league', 'GET').flush(readServerResponse('delivery-league.json'));
    await settle();
    expect(deliveries.leagueNotifications()).toEqual([
      {
        message: 'You advanced to Rivendell!',
        old: { visual: '1_331', modifier: '90_1' },
        next: { visual: '1_340', modifier: '90_7' },
      },
    ]);
    deliveries.closeLeagueNotification();
    expect(deliveries.leagueNotifications()).toEqual([]);
    expect(parseLeagueNotification({ contents: '|||' })).toEqual({
      message: 'You have advanced!',
      old: null,
      next: null,
    });
    expect(parseLeagueNotification({ contents: 'a||b|' })).toMatchObject({ old: { visual: 'a', modifier: null } });
  });
});
