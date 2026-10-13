package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_245_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("measures", "51_245");

					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, strength +2 only while bearing a weapon
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc minion; only ever in a discard pile here
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition; only ever in a discard pile here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, so a [sauron] minion who is not an Orc; Damage +1 only

					// These make the draw deck 10 cards, so that 8 can be discarded from it and 2 left.
					put("chaff1", "1_177");
					put("chaff2", "1_177");
					put("chaff3", "1_177");
					put("chaff4", "1_177");
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
		 * Game Text: Spot a [sauron] Orc and remove a burden to take a [sauron] card into hand from your discard pile.
		 * The Free Peoples player may discard the top 8 cards of their draw deck to prevent this.
		 */

		var scn = GetScenario();

		var measures = scn.GetFreepsCard("measures");

		assertEquals("Desperate Measures", measures.getBlueprint().getTitle());
		assertNull(measures.getBlueprint().getSubtitle());
		assertFalse(measures.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, measures.getBlueprint().getSide());
		assertEquals(Culture.SAURON, measures.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, measures.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(measures, Timeword.MANEUVER));
		assertEquals(2, measures.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutASauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(scn.GetShadowCard("soldier"));

		scn.StartGame();
		scn.AddBurdens(1);
		// A [moria] Orc and a [sauron] minion that is not an Orc.
		scn.MoveMinionsToTable(troop, sentry);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(measures));

		scn.MoveMinionsToTable(swordsman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(measures));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutABurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(scn.GetShadowCard("soldier"));

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(measures));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(measures));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpotsASauronOrcAndRemovesABurdenToTakeASauronCardFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		var soldier = scn.GetShadowCard("soldier");
		var bowmen = scn.GetShadowCard("bowmen");
		var troop = scn.GetShadowCard("troop");
		// The Free Peoples player's own [sauron] card, in his discard pile and not the Shadow player's.
		var theirSentry = scn.GetFreepsCard("sentry");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(soldier, bowmen, troop, theirSentry);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(swordsman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(measures));

		scn.ShadowPlayCard(measures);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, swordsman);

		// A [sauron] minion and a [sauron] condition are offered; a [moria] Orc and the other player's [sauron] card are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, bowmen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, theirSentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(soldier);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInZone(Zone.HAND, soldier);
		assertInZone(Zone.DISCARD, bowmen, troop, theirSentry, measures);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTheTop8CardsOfTheirDrawDeckToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		var soldier = scn.GetShadowCard("soldier");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(soldier, bowmen);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(swordsman);

		// The 8 cards on top of the Free Peoples deck, and the 2 below them.
		var top = new PhysicalCardImpl[] {
				scn.GetFreepsCard("measures"), scn.GetFreepsCard("swordsman"), scn.GetFreepsCard("soldier"),
				scn.GetFreepsCard("bowmen"), scn.GetFreepsCard("troop"), scn.GetFreepsCard("chaff1"),
				scn.GetFreepsCard("chaff2"), scn.GetFreepsCard("chaff3") };
		scn.MoveCardsToTopOfDeck(top);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(10, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(measures);
		scn.ShadowChooseCardBPFromSelection(soldier);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(8, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, top);
		assertInZone(Zone.DECK, scn.GetFreepsCard("sentry"), scn.GetFreepsCard("chaff4"));

		// Nothing is taken, and the burden and the twilight stay spent.
		assertInZone(Zone.DISCARD, soldier, bowmen, measures);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSevenCardsInTheirDrawDeckTheFreePeoplesPlayerCannotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		var soldier = scn.GetShadowCard("soldier");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(soldier, bowmen);
		scn.MoveCardsToFreepsDiscard("chaff1", "chaff2", "chaff3");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(swordsman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(measures);
		scn.ShadowChooseCardBPFromSelection(soldier);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.HAND, soldier);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSauronCardInYourDiscardPileItTakesNothingAndTheFreePeoplesPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measures = scn.GetShadowCard("measures");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(measures);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(swordsman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		// An event's target is no requirement: it may be played for nothing.
		assertTrue(scn.ShadowPlayAvailable(measures));

		scn.ShadowPlayCard(measures);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, troop, measures);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
