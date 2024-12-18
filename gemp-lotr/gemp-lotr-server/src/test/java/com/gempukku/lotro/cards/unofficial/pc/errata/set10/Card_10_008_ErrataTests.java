package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_008_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("cirdan", "60_8");
					put("event1", "1_37");
					put("event2", "1_37");
					put("event3", "1_37");
					put("event4", "1_37");
					put("event5", "1_37");
					put("event6", "1_37");
					put("event7", "1_37");

					put("nazgul", "1_233");
				}}
		);
	}

	@Test
	public void CirdanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirdan, The Shipwright
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 2 Elves.
		* 	 <b>Skirmish:</b> Exert Cirdan to make a minion he is skirmishing strength -1 for each [elven] event in your discard pile. If that minion is overwhelmed, remove half of the [elven] events in your discard pile from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cirdan");

		assertEquals("Cirdan", card.getBlueprint().getTitle());
		assertEquals("The Shipwright", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CirdanReducesMinionStrengthAndRemovesEventsIfOverwhelming() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl cirdan = scn.GetFreepsCard("cirdan");
		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");

		scn.ShadowMoveCharToTable(cirdan);
		scn.FreepsMoveCardToDiscard("event1", "event2", "event3", "event4", "event5", "event6", "event7");

		scn.ShadowMoveCharToTable(nazgul);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(cirdan, nazgul);

		//this nazgul should be strength 10, -5 = 5, enough to trigger Cirdan's secondary effect after the skirmish ends
		scn.FreepsResolveSkirmish(cirdan);
		assertTrue(scn.FreepsActionAvailable("Use Cirdan"));
		scn.FreepsUseCardAction(cirdan);

		assertEquals(1, scn.GetWoundsOn(cirdan));
		assertEquals(3, scn.GetStrength(nazgul));
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveRuleFirst(); //Skirmish result first
		assertEquals(Zone.DISCARD, nazgul.getZone());
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertEquals(3, scn.FreepsGetChoiceMin());
		scn.FreepsChoose(scn.FreepsGetCardChoices().get(0), scn.FreepsGetCardChoices().get(1), scn.FreepsGetCardChoices().get(2));

		assertEquals(4, scn.GetFreepsDiscardCount());
	}
}
