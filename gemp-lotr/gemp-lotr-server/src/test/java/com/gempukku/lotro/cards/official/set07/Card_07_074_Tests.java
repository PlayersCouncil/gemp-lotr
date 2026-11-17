package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_074_Tests
{

	// Each test plays its Shadow phase at site 4, Balin's Tomb 1_343, where a Gollum (site 3) is not roaming.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("polite", "7_74");
					put("vilegollum", "7_59");      // Gollum, Vile Creature: [gollum] minion, cost 2, site 3; a skirmish ability
					put("nastygollum", "5_24");     // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability
					// Gollum, Half a Wraith Himself: the one Gollum who is not [gollum]: a [wraith] minion, cost 2, site 3.
					// His trigger answers a Nazgul being played and his ability is an assignment action.
					put("wraithgollum", "103_66");
					// Gollum, Old Villain: [gollum] minion, cost 2; a skirmish ability.  The Free Peoples player's own
					// Gollum, of a version no other test card is: an offer from a discard pile is read by version.
					put("oldgollum", "6_40");
					put("shelob", "8_25");          // Shelob, Eater of Light: [gollum] minion that is not Gollum, cost 6, site 8

					// Three companions beside Frodo: room for four threats.
					put("companion1", "1_7");       // Dwarf Guard
					put("companion2", "1_11");      // Farin, Dwarven Emissary
					put("companion3", "1_12");      // Gimli, Dwarf of Erebor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SoPoliteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: So Polite
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play Gollum from your discard pile to add (1) for each threat you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("polite");

		assertEquals("So Polite", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAnyGollumFromHisDiscardPileAndAddsOneForEachOfThreeThreatsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polite = scn.GetShadowCard("polite");
		var vileGollum = scn.GetShadowCard("vilegollum");
		var nastyGollum = scn.GetShadowCard("nastygollum");
		var wraithGollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var freepsGollum = scn.GetFreepsCard("oldgollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(polite);
		scn.MoveCardsToDiscard(vileGollum, nastyGollum, wraithGollum, shelob, freepsGollum);
		scn.AddThreats(3);
		assertInHand(polite);
		assertInDiscard(vileGollum, nastyGollum, wraithGollum, shelob, freepsGollum);
		assertEquals(3, scn.GetThreats());
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowPlayAvailable(polite));
		scn.ShadowPlayCard(polite);

		// "Gollum" is the title and nothing more: the [wraith] Gollum is one of them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vileGollum, nastyGollum, wraithGollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, freepsGollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wraithGollum);

		// How many threats to spot is his to say, 0 to 3, filled in with all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("3");

		assertInZone(Zone.SHADOW_CHARACTERS, wraithGollum);
		assertInDiscard(vileGollum, nastyGollum, shelob, polite);
		// 10 - 2 for Gollum + 3 for the threats
		assertEquals(11, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneThreatHeMaySpotNoneAndAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polite = scn.GetShadowCard("polite");
		var nastyGollum = scn.GetShadowCard("nastygollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(polite);
		scn.MoveCardsToDiscard(nastyGollum);
		scn.AddThreats(1);
		assertInHand(polite);
		assertInDiscard(nastyGollum);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(polite);

		// The one Gollum in his pile is played unasked; the number is asked, 0 to 1, filled in with 1.
		assertInZone(Zone.SHADOW_CHARACTERS, nastyGollum);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(1), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("0");

		// 10 - 2 for Gollum + 0
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(polite);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithGollumInHandAndNoneInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polite = scn.GetShadowCard("polite");
		var gollum = scn.GetShadowCard("vilegollum");
		var shelob = scn.GetShadowCard("shelob");
		var freepsGollum = scn.GetFreepsCard("oldgollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(3);
		// His Gollum in hand, Shelob in his discard pile, and a Gollum in the other player's discard pile.
		scn.MoveCardsToHand(polite, gollum);
		scn.MoveCardsToDiscard(shelob, freepsGollum);
		scn.AddThreats(2);
		assertInHand(polite, gollum);
		assertInDiscard(shelob, freepsGollum);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(gollum));
		assertFalse(scn.ShadowPlayAvailable(polite));
	}

	@Test
	public void NotPlayableUntilThePoolCanPayForGollumAndWithNoThreatNoNumberIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polite = scn.GetShadowCard("polite");
		var gollum = scn.GetShadowCard("vilegollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(polite);
		scn.MoveCardsToDiscard(gollum);
		assertInHand(polite);
		assertInDiscard(gollum);
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		// One short of Gollum's 2 (site 3, not roaming at site 4).
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(polite));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(polite));
		scn.ShadowPlayCard(polite);

		// 2 - 2 for Gollum, and no threat to spot: nothing is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(polite);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
