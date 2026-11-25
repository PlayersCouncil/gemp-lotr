// The hall's and the login page's scenarios for hall/run.mjs: each opens the legacy page and the Angular page at the
// same place, with the same server answers (hall/stubs.mjs), then takes both through the same steps; the screens are
// compared after loading and after each step.
//
//   name, note   what it shows
//   legacy       the legacy page's address (hall-legacy.html#<tab>, login-legacy.html)
//   angular      the Angular page's address (/gemp-lotr/ng/<route>)
//   settle       ms to wait after loading before the first screenshot (default 2500)
//   styles       { legacy, angular }: CSS added to each page, for what is left out of the comparison on purpose
//   stubs        hall/stubs.mjs's options: visitor (nobody logged in), hall (the hall's snapshot), history, player
//   steps        [label, legacy actions, Angular actions]: an action is [gesture, selector, value?] where the gesture is
//                one of gestures.mjs's (press, hover, escape, ...) or fill, select, key, wait, scrollTo (run.mjs)
//
// The legacy hall's long-poll and chat answer every 4 s, so a screenshot taken a second after a step catches no
// update in flight.

const HALL = '/gemp-lotr/hall-legacy.html';
const NG = '/gemp-lotr/ng/';

/** A scenario of one hall tab: the legacy hall at `#hash`, the Angular hall at `route`. */
const tab = (name, note, hash, route, options = {}) => ({
  name,
  note,
  legacy: `${HALL}#${hash}`,
  angular: `${NG}${route}`,
  steps: [],
  ...options,
});

/**
 * The login page without its picture. The legacy stylesheet sized it to the page's content (`shtml { height: 100% }`,
 * a typo for `html`), the Angular page fills the window as was meant (Ketura, 2026-11-20: not to be preserved), so the
 * picture differs everywhere; without it the rest of the page is compared.
 */
const NO_LOGIN_PICTURE = {
  legacy: 'body { background-image: none !important; }',
  angular: 'gd-login-page { background-image: none !important; }',
};

const PLAY_LEGACY = '#open-table-button';
const PLAY_ANGULAR = 'button.gd-hall__action[data-action=play]';
const FLOWS = {
  scenario: '#create-scenario-button',
  bot: '#create-bot-table-button',
  casual: '#create-unranked-table-button',
  league: '#create-league-table-button',
  tournament: '#create-tournament-button',
};

/** The Play popup, then one of its flows. */
const playFlow = (flow, note, more = []) =>
  tab(`play-${flow}`, note, 'game-hall', 'hall', {
    steps: [
      ['Play', [['press', PLAY_LEGACY]], [['press', PLAY_ANGULAR]]],
      [flow, [['press', FLOWS[flow]]], [['press', `button.gd-play-overlay__choice[data-flow=${flow}]`]]],
      ...more,
    ],
  });

