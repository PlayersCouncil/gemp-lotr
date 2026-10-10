package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCarasGaladhonScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Assassin: [sauron] Orc, site 6, "Assignment: Spot 2 Hobbit companions to make the
					// Free Peoples player assign a Hobbit to skirmish this minion."
					put("assassin1", "1_262");
					put("assassin2", "1_262");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("merry", "1_303");      // Merry, From O'er the Brandywine: his text is about bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text is about [shire] tales
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "3_115");      // Caras Galadhon
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table with Galadriel's Glade 1_351 at site 6, a sanctuary that says nothing about assigning.
	protected VirtualTableScenario GetGaladrielsGladeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin1", "1_262");
					put("assassin2", "1_262");
					put("troop", "1_177");
					put("merry", "1_303");
					put("pippin", "1_306");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CarasGaladhonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Caras Galadhon
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 6
		 * Game Text: <b>Forest</b>. <b>Sanctuary</b>. While the fellowship is at Caras Galadhon, no more than one minion may be assigned to each skirmish.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(6);
		var card = scn.GetFreepsCard("card");

		assertEquals("Caras Galadhon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtCarasGaladhonASecondAssassinTakesAnotherHobbitAndTheShadowPlayerCannotAddAMinionToOneAlreadyTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCarasGaladhonScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var assassin1 = scn.GetShadowCard("assassin1");
		var assassin2 = scn.GetShadowCard("assassin2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(assassin1, assassin2, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals("Caras Galadhon", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(assassin1));
		assertTrue(scn.ShadowActionAvailable(assassin2));

		// The first Assassin: the Free Peoples player has all three Hobbits to choose from.
		scn.ShadowUseCardAction(assassin1);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, pippin));
		scn.FreepsChooseCard(merry);
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertFalse(scn.IsCharAssigned(assassin2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// One minion assigned at this site does not shut the second Assassin's ability off; the first
		// Assassin, now assigned, no longer has his.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin1));
		assertTrue(scn.ShadowActionAvailable(assassin2));

		// The second Assassin goes to one of the two Hobbits left, never to the one already taken.
		scn.ShadowUseCardAction(assassin2);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();

		// What only this site does: the Troop, left over, may not join a skirmish that already has a
		// minion, though it was a card and not a player that put the minion there.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowCanAssignAgainst(merry, troop));
		assertFalse(scn.ShadowCanAssignAgainst(frodo, troop));
		assertTrue(scn.ShadowCanAssignAgainst(pippin, troop));

		scn.ShadowAssignToMinions(pippin, troop);
		assertTrue(scn.IsCharAssignedAgainst(pippin, troop));
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AtAnotherSiteSixTheShadowPlayerMayAddAMinionToAHobbitAnAssassinTook() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielsGladeScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var assassin1 = scn.GetShadowCard("assassin1");
		var assassin2 = scn.GetShadowCard("assassin2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(assassin1, assassin2, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals("Galadriel's Glade", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin1);
		scn.FreepsChooseCard(merry);
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin2);
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();

		// The control for Caras Galadhon: here the leftover Troop may join either Assassin's skirmish.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(merry, troop));
		assertTrue(scn.ShadowCanAssignAgainst(frodo, troop));
		assertTrue(scn.ShadowCanAssignAgainst(pippin, troop));

		scn.ShadowAssignToMinions(merry, troop);
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertTrue(scn.IsCharAssignedAgainst(merry, troop));
		assertFalse(scn.IsCharAssigned(pippin));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
