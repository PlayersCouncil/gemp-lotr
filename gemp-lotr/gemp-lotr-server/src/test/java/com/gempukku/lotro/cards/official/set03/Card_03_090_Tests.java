package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hand", "3_90");

					// The minions.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, vitality 2; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// Four cards of four titles for the Free Peoples hand, none of them played.
					put("guard", "1_7");        // Dwarf Guard
					put("boromir", "1_96");     // Boromir, Lord of Gondor
					put("merry", "1_303");      // Merry, From O'er the Brandywine
					put("arwen", "1_30");       // Arwen, Daughter of Elrond
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HandofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hand of Sauron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a [sauron] minion to make the Free Peoples player discard a card at random from his or her hand.
		*/

		var scn = GetScenario();

		var hand = scn.GetFreepsCard("hand");

		assertEquals("Hand of Sauron", hand.getBlueprint().getTitle());
		assertNull(hand.getBlueprint().getSubtitle());
		assertFalse(hand.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hand.getBlueprint().getSide());
		assertEquals(Culture.SAURON, hand.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hand.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hand, Timeword.MANEUVER));
		assertEquals(0, hand.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionToMakeTheFreePeoplesPlayerDiscardACardAtRandomFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var soldier = scn.GetShadowCard("soldier");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop");
		var shadowGuard = scn.GetShadowCard("guard");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(guard, boromir, merry, arwen);
		scn.MoveCardsToHand(hand, shadowGuard);

		scn.StartGame();
		assertInZone(Zone.HAND, guard, boromir, merry, arwen, hand, shadowGuard);
		scn.MoveMinionsToTable(soldier, lights, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(4, scn.GetFreepsHandCount());
		int discard = scn.GetFreepsDiscardCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(hand));

		scn.ShadowPlayCard(hand);
		// The cost: a [sauron] Orc and a [sauron] Wraith, and not the [moria] Orc.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, lights));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(lights);

		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		// One card of the four goes, and nobody is asked which.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertNotInZone(Zone.DECK, guard, boromir, merry, arwen);
		// Not the Shadow player's hand.
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, shadowGuard);
		assertInDiscard(hand);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(guard);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		// The [moria] Orc beside him is fresh.
		scn.MoveMinionsToTable(soldier, troop);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(soldier));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hand));
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(guard);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hand));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hand));
	}

	@Test
	public void WithTheFreePeoplesHandEmptyItIsStillPlayedAndTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var soldier = scn.GetShadowCard("soldier");
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(hand, shadowGuard);

		scn.StartGame();
		assertInZone(Zone.HAND, hand, shadowGuard);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowPlayAvailable(hand));

		// Orc Soldier is the one [sauron] minion: he exerts unasked.
		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, shadowGuard);
		assertInDiscard(hand);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
