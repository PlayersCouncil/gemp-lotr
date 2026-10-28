package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "12_118");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8
					put("boromir", "1_97");      // Boromir, Son of Denethor: strength 7
					put("blade", "1_95");        // Blade of Gondor: Boromir's, strength +2
					put("gimli", "1_13");        // Gimli, Son of Glóin: strength 6
					put("axe", "1_9");           // Dwarven Axe: a Dwarf's, strength +2
					put("treebeard", "4_103");   // Treebeard, Earthborn: an ally of strength 12
					put("visitor", "7_126");     // Unexpected Visitor: each minion gains "Assignment: Assign this minion to bearer"
					put("runner", "1_178");      // Goblin Runner: strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Mouth of Sauron, Lieutenant of Barad-dûr
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Assignment:</b> Assign The Mouth of Sauron to the companion who has the highest strength. (If two or more are tied for highest, choose one.)
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Barad-dûr", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignsHimselfToTheCompanionWhoseStrengthIsNowHighestAndIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var blade = scn.GetFreepsCard("blade");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, blade);
		scn.MoveAlliesToTable(treebeard);

		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Aragorn has the highest printed strength, Boromir the highest strength now (7 + 2), and the ally Treebeard
		// more than either, but he is no companion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(4, scn.GetStrength(frodo));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		// One companion has the highest strength: no choice is asked.
		assertTrue(scn.IsCharAssignedAgainst(boromir, mouth));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));

		// Once he is assigned, the ability is not offered again.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mouth));
	}

	@Test
	public void WithTwoCompanionsTiedForHighestTheShadowPlayerChoosesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.AttachCardsTo(gimli, axe);

		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Aragorn 8 printed, Gimli 6 + 2: tied; Boromir 7 just below.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPass();

		scn.ShadowUseCardAction(mouth);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, frodo));
		scn.ShadowChooseCard(gimli);

		assertTrue(scn.IsCharAssignedAgainst(gimli, mouth));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mouth));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mouth));
	}

	@Test
	public void WithTheCompanionOfHighestStrengthAlreadyAssignedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, visitor);

		var mouth = scn.GetShadowCard("mouth");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPass();
		// The Goblin Runner takes Aragorn by Unexpected Visitor's ability.
		scn.ShadowUseCardAction(runner, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		scn.FreepsPass();

		// Aragorn is still the companion with the highest strength, and cannot be assigned again; Boromir is not
		// the companion the text names.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mouth));
		assertFalse(scn.IsCharAssigned(boromir));
	}

	@Test
	public void NotOfferedToTheMouthAlreadyAssignedWhileTheCompanionOfHighestStrengthIsFree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, visitor);

		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPass();
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.ShadowActionAvailable(mouth));
		// The Mouth takes Boromir by Unexpected Visitor's ability, and so is assigned.
		scn.ShadowUseCardAction(mouth, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(boromir, mouth));
		scn.FreepsPass();

		// Aragorn has the highest strength and is free, but the Mouth is no longer unassigned.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mouth));
		assertFalse(scn.IsCharAssigned(aragorn));
	}
}
