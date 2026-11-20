/** The hall's chat room (HallServer: createChatRoom("Game Hall", ...)). */
export const HALL_CHAT_ROOM = 'Game Hall';

/** chat.js chatErrorMap: the warning for each failed chat call; the chat stops after any of them. */
export const CHAT_CONNECTION_LOST = 'Chat server has been closed or there was a problem with your internet connection.';
/** Shown with a "Click here to register or log in." link (ChatLine.loginLink). */
export const CHAT_NOT_LOGGED_IN = 'You are not logged in.';
export const CHAT_LOGIN_LINK = 'Click here to register or log in.';
export const CHAT_NO_PERMISSION = 'You have no permission to participate in this chat.';
export const CHAT_ROOM_CLOSED = 'Chat room is closed.';
export const CHAT_INACTIVE =
  'You have been inactive for too long and were removed from the chat room. Refresh the page if you wish to re-enter.';
/** Any other failure (the legacy text read "occured"). */
export const chatProblem = (status: number) => `Unknown chat problem occurred (error=${status})`;

/** The message box. */
export const CHAT_PLACEHOLDER = 'Message the game hall...';
export const CHAT_PLACEHOLDER_LOGGED_OUT = 'Log in to chat';

/** The user list's status line (shown for USER_LIST_STATUS_MS). */
export const ignoredStatus = (name: string) => `Ignored ${name}.`;
export const unignoredStatus = (name: string) => `Unignored ${name}.`;
export const STATUS_UNREACHABLE = 'Could not reach the server.';
export const STATUS_NOT_LOGGED_IN = 'You are not logged in.';
export const STATUS_IGNORE_FAILED = 'Could not change your ignore list. Try again later.';
export const STATUS_NOT_IN_CHAT = 'You are not in the hall chat. Reload the page and try again.';
export const STATUS_VISIBILITY_FAILED = 'Could not change your visibility. Try again later.';
export const STATUS_INCOGNITO = 'No longer showing as online';
export const STATUS_ONLINE = 'Now showing as online';

/** The own entry's chip and its tooltip. */
export const INCOGNITO_TITLE =
  'Hidden from the user list (admins still see you). Click to show as online. Resets when you re-enter the hall.';
export const ONLINE_TITLE = 'Shown in the user list. Click to go incognito (hidden from everyone but admins).';

/** The Discord chat the hall can show instead (chat.js handleChatVisibility: a WidgetBot embed). */
export const DISCORD_WIDGET = {
  script: 'https://cdn.jsdelivr.net/npm/@widgetbot/html-embed',
  server: '699957633121255515',
  channel: '873065954609881140',
  shard: 'https://emerald.widgetbot.io',
} as const;
export const SWITCH_TO_DISCORD = 'Switch to Discord';
export const SWITCH_TO_LEGACY = 'Switch to Legacy';
/** localStorage key of the choice: "discord" or "builtin" (Discord until the player chooses). */
export const CHAT_CHOICE_KEY = 'gemp.hallChat';
