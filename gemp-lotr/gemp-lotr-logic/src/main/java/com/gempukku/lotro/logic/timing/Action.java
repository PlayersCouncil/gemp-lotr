package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface Action {
    public enum Type {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, RECONCILE, RESOLVE_DAMAGE, OTHER
    }

    public Type getType();

    public PhysicalCard getActionSource();

    public void setActionTimeword(Timeword timeword);

    public PhysicalCard getActionAttachedToCard();

    public void setVirtualCardAction(boolean virtualCardAction);

    public boolean isVirtualCardAction();

    public void setPerformingPlayer(String playerId);

    public String getPerformingPlayer();

    public Timeword getActionTimeword();

    public String getText(LotroGame game);

    public Effect nextEffect(LotroGame game);

    /**
     * Whether this action has been judged, before being taken, to do nothing.  The judgement is made once, the
     * first time it is asked for, which is when the action is put in front of a player.
     * @return Why it will do nothing, or null if it is not known to.
     */
    default NoEffect getNoEffect() {
        return null;
    }

    /**
     * What set this action off, for a trigger or response whose definition remembers it ("memorize: winner"): the
     * names of those cards.  It is not part of the action's text.  A decision adds it to the text only where one
     * card offers the same action more than once, so that the offers can be told apart (see OfferedActionTexts).
     * @return The names, or null if the action remembers nothing of the kind.
     */
    default String getOfferedFor() {
        return null;
    }

    /**
     * The ability of its card this action was made from: the one piece of a card's definition (an activated
     * ability, a trigger, a play) that built it.  Two actions of one card are the same ability when this is the
     * same object.  The loop step (LoopTracker) tells one use of an ability from another by it.
     * @return The ability, or null for an action no definition built (the rules' own actions, and a few cards
     * written in Java).
     */
    default ActionSource getAbility() {
        return null;
    }
}
