package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skinbark", "15_37");

					// [gandalf] conditions
					put("laststand", "18_21");  // Last Stand: [gandalf] support-area condition, cost 2; abilities used only on purpose
					put("halfling", "8_18");    // Not the First Halfling: [gandalf] support-area condition, cost 1, no requirement to play
					put("draught", "9_24");     // Ent Draught: [gandalf] condition a companion bears; vitality +1, a skirmish ability used only on purpose
					// Cards that are not [gandalf] conditions
					put("path", "8_42");        // A Path Appointed: a [gondor] condition
					put("glamdring", "1_75");   // Glamdring: a [gandalf] possession Gandalf bears, damage +1
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion
					put("wizard", "1_78");      // Mysterious Wizard: a [gandalf] Skirmish event
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("huorn", "9_25");       // Huorn: a [gandalf] companion, kept in hand
					put("ottar", "1_80");       // Ottar, Man of Laketown: a [gandalf] ally, for the other player's discard pile
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, cost 4, to read in hand beside Skinbark

					put("seeker", "4_195");     // Uruk Seeker: fierce Uruk-hai, strength 6, vitality 2: Skinbark (8) beats it twice
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text: beats Skinbark (8) without overwhelming him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SkinbarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Skinbark, Elder Ent
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 7
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: Skinbark is twilight cost -1 for each [gandalf] condition you can spot.<br>Each time Skinbark wins a skirmish, you may place a [gandalf] card from your discard pile beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skinbark");

		assertEquals("Skinbark", card.getBlueprint().getTitle());
		assertEquals("Elder Ent", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

@Test
	public void IsTwilightCostMinusOneForEachGandalfConditionSpottedSupportAreaOrBorne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var halfling = scn.GetFreepsCard("halfling");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		var draught = scn.GetFreepsCard("draught");
		var glamdring = scn.GetFreepsCard("glamdring");
		var laststand = scn.GetFreepsCard("laststand");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(gandalf);
		// A [gandalf] condition he bears beside a [gandalf] possession; one in the support area beside a [gondor] one.
		scn.AttachCardsTo(gandalf, draught, glamdring);
		scn.MoveCardsToSupportArea(laststand, path);
		scn.MoveCardsToHand(skinbark, halfling, avenger);

		scn.StartGame();

		assertAttachedTo(draught, gandalf);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, laststand, path);
		assertInZone(Zone.HAND, skinbark, halfling, avenger);
		// Ent Draught and Last Stand: 7 - 2.  Glamdring, A Path Appointed and Gandalf himself do not count.
		assertEquals(5, scn.GetTwilightCost(skinbark));
		// Another companion in hand is discounted by nothing.
		assertEquals(4, scn.GetTwilightCost(avenger));

		scn.FreepsPlayCard(halfling);
		assertInZone(Zone.SUPPORT, halfling);
		// A third: 7 - 3
		assertEquals(4, scn.GetTwilightCost(skinbark));
		assertEquals(4, scn.GetTwilightCost(avenger));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(skinbark);

		assertInZone(Zone.FREE_CHARACTERS, skinbark);
		// 7 printed - 3 for Ent Draught, Last Stand and Not the First Halfling
		assertEquals(twilight + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeSkinbarkWinsASkirmishHeMayPlaceAGandalfCardFromTheDiscardPileBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizard = scn.GetFreepsCard("wizard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn");
		var shadowOttar = scn.GetShadowCard("ottar");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(skinbark);
		// [gandalf] cards of three types and a [gondor] card in the pile; a [gandalf] card in hand, and one in
		// the other player's pile that is no card of this one.
		scn.MoveCardsToDiscard(gandalf, glamdring, wizard, aragorn);
		scn.MoveCardsToDiscard(shadowOttar);
		scn.MoveCardsToHand(huorn);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gandalf, glamdring, wizard, aragorn, shadowOttar);
		assertInZone(Zone.HAND, huorn);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(skinbark, seeker);
		scn.FreepsResolveSkirmish(skinbark);
		var deckCount = scn.GetFreepsDeckCount();
		var shuffles = scn.GetFreepsDeckShuffleCount();
		scn.PassSkirmishActions();

		// 8 against 6: Skinbark wins.
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		scn.FreepsChooseOptionalTrigger(skinbark);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, glamdring, wizard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, huorn, shadowOttar));
		scn.FreepsChooseCard(glamdring);

		assertInZone(Zone.DECK, glamdring);
		assertSame(glamdring, scn.GetFreepsBottomOfDeck());
		assertEquals(deckCount + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DISCARD, gandalf, wizard, aragorn, shadowOttar);

		// The Seeker is fierce, lives with one wound, and loses to Skinbark again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(skinbark, seeker);
		scn.FreepsResolveSkirmish(skinbark);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		scn.FreepsChooseOptionalTrigger(skinbark);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, wizard));
		scn.FreepsChooseCard(wizard);

		assertSame(wizard, scn.GetFreepsBottomOfDeck());
		assertSame(glamdring, scn.GetFromBottomOfFreepsDeck(2));
		assertEquals(deckCount + 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, gandalf, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(skinbark);
		scn.MoveCardsToDiscard(gandalf);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gandalf);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(skinbark, troop);
		scn.FreepsResolveSkirmish(skinbark);
		assertEquals(0, scn.GetWoundsOn(skinbark));
		scn.PassSkirmishActions();

		// 8 against 13: he loses and lives, with a wound.
		assertEquals(1, scn.GetWoundsOn(skinbark));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		assertInZone(Zone.DISCARD, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGandalfCardInTheDiscardPileAWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(skinbark);
		scn.MoveCardsToDiscard(aragorn);
		scn.MoveCardsToHand(huorn);

		scn.StartGame();

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.HAND, huorn);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(skinbark, seeker);
		scn.FreepsResolveSkirmish(skinbark);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(seeker));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		assertInZone(Zone.DISCARD, aragorn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherCompanionWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wizard = scn.GetFreepsCard("wizard");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(skinbark, gandalf);
		scn.MoveCardsToDiscard(wizard);

		scn.StartGame();

		assertInZone(Zone.DISCARD, wizard);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, seeker);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// Gandalf (7) beats the Seeker (6) while Skinbark stands by: it is not Skinbark who wins.
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		assertInZone(Zone.DISCARD, wizard);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheDiscardPileAndTheDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var gandalf = scn.GetFreepsCard("gandalf");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(skinbark);
		scn.MoveCardsToDiscard(gandalf);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gandalf);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(skinbark, seeker);
		scn.FreepsResolveSkirmish(skinbark);
		var deckCount = scn.GetFreepsDeckCount();
		var bottom = scn.GetFreepsBottomOfDeck();
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, gandalf);
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
