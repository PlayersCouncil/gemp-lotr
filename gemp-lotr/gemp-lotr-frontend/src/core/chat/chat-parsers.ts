import { attr, childElements, rootElement } from '../api/xml';
import { ChatMessage, ChatRole, ChatSnapshot, ChatUser, IgnoreResult } from './chat-models';

/**
 * The one reader of the `<chat>` answer, for every page's chat (ChatService in the hall, ChatRoom on the game page):
 * <chat roomName><message from date>text</message>*<user [role] [self] [incognito]>name</user>*</chat>. Null for any
 * other root (the legacy chat ignored it).
 */
export function parseChat(doc: Document): ChatSnapshot | null {
  const root = rootElement(doc, 'chat');
  if (!root) {
    return null;
  }
  const messages = childElements(root, 'message').map((message): ChatMessage => {
    const from = attr(message, 'from') ?? '';
    const date = Number(attr(message, 'date') ?? Number.NaN);
    return { from, html: message.textContent, date: Number.isFinite(date) ? date : null, system: from === 'System' };
  });
  return { room: attr(root, 'roomName') ?? '', messages, users: childElements(root, 'user').map(readUser) };
}

/** A <user>; older servers put the role in the name ("* name" admin, "+ name" league admin), which is understood too. */
function readUser(user: Element): ChatUser {
  let name = user.textContent;
  let role = toRole(attr(user, 'role'));
  const prefix = /^([*+]) (.+)$/.exec(name);
  if (prefix) {
    name = prefix[2] ?? name;
    role ??= prefix[1] === '*' ? 'admin' : 'leagueAdmin';
  }
  return { name, role, self: attr(user, 'self') === 'true', incognito: attr(user, 'incognito') === 'true' };
}

function toRole(value: string | null): ChatRole | null {
  return value === 'admin' || value === 'leagueAdmin' ? value : null;
}

/** GET /player/ignores: {ignored: [names]}; anything else is an empty list. */
export function parseIgnored(json: unknown): string[] {
  const ignored = (json as { ignored?: unknown } | null)?.ignored;
  return Array.isArray(ignored) ? ignored.filter((name): name is string => typeof name === 'string') : [];
}

/** POST /player/ignores/add or /remove: {outcome, changed, error, name, message, ignored}. */
export function parseIgnoreResult(json: unknown): IgnoreResult {
  const result = (json ?? {}) as { message?: unknown; ignored?: unknown; changed?: unknown; error?: unknown };
  return {
    message: typeof result.message === 'string' && result.message !== '' ? result.message : null,
    ignored: Array.isArray(result.ignored) ? parseIgnored(result) : null,
    changed: result.changed === true,
    error: result.error === true,
  };
}

/** POST /player/incognito: {inRoom, incognito}; null when it doesn't say. */
export function parseIncognito(json: unknown): boolean | null {
  const incognito = (json as { incognito?: unknown } | null)?.incognito;
  return typeof incognito === 'boolean' ? incognito : null;
}
