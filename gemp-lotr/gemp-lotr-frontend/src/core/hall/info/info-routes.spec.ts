import { hallRouteOf } from '../hall-links';
import { INFO_PAGES, patchNotesRoute } from './info-routes';

describe('Server Info routes', () => {
  it('has the legacy sub-tabs, in order, each with its route', () => {
    expect(INFO_PAGES.map((page) => [page.slug, page.label])).toEqual([
      ['stats', 'Server Stats'],
      ['patch-notes', 'Patch Notes'],
      ['contribute', 'Contribute'],
      ['about', 'About'],
    ]);
    for (const page of INFO_PAGES) {
      expect(hallRouteOf(`#info/${page.slug}`)?.path).toBe(`/info/${page.slug}`);
    }
  });

  it('links the patch notes the way the legacy links read', () => {
    expect(patchNotesRoute(null)).toEqual(hallRouteOf('#patch-notes'));
    expect(patchNotesRoute('2026-11-18')).toEqual(hallRouteOf('#patch-notes/2026-11-18'));
  });
});
