package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "13_60");
					put("faramir", "4_117");   // Faramir, Son of Denethor: passive text only
					put("boromir", "1_97");    // Boromir, Son of Denethor: activated text only
					put("denethor", "7_86");   // Denethor, Wizened Steward: passive text only
					put("aragorn", "1_89");
					put("troop", "1_177");     // Goblin Patrol Troop, str 13: beats a 7-str companion without overwhelming
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AwayontheWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Away on the Wind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If Faramir is about to take a wound, discard this condition from play and place Boromir or Denethor in the dead pile from play to prevent that and heal Faramir twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Away on the Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponsePreventsWoundOnFaramirByKillingBoromirOrDenethor() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1078: the kill filter was written as name(Boromir) AND name(Denethor), so the cost could
		// never be paid and the response was never offered.
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, denethor, aragorn);
		scn.MoveCardsToSupportArea(wind);
		scn.AddWoundsToChar(faramir, 2);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();

		// Faramir (7) loses to the Troop (13) and is about to take a lethal third wound
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, denethor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, faramir));
		scn.FreepsChooseCard(boromir);

		assertEquals(Zone.DISCARD, wind.getZone());
		assertEquals(Zone.DEAD, boromir.getZone());
		assertEquals(Zone.FREE_CHARACTERS, denethor.getZone());
		assertEquals(Zone.FREE_CHARACTERS, faramir.getZone());
		assertEquals(0, scn.GetWoundsOn(faramir));   // wound prevented, then healed twice
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithoutBoromirOrDenethorInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToSupportArea(wind);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(Zone.SUPPORT, wind.getZone());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenSomeoneOtherThanFaramirIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.MoveCardsToSupportArea(wind);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.SUPPORT, wind.getZone());
		assertEquals(Zone.FREE_CHARACTERS, boromir.getZone());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
