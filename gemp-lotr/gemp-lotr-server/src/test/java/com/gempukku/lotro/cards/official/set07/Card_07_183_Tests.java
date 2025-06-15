package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_183_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    put("mind", "7_183");
                    put("shotgun", "1_231");
                    put("breath1", "1_207");
                    put("breath2", "1_207");
                    put("breath3", "1_207");
                    put("charge", "1_223");

                    put("sam", "1_311");
                    put("merry", "1_302");
                    put("pippin", "1_307");
                    put("greenleaf", "1_50");
                    put("bounder", "1_286");

                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }

    @Test
    public void MindAndBodyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 7
         * Name: Mind and Body
         * Unique: False
         * Side: Shadow
         * Culture: Ringwraith
         * Twilight Cost: 1
         * Type: Event
         * Subtype: Response
         * Game Text: If a Nazgûl kills a character, wound each character bearing a [RINGWRAITH] condition.
         */

        //Pre-game setup
        var scn = GetScenario();

        var card = scn.GetFreepsCard("mind");

        assertEquals("Mind and Body", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
        assertEquals(1, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void MindAndBodyTriggersOnNormalSkirmishNazgulKill() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        var greenleaf = scn.GetFreepsCard("greenleaf");
        scn.MoveCompanionToTable(sam, merry, pippin, greenleaf);

        var mind = scn.GetShadowCard("mind");
        var shotgun = scn.GetShadowCard("shotgun");
        var breath1 = scn.GetShadowCard("breath1");
        var breath2 = scn.GetShadowCard("breath2");
        var breath3 = scn.GetShadowCard("breath3");
        scn.MoveMinionsToTable(shotgun);
        scn.AttachCardsTo(merry, breath1);
        scn.AttachCardsTo(pippin, breath2);
        scn.AttachCardsTo(pippin, breath3);
        scn.MoveCardsToHand(mind);

        scn.StartGame();

        scn.AddWoundsToChar(greenleaf, 2);
        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(greenleaf, shotgun);
        scn.FreepsResolveSkirmish(greenleaf);

        assertEquals(1, scn.GetVitality(greenleaf));
        assertEquals(11, scn.GetStrength(shotgun));
        assertEquals(6, scn.GetStrength(greenleaf));
        assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
        assertEquals(Zone.HAND, mind.getZone());

        scn.PassCurrentPhaseActions();

        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        assertTrue(scn.ShadowPlayAvailable(mind));
        assertEquals(4, scn.GetVitality(frodo));
        assertEquals(4, scn.GetVitality(sam));
        assertEquals(4, scn.GetVitality(merry));
        assertEquals(4, scn.GetVitality(pippin));
        assertEquals(Zone.DEAD, greenleaf.getZone());

        scn.ShadowAcceptOptionalTrigger();
        assertEquals(4, scn.GetVitality(frodo));
        assertEquals(4, scn.GetVitality(sam));
        assertEquals(3, scn.GetVitality(merry));
        assertEquals(3, scn.GetVitality(pippin));

        assertEquals(Zone.DISCARD, mind.getZone());

    }

    @Test
    public void MindAndBodyTriggersOnlyOnceOnNormalSkirmishNazgulKill() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        var greenleaf = scn.GetFreepsCard("greenleaf");
        scn.MoveCompanionToTable(sam, merry, pippin, greenleaf);

        var mind = scn.GetShadowCard("mind");
        var shotgun = scn.GetShadowCard("shotgun");
        var breath1 = scn.GetShadowCard("breath1");
        var breath2 = scn.GetShadowCard("breath2");
        var breath3 = scn.GetShadowCard("breath3");
        scn.MoveMinionsToTable(shotgun);
        scn.AttachCardsTo(merry, breath1);
        scn.AttachCardsTo(pippin, breath2);
        scn.AttachCardsTo(pippin, breath3);
        scn.MoveCardsToHand(mind);

        scn.StartGame();

        scn.AddWoundsToChar(greenleaf, 2);
        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(greenleaf, shotgun);
        scn.FreepsResolveSkirmish(greenleaf);

        assertEquals(1, scn.GetVitality(greenleaf));
        assertEquals(11, scn.GetStrength(shotgun));
        assertEquals(6, scn.GetStrength(greenleaf));
        assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
        assertEquals(Zone.HAND, mind.getZone());

        scn.PassCurrentPhaseActions();

        assertEquals(Zone.DEAD, greenleaf.getZone());

        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        assertTrue(scn.ShadowPlayAvailable(mind));

        //Ensure we're not double-dipping
        scn.ShadowDeclineOptionalTrigger();
        assertFalse(scn.ShadowHasOptionalTriggerAvailable());

    }

    @Test
    public void MindAndBodyTriggersOnNazgulOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        scn.MoveCompanionToTable(sam, merry, pippin);

        var mind = scn.GetShadowCard("mind");
        var shotgun = scn.GetShadowCard("shotgun");
        scn.MoveMinionsToTable(shotgun);
        scn.MoveCardsToHand(mind);

        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(sam, shotgun);
        scn.FreepsResolveSkirmish(sam);

        assertEquals(11, scn.GetStrength(shotgun));
        assertEquals(3, scn.GetStrength(sam));
        assertEquals(Zone.FREE_CHARACTERS, sam.getZone());
        assertEquals(Zone.HAND, mind.getZone());

        scn.PassCurrentPhaseActions();

        assertEquals(Zone.DEAD, sam.getZone());
        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        assertTrue(scn.ShadowPlayAvailable(mind));

        //Ensure we're not double-dipping
        scn.ShadowDeclineOptionalTrigger();
        assertFalse(scn.ShadowHasOptionalTriggerAvailable());
    }

    @Test
    public void MindAndBodyTriggersOnNazgulAllyKill() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var bounder = scn.GetFreepsCard("bounder");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        scn.MoveCompanionToTable(merry, pippin);
        scn.MoveCardsToSupportArea(bounder);

        var mind = scn.GetShadowCard("mind");
        var shotgun = scn.GetShadowCard("shotgun");
        scn.MoveMinionsToTable(shotgun);
        scn.MoveCardsToHand(mind);

        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(bounder, shotgun);
        scn.FreepsResolveSkirmish(bounder);

        assertEquals(11, scn.GetStrength(shotgun));
        assertEquals(2, scn.GetStrength(bounder));
        assertEquals(Zone.SUPPORT, bounder.getZone());
        assertEquals(Zone.HAND, mind.getZone());

        scn.PassCurrentPhaseActions();

        assertEquals(Zone.DEAD, bounder.getZone());
        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        assertTrue(scn.ShadowPlayAvailable(mind));

        //Ensure we're not double-dipping
        scn.ShadowDeclineOptionalTrigger();
        assertFalse(scn.ShadowHasOptionalTriggerAvailable());
    }

    @Test
    public void MindAndBodyTriggersOnEnqueaKill() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        scn.MoveCompanionToTable(sam);

        var mind = scn.GetShadowCard("mind");
        var shotgun = scn.GetShadowCard("shotgun");
        scn.MoveMinionsToTable(shotgun);
        scn.MoveCardsToHand(mind);

        scn.StartGame();

        scn.AddBurdens(5);
        scn.AddWoundsToChar(sam, 3);

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowActionAvailable(shotgun));
        assertEquals(Zone.FREE_CHARACTERS, sam.getZone());
        assertEquals(1, scn.GetVitality(sam));
        assertEquals(Zone.HAND, mind.getZone());

        scn.ShadowUseCardAction(shotgun);

        assertEquals(Zone.DEAD, sam.getZone());
        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        assertTrue(scn.ShadowPlayAvailable(mind));

        //Ensure we're not double-dipping
        scn.ShadowDeclineOptionalTrigger();
        assertFalse(scn.ShadowHasOptionalTriggerAvailable());
    }

    @Test
    public void MindAndBodyDoesNotTriggerOnRelentlessChargeKill() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var greenleaf = scn.GetFreepsCard("greenleaf");
        scn.MoveCompanionToTable( greenleaf);

        var mind = scn.GetShadowCard("mind");
        var charge = scn.GetShadowCard("charge");
        var shotgun = scn.GetShadowCard("shotgun");
        scn.MoveMinionsToTable(shotgun);
        scn.MoveCardsToHand(mind, charge);

        scn.StartGame();
        scn.AddWoundsToChar(greenleaf, 2);

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowPlayAvailable(charge));
        assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
        assertEquals(1, scn.GetVitality(greenleaf));
        assertEquals(Zone.HAND, mind.getZone());

        scn.ShadowPlayCard(charge);

        assertEquals(Zone.DEAD, greenleaf.getZone());
        assertFalse(scn.ShadowHasOptionalTriggerAvailable());
        assertFalse(scn.ShadowPlayAvailable(mind));
    }
}
