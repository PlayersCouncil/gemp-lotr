/**
 * What the page shows before the game starts (the server's PGS event): the table's summary, and each side's name,
 * deck notes and cards shown beforehand (a map, RTMD meta-sites). Pure.
 */
import { PreGameInfo } from '../state/game-model';

export interface PreGameCard {
  blueprintId: string;
  /** A meta-site's modifier, drawn over the lower part of its card. */
  overlayBlueprintId: string | null;
  label: 'Map' | 'Meta-Site';
}

export interface PreGameView {
  /** The summary as the server wrote it (HTML), with the page's two reminders after it. */
  summary: string;
  left: { name: string; cards: readonly PreGameCard[]; notes: string };
  right: { name: string; cards: readonly PreGameCard[] };
}

const REMINDERS =
  '<br><br>Please be courteous to your fellow players; we are all here to have fun. :)' +
  "<br><br>If you need to leave for any reason, please remember to concede ('Options' tab → 'Concede game' button).";

/** "player:value,player:value" as a map. */
function perPlayer(text: string): Record<string, string> {
  const result: Record<string, string> = {};
  for (const entry of text.split(',').filter(Boolean)) {
    const colon = entry.indexOf(':');
    result[entry.slice(0, colon)] = entry.slice(colon + 1);
  }
  return result;
}

/** Each meta-site's modifier and the card it is drawn on: "player:visual|modifier;visual|modifier,player:…". */
export function metaSitesOf(metaSites: string): Record<string, { visual: string; modifier: string }[]> {
  return Object.fromEntries(
    Object.entries(perPlayer(metaSites)).map(([player, pairs]) => [
      player,
      pairs.split(';').map((pair) => {
        const [visual = '', modifier = ''] = pair.split('|');
        return { visual, modifier };
      }),
    ]),
  );
}

export function preGameView(info: PreGameInfo): PreGameView {
  // The page's player on the left; a spectator sees the first player there
  const plays = info.participantId !== null && info.players.includes(info.participantId);
  const left = plays ? (info.participantId ?? '') : (info.players[0] ?? '');
  const right = plays ? (info.players.filter((player) => player !== left).at(-1) ?? '') : (info.players[1] ?? '');
  const maps = perPlayer(info.maps);
  const sites = metaSitesOf(info.metaSites);
  const cards = (player: string): PreGameCard[] => [
    ...(maps[player] ? [{ blueprintId: maps[player], overlayBlueprintId: null, label: 'Map' as const }] : []),
    ...(sites[player] ?? []).map(({ visual, modifier }) => ({
      blueprintId: visual,
      overlayBlueprintId: modifier,
      label: 'Meta-Site' as const,
    })),
  ];
  return {
    summary: info.summary + REMINDERS,
    left: { name: left, cards: cards(left), notes: info.notes },
    right: { name: right, cards: cards(right) },
  };
}
