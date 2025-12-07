package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enter", "103_9");
					put("gandalf", "1_364");

					// Shadow cards for targeting
					put("runner", "1_178"); // Minion
					put("ithilstone", "9_47"); // Artifact
					put("ships", "8_65"); // Possession
					put("armory", "1_173"); // Condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouCannotEnterHereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: You Cannot Enter Here
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your Wizard to hinder a Shadow card.  Exhaust that Wizard if you chose a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enter");

		assertEquals("You Cannot Enter Here", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void YouCannotEnterHereCannotPlayWithExhaustedWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Exhaust Gandalf (vitality 4, so 3 wounds = exhausted)
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(1, scn.GetVitality(gandalf));

		scn.SkipToPhase(Phase.MANEUVER);

		// Cannot play - exhausted wizard cannot exert
		assertFalse(scn.FreepsPlayAvailable(enter));
	}

	@Test
	public void YouCannotEnterHereHindersNonMinionWithoutExhaustingWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter");
		var runner = scn.GetShadowCard("runner");
		var ithilstone = scn.GetShadowCard("ithilstone");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(ithilstone, ships, armory);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsHindered(armory));

		assertTrue(scn.FreepsPlayAvailable(enter));
		scn.FreepsPlayCard(enter);

		// Should have 4 valid targets (all Shadow cards)
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(runner, ithilstone, ships, armory));

		// Choose non-minion (condition)
		scn.FreepsChooseCard(armory);

		// Armory should be hindered
		assertTrue(scn.IsHindered(armory));

		// Gandalf should only be exerted once, not exhausted
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Event goes to discard
		assertEquals(Zone.DISCARD, enter.getZone());
	}

	@Test
	public void YouCannotEnterHereHindersMinionAndExhaustsWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsHindered(runner));

		scn.FreepsPlayCard(enter);

		// Runner should be hindered as the only option
		assertTrue(scn.IsHindered(runner));

		// Gandalf should be exhausted (vitality 1)
		assertEquals(1, scn.GetVitality(gandalf));
	}
}
