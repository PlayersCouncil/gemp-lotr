package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.Map;

public interface ActionSource {
    PlayerSource getPlayer();
    boolean requiresRanger();

    /**
     * A special ability is text with a bold phase word and a colon ("Shadow: ...", "Response: ..."), wherever the
     * card is when it is used.  A card's own permission to be played from somewhere else, with no phase word ("you
     * may play this minion from your discard pile"), is offered to the player in the same way but is not one.
     * @return False if this action is such a permission to play the card; true otherwise.
     */
    default boolean isSpecialAbility() {
        return true;
    }

    boolean isValid(ActionContext actionContext);

    void createAction(CostToEffectAction action, ActionContext actionContext);

    /**
     * Whether the uses of this ability are limited ("limit once per phase", "(limit +3)", "limit once per site"):
     * such an ability cannot be used for ever, so the loop step never takes it for part of a loop.
     * @return True if the definition sets any limit on its uses or on what it gives.
     */
    default boolean isLimited() {
        return false;
    }

    /**
     * The wounds one use of this action would place in paying its costs, added to the tally: what each cost
     * says of one paying of it (EffectAppender.tallyWoundsOfPayingOnce), each asked with the wounds of the costs
     * before it in place.  Asked by the toil rule of a response to the toil being paid, to count its uses in a
     * row (ToilRule, Q-0381).
     */
    default void tallyWoundsOfPayingCosts(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
    }
}
