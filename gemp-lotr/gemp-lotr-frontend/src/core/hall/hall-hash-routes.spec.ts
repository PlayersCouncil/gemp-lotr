import { readLegacyWebFile } from '../../testing/files';
import { hallAddressOfHash, LEGACY_HALL_URL } from './hall-hash-routes';
import { HALL_TABS } from './hall-tabs';

const route = (hash: string) => {
  const address = hallAddressOfHash(hash);
  return address !== null && 'route' in address ? address.route : address;
};

describe('hallAddressOfHash', () => {
  it.each([
    ['', '/'],
    ['#', '/'],
    ['#game-hall', '/hall'],
    ['#hall', '/hall'],
    ['#gameHall', '/hall'],
    ['#help', '/help'],
    ['#help/', '/help'],
    ['#help/howtoplay', '/help/how-to-play'],
    ['#help/coc', '/help/conduct'],
    ['#help/formats', '/help/formats'],
    ['#help/leagues', '/help/leagues'],
    ['#help/pc-errata', '/help/errata'],
    ['#help/manual', '/help/how-to-play#manual'],
    ['#help/nonsense', '/help'],
    ['#format-pc_fotr_block', '/help/formats/pc_fotr_block'],
    ['#pc-errata', '/help/errata'],
    [
      '#pc-errata?q=gandalf&set=1&format=pc_movie&show=recent',
      '/help/errata?q=gandalf&set=1&format=pc_movie&show=recent',
    ],
    ['#pc-errata?q=the+one%20ring&sort=num&bad=%E0%A4%A', '/help/errata?q=the+one+ring'],
    ['#pc-errata?card=1_2&set=1', '/help/errata/1_2?set=1'],
    ['#errata-1_2', '/help/errata/1_2'],
    ['#errata-51_12?format=pc_movie', '/help/errata/51_12?format=pc_movie'],
    ['#events', '/events'],
    ['#events/calendar', '/events/calendar'],
    ['#events/leagues', '/events/leagues'],
    ['#events/tournaments', '/events/tournaments'],
    ['#league-1234', '/events/leagues/1234'],
    ['#tournament-pc_fotr.2026-11', '/events/tournaments/pc_fotr.2026-11'],
    ['#info', '/info'],
    ['#server-info', '/info'],
    ['#info/stats', '/info/stats'],
    ['#info/patch-notes', '/info/patch-notes'],
    ['#info/contribute', '/info/contribute'],
    ['#info/about', '/info/about'],
    ['#patch-notes', '/info/patch-notes'],
    ['#patch-notes/2026-11-yuletide', '/info/patch-notes/2026-11-yuletide'],
    ['#account', '/account'],
    ['#my-account', '/account'],
    ['#account/overview', '/account'],
    ['#account/history', '/account/history'],
    ['#account/stats', '/account/stats'],
    ['#account/scenarios', '/account/scenarios'],
    ['#users', '/hall'],
    ['help/formats', '/help/formats'],
  ])('%s → %s', (hash, expected) => {
    expect(route(hash)).toBe(expected);
  });

  it('sends the Admin tab and its sub-tabs to the legacy hall', () => {
    expect(hallAddressOfHash('#admin')).toEqual({ page: `${LEGACY_HALL_URL}#admin` });
    expect(hallAddressOfHash('#admin/scenarios')).toEqual({ page: `${LEGACY_HALL_URL}#admin/scenarios` });
    expect(hallAddressOfHash('#admin/nonsense')).toEqual({ page: `${LEGACY_HALL_URL}#admin` });
  });

  it.each(['#nonsense', '#help/formats/extra', '#format-', '#league-a b', '#<script>', '#errata-'])(
    'knows nothing of %s',
    (hash) => {
      expect(hallAddressOfHash(hash)).toBeNull();
    },
  );

  it("covers every tab and sub-tab of the legacy router's table (hallLinks.js GempLinks.TABS)", () => {
    const links = readLegacyWebFile('js/gemp-022/hallLinks.js');
    const table = links.slice(links.indexOf('TABS: ['), links.indexOf('ROUTES: ['));
    const tabs = [...table.matchAll(/\{slug: "([\w-]+)", href:[^\n]*?(?:subs: \[([\s\S]*?)\n\t\t\]\}|\})/g)];
    expect(tabs.map((m) => m[1])).toEqual(HALL_TABS.map((tab) => tab.slug));
    for (const [, tab, subs] of tabs) {
      for (const [, sub] of (subs ?? '').matchAll(/\{slug: "([\w-]+)"/g)) {
        expect(hallAddressOfHash(`#${tab}/${sub}`), `#${tab}/${sub}`).not.toBeNull();
      }
      const own = HALL_TABS.find((t) => t.slug === tab);
      expect(own?.subTabs.map((s) => s.slug)).toEqual(
        [...(subs ?? '').matchAll(/\{slug: "([\w-]+)"/g)].map((m) => m[1]),
      );
    }
  });
});
