import { attr, boolAttr, childElements, rootElement } from '../api/xml';
import { HallChangeAction, HallItemChange, HallUpdate } from './hall-models';

/**
 * <hall channelNumber currency serverTime serverTimeMs [motd] [shutdown="true"]> with <queue>, <tournament>, <table>
 * (each `action` add|update|remove and `id`, plus their own attributes) and <newGame id> children
 * (HallRequestHandler.SerializeHallInfoVisitor). Null for any other root.
 */
export function parseHallUpdate(doc: Document, full: boolean): HallUpdate | null {
  const root = rootElement(doc, 'hall');
  if (!root) {
    return null;
  }
  return {
    full,
    channelNumber: intOrNull(attr(root, 'channelNumber')),
    currency: intOrNull(attr(root, 'currency')),
    serverTime: attr(root, 'serverTime'),
    serverTimeMs: intOrNull(attr(root, 'serverTimeMs')),
    motd: attr(root, 'motd'),
    shutdown: boolAttr(root, 'shutdown'),
    queues: changes(root, 'queue'),
    tournaments: changes(root, 'tournament'),
    tables: changes(root, 'table'),
    newGames: childElements(root, 'newGame')
      .map((game) => attr(game, 'id'))
      .filter((id): id is string => id !== null && id !== ''),
  };
}

/** The changes of one kind; one with an unknown action or no id is left out, as the legacy hall skipped it. */
function changes(root: Element, tag: string): HallItemChange[] {
  return childElements(root, tag).flatMap((element) => {
    const action = attr(element, 'action');
    const id = attr(element, 'id');
    if (!isAction(action) || id === null) {
      return [];
    }
    const attributes: Record<string, string> = {};
    for (const { name, value } of Array.from(element.attributes)) {
      if (name !== 'action' && name !== 'id') {
        attributes[name] = value;
      }
    }
    return [{ action, id, attributes }];
  });
}

function isAction(value: string | null): value is HallChangeAction {
  return value === 'add' || value === 'update' || value === 'remove';
}

function intOrNull(value: string | null): number | null {
  if (value === null || value === '') {
    return null;
  }
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? null : parsed;
}

/** Epoch ms -> "yyyy-MM-dd HH:mm:ss" in GMT, the way the server writes its clock (DateUtils.getStringDateWithHour). */
export function formatServerTime(ms: number): string {
  const date = new Date(ms);
  const two = (n: number) => String(n).padStart(2, '0');
  return (
    `${date.getUTCFullYear()}-${two(date.getUTCMonth() + 1)}-${two(date.getUTCDate())} ` +
    `${two(date.getUTCHours())}:${two(date.getUTCMinutes())}:${two(date.getUTCSeconds())}`
  );
}
