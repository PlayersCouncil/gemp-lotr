package com.gempukku.lotro.logic.cardtype;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

public class AbstractPermanent extends AbstractLotroCardBlueprint {
    public AbstractPermanent(Side side, int twilightCost, CardType cardType, Culture culture, String name) {
        this(side, twilightCost, cardType, culture, name, null, false);
    }

    public AbstractPermanent(Side side, int twilightCost, CardType cardType, Culture culture, String name, String subTitle, boolean unique) {
        super(twilightCost, side, cardType, culture, name, subTitle, unique);
        if (cardType != CardType.COMPANION && cardType!= CardType.MINION)
            addKeyword(Keyword.SUPPORT_AREA);
    }

    private Zone getPlayToZone() {
        final CardType cardType = getCardType();
        switch (cardType) {
            case COMPANION:
                return Zone.FREE_CHARACTERS;
            case MINION:
                return Zone.SHADOW_CHARACTERS;
            default:
                return Zone.SUPPORT;
        }
    }

    @Override
    public final PlayPermanentAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        PlayPermanentAction action = new PlayPermanentAction(self, getPlayToZone(), twilightModifier, ignoreRoamingPenalty);

        game.getModifiersQuerying().appendExtraCosts(game, action, self);
        game.getModifiersQuerying().appendPotentialDiscounts(game, action, self);

        return action;
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && PlayConditions.checkUniqueness(game, self, ignoreCheckingDeadPile);
    }

    protected List<? extends Action> getExtraPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    protected Phase getExtraPlayableInPhase(LotroGame game) {
        return null;
    }

    @Override
    public final List<? extends Action> getPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        final Phase phase = (getSide() == Side.FREE_PEOPLE) ? Phase.FELLOWSHIP : Phase.SHADOW;
        if (PlayConditions.canPlayCardDuringPhase(game, phase, self)
                && checkPlayRequirements(playerId, game, self, 0, 0, false, false))
            return Collections.singletonList(getPlayCardAction(playerId, game, self, 0, false));

        Phase extraPhase = getExtraPlayableInPhase(game);
        if (extraPhase != null)
            if (PlayConditions.canPlayCardDuringPhase(game, extraPhase, self)
                    && checkPlayRequirements(playerId, game, self, 0, 0, false, false))
                return Collections.singletonList(getPlayCardAction(playerId, game, self, 0, false));


        return getExtraPhaseActions(playerId, game, self);
    }

    @Override
    public final List<? extends Action> getOptionalBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (self.getZone().isInPlay())
            return getOptionalInPlayBeforeActions(playerId, game, effect, self);
        return null;
    }

    @Override
    public final List<? extends Action> getOptionalAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (self.getZone().isInPlay())
            return getOptionalInPlayAfterActions(playerId, game, effectResult, self);
        return null;
    }

    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        return null;
    }

    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }
}