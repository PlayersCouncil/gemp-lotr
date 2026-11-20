import { readLegacyWebFile } from '../../testing/files';
import { findHallTab, HALL_TABS, visibleHallTabs } from './hall-tabs';

describe('hall tabs', () => {
  it('has the legacy tabs, labels and slugs, in order', () => {
    const html = readLegacyWebFile('hall.html');
    const labels = [...html.matchAll(/<a href="[^"]*" class="custom-icon icon-\w+">([^<]+)<\/a>/g)].map((m) => m[1]);
    expect(HALL_TABS.map((t) => t.label)).toEqual(labels);
    const links = readLegacyWebFile('js/gemp-022/hallLinks.js');
    const slugs = [...links.matchAll(/^\t\t\{slug: "([\w-]+)", href:/gm)].map((m) => m[1]);
    expect(HALL_TABS.map((t) => t.slug)).toEqual(slugs);
  });

  it('finds tabs by slug or alias', () => {
    expect(findHallTab('hall')?.slug).toBe('game-hall');
    expect(findHallTab('my-account')?.slug).toBe('account');
    expect(findHallTab('nope')).toBeNull();
  });

  it('hides Users, and Admin from non-admins', () => {
    expect(visibleHallTabs(false).map((t) => t.slug)).toEqual(['game-hall', 'help', 'events', 'info', 'account']);
    expect(visibleHallTabs(true).map((t) => t.slug)).toContain('admin');
  });
});
