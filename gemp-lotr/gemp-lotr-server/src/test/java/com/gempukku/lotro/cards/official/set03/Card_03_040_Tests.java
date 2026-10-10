package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("citadel", "3_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor], vitality 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven], strength 6, vitality 3
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; Gimli beats it
					// Dunlending Brigand: strength 8, vitality 1, no text; it beats Gimli without overwhelming him
					put("brigand", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CitadelofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Citadel of Minas Tirith
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>You may heal a [gondor] companion at the end of each turn during which no companion or ally lost a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Citadel of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheEndOfATurnInWhichNoCompanionOrAllyLostASkirmishAGondorCompanionMayBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToSupportArea(citadel);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		// Gimli, 6 against 5, wins.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The turn ends.
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(citadel));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));

		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AtTheEndOfATurnInWhichACompanionLostASkirmishNoHealIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToSupportArea(citadel);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, brigand);
		// Gimli, 6 against 8, loses: a companion of another culture than the two the Citadel could heal.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(brigand));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		// The turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, citadel);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
