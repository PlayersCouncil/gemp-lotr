import { Provider } from '@angular/core';
import { FormatsService } from '../../formats/formats.service';
import { BoardChanges } from '../authoring/board-changes.service';
import { AuthorBackend } from './author-backend';
import { CardSearch, CardSearchApi } from './card-search';
import { EditSeating } from './edit-seating.service';
import { PuzzleBackend } from './puzzle-backend';
import { RecentCards } from './recent-cards';
import { SandboxBackend } from './sandbox-backend';
import { TABLE_EDIT_BACKENDS } from './table-edit-backend';
import { TableEdit } from './table-edit.service';
import { TableFlip } from './table-flip.service';

/**
 * The table changed by hand (TableEdit) with the sandbox's backend, the card search and the cards used lately, for the
 * game page's providers (provideGameState). Another level's backend is added beside the sandbox's:
 * `{ provide: TABLE_EDIT_BACKENDS, useExisting: PuzzleBackend, multi: true }`.
 */
export function provideTableEdit(): Provider[] {
  return [
    TableEdit,
    SandboxBackend,
    { provide: TABLE_EDIT_BACKENDS, useExisting: SandboxBackend, multi: true },
    RecentCards,
    CardSearchApi,
    CardSearch,
    FormatsService,
    EditSeating,
    PuzzleBackend,
    { provide: TABLE_EDIT_BACKENDS, useExisting: PuzzleBackend, multi: true },
    // (the author's: the Board tab's changes, made from the table)
    BoardChanges,
    AuthorBackend,
    { provide: TABLE_EDIT_BACKENDS, useExisting: AuthorBackend, multi: true },
    TableFlip,
  ];
}
