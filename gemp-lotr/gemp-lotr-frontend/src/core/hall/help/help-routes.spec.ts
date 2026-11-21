import { hallRouteOf } from '../hall-links';
import { errataRoute, formatRoute, HELP_PAGES, MANUAL_FRAGMENT } from './help-routes';

describe('Help routes', () => {
  it('has the legacy sub-tabs, in order, each with its route', () => {
    expect(HELP_PAGES.map((page) => [page.slug, page.label])).toEqual([
      ['how-to-play', 'How Do I Play?'],
      ['conduct', 'Code of Conduct'],
      ['formats', 'Format Definitions'],
      ['leagues', 'What Are Leagues?'],
      ['errata', 'PC Errata'],
    ]);
    for (const page of HELP_PAGES) {
      expect(hallRouteOf(`#help/${page.legacySlug}`)?.path).toBe(`/help/${page.slug}`);
    }
    expect(hallRouteOf('#help/manual')?.fragment).toBe(MANUAL_FRAGMENT);
  });

  it('links a format and the errata the way the legacy links read', () => {
    expect(formatRoute('pc_movie')).toEqual(hallRouteOf('#format-pc_movie'));
    expect(errataRoute('pc_movie')).toEqual({
      path: '/help/errata',
      queryParams: { format: 'pc_movie' },
      fragment: null,
    });
    expect(errataRoute(null)).toEqual(hallRouteOf('#pc-errata'));
  });
});
