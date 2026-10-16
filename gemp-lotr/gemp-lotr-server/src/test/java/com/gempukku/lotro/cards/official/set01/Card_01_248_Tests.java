package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_248_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forces", "1_248");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					// Strength Born of Fear: [sauron] support-area condition, a [sauron] card that is no minion;
					// its skirmish ability costs (3) and is not used
					put("fear", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForcesofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Forces of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot X [sauron] minions to add (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forces");

		assertEquals("Forces of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeSauronMinionsAddsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(forces);
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		// Two [sauron] Orcs and a [sauron] Uruk-hai; beside them a [moria] Orc and a [sauron] condition.
		scn.MoveMinionsToTable(chieftain, soldier, sentry, troop);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(forces));
		scn.ShadowPlayCard(forces);

		// X is the Shadow player's to name, from none to the three [sauron] minions; the [moria] Orc and the
		// condition are not counted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		assertEquals(pool + 3, scn.GetTwilight());
		// Spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertInZone(Zone.DISCARD, forces);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTheOneSauronMinionAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(forces));
		scn.ShadowPlayCard(forces);

		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, forces);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(forces);
		assertInZone(Zone.HAND, forces);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, soldier);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(forces));
	}
}
