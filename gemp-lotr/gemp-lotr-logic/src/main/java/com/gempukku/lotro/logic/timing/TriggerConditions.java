package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.DrawOneCardEffect;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.results.*;

import java.util.Objects;

public class TriggerConditions {
    public static boolean losesInitiative(EffectResult effectResult, Side side) {
        if (effectResult.getType() == EffectResult.Type.INITIATIVE_CHANGE) {
            InitiativeChangeResult initiativeChangeResult = (InitiativeChangeResult) effectResult;
            if (initiativeChangeResult.getSide() != side)
                return true;
        }
        return false;
    }

    public static boolean takenControlOfASite(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.TAKE_CONTROL_OF_SITE) {
            TakeControlOfSiteResult takeResult = (TakeControlOfSiteResult) effectResult;
            return takeResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean startOfPhase(LotroGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.START_OF_PHASE
                && game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean endOfPhase(LotroGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.END_OF_PHASE
                && (game.getGameState().getCurrentPhase() == phase || phase == null));
    }

    public static boolean startOfTurn(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.START_OF_TURN;
    }

    public static boolean endOfTurn(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.END_OF_TURN;
    }

    public static boolean reconciles(LotroGame game, EffectResult effectResult, String playerId) {
        return effectResult.getType() == EffectResult.Type.RECONCILE && (playerId == null || ((ReconcileResult) effectResult).getPlayerId().equals(playerId));
    }

    public static boolean winsSkirmish(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
            CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) effectResult;
            return Filters.and(filters).accepts(game, wonResult.getWinner());
        }
        return false;
    }

    public static boolean winsSkirmishInvolving(LotroGame game, EffectResult effectResult, Filterable winnerFilter, Filterable involvingFilter) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
            CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) effectResult;
            return Filters.and(winnerFilter).accepts(game, wonResult.getWinner())
                    && Filters.filter(wonResult.getInvolving(), game, involvingFilter).size() > 0;
        }
        return false;
    }

    public static boolean losesSkirmish(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
            CharacterLostSkirmishResult wonResult = (CharacterLostSkirmishResult) effectResult;
            return Filters.and(filters).accepts(game, wonResult.getLoser());
        }
        return false;
    }

    public static boolean losesSkirmishInvolving(LotroGame game, EffectResult effectResult, Filterable loserFilter, Filterable involvingFilter) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
            CharacterLostSkirmishResult wonResult = (CharacterLostSkirmishResult) effectResult;
            return Filters.and(loserFilter).accepts(game, wonResult.getLoser())
                    && Filters.filter(wonResult.getInvolving(), game, involvingFilter).size() > 0;
        }
        return false;
    }

    public static boolean sidePlayerAddedBurden(LotroGame game, EffectResult effectResult, Side side) {
        if (effectResult.getType() == EffectResult.Type.ADD_BURDEN) {
            AddBurdenResult burdenResult = (AddBurdenResult) effectResult;
            String fpPlayer = game.getGameState().getCurrentPlayerId();
            if ((side == Side.FREE_PEOPLE && fpPlayer.equals(burdenResult.getPerformingPlayer()))
                    || (side == Side.SHADOW && !fpPlayer.equals(burdenResult.getPerformingPlayer())))
                return true;
            else
                return false;
        }
        return false;
    }

    public static boolean addedBurden(LotroGame game, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_BURDEN) {
            AddBurdenResult burdenResult = (AddBurdenResult) effectResult;
            return (Filters.and(sourceFilters).accepts(game, burdenResult.getSource()));
        }
        return false;
    }

    public static boolean removedBurden(LotroGame game, EffectResult effectResult, String player, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.REMOVE_BURDEN) {
            RemoveBurdenResult burdenResult = (RemoveBurdenResult) effectResult;
            return (player == null || burdenResult.getPerformingPlayerId().equals(player))
                    && Filters.and(sourceFilters).accepts(game, burdenResult.getSource());
        }
        return false;
    }

    public static boolean addedThreat(LotroGame game, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_THREAT) {
            AddThreatResult threatResult = (AddThreatResult) effectResult;
            return (Filters.and(sourceFilters).accepts(game, threatResult.getSource()));
        }
        return false;
    }

    public static boolean assignedAgainst(LotroGame game, EffectResult effectResult, Side side, Filterable againstFilter, Filterable... assignedFilters) {
        if (effectResult.getType() == EffectResult.Type.ASSIGNED_AGAINST) {
            AssignAgainstResult assignmentResult = (AssignAgainstResult) effectResult;
            if (side != null) {
                if (assignmentResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                    if (side == Side.SHADOW)
                        return false;
                } else {
                    if (side == Side.FREE_PEOPLE)
                        return false;
                }
            }

            return Filters.and(assignedFilters).accepts(game, assignmentResult.getAssignedCard())
                    && Filters.filter(assignmentResult.getAgainst(), game, againstFilter).size() > 0;
        }
        return false;
    }

    public static boolean assignedToSkirmish(LotroGame game, EffectResult effectResult, Side side, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ASSIGNED_TO_SKIRMISH) {
            AssignedToSkirmishResult assignResult = (AssignedToSkirmishResult) effectResult;
            if (side != null) {
                if (assignResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                    if (side == Side.SHADOW)
                        return false;
                } else {
                    if (side == Side.FREE_PEOPLE)
                        return false;
                }
            }

            return Filters.and(filters).accepts(game, ((AssignedToSkirmishResult) effectResult).getAssignedCard());
        }
        return false;
    }

    public static boolean forEachCardDrawnOrPutIntoHandByOpponent(LotroGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return !drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachCardDrawnOrPutIntoHand(LotroGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachCardDrawn(LotroGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.isDraw() && drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachDiscardedFromPlay(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY)
            return Filters.and(filters).accepts(game, ((DiscardCardsFromPlayResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHand(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND)
            return Filters.and(filters).accepts(game, ((DiscardCardFromHandResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromDeck(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_DECK)
            return Filters.and(filters).accepts(game, ((DiscardCardFromDeckResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHandBy(LotroGame game, EffectResult effectResult, Filterable discardedBy, Filterable... discarded) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND) {
            DiscardCardFromHandResult discardResult = (DiscardCardFromHandResult) effectResult;
            if (discardResult.getSource() != null
                    && Filters.and(discardedBy).accepts(game, discardResult.getSource()))
                return Filters.and(discarded).accepts(game, discardResult.getDiscardedCard());
        }
        return false;
    }

    public static boolean forEachWoundedBy(LotroGame game, EffectResult effectResult, Filterable woundedBy, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED) {
            var woundResult = (WoundResult) effectResult;

            return Filters.filter(woundResult.getSources(), game, woundedBy).size() > 0 &&
                    Filters.and(filters).accepts(game, woundResult.getWoundedCard());
        }
        return false;
    }

    public static boolean forEachMortallyWoundedBy(LotroGame game, EffectResult effectResult, Filterable woundedBy, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED) {
            var woundResult = (WoundResult) effectResult;
            var wounded = woundResult.getWoundedCard();

            return Filters.filter(woundResult.getSources(), game, woundedBy).size() > 0 &&
                    Filters.and(filters).accepts(game, woundResult.getWoundedCard()) &&
                    (
                            (Filters.or(CardType.ALLY, CardType.COMPANION).accepts(game, wounded) &&
                                    wounded.getZone() == Zone.DEAD) ||
                                    (Filters.and(CardType.MINION).accepts(game, wounded) &&
                                            wounded.getZone() != Zone.SHADOW_CHARACTERS)
                    );
        }
        return false;
    }

    public static boolean forEachWounded(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED)
            return Filters.and(filters).accepts(game, ((WoundResult) effectResult).getWoundedCard());
        return false;
    }

    public static boolean forEachHealed(LotroGame game, EffectResult effectResult, String player, Filterable... filters) {
        if (effectResult.getType() != EffectResult.Type.FOR_EACH_HEALED)
            return false;

        HealResult healResult = (HealResult) effectResult;
        if (player == null || player.equals(healResult.getPerformingPlayer()))
            return Filters.and(filters).accepts(game, ((HealResult) effectResult).getHealedCard());
        else
            return false;

    }

    public static boolean forEachExerted(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_EXERTED)
            return Filters.and(filters).accepts(game, ((ExertResult) effectResult).getExertedCard());
        return false;
    }

    public static boolean forEachExertedBy(LotroGame game, EffectResult effectResult, Filterable exertedBy, Filterable... exerted) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_EXERTED) {
            ExertResult exertResult = (ExertResult) effectResult;
            if (exertResult.getAction().getActionSource() != null
                    && Filters.and(exertedBy).accepts(game, exertResult.getAction().getActionSource()))
                return Filters.and(exerted).accepts(game, exertResult.getExertedCard());
        }
        return false;
    }

    public static boolean isTakingControlOfSite(Effect effect, LotroGame game, Filterable... sourceFilters) {
        if (effect.getType() == Effect.Type.BEFORE_TAKE_CONTROL_OF_A_SITE) {
            Preventable takeControlOfASiteEffect = (Preventable) effect;
            return !takeControlOfASiteEffect.isPrevented(game) && Filters.and(sourceFilters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean revealedCardsFromTopOfDeck(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK) {
            RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) effectResult;
            return revealCardFromTopOfDeckResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean isAddingBurden(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_ADD_BURDENS) {
            Preventable addBurdenEffect = (Preventable) effect;
            return !addBurdenEffect.isPrevented(game) && Filters.and(filters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean isAddingTwilight(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_ADD_TWILIGHT) {
            Preventable addTwilightEffect = (Preventable) effect;
            return !addTwilightEffect.isPrevented(game) && effect.getSource() != null && Filters.and(filters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean isGettingHealed(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_HEALED) {
            PreventableCardEffect healEffect = (PreventableCardEffect) effect;
            return Filters.filter(healEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingKilled(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_KILLED) {
            KillEffect killEffect = (KillEffect) effect;
            return Filters.filter(killEffect.getCharactersToBeKilled(), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isSkirmishAboutToEnd(EffectResult effectResult, LotroGame game, Filterable... minionsInvolving) {
        if (effectResult.getType() == EffectResult.Type.SKIRMISH_ABOUT_TO_END) {
            SkirmishAboutToEndResult skirmishAboutToEnd = (SkirmishAboutToEndResult) effectResult;
            return Filters.filter(skirmishAboutToEnd.getMinionsInvolved(), game, minionsInvolving).size() > 0;
        }
        return false;
    }

    public static boolean isDrawingACard(Effect effect, LotroGame game, String playerId) {
        if (effect.getType() == Effect.Type.BEFORE_DRAW_CARD) {
            DrawOneCardEffect drawEffect = (DrawOneCardEffect) effect;
            if (effect.getPerformingPlayer().equals(playerId) && drawEffect.canDrawCard(game))
                return true;
        }
        return false;
    }

    public static boolean forEachReturnedToHand(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_RETURNED_TO_HAND) {
            ReturnCardsToHandResult result = (ReturnCardsToHandResult) effectResult;
            return Filters.and(filters).accepts(game, result.getReturnedCard());
        }
        return false;
    }

    public static boolean forEachKilled(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED) {
            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;
            return Filters.and(filters).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean forEachKilledBy(LotroGame game, EffectResult effectResult, Filterable killedBy, Filterable... killed) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED) {
            return forEachMortallyWoundedBy(game, effectResult, killedBy, killed);
        } else if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED) {
            if (forEachKilledInASkirmish(game, effectResult, killedBy, killed))
                return true;

            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;
            var killers = killResult.getKillers();

            if (killers == null || killers.isEmpty() || killers.stream().allMatch(Objects::isNull))
                return false;

            return Filters.filter(killResult.getKillers(), game, killedBy).size() > 0 &&
                    Filters.and(killed).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean forEachKilledInASkirmish(LotroGame game, EffectResult effectResult, Filterable killedBy, Filterable... killed) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED
                && game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && Filters.countActive(game, Filters.inSkirmish, killedBy) > 0) {
            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;

            return Filters.and(killed).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean isGettingWoundedBy(Effect effect, LotroGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            WoundCharactersEffect woundEffect = (WoundCharactersEffect) effect;
            if (woundEffect.getSources() != null && Filters.filter(woundEffect.getSources(), game, sourceFilter).size() > 0)
                return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingExerted(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_EXERT) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingWounded(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscardedBy(Effect effect, LotroGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.and(sourceFilter).accepts(game, effect.getSource()))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscardedByOpponent(Effect effect, LotroGame game, String playerId, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && !effect.getPerformingPlayer().equals(playerId))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscarded(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect discardEffect = (PreventableCardEffect) effect;
            return Filters.filter(discardEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean activated(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ACTIVATE) {
            PhysicalCard source = ((ActivateCardResult) effectResult).getSource();
            return Filters.and(filters).accepts(game, source);
        }
        return false;
    }

    public static boolean played(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard);
        }
        return false;
    }

    public static boolean playedFromZone(LotroGame game, EffectResult effectResult, Zone zone, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == zone && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedFromStacked(LotroGame game, EffectResult effectResult, Filterable stackedOnFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == Zone.STACKED
                    && Filters.and(stackedOnFilter).accepts(game, playResult.getAttachedOrStackedPlayedFrom())
                    && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedOn(LotroGame game, EffectResult effectResult, Filterable targetFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            final PhysicalCard attachedTo = playResult.getAttachedTo();
            if (attachedTo == null)
                return false;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard)
                    && Filters.and(targetFilter).accepts(game, attachedTo);
        }
        return false;
    }

    public static boolean movesTo(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.WHEN_MOVE_TO
                && Filters.and(filters).accepts(game, game.getGameState().getCurrentSite())) {
            return true;
        }
        return false;
    }

    public static boolean isMovingTo(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_MOVE_TO
                && Filters.and(filters).accepts(game, game.getGameState().getCurrentSite()))
            return true;

        return false;
    }

    public static boolean movesFrom(LotroGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.WHEN_MOVE_FROM
                && Filters.and(filters).accepts(game, ((WhenMoveFromResult) effectResult).getSite())) {
            return true;
        }
        return false;
    }

    public static boolean moves(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.WHEN_FELLOWSHIP_MOVES;
    }

    public static boolean transferredCard(LotroGame game, EffectResult effectResult, Filterable transferredCard, Filterable transferredFrom, Filterable transferredTo) {
        if (effectResult.getType() == EffectResult.Type.CARD_TRANSFERRED) {
            CardTransferredResult transferResult = (CardTransferredResult) effectResult;
            return (Filters.and(transferredCard).accepts(game, transferResult.getTransferredCard())
                    && (transferredFrom == null || (transferResult.getTransferredFrom() != null && Filters.and(transferredFrom).accepts(game, transferResult.getTransferredFrom())))
                    && (transferredTo == null || (transferResult.getTransferredTo() != null && Filters.and(transferredTo).accepts(game, transferResult.getTransferredTo()))));
        }
        return false;
    }

    public static boolean skirmishCancelled(LotroGame game, EffectResult effectResult, Filterable... fpCharacterFilter) {
        if (effectResult.getType() == EffectResult.Type.SKIRMISH_CANCELLED) {
            SkirmishCancelledResult cancelledResult = (SkirmishCancelledResult) effectResult;
            return Filters.and(fpCharacterFilter).accepts(game, cancelledResult.getFpCharacter());
        }
        return false;
    }

    public static boolean freePlayerStartedAssigning(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_STARTS_ASSIGNING;
    }

    public static boolean freePlayerDecidedIfMoving(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING;
    }

    public static boolean freePlayerDecidedToMove(LotroGame game, EffectResult effectResult) {
        if (effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING) {
            var result = (FreePlayerMoveDecisionResult) effectResult;
            return result.IsMoving();
        }

        return false;
    }

    public static boolean freePlayerDecidedToStay(LotroGame game, EffectResult effectResult) {
        if (effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING) {
            var result = (FreePlayerMoveDecisionResult) effectResult;
            return result.IsStaying();
        }

        return false;
    }

}
