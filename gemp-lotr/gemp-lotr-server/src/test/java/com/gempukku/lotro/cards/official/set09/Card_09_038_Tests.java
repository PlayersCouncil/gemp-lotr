package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "9_38");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, cost 2, vitality 3; Knight is his whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4
					// Gondorian Merchant: the only [gondor] ally, a Man of vitality 1; his maneuver ability needs
					// initiative.
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, vitality 4.
					put("pippin", "103_31");
					// Narsil, Blade of the Faithful: a [gondor] artifact, vitality +1, "Bearer must be a [gondor] Man."
					// Its text acts on a wound or on Elendil or Isildur, none of which is here.
					put("narsil", "9_34");
					// Ranger's Cloak: a [gondor] possession, vitality +1, "Bearer must be a [gondor] Man."  Its texts
					// act in a skirmish or on a ranger, neither of which is here.
					put("cloak", "15_68");
					put("guard", "5_83");       // Household Guard: a [rohan] Man, vitality 3; Valiant is his whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeeingStoneofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Seeing Stone of Orthanc
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [gondor] Man with 3 or more vitality (or spot a [gondor] Man and add 2 threats).
		 * <b>Regroup:</b> Add a threat or discard this artifact to remove (2) or to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stone");

		assertEquals("Seeing Stone of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SeeingStoneofOrthancAddsThreatsIfNoGondorManOfVit3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		// A [gondor] companion of 4 vitality who is no Man, and a Man of 3 vitality who is not [gondor].
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToHand(stone, knight);

		scn.StartGame();
		// Three companions: there is room for the 2 threats.
		assertEquals(3, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(stone));

		// With a wound he has 2 vitality left: only the second way is open.
		scn.AddWoundsToChar(knight, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetVitality(knight));
		assertEquals(0, scn.GetThreats());
		// The Knight cost 2.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stone);
		assertNull(stone.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SeeingStoneofOrthancOffersChoiceOf2ThreatsOrSpotIfCanSpotGondorManOf3Vit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stone);

		scn.StartGame();
		// Aragorn's vitality of 4 with a wound: exactly 3 left.
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetVitality(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("3 or more vitality"));
		assertTrue(scn.FreepsChoiceAvailable("add 2 threats"));
		scn.FreepsChooseOption("3 or more vitality");

		assertEquals(0, scn.GetThreats());
		// The Stone costs 0.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stone);
		assertNull(stone.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheSecondWayMayBeChosenThoughAGondorManOf3VitalityIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(stone);

		scn.StartGame();

		assertEquals(3, scn.GetVitality(knight));
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(stone);
		assertTrue(scn.FreepsChoiceAvailable("3 or more vitality"));
		assertTrue(scn.FreepsChoiceAvailable("add 2 threats"));
		scn.FreepsChooseOption("add 2 threats");

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAManOf3VitalityAndNoRoomFor2ThreatsItIsPlayedWithNoQuestionAndNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(stone);

		scn.StartGame();
		// Two companions and a threat already: one more could be added, not two.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(3, scn.GetVitality(knight));
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyGondorManHas2VitalityAndTwoThreatsCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(stone);

		scn.StartGame();
		scn.AddWoundsToChar(knight, 1);
		// Two companions and a threat already: one more could be added, not two.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetVitality(knight));
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stone));

		// With that threat gone the two can be added.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(stone));
	}

	@Test
	public void AGondorAllyOfOneVitalityIsAManToSpotForTheSecondWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		// The Household Guard is a second companion, so that 2 threats can be added.
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(stone);

		scn.StartGame();

		assertEquals(1, scn.GetVitality(merchant));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorAllyOfThreeVitalityIsAManToSpotForTheFirstWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var cloak = scn.GetFreepsCard("cloak");
		// The Ring-bearer is the only companion, so 2 threats cannot be added: only the first way can play the Stone.
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(stone);

		scn.StartGame();

		assertEquals(1, scn.GetThreatLimit());
		// The Merchant's 1 and Narsil's 1.
		assertEquals(2, scn.GetVitality(merchant));
		assertFalse(scn.FreepsPlayAvailable(stone));

		// Ranger's Cloak is the third point: 1 + 1 + 1.
		scn.AttachCardsTo(merchant, cloak);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetVitality(merchant));
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityAddsAThreatToRemove2OrToDrawACardAndDiscardsTheStoneWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// Not a maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stone));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(stone));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// A threat, to remove (2).
		scn.FreepsUseCardAction(stone);
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this artifact"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsChoiceAvailable("Remove (2)"));
		assertTrue(scn.FreepsChoiceAvailable("Draw a card"));
		scn.FreepsChooseOption("Remove (2)");
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, stone);

		// A second threat, to draw a card.
		scn.ShadowPass();
		scn.FreepsUseCardAction(stone);
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		scn.FreepsChooseOption("Draw a card");
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stone);

		// Two threats and two companions: no threat can be added, so the Stone is what pays, with no question.
		scn.ShadowPass();
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.FreepsActionAvailable(stone));
		scn.FreepsUseCardAction(stone);
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertInDiscard(stone);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsChoiceAvailable("Remove (2)"));
		scn.FreepsChooseOption("Remove (2)");
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheStoneMayBeDiscardedThoughAThreatCouldBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(stone);
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		scn.FreepsChooseOption("Discard this artifact");
		assertInDiscard(stone);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseOption("Draw a card");
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithFewerThan2InThePoolACardIsDrawnAndWithAnEmptyDrawDeck2AreRemovedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merchant);
		scn.SkipToPhase(Phase.REGROUP);

		// One token in the pool: (2) cannot be removed in full, so the card is drawn.
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(stone);
		scn.FreepsChooseOption("Add a threat");
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, merchant);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// No card left in the draw deck: none can be drawn, so (2) are removed.
		scn.ShadowPass();
		scn.MoveCardsToDiscard(scn.GetFreepsCard("pippin"), scn.GetFreepsCard("guard"), scn.GetFreepsCard("troop"),
				scn.GetFreepsCard("narsil"), scn.GetFreepsCard("cloak"));
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsUseCardAction(stone);
		scn.FreepsChooseOption("Add a threat");
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolAndAnEmptyDrawDeckNothingIsAskedOnceTheCostIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("merchant"), scn.GetFreepsCard("pippin"),
				scn.GetFreepsCard("guard"), scn.GetFreepsCard("troop"),
				scn.GetFreepsCard("narsil"), scn.GetFreepsCard("cloak"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(0);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());

		// The ability has a cost, so it is offered though nothing can come of it.
		assertTrue(scn.FreepsActionAvailable(stone));
		scn.FreepsUseCardAction(stone);
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());

		// Neither (2) can be removed nor a card drawn: there is nothing to choose between.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, stone);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
