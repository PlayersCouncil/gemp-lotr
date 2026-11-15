import { computed, effect, inject, Injectable, untracked } from '@angular/core';
import { Notifier } from '../../notifier/notifier';
import { decisionKey } from '../session/game-restore';
import { GameSession } from '../session/game-session.service';
import { GameViewState } from '../session/game-view-state';
import { GameSettings } from '../settings/game-settings';
import {
  assignMinion,
  assignmentAnswer,
  clearSelection,
  pickMinion,
  selectAction,
  selectCard,
  unassignMinion,
} from '../state/local-choices';
import { CardAction, DecisionButton, decisionView, minimum, minionUnassigned } from './decision-view';

/** A second click on the same card within this time is a bounce of the finger, not a choice (ms). */
export const SELECTION_DEBOUNCE_MS = 200;

/** What Done asks when a minion is left without a character. */
export const UNASSIGNED_QUESTION = 'At least one minion has not been assigned, do you want to proceed?';

/** The menu of a card with several actions, where the card was clicked (in the page). */
export interface ActionMenu {
  cardId: string;
  x: number;
  y: number;
  actions: readonly CardAction[];
}

/**
 * The waiting decision as the player answers it: what the page shows for it (`view`) and what each gesture does: a
 * card clicked, a button, an action of the menu, a number, an answer of a list, Escape. Choices that aren't sent yet
 * are kept with the game (GameSession), so a reload finds them. Page-scoped.
 */
@Injectable()
export class GameDecisions {
  private readonly session = inject(GameSession);
  private readonly settings = inject(GameSettings);
  private readonly notifier = inject(Notifier);

  readonly view = computed(() =>
    decisionView(this.session.model(), {
      autoAccept: this.settings.autoAccept(),
      alwaysDropDown: this.settings.alwaysDropDown(),
    }),
  );

  private readonly saved = inject(GameViewState).menu;

  /** The open menu of a card's actions, or null. Kept with the game, so a reload shows it open again. */
  readonly menu = computed<ActionMenu | null>(() => {
    const menu = this.saved();
    const waiting = this.session.model().decision;
    if (!menu || !waiting || menu.decision !== decisionKey(waiting.decision)) return null;
    const actions = this.view().actions[menu.cardId];
    return actions && actions.length > 1 ? { cardId: menu.cardId, x: menu.x, y: menu.y, actions } : null;
  });

  private lastClick: { cardId: string; at: number } | null = null;

  constructor() {
    effect(() => {
      const waiting = this.session.model().decision;
      untracked(() => {
        // A menu is for one decision: another one (or none) closes it; the same after a reload keeps it
        // (before the game has come, after a reload, nothing is known yet)
        const menu = this.saved();
        const loaded = this.session.model().eventCount > 0;
        if (menu && loaded && (!waiting || menu.decision !== decisionKey(waiting.decision))) this.saved.set(null);
        // Looking through one's own deck with nothing to choose: answered at once, if the player asked for that
        if (
          waiting?.decision.type === 'ARBITRARY_CARDS' &&
          waiting.decision.params['deckInspection']?.[0] === 'own' &&
          this.settings.autoDismissDeckReveal()
        ) {
          this.session.answer('');
        }
      });
    });
  }

  private get decision() {
    return this.session.model().decision?.decision ?? null;
  }

  private get selected(): readonly string[] {
    return this.session.model().local.selectedCardIds;
  }

  /** A card clicked, at a place of the page (for its menu). Nothing happens unless the decision offers the card. */
  cardClicked(cardId: string, at: { x: number; y: number }, now = Date.now()): void {
    const decision = this.decision;
    const mark = this.view().marks[cardId];
    if (!decision || (mark !== 'selectable' && mark !== 'actionable')) return;
    if (this.lastClick?.cardId === cardId && now - this.lastClick.at < SELECTION_DEBOUNCE_MS) return;
    this.lastClick = { cardId, at: now };

    switch (decision.type) {
      case 'CARD_SELECTION':
      case 'ARBITRARY_CARDS': {
        this.session.choose((model) => selectCard(model, cardId));
        const max = Number(decision.params['max']?.[0] ?? 0);
        if (this.selected.length === max && this.settings.autoAccept()) this.send();
        break;
      }
      case 'ACTION_CHOICE': {
        const actionId = decision.params['actionId']?.[Number(cardId.substring(4))];
        if (actionId !== undefined) this.take(actionId, cardId);
        break;
      }
      case 'CARD_ACTION_CHOICE': {
        const actions = this.view().actions[cardId] ?? [];
        const [only] = actions;
        if (actions.length === 1 && only) {
          this.take(only.actionId, cardId);
        } else if (actions.length > 1) {
          this.saved.set({ cardId, x: at.x, y: at.y, decision: decisionKey(decision) });
        }
        break;
      }
      case 'ASSIGN_MINIONS': {
        const minion = this.session.model().local.minion;
        if (minion === null) {
          this.session.choose((model) => pickMinion(model, cardId));
        } else {
          // Another card is the character it goes to; itself again takes it back
          this.session.choose((model) =>
            pickMinion(cardId === minion ? unassignMinion(model, minion) : assignMinion(model, minion, cardId), null),
          );
        }
        break;
      }
      default:
        break;
    }
  }

  /** An action chosen in the open menu. */
  chooseAction(actionId: string): void {
    const menu = this.menu();
    this.saved.set(null);
    if (menu && this.decision?.type === 'CARD_ACTION_CHOICE') this.take(actionId, menu.cardId);
  }

  closeMenu(): void {
    this.saved.set(null);
  }

  async press(button: DecisionButton): Promise<void> {
    const decision = this.decision;
    if (!decision) return;
    switch (button) {
      case 'Pass':
        this.session.answer('');
        break;
      case 'Reset choice':
      case 'Clear selection':
        this.session.choose(clearSelection);
        break;
      case 'Done':
        if (decision.type === 'ASSIGN_MINIONS') {
          if (minionUnassigned(this.session.model()) && !(await this.notifier.confirm(UNASSIGNED_QUESTION))) return;
          this.session.answer(assignmentAnswer(this.session.model()));
        } else if (this.selected.length >= minimum(decision)) {
          this.send();
        }
        break;
      case 'OK':
        break;
    }
  }

  /** The number of an integer decision, as typed. */
  answerNumber(value: string): void {
    if (this.decision?.type === 'INTEGER') this.session.answer(value);
  }

  /** The answer of a multiple choice, by its place in the list. */
  answerChoice(index: number): void {
    if (this.decision?.type === 'MULTIPLE_CHOICE') this.session.answer(String(index));
  }

  /** Escape finishes a choice of cards shown in a dialog, if enough are chosen. */
  escape(): void {
    const decision = this.decision;
    if (decision?.type === 'ARBITRARY_CARDS' && this.selected.length >= minimum(decision)) this.send();
  }

  private take(actionId: string, cardId: string): void {
    this.session.choose((model) => selectAction(model, actionId, cardId));
    if (this.settings.autoAccept()) this.send();
  }

  private send(): void {
    this.session.answer(this.selected.join(','));
  }
}
