import { escapeHtml } from '../hall-dialogs.service';

/** The queues' and tournaments' wording (hallUi.js drawQueue, drawTournament, the ready check; JoinTable). */

/** The label of a breakdown when the server sent only its old HTML (the label it always had). */
export const PRIZE_BREAKDOWN = 'Prize Breakdown';

export const JOIN_QUEUE = 'Join Queue';
export const LEAVE_QUEUE = 'Leave Queue';
export const START_NOW = 'Start Now';
export const SHOW_PRIZES = 'Show Prizes';
export const readyCheckLabel = (secs: number) => `READY CHECK - ${secs} s`;
export const waitingForOthersLabel = (secs: number) => `Waiting for others - ${secs} s`;

export const GO_TO_DRAFT = 'Go to Draft';
export const REGISTER_DECK = 'Register Deck';
export const ABANDON_TOURNAMENT = 'Abandon Tournament';
export const JOIN_TOURNAMENT = 'Join Tournament';
/** The question before Abandon Tournament. */
export const abandonQuestion = (name: string) =>
  `Are you sure you want to resign from the ${name} tournament? This cannot be undone.`;

/** The prize breakdown dialog (showPrizes). */
export const PRIZES_TITLE = 'Prizes details';
export const NO_PRIZE_INFO = 'No prize information available.';

/** A tournament of more than 8 players: its count opens the list. */
export const COMPETING_PLAYERS_TITLE = 'Competing Players';
export const ABANDONED_NOTE = '* = abandoned';
/** The draft format button's title. */
export const DRAFT_FORMAT_TITLE = 'Show the draft format';

/** The ready check (showReadyCheckDialog). */
export const READY_CHECK_TITLE = 'Ready Check';
export const READY_CHECK_READY = 'Ready';
export const READY_CHECK_OK = 'OK';
/** "Ready Check started for the <b>name</b> tournament." then "Confirm you are present within N seconds." */
export const readyCheckText = (queueName: string, seconds: number) =>
  `<p>Ready Check started for the <b>${escapeHtml(queueName)}</b> tournament.</p>` +
  `<p>Confirm you are present within ${seconds} seconds.</p>`;

/** processResponse's dialog titles. */
export const RESPONSE_ERROR_TITLE = 'Error';
export const RESPONSE_TITLE = 'Response';

export const TOURNAMENT_UPDATE_TITLE = 'Tournament Update';
export const JOINED_TOURNAMENT_TITLE = 'Joined Tournament';
export const DECK_REGISTERED_TITLE = 'Deck Registered';

/** The Join dialog's titles and headings (JoinTable). */
export const JOIN_TABLE = 'Join Table';
export const joinQueueTitle = (name: string) => `Join Queue: ${name || 'tournament'}`;
export const joinTournamentTitle = (name: string) => `Join Tournament: ${name || 'tournament'}`;
export const registerDeckTitle = (name: string) => `Register Deck: ${name || 'tournament'}`;
export const REGISTER_DECK_SUBTITLE = "Choose the deck you built from this tournament's cards.";
export const MUST_SELECT_DECK_SHORT = 'You must select a deck.';

/** "Format: X · Starts: Y" under the Join Queue heading (JoinTable.describeEvent). */
export function describeEvent(format: string, start: string): string {
  const parts: string[] = [];
  if (format) {
    parts.push(`Format: ${format}`);
  }
  if (start) {
    parts.push(`Starts: ${start}`);
  }
  return parts.join(' · ');
}

/** "at 2026-09-24 18:00" / "When 8 players join" made readable after "The first game begins" (JoinTable.startPhrase). */
export function startPhrase(start: string): string {
  if (/^at /i.test(start) || /^when /i.test(start)) {
    return escapeHtml(start.charAt(0).toLowerCase() + start.substring(1));
  }
  return `at ${escapeHtml(start)}`;
}

/** The confirmation after a queue join (JoinTable.queueJoinedMessage), as markup with every server value escaped. */
export function queueJoinedMessage(
  type: string,
  queueName: string,
  deckName: string | null,
  queueStart: string,
): string {
  const start = `You have signed up to participate in the <b>${escapeHtml(queueName)}</b> tournament.<br><br>`;
  const lockIn =
    'At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>';
  const draftThenBuild =
    "When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. ";
  switch (type) {
    case 'sealed':
      return (
        start +
        'When the event begins, you will be issued sealed packs to open and make a deck. ' +
        lockIn +
        `Deck building begins at ${escapeHtml(queueStart)}. Good luck!`
      );
    case 'solodraft':
    case 'table_solodraft':
      return start + draftThenBuild + lockIn + `Deck building begins at ${escapeHtml(queueStart)}. Good luck!`;
    case 'table_draft':
      return start + draftThenBuild + lockIn + `Draft begins at ${escapeHtml(queueStart)}. Good luck!`;
    default:
      return (
        start +
        `You will use a snapshot of your '<b>${escapeHtml(deckName)}</b>' deck as it is right now. ` +
        'If you need to change or update your deck, you will need to leave the queue and rejoin.<br><br>' +
        `The first game begins ${startPhrase(queueStart)}. Good luck!`
      );
  }
}

/** A flow's dialog title when it failed while its result box was not on screen (TableFlow.respond). */
export const failedTitle = (title: string) => `${title} failed`;
