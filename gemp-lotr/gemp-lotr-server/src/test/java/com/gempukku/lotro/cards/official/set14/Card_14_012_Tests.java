package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halftroll", "14_12");
					// A second copy, for the discard pile: an [orc] minion who is no Orc, cost 3
					put("halftroll2", "14_12");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					// Scurrying Goblin: [orc] Orc, cost 1, site 4; its play trigger wants an underground site
					put("goblin", "11_137");
					// Denizen of Moria: [orc] Orc, cost 2, site 4; +3 only at an underground site
					put("denizen", "11_116");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, cost 1
					// Cutthroat Orc: [orc] Orc, cost 3, a strength bonus only; laid in the Free Peoples player's discard pile
					put("cutthroat", "11_113");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgul
					// Gondorian Merchant: a [gondor] ally, so a character who is no companion; his ability needs initiative
					put("merchant", "7_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HalftrollofFarHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Half-troll of Far Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Half-troll
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of each assignment phase, if you can spot more companions than minions, you may remove (2) to play an [orc] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halftroll");

		assertEquals("Half-troll of Far Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.HALF_TROLL, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheAssignmentPhaseWithMoreCompanionsThanMinionsItPlaysAnOrcOrcFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cutthroat = scn.GetFreepsCard("cutthroat");
		scn.MoveCompanionsToTable("aragorn", "arwen");
		scn.MoveCardsToFreepsDiscard("cutthroat");

		var halftroll = scn.GetShadowCard("halftroll");
		var halftroll2 = scn.GetShadowCard("halftroll2");
		var brigand = scn.GetShadowCard("brigand");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToShadowDiscard("halftroll2", "goblin", "denizen", "runner");

		scn.StartGame();
		scn.SetTwilight(10);
		// Three companions against two minions
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(halftroll2, goblin, denizen, runner, cutthroat);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();
		// The start of the maneuver phase and of the archery phase offer nothing.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// 10 set + 3 companions + 2 for Ettenmoors
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		scn.ShadowAcceptOptionalTrigger();

		// Each candidate could be paid for: the second Half-troll is no Orc, the Runner no [orc] card, and the
		// Cutthroat Orc lies in the other player's pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll2, runner, cutthroat));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(goblin);

		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		// 15 - 2 removed - 1 to play the Goblin - 2 roaming at site 2
		assertEquals(10, scn.GetTwilight());
		assertInDiscard(halftroll2, denizen, runner, cutthroat);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoCompanionsAndAnAllyAgainstTwoMinionsAreNotMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");
		scn.MoveAlliesToTable("merchant");

		var halftroll = scn.GetShadowCard("halftroll");
		var brigand = scn.GetShadowCard("brigand");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToShadowDiscard("goblin");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Frodo and Aragorn against the Half-troll and the Brigand; the Merchant is no companion.
		// 10 set + 2 companions + 2 for Ettenmoors: enough for the Goblin
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInDiscard(goblin);
	}

	@Test
	public void WithThePoolOneShortOfTheRemovalAndTheOrcTogetherItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "arwen");

		var halftroll = scn.GetShadowCard("halftroll");
		var brigand = scn.GetShadowCard("brigand");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToShadowDiscard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		// 2 removed + 1 for the Goblin + 2 roaming would be 5.
		scn.SetTwilight(4);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInDiscard(goblin);
	}

	@Test
	public void WithThePoolExactlyEnoughTheRemovalAndTheOrcTakeItAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "arwen");

		var halftroll = scn.GetShadowCard("halftroll");
		var brigand = scn.GetShadowCard("brigand");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToShadowDiscard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		scn.ShadowAcceptOptionalTrigger();
		// The Goblin is the one card there to play, and goes without a choice.
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		// 5 - 2 - 1 - 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoOrcOrcInYourDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cutthroat = scn.GetFreepsCard("cutthroat");
		scn.MoveCompanionsToTable("aragorn", "arwen");
		scn.MoveCardsToFreepsDiscard("cutthroat");

		var halftroll = scn.GetShadowCard("halftroll");
		var halftroll2 = scn.GetShadowCard("halftroll2");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToShadowDiscard("halftroll2", "runner");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(halftroll2, runner, cutthroat);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// 10 set + 3 companions + 2 for Ettenmoors: enough for any of them
		assertEquals(15, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInDiscard(halftroll2, runner, cutthroat);
	}

	@Test
	public void DeclinedAtOneAssignmentPhaseItIsOfferedAgainAtTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "arwen");

		var halftroll = scn.GetShadowCard("halftroll");
		var brigand = scn.GetShadowCard("brigand");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToShadowDiscard("goblin");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(halftroll, brigand);
		assertInDiscard(goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(15, scn.GetTwilight());
		assertInDiscard(goblin);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// No skirmish; the fellowship moves again, and the minions are still there.
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// 15 + 3 companions + 0 for Rivendell Valley
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		// 18 - 2 removed - 1 for the Goblin - 2 roaming at site 3
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
