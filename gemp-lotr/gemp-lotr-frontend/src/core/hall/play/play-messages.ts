import { orIfEmpty } from '../json-values';

/**
 * The Play overlay's and the Join dialog's wording (hall.html #create-table-popup and #join-table-popup, and the
 * tables/*.js flows): the choice screen, each form's blurb, field labels and the help text its (i) shows.
 * Blurbs that hold emphasis are markup (pageMarkup); everything else is plain text.
 */

/** The overlay's title: "Create Table", then "Create Table ▸ <flow>" (CreateTable.openFlow). */
export const PLAY_TITLE = 'Create Table';
export const playFlowTitle = (name: string) => `${PLAY_TITLE} ▸ ${name}`;
export const BACK = '< Back';
export const RESULT_LABEL = 'Result:';
export const CREATE_TABLE_BUTTON = 'Create Table';
/** The (i) toggles' title. */
export const INFO_TOGGLE_TITLE = 'What is this?';

/** The flows of the choice screen, in order. */
export type PlayFlow = 'scenario' | 'bot' | 'casual' | 'league' | 'tournament';

export interface PlayChoice {
  readonly flow: PlayFlow;
  /** The icon's name (the legacy `icon-<name>` sprite). */
  readonly icon: string;
  readonly title: string;
  readonly subtitle: string;
  /** Its part in the overlay's title ("Create Table ▸ Bot"). */
  readonly flowName: string;
}

export const PLAY_CHOICES: readonly PlayChoice[] = [
  {
    flow: 'scenario',
    icon: 'scenario',
    title: 'Play Scenario or Tutorial',
    subtitle: 'Play an interactive tutorial or a single-player game puzzle.',
    flowName: 'Scenarios & Tutorials',
  },
  {
    flow: 'bot',
    icon: 'bot',
    title: 'Play Against Bots (Beta)',
    subtitle: 'A practice game against a computer opponent.',
    flowName: 'Bot',
  },
  {
    flow: 'casual',
    icon: 'unranked',
    title: 'Open Casual Table',
    subtitle: 'A 1-on-1 game against another player.',
    flowName: 'Casual',
  },
  {
    flow: 'league',
    icon: 'league',
    title: 'Open League Table',
    subtitle: 'Join a multi-week league and participate in themed Sealed or Constructed events of all kinds.',
    flowName: 'League',
  },
  {
    flow: 'tournament',
    icon: 'tournament',
    title: 'Create Tournament',
    subtitle: 'Host an event that other players sign up for.',
    flowName: 'Tournament',
  },
];

// ---- shared by every deck picker ----

export const YOUR_DECK = 'Your Deck:';
export const DECK = 'Deck:';
export const SELECT_LIBRARY_DECK = 'Select Library Deck:';
export const CHOOSE_LIBRARY_DECK = 'Choose a Deck Library deck';
export const HELP_LIBRARY =
  'The Deck Library contains sample decks you can use, including starter decks, past championship decks, and more.';
/** A deck's line in a picker: "[Fellowship Block] - Elves" (Deck.formatDeck). */
export const deckLabel = (formatName: string, deckName: string) => `[${formatName}] - ${deckName}`;

// ---- the format (i) (TableFlow.renderFormatInfo) ----

export const TABLE_FORMAT = 'Table Format:';
export const FORMAT_INTRO =
  "A 'format' is a shorthand for what cards are legal to use and what ruleset is being followed.";
export const CHOOSE_A_FORMAT = 'Choose a format to see what it allows.';
export const formatSites = (sites: string) => `Sites: ${sites}`;
export const ringBearerCancel = (yes: boolean) => `Ring-bearer skirmish cancel: ${yes ? 'Yes' : 'No'}`;
export const compareFormat = (name: string) => `Compare ${name} with the other formats: `;
export const FORMAT_HELP_LINK = 'Help › Format Definitions';
export const SEALED_FORMAT_INFO =
  'Everyone receives the same sealed product for this format and builds a deck from it.';
export const DRAFT_FORMAT_INFO = 'The set of packs drafted from; the deck is built from the cards drafted.';

// ---- Bot (CreateBotTable, #create-bot-table) ----

