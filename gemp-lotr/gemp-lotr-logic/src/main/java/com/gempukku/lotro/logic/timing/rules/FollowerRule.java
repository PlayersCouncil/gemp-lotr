package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.TransferPermanentEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.NoEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.LinkedList;
import java.util.List;

public class FollowerRule {
    private final DefaultActionsEnvironment defaultActionsEnvironment;

    public FollowerRule(DefaultActionsEnvironment defaultActionsEnvironment) {
        this.defaultActionsEnvironment = defaultActionsEnvironment;
    }

    public void applyRule() {
        defaultActionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.startOfPhase(game, effectResult, Phase.MANEUVER)) {
                            final Filter followerTarget = Filters.and(Filters.owner(playerId), Filters.or(CardType.COMPANION, CardType.MINION));

                            List<OptionalTriggerAction> optionalTriggerActions = new LinkedList<>();
                            for (final PhysicalCard follower : Filters.filterActive(game, CardType.FOLLOWER, Filters.owner(playerId))) {
                                if (follower.getBlueprint().canPayAidCost(game, follower)
                                        && PlayConditions.isActive(game, followerTarget)) {
                                    final OptionalTriggerAction action = new OptionalTriggerAction(follower);
                                    action.setText("Use " + GameUtils.getCardLink(follower) + " Aid");
                                    follower.getBlueprint().appendAidCosts(game, action, follower);
                                    // The characters the follower could be transferred to: its owner's, other
                                    // than the one that bears it, and none of them while something says that
                                    // the follower cannot be transferred ("followers cannot be transferred to a
                                    // character", Streets of Bree 18_140).  TransferPermanentEffect asks that
                                    // too, and used to refuse the transfer after the character had been asked
                                    // for (F-0363).
                                    final Filter couldTakeFollower = Filters.and(followerTarget, Filters.not(Filters.hasAttached(follower)),
                                            (Filter) (g, character) -> g.getModifiersQuerying().canBeTransferred(g, follower));
                                    // With nobody to take it the Aid is offered all the same, as any action
                                    // with a cost is: the cost is paid, nobody is asked for a character and
                                    // nothing moves.  The offer says so beforehand (the no-effect flag).
                                    action.setNoEffectJudge(
                                            () -> Filters.countActive(game, couldTakeFollower) > 0 ? null : NoEffect.EFFECT);
                                    action.appendCost(
                                            new ChooseActiveCardEffect(follower, playerId, "Choose character to transfer follower to", couldTakeFollower) {
                                                @Override
                                                protected void cardSelected(LotroGame game, PhysicalCard card) {
                                                    action.appendEffect(
                                                            new TransferPermanentEffect(follower, card));
                                                }
                                            });
                                    optionalTriggerActions.add(action);
                                }
                            }
                            return optionalTriggerActions;
                        }
                        return null;
                    }
                });
    }
}
