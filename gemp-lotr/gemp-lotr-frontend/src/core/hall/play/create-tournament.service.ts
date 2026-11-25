import { inject, Injectable, signal } from '@angular/core';
import { catchError, firstValueFrom, map, Observable, of, Subject } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { escapeHtml, HallDialogs, pageMarkup } from '../hall-dialogs.service';
import { jsonCallResult, tableCallFailure } from '../tables/hall-call';
import { RESULT_READY, RESULT_WORKING } from '../tables/hall-tables-messages';
import { HallDeckChoice, TableCallResult, TableResult } from '../tables/hall-table-models';
import { HallTablesService } from '../tables/hall-tables.service';
import { FormatInfo, limitedFormatInfo } from './format-info';
import {
  ENTER_PLAYER_COUNT,
  MUST_SELECT_TOURNAMENT_DECK,
  TOURNAMENT_CREATED_TITLE,
  tournamentCreated,
  tournamentFormatsFailed,
} from './play-messages';
import {
  canSubmitTournament,
  draftTimerOf,
  initialTournamentForm,
  parseTournamentFormats,
  selectedFormat,
  TournamentFormats,
  TournamentFormState,
  TournamentRequest,
  TournamentType,
  withFormat,
  withPlayers,
  withType,
} from './tournament-form';

/** The constructed deck is remembered per format ("tournament-decks"). */
export const TOURNAMENT_DECK_FLOW = 'tournament';

/** The tournament calls (TournamentRequestHandler). */
@Injectable()
export class TournamentApi {
  private readonly http = inject(GempHttp);

  /** GET /tournament/tournamentFormats: what each type of player-made tournament offers. */
  formats(): Observable<TournamentFormats> {
    return this.http.getJson<unknown>('/tournament/tournamentFormats').pipe(map(parseTournamentFormats));
  }

  /** POST /tournament/create: answers "OK" (JSON), or 400 with {"error"} the player can act on. */
  create(request: TournamentRequest): Observable<TableCallResult> {
    return this.http.postJson<unknown>('/tournament/create', { ...request }).pipe(
      map(jsonCallResult),
      catchError((error: unknown) => of(tableCallFailure(ApiError.from(error)))),
    );
  }
}

/**
 * Play ▸ Create Tournament (CreateTournament): a player-made tournament (Constructed, Sealed, Live Draft or Solo Draft)
 * others sign up for, its creator signed up with it. The formats are asked for once the hall goes live; the form goes
 * back to its defaults each time the Play overlay opens. Success closes the overlay and says what happens next.
 */
@Injectable()
export class CreateTournament {
  private readonly api = inject(TournamentApi);
  private readonly tables = inject(HallTablesService);
  private readonly dialogs = inject(HallDialogs);

  readonly formats = signal<TournamentFormats | null>(null);
  readonly form = signal<TournamentFormState>(initialTournamentForm());
  readonly result = signal<TableResult>({ kind: 'ready', message: RESULT_READY });
  private loading: Promise<void> | null = null;
  private readonly created = new Subject<void>();
  /** A tournament was created: its creator is signed up (the hall's setPendingTournament(true)). */
  readonly created$: Observable<void> = this.created.asObservable();

  /** Once, when the hall goes live (not for a visitor: mainHall.whenStarted). */
  loadFormats(): Promise<void> {
    this.loading ??= firstValueFrom(this.api.formats()).then(
      (formats) => {
        this.formats.set(formats);
        // a type chosen before the formats arrived
        const type = this.form().type;
        if (type !== '') {
          this.form.set(withType(this.form(), type, formats));
        }
      },
      (error: unknown) => {
        this.loading = null;
        const failure = tableCallFailure(ApiError.from(error));
        this.result.set({ kind: 'error', message: tournamentFormatsFailed(failure.message) });
      },
    );
    return this.loading;
  }

  /** Back to the page's defaults (each Play open). */
  resetForm(): void {
    this.form.set(initialTournamentForm());
    this.result.set({ kind: 'ready', message: RESULT_READY });
  }

  setType(type: TournamentType): void {
    this.form.set(withType(this.form(), type, this.formats()));
  }

  setFormat(code: string): void {
    this.form.set(withFormat(this.form(), code, this.formats()));
  }

  setPlayers(players: string): void {
    this.form.set(withPlayers(this.form(), players));
  }

  /** The other fields (pairing, deck-building minutes, competitive, Early Start, ready check, draft timer, advanced). */
  patch(change: Partial<Omit<TournamentFormState, 'type' | 'format' | 'players' | 'small' | 'savedLarge'>>): void {
    this.form.update((form) => ({ ...form, ...change }));
  }

  /** The format (i): a Constructed format's details, else what the format means in Sealed or a draft. */
  formatInfo(): FormatInfo | null {
    const form = this.form();
    if (form.type === '') {
      return null;
    }
    return form.type === 'constructed'
      ? this.tables.formatInfo(form.format || null)
      : limitedFormatInfo(form.type === 'sealed');
  }

  /** The deck the Constructed form starts with or switches to for the chosen format (restoreUnlessChosen). */
  initialDeck(): HallDeckChoice | null {
    return this.tables.initialDeck(TOURNAMENT_DECK_FLOW, this.form().format || null);
  }

  canSubmit(): boolean {
    return canSubmitTournament(this.form());
  }

  /** Host Tournament. True when the overlay should close. */
  async submit(deck: HallDeckChoice | null): Promise<boolean> {
    const form = this.form();
    if (!canSubmitTournament(form)) {
      return false;
    }
    const type = form.type as TournamentType;
    const chosen = type === 'constructed' ? deck : null;
    if (type === 'constructed' && chosen === null) {
      this.result.set({ kind: 'error', message: MUST_SELECT_TOURNAMENT_DECK });
      return false;
    }
    const maxPlayers = parseInt(form.players, 10);
    if (Number.isNaN(maxPlayers) || maxPlayers < 1) {
      this.result.set({ kind: 'error', message: ENTER_PLAYER_COUNT });
      return false;
    }
    if (chosen !== null) {
      this.tables.rememberDeck(TOURNAMENT_DECK_FLOW, chosen, form.format);
    }
    const deckbuilding = parseInt(form.deckbuilding, 10);
    this.result.set({ kind: 'pending', message: RESULT_WORKING });
    const result = await firstValueFrom(
      this.api.create({
        type,
        deckName: chosen?.name ?? '',
        deckSource: chosen?.source ?? '',
        maxPlayers,
        formatCode: form.format,
        tableDraftTimer: draftTimerOf(form, this.formats()),
        playoff: form.pairing,
        deckbuildingDuration: Number.isNaN(deckbuilding) ? '' : deckbuilding,
        competitive: form.competitive,
        startable: form.earlyStart,
        readyCheck: form.readyCheck,
      }),
    );
    this.result.set({ kind: result.ok ? 'success' : 'error', message: result.message });
    if (!result.ok) {
      return false;
    }
    this.created.next();
    const formatName = selectedFormat(form, this.formats())?.name ?? form.format;
    this.dialogs.show(
      TOURNAMENT_CREATED_TITLE,
      pageMarkup(escapeHtml(tournamentCreated(form.competitive, formatName, maxPlayers, form.earlyStart))),
    );
    return true;
  }
}
