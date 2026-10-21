package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("command", "8_31");
					put("aragorn", "1_365");    // Aragorn, King in Exile: [gondor] Man, Aragorn signet; an optional start-of-turn heal
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn, no signet
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, Aragorn signet, strength 7
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, Aragorn signet, strength 6
					put("faramir", "4_116");    // Faramir, Captain of Gondor: Ring-bound, Aragorn signet
					put("gimli", "1_13");       // Gimli, Son of Glóin: unbound, Gandalf signet
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AtHisCommandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: At His Command
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Aragorn to make an unbound companion with the Aragorn signet strength +1 for each threat you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("command");

		assertEquals("At His Command", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornToMakeAnUnboundCompanionWithTheAragornSignetStrongerByTheThreatsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var command = scn.GetFreepsCard("command");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(command);
		assertInHand(command);
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, boromir, arwen, faramir, gimli);
		scn.MoveMinionsToTable(runner1);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner1);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(arwen));
		assertInHand(command);
		assertTrue(scn.FreepsPlayAvailable(command));
		scn.FreepsPlayCard(command);

		// Aragorn, the one Aragorn, exerts unasked; then the threats spotted: up to the 2 there are, filled in with 2.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// Unbound companions with the Aragorn signet, in the skirmish or not: Aragorn himself, Boromir and Arwen.
		// Faramir has the signet but is Ring-bound; Gimli is unbound with the Gandalf signet; Frodo has neither.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(faramir, gimli, frodo));
		scn.FreepsChooseCard(boromir);

		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, command);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatItIsPlayedForNothingAndTheRohanAragornPaysWithoutBeingAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var command = scn.GetFreepsCard("command");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(rohanaragorn, boromir, arwen);
		scn.MoveCardsToHand(command);
		assertInHand(command);
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner1);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(rohanaragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(command));
		scn.FreepsPlayCard(command);

		// The [rohan] Aragorn exerts; with no threat to spot, no number is asked.  He has no signet, so is no choice.
		assertEquals(1, scn.GetWoundsOn(rohanaragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rohanaragorn));
		scn.FreepsChooseCard(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertInZone(Zone.DISCARD, command);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAragornExhaustedThoughAnotherGondorCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var command = scn.GetFreepsCard("command");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(command);
		assertInHand(command);
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(runner1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner1);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInHand(command);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(command));
	}

	@Test
	public void TheStrengthLastsTheSkirmishItIsPlayedInAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var command = scn.GetFreepsCard("command");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(command);
		assertInHand(command);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{arwen, runner2});
		scn.FreepsResolveSkirmish(boromir);

		// Played in Boromir's skirmish on Arwen, whose skirmish is still to come.
		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(command);
		// One threat: a number from 0 to 1, filled in with 1.
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(1), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");
		scn.FreepsChooseCard(arwen);
		assertEquals(7, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
