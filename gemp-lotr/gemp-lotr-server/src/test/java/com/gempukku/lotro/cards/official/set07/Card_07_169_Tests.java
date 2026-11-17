package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("surging", "7_169");
					put("surging2", "7_169");
					// Easterling Infantry: a [raider] Easterling Man, vitality 2; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Easterling Veteran: a [raider] Easterling Man, vitality 3; a skirmish ability that removes burdens,
					// used only on purpose
					put("veteran", "7_147");
					// Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul and no Man; fierce; his other text
					// needs 3 burdens
					put("khamul", "103_61");
					// Southron Explorer: a [raider] Man who is no Easterling; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Easterling Scout: a [men] Man with "Easterling" in the title only; a skirmish ability that needs
					// him skirmishing, used only on purpose
					put("scout", "15_77");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this
					// turn +1.  It lifts the limit to 3, so that a -1 is not hidden by the floor of 1.
					put("secret", "1_71");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf; a skirmish ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: the Elf; a response that discards 3 cards from hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SurgingUpStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Surging Up
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Exert an Easterling and discard this condition to make the move limit -1 for this turn. The Free Peoples player may add 2 burdens to prevent this.<br><b>Skirmish:</b> Discard this condition to heal an Easterling.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("surging");

		assertEquals("Surging Up", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsAnEasterlingAndDiscardsItselfToMakeTheMoveLimitMinus1ForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "arwen");
		scn.MoveCardsToHand(secret);

		var surging = scn.GetShadowCard("surging");
		var surging2 = scn.GetShadowCard("surging2");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(surging, surging2);
		assertInZone(Zone.SUPPORT, surging, surging2);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1

		// Two Easterlings, a Man and a Nazgul, beside a [raider] Man who is no Easterling and a [men] Man who is one by
		// title only; all left unassigned so that they stand in the regroup phase.
		scn.MoveMinionsToTable(infantry, khamul, explorer, scout);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(1, scn.GetMoveCount());
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(surging));
		scn.ShadowUseCardAction(surging);

		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(infantry);
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertInDiscard(surging);
		assertInZone(Zone.SUPPORT, surging2);

		// The Free Peoples player declines to prevent it.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1

		// The fellowship may still move a second time, and does.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the -1 has outlasted the regroup phase it was used in.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other player's turn: his limit is 2, and nothing of the -1 is left.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheFreePeoplesPlayerMayAdd2BurdensToKeepTheMoveLimitAndTheCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surging = scn.GetShadowCard("surging");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToSupportArea(surging);
		assertInZone(Zone.SUPPORT, surging);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(0, scn.GetWoundsOn(infantry));
		scn.ShadowUseCardAction(surging);
		// The lone Easterling exerts unasked.
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertInDiscard(surging);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetBurdens());
		// Not lowered: a -1 from 2 would read 1.
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertInDiscard(surging);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhaseNorWithTheOnlyEasterlingExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surging = scn.GetShadowCard("surging");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(surging);
		assertInZone(Zone.SUPPORT, surging);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer, scout);

		// The maneuver phase, with an Easterling able to exert.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(surging));

		// The Easterling exhausted (vitality 2), with the two who are no Easterlings able to exert beside him.
		scn.AddWoundsToChar(infantry, 1);
		assertEquals(1, scn.GetWoundsOn(infantry));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(surging));
		assertInZone(Zone.SUPPORT, surging);
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfToHealAnEasterlingAndIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var surging = scn.GetShadowCard("surging");
		var surging2 = scn.GetShadowCard("surging2");
		var veteran = scn.GetShadowCard("veteran");
		var khamul = scn.GetShadowCard("khamul");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(surging, surging2);
		assertInZone(Zone.SUPPORT, surging, surging2);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, khamul, explorer, scout);
		scn.AddWoundsToChar(veteran, 2);
		scn.AddWoundsToChar(khamul, 1);
		scn.AddWoundsToChar(explorer, 1);
		scn.AddWoundsToChar(scout, 1);
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(scout));

		// The Shadow player's assignment actions, the phase before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(surging));
		scn.ShadowPass();

		// The Veteran fights Aragorn; Khamul is left unassigned.
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(surging));
		scn.ShadowUseCardAction(surging);
		assertInDiscard(surging);
		assertInZone(Zone.SUPPORT, surging2);

		// An Easterling in the skirmish and one outside it; not the wounded [raider] Man nor the Easterling by title.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(veteran);
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoEasterlingWoundedTheSkirmishAbilityDiscardsItselfForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var surging = scn.GetShadowCard("surging");
		var veteran = scn.GetShadowCard("veteran");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(surging);
		assertInZone(Zone.SUPPORT, surging);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, explorer);
		scn.AddWoundsToChar(explorer, 1);
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(explorer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(surging));
		scn.ShadowUseCardAction(surging);
		assertInDiscard(surging);
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
