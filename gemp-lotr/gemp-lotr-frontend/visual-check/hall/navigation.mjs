// The old addresses, for hall/run.mjs: hall.html#<fragment> and the site's start page (/gemp-lotr/, index.html) are
// small pages that send the browser on to the Angular app (its `go` and `start` routes), which decides where each
// legacy link leads (core/hall/legacy-links.guard.ts). Not a screenshot check: each address is followed to where it
// ends, and the tab and sub-tab shown there are compared with what the legacy hall (hall-legacy.html) shows for the
// same fragment. A visitor at the start page goes to the login page, which comes back to that place. The Admin link
// of an admin's Angular hall leads to the legacy hall's Admin tab, which then runs no hall of its own.
import { STUB_ORIGIN } from '../stub-server.mjs';

/** The fragments the legacy hall understood (hallLinks.js GempLinks): tabs, sub-tabs and the links inside pages. */
const FRAGMENTS = [
  { fragment: '', known: false },
  { fragment: '#game-hall' },
  { fragment: '#hall' },
  { fragment: '#help' },
  { fragment: '#help/formats' },
  { fragment: '#format-pc_fotr_block' },
  { fragment: '#help/manual' },
  { fragment: '#pc-errata' },
  { fragment: '#errata-1_45' },
  { fragment: '#events' },
  { fragment: '#events/tournaments' },
  { fragment: '#league-1700000001' },
  { fragment: '#tournament-daily-pc_movie-20261120' },
  { fragment: '#info/patch-notes' },
  { fragment: '#server-info' },
  { fragment: '#account/history' },
  { fragment: '#my-account' },
  { fragment: '#nonsense', known: false },
  { fragment: '#admin/league', admin: true },
];

/** Where the page is: the tab and sub-tab shown (legacy: GempLinks.current(); Angular: the frame and its sub-tabs). */
const WHERE = () => {
  if (window.GempLinks && typeof window.GempLinks.current === 'function') {
    const current = window.GempLinks.current();
    return current ? { tab: current.tab?.slug ?? null, sub: current.sub?.slug ?? null } : { tab: null, sub: null };
  }
  const frame = document.querySelector('gd-hall-shell, .gd-hall');
  const sub = document.querySelector('.gd-hall-sub-tabs__tab[aria-selected="true"]');
  return { tab: frame?.getAttribute('data-tab') ?? null, sub: sub?.getAttribute('data-sub-tab') ?? null };
};

const describe = (where) => (where ? `${where.tab ?? '-'}${where.sub ? `/${where.sub}` : ''}` : '-');
const localUrl = (page) => page.url().replace(STUB_ORIGIN, '');

/** Opens `address` in a page of its own (as a visitor, or an admin) and says where it ended. */
async function follow(openPage, size, address, stubs) {
  const { context, page } = await openPage(size, stubs, []);
  const hallCalls = [];
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (/^\/gemp-lotr-server\/(hall|chat\/)/.test(url.pathname) && !url.pathname.includes('/json')) {
      hallCalls.push(url.pathname);
    }
  });
  try {
    await page.goto(STUB_ORIGIN + address);
    await page.waitForTimeout(3000);
    return { url: localUrl(page), where: await page.evaluate(WHERE), page, context, hallCalls };
  } catch (error) {
    await context.close();
    return { url: `could not open: ${String(error.message).split('\n')[0]}`, where: null, hallCalls };
  }
}

