package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_245_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("measures", "1_245");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					// Strength Born of Fear: [sauron] support-area condition, a [sauron] card that is no minion;
					// its skirmish ability costs (3) and is not used
					put("fear", "1_278");

					// One more card for a Free Peoples draw deck of seven.
					put("chaff", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesperateMeasuresStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Desperate Measures
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a [sauron] Orc and remove a burden to make the Free Peoples player discard top 5 cards from his draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("measures");

		assertEquals("Desperate Measures", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsASauronOrcAndRemovesABurdenToDiscardTheTopFiveCardsOfTheFreePeoplesDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(measures);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain);

		// Seven known cards in the Free Peoples draw deck: the five on top, and the two below them.
		var top1 = scn.GetFreepsCard("measures");
		var top2 = scn.GetFreepsCard("chieftain");
		var top3 = scn.GetFreepsCard("soldier");
		var top4 = scn.GetFreepsCard("troop");
		var top5 = scn.GetFreepsCard("sentry");
		var sixth = scn.GetFreepsCard("fear");
		var seventh = scn.GetFreepsCard("chaff");
		scn.SetFreepsDrawDeck(top1, top2, top3, top4, top5, sixth, seventh);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetBurdens());
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(measures));
		scn.ShadowPlayCard(measures);

		// One burden, and not every burden.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, top1, top2, top3, top4, top5);
		assertInZone(Zone.DECK, sixth, seventh);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		// The Shadow player's own draw deck is not touched; the Orc spotted is still in play.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.DISCARD, measures);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutASauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		// A [moria] Orc, a [sauron] minion that is no Orc, and a [sauron] card that is no minion.
		scn.MoveMinionsToTable(troop, sentry);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.GetTwilight() >= 2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(measures));

		// A [sauron] Orc of another title than the one in the other tests.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(measures));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutABurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(measures);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.GetTwilight() >= 2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(measures));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(measures));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithThreeCardsInTheFreePeoplesDrawDeckItIsPlayedAndDiscardsAllThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(measures);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain);

		var first = scn.GetFreepsCard("measures");
		var second = scn.GetFreepsCard("troop");
		var third = scn.GetFreepsCard("chaff");
		scn.SetFreepsDrawDeck(first, second, third);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(measures));
		scn.ShadowPlayCard(measures);

		// Five cannot be discarded: the three there are go, and the burden is paid.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, first, second, third);
		assertInZone(Zone.DISCARD, measures);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
