import { APP_URL } from '../../core/platform/app-paths';

/** A link button at the top of the Game Hall tab (hall.html #infoButtons), opened in a new tab. */
export interface HallInfoLink {
  /** Which link (`data-link`, for its icon). */
  readonly link: string;
  readonly label: string;
  readonly href: string;
  /** Shown struck through (the legacy hall strikes out the Merchant). */
  readonly struck: boolean;
}

/** hall.html #infoButtons, in order. */
export const HALL_INFO_LINKS: readonly HallInfoLink[] = [
  { link: 'bug', label: 'Report a Bug', href: 'https://forms.gle/pks6CHMWcpBsTcwv9', struck: false },
  {
    link: 'report',
    label: 'Report a Player',
    href: 'https://lotrtcgwiki.com/forums/index.php?topic=8267.585#new',
    struck: false,
  },
  { link: 'discord', label: 'Join the PC Discord', href: 'https://lotrtcgpc.net/discord', struck: false },
  { link: 'wiki', label: 'Browse the LOTR-TCG Wiki', href: 'https://wiki.lotrtcgpc.net', struck: false },
  // The merchant stays a legacy page (HALL-PORT.md)
  { link: 'merchant', label: 'Merchant', href: '/gemp-lotr/merchant.html', struck: true },
];

/** The main bar's Deck-builder link (a new tab, as the legacy hall: hall.html #deckbuilder-button). */
export const DECKBUILDER_HREF = `${APP_URL}/deckbuilder/`;

/** The Decipher notice at the bottom of the hall (hall.html #disclaimer). */
export const HALL_DISCLAIMER =
  'The information presented on this site about LotR TCG, both literal and graphical, is copyrighted by Decipher ' +
  'Inc. This website is not produced, endorsed, supported, or affiliated with Decipher.';