export const BOT_HEADING = 'Open Bot Table';
export const BOT_BLURB =
  'Welcome to the future!  Here you may open a match against a neural-net bot trained on playing the LOTR-TCG.  ' +
  'This is an experimental feature, and so can go wrong in many ways.\nRemember:' +
  '<ul><li>Bots will concede if they get confused or have an error.</li>' +
  "<li>Bots will sometimes make dumb decisions that a human never would.  Don't give them the blue ATAR ring or they " +
  '<i>will</i> self-corrupt, for example.</li></ul>' +
  "Please report a bug for any matches where you encounter a crash or other concession by the bot.  But don't bother " +
  'reporting bad gameplay; we already know they are dumb. :)';
export const UNLOCK_FORMATS = 'Unlock Formats';
export const BOT_DECK = 'Bot Deck:';
export const DRAGONS_TITLE = 'Here There Be Dragons';
export const DRAGONS_TEXT =
  'WARNING: The bots have only been trained on starters from FOTR block.  As such, they are likely to break when ' +
  'presented with unfamiliar mechanics, which may be as novel as threats or site manipulation, or as mundane as ' +
  "'assigning allies to skirmishes'.<br/><br/>Use formats beyond Fellowship Block at your own risk, and do not be " +
  'surprised if they break.';

// ---- Casual (CreateUnrankedTable, #create-unranked-table) ----

export const CASUAL_HEADING = 'Open Casual Table';
export const CASUAL_BLURB =
  'Open a casual table to play another human 1v1 in your format of choice.\nRemember:<ul>' +
  '<li>If you have to leave early for any reason, go to <b>Options -&gt; Concede game</b> (and then pass your current ' +
  'action, if necessary)</li>' +
  '<li>If you have no deck of your own yet, pick one from <b>Select Library Deck</b>: the Deck Library has many ' +
  'starter, sample, and championship decks from years past.</li>' +
  '<li>Always be courteous to your fellow players! Use the description field to avoid misunderstandings.</li></ul>';
export const DESCRIPTION = 'Description:';
export const DESCRIPTION_MAX_LENGTH = 100;
export const HELP_DESCRIPTION =
  'A short note shown with your table in the hall, for example "new player, go easy" (up to 100 characters). An ' +
  "invite-only table uses the invited player's name here instead.";
/** The description's placeholder while invite-only is on (updateInviteState). */
export const DESCRIPTION_INVITE_PLACEHOLDER = "An invite-only table's description is the invited player's name";
export const GAME_TIMER = 'Game Timer:';
export const INVITE_ONLY = 'Invite Only:';
export const INVITEE_PLACEHOLDER = 'Choose or type a player';
export const INVITEE_LABEL = 'Player to invite';
export const HELP_INVITE =
  'Only the player you name can join. Pick someone who is in the hall, or type to search for any player (for ' +
  'example a friend who is about to log in).';
export const PRIVATE_MODE = 'Private Mode:';
export const HELP_PRIVATE =
  'If checked, nobody can join your game to watch from the sidelines (except administrators).';
export const KEEP_OPEN = 'Keep this window open:';
export const HELP_KEEP_OPEN =
  'After the table is created, stay on this form (to open another table) instead of closing it.';
export const DEFAULT_FLOW = 'Open this flow by default:';
export const HELP_DEFAULT_FLOW =
  'Checking this box makes this screen show immediately when clicking Play.  Click the Back button in the upper left ' +
  'to return to the main menu.';

// ---- the invitee picker (playerPicker.js) ----

export const PICKER_TOGGLE = 'Show the players in the hall';
export const PICKER_IN_HALL = 'In the hall';
export const PICKER_OTHERS = 'Other players';
export const PICKER_NOBODY = 'Nobody else is in the hall. Type a name to search all players.';
export const pickerNoMatch = (term: string) => `No player's name starts with "${term}".`;

// ---- League (CreateLeagueTable, #create-league-table) ----

export const LEAGUE_HEADING = 'Open League Table';
export const HELP_LEAGUE =
  "League games are ranked: each win or loss counts toward the league's standings, within the league's limits on " +
  'games per serie and per opponent. They always use the Competitive timer (40 minutes each, 5 minutes per ' +
  'decision), and cannot be private or invite-only.';
export const LEAGUE_BLURB =
  'Leagues are multi-week or multi-month events where players compete in many matches across days to improve their ' +
  'skills and hone their craft.<br /><br />Join leagues below or using the Events tab at the top of the main hall.  ' +
  'If you enter a Limited league (Sealed or Draft), you will need to receive your cards and construct a new deck ' +
  'using those cards before you can participate.';
export const LEAGUE = 'League:';
export const NO_LEAGUE_YET = 'You have not joined a league yet; join one below';
export const JOIN_LEAGUES_HEADING = 'Join Leagues';
export const LEAGUE_LIST_HINT =
  'All times are server time (UTC / GMT). Open a league for its details, standings and Join button.';
