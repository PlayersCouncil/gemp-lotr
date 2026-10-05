package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("harry", "101_38");

					// Nazgul
					put("lemenya", "1_232");    // Ulaire Lemenya: unique, cost 4, site 3; weakens characters bearing [ringwraith] conditions, and none does
					put("wk", "1_237");         // The Witch-king, Lord of Angmar: unique, cost 8, site 3; fierce, stronger for each other Nazgul
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: unique, cost 5, site 2; twilight
					put("rider", "12_161");     // Black Rider: not unique, cost 5, site 3; fierce, stronger for each companion
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, cost 6, site 3; its text needs 3 burdens

					// [ringwraith] cards that are not Nazgul
					put("brawler", "7_187");    // Morgul Brawler: Orc, cost 4, site 4; its text needs an exhausted Ring-bearer
					put("sword", "1_218");      // Nazgul Sword: possession, cost 1, bearer must be a Nazgul

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("farin", "1_11");       // Farin, Dwarven Emissary: strength 5; stronger only against an Orc
					put("guard", "1_7");        // Dwarf Guard: strength 4, no text once in play
					put("pippin", "1_307");     // Pippin, Hobbit of Some Intelligence: strength 3; his text is for when he is played
					put("hobbitsword", "1_299"); // Hobbit Sword: strength +2 and no text
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, strength 8, home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HarryGoatleafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Harry Goatleaf, In Thrall to the Nine
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 1
		 * Site Number: 2
		 * Game Text: Nazgul are twilight cost -1.
		 * 	Assignment: Make the Free Peoples player assign a companion with 5 strength or more to skirmish this minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("harry");

		assertEquals("Harry Goatleaf", card.getBlueprint().getTitle());
		assertEquals("In Thrall to the Nine", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysForOneTwilightAndLeavesANazgulPlayableForOneLessButNotAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harry = scn.GetShadowCard("harry");
		var lemenya = scn.GetShadowCard("lemenya");
		var brawler = scn.GetShadowCard("brawler");
		scn.MoveCardsToHand(harry, lemenya, brawler);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		// Each costs 4 + 2 roaming at site 2, which is exactly the pool.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		assertTrue(scn.ShadowPlayAvailable(brawler));
		assertInZone(Zone.HAND, harry);

		scn.ShadowPlayCard(harry);

		assertInZone(Zone.SHADOW_CHARACTERS, harry);
		// Harry's site number is 2, so at site 2 he is not roaming: 6 - 1
		assertEquals(5, scn.GetTwilight());
		// The Nazgul now costs 4 - 1 + 2 roaming = 5. The [ringwraith] Orc is no Nazgul and still costs 6.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		assertFalse(scn.ShadowPlayAvailable(brawler));

		scn.ShadowPlayCard(lemenya);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, brawler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EveryNazgulCostsOneLessOnceHarryIsInPlayAndOtherRingwraithCardsCostWhatTheyDid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harry = scn.GetShadowCard("harry");
		var lemenya = scn.GetShadowCard("lemenya");
		var wk = scn.GetShadowCard("wk");
		var nelya = scn.GetShadowCard("nelya");
		var rider = scn.GetShadowCard("rider");
		var khamul = scn.GetShadowCard("khamul");
		var brawler = scn.GetShadowCard("brawler");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(harry, lemenya, wk, nelya, rider, khamul, brawler, sword);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 40 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(43, scn.GetTwilight());

		// Before Harry: 4 + 2 roaming
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(37, scn.GetTwilight());

		scn.ShadowPlayCard(harry);
		assertInZone(Zone.SHADOW_CHARACTERS, harry);
		assertEquals(36, scn.GetTwilight());

		// A roaming Nazgul: 8 - 1 + 2 roaming
		scn.ShadowPlayCard(wk);
		assertInZone(Zone.SHADOW_CHARACTERS, wk);
		assertEquals(27, scn.GetTwilight());

		// A twilight Nazgul whose site number is 2, so not roaming: 5 - 1
		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(23, scn.GetTwilight());

		// A Nazgul that is not unique: 5 - 1 + 2 roaming
		scn.ShadowPlayCard(rider);
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertEquals(17, scn.GetTwilight());

		// A Nazgul of another culture, [raider]: 6 - 1 + 2 roaming
		scn.ShadowPlayCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertEquals(10, scn.GetTwilight());

		// A [ringwraith] Orc: 4 + 2 roaming, nothing off
		scn.ShadowPlayCard(brawler);
		assertInZone(Zone.SHADOW_CHARACTERS, brawler);
		assertEquals(4, scn.GetTwilight());

		// A [ringwraith] possession that only a Nazgul may bear: its printed 1
		scn.ShadowPlayCard(sword);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, wk, nelya, rider, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(harry, brawler));
		scn.ShadowChooseCard(wk);
		assertAttachedTo(sword, wk);
		assertEquals(3, scn.GetTwilight());

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentActionMakesTheFreePeoplesPlayerAssignACompanionOfStrengthFiveOrMoreToHarry() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harry = scn.GetShadowCard("harry");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, farin, guard, pippin);
		scn.AttachCardsTo(pippin, hobbitsword);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// The fellowship fights at site 3, Elrond's home, where an ally may be assigned like a companion.
		scn.SkipToSite(2);
		// A second minion, so that "this minion" has another beside it
		scn.MoveMinionsToTable(harry, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(farin));
		// 3 + 2 for the Hobbit Sword
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(guard));
		// 3 + 1 for The One Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.FreepsCanAssign(elrond));

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(harry));
		assertFalse(scn.IsCharAssigned(harry));
		assertFalse(scn.IsCharAssigned(lemenya));

		scn.ShadowUseCardAction(harry);

		// The choice is the Free Peoples player's, and is not one he may decline.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Strength 8, strength exactly 5, and strength 5 only with the sword
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, farin, pippin));
		// Two companions of strength 4, and an ally of strength 8 who could be assigned here
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, frodo, elrond));
		scn.FreepsChooseCard(farin);

		assertTrue(scn.IsCharAssignedAgainst(farin, harry));
		// It is Harry he is made to face, and not the Nazgul beside him.
		assertFalse(scn.IsCharAssigned(lemenya));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(pippin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionIsOfferedInTheAssignmentPhaseAndNotInTheArcheryPhaseBeforeNorASkirmishAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harry = scn.GetShadowCard("harry");
		var lemenya = scn.GetShadowCard("lemenya");
		var farin = scn.GetFreepsCard("farin");
		// Farin is the one companion the ability could take, and at strength 5 only just.
		scn.MoveCompanionsToTable(farin);

		scn.StartGame();
		scn.MoveMinionsToTable(harry, lemenya);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(5, scn.GetStrength(farin));
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(harry));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(harry));

		// Harry is left out of the assignments, so he is still unassigned when Farin's skirmish is fought.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsAssignToMinions(farin, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(farin);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(harry));
		assertInZone(Zone.SHADOW_CHARACTERS, harry);
		assertFalse(scn.ShadowActionAvailable(harry));
	}
}
