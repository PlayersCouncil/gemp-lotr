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
					put("dervorin", "7_88");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] Man companion; text only on being overwhelmed
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("horseman", "7_226");   // Enraged Horseman: a [rohan] Man companion; text only in a skirmish
					put("axe1", "1_3");         // Axe Strike: a skirmish event that needs a Dwarf; cards to hold
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("axe5", "1_3");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 only
					put("poison", "8_28");      // Spider Poison: a Shadow possession in its owner's support area; a skirmish ability used only on purpose
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

		var card = scn.GetFreepsCard("dervorin");

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

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDecliningAtRegroupLeavesTheMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop, savage);

		// The start of the phase before: the same board, and nothing asked.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeCardsInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveCardsInHandItIsNotOfferedThoughTheShadowPlayerHoldsFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(dervorin, boromir);

		scn.StartGame();

		scn.SetFreepsHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"),
				scn.GetFreepsCard("axe4"), scn.GetFreepsCard("axe5"));
		scn.SetShadowHand(scn.GetShadowCard("axe1"), scn.GetShadowCard("axe2"), scn.GetShadowCard("axe3"),
				scn.GetShadowCard("axe4"));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAGondorHobbitAndARohanManBesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var pippin = scn.GetFreepsCard("pippin");
		var horseman = scn.GetFreepsCard("horseman");
		scn.MoveCompanionsToTable(dervorin, pippin, horseman);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// Dervorin is the only [gondor] Man, and "another" leaves him out.
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheGondorManAllyIsSpottedAndMinionsOfAnyCultureAndRaceMayBeDiscardedButNoOtherShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(dervorin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var poison = scn.GetShadowCard("poison");
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, dervorin, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfHisNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		var axes = new PhysicalCardImpl[]{scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4")};
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(axes);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(troop);

		// Player 2's turn, then his own turn 3, with the hand made up to 4 again and another minion.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SetFreepsHand(axes);
		assertEquals(4, scn.GetFreepsHandCount());
		scn.MoveMinionsToTable(troop2);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(troop2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