export const MUST_SELECT_LEAGUE = 'You must select a league.  If you are not enrolled in one, join one below.';
export const MUST_SELECT_LEAGUE_DECK =
  'You must select a deck.  Remember that if this is a sealed or draft league, you may only use cards issued by this league.';
export const leaguesFailed = (message: string) => `Could not load your leagues: ${message}`;
export const leagueJoinFailed = (message: string) => `Could not join the league: ${message}`;
export const leagueJoined = (name: string) => `You joined ${name}. Next:`;
export const NEXT_CREATE_TABLE = 'Create a table in this league';
export const NEXT_GO_TO_DRAFT = 'Go to draft';
export const NEXT_OPEN_DECKBUILDER = 'Open the deck builder';
export const NEXT_LIMITED_HINT =
  "This league issues its own cards: in the deck builder, choose the league's collection to open your packs and " +
  'build a deck from them. League tables only accept such decks.';

// ---- Tournament (CreateTournament, #create-tournament) ----

export const TOURNAMENT_HEADING = 'Create Tournament';
export const TOURNAMENT_BLURB =
  'Tournaments are competitive events where players fight to prove their skills against other players!  Anyone may ' +
  'create or enter a tournament, but know that others will be playing to win! :)<ul>' +
  '<li><b>Constructed</b>: Players provide their own constructed decks with no limits beyond the selected format.</li>' +
  '<li><b>Sealed</b>: Players will receive standardized starter decks with digital boosters and 30 minutes (by ' +
  'default) to crack those packs and build a deck for the tournament. </li>' +
  '<li><b>Live Draft</b>: Players will sit around the table and open digital packs, selecting 1 card and passing the ' +
  'rest to their opponents.  After all packs have been drafted, players have 15 minutes (by default) to construct a ' +
  'deck for the tournament.</li>' +
  '<li><b>Solo Draft</b>: Players will each draft against the computer, meaning that they have a limited selection ' +
  'but the remainder is <b>not</b> passed to a human opponent. After drafting, players have 30 minutes (by default) ' +
  'to construct a deck for the tournament.</li></ul>';
export const TOURNAMENT_TYPE = 'Tournament Type:';
export const SELECT_GAME_TYPE = 'Select Game Type';
export const FORMAT = 'Format:';
export const DRAFT_INFO = 'Draft Info:';
export const NUMBER_OF_PLAYERS = 'Number of Players:';
export const HELP_PLAYERS =
  'The tournament starts when this many players have signed up (or earlier, if Early Start is on and you start it). ' +
  'With 2 or fewer players there is no ready check and no pairing choice.';
export const SHOW_ADVANCED = 'Show Advanced Settings';
export const HIDE_ADVANCED = 'Hide Advanced Settings';
export const PAIRING_TYPE = 'Pairing Type:';
export const HELP_PAIRING =
  'Swiss: every round pairs players with similar records, and everyone plays every round; the best record wins. ' +
  'Single-elimination: a loss knocks you out, and the last player standing wins.';
export const DECKBUILDING_DURATION = 'Deck-building Duration (minutes):';
export const HELP_DECKBUILDING =
  'How long players have, after opening their packs or finishing the draft, to build and register a deck before the ' +
  'first round.';
export const COMPETITIVE_MODE = 'Competitive Mode:';
export const COMPETITIVE_NOTE = '(Named "Competitive …" instead of "Casual …"; no spectators)';
export const HELP_COMPETITIVE =
  'A competitive tournament is named "Competitive <format>" instead of "Casual <format>". Its games have no ' +
  'spectators, the game chat is limited to the two players, and player names are hidden while the tournament is ' +
  'still signing players up.';
export const EARLY_START = 'Early Start:';
export const EARLY_START_NOTE = '(Tournament owner can force start without all player slots filled)';
export const HELP_EARLY_START =
  'You, as the first player in the queue, get a Start button in the hall to begin before every seat is filled.';
export const READY_CHECK = 'Ready Check:';
export const HELP_READY_CHECK =
  'When the tournament is about to start, every player must confirm they are present within this time. Players who ' +
  'do not are removed from the queue (and refunded), so nobody starts against an absent opponent.';
