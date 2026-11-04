package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "13_30");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("ent", "6_27");         // Ent Avenger: [gandalf] Ent companion, not unique
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, home 1F
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion
					put("rider", "4_88");       // Behold the White Rider: [gandalf] condition that holds [gandalf] tokens
					put("wisdom", "2_23");      // Gandalf's Wisdom: [gandalf] support-area condition, cost 0, no trigger

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FearandGreatWonderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fear and Great Wonder
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, add a [gandalf] token here.<br><b>Maneuver:</b> Discard this from play or remove a token from here to spot a [gandalf] companion. That companion gains <b>muster</b> until end of turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("Fear and Great Wonder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItTakesAGandalfTokenAndAnotherGandalfConditionPlayedAfterItAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetFreepsCard("fear");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCardsToHand(fear, wisdom);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(fear));
		scn.FreepsPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));

		// Another [gandalf] condition played after it: no second token here.
		scn.FreepsPlayCard(wisdom);
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(0, scn.GetCultureTokensOn(wisdom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverRemovesATokenToGiveASpottedGandalfCompanionMusterThenWithNoTokenLeftItDiscardsItselfUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetFreepsCard("fear");
		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, ent, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(fear, rider);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(rider, 1);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(rider));
		assertInZone(Zone.SUPPORT, fear, rider);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(ent, Keyword.MUSTER));
		assertTrue(scn.FreepsActionAvailable(fear));
		scn.FreepsUseCardAction(fear);

		// Both costs can be paid: the player is asked which.
		assertEquals(List.of("Discard this from play", "Remove a token from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(rider));
		assertInZone(Zone.SUPPORT, fear);

		// The spot: [gandalf] companions of two races, not a [gandalf] ally nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, ent));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(ent, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));

		scn.ShadowPass();

		// No token left: the discard is paid with nothing asked, and Gandalf, who has muster already, is offered again.
		assertTrue(scn.FreepsActionAvailable(fear));
		scn.FreepsUseCardAction(fear);
		assertInDiscard(fear);
		assertEquals(1, scn.GetCultureTokensOn(rider));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, ent));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn));
		scn.FreepsChooseCard(ent);
		assertTrue(scn.HasKeyword(ent, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionToSpotTheTokenIsStillPaidAndNobodyGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetFreepsCard("fear");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(fear);
		scn.AddTokensToCard(fear, 1);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(fear));
		scn.FreepsUseCardAction(fear);
		scn.FreepsChoose("Remove a token from here");

		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertInZone(Zone.SUPPORT, fear);
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(barliman, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetFreepsCard("fear");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(fear);
		scn.AddTokensToCard(fear, 1);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(fear));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fear));
		assertEquals(1, scn.GetCultureTokensOn(fear));
	}

	@Test
	public void MusterGainedInTheManeuverPhaseLastsUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetFreepsCard("fear");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(fear);
		scn.AddTokensToCard(fear, 1);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(fear);
		scn.FreepsChoose("Remove a token from here");
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		// A card in hand, so that muster can be used.
		scn.MoveCardsToHand(wisdom);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// The regroup phase: muster is still his, and is offered.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		scn.FreepsDeclineOptionalTrigger();

		// A second move: still his in the Shadow phase at site 3.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));

		// Still his in the regroup phase at site 3, the last phase of the turn (the skip rests there on the muster offer
		// it declines); gone once the turn is over, in the other player's fellowship phase.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
	}
}
