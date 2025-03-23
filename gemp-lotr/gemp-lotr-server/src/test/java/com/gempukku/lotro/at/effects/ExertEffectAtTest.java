package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ExertEffectAtTest extends AbstractAtTest {
    @Test
    public void chooseAndExertTwiceAndCanOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addWound(merry);
        _game.getGameState().addWound(merry);

        ActivateCardAction action = new ActivateCardAction(merry, AbstractAtTest.P1);
        ChooseAndExertCharactersEffect exertEffect = new ChooseAndExertCharactersEffect(action, P1, 1, 1, 2, CardType.COMPANION, Filters.not(Filters.ringBearer));
        action.appendEffect(exertEffect);

        carryOutEffectInPhaseActionByPlayer(P1, action);

        assertEquals(3, _game.getGameState().getWounds(merry));
        assertFalse(exertEffect.wasCarriedOut());
    }
}