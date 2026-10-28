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

public class Card_07_296_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brood", "7_296");
					put("brood2", "7_296");     // a second copy, in the same skirmish
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon PLAYED on him only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for the hand
					put("clutches", "7_178");   // Foul Clutches: a Shadow event, for the hand
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession with no other text, for the hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcBroodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Brood
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Discard 3 cards from hand to make this minion strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brood");

		assertEquals("Orc Brood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardingThreeCardsFromHandMakesThisCopyStrengthPlus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var brood = scn.GetShadowCard("brood");
		var brood2 = scn.GetShadowCard("brood2");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var clutches = scn.GetShadowCard("clutches");
		var sword = scn.GetShadowCard("sword");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		// Four cards in the Shadow hand: a minion, an event, and a Free Peoples possession and companion.
		scn.MoveCardsToHand(troop, clutches, sword, shadowAragorn);
		assertInHand(troop, clutches, sword, shadowAragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, brood2, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, brood}, new PhysicalCardImpl[]{aragorn, chieftain});
		// Both copies against Boromir.
		scn.ShadowAssignToMinions(boromir, brood2);
		assertTrue(scn.IsCharAssignedAgainst(boromir, brood));
		assertTrue(scn.IsCharAssignedAgainst(boromir, brood2));

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(7, scn.GetStrength(brood));
		assertEquals(7, scn.GetStrength(brood2));
		assertTrue(scn.ShadowActionAvailable(brood));

		scn.ShadowUseCardAction(brood);
		// Any 3 of the 4 cards in hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, clutches, sword, shadowAragorn));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(troop, clutches, sword);
		assertInDiscard(troop, clutches, sword);
		assertInHand(shadowAragorn);
		assertEquals(1, scn.GetShadowHandCount());

		// 7 + 3 on this copy alone; the other copy in the same skirmish is untouched.
		assertEquals(10, scn.GetStrength(brood));
		assertEquals(7, scn.GetStrength(brood2));
		assertEquals(7, scn.GetStrength(chieftain));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(brood));

		// 10 + 7 against Boromir's 7: he takes a wound.  The next skirmish is another phase: the bonus is gone.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(brood));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoCardsInHandAndOfferedWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var brood = scn.GetShadowCard("brood");
		var troop = scn.GetShadowCard("troop");
		var clutches = scn.GetShadowCard("clutches");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(troop, clutches);
		assertInHand(troop, clutches);

		scn.StartGame();
		scn.MoveMinionsToTable(brood);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brood);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(brood));

		// A third card: now it is offered.
		scn.MoveCardsToHand(sword);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(brood));
		assertEquals(7, scn.GetStrength(brood));
	}

	@Test
	public void ABroodInNoSkirmishMayUseItDuringAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var brood = scn.GetShadowCard("brood");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var clutches = scn.GetShadowCard("clutches");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(troop, clutches, sword);
		assertInHand(troop, clutches, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, chieftain);
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(brood));

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(brood));
		assertTrue(scn.ShadowActionAvailable(brood));

		scn.ShadowUseCardAction(brood);
		// The three cards are all there is: they go unasked.
		assertInDiscard(troop, clutches, sword);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(10, scn.GetStrength(brood));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
