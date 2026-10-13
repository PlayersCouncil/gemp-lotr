package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_95");
					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc
					put("soldier2", "1_271");
					put("shadow", "1_252");       // The Irresistible Shadow: [sauron] condition, cost 0; to play, exert a [sauron] Orc
					put("armory", "1_173");       // Goblin Armory: [moria] condition, cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadMarshesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Dead Marshes
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>While the fellowship is at site 4[T], Ring-bound companions skirmishing [sauron] Orcs are strength -1.<br><b>Maneuver:</b> Spot 2 [sauron] Orcs to play up to 2 [sauron] conditions from your discard pile. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dead Marshes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DeadMarshesTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void ManeuverActionIsNotOfferedUnlessASauronConditionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var shadow = scn.GetShadowCard("shadow");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(card);
		//A condition that could be played, but is not a [sauron] one
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		//Playing from the discard pile is treated as a cost: with nothing to play, the ability is withheld
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(card));

		scn.MoveCardsToDiscard(shadow);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(card));
	}
}
