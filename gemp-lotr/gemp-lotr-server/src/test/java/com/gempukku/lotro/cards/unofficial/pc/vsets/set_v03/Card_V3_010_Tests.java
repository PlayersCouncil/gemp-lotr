package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("broken", "103_10");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4; a fellowship ability never used here
					put("staff", "2_22");       // Gandalf's Staff: an artifact Gandalf bears; vitality +1; a skirmish ability never used here
					put("glamdring", "1_75");   // Glamdring: a possession Gandalf bears, and no artifact
					put("coat", "2_105");       // Mithril-coat: a [shire] artifact, an armor; Frodo bears it, or the rig puts it on Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; takes the skirmishes
					put("catapult", "8_32");    // Catapult: a Free Peoples possession in the support area
					put("sapling", "9_35");     // Sapling of the White Tree: a Free Peoples artifact in the support area

					put("runner", "1_178");     // Goblin Runner: [moria] minion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion with no text, strength 13, vitality 3
					put("stone", "9_47");       // Ithil Stone: [sauron] artifact in the support area
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area
					put("armory", "1_173");     // Goblin Armory: [moria] condition in the support area
					put("poison", "8_28");      // Spider Poison: [gollum] possession that a companion bears
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YourStaffisBrokenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Your Staff is Broken
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of each Assignment phase, you may exert Gandalf bearing an artifact to discard a hindered Shadow card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("broken");

		assertEquals("Your Staff is Broken", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForThreeToTheSupportAreaWithNoGandalfInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(broken);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(broken));

		scn.FreepsPlayCard(broken);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, broken);
		assertNotAttachedTo(broken, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsGandalfToDiscardAHinderedShadowCardOfAnyKindAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var catapult = scn.GetFreepsCard("catapult");
		var sapling = scn.GetFreepsCard("sapling");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var stone = scn.GetShadowCard("stone");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken, catapult, sapling);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.MoveCardsToSupportArea(stone, ships, armory);
		scn.AttachCardsTo(aragorn, poison);
		// Hindered: a minion, an artifact in the support area and a possession on a companion, of three
		// cultures; and two Free Peoples cards. Not hindered: a minion, a possession and a condition.
		scn.HinderCard(runner, stone, poison, catapult, sapling);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(broken));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, stone, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, ships, armory, catapult, sapling));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(poison);

		assertInZone(Zone.DISCARD, poison);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.IsHindered(runner));
		assertTrue(scn.IsHindered(stone));
		assertInZone(Zone.SUPPORT, catapult, sapling);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OfferedAtTheStartOfAssignmentOnlyAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.HinderCard(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(broken));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.IsHindered(runner));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The skirmish phase that follows starts with no offer.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var stone = scn.GetShadowCard("stone");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.MoveCardsToSupportArea(stone, armory);
		scn.HinderCard(runner, stone, armory);
		// A fierce minion that survives its first skirmish brings a second assignment phase.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(broken));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(runner);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, runner);

		// Aragorn, strength 8, loses to the Troop, strength 13, and takes the wound.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.PassSkirmishActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(broken));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stone, armory));
		scn.FreepsChooseCard(armory);

		assertInZone(Zone.DISCARD, armory);
		assertInZone(Zone.SUPPORT, stone);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		scn.StartGame();
		// Vitality 4, and 1 more for the Staff: 4 wounds leave him exhausted.
		scn.AddWoundsToChar(gandalf, 4);
		scn.MoveMinionsToTable(runner, troop);
		scn.HinderCard(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetVitality(gandalf));
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.IsHindered(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenGandalfBearsOnlyAPossessionAndTheArtifactIsOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var coat = scn.GetFreepsCard("coat");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToSupportArea(broken);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.HinderCard(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(coat, frodo);
		assertTrue(scn.IsHindered(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyArtifactGandalfBearsIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		// A hindered card has no card type, so the Staff is no artifact while it is hindered.
		scn.HinderCard(runner, staff);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertAttachedTo(staff, gandalf);
		assertTrue(scn.IsHindered(staff));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void StillOfferedWithNoHinderedShadowCardAndThenExertsGandalfForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");
		var gandalf = scn.GetFreepsCard("gandalf");
		var coat = scn.GetFreepsCard("coat");
		var catapult = scn.GetFreepsCard("catapult");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(gandalf);
		// Any artifact will do: the one he bears here is no staff and not [gandalf]. Only Frodo may be
		// given the Mithril-coat by legal play, so the rig puts it on Gandalf.
		scn.AttachCardsTo(gandalf, coat);
		scn.MoveCardsToSupportArea(broken, catapult);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(armory);
		// The only hindered card is a Free Peoples one; the Shadow cards are not hindered.
		scn.HinderCard(catapult);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertAttachedTo(coat, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// The cost can be paid, so the trigger is offered though it will discard nothing.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(broken));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, armory, catapult);
		assertTrue(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
