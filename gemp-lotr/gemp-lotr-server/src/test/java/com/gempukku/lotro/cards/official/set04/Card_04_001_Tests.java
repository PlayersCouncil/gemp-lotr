package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_001_Tests
{

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
				VirtualTableScenario.ATARRing
		);
	}

	// Isildur, Bearer of Heirlooms bears the Ring: a [gondor] Man, strength 7, vitality 3, resistance 6.  "While Isildur
	// is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or exert 3 companions."
	protected VirtualTableScenario GetIsildurScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sapling of the White Tree: [gondor] artifact in the support area.  "Response: If a [gondor]
					// Man is about to take a wound, discard this artifact to prevent that."
					put("sapling", "9_35");
					put("soldier", "1_271");    // Orc Soldier: strength 7; "Skirmish: Exert this minion to wound a character he is skirmishing."
					put("runner", "1_178");     // Goblin Runner: strength 5; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				"9_33",
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void AnswerToAllRiddlesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: The One Ring, Answer To All Riddles
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Vitality: +2
		 * Game Text: While wearing The One Ring, the Ring-bearer is strength +2, and each time he is about to take
		 * a wound in a skirmish, add a burden instead.
		 * <b>Skirmish:</b> Add a burden to wear The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Answer To All Riddles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void AnswerToAllRiddlesPumpsRBPlus2WhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Frodo, Son of Drogo is strength 3: this Ring adds nothing to his strength while it is not worn.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

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
		assertEquals(5, scn.GetStrength(frodo));
		// Sam is Ring-bound and bears no Ring.
		assertEquals(3, scn.GetStrength(sam));

		// Sam, 3, loses to the Runner, 5.  He is not the Ring-bearer: his wound is a wound and no burden is added.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off and the bonus is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(frodo));
	}

	@Test
	public void AnswerToAllRiddlesConvertsWoundsOnlyDuringSkirmishPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
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
		assertEquals(5, scn.GetStrength(frodo));

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

		// ... and so is the wound for losing, 3 + 2 against 7.
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
	public void AnswerToAllRiddlesSkirmishAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Worn, the ability still has a cost that can be paid, and is still among the actions.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// 3 + 2 against 7: the wound for losing is a burden.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fierce skirmish follows one already resolved, and the Ring is still on.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(5, scn.GetStrength(frodo));

		scn.PassFierceSkirmishActions();
		// 1 more from losing again
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(frodo));
	}

	@Test
	public void NotWornALossIsTwoWoundsAndWornTheSameLossIsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
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

		// 3 + 2 against 5, and a tie is the Shadow's: each of the two wounds is a burden.
		scn.ShadowPass();
		scn.FreepsPass();
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

	// Comprehensive Rules 4.2, "response": "Isildur is wearing The One Ring, Answer to All Riddles. ... The Free
	// Peoples player has Sapling of the White Tree in play. ... Isildur loses a skirmish and is about to take a wound.
	// Because the required action of The One Ring causes a burden to be added "instead" of a wound, the optional
	// action of Sapling of the White Tree cannot be used, as the situation it responds to no longer exists."
	@Test
	public void WornByIsildurTheWoundForALostSkirmishIsABurdenAndTheSaplingCannotAnswerIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildurScenario();

		var isildur = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sapling = scn.GetFreepsCard("sapling");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		// The unbound [gondor] Man it was played beside is gone.
		scn.MoveCardsToSupportArea(sapling);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, soldier);
		scn.ShadowAssignToMinions(isildur, runner);

		// Isildur's own text, with no 3 companions to exert: 2 burdens as his skirmish starts.
		scn.FreepsResolveSkirmish(isildur);
		assertEquals(2, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(isildur));

		// The Ring is not on yet: the Soldier's wound is one the Sapling may answer.  It is left in play.
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertInZone(Zone.SUPPORT, sapling);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(3, scn.GetBurdens());
		// 7 + 2 for the Ring worn
		assertEquals(9, scn.GetStrength(isildur));

		// 9 against 7 + 5: he loses.  The wound is a burden, and the Sapling is not offered for it.
		scn.ShadowPass();
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertInZone(Zone.SUPPORT, sapling);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
