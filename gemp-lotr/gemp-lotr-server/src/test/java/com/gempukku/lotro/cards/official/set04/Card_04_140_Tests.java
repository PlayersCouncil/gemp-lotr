package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "4_140");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Uruk Pursuer: an [isengard] Uruk-hai, vitality 3, fierce; its texts are about unbound Hobbits
					// killed or discarded.
					put("pursuer", "4_190");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text.
					put("guard", "10_81");
					// Isengard Forger: an [isengard] Orc, vitality 2; a Shadow ability never used.
					put("forger", "3_56");
					// The cards laid in the dead piles.  Boromir, Son of Denethor and Gimli, Son of Glóin are
					// companions, Bounder a Hobbit ally; none of their texts acts from a dead pile.
					put("boromir", "1_97");
					put("gimli", "1_13");
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondAllHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Beyond All Hope
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert 2 Uruk-hai to add a burden for each card in the dead pile.
		*/

		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");

		assertEquals("Beyond All Hope", hope.getBlueprint().getTitle());
		assertNull(hope.getBlueprint().getSubtitle());
		assertFalse(hope.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hope.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, hope.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hope.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hope, Timeword.MANEUVER));
		assertEquals(5, hope.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoUrukHaiToAddABurdenForEachCardInTheFreePeoplesDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var savage = scn.GetShadowCard("savage");
		var pursuer = scn.GetShadowCard("pursuer");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		// The Shadow player's own copy, in his own dead pile.
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCardsToHand(hope);
		scn.MoveCardsToDeadPile(boromir, bounder, shadowGimli);

		scn.StartGame();
		assertInZone(Zone.HAND, hope);
		assertInZone(Zone.DEAD, boromir, bounder, shadowGimli);
		// A companion and an ally in the Free Peoples dead pile.
		assertEquals(2, scn.GetFreepsDeadCount());
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, pursuer, guard, forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(hope));
		scn.ShadowPlayCard(hope);

		// Three Uruk-hai, of two cultures; not an [isengard] Orc.  Two exactly.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, pursuer, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		scn.ShadowChooseCards(savage, guard);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(forger));
		// Two cards in the Free Peoples dead pile; the Shadow player's Gimli is not counted.
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.DISCARD, hope);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeCardsInTheDeadPileItAddsThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(hope);
		scn.MoveCardsToDeadPile(boromir, gimli, bounder);

		scn.StartGame();
		assertInZone(Zone.HAND, hope);
		assertEquals(3, scn.GetFreepsDeadCount());
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, guard);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(hope);

		// Only two Uruk-hai: both exert, unasked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneUrukHaiAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(hope);
		scn.MoveCardsToDeadPile(boromir);

		scn.StartGame();
		assertInZone(Zone.HAND, hope);
		assertEquals(1, scn.GetFreepsDeadCount());
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, guard, forger);
		// Vitality 3 with 2 wounds: the Savage cannot exert.  The Guard can, and so can the Orc beside them.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.GetTwilight() >= 5);
		assertInZone(Zone.HAND, hope);
		assertFalse(scn.ShadowPlayAvailable(hope));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
