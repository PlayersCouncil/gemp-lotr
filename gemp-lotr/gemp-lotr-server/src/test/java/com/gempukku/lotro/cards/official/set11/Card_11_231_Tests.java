package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_231_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  The test puts
	// the Shadow player's Caras Galadhon on the path as site 6 (MoveCardToAdventurePath); where it does not, site 6
	// is Galadriel's Glade 1_351, a sanctuary that says nothing about assigning.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Assassin: [sauron] Orc, "Assignment: Spot 2 Hobbit companions to make the Free Peoples
					// player assign a Hobbit to skirmish this minion."
					put("assassin1", "1_262");
					put("assassin2", "1_262");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("merry", "1_303");      // Merry, From O'er the Brandywine: his text is about bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text is about [shire] tales
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Never reached in these tests.
					remove("site9");
					put("caras", "11_231");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CarasGaladhonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Caras Galadhon
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Forest</b>. No more than 1 minion can be assigned to each skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("caras");

		assertEquals("Caras Galadhon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void AtCarasGaladhonASecondAssassinTakesAnotherHobbitAndTheShadowPlayerCannotAddAMinionToOneAlreadyTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var assassin1 = scn.GetShadowCard("assassin1");
		var assassin2 = scn.GetShadowCard("assassin2");
		var troop = scn.GetShadowCard("troop");
		var caras = scn.GetShadowSite("caras");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(caras);
		scn.MoveMinionsToTable(assassin1, assassin2, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(caras, scn.GetCurrentSite());
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

		// One minion assigned at this site does not shut the second Assassin's ability off.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin1));
		assertTrue(scn.ShadowActionAvailable(assassin2));

		// The second Assassin goes to one of the two Hobbits left.
		scn.ShadowUseCardAction(assassin2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();

		// The [moria] Troop, left over, may not join either skirmish that already has a [sauron] minion, though a
		// card and not a player put the minion there; the skirmish with none is open to it.
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
		var scn = GetScenario();

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

		// The control: here the leftover Troop may join either Assassin's skirmish.
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

	@Test
	public void OnceTheFellowshipHasMovedOnFromCarasGaladhonTwoMinionsMayBeAssignedToOneSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var assassin1 = scn.GetShadowCard("assassin1");
		var troop = scn.GetShadowCard("troop");
		var caras = scn.GetShadowSite("caras");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(caras);
		scn.SkipToSite(6);
		assertEquals(caras, scn.GetCurrentSite());
		scn.MoveMinionsToTable(assassin1, troop);

		// The fellowship moves on to site 7, and Caras Galadhon lies behind it.
		scn.SkipToAssignments();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(caras, scn.GetSite(6));
		scn.FreepsAssignToMinions(merry, troop);

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(merry, assassin1));
		scn.ShadowAssignToMinions(merry, assassin1);
		assertTrue(scn.IsCharAssignedAgainst(merry, troop));
		assertTrue(scn.IsCharAssignedAgainst(merry, assassin1));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
