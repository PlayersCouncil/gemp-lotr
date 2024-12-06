package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// Wielder of the Flame
public class Card_18_118_Tests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("lurtz", "18_118");

                }}
        );
    }

    @Test
    public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 18
         * Name: Lurtz, Halfling Hunter
         * Unique: True
         * Side: Shadow
         * Culture: Uruk-hai
         * Twilight Cost: 7
         * Type: Minion
         * Subtype: Uruk-hai
         * Strength: 13
         * Vitality: 3
         * Site Number: 5
         * Game Text: <b>Archer</b>. <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 6 companions to make Lurtz <b>fierce</b> until the regroup phase.<br><b>Assignment:</b> Exert Lurtz twice to assign it to an unbound companion. The Free Peoples player may exert that companion or add a burden to prevent this.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("lurtz");

        assertEquals("Lurtz", card.getBlueprint().getTitle());
        assertEquals("Halfling Hunter", card.getBlueprint().getSubtitle());
        assertTrue(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
        assertEquals(CardType.MINION, card.getBlueprint().getCardType());
        assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
        assertTrue(scn.hasKeyword(card, Keyword.ARCHER));
        assertTrue(scn.hasKeyword(card, Keyword.DAMAGE));
        assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
        assertEquals(7, card.getBlueprint().getTwilightCost());
        assertEquals(13, card.getBlueprint().getStrength());
        assertEquals(3, card.getBlueprint().getVitality());
        assertEquals(5, card.getBlueprint().getSiteNumber());
    }

    //TODO: Finish this
    @Test
    public void LurtzAbilityOffersChoiceOfPrevention() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl frodo = scn.GetRingBearer();
        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        PhysicalCardImpl lurtz = scn.GetShadowCard("lurtz");
        scn.ShadowMoveCharToTable(lurtz);

        scn.StartGame();

        scn.SkipToPhase(Phase.ARCHERY);
        scn.PassCurrentPhaseActions();
        scn.FreepsChooseCard(frodo);

        //assignment phase
        scn.FreepsPassCurrentPhaseAction();
        assertEquals(0, scn.GetWoundsOn(lurtz));

        scn.ShadowUseCardAction(lurtz);
        assertEquals(2, scn.GetWoundsOn(lurtz));
        assertTrue(scn.FreepsAnyDecisionsAvailable());
        scn.FreepsAcceptOptionalTrigger();
        assertTrue(scn.FreepsAnyDecisionsAvailable());
        assertEquals("Exert - <div class='cardHint' value='1_89'>·Aragorn, Ranger of the North</div>", scn.FreepsGetADParamAsList("results").toArray()[0]);
        assertEquals("Add 1 burden", scn.FreepsGetADParamAsList("results").toArray()[1]);
    }



}