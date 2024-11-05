package com.gempukku.lotro.logic.timing.processes.turn.archery;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

public class FellowshipPlayerAssignsArcheryDamageGameProcess implements GameProcess {
    private final int _woundsToAssign;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;

    public FellowshipPlayerAssignsArcheryDamageGameProcess(int woundsToAssign, GameProcess followingGameProcess) {
        _woundsToAssign = woundsToAssign;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(LotroGame game) {
        if (_woundsToAssign > 0) {

            Filter filter = getArcheryFireWoundableFilter(game);

            SystemQueueAction action = new SystemQueueAction();
            for (int i = 0; i < _woundsToAssign; i++) {
                final int woundsLeft = _woundsToAssign - i;
                ChooseAndWoundCharactersEffect woundCharacter = new ChooseAndWoundCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, filter);
                woundCharacter.setSourceText("Archery Fire");
                woundCharacter.setChoiceText("Choose character to assign archery wound to - remaining wounds: " + woundsLeft);
                action.appendEffect(woundCharacter);
            }

            game.getActionsEnvironment().addActionToStack(action);
        }
        _nextProcess = _followingGameProcess;
    }

    private static Filter getArcheryFireWoundableFilter(LotroGame game) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.ALLIES_TAKE_ARCHERY_FIRE_WOUNDS_INSTEAD_OF_COMPANIONS)) {
            return Filters.and(
                    CardType.ALLY,
                    new Filter() {
                        @Override
                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                            return game.getModifiersQuerying().canTakeArcheryWound(game, physicalCard);
                        }
                    }
            );
        } else {
            return Filters.and(
                    Filters.or(
                            CardType.COMPANION,
                            Filters.and(
                                    CardType.ALLY,
                                    Filters.or(
                                            Filters.and(
                                                    Filters.allyAtHome,
                                                    new Filter() {
                                                        @Override
                                                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                            return !game.getModifiersQuerying().isAllyPreventedFromParticipatingInArcheryFire(game, physicalCard);
                                                        }
                                                    }),
                                            new Filter() {
                                                @Override
                                                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                    return game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(game, physicalCard);
                                                }
                                            })
                            )
                    ),
                    new Filter() {
                        @Override
                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                            return game.getModifiersQuerying().canTakeArcheryWound(game, physicalCard);
                        }
                    }
            );
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
