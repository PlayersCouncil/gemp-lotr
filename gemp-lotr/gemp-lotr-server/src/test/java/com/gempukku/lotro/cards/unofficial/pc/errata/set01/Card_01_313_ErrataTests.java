package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_313_ErrataTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("sting", "51_313");
                    put("sam", "1_311");
                    put("merry", "1_302");

                    put("orc1", "1_178");
                    put("orc2", "1_178");
                    put("orc3", "1_178");
                    put("scimitar1", "1_180");
                    put("scimitar2", "1_180");
                    put("scimitar3", "1_180");
                }}
        );
    }

    @Test
    public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Sting
         * Side: Free Peoples
         * Culture: Shire
         * Twilight Cost: 1
         * Strength: +2
         * Type: Possession
         * Subtype: Hand Weapon
         * Errata Game Text: Bearer must be Frodo.
         * Fellowship or Regroup: Exert Frodo to reveal 4 cards at random from an opponent's hand.  Remove (1) for each Orc revealed (limit (2)).
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var sting = scn.GetFreepsCard("sting");

        assertTrue(sting.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, sting.getBlueprint().getSide());
        assertEquals(Culture.SHIRE, sting.getBlueprint().getCulture());
        assertEquals(CardType.POSSESSION, sting.getBlueprint().getCardType());
        assertTrue(sting.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
        assertEquals(1, sting.getBlueprint().getTwilightCost());
        assertEquals(2, sting.getBlueprint().getStrength());
    }


    @Test
    public void StingCanOnlyBeBorneByFrodo() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var sting = scn.GetFreepsCard("sting");

        scn.MoveCompanionsToTable(sam);
        scn.MoveCardsToHand(sting);

        scn.StartGame();

        scn.FreepsPlayCard(sting);

        //should have automatically gone to frodo as the only valid target
        Assert.assertEquals(Zone.ATTACHED, sting.getZone());
        Assert.assertEquals(frodo, sting.getAttachedTo());
    }

    @Test
    public void StingAbilityAvailableInBothFellowshipAndRegroup() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sting = scn.GetFreepsCard("sting");

        scn.AttachCardsTo(frodo, sting);

        scn.StartGame();

        assertTrue(scn.FreepsActionAvailable(sting));

        scn.SkipToPhase(Phase.REGROUP);

        assertTrue(scn.FreepsActionAvailable(sting));
    }

    @Test
    public void StingAbilityExertsFrodoAndRevealsFourCards() throws DecisionResultInvalidException, CardNotFoundException {
        VirtualTableScenario scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sting = scn.GetFreepsCard("sting");

        var orc1 = scn.GetShadowCard("orc1");
        var orc2 = scn.GetShadowCard("orc2");
        var orc3 = scn.GetShadowCard("orc3");
        var scimitar1 = scn.GetShadowCard("scimitar1");

        scn.AttachCardsTo(frodo, sting);

        scn.MoveCardsToHand(orc1, orc2, orc3, scimitar1);

        scn.StartGame();

        scn.FreepsUseCardAction(sting);

        //Reveals 2 cards
        assertEquals(4, scn.FreepsGetADParamAsList("blueprintId").size());
        assertEquals(1, scn.GetWoundsOn(frodo));
    }

    @Test
    public void StingAbilityRemovesTwilight() throws DecisionResultInvalidException, CardNotFoundException {
        VirtualTableScenario scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sting = scn.GetFreepsCard("sting");
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");

        var orc1 = scn.GetShadowCard("orc1");

        scn.MoveCompanionsToTable(sam, merry);
        scn.AttachCardsTo(frodo, sting);

        scn.MoveCardsToHand(orc1);

        scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsUseCardAction(sting);
        //Both players need to dismiss the card reveal dialog.
        scn.FreepsDismissRevealedCards();
        scn.ShadowDismissRevealedCards();

        // 10 twilight - 1 orc in the hand = 9 twilight
        assertEquals(9, scn.GetTwilight());

        scn.FreepsUseCardAction(sting);
        scn.FreepsDismissRevealedCards();
        scn.ShadowDismissRevealedCards();
        // 9 twilight - 1 orc in the hand = 8 twilight
        assertEquals(8, scn.GetTwilight());

        scn.FreepsUseCardAction(sting);
        scn.FreepsDismissRevealedCards();
        scn.ShadowDismissRevealedCards();
        // limit of 2 should have been hit, so no twilight should be removed
        assertEquals(8, scn.GetTwilight());

    }

}