export const DRAFT_TIMER = 'Draft Timer:';
export const HELP_DRAFT_TIMER = 'How long each player has to make each pick during the live draft.';
export const HOST_TOURNAMENT = 'Host Tournament';
export const MUST_SELECT_TOURNAMENT_DECK = 'You must select your deck before starting a Constructed tournament.';
export const ENTER_PLAYER_COUNT = 'Enter the number of players (1 or more).';
export const tournamentFormatsFailed = (message: string) => `Could not load the tournament formats: ${message}`;
export const CREATE_TOURNAMENT_TITLE = 'Create Tournament';
export const TOURNAMENT_CREATED_TITLE = 'Tournament Created';
/** The dialog after a tournament was created (escape `formatName` when building markup). */
export const tournamentCreated = (competitive: boolean, formatName: string, maxPlayers: number, early: boolean) =>
  `Your ${competitive ? 'Competitive' : 'Casual'} ${formatName} tournament is open, and you are signed up for it. ` +
  'It is listed in Waiting Tables, where other players can join it; it starts when ' +
  `${maxPlayers} players have joined${early ? ', or earlier when you press its Start button.' : '.'}`;

// ---- Scenarios (PlayScenario, #create-scenario) ----

export const SCENARIO_HEADING = 'Scenarios & Tutorials';
export const SCENARIO_BLURB =
  'Scenarios are single-player puzzles or examples of gameplay.  Tutorial scenarios walk the player through the ' +
  'basics of the game, or explore more advanced topics in an interactive guide.';
export const SCENARIO_SMALL_WINDOW =
  'This window is small for a tutorial, which points at things on the game table.  It will be easier to follow on a ' +
  'desktop or laptop, or with the window made larger.';
export const SCENARIO_FOLDERS_LABEL = 'Scenario folders';
export const SCENARIO_LOADING = 'Loading…';
export const SCENARIO_LIST_FAILED = 'The list of scenarios could not be loaded.';
export const NO_SCENARIOS = 'There are no scenarios to play yet.';
export const IN_THE_MIDDLE_OF = 'You are in the middle of ';
export const CONTINUE = 'Continue';
export const PLAY = 'Play';
export const PLAY_AGAIN = 'Play Again';
export const START_OVER = 'Start Over';
export const SETTING_UP = 'Setting up the table…';
export const SCENARIO_STARTED = 'Started.';
export const SCENARIO_OPENED = 'Opened.';
export const SCENARIO_NOT_STARTED = 'The scenario could not be started.';
export const scenarioStartFailed = (status: number) => `The scenario could not be started (HTTP ${status}).`;
export const scenarioOpenFailed = (status: number) => `The scenario could not be opened for writing (HTTP ${status}).`;
export const UNREACHABLE = 'Could not reach the server. Check your internet connection and try again.';
export const byAuthor = (author: string) => `By ${author}`;
export const NOT_OFFICIAL = 'Not an official scenario';
export const NOT_VERIFIED = 'Not verified: this scenario does not play through from start to finish yet';
export const playedTimes = (attempts: number) => `Played ${attempts} times`;
export const AUTHOR_NOTE_BEFORE = 'Create your own Puzzle or Tutorial Scenarios on ';
export const AUTHOR_LINK = 'My Account > My Scenarios';
export const AUTHOR_NOTE_AFTER = '.';
export const DIRTY_DRAFT_QUESTION =
  'You have a draft open with changes that have not been saved. Opening another drops it, and they are lost.';
export const unreadableHeading = (count: number) =>
  `${count === 1 ? '1 scenario file has' : `${count} scenario files have`} mistakes in ${count === 1 ? 'it' : 'them'}, ` +
  'and cannot be played:';
export const UNREADABLE_DEFAULT_ERROR = 'It could not be read.';
export const EDIT = 'Edit';
export const EDIT_TITLE =
  'Open it in the authoring tool, with the mistaken parts left out. The tool shows what they were.';
export const CANNOT_OPEN_NOTE = 'The authoring tool cannot open this one: the file itself has to be put right.';
/** The result box's line about the unreadable files (sayUnreadable). */
export function unreadableSummary(files: readonly { path: string; errors: readonly string[] }[]): string {
  const [first] = files;
  if (files.length === 1 && first) {
    const errors = first.errors;
    const more = errors.length > 1 ? `  (and ${errors.length - 1} more, above)` : '';
    return `${first.path} cannot be played: ${orIfEmpty(errors[0], 'see above.')}${more}`;
  }
  return `${files.length} scenario files have mistakes in them and cannot be played (they are listed above).`;
}

// ---- the Join dialog (JoinTable, #join-table-popup) ----

export const JOIN_TABLE_BUTTON = 'Join Table';
