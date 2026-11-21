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
import { DecisionGates, DecisionGesture } from './decision-gate';
import { CardAction, DecisionButton, decisionView, minimum, minionUnassigned } from './decision-view';

/** A second click on the same card within this time is a bounce of the finger, not a choice (ms). */
export const SELECTION_DEBOUNCE_MS = 200;

/** What Done asks when a minion is left without a character. */
export const UNASSIGNED_QUESTION = 'At least one minion has not been assigned, do you want to proceed?';
/** How the page's own dialog asks it (the browser's has only OK and Cancel). */
export const UNASSIGNED_WORDS = { title: 'Unassigned minions', ok: 'Proceed', cancel: 'Go back' } as const;

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
 * are kept with the game (GameSession), so a reload finds them. Every gesture is put to the page's gates first
 * (DecisionGates: a scripted game's step; a revert request waiting, GameUndo.frozen, when the server would refuse the
 * answer), and happens only if they allow it.
 * Page-scoped.
 */
@Injectable()
export class GameDecisions {
  private readonly session = inject(GameSession);
  private readonly settings = inject(GameSettings);
  private readonly notifier = inject(Notifier);
  private readonly gates = inject(DecisionGates);

  readonly view = computed(() =>
    decisionView(this.session.model(), {
      autoAccept: this.settings.autoAccept(),
      alwaysDropDown: this.settings.alwaysDropDown(),
      replay: this.session.replaying(),
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
        // A decision that only asks to be confirmed (confirmOnly: DONE on a full starting fellowship) is answered with
        // nothing, if the player asked for that ("Skip extra confirmations"; legacy confirmsItself)
        if (
          waiting?.decision.params['confirmOnly']?.[0] === 'true' &&
          this.settings.skipConfirmations() &&
          !this.session.replaying()
        ) {
          this.session.answer('');
        }
      });
    });
    // After the game was taken back and read again, a card's menu open before is for a game that is gone
    effect(() => {
      if (this.session.reopened() > 0) untracked(() => this.saved.set(null));
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
    if (!decision || this.session.replaying() || (mark !== 'selectable' && mark !== 'actionable')) return;
    if (this.lastClick?.cardId === cardId && now - this.lastClick.at < SELECTION_DEBOUNCE_MS) return;
    this.lastClick = { cardId, at: now };
    const gesture: DecisionGesture = { kind: 'card', cardId };
    if (!this.gates.allows(gesture)) return;

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
    this.gates.after(gesture);
  }

  /** An action chosen in the open menu. */
  chooseAction(actionId: string): void {
    const menu = this.menu();
    if (!menu || this.decision?.type !== 'CARD_ACTION_CHOICE') {
      this.saved.set(null);
      return;
    }
    // (an action the gates refuse leaves the menu open, for another of its actions)
    const gesture: DecisionGesture = { kind: 'action', actionId, cardId: menu.cardId };
    if (!this.gates.allows(gesture)) return;
    this.saved.set(null);
    this.take(actionId, menu.cardId);
    this.gates.after(gesture);
  }

  closeMenu(): void {
    this.saved.set(null);
  }

  async press(button: DecisionButton): Promise<void> {
    const decision = this.decision;
    if (!decision) return;
    const gesture: DecisionGesture = { kind: 'button', button };
    if (!this.gates.allows(gesture)) return;
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
          if (
            minionUnassigned(this.session.model()) &&
            !(await this.notifier.confirm(UNASSIGNED_QUESTION, UNASSIGNED_WORDS))
          ) {
            return;
          }
          this.session.answer(assignmentAnswer(this.session.model()));
        } else if (this.selected.length >= minimum(decision)) {
          this.send();
        }
        break;
      case 'OK':
        break;
    }
    this.gates.after(gesture);
  }

  /** The number of an integer decision, as typed. */
  answerNumber(value: string): void {
    if (this.decision?.type !== 'INTEGER') return;
    const gesture: DecisionGesture = { kind: 'number', value };
    if (!this.gates.allows(gesture)) return;
    this.session.answer(value);
    this.gates.after(gesture);
  }

  /** The answer of a multiple choice, by its place in the list. */
  answerChoice(index: number): void {
    if (this.decision?.type !== 'MULTIPLE_CHOICE') return;
    const gesture: DecisionGesture = { kind: 'choice', index };
    if (!this.gates.allows(gesture)) return;
    this.session.answer(String(index));
    this.gates.after(gesture);
  }

  /** Escape finishes a choice of cards shown in a dialog, if enough are chosen. */
  escape(): void {
    const decision = this.decision;
    if (decision?.type !== 'ARBITRARY_CARDS' || this.selected.length < minimum(decision)) return;
    const gesture: DecisionGesture = { kind: 'escape' };
    if (!this.gates.allows(gesture)) return;
    this.send();
    this.gates.after(gesture);
  }

  private take(actionId: string, cardId: string): void {
    this.session.choose((model) => selectAction(model, actionId, cardId));
    if (this.settings.autoAccept()) this.send();
  }

  private send(): void {
    this.session.answer(this.selected.join(','));
  }
}
