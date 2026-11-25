import { readLegacyWebFile } from '../../testing/files';
import {
  findHallTab,
  HALL_TAB_STORAGE_KEY,
  HALL_TABS,
  hallTabAt,
  rememberedHallTab,
  visibleHallTabs,
} from './hall-tabs';

describe('hall tabs', () => {
  it('has the legacy tabs, labels and slugs, in order', () => {
    const html = readLegacyWebFile('hall-legacy.html');
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

  it("has the legacy tabs' links (what the legacy hall remembers) and the sub-tabs' labels", () => {
    const html = readLegacyWebFile('hall-legacy.html');
    const hrefs = [...html.matchAll(/<a href="([^"]*)" class="custom-icon icon-\w+">/g)].map((m) => m[1]);
    expect(HALL_TABS.map((t) => t.href)).toEqual(hrefs);
    for (const tab of HALL_TABS.filter((t) => t.subTabs.length > 0)) {
      const page = readLegacyWebFile(tab.href);
      const labels = [...page.matchAll(/<li><a [^>]*href="[^"]*">([^<]+)<\/a><\/li>/g)].map((m) => m[1]);
      expect(tab.subTabs.map((sub) => sub.label)).toEqual(labels);
    }
  });

  it('finds a tab by its route', () => {
    expect(hallTabAt('hall')?.slug).toBe('game-hall');
    expect(hallTabAt('info')?.slug).toBe('info');
    expect(hallTabAt('admin')).toBeNull();
  });

  it(`opens the tab last opened (${HALL_TAB_STORAGE_KEY}, as either hall stored it), else the Game Hall`, () => {
    expect(rememberedHallTab('includes/help.html').slug).toBe('help');
    expect(rememberedHallTab('account').slug).toBe('account');
    expect(rememberedHallTab('#gameHall').slug).toBe('game-hall');
    expect(rememberedHallTab('includes/admin.html').slug).toBe('game-hall');
    expect(rememberedHallTab('includes/user.html').slug).toBe('game-hall');
    expect(rememberedHallTab('nonsense').slug).toBe('game-hall');
    expect(rememberedHallTab(null).slug).toBe('game-hall');
  });
});
