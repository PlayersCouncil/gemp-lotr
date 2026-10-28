package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_183_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    put("mind", "7_183");
                    put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgûl, strength 11, vitality 4, fierce; "Maneuver: Spot 6 companions (or 5 burdens) and exert Úlairë Enquëa to wound a companion (except the Ring-bearer)."
                    put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a Nazgûl who is not [wraith] ([raider]), strength 12, fierce; his text needs 3 burdens
                    put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is no Nazgûl (Orc), strength 5, vitality 2; his text needs 2 wounds on the Ring-bearer
                    put("breath1", "1_207");    // Black Breath: [wraith] condition, transferred by play to a character skirmishing a Nazgûl; bearer cannot heal
                    put("breath2", "1_207");
                    put("breath3", "1_207");
                    put("twilight", "19_34");   // In Twilight: a [wraith] condition a [wraith] minion bears, strength +2; its triggers answer its play and its bearer's win
                    put("sword", "1_218");      // Nazgûl Sword: a [wraith] possession a Nazgûl bears, strength +2
                    put("defense", "1_244");    // Desperate Defense of the Ring: a [sauron] condition a companion bears; it answers its bearer's assignment
                    put("charge", "1_223");     // Relentless Charge: [wraith] Maneuver event, "Exert a Nazgûl to wound every archer companion."

                    put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
                    put("merry", "1_302");      // Merry, Friend to Sam: strength 3, vitality 4
                    put("pippin", "1_307");     // Pippin, Hobbit of Some Intelligence: strength 3, vitality 4
                    put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3, no archer
                    put("greenleaf", "1_50");   // Legolas, Greenleaf: an archer companion, vitality 3
                    put("bounder1", "1_286");   // Bounder: Hobbit ally, home site 2, strength 2, vitality 2
                    put("bounder2", "1_286");
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
         * Game Text: If a Nazgûl kills a character, wound each character bearing a [wraith] condition.
         */

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
    public void ACompanionKilledByTheWoundForLosingToANazgulLetsItBePlayedToWoundEachBearerOfAWraithConditionOnce() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        var boromir = scn.GetFreepsCard("boromir");
        scn.MoveCompanionsToTable(sam, merry, pippin, boromir);

        var mind = scn.GetShadowCard("mind");
        var enquea = scn.GetShadowCard("enquea");
        var breath1 = scn.GetShadowCard("breath1");
        var breath2 = scn.GetShadowCard("breath2");
        var breath3 = scn.GetShadowCard("breath3");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea);
        // Merry bears one [wraith] condition and Pippin two.
        scn.AttachCardsTo(merry, breath1);
        scn.AttachCardsTo(pippin, breath2, breath3);
        scn.MoveCardsToHand(mind);
        scn.AddWoundsToChar(boromir, 2);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(boromir, enquea);
        scn.FreepsResolveSkirmish(boromir);

        assertEquals(1, scn.GetVitality(boromir));
        assertEquals(11, scn.GetStrength(enquea));
        assertEquals(7, scn.GetStrength(boromir));

        scn.PassSkirmishActions();

        // Boromir loses 7 to 11, and the one wound kills him.
        assertInZone(Zone.DEAD, boromir);
        assertTrue(scn.ShadowPlayAvailable(mind));
        // 5 companions + 2 for site 2
        assertEquals(7, scn.GetTwilight());
        assertEquals(0, scn.GetWoundsOn(merry));
        assertEquals(0, scn.GetWoundsOn(pippin));

        scn.ShadowPlayCard(mind);

        assertInDiscard(mind);
        assertEquals(1, scn.GetWoundsOn(merry));
        // One wound for the character, not one for each condition.
        assertEquals(1, scn.GetWoundsOn(pippin));
        assertEquals(0, scn.GetWoundsOn(sam));
        assertEquals(0, scn.GetWoundsOn(frodo));
        assertEquals(0, scn.GetWoundsOn(enquea));
        // Enquëa is fierce: the game goes on to the fierce assignments.
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
    }

    @Test
    public void ACompanionOverwhelmedByANazgulLetsItBePlayedAndDecliningLeavesEveryoneAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        scn.MoveCompanionsToTable(sam, merry);

        var mind = scn.GetShadowCard("mind");
        var enquea = scn.GetShadowCard("enquea");
        var breath1 = scn.GetShadowCard("breath1");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea);
        scn.AttachCardsTo(merry, breath1);
        scn.MoveCardsToHand(mind);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(sam, enquea);
        scn.FreepsResolveSkirmish(sam);

        assertEquals(11, scn.GetStrength(enquea));
        assertEquals(3, scn.GetStrength(sam));
        assertEquals(0, scn.GetWoundsOn(sam));

        scn.PassSkirmishActions();

        // 11 is more than double 3: Sam is killed with no wound placed.
        assertInZone(Zone.DEAD, sam);
        assertTrue(scn.ShadowPlayAvailable(mind));
        // 3 companions + 2 for site 2
        assertEquals(5, scn.GetTwilight());

        scn.ShadowDeclineOptionalTrigger();

        // It is not offered a second time for the same death: the Shadow player is asked nothing more.
        assertFalse(scn.ShadowAnyDecisionsAvailable());
        assertInZone(Zone.HAND, mind);
        assertEquals(5, scn.GetTwilight());
        assertEquals(0, scn.GetWoundsOn(merry));
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
    }

    @Test
    public void AnAllyKilledByANazgulLetsItBePlayedAndAnAllyBearingAWraithConditionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var bounder = scn.GetFreepsCard("bounder1");
        var other = scn.GetFreepsCard("bounder2");
        scn.MoveAlliesToTable(bounder, other);

        var mind = scn.GetShadowCard("mind");
        var enquea = scn.GetShadowCard("enquea");
        var breath1 = scn.GetShadowCard("breath1");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea);
        scn.AttachCardsTo(other, breath1);
        scn.MoveCardsToHand(mind);

        // Site 2 is the Bounders' home, where they may be assigned.
        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(bounder, enquea);
        scn.FreepsResolveSkirmish(bounder);

        assertEquals(11, scn.GetStrength(enquea));
        assertEquals(2, scn.GetStrength(bounder));

        scn.PassSkirmishActions();

        assertInZone(Zone.DEAD, bounder);
        assertTrue(scn.ShadowPlayAvailable(mind));
        assertEquals(0, scn.GetWoundsOn(other));

        scn.ShadowPlayCard(mind);

        assertInDiscard(mind);
        assertEquals(1, scn.GetWoundsOn(other));
        assertEquals(0, scn.GetWoundsOn(frodo));
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
    }

    @Test
    public void ACompanionKilledByANazgulsOwnAbilityLetsItBePlayedAndOnlyBearersOfAWraithConditionAreWounded() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var sam = scn.GetFreepsCard("sam");
        var merry = scn.GetFreepsCard("merry");
        var pippin = scn.GetFreepsCard("pippin");
        scn.MoveCompanionsToTable(sam, merry, pippin);

        var mind = scn.GetShadowCard("mind");
        var enquea = scn.GetShadowCard("enquea");
        var hound = scn.GetShadowCard("hound");
        var breath1 = scn.GetShadowCard("breath1");
        var breath2 = scn.GetShadowCard("breath2");
        var twilight = scn.GetShadowCard("twilight");
        var sword = scn.GetShadowCard("sword");
        var defense = scn.GetShadowCard("defense");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea, hound);
        // A [wraith] condition on a companion and on a minion; a [sauron] condition on a companion; a [wraith]
        // possession on a minion; and a [wraith] condition in the support area, which nobody bears.
        scn.AttachCardsTo(merry, breath1);
        scn.AttachCardsTo(hound, twilight);
        scn.AttachCardsTo(pippin, defense);
        scn.AttachCardsTo(enquea, sword);
        scn.MoveCardsToSupportArea(breath2);
        scn.MoveCardsToHand(mind);
        scn.AddBurdens(5);
        scn.AddWoundsToChar(sam, 3);

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPass();

        assertTrue(scn.ShadowActionAvailable(enquea));
        assertFalse(scn.ShadowPlayAvailable(mind));

        scn.ShadowUseCardAction(enquea);
        scn.ShadowChooseCard(sam);

        assertInZone(Zone.DEAD, sam);
        assertTrue(scn.ShadowPlayAvailable(mind));
        // 4 companions + 2 for site 2
        assertEquals(6, scn.GetTwilight());
        assertEquals(0, scn.GetWoundsOn(merry));
        assertEquals(0, scn.GetWoundsOn(hound));
        assertEquals(1, scn.GetWoundsOn(enquea));

        scn.ShadowPlayCard(mind);

        assertInDiscard(mind);
        assertEquals(1, scn.GetWoundsOn(merry));
        assertEquals(1, scn.GetWoundsOn(hound));
        assertEquals(0, scn.GetWoundsOn(pippin));
        // Enquëa has the one wound he exerted for and no more.
        assertEquals(1, scn.GetWoundsOn(enquea));
        assertEquals(0, scn.GetWoundsOn(frodo));
        assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
    }

    @Test
    public void ACompanionKilledByAWraithEventANazgulExertedForDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var greenleaf = scn.GetFreepsCard("greenleaf");
        scn.MoveCompanionsToTable(greenleaf);

        var mind = scn.GetShadowCard("mind");
        var charge = scn.GetShadowCard("charge");
        var enquea = scn.GetShadowCard("enquea");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea);
        scn.MoveCardsToHand(mind, charge);
        scn.AddWoundsToChar(greenleaf, 2);

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPass();

        assertTrue(scn.ShadowPlayAvailable(charge));
        assertEquals(1, scn.GetVitality(greenleaf));
        // 2 companions + 2 for site 2: enough for Mind and Body
        assertEquals(4, scn.GetTwilight());

        scn.ShadowPlayCard(charge);

        // The event wounded the archer; the Nazgûl only paid for it.
        assertInZone(Zone.DEAD, greenleaf);
        assertEquals(1, scn.GetWoundsOn(enquea));
        // The Shadow player is asked nothing: the game has gone on to the Free Peoples player's maneuver action.
        assertFalse(scn.ShadowAnyDecisionsAvailable());
        assertInZone(Zone.HAND, mind);
        assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
    }

    @Test
    public void ACompanionKilledByAWraithOrcOrWoundedByANazgulAndLeftAliveDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var sam = scn.GetFreepsCard("sam");
        var boromir = scn.GetFreepsCard("boromir");
        scn.MoveCompanionsToTable(sam, boromir);

        var mind = scn.GetShadowCard("mind");
        var enquea = scn.GetShadowCard("enquea");
        var hound = scn.GetShadowCard("hound");

        scn.StartGame();
        scn.MoveMinionsToTable(enquea, hound);
        scn.MoveCardsToHand(mind);
        scn.AddWoundsToChar(sam, 3);
        scn.AddWoundsToChar(boromir, 1);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, enquea}, new PhysicalCardImpl[]{sam, hound});
        scn.FreepsResolveSkirmish(boromir);
        scn.PassSkirmishActions();

        // Boromir loses 7 to 11 to the Nazgûl, takes the wound and lives.
        assertInZone(Zone.FREE_CHARACTERS, boromir);
        assertEquals(2, scn.GetWoundsOn(boromir));
        // The Shadow player is asked nothing: the game has gone on to the choice of the next skirmish.
        assertFalse(scn.ShadowAnyDecisionsAvailable());
        assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

        scn.FreepsResolveSkirmish(sam);
        scn.PassSkirmishActions();

        // Sam loses 3 to 5 to the [wraith] Orc, and the wound kills him; the Nazgûl is in play, and it was not his kill.
        assertInZone(Zone.DEAD, sam);
        // 3 companions + 2 for site 2: enough for Mind and Body
        assertEquals(5, scn.GetTwilight());
        assertFalse(scn.ShadowAnyDecisionsAvailable());
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

        // The fierce round: Boromir, with one vitality left, loses to Enquëa again.
        scn.PassFierceAssignmentActions();
        scn.FreepsAssignToMinions(boromir, enquea);
        scn.FreepsResolveSkirmish(boromir);
        scn.PassFierceSkirmishActions();

        assertInZone(Zone.DEAD, boromir);
        assertTrue(scn.ShadowPlayAvailable(mind));
        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
    }

    @Test
    public void ANazgulWhoIsNotRingwraithKillingACompanionLetsItBePlayedThoughNobodyBearsAWraithCondition() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var boromir = scn.GetFreepsCard("boromir");
        var pippin = scn.GetFreepsCard("pippin");
        scn.MoveCompanionsToTable(boromir, pippin);

        var mind = scn.GetShadowCard("mind");
        var khamul = scn.GetShadowCard("khamul");
        var defense = scn.GetShadowCard("defense");

        scn.StartGame();
        scn.MoveMinionsToTable(khamul);
        scn.AttachCardsTo(pippin, defense);
        scn.MoveCardsToHand(mind);
        scn.AddWoundsToChar(boromir, 2);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(boromir, khamul);
        scn.FreepsResolveSkirmish(boromir);

        assertEquals(12, scn.GetStrength(khamul));
        assertEquals(7, scn.GetStrength(boromir));

        scn.PassSkirmishActions();

        assertInZone(Zone.DEAD, boromir);
        assertTrue(scn.ShadowPlayAvailable(mind));
        // 3 companions + 2 for site 2
        assertEquals(5, scn.GetTwilight());

        scn.ShadowPlayCard(mind);

        // Played for nothing: nobody bears a [wraith] condition.
        assertInDiscard(mind);
        assertEquals(0, scn.GetWoundsOn(pippin));
        assertEquals(0, scn.GetWoundsOn(frodo));
        assertEquals(0, scn.GetWoundsOn(khamul));
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
    }
}
