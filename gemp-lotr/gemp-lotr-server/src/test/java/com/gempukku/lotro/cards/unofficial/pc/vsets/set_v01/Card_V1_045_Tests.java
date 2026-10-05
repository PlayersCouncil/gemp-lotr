package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaze", "101_45");

					put("summons", "1_242");        // The Dark Lord's Summons: [sauron] condition, support area; a Shadow-phase ability that is never used
					put("evil", "1_283");           // You Bring Great Evil: [sauron] condition borne by a companion, who cannot heal at a sanctuary
					put("encirclement", "7_268");   // Encirclement: [sauron] condition; "Regroup: Spot a [sauron] minion to draw a card. Discard this condition."
					put("pinned", "1_192");         // Pinned Down: [moria] condition, support area; does nothing without a [moria] archer
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, strength 7
					// Orc Veteran: strength 10; "Skirmish: Discard your [sauron] condition to make a character
					// skirmishing this minion strength -1."
					put("veteran", "3_100");
					put("scimitar", "1_269");       // Orc Scimitar: [sauron] possession borne by a [sauron] Orc
					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, no text
					// Mordor Enraged: [sauron] condition borne by a [sauron] minion; a Shadow-phase ability that is
					// never used
					put("enraged1", "1_254");
					put("enraged2", "1_254");

					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk: the Wizard the two spells need
					put("sleep", "1_84");           // Sleep, Caradhras: "Fellowship: Exert Gandalf to discard every condition."
					put("introspection", "12_29");  // Introspection: spot a [gandalf] Wizard and an opponent must discard one of his conditions
					// Stand Against Darkness: "Maneuver: Exert an Elf to discard a [sauron] minion, a [sauron]
					// condition, or a [sauron] possession."
					put("stand1", "1_63");
					put("stand2", "1_63");
					put("stand3", "1_63");
					// Lorien Elf: strength 4, vitality 2, no text in play.  Each can exert once.
					put("elf1", "1_53");
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("guard", "1_7");            // Dwarf Guard: strength 4, vitality 2, no text in play
					put("wall", "5_33");            // City Wall: [gondor] fortification, strength -3 once it is on a minion
					put("fallback", "5_34");        // Fall Back: "Skirmish: Discard all Shadow cards borne by a minion bearing a [gondor] fortification."
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GazeoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gaze of the Eye
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Response: If your [Sauron] condition is about to be discarded, remove a burden to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaze");

		assertEquals("Gaze of the Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 companion + 2 for the shadow number of site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, gaze);
		assertTrue(scn.ShadowPlayAvailable(gaze));

		scn.ShadowPlayCard(gaze);

		assertInZone(Zone.SUPPORT, gaze);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesABurdenToKeepItselfInPlayWhenItIsAboutToBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var introspection = scn.GetFreepsCard("introspection");
		scn.MoveCardsToSupportArea(gaze, summons);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		scn.AddBurdens(2);

		scn.FreepsPlayCard(introspection);
		// The Shadow player must discard one of his conditions, and picks this one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons));
		scn.ShadowChooseCard(gaze);

		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLetsTheConditionBeDiscardedAndKeepsTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var introspection = scn.GetFreepsCard("introspection");
		scn.MoveCardsToSupportArea(gaze, summons);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		scn.AddBurdens(1);

		scn.FreepsPlayCard(introspection);
		scn.ShadowChooseCard(summons);

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(summons);
		assertInZone(Zone.SUPPORT, gaze);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenEveryConditionIsAboutToBeDiscardedEachSauronConditionCanBeSavedForABurdenAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var evil = scn.GetShadowCard("evil");
		var pinned = scn.GetShadowCard("pinned");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToSupportArea(gaze, summons, pinned);
		scn.AttachCardsTo(frodo, evil);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();
		scn.AddBurdens(3);

		scn.FreepsPlayCard(sleep);

		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons, pinned);
		assertAttachedTo(evil, frodo);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();

		// This condition, another [sauron] condition in the support area, and one borne by a companion;
		// not the [moria] condition that is about to be discarded with them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned));
		scn.ShadowChooseCard(evil);
		assertEquals(2, scn.GetBurdens());

		// One burden saves one condition, and the response may be used again for the next.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(evil, pinned));
		scn.ShadowChooseCard(gaze);
		assertEquals(1, scn.GetBurdens());

		// The last of the three is the only one left to choose.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());

		assertInZone(Zone.SUPPORT, gaze, summons);
		assertAttachedTo(evil, frodo);
		assertInDiscard(pinned);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWhileThereIsABurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToSupportArea(gaze, summons);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();
		scn.AddBurdens(1);

		scn.FreepsPlayCard(sleep);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons));
		scn.ShadowChooseCard(summons);

		// The one burden is spent.  This condition is still about to be discarded, and with no burden to
		// remove nothing more is offered.
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, summons);
		assertInDiscard(gaze);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMoriaConditionASauronPossessionOrASauronMinionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var pinned = scn.GetShadowCard("pinned");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var introspection = scn.GetFreepsCard("introspection");
		var stand1 = scn.GetFreepsCard("stand1");
		var stand2 = scn.GetFreepsCard("stand2");
		var stand3 = scn.GetFreepsCard("stand3");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		scn.MoveCardsToSupportArea(gaze, summons, pinned);
		scn.MoveCompanionsToTable(gandalf, elf1, elf2, elf3);
		scn.MoveCardsToHand(introspection, stand1, stand2, stand3);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, scimitar);

		// A [moria] condition of the Shadow player's.
		scn.FreepsPlayCard(introspection);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons, pinned));
		scn.ShadowChooseCard(pinned);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(pinned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.MANEUVER);

		// A [sauron] possession.
		scn.FreepsPlayCard(stand1);
		scn.FreepsChooseCard(elf1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, soldier, gaze, summons));
		scn.FreepsChooseCard(scimitar);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(scimitar);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A [sauron] minion.
		scn.FreepsPlayCard(stand2);
		scn.FreepsChooseCard(elf2);
		scn.FreepsChooseCard(soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(soldier);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The same event on a [sauron] condition, with the burden still there: now it is offered.
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(stand3);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaze, summons));
		scn.FreepsChooseCard(summons);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OfSauronCardsAboutToBeDiscardedTogetherOnlyAConditionCanBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var enraged1 = scn.GetShadowCard("enraged1");
		var enraged2 = scn.GetShadowCard("enraged2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var fallback = scn.GetFreepsCard("fallback");
		scn.MoveCardsToSupportArea(gaze);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fallback);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(soldier);
		// The Orc bears a [sauron] possession and two [sauron] conditions, and the fortification that lets
		// Fall Back discard all three at once.
		scn.AttachCardsTo(soldier, scimitar, enraged1, enraged2, wall);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(fallback);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(enraged1, enraged2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar));
		scn.ShadowChooseCard(enraged1);

		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(enraged1, soldier);
		assertInDiscard(enraged2, scimitar);
		assertAttachedTo(wall, soldier);
		assertInZone(Zone.SUPPORT, gaze);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADiscardByTheShadowPlayersOwnCardIsPreventedTheSameWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var encirclement = scn.GetShadowCard("encirclement");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(gaze, encirclement);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(encirclement));

		// Encirclement spots the Orc, draws a card, and then discards itself.
		scn.ShadowUseCardAction(encirclement);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, encirclement);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, encirclement);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADiscardThatIsACostIsPreventedTooAndTheAbilityItWasToPayForThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var summons = scn.GetShadowCard("summons");
		var veteran = scn.GetShadowCard("veteran");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(gaze, summons);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gaze, summons));
		scn.ShadowChooseCard(summons);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowAcceptOptionalTrigger();

		// The condition stays, so the Veteran's cost was not paid and his ability has no effect.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze, summons);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The same ability with the response declined: the condition is discarded and Aragorn is strength -1.
		scn.FreepsPass();
		scn.ShadowUseCardAction(veteran);
		scn.ShadowChooseCard(summons);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gaze));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(summons);
		assertInZone(Zone.SUPPORT, gaze);
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForAConditionDiscardedBecauseItsBearerWasKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var evil = scn.GetShadowCard("evil");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(gaze);
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, evil);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertAttachedTo(evil, guard);
		assertEquals(1, scn.GetBurdens());

		// The Troop's 13 is more than double the Guard's 4: he is overwhelmed and killed, and what he bore
		// is discarded with him.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, guard);
		assertInDiscard(evil);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, gaze);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