export async function checkNavigation(openPage, size) {
  const results = [];
  const record = (label, legacy, angular, problems) => {
    results.push({ label, legacy, angular, problems });
    console.log(
      `${problems.length ? 'DIFF' : 'SAME'} navigation ${label}${problems.length ? `: ${problems.join('; ')}` : ''}`,
    );
  };
  for (const { fragment, known = true, admin = false } of FRAGMENTS) {
    const stubs = admin ? { player: { type: 'au' } } : {};
    const legacy = await follow(openPage, size, `/gemp-lotr/hall-legacy.html${fragment}`, stubs);
    const angular = await follow(openPage, size, `/gemp-lotr/hall.html${fragment}`, stubs);
    const problems = [];
    if (describe(legacy.where) !== describe(angular.where)) {
      problems.push(`the legacy hall shows ${describe(legacy.where)}, the Angular hall ${describe(angular.where)}`);
    }
    if (admin && !angular.url.startsWith('/gemp-lotr/hall-legacy.html?admin=1')) {
      problems.push(`an Admin link should open the legacy hall for its Admin tab, not ${angular.url}`);
    }
    record(`hall.html${fragment}`, describe(legacy.where), `${describe(angular.where)} at ${angular.url}`, problems);

    // The same fragment on the start page: a player goes where hall.html went, a visitor to the login page
    if (!admin) {
      const start = await follow(openPage, size, `/gemp-lotr/${fragment}`, {});
      const startProblems = start.url === angular.url ? [] : [`a player ends at ${start.url}, not ${angular.url}`];
      record(`/gemp-lotr/${fragment}`, '-', `${describe(start.where)} at ${start.url}`, startProblems);
      await start.context?.close();

      // A visitor: the login page, which comes back (?return=) to where a player went
      const visitor = await follow(openPage, size, `/gemp-lotr/${fragment}`, { visitor: true });
      const asked = new URL(STUB_ORIGIN + visitor.url);
      const back = asked.searchParams.get('return');
      const visitorProblems = [];
      if (asked.pathname !== '/gemp-lotr/ng/login') {
        visitorProblems.push(`a visitor ends at ${visitor.url}, not the login page`);
      } else if (!known && back !== null) {
        visitorProblems.push(`the login page comes back to ${back}, not the hall's first page`);
      } else if (known && back === null) {
        visitorProblems.push('the login page does not come back to the place asked for');
      } else if (back !== null) {
        const returned = await follow(openPage, size, `/gemp-lotr/ng${back}`, {});
        if (describe(returned.where) !== describe(angular.where)) {
          visitorProblems.push(`after logging in it shows ${describe(returned.where)}, not ${describe(angular.where)}`);
        }
        await returned.context?.close();
      }
      record(`/gemp-lotr/${fragment} (visitor)`, '-', visitor.url, visitorProblems);
      await visitor.context?.close();
    }
    await legacy.context?.close();
    await angular.context?.close();
  }

  // hall.html as a visitor: the tab still opens (the legacy hall asked for no login itself)
  for (const fragment of ['#events/leagues', '#help/formats']) {
    const legacy = await follow(openPage, size, `/gemp-lotr/hall-legacy.html${fragment}`, { visitor: true });
    const angular = await follow(openPage, size, `/gemp-lotr/hall.html${fragment}`, { visitor: true });
    const problems =
      describe(legacy.where) === describe(angular.where)
        ? []
        : [`the legacy hall shows ${describe(legacy.where)}, the Angular hall ${describe(angular.where)}`];
    record(
      `hall.html${fragment} (visitor)`,
      describe(legacy.where),
      `${describe(angular.where)} at ${angular.url}`,
      problems,
    );
    await legacy.context?.close();
    await angular.context?.close();
  }

  // An admin's Admin link: the legacy hall's Admin tab, which starts no hall or chat of its own
  const hall = await follow(openPage, size, '/gemp-lotr/ng/hall?theme=classic', { player: { type: 'au' } });
  const problems = [];
  let target = '-';
  if (hall.page) {
    const link = hall.page.locator('a[href*="hall-legacy.html"]').first();
    const href = await link.getAttribute('href', { timeout: 3000 }).catch(() => null);
    if (href !== '/gemp-lotr/hall-legacy.html?admin=1#admin') {
      problems.push(`the Admin link is ${href ?? 'missing'}`);
    }
    if (href) {
      const admin = await follow(openPage, size, href, { player: { type: 'au' } });
      target = `${describe(admin.where)} at ${admin.url}`;
      if (admin.where?.tab !== 'admin') problems.push(`it opens ${describe(admin.where)}, not the Admin tab`);
      if (admin.hallCalls.length)
        problems.push(`the legacy hall opened for Admin calls ${[...new Set(admin.hallCalls)].join(', ')}`);
      await admin.context?.close();
    }
  }
  record('the Admin link', '-', target, problems);
  await hall.context?.close();
  return results;
}
