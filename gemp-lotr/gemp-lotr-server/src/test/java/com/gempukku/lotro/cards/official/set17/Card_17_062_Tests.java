package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "17_62");
					put("savage2", "17_62");

					put("armor", "17_41");      // Ceremonial Armor: [men] possession in the support area; its own trigger answers a discard in the maneuver phase only
					put("oath", "17_66");       // Wildman's Oath: a second [men] support-area possession, the same trigger; a skirmish ability never used here
					put("hides", "4_19");       // Hides: a [dunland] possession in the support area; its response is to a wound on a [dunland] Man
					put("wait", "13_95");       // Lying in Wait: a [men] condition in the support area; its win trigger needs it borne
					put("brand", "17_47");      // Primitive Brand: a [men] possession borne by Corps of Harad, in no support area; its trigger is on its bearer's win
					put("brand2", "17_47");     // a second Brand, a [men] card in the discard pile that is no minion
					put("corps", "11_73");      // Corps of Harad: [men] Man, to bear the Brand; fierce only at a battleground (none on this path)
					put("hillsman", "17_50");   // Stampeding Hillsman: a [men] Man in the discard pile
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), in the discard pile
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man in the discard pile (fierce only at 2 burdens)

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengefulSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vengeful Savage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, you may stack a [men] minion from your discard pile on a [men] possession in your support area.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("savage");

		assertEquals("Vengeful Savage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningStacksAMenMinionFromTheDiscardPileOnAMenPossessionInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var hides = scn.GetShadowCard("hides");
		var wait = scn.GetShadowCard("wait");
		var brand = scn.GetShadowCard("brand");
		var brand2 = scn.GetShadowCard("brand2");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, oath, hides, wait);
		scn.MoveCardsToDiscard(hillsman, saruman, axeman, brand2);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, oath, hides, wait);
		assertInZone(Zone.DISCARD, hillsman, saruman, axeman, brand2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 10 against 8: the Savage wins.  The other copy, in no skirmish, makes no offer.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage2));
		scn.ShadowAcceptOptionalTrigger();

		// The possession is asked first: the two [men] ones in the support area; not the [dunland] one there, nor the [men]
		// condition there, nor the [men] possession a minion bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hides, wait, brand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(oath);

		// Then the minion: a [men] Man and a [men] Wizard; not the [raider] Man, nor the [men] possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, brand2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertInZone(Zone.STACKED, saruman);
		assertEquals(1, scn.GetStackedCards(oath).size());
		assertTrue(scn.GetStackedCards(oath).contains(saruman));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertInZone(Zone.DISCARD, hillsman, axeman, brand2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAsItWasAndASecondWinOffersAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetShadowCard("armor");
		var hillsman = scn.GetShadowCard("hillsman");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(armor);
		scn.MoveCardsToDiscard(hillsman, saruman);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.DISCARD, hillsman, saruman);
		// Fierce only so that the Savage fights a second skirmish this turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertInZone(Zone.DISCARD, hillsman, saruman);

		// The fierce round: the Savage beats Aragorn again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(hillsman);
		// One [men] possession in the support area: it is taken unasked.
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertTrue(scn.GetStackedCards(armor).contains(hillsman));
		assertInZone(Zone.DISCARD, saruman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetShadowCard("armor");
		var hillsman = scn.GetShadowCard("hillsman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(armor);
		scn.MoveCardsToDiscard(hillsman);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.DISCARD, hillsman);
		// Aragorn at 13 beats the Savage's 10 without overwhelming him: he takes a wound and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The Armor's response to the wound on a [men] minion is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertInZone(Zone.DISCARD, hillsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningWithNoMenMinionInTheDiscardPileOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetShadowCard("armor");
		var brand2 = scn.GetShadowCard("brand2");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(armor);
		// Only a [raider] Man and a [men] possession in the discard pile.
		scn.MoveCardsToDiscard(axeman, brand2);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.DISCARD, axeman, brand2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningWithNoMenPossessionInTheSupportAreaOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var hides = scn.GetShadowCard("hides");
		var wait = scn.GetShadowCard("wait");
		var brand = scn.GetShadowCard("brand");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, corps);
		// A [men] possession borne by a minion, a [dunland] possession and a [men] condition in the support area.
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(hides, wait);
		scn.MoveCardsToDiscard(hillsman);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, hides, wait);
		assertInZone(Zone.DISCARD, hillsman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertInZone(Zone.DISCARD, hillsman);
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertEquals(0, scn.GetStackedCards(hides).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
