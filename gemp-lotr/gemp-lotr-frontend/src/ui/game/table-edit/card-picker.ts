import { inject, Injectable, signal } from '@angular/core';
import { cardById } from '../../../core/game/table-edit/edit-board';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { cardRect } from '../scenario/scenario-anchors';
import { PickHit } from '../../../core/game/authoring/picking';
import { TablePick } from './table-pick';

/** Where the cards a pick can take lie: the table's rows and fights. */
const TABLE = '.gd-game__table';
/** What lies over the table without being of it (the edit controls' own popups, a scripted game's layer). */
const OVER_THE_TABLE = 'gd-table-edit, gd-game-scenario';

/**
 * Picking a card on the table for the card menu's Attach and Stack: the shared TablePick (the crosshair, the box round
 * the card under the pointer with its name), taking a card of the table's rows (not the card being moved itself). What
 * the person is told meanwhile is the edit bar's (`told`). Page-scoped (ui).
 */
@Injectable()
export class CardPicker extends TablePick {
  private readonly edit = inject(TableEdit);
  private readonly telling = signal<string | null>(null);
  /** What the person is told while picking (the edit bar says it), or null. */
  readonly told = this.telling.asReadonly();
  /** The card being attached or stacked: not one to pick. */
  private readonly not = signal<string | null>(null);

  /** Lets the person click a card on the table (not `exclude`); resolves to its card id, or null for none. */
  pickCard(told: string, exclude: string | null = null): Promise<string | null> {
    this.not.set(exclude);
    return this.start(told);
  }

  thingAt(x: number, y: number): (PickHit & { text?: string }) | null {
    const first = this.document.elementsFromPoint(x, y).find((element) => element.closest(OVER_THE_TABLE) === null);
    const card = first?.closest(`${TABLE} [data-card-id]`) ?? null;
    const cardId = card?.getAttribute('data-card-id') ?? null;
    const box = cardRect(card);
    if (!card || !cardId || !box || cardId === this.not()) return null;
    const name = cardById(this.edit.board(), cardId)?.name ?? card.getAttribute('data-blueprint-id') ?? cardId;
    return { anchor: cardId, text: name, box };
  }

  protected began(told: string): void {
    this.telling.set(told);
  }

  protected ended(): void {
    this.telling.set(null);
    this.not.set(null);
  }
}
