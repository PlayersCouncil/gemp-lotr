package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_091_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "52_91");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; text only when a weapon is played on him
					put("deadones", "6_100");   // Dead Ones: [sauron] Wraith minion; a play requirement, so cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; text only when played
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition; adds to archery only with a [sauron] Orc

					// Cards that only sit in a hand.
					put("armor", "1_8");        // Dwarven Armor
					put("guard", "1_7");        // Dwarf Guard
					put("gimli", "1_13");       // Gimli, Son of Gloin
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring: "Maneuver: Wear The One Ring until the regroup phase. Skirmish: Add a burden to
				// make the Ring-bearer strength +3."
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void SouthernSpiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Southern Spies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the Ring-bearer puts on The One Ring, spot a [sauron] minion to add a burden for each card
		 * in the Free Peoples player's hand. The Free Peoples player may discard their hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spies");

		assertEquals("Southern Spies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RingBearerPuttingOnTheRingWithASauronMinionAddsABurdenForEachCardInTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var chieftain = scn.GetShadowCard("chieftain");
		// The Free Peoples player holds 3 cards; the Shadow player holds 4 besides Southern Spies.
		scn.MoveCardsToHand(scn.GetFreepsCard("armor"), scn.GetFreepsCard("guard"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(spies, scn.GetShadowCard("armor"), scn.GetShadowCard("guard"),
				scn.GetShadowCard("gimli"), scn.GetShadowCard("bowmen"));

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertFalse(scn.RBWearingOneRing());

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));

		scn.ShadowChooseOptionalTrigger(spies);
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Discard your hand"));

		scn.FreepsChooseNo();
		assertInDiscard(spies);
		// One burden for each of the 3 cards in the Free Peoples hand, not for the Shadow player's 4.
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneCardInTheFreePeoplesHandASauronWraithIsSpottedForOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var deadones = scn.GetShadowCard("deadones");
		scn.MoveCardsToHand(scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(spies, scn.GetShadowCard("armor"), scn.GetShadowCard("guard"));

		scn.StartGame();
		scn.MoveMinionsToTable(deadones);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));

		scn.ShadowChooseOptionalTrigger(spies);
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithOnlyAMoriaMinionAndASauronConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var runner = scn.GetShadowCard("runner");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToHand(scn.GetFreepsCard("armor"), scn.GetFreepsCard("guard"));
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(bowmen);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spies));
		assertInHand(spies);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhenTheRingBearerAddsABurdenWithoutPuttingOnTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(scn.GetFreepsCard("armor"), scn.GetFreepsCard("guard"));
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, chieftain);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Great Ring's skirmish ability adds a burden; the Ring stays off.
		scn.FreepsUseCardAction(ring);
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spies));
		assertInHand(spies);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTheirHandToPreventTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var chieftain = scn.GetShadowCard("chieftain");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(armor, guard, gimli);
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsUseCardAction(ring);
		scn.ShadowChooseOptionalTrigger(spies);
		assertTrue(scn.FreepsDecisionAvailable("Discard your hand"));

		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInDiscard(armor, guard, gimli, spies);
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnEmptyFreePeoplesHandNothingIsAddedAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var spies = scn.GetShadowCard("spies");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(ring);
		// An event is offered whatever its effect will do.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));

		scn.ShadowChooseOptionalTrigger(spies);
		assertFalse(scn.FreepsDecisionAvailable("Discard your hand"));
		assertInDiscard(spies);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
