import { ChatHighlight, ChatUser, TournamentAnnouncement } from './chat-models';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/** A message's time as the hall shows it before the sender, in the viewer's time zone: "Nov 9 08:05:03". */
export function formatChatTimestamp(ms: number): string {
  const date = new Date(ms);
  const two = (n: number) => String(n).padStart(2, '0');
  return (
    `${MONTHS[date.getMonth()]} ${date.getDate()} ` +
    `${two(date.getHours())}:${two(date.getMinutes())}:${two(date.getSeconds())}`
  );
}

const EVERYONE = /(@everyone|@anyone)/;

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Whether a message mentions the viewer (chat.js appendMessage): "@<name>" is a ping; the bare name (as a word),
 * "@everyone" or "@anyone" a mention. Case-sensitive, as in the legacy chat. Tested on the message only, not on the
 * sender in front of it.
 */
export function highlightOf(html: string, userName: string | null): ChatHighlight | null {
  if (userName) {
    const name = escapeRegExp(userName);
    if (new RegExp(`@${name}\\b`).test(html)) {
      return 'ping';
    }
    if (new RegExp(`\\b${name}\\b`).test(html)) {
      return 'mention';
    }
  }
  return EVERYONE.test(html) ? 'mention' : null;
}

/** The sender as shown: both tournament senders show as "TournamentSystem". */
export function displayedSender(from: string): string {
  return from.startsWith('TournamentSystem') ? 'TournamentSystem' : from;
}

/**
 * A tournament message's dialog (hallUi.js tournamentCallback): "TournamentSystem" goes to every player in a queue,
 * "TournamentSystemTo:<a>;<b>" to the players named. Null for other senders.
 */
export function tournamentAnnouncement(from: string, html: string): TournamentAnnouncement | null {
  if (from === 'TournamentSystem') {
    return { message: html, recipients: null };
  }
  if (from.startsWith('TournamentSystemTo:')) {
    return { message: html, recipients: from.slice('TournamentSystemTo:'.length).split(';') };
  }
  return null;
}

/** Whether a name is on the ignore list (ignoring case, as the server matches). */
export function isIgnored(ignored: readonly string[], name: string): boolean {
  const lower = name.toLowerCase();
  return ignored.some((n) => n.toLowerCase() === lower);
}

/** The user list in the order shown: the viewer's own entry first, then the others in server order (by name). */
export function orderUsers(users: readonly ChatUser[]): ChatUser[] {
  return [...users.filter((u) => u.self), ...users.filter((u) => !u.self)];
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Server text (an <error>/<response> message, a tournament announcement) as safe markup (GempLotrHallUI.serverMarkup):
 * escaped, then only the bare formatting tags the server writes (<b>, <strong>, <i>, <em>, <u>, <br>) are put back.
 */
export function serverMarkup(text: string | null): string {
  if (text === null) {
    return '';
  }
  return escapeHtml(text)
    .replace(
      /&lt;(\/?)(b|strong|i|em|u)&gt;/gi,
      (_match, slash: string, tag: string) => `<${slash}${tag.toLowerCase()}>`,
    )
    .replace(/&lt;br\s*\/?&gt;/gi, '<br>');
}
