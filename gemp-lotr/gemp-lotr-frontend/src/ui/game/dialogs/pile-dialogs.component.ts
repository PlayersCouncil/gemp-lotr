import { Component, computed, effect, inject, untracked } from '@angular/core';
import { displayName } from '../../../core/game/scenario/scenario-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { boardPileCards, inBoardOrder, pageHasPile, pileSpots } from '../../../core/game/table-edit/edit-piles';
import { EditPlace } from '../../../core/game/table-edit/edit-ops';
import { seatOf } from '../../../core/game/table-edit/edit-seats';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { handRevealed, pileCards, pileTitle } from '../../../core/game/table/game-table';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';
import { CardComponent } from '../../shared/card/card.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { CardWindowDirective } from '../../shared/fit-cards/card-window.directive';
import { OpenSpotComponent } from '../table-edit/open-spot.component';
import { GameCardDirective } from '../table/game-card.directive';

/** How long the game is let move before a pile the page is not shown is read again (ms). */
export const PILE_READ_MS = 250;

const UPRIGHT: FitCard = { horizontal: false };

/**
 * The piles the player opened from the counters (a discard pile, a dead pile, the adventure deck, the removed pile,
 * the draw deck once the game has ended, a revealed hand), each in a dialog of its own titled "<pile> - <player>",
 * that stays open beside the game until closed. The cards fill it as the legacy groups did; a revealed hand's dialog is
 * sized to its cards instead (CardWindowDirective; Ketura, 2026-11-19).
 *
 * In a sandbox every pile of both seats opens (a hand as it lies, every pile top first, as the sandbox orders them). A pile the page's own game
 * view is not shown (the hidden ones, with the sandbox's `piles` tool) comes from the table as the sandbox knows it
 * (TableEdit.board), read again when its window opens and whenever the game moves while it is open (and after every
 * edit). While the table is edited, each window has the open spots of its pile (a draw deck: its top first, its bottom
 * last; none for an adventure deck) and its cards the card menu, as on the table.
 *
 * DOM contract: `gd-dialog`s with `data-dialog=pile`, each holding `.gd-game-pile` (the fitCards box; `data-pile`
 * and `data-seat` in a sandbox, `data-source` page or board) with a `button.gd-card` per card (see GameCardDirective)
 * and, while edited, `gd-open-spot`s (see OpenSpotComponent).
 */
@Component({
  selector: 'gd-game-piles',
  styleUrl: '../decision/decision-dialogs.component.css',
  imports: [
    DialogComponent,
    CardComponent,
    FitCardsDirective,
    CardWindowDirective,
    GameCardDirective,
    OpenSpotComponent,
  ],
  host: { style: 'display: contents' },
  template: `
    @for (dialog of dialogs(); track dialog.key) {
      <gd-dialog
        name="pile"
        [heading]="dialog.title"
        [modal]="false"
        [focusSelf]="true"
        [open]="true"
        (closed)="view.closePile(dialog.pile, dialog.player)"
      >
        <div
          class="gd-game-decision__cards gd-game-pile"
          [attr.data-pile]="dialog.sandbox ? dialog.pile : null"
          [attr.data-seat]="dialog.seat"
          [attr.data-source]="dialog.sandbox ? (dialog.fromBoard ? 'board' : 'page') : null"
          [gdFitCards]="dialog.fit"
          [gdFitCardsRoundedFrom]="roundedFrom"
          [gdCardWindow]="dialog.pile === 'revealedHand' ? dialog.fit : null"
        >
          @for (where of dialog.first; track where.zone) {
            <gd-open-spot [where]="where" />
          }
          @for (card of dialog.cards; track card.id) {
            <button [gdCard]="views.of(card)" [gdGameCard]="card"></button>
          }
          @for (where of dialog.last; track where.zone) {
            <gd-open-spot [where]="where" />
          }
        </div>
      </gd-dialog>
    }
  `,
})
export class PileDialogsComponent {
  private readonly session = inject(GameSession);
  protected readonly view = inject(GameViewState);
  protected readonly views = inject(GameCardViews);
  private readonly edit = inject(TableEdit);
  private readonly seating = inject(EditSeating);
  private readonly lifetime = inject(PageLifetime);
  /** As the decisions' dialog of cards: the legacy group was sized from the content's height in whole pixels. */
  protected readonly roundedFrom = 8 + 13.2 - 2;

  /** Each window open, where its cards come from (the page's own view, or the sandbox's board) and its seat. */
  private readonly sources = computed(() => {
    const model = this.session.model();
    const sandbox = this.edit.level() === 'sandbox';
    const seats = this.seating.seats();
    return this.view.pileDialogs().map(({ pile, player }) => {
      const own = !model.spectator && player === model.participantId;
      const seat = sandbox ? seatOf(seats, player) : null;
      const fromBoard = seat !== null && !pageHasPile(pile, own, model.discardPublic, handRevealed(model, player));
      return { pile, player, own, seat, sandbox, fromBoard };
    });
  });

  /** The windows that show a pile from the sandbox's board (the board is read for them), by key. */
  private readonly fromBoard = computed(() =>
    this.sources()
      .filter((source) => source.fromBoard)
      .map((source) => `${source.pile}:${source.player}`)
      .join(),
  );

  protected readonly dialogs = computed(() => {
    const model = this.session.model();
    const board = this.edit.board();
    const adding = this.edit.may('add');
    return this.sources().map(({ pile, player, own, seat, sandbox, fromBoard }) => {
      const cards =
        fromBoard && seat !== null
          ? boardPileCards(board, pile, seat, player)
          : sandbox
            ? inBoardOrder(pileCards(model, pile, player), board)
            : pileCards(model, pile, player);
      const spots: EditPlace[] = adding && seat !== null ? pileSpots(pile).map((zone) => ({ seat, zone })) : [];
      // (a draw deck's top before its cards, any other spot after them)
      const first = spots.filter((spot) => spot.zone === 'deck.top');
      const last = spots.filter((spot) => spot.zone !== 'deck.top');
      return {
        key: `${pile}:${player}`,
        pile,
        player,
        seat,
        sandbox,
        fromBoard,
        title: `${pileTitle(pile, own)} - ${displayName(model.scenario, player)}`,
        cards,
        first,
        last,
        fit: [
          ...first.map(() => UPRIGHT),
          ...cards.map((card): FitCard => ({ horizontal: this.views.of(card).horizontal })),
          ...last.map(() => UPRIGHT),
        ],
      };
    });
  });

  constructor() {
    // A pile from the board: read when its window opens, and again (a moment after) whenever the game moves
    let asked = 0;
    effect(() => {
      const keys = this.fromBoard();
      this.session.model();
      if (keys === '') return;
      const mine = ++asked;
      untracked(() =>
        this.lifetime.later(() => {
          if (mine === asked) this.edit.refresh();
        }, PILE_READ_MS),
      );
    });
  }
}
