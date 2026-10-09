package com.gempukku.lotro.logic.timing.processes.turn.archery;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
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

            Filter filter = getArcheryFireWoundableFilter();

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

    /**
     * Who an archery wound may be assigned to. The filter is read afresh as each wound is assigned.
     *
     * As usual: a companion, or an ally who participates in archery fire (at home or allowed to by a card, and
     * in neither case barred from it by a card: the querying answers that no card allows a barred ally).
     *
     * While allies take archery wounds instead of companions (Come Down): any ally, at home or not, for as long
     * as there is an ally who can take the wound. A wound that no ally can take, because there is none or the
     * last has been killed, is assigned as usual (CR 4.2 "instead": "If the second effect cannot happen for any
     * reason, then the original effect occurs"). Taking the wounds is all this lets an ally do: an archer ally
     * away from home is not counted for the fellowship archery total (RuleUtils.calculateFellowshipArcheryTotal
     * does not read the flag).
     *
     * An ally a card bars from archery fire (Streets of Edoras) is not an ally who can take the wound: taking
     * archery wounds is part of participating in archery fire, and "can't" beats "can". The wound goes to
     * another ally or, none being able to take it, as usual.
     */
    private static Filter getArcheryFireWoundableFilter() {
        final Filter canTakeArcheryWound = (game, physicalCard) -> game.getModifiersQuerying().canTakeArcheryWound(game, physicalCard);

        final Filter usual = Filters.and(
                Filters.or(
                        CardType.COMPANION,
                        Filters.and(
                                CardType.ALLY,
                                Filters.or(
                                        Filters.and(
                                                Filters.allyAtHome,
                                                (Filter) (game, physicalCard) -> !game.getModifiersQuerying().isAllyPreventedFromParticipatingInArcheryFire(game, physicalCard)),
                                        (Filter) (game, physicalCard) -> game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(game, physicalCard))
                        )
                ),
                canTakeArcheryWound
        );

        // The same test of whether a wound can be placed that ChooseAndWoundCharactersEffect makes of each candidate.
        final Filter allyWhoCanTakeTheWound = Filters.and(CardType.ALLY, Filters.not(Filters.barredFromArcheryFire),
                canTakeArcheryWound, Filters.canTakeWounds(null, 1));

        return (game, physicalCard) -> {
            if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.ALLIES_TAKE_ARCHERY_FIRE_WOUNDS_INSTEAD_OF_COMPANIONS))
                return usual.accepts(game, physicalCard);
            if (allyWhoCanTakeTheWound.accepts(game, physicalCard))
                return true;
            return usual.accepts(game, physicalCard) && !Filters.hasActive(game, allyWhoCanTakeTheWound);
        };
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
