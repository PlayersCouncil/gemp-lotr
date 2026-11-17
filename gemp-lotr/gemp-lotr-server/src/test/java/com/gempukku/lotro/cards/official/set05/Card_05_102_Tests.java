package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_102_Tests
{

	// The Towers-block path: its site 4 is White Rocks.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morannon", "5_102");
					// Morgul Hunter: [sauron] Orc, cost 5, site 6; stronger for each companion and nothing else
					put("hunter1", "1_256");
					put("hunter2", "1_256");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morannon", "5_102");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path again, with the minions of the other tests of site 4[T].
	protected VirtualTableScenario GetTowersMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morannon", "5_102");
					put("hunter", "1_256");       // Morgul Hunter: [sauron] Orc, cost 5, site 6; stronger for each companion and nothing else
					put("trooper", "3_99");       // Orc Trooper: [sauron] Orc, cost 4, site 6; strength +1 for each card in the Shadow hand
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc, cost 3, site 6; damage +1 and nothing else
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc, cost 2, site 4; +4 and fierce only at 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path: its site 4 is Balin's Tomb, no site 4[T].
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morannon", "5_102");
					put("hunter", "1_256");       // Morgul Hunter: [sauron] Orc, cost 5, site 6; stronger for each companion and nothing else
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc; damage +1 and nothing else
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer
					put("armory", "1_173");       // Goblin Armory: [moria] condition, a card of another culture for the hand
					// Strength Born of Fear: [sauron] condition; for the hand and the draw deck
					put("fear1", "1_278");
					put("fear2", "1_278");
					put("fear3", "1_278");
					put("fear4", "1_278");
					put("fear5", "1_278");
					put("fear6", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorannonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Morannon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * The twilight cost of the first [sauron] Orc played at site 4[T] is -3.
		 * Shadow: Spot 2 [sauron] Orcs and discard 4 cards from hand to draw 3 cards. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("morannon");

		assertEquals("Morannon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyTheFirstSauronOrcPlayedAtSite4CostsThreeLessWhateverThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var morannon = scn.GetShadowCard("morannon");
		var hunter1 = scn.GetShadowCard("hunter1");
		var hunter2 = scn.GetShadowCard("hunter2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(morannon);
		scn.MoveCardsToHand(hunter1, hunter2);
		scn.SetTwilight(20);
		// Site 3 of the Towers block: nothing is discounted there.
		assertEquals(5, scn.GetTwilightCost(hunter1));
		scn.FreepsPass();

		assertEquals(4, (int) scn.GetCurrentSite().getSiteNumber());
		int twilight = scn.GetTwilight();
		// 5 - 3 for the first [sauron] Orc played at site 4
		assertEquals(2, scn.GetTwilightCost(hunter1));
		assertEquals(2, scn.GetTwilightCost(hunter2));

		scn.ShadowPlayCard(hunter1);

		assertInZone(Zone.SHADOW_CHARACTERS, hunter1);
		// 2 to play + 2 roaming
		assertEquals(twilight - 4, scn.GetTwilight());
		// The second [sauron] Orc costs what is printed on it,
		assertEquals(5, scn.GetTwilightCost(hunter2));

		// and still does in the later phases of the turn: it is not the first played at site 4.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetTwilightCost(hunter2));
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnOrcOfAnotherCultureAndAnUrukHaiPlayedFirstAtSite4DoNotTakeTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersMinionsScenario();

		var morannon = scn.GetShadowCard("morannon");
		var trooper = scn.GetShadowCard("trooper");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(morannon);
		scn.MoveCardsToHand(trooper, guard, hound);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, morannon);
		assertInZone(Zone.HAND, trooper);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilightCost(guard));
		assertEquals(2, scn.GetTwilightCost(hound));
		// 4 - 3
		assertEquals(1, scn.GetTwilightCost(trooper));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(guard);
		// 3 to play + 2 roaming
		assertEquals(twilight - 5, scn.GetTwilight());
		scn.ShadowPlayCard(hound);
		// 2 to play, and site 4 is no roaming for him
		assertEquals(twilight - 7, scn.GetTwilight());

		// Neither was a [sauron] Orc, so the Trooper is still the first.
		assertEquals(1, scn.GetTwilightCost(trooper));
		scn.ShadowPlayCard(trooper);
		// 1 to play + 2 roaming
		assertEquals(twilight - 10, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstSauronOrcPlayedAtSite5PaysFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersMinionsScenario();

		var morannon = scn.GetShadowCard("morannon");
		var hunter = scn.GetShadowCard("hunter");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(morannon);
		scn.MoveCardsToHand(hunter);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, morannon);
		assertInZone(Zone.HAND, hunter);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilightCost(hunter));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(hunter);
		// 5 to play + 2 roaming
		assertEquals(twilight - 7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstSauronOrcPlayedAtSite4OfTheFellowshipBlockPaysFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var morannon = scn.GetShadowCard("morannon");
		var hunter = scn.GetShadowCard("hunter");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(morannon);
		scn.MoveCardsToHand(hunter);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, morannon);
		assertInZone(Zone.HAND, hunter);
		scn.FreepsPass();

		// Balin's Tomb
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilightCost(hunter));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(hunter);
		// 5 to play + 2 roaming
		assertEquals(twilight - 7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsFourCardsFromHandToDrawThreeAndDiscardsMorannon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var morannon = scn.GetShadowCard("morannon");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var hunter = scn.GetShadowCard("hunter");
		var armory = scn.GetShadowCard("armory");
		var fear1 = scn.GetShadowCard("fear1");
		var fear2 = scn.GetShadowCard("fear2");
		var fear3 = scn.GetShadowCard("fear3");
		var fear4 = scn.GetShadowCard("fear4");
		var fear5 = scn.GetShadowCard("fear5");
		var fear6 = scn.GetShadowCard("fear6");
		scn.MoveCardsToSupportArea(morannon);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, morannon);
		scn.MoveMinionsToTable(chieftain, soldier, guard, hound);
		scn.SetShadowHand(hunter, armory, fear1, fear2, fear3);
		scn.SetShadowDrawDeck(fear4, fear5, fear6);
		scn.FreepsPass();

		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDrawDeck().size());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowActionAvailable(morannon));
		scn.ShadowUseCardAction(morannon);

		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, armory, fear1, fear2, fear3));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(hunter, armory, fear1, fear2);

		assertInZone(Zone.DISCARD, hunter);
		assertInZone(Zone.DISCARD, armory);
		assertInZone(Zone.DISCARD, fear1);
		assertInZone(Zone.DISCARD, fear2);
		assertInZone(Zone.HAND, fear3);
		assertInZone(Zone.HAND, fear4);
		assertInZone(Zone.HAND, fear5);
		assertInZone(Zone.HAND, fear6);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDrawDeck().size());
		assertInZone(Zone.DISCARD, morannon);
		assertEquals(5, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilTwoSauronOrcsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var morannon = scn.GetShadowCard("morannon");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var hunter = scn.GetShadowCard("hunter");
		var armory = scn.GetShadowCard("armory");
		var fear1 = scn.GetShadowCard("fear1");
		var fear2 = scn.GetShadowCard("fear2");
		var fear3 = scn.GetShadowCard("fear3");
		scn.MoveCardsToSupportArea(morannon);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, morannon);
		scn.MoveMinionsToTable(chieftain, guard, hound);
		scn.SetShadowHand(hunter, armory, fear1, fear2, fear3);
		scn.FreepsPass();

		assertEquals(5, scn.GetShadowHandCount());
		// One [sauron] Orc, a [sauron] Uruk-hai and a [wraith] Orc.
		assertFalse(scn.ShadowActionAvailable(morannon));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(morannon));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var morannon = scn.GetShadowCard("morannon");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var armory = scn.GetShadowCard("armory");
		var fear1 = scn.GetShadowCard("fear1");
		var fear2 = scn.GetShadowCard("fear2");
		var fear3 = scn.GetShadowCard("fear3");
		scn.MoveCardsToSupportArea(morannon);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, morannon);
		scn.MoveMinionsToTable(chieftain, soldier);
		scn.SetShadowHand(armory, fear1, fear2);
		scn.FreepsPass();

		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(morannon));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToHand(fear3);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(morannon));
	}
}
