package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_001_Tests
{

	// The Fellowship path: the Shadow phase and the skirmishes of the first turn are at Ettenmoors, "Skirmish: Exert
	// your companion or minion to make that character strength +2."
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");

					// Saruman's Reach: [isengard] Maneuver event, cost 0.  "Exert an Uruk-hai to make the opponent
					// choose to either exert 2 companions or make the Ring-bearer put on The One Ring until the
					// regroup phase."
					put("reach", "1_137");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// Gate Picket: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert this minion twice to make
					// the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer and nothing else
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities exert the Ring-bearer or remove a burden to add twilight.
					put("gollum", "9_28");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"15_1"
		);
	}

	@Test
	public void RingOfDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: The One Ring, The Ring of Doom
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Vitality: +2
		 * Game Text: While wearing The One Ring, the Ring-bearer gains <b>hunter 3</b>, and each time he or she is
		 * about to take a wound in a skirmish, add a burden instead.
		 * <b>Skirmish:</b> Add a burden to wear The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Ring of Doom", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void RingOfDoomGrantsRBHunter3WhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Frodo, Son of Drogo is strength 3 and vitality 4: this Ring adds 2 to his vitality, and no keyword while
		// it is not worn.
		assertEquals(6, scn.GetVitality(frodo));
		assertEquals(3, scn.GetStrength(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sam, Keyword.HUNTER));

		// The maneuver phase is no skirmish: the Ring's ability is not among the actions.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// The text names no skirmish: the Ring is put on in Sam's.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));
		// Hunter is a bonus in the hunter's own skirmish: in Sam's, Frodo is still 3.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(6, scn.GetVitality(frodo));
		// Sam is Ring-bound and bears no Ring.
		assertFalse(scn.HasKeyword(sam, Keyword.HUNTER));
		assertEquals(3, scn.GetStrength(sam));

		// Sam, 3, loses to the Runner, 5.  He is not the Ring-bearer: his wound is a wound and no burden is added.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off and the keyword is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.HasKeyword(frodo, Keyword.HUNTER));
	}

	@Test
	public void RingOfDoomConvertsWoundsOnlyDuringSkirmishPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var picket = scn.GetShadowCard("picket");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, picket, marksman, soldier, gollum);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Saruman's Reach: the Savage exerts, and with one companion there are no two to exert, so the
		// Ring-bearer puts the Ring on in the maneuver phase, for no burden.
		assertFalse(scn.RBWearingOneRing());
		scn.ShadowPlayCard(reach);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));

		// Worn, in the maneuver phase: the Free Peoples player names no culture, so Gate Picket wounds Frodo.
		// The wound is in no skirmish: it is a wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Worn, in the archery phase: the Marksman's wound goes to the only companion, and is a wound.
		scn.SkipToArcheryWounds();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// Worn, in a skirmish: the Soldier's own wound is a burden ...
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and so is the wound for losing, 3 + 3 as a hunter against 7.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// The regroup phase is no skirmish, and the Ring is off: Gollum's wound is a wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RingOfDoomSkirmishAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// Fierce only so that the Soldier fights Frodo a second time.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, soldier, null, Keyword.FIERCE));

		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Hunter 3, skirmishing a minion that is no hunter: 3 + 3.
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(frodo));

		// Worn, the ability still has a cost that can be paid, and is still among the actions.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// 6 against 7: the wound for losing is a burden.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fierce skirmish follows one already resolved, and the Ring is still on.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(frodo));

		scn.PassFierceSkirmishActions();
		// 1 more from losing again
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertEquals(3, scn.GetStrength(frodo));
	}

	@Test
	public void NotWornALossIsTwoWoundsAndWornALossIsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Fierce only so that the Savage fights Frodo a second time.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		// The Ring could be put on, and is not.  3 against 5, and damage +1: two wounds and no burden.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.PassSkirmishActions();
		assertFalse(scn.RBWearingOneRing());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(frodo));

		// A hunter now, Frodo would beat the Savage, 6 against 5: it exerts at Ettenmoors for +2.
		scn.ShadowUseCardAction(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(savage));

		// 6 against 7: each of the two wounds is a burden.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileNoBurdenCanBeAddedToAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// No burden can be added to a hindered Ring-bearer.
		scn.HinderCard(frodo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// Sam's skirmish, in which the Ring could otherwise be put on.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
	}
}
