package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.Collections;

/**
 * One Sanctuary-heal "slot" for a player whose modifier (94_7) lets them remove burdens instead
 * of healing wounds. Presents the ordinary "choose a companion to heal" decision, except the
 * Ring-bearer is also selectable purely because there is a burden to remove, even if he is
 * unwounded. Only when the Ring-bearer is chosen AND both a wound and a burden are available is a
 * follow-up "heal or remove a burden?" choice shown; when only one of the two applies, that one is
 * simply done, with no extra prompt.
 */
public class ChooseHealOrRemoveBurdenEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private final int _remainingHeals;

    public ChooseHealOrRemoveBurdenEffect(Action action, String playerId, int remainingHeals) {
        super(action.getActionSource(), playerId,
                "Sanctuary healing - Choose companion to heal - remaining heals: " + remainingHeals,
                0, 1, Collections.emptyMap(), CardType.COMPANION);
        _action = action;
        _playerId = playerId;
        _remainingHeals = remainingHeals;
    }

    private Filter canBeHealed() {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeHealed(game, physicalCard);
    }

    private Filter hasRemovableBurden() {
        return (game, physicalCard) -> game.getGameState().getBurdens() > 0;
    }

    private Filter selectableCharacters(LotroGame game) {
        return Filters.or(
                Filters.and(Filters.wounded, canBeHealed()),
                Filters.and(Filters.ringBearer, hasRemovableBurden()));
    }

    @Override
    protected Filter getExtraFilterForPlaying(LotroGame game) {
        return selectableCharacters(game);
    }

    @Override
    protected Filter getExtraFilterForPlayabilityCheck(LotroGame game) {
        return selectableCharacters(game);
    }

    @Override
    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
        if (cards.isEmpty()) {
            game.getActionsEnvironment().addActionToStack(new SubAction(_action));
            return;
        }

        final PhysicalCard target = cards.iterator().next();
        boolean isRingBearer = Filters.ringBearer.accepts(game, target);
        boolean hasWound = game.getGameState().getWounds(target) > 0;
        boolean hasBurden = game.getGameState().getBurdens() > 0;

        if (isRingBearer && hasWound && hasBurden) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1,
                            "Sanctuary healing - remaining heals: " + _remainingHeals + " - heal the Ring-bearer or remove a burden?",
                            new String[]{"Heal a wound", "Remove a burden"}) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            SubAction subAction = new SubAction(_action);
                            if (index == 1)
                                subAction.appendEffect(new RemoveBurdenEffect(_playerId, target, 1));
                            else
                                subAction.appendEffect(new HealCharactersEffect(_action.getActionSource(), _playerId, target));
                            game.getActionsEnvironment().addActionToStack(subAction);
                        }
                    });
        } else if (isRingBearer && !hasWound && hasBurden) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new RemoveBurdenEffect(_playerId, target, 1));
            game.getActionsEnvironment().addActionToStack(subAction);
        } else {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new HealCharactersEffect(_action.getActionSource(), _playerId, target));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
