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
    public static boolean startOfPhase(LotroGame game, EffectResult effectResult, Phase phase) {
        if (effectResult.getType() == EffectResult.Type.START_OF_PHASE) {
            StartOfPhaseResult startOfPhaseResult = (StartOfPhaseResult) effectResult;
            return (startOfPhaseResult.getPhase() == phase || phase == null);
        }
        return false;
    }

    public static boolean endOfPhase(LotroGame game, EffectResult effectResult, Phase phase) {
        if (effectResult.getType() == EffectResult.Type.END_OF_PHASE) {
            EndOfPhaseResult endOfPhaseResult = (EndOfPhaseResult) effectResult;
            return (endOfPhaseResult.getPhase() == phase || phase == null);
        }
        return false;
    }

    public static boolean startOfTurn(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.START_OF_TURN;
    }

    public static boolean endOfTurn(LotroGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.END_OF_TURN;
    }

    public static boolean losesSkirmishInvolving(LotroGame game, EffectResult effectResult, Filterable loserFilter, Filterable involvingFilter) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
            CharacterLostSkirmishResult wonResult = (CharacterLostSkirmishResult) effectResult;
            return Filters.accepts(game, wonResult.getLoser(), loserFilter)
                    && Filters.acceptsAny(game, wonResult.getInvolving(), involvingFilter);
        }
        return false;
    }

    public static boolean addedBurden(LotroGame game, String playerId, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_BURDEN) {
            AddBurdenResult burdenResult = (AddBurdenResult) effectResult;
            if (playerId != null && !playerId.equals(burdenResult.getPerformingPlayer()))
                return false;
            return Filters.and(sourceFilters).accepts(game, burdenResult.getSource());
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

    public static boolean addedCultureToken(LotroGame game, String playerId, EffectResult effectResult, Filterable from, Filterable... targetFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_CULTURE_TOKEN) {
            var tokenResult = (AddCultureTokenResult) effectResult;
            if (playerId != null && !playerId.equals(tokenResult.getPerformingPlayer()))
                return false;
            return Filters.accepts(game, tokenResult.getSource(), from)
                    && Filters.and(targetFilters).accepts(game, tokenResult.getTarget());
        }
        return false;
    }

    public static boolean removedCultureToken(LotroGame game, String playerId, EffectResult effectResult, Filterable from, Filterable... targetFilters) {
        if (effectResult.getType() == EffectResult.Type.REMOVE_CULTURE_TOKEN) {
            var tokenResult = (RemoveCultureTokenResult) effectResult;
            if (playerId != null && !playerId.equals(tokenResult.getPerformingPlayer()))
                return false;
            return Filters.accepts(game, tokenResult.getSource(), from)
                    && Filters.and(targetFilters).accepts(game, tokenResult.getTarget());
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
                    && Filters.acceptsAny(game, assignmentResult.getAgainst(), againstFilter);
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
                    && Filters.accepts(game, discardResult.getSource(), discardedBy))
                return Filters.and(discarded).accepts(game, discardResult.getDiscardedCard());
        }
        return false;
    }

    public static boolean forEachMortallyWoundedBy(LotroGame game, EffectResult effectResult, Filterable woundedBy, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED) {
            var woundResult = (WoundResult) effectResult;
            var wounded = woundResult.getWoundedCard();

            return Filters.acceptsAny(game, woundResult.getSources(), woundedBy) &&
                    Filters.accepts(game, woundResult.getWoundedCard(), filters) &&
                    (
                            (Filters.or(CardType.ALLY, CardType.COMPANION).accepts(game, wounded) &&
                                    wounded.getZone() == Zone.DEAD) ||
                                    (Filters.accepts(game, wounded, CardType.MINION) &&
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
        return forEachExertedBy(game, effectResult, Filters.any, filters);
    }

    public static boolean forEachExertedBy(LotroGame game, EffectResult effectResult, Filterable exertedBy, Filterable... exerted) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_EXERTED) {
            ExertResult exertResult = (ExertResult) effectResult;
            if (exertResult.getAction().getActionSource() != null
                    && Filters.accepts(game, exertResult.getAction().getActionSource(), exertedBy))
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

    public static boolean forEachCardRevealedFromHand(EffectResult effectResult) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_REVEALED_FROM_HAND)
            return true;
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
            return Filters.acceptsAny(game, healEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean isGettingKilled(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_KILLED) {
            KillEffect killEffect = (KillEffect) effect;
            return Filters.acceptsAny(game, killEffect.getCharactersToBeKilled(), filters);
        }
        return false;
    }

    public static boolean isSkirmishAboutToEnd(EffectResult effectResult, LotroGame game, Filterable... charactersInvolving) {
        if (effectResult.getType() == EffectResult.Type.SKIRMISH_ABOUT_TO_END) {
            SkirmishAboutToEndResult skirmishAboutToEnd = (SkirmishAboutToEndResult) effectResult;
            return Filters.acceptsAny(game, skirmishAboutToEnd.getAllInvolved(), charactersInvolving);
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
        return forEachKilledBy(game, effectResult, Filters.any, null, filters);
    }

    public static boolean forEachKilledBy(LotroGame game, EffectResult effectResult, Filterable killedBy, KillEffect.Cause cause, Filterable... killed) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED) {
            return forEachMortallyWoundedBy(game, effectResult, killedBy, killed);
        } else if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED) {
            if (forEachKilledInASkirmish(game, effectResult, killedBy, cause, killed))
                return true;

            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;
            var killers = killResult.getKillers();

            if (killedBy != Filters.any && (killers == null || killers.isEmpty() || killers.stream().allMatch(Objects::isNull)))
                return false;

            return Filters.acceptsAny(game, killers, killedBy) &&
                    Filters.and(killed).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean forEachKilledInASkirmish(LotroGame game, EffectResult effectResult, Filterable killedBy, KillEffect.Cause cause, Filterable... killed) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED
                && game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && Filters.countActive(game, Filters.inSkirmish, killedBy) > 0) {
            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;

            if(cause != null && killResult.getCause() != cause)
                return false;

            return Filters.and(killed).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean isGettingWoundedBy(Effect effect, LotroGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            WoundCharactersEffect woundEffect = (WoundCharactersEffect) effect;
            if (woundEffect.getSources() != null && Filters.acceptsAny(game, woundEffect.getSources(), sourceFilter))
                return Filters.acceptsAny(game, woundEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean isGettingExerted(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_EXERT) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.acceptsAny(game, woundEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean isGettingWounded(Effect effect, LotroGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.acceptsAny(game, woundEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean isGettingDiscardedBy(Effect effect, LotroGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.accepts(game, effect.getSource(), sourceFilter))
                return Filters.acceptsAny(game, preventableEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean isGettingDiscardedByOpponent(Effect effect, LotroGame game, String playerId, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.accepts(game, effect.getSource(), sourceFilter)
                    && !effect.getPerformingPlayer().equals(playerId))
                return Filters.acceptsAny(game, preventableEffect.getAffectedCardsMinusPrevented(game), filters);
        }
        return false;
    }

    public static boolean activated(LotroGame game, String player, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ACTIVATE) {
            var result = (ActivateCardResult) effectResult;
            PhysicalCard source = result.getSource();
            if(player != null) {
                if(!player.equals(result.getPerformingPlayer()))
                    return false;
            }
            return Filters.and(filters).accepts(game, source);
        }
        return false;
    }

    public static boolean played(LotroGame game, EffectResult effectResult, Filterable targetFilter, Filterable fromFilter, Zone fromZone, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final var playResult = (PlayCardResult) effectResult;
            final var attachedTo = playResult.getAttachedTo();
            final var cardPlayedFrom = playResult.getAttachedOrStackedPlayedFrom();
            final var zone = playResult.getPlayedFrom();

            if (targetFilter != null && targetFilter != Filters.any &&
                    (attachedTo == null || !Filters.accepts(game, attachedTo, targetFilter)))
                return false;

            if(fromFilter != null && fromFilter != Filters.any && cardPlayedFrom != null
                    && !Filters.accepts(game, cardPlayedFrom, fromFilter))
                return false;

            if(fromZone != null && zone != fromZone)
                return false;

            return Filters.and(filters).accepts(game, playResult.getPlayedCard());
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
            return (Filters.accepts(game, transferResult.getTransferredCard(), transferredCard)
                    && (transferredFrom == null || (transferResult.getTransferredFrom() != null && Filters.accepts(game, transferResult.getTransferredFrom(), transferredFrom)))
                    && (transferredTo == null || (transferResult.getTransferredTo() != null && Filters.accepts(game, transferResult.getTransferredTo(), transferredTo))));
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
