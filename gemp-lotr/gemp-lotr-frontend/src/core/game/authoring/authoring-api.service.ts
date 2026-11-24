import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { FormData } from '../../api/form-codec';
import { SERVER_URL } from '../../platform/app-paths';
import { PageExit } from '../../platform/page-exit';
import { AuthoringResult, AuthoringState, CheckReport, FoundCard, OffScriptReport } from './authoring-state';
import { StepOp } from './step-ops';
import { OpponentPatch } from './opponent-orders';

const AUTHOR = '/scenarios/author';

/** What `edit` changes: top-level parts of the file (null: taken out), the same as the file's text, and steps. */
export interface DraftPatch {
  top?: Readonly<Record<string, unknown>>;
  topText?: Readonly<Record<string, string>>;
  steps?: readonly StepOp[];
  /** A change to the opponent that leaves the orders it does not name as they are (opponent-orders.ts). */
  opponent?: OpponentPatch;
}

/** What `settings` changes: the panel's ticks, the forks' choices, and that the left-out parts have been seen. */
export interface AuthoringSettings {
  recording?: boolean;
  askOpponent?: boolean;
  way?: readonly string[];
  seenLeftOut?: boolean;
}

/** How Save files the draft: in place; or a copy, or the file moved, to `path`. */
export type SaveHow = { as?: undefined } | { as: 'copy' | 'rename'; path: string; canonical: boolean };

const flag = (value: boolean | undefined) => (value === undefined ? null : String(value));

/**
 * The server calls of the scenario authoring panel, one per endpoint of /scenarios/author/ (ScenarioRequestHandler),
 * typed. A change answers an AuthoringResult; what to do with it is the Authoring service's. A refusal is an ApiError
 * whose `said` is the server's reason. Page-scoped.
 */
@Injectable()
export class AuthoringApi {
  private readonly http = inject(GempHttp);
  private readonly exit = inject(PageExit);

  private get<T>(what: string, data: FormData = {}): Observable<T> {
    return this.http.getJson<T>(`${AUTHOR}/${what}`, data);
  }

  private post<T>(what: string, data: FormData = {}): Observable<T> {
    return this.http.postJson<T>(`${AUTHOR}/${what}`, data);
  }

  /** The draft and its game (`{session: false}` with none open). */
  state(): Observable<AuthoringState> {
    return this.get('state');
  }

  /** The draft as its file would be written. */
  source(): Observable<{ source: string }> {
    return this.get('source');
  }

  /** Cards by name (two letters at least), or blueprint id; sites only, or everything else. */
  cards(query: string, sites = false): Observable<{ cards: readonly FoundCard[] }> {
    return this.get('cards', { q: query, sites: sites ? 'true' : null });
  }

  /** The pictures a message can show large that are not cards. */
  images(): Observable<{ images: readonly { name: string; url: string }[] }> {
    return this.get('images');
  }

  /** Opens a scenario's file as the draft (the hall's call): `id`; or a new one with that id and title. */
  open(id: string, created?: { title: string; canonical: boolean }): Observable<AuthoringResult> {
    return this.post(
      'open',
      created ? { id, new: 'true', title: created.title, canonical: String(created.canonical) } : { id },
    );
  }

  /** Changes the draft; `goto`: a fresh game at that step, whether or not the game in hand could take the change. */
  edit(patch: DraftPatch, goto: number | null = null): Observable<AuthoringResult> {
    return this.post('edit', { patch: JSON.stringify(patch), goto });
  }

  /** A fresh game, played by the script up to the step (from 1); `sub`: up to that one of a block's steps (from 1). */
  goTo(step: number, sub: number | null = null): Observable<AuthoringResult> {
    return this.post('goto', { step, sub: sub === 0 ? null : sub });
  }

  /** A player's game of the draft from the step. */
  test(step: number): Observable<AuthoringResult> {
    return this.post('test', { step });
  }

  /** Back to writing from a test. */
  resume(): Observable<AuthoringResult> {
    return this.post('resume');
  }

  /** Answers the opponent's held decision with a term, written into the script or not. */
  answer(term: string, record: boolean): Observable<AuthoringResult> {
    return this.post('answer', { term, record: String(record) });
  }

  /** The opponent does the least it can, unwritten. */
  answerLeast(): Observable<AuthoringResult> {
    return this.post('answer', { least: 'true', record: 'false' });
  }

  /** Changes the board: `do` terms, to the game in hand or (`setup`) to the board the scenario starts with. */
  apply(ops: readonly string[], setup: boolean): Observable<AuthoringResult> {
    return this.post('do', { op: ops.join(', '), setup: String(setup) });
  }

  /** Renames a card of the scenario. */
  alias(from: string, to: string): Observable<AuthoringResult> {
    return this.post('alias', { from, to });
  }

  /** A step for the decision that is waiting, written where the script stands. */
  here(): Observable<AuthoringResult> {
    return this.post('here');
  }

  /** A step put in before the one in hand, for the decision it has. */
  before(): Observable<AuthoringResult> {
    return this.post('before');
  }

  /** The step in hand takes what it allows, and its answer, from what is done next. */
  retake(): Observable<AuthoringResult> {
    return this.post('retake');
  }

  settings(settings: AuthoringSettings): Observable<AuthoringResult> {
    return this.post('settings', {
      recording: flag(settings.recording),
      askOpponent: flag(settings.askOpponent),
      way: settings.way === undefined ? null : settings.way.join(','),
      seenLeftOut: settings.seenLeftOut ? 'true' : null,
    });
  }

  /** Writes the draft to its file; or a copy, or moves it (`rename`: the answer has `renamed` and no `saved`). */
  save(how: SaveHow = {}): Observable<AuthoringResult> {
    return this.post(
      'save',
      how.as === undefined ? {} : { as: how.as, path: how.path, canonical: String(how.canonical) },
    );
  }

  /** Puts the saved draft up for review; the draft is closed. */
  submit(): Observable<{ ok: true; submitted: string }> {
    return this.post('submit');
  }

  /** Whether the draft plays through as written. */
  check(): Observable<CheckReport> {
    return this.post('check');
  }

  /** Whether every other answer the player could give is handled. */
  offScript(): Observable<OffScriptReport> {
    return this.post('offscript');
  }

  /** Closes the draft (what is not saved is lost). */
  close(): Observable<{ ok: true }> {
    return this.post('close');
  }

  /**
   * The page with the draft is going away (sent as it goes, so the hall stops offering a draft that was left); false
   * when the browser can't send it.
   */
  left(): boolean {
    return this.exit.beacon(`${SERVER_URL}${AUTHOR}/left`, { left: 'true' });
  }
}
