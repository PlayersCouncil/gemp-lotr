/** Which part of the hall rewrite fills a tab (see README.md). */
export type HallPart = '4.1' | '4.2' | '4.3' | '4.4' | '4.5';

/** A sub-tab of a hall tab (the jQuery UI tabs inside includes/help.html, events.html, info.html, account.html). */
export interface HallSubTab {
  /** The legacy deep-link slug (hallLinks.js GempLinks.TABS subs): "#help/<slug>". */
  readonly slug: string;
  /** Its route under the tab's ('' for the tab's own first page, My Account's overview). */
  readonly path: string;
  /** The legacy sub-tab's label. */
  readonly label: string;
}

/** A top-level tab of the hall (hall.html #tabs; the deep-link slugs of hallLinks.js GempLinks.TABS). */
export interface HallTab {
  /** The legacy deep-link slug ("#help"). */
  readonly slug: string;
  /** Its route in the Angular hall ("/help"); null for a tab the Angular hall doesn't have (Admin, Users). */
  readonly path: string | null;
  /** The legacy tab's link (hall.html), which the legacy hall keeps in localStorage as the tab last opened. */
  readonly href: string;
  readonly label: string;
  /** Opening it needs a logged-in player (it starts the hall, or asks the visitor to log in). */
  readonly login: boolean;
  /** Shown only to admins and league admins (the player's type has "a" or "l"). */
  readonly adminOnly: boolean;
  /** Hidden in the legacy hall ("until that feature is ready"). */
  readonly hidden: boolean;
  /** Older slugs that still open it. */
  readonly aliases: readonly string[];
  /** The sub-tabs, in order. */
  readonly subTabs: readonly HallSubTab[];
  readonly part: HallPart;
}

export const HALL_TABS: readonly HallTab[] = [
  {
    slug: 'game-hall',
    path: 'hall',
    href: '#gameHall',
    label: 'Game Hall',
    login: true,
    adminOnly: false,
    hidden: false,
    aliases: ['hall', 'gameHall'],
    subTabs: [],
    part: '4.2',
  },
  {
    slug: 'help',
    path: 'help',
    href: 'includes/help.html',
    label: 'Help',
    login: false,
    adminOnly: false,
    hidden: false,
    aliases: [],
    subTabs: [
      { slug: 'howtoplay', path: 'how-to-play', label: 'How Do I Play?' },
      { slug: 'coc', path: 'conduct', label: 'Code of Conduct' },
      { slug: 'formats', path: 'formats', label: 'Format Definitions' },
      { slug: 'leagues', path: 'leagues', label: 'What Are Leagues?' },
      { slug: 'pc-errata', path: 'errata', label: 'PC Errata' },
    ],
    part: '4.3',
  },
  {
    slug: 'events',
    path: 'events',
    href: 'includes/events.html',
    label: 'Events',
    login: true,
    adminOnly: false,
    hidden: false,
    aliases: [],
    subTabs: [
      { slug: 'calendar', path: 'calendar', label: 'Calendar' },
      { slug: 'leagues', path: 'leagues', label: 'Current Leagues' },
      { slug: 'tournaments', path: 'tournaments', label: 'Current Tournaments' },
    ],
    part: '4.4',
  },
  {
    slug: 'info',
    path: 'info',
    href: 'includes/info.html',
    label: 'Server Info',
    login: false,
    adminOnly: false,
    hidden: false,
    aliases: ['server-info'],
    subTabs: [
      { slug: 'stats', path: 'stats', label: 'Server Stats' },
      { slug: 'patch-notes', path: 'patch-notes', label: 'Patch Notes' },
      { slug: 'contribute', path: 'contribute', label: 'Contribute' },
      { slug: 'about', path: 'about', label: 'About' },
    ],
    part: '4.3',
  },
  {
    slug: 'users',
    path: null,
    href: 'includes/user.html',
    label: 'Users',
    login: true,
    adminOnly: false,
    hidden: true,
    aliases: [],
    subTabs: [],
    part: '4.4',
  },
  {
    slug: 'account',
    path: 'account',
    href: 'includes/account.html',
    label: 'My Account',
    login: true,
    adminOnly: false,
    hidden: false,
    aliases: ['my-account'],
    subTabs: [
      { slug: 'overview', path: '', label: 'My Account' },
      { slug: 'history', path: 'history', label: 'My Game History' },
      { slug: 'stats', path: 'stats', label: 'My Stats' },
      { slug: 'scenarios', path: 'scenarios', label: 'My Scenarios' },
    ],
    part: '4.4',
  },
  {
    slug: 'admin',
    path: null,
    href: 'includes/admin.html',
    label: 'Admin',
    login: true,
    adminOnly: true,
    hidden: false,
    aliases: [],
    subTabs: [
      { slug: 'general', path: 'general', label: 'General Admin' },
      { slug: 'league', path: 'league', label: 'League Admin' },
      { slug: 'tournament', path: 'tournament', label: 'Tournament Admin' },
      { slug: 'prizes', path: 'prizes', label: 'Prizes' },
      { slug: 'scenarios', path: 'scenarios', label: 'Scenario Admin' },
      { slug: 'users', path: 'users', label: 'Manage Users' },
    ],
    part: '4.5',
  },
];

/** The tab opened when nothing else says which. */
export const DEFAULT_HALL_TAB = 'game-hall';

/** localStorage key of the tab last opened (GempHallTabs.STORAGE_KEY; the value is the tab's legacy href). */
export const HALL_TAB_STORAGE_KEY = 'gemp.hall.activeTab';

/** The tab a slug (or alias) names, or null. */
export function findHallTab(slug: string): HallTab | null {
  return HALL_TABS.find((tab) => tab.slug === slug || tab.aliases.includes(slug)) ?? null;
}

/** The tab whose route is `path` (the first segment of the hall's address), or null. */
export function hallTabAt(path: string): HallTab | null {
  return HALL_TABS.find((tab) => tab.path !== null && tab.path === path) ?? null;
}

/** A tab's sub-tab by its legacy slug, or null. */
export function findHallSubTab(tab: HallTab, slug: string): HallSubTab | null {
  return tab.subTabs.find((sub) => sub.slug === slug) ?? null;
}

/** The tabs a player sees: not the hidden ones, and the Admin tab only for admins and league admins. */
export function visibleHallTabs(admin: boolean): HallTab[] {
  return HALL_TABS.filter((tab) => !tab.hidden && (!tab.adminOnly || admin));
}

/**
 * The tab to open with no address but the hall's: the one last opened (as stored by either hall: the legacy href, or
 * a slug), if the Angular hall has it; else the Game Hall.
 */
export function rememberedHallTab(stored: string | null): HallTab {
  const tab = stored === null ? null : (HALL_TABS.find((t) => t.href === stored) ?? findHallTab(stored));
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- the default tab is in the table
  return tab?.path != null && !tab.hidden ? tab : findHallTab(DEFAULT_HALL_TAB)!;
}
