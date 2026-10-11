package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");

					// Gate Picket: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert this minion twice to make
					// the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer and nothing else
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities exert the Ring-bearer or remove a burden to add twilight.
					put("gollum", "9_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"7_2"
		);
	}

	@Test
	public void SuchAWeightToCarryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: The One Ring, Such a Weight to Carry
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Strength: +2
		 * Game Text: <b>Maneuver:</b> Add a burden to wear The One Ring until the regroup phase.
		 * While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a burden instead.
		*/

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Such a Weight to Carry", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void SuchAWeightToCarryManeuverAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, gollum);

		// Frodo, Son of Drogo is strength 3: the Ring's printed +2 is his whether it is worn or not.
		assertEquals(5, scn.GetStrength(frodo));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(frodo));

		// Worn, the ability still has a cost that can be paid, and is still among the maneuver actions.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// The archery phase is the phase next door: the ability is not among its actions.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowAssignToMinions(sam, runner);

		// Sam, 3, loses to the Runner, 5.  He is Ring-bound and no Ring-bearer: his wound is a wound.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());

		// Frodo's skirmish follows one already resolved, and the Ring is still on.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());

		// 3 + 2 against 5, and a tie is the Shadow's; damage +1: each of the two wounds is a burden.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// Off, Gollum's wound is a wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SuchAWeightToCarryConvertsWoundsDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var picket = scn.GetShadowCard("picket");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(picket, marksman, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		// Worn, in the maneuver phase: the Free Peoples player names no culture, so Gate Picket wounds Frodo and
		// Sam.  The Ring-bearer's wound is a burden; Sam, Ring-bound and no Ring-bearer, takes his.
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertEquals(2, scn.GetWoundsOn(picket));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Worn, in the archery phase: the Marksman's wound, placed on Frodo, is a burden.
		scn.SkipToArcheryWounds();
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// Worn, in a skirmish, an exertion is no wound: Ettenmoors, the site the fellowship is at, has Frodo exert
		// to be strength +2, and the Ring has nothing to say.
		assertTrue(scn.RBWearingOneRing());
		var ettenmoors = scn.GetCurrentSite();
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		assertEquals(5, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(frodo));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Soldier's own wound is a burden ...
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and so is the wound for losing, 3 + 2 (Ring) + 2 (Ettenmoors) against 7, a tie, which is the Shadow's.
		scn.PassSkirmishActions();
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void NotWornTheSameWoundsAreWoundsAndNoBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var picket = scn.GetShadowCard("picket");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(picket, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Ring could be put on, and is not.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertFalse(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// 3 + 2 against 5, a tie, and damage +1: two wounds more, of his vitality of 4.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileNoBurdenCanBeAddedToAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(ring));

		// No burden can be added to a hindered Ring-bearer.
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
	}
}
