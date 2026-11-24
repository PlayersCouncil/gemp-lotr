import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { EditBoard, EditLevel } from './edit-board';
import { EditOp } from './edit-ops';

/**
 * Where the on-table edit controls' changes go, for one level (TableEdit picks the backend whose `level` the scenario's
 * info names). The controls never talk to a server themselves: they call TableEdit, and TableEdit calls the backend.
 * Each call answers the table as the level now knows it (an EditBoard); a refusal is an error (an ApiError whose
 * `said` is the server's words, or any Error whose message is to be shown as it is).
 *
 * The sandbox's (SandboxBackend) sends every change to the server at once. A puzzle's would hold the picks on the page
 * and send them with Submit; an author's would translate the edits into the authoring service's board changes. A
 * level that has no such thing as Stop, Start or Reset answers those with an error.
 */
export abstract class TableEditBackend {
  abstract readonly level: Exclude<EditLevel, 'none'>;
  /** The table as the level knows it now. */
  abstract state(gameId: string): Observable<EditBoard>;
  /** Makes the edits, in order, all or none. */
  abstract apply(gameId: string, ops: readonly EditOp[]): Observable<EditBoard>;
  /** Leaves play for edit mode. */
  abstract stop(gameId: string): Observable<EditBoard>;
  /** Leaves edit mode for play. */
  abstract start(gameId: string): Observable<EditBoard>;
  /** A fresh game of the scenario as it opened: the new game's id, which the page goes to. */
  abstract reset(gameId: string): Observable<string>;
  /** Submit (a puzzle's): a fresh game built with what the level holds, which the page goes to; its id. Optional. */
  submit?(gameId: string): Observable<string>;
}

/**
 * The backends of the page, one per level (a multi provider: `{ provide: TABLE_EDIT_BACKENDS, useExisting:
 * SandboxBackend, multi: true }`). provideTableEdit gives the sandbox's; a new level adds its own beside it.
 */
export const TABLE_EDIT_BACKENDS = new InjectionToken<readonly TableEditBackend[]>('TABLE_EDIT_BACKENDS');
