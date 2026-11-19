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

public class Card_07_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "7_98");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight and no other text
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man companion, strength 6; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability used only on purpose
					put("king", "8_38");        // King of the Dead, Oathbreaker: a [gondor] companion who is a Wraith, no Man; a response for Aragorn only
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, Valiant and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, here only a card in hand
					put("bow", "1_41");         // Elven Bow: an [elven] possession, here only a card in hand
					put("defiance", "1_37");    // Defiance: an [elven] Skirmish event, here only a card in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, here only a card in hand
					put("runner1", "1_178");    // Goblin Runner: a [moria] Orc, strength 5, vitality 1; its play trigger never fires when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondorian Sword
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Skirmish:</b> Discard 3 cards from hand to make bearer <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Gondorian Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertFalse(sword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sword.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var king = scn.GetFreepsCard("king");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, king, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		// A [gondor] Man companion and the [gondor] Man ally; not the [gondor] Wraith, the [rohan] Man or the Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(king, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(merchant);
		assertAttachedTo(sword, merchant);
		assertEquals(0, scn.GetAttachedCards(knight).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThreeCardsFromHandToMakeBearerDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.AttachCardsTo(knight, sword);
		assertAttachedTo(sword, knight);
		// A companion, a possession, an event and a Shadow minion in hand.
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, bow, defiance, handTroop);
		assertInHand(aragorn, bow, defiance, handTroop);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, runner1}, new PhysicalCardImpl[]{ranger, runner2});
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);
		// Any three cards of the hand, of either side and any type.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, bow, defiance, handTroop));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, defiance, handTroop);

		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(bow);
		assertInDiscard(aragorn, defiance, handTroop);
		// The bearer, and not the other [gondor] Man.
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Knight, 6 + 2, beats the Runner; in the next skirmish the bonus is gone.
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(ranger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
	}

	@Test
	public void NotOfferedWithTwoCardsInHandAndOfferedWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, sword);
		assertAttachedTo(sword, knight);
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCardsToHand(aragorn, bow);
		assertInHand(aragorn, bow);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(sword));

		scn.MoveCardsToHand(defiance);
		assertInHand(defiance);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void NotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, sword);
		assertAttachedTo(sword, knight);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("bow"), scn.GetFreepsCard("defiance"));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(sword));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void ABearerInNoSkirmishMayUseItAndIsMadeDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.AttachCardsTo(knight, sword);
		assertAttachedTo(sword, knight);
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCardsToHand(aragorn, bow, defiance);
		assertInHand(aragorn, bow, defiance);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		// The Ranger's skirmish; the bearer is assigned to nobody.
		scn.FreepsAssignToMinions(ranger, runner1);
		scn.FreepsResolveSkirmish(ranger);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		// The text names no skirmish.
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(aragorn, bow, defiance);
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
