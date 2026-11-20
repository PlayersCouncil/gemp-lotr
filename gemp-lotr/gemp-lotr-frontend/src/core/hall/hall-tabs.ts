/** Which part of the hall rewrite fills a tab (see README.md). */
export type HallPart = '4.1' | '4.2' | '4.3' | '4.4' | '4.5';

/** A top-level tab of the hall (hall.html #tabs; the deep-link slugs of hallLinks.js GempLinks.TABS). */
export interface HallTab {
  readonly slug: string;
  readonly label: string;
  /** Opening it needs a logged-in player (it starts the hall, or asks the visitor to log in). */
  readonly login: boolean;
  /** Shown only to admins and league admins (the player's type has "a" or "l"). */
  readonly adminOnly: boolean;
  /** Hidden in the legacy hall ("until that feature is ready"). */
  readonly hidden: boolean;
  /** Older slugs that still open it. */
  readonly aliases: readonly string[];
  /** The sub-tabs' slugs, in order. */
  readonly subTabs: readonly string[];
  readonly part: HallPart;
}

export const HALL_TABS: readonly HallTab[] = [
  {
    slug: 'game-hall',
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
    label: 'Help',
    login: false,
    adminOnly: false,
    hidden: false,
    aliases: [],
    subTabs: ['howtoplay', 'coc', 'formats', 'leagues', 'pc-errata'],
    part: '4.3',
  },
  {
    slug: 'events',
    label: 'Events',
    login: true,
    adminOnly: false,
    hidden: false,
    aliases: [],
    subTabs: ['calendar', 'leagues', 'tournaments'],
    part: '4.4',
  },
  {
    slug: 'info',
    label: 'Server Info',
    login: false,
    adminOnly: false,
    hidden: false,
    aliases: ['server-info'],
    subTabs: ['stats', 'patch-notes', 'contribute', 'about'],
    part: '4.3',
  },
  {
    slug: 'users',
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
    label: 'My Account',
    login: true,
    adminOnly: false,
    hidden: false,
    aliases: ['my-account'],
    subTabs: ['overview', 'history', 'stats'],
    part: '4.4',
  },
  {
    slug: 'admin',
    label: 'Admin',
    login: true,
    adminOnly: true,
    hidden: false,
    aliases: [],
    subTabs: ['general', 'league', 'tournament', 'prizes'],
    part: '4.5',
  },
];

/** The tab opened when nothing else says which. */
export const DEFAULT_HALL_TAB = 'game-hall';

/** localStorage key of the tab last opened (GempHallTabs.STORAGE_KEY; the legacy value is the tab's href). */
export const HALL_TAB_STORAGE_KEY = 'gemp.hall.activeTab';

/** The tab a slug (or alias) names, or null. */
export function findHallTab(slug: string): HallTab | null {
  return HALL_TABS.find((tab) => tab.slug === slug || tab.aliases.includes(slug)) ?? null;
}

/** The tabs a player sees: not the hidden ones, and the Admin tab only for admins and league admins. */
export function visibleHallTabs(admin: boolean): HallTab[] {
  return HALL_TABS.filter((tab) => !tab.hidden && (!tab.adminOnly || admin));
}
