package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_066_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "103_66");
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, cost 4, site 3; weakens bearers of [wraith] conditions
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: twilight [wraith] Nazgul, cost 5, site 2
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: the [raider] Nazgul, cost 6, site 3; fierce
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: [wraith] Nazgul, cost 8, site 3; fierce
					put("lackey", "7_193");     // Morgul Lackey: [wraith] Orc, cost 2, site 4; a skirmish ability

					put("galadriel", "13_15");  // Galadriel, Sorceress of the Hidden Land: costs 0; text only on a regroup move
					put("smeagol", "5_29");     // Smeagol, Slinker: costs 0, Ring-bound; a cost to play, so he is cheated in
					put("merry", "1_302");      // Merry, Friend to Sam: costs 1
					put("sam", "1_311");        // Sam, Son of Hamfast: costs 2, Ring-bound and not the Ring-bearer
					put("gimli", "1_13");       // Gimli, Son of Gloin: costs 2
					put("boromir", "1_97");     // Boromir, Son of Denethor: costs 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: costs 4
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: ally costing 2, home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gollum, Half a Wraith Himself
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype:
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Each time you play a Nazgul, you may exert Gollum to add a threat.
		 * 	Assignment: Remove X threats to assign Gollum to a companion costing X or less (except the Ring-bearer).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Half a Wraith Himself", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForTwoAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(gollum);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		scn.ShadowPlayCard(gollum);

		// 2 to play + 2 roaming (his site number is 3, the fellowship is at site 2)
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// He is no Nazgul: his own play is not answered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachNazgulPlayedLetsGollumExertToAddAThreatUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var lackey = scn.GetShadowCard("lackey");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(lackey, lemenya, nelya, khamul, witchking);
		// Four companions with Frodo, so three threats are within the limit.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(40);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());

		// A [wraith] minion that is not a Nazgul.
		scn.ShadowPlayCard(lackey);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());

		// A twilight Nazgul.
		scn.ShadowPlayCard(nelya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetThreats());

		// The [raider] Nazgul.
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(3, scn.GetThreats());

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		scn.ShadowPlayCard(witchking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NazgulTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheThreatLimitGollumMayStillExertAndNoThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Frodo alone: one companion, so one threat is all there can be.
		scn.AddThreats(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());

		// The exertion is a cost, so the offer is made whether or not a threat can be added.
		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentAbilityRemovesXThreatsToAssignGollumToACompanionCostingXOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(merry, sam, gimli, boromir, aragorn);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddThreats(5);
		scn.MoveMinionsToTable(gollum);

		// Not a maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Site 3 is Silinde's home, where an ally may be assigned to a skirmish.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertEquals(5, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(gollum));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChoose("2");

		assertEquals(3, scn.GetThreats());
		// Costing 2 (Sam, who is Ring-bound, and Gimli) or less (Merry). Not Boromir at 3 or Aragorn at
		// 4, not the Ring-bearer for all that he costs 0, and not an ally costing 2.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, aragorn, frodo, silinde));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertTrue(scn.IsCharAssignedAgainst(sam, gollum));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Nor a skirmish ability: in the skirmish that follows, with threats still there to remove.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void RemovingFourThreatsReachesACompanionCostingFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, sam, boromir, aragorn);

		scn.StartGame();
		scn.AddThreats(4);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(4, scn.GetThreats());
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChoose("4");

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, sam, boromir, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoThreatsXIsZeroAndReachesACompanionCostingNothingButNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var galadriel = scn.GetFreepsCard("galadriel");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(galadriel, smeagol, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("0");

		// Frodo costs 0 as well, and Smeagol is Ring-bound: only the Ring-bearer is excepted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(galadriel, smeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo));
		scn.ShadowChooseCard(smeagol);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(smeagol, gollum));
		assertFalse(scn.IsCharAssigned(galadriel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void XCannotBeMoreThanTheThreatsThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, sam, aragorn);

		scn.StartGame();
		scn.AddThreats(2);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());
		scn.ShadowUseCardAction(gollum);

		// Two threats, so 2 at most: Aragorn at 4 is out of reach.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(merry);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(merry, gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void XIsOfferedNoHigherThanTheDearestCompanionCosts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddThreats(5);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetThreats());
		scn.ShadowUseCardAction(gollum);

		// Five threats, but nobody costs more than Sam's 2: the game offers 2 at most.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// Sam is the only companion within reach and is chosen without asking.
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(sam, gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	@Ignore("Q-0087: is X capped at what the dearest companion costs? The text says only 'Remove X threats'; the game offers no more than the sum of the costs of the companions tied for dearest (4 here)")
	public void XMayBeAnyNumberOfTheThreatsThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, gimli);

		scn.StartGame();
		scn.AddThreats(5);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetThreats());
		scn.ShadowUseCardAction(gollum);

		// Sam and Gimli both cost 2. "Remove X threats ... costing X or less": X is the player's, up to
		// the threats there are.
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("5");
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, gimli));
		scn.ShadowChooseCard(gimli);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(gimli, gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	@Ignore("F-0132: with no threats and no companion costing 0 but the Ring-bearer, the ability is offered and asks for a number from 0 to 0; it does nothing and can be taken without end")
	public void NotOfferedWithNoThreatsAndNoCompanionCostingNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// X could only be 0, and nobody but the Ring-bearer costs 0: nothing to pay and nothing to do.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	@Ignore("F-0132: once Gollum is assigned and no threats are left the ability is still offered; X can only be 0, nobody can be assigned, and it can be taken without end")
	public void NotOfferedAgainForNothingOnceGollumIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var galadriel = scn.GetFreepsCard("galadriel");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(galadriel, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChoose("0");
		scn.ShadowChooseCard(smeagol);
		assertTrue(scn.IsCharAssignedAgainst(smeagol, gollum));

		// Galadriel costs 0 and is unassigned, but Gollum is assigned already (an assignment action
		// needs both unassigned): X could only be 0 and would do nothing.
		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	@Ignore("F-0132: X may be named 0 with no companion costing 0 but the Ring-bearer; that removes nothing, assigns nobody and can be repeated without end")
	public void XStartsAtOneWhenNoCompanionButTheRingBearerCostsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.AddThreats(3);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChoose("1");

		// Merry is the only companion costing 1 or less who is not the Ring-bearer.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(merry, gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