export const SCENARIOS = [
  // ---- the login page ----
  {
    name: 'login',
    note: 'The login page with the server status',
    legacy: '/gemp-lotr/login-legacy.html',
    angular: `${NG}login`,
    stubs: { visitor: true },
    styles: NO_LOGIN_PICTURE,
    steps: [],
  },
  {
    name: 'register',
    note: 'The login page switched to registering',
    legacy: '/gemp-lotr/login-legacy.html',
    angular: `${NG}login`,
    stubs: { visitor: true },
    styles: NO_LOGIN_PICTURE,
    steps: [['Register', [['press', 'text="Register"']], [['press', '[data-action=register]']]]],
  },

  // ---- the frame ----
  tab('frame-player', "The frame, the Game Hall's tables and the chat, as a player", 'game-hall', 'hall', {
    steps: [
      ['Connection readout', [['hover', '.hall-connection-readout']], [['hover', '.gd-hall-connection__readout']]],
    ],
  }),
  tab('frame-visitor-help', 'The frame as a visitor, on a public tab (Help)', 'help', 'help', {
    stubs: { visitor: true },
  }),
  tab('frame-visitor-hall', 'A visitor opening the Game Hall: the "Not logged in" dialog', 'game-hall', 'hall', {
    stubs: { visitor: true },
  }),

  // ---- the Game Hall tab ----
  tab('game-hall-queues', 'Queues, tournaments, the World Championship and a ready check', 'game-hall', 'hall', {
    stubs: { hall: 'hall-queues.xml' },
  }),
  tab('game-hall-prizes', "The ready check answered, then a queue's prizes", 'game-hall', 'hall', {
    stubs: { hall: 'hall-queues.xml' },
    steps: [
      [
        'Ready check: OK',
        [['press', '.ui-dialog button:has-text("OK")']],
        [['press', 'dialog[data-dialog=ready-check] [data-action=ok]']],
      ],
      ['Show Prizes', [['press', 'button:has-text("Show Prizes")']], [['press', 'button:has-text("Show Prizes")']]],
    ],
  }),
  tab('game-hall-sections', 'Closing and opening the sections', 'game-hall', 'hall', {
    steps: [
      ['Close Waiting Tables', [['press', '#waitingTablesHeader']], [['press', '[data-section=waiting]']]],
      ['Close Finished Tables', [['press', '#finishedTablesHeader']], [['press', '[data-section=finished]']]],
    ],
  }),
  tab('join-table', "Join Table on another player's table: the deck dialog", 'game-hall', 'hall', {
    stubs: { hall: 'join' },
    steps: [
      [
        'Join Table',
        [['press', 'button:has-text("Join Table")']],
        [['press', 'button.gd-hall-sections__button[data-action=join]']],
      ],
    ],
  }),
  playFlow('scenario', 'Play: scenarios and tutorials'),
  playFlow('bot', 'Play: a game against the bot'),
  playFlow('casual', 'Play: a casual table, with a refusal for want of a format', [
    [
      'Create without a deck',
      [['press', '#create-unranked-table .create-table-button, #submit-unranked-table-button']],
      [['press', 'gd-casual-form .gd-play-form__create']],
    ],
  ]),
  playFlow('league', 'Play: a league table, the leagues listed'),
  playFlow('tournament', 'Play: creating a tournament'),
  tab('play-back', 'Play, a flow, then Back to the choices', 'game-hall', 'hall', {
    steps: [
      ['Play', [['press', PLAY_LEGACY]], [['press', PLAY_ANGULAR]]],
      ['Casual', [['press', FLOWS.casual]], [['press', 'button.gd-play-overlay__choice[data-flow=casual]']]],
      ['Back', [['press', '#create-table-back-button']], [['press', 'button.gd-play-overlay__back']]],
    ],
  }),

  // ---- Help ----
  tab('help-how-to-play', 'Help › How Do I Play?', 'help/howtoplay', 'help/how-to-play'),
  tab('help-conduct', 'Help › Code of Conduct', 'help/coc', 'help/conduct'),
  tab('help-formats', 'Help › Format Definitions, then filtered', 'help/formats', 'help/formats', {
    steps: [['Filter "gandalf"', [['fill', '.fd-filter', 'gandalf']], [['fill', '.gd-formats__filter', 'gandalf']]]],
  }),
  tab('help-format-link', "A format's own link (#format-<code>)", 'format-pc_fotr_block', 'help/formats/pc_fotr_block'),
  tab('help-leagues', 'Help › What Are Leagues?', 'help/leagues', 'help/leagues'),
  tab('help-errata', 'Help › PC Errata', 'pc-errata', 'help/errata'),
  tab('help-errata-card', "One errata'd card, then its original", 'errata-55_10', 'help/errata/55_10', {
    steps: [['Original (o)', [['key', 'o']], [['key', 'o']]]],
  }),
  tab(
    'help-errata-filtered',
    'PC Errata filtered to a format without errata',
    'pc-errata?format=fotr_block',
    'help/errata?format=fotr_block',
  ),

  // ---- Events ----
  tab(
    'events-calendar',
    'Events › Calendar, then a league clicked and a tournament clicked',
    'events/calendar',
    'events/calendar',
    {
      steps: [
        [
          'A league',
          [['press', '.calendar-bar.kind-league']],
          [['press', '.gd-events-calendar__bar[data-kind=league]']],
        ],
        [
          'A tournament',
          [['press', '.calendar-bar.kind-tournament']],
          [['press', '.gd-events-calendar__bar[data-kind=tournament]']],
        ],
        ['Next month', [['press', '.calendar-header button:has-text(">")']], [['press', '[data-action=next]']]],
      ],
    },
  ),
  tab('events-leagues', 'Events › Current Leagues, a league opened, its matches', 'events/leagues', 'events/leagues', {
    steps: [
      [
        'Open the first league',
        [['press', '#eventLeagueList .event-row-button']],
        [['press', '.gd-events__list .gd-event-row__toggle']],
      ],
      ['Your league matches', [['press', 'a:has-text("Your league matches")']], [['press', '[data-tab=matches]']]],
    ],
  }),
  tab(
    'events-league-link',
    "A league's own link: its drawer open (RTMD)",
    'league-1700000002',
    'events/leagues/1700000002',
    // the legacy hall's smooth scroll to the row ends about 2.2 s after loading, later when the machine is busy
    { settle: 8000 },
  ),
  tab(
    'events-tournaments',
    'Events › Current Tournaments, a completed one opened',
    'events/tournaments',
    'events/tournaments',
    {
      steps: [
        [
          'Completed: details',
          [['press', '#tournamentHistory .event-history-details']],
          [['press', 'gd-event-history[data-kind=tournament] .gd-event-history__detail-cell button']],
        ],
        [
          'Month dropdown',
          [['press', '#tournamentHistory .event-history-month-title']],
          [['press', 'gd-event-history[data-kind=tournament] .gd-event-history__title']],
        ],
      ],
    },
  ),
  tab(
    'events-tournament-link',
    "A tournament's own link: its drawer open",
    'tournament-daily-pc_movie-20261120',
    'events/tournaments/daily-pc_movie-20261120',
    // the legacy hall's smooth scroll to the row ends about 2.2 s after loading, later when the machine is busy
    { settle: 5000 },
  ),

  // ---- Server Info ----
  tab('info-stats', 'Server Info › Server Stats', 'info/stats', 'info/stats'),
  tab('info-patch-notes', 'Server Info › Patch Notes, then the month list', 'info/patch-notes', 'info/patch-notes', {
    steps: [['Months', [['press', '.event-history-month-title']], [['press', '.gd-notes__month-title']]]],
  }),
  tab('info-contribute', 'Server Info › Contribute', 'info/contribute', 'info/contribute'),
  tab('info-about', 'Server Info › About', 'info/about', 'info/about'),

  // ---- My Account ----
  tab('account-overview', 'My Account, then the player picker', 'account/overview', 'account', {
    // the legacy page draws the name and the pocket once the player has answered, later when the machine is busy
    settle: 5000,
    steps: [['Player picker', [['press', '.player-picker-toggle']], [['press', '.gd-player-picker__toggle']]]],
  }),
  tab('account-history', 'My Account › My Game History', 'account/history', 'account/history'),
  tab('account-stats', 'My Account › My Stats', 'account/stats', 'account/stats'),
  tab(
    'account-scenarios',
    'My Account › My Scenarios, Copy Public…, then a Delete question',
    'account/scenarios',
    'account/scenarios',
    {
      steps: [
        [
          'Copy Public...',
          [['press', 'button:has-text("Copy Public...")']],
          [['press', 'button:has-text("Copy Public...")']],
        ],
        ['Close', [['escape', 'body']], [['escape', 'body']]],
      ],
    },
  ),
];
