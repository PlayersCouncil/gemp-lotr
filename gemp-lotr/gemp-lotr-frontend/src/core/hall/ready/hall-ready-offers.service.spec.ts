import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallReadyOffers } from './hall-ready-offers.service';

describe('HallReadyOffers', () => {
  let hall: HallTestBed;
  let offers: HallReadyOffers;

  beforeEach(() => {
    hall = setUpHall();
    offers = TestBed.inject(HallReadyOffers);
  });

  afterEach(() => hall.finish());

  it('opens a tab, or offers the page once when the browser blocks it', () => {
    expect(offers.openOrOffer('game:1', 'game', '/g1')).toBe(true);
    expect(offers.offers()).toEqual([]);
    hall.navigation.blockPopups = true;
    expect(offers.openOrOffer('game:2', 'game', '/g2')).toBe(false);
    offers.openOrOffer('game:2', 'game', '/g2');
    offers.openOrOffer('draft:3', 'draft', '/d3');
    expect(offers.offers().map((o) => o.key)).toEqual(['game:2', 'draft:3']);
    offers.keepOnly('game', new Set());
    expect(offers.offers().map((o) => o.key)).toEqual(['draft:3']);
    offers.dismiss('draft:3');
    expect(offers.has('draft:3')).toBe(false);
  });

  it('points a reserved tab at the page, or offers it when there is none', () => {
    const tab = hall.navigation.reserveTab();
    offers.openReserved(tab, 'game:4', 'game', '/g4');
    expect(hall.navigation.reserved[0].url).toBe('/g4');
    offers.openReserved(null, 'game:5', 'game', '/g5');
    expect(offers.offers().map((o) => o.url)).toEqual(['/g5']);
  });
});
