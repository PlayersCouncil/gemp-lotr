import { hallRouteOf } from './hall-links';

describe('hallRouteOf (the legacy hall links)', () => {
  it('reads the links into a page', () => {
    expect(hallRouteOf('#format-pc_movie')).toEqual({
      path: '/help/formats/pc_movie',
      queryParams: {},
      fragment: null,
    });
    expect(hallRouteOf('format-pc_fotr_block')?.path).toBe('/help/formats/pc_fotr_block');
    expect(hallRouteOf('#pc-errata')).toEqual({ path: '/help/errata', queryParams: {}, fragment: null });
    expect(hallRouteOf('#errata-1_5?format=pc_movie&q=gandalf+the')).toEqual({
      path: '/help/errata/1_5',
      queryParams: { format: 'pc_movie', q: 'gandalf the' },
      fragment: null,
    });
    expect(hallRouteOf('#pc-errata?q=%E0%A4%A&set=1')?.queryParams).toEqual({ set: '1' });
    expect(hallRouteOf('#patch-notes')?.path).toBe('/info/patch-notes');
    expect(hallRouteOf('#patch-notes/2026-09-27-Hall-Overhaul')?.path).toBe(
      '/info/patch-notes/2026-09-27-hall-overhaul',
    );
    expect(hallRouteOf('#help/manual')).toEqual({ path: '/help/how-to-play', queryParams: {}, fragment: 'manual' });
    expect(hallRouteOf('#league-wc2026')?.path).toBe('/events/leagues/wc2026');
    expect(hallRouteOf('#tournament-2026.11.1')?.path).toBe('/events/tournaments/2026.11.1');
    expect(hallRouteOf('#card-1_5')).toEqual({ path: '/help', queryParams: { card: '1_5' }, fragment: null });
  });

  it('reads the tab and sub-tab links, aliases included', () => {
    expect(hallRouteOf('#game-hall')?.path).toBe('/hall');
    expect(hallRouteOf('#gameHall')?.path).toBe('/hall');
    expect(hallRouteOf('#help')?.path).toBe('/help');
    expect(hallRouteOf('#help/howtoplay')?.path).toBe('/help/how-to-play');
    expect(hallRouteOf('#help/coc')?.path).toBe('/help/conduct');
    expect(hallRouteOf('#help/formats/')?.path).toBe('/help/formats');
    expect(hallRouteOf('#help/pc-errata')?.path).toBe('/help/errata');
    expect(hallRouteOf('#server-info/stats')?.path).toBe('/info/stats');
    expect(hallRouteOf('#info/patch-notes')?.path).toBe('/info/patch-notes');
    expect(hallRouteOf('#events/leagues')?.path).toBe('/events/leagues');
    expect(hallRouteOf('#my-account/overview')?.path).toBe('/account');
    expect(hallRouteOf('#account/scenarios')?.path).toBe('/account/scenarios');
    expect(hallRouteOf('#help/nothing')?.path).toBe('/help');
  });

  it('names no route for what is not ported or not a link', () => {
    expect(hallRouteOf('#admin')).toBeNull();
    expect(hallRouteOf('#admin/scenarios')).toBeNull();
    // Users is hidden in the legacy hall: its link opens the hall
    expect(hallRouteOf('#users')?.path).toBe('/hall');
    expect(hallRouteOf('#nowhere')).toBeNull();
    expect(hallRouteOf('#')).toBeNull();
    expect(hallRouteOf('#format-')).toBeNull();
  });
});
