package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_88");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dervorin", "7_88");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the other [gondor] Man
					put("axe1", "1_3");         // Axe Strike: a skirmish event that needs a Dwarf; four cards to hold
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DervorinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Dervorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: At the start of the regroup phase, if you have only 4 cards in hand, you may spot another [gondor] Man to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dervorin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfRegroupWithOnlyFourCardsInHandAndAnotherGondorManAMinionMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		// Nobody is assigned to either minion, so both are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered, and a
	// spot is no cost (F-0348).  The old at/ tests of muster (TriggersAtTest) had this offer made with no minion in
	// play.
	@Test
	public void WithNoMinionInPlayTheDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Only 4 cards in hand and another [gondor] Man beside him, as his text asks, and no minion to discard.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
