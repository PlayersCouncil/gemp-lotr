import { LeagueDetail, LeagueSerie, LeagueStanding, RtmdMetaSite, RtmdPath, TournamentDetail } from './events-models';
import { soloDraftUrl } from './events-parsers';
import { CONSTRUCTED_PREFIX, RETIRED, rtmdAdvance, serieHeading } from './events-messages';

/**
 * What a league's and a tournament's detail show, worked out from the data (leagueResultsUi.js loadedLeague and
 * createStandingsTable, tournamentResultsUi.js loadedTournament). The view draws it.
 */

/** The membership line of a league's detail. */
export type LeagueMembership =
  | { readonly kind: 'member'; readonly draftUrl: string | null }
  | { readonly kind: 'joinable' }
  | { readonly kind: 'inviteOnly' }
  | { readonly kind: 'none' };

/**
 * loadedLeague's membership block. `showMembership` false (a finished league, in Completed leagues) shows none
 * whatever the server says.
 */
export function leagueMembership(league: LeagueDetail, showMembership: boolean): LeagueMembership {
  if (!showMembership) {
    return { kind: 'none' };
  }
  if (league.member) {
    return { kind: 'member', draftUrl: league.draftable ? soloDraftUrl(league.code) : null };
  }
  if (league.joinable) {
    return { kind: 'joinable' };
  }
  return league.inviteOnly ? { kind: 'inviteOnly' } : { kind: 'none' };
}

/** A serie's lines above the results tabs: "name - start to end", its format and its collection. */
export interface SerieSummary {
  readonly heading: string;
  /** "Constructed <format>" unless limited, "(retired)" after a format that no longer exists. */
  readonly format: string;
  /** The format's code, when its description can be shown (the format still exists). */
  readonly formatCode: string | null;
  readonly collection: string;
}

export function serieSummary(serie: LeagueSerie): SerieSummary {
  return {
    heading: serieHeading(serie.name, serie.start, serie.end),
    format: (serie.limited ? '' : CONSTRUCTED_PREFIX) + serie.format + (serie.formatAvailable ? '' : RETIRED),
    formatCode: serie.formatAvailable ? serie.formatCode : null,
    collection: serie.collection,
  };
}

/**
 * createStandingsTable: a league's standings in two halves side by side, the first ceil(n / 2) on the left, the
 * rest on the right. One row per left entry; the right is null where it runs out.
 */
export function standingsHalves(
  standings: readonly LeagueStanding[],
): { readonly left: LeagueStanding; readonly right: LeagueStanding | null }[] {
  const half = Math.ceil(standings.length / 2);
  return standings.slice(0, half).map((left, i) => ({ left, right: standings[half + i] ?? null }));
}

/** A meta-site's card of the RTMD path, and how it is marked for the viewer. */
export interface RtmdCard {
  readonly site: RtmdMetaSite;
  /** current: the viewer's level; passed: an earlier one still in force (cumulative mode); none. */
  readonly mark: 'current' | 'passed' | 'none';
}

/** The RTMD block's facts. */
export interface RtmdSummary {
  readonly playerPosition: number | null;
  readonly pathLength: number;
  /** "Advance every n points / win(s)", only when n > 1. */
  readonly advance: string | null;
  readonly cumulative: boolean;
  readonly cards: readonly RtmdCard[];
}

export function rtmdSummary(path: RtmdPath): RtmdSummary {
  const position = path.playerPosition;
  return {
    playerPosition: position,
    pathLength: path.pathLength,
    advance: path.advanceFactor > 1 ? rtmdAdvance(path.advanceFactor, path.advancementMode) : null,
    cumulative: path.cumulative,
    cards: path.metaSites.map((site) => ({
      site,
      mark:
        position === null
          ? 'none'
          : site.position === position
            ? 'current'
            : path.cumulative && site.position < position
              ? 'passed'
              : 'none',
    })),
  };
}

/**
 * loadedTournament's lines: "Rounds:" first where the drawer asks for it (the Current Tournaments rows and the
 * calendar; not Completed tournaments), Format and Collection, and "Round:" while games are being played.
 */
export interface TournamentLines {
  readonly rounds: string | null;
  readonly format: string;
  readonly collection: string;
  readonly round: string | null;
}

export function tournamentLines(tournament: TournamentDetail, showRounds: boolean): TournamentLines {
  return {
    rounds: showRounds ? tournament.round : null,
    format: tournament.format,
    collection: tournament.collection,
    round: tournament.playingGames ? tournament.round : null,
  };
}
