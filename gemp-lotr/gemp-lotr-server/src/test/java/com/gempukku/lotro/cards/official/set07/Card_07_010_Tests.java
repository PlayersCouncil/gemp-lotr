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

public class Card_07_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loyalty1", "7_10");
					put("loyalty2", "7_10");      // a second copy: the card is not unique and says "here" and "this condition"

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only
					// Stairs of Khazad-dum: [dwarven] condition, cost 0, to the support area; its text is about
					// [moria] minions.
					put("stairs", "1_24");

					// Cards for the hand and for the stack.
					put("axe", "1_9");            // Dwarven Axe: [dwarven] possession
					put("strike", "1_3");         // Axe Strike: [dwarven] skirmish event
					put("strike2", "1_3");
					put("sleep", "1_84");         // Sleep, Caradhras: [gandalf] event
					put("luthien", "1_100");      // The Choice of Luthien: [gondor] condition

					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two copies of the condition are the Free Peoples player's whole hand.
	protected VirtualTableScenario GetStackOfferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loyalty1", "7_10");
					put("loyalty2", "7_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoyaltyUnshakenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Loyalty Unshaken
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, you may stack 2 cards from hand here and draw a card for each [dwarven] card you stack.<br><b>Skirmish:</b> Make a Dwarf strength +2. Also, make that Dwarf <b>damage +2</b> for each [dwarven] card stacked on this condition. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("loyalty1");

		assertEquals("Loyalty Unshaken", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  With no card left
	// in hand nothing can be stacked, and no card is drawn "for each [dwarven] card you stack".
	@Test
	public void TheOfferToStackIsMadeWhileACardIsLeftInHandAndNotOnceTheHandIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackOfferScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var loyalty2 = scn.GetFreepsCard("loyalty2");
		scn.MoveCardsToHand(loyalty1, loyalty2);

		scn.StartGame();
		assertInHand(loyalty1, loyalty2);

		// The second copy is still in hand: there is a card to stack.
		scn.FreepsPlayCard(loyalty1);
		assertInZone(Zone.SUPPORT, loyalty1);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(loyalty1));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(loyalty2);

		// It is the last card in hand: once it is played the hand is empty.
		scn.FreepsPlayCard(loyalty2);
		assertInZone(Zone.SUPPORT, loyalty2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(loyalty2));
		assertEquals(0, scn.GetStackedCards(loyalty2).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedTwoDwarvenCardsFromHandAreStackedHereAndTwoCardsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(loyalty1, axe, strike, sleep);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(loyalty1, axe, strike, sleep);

		scn.FreepsPlayCard(loyalty1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(loyalty1));
		scn.FreepsAcceptOptionalTrigger();

		// Any card in hand, of any type or culture; exactly two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, strike, sleep));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(axe, strike);

		// A [dwarven] possession and a [dwarven] event: a card drawn for each.
		assertEquals(2, scn.GetStackedCards(loyalty1).size());
		assertTrue(scn.GetStackedCards(loyalty1).contains(axe));
		assertTrue(scn.GetStackedCards(loyalty1).contains(strike));
		assertInHand(sleep);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyStacksOnItselfAndDrawsOnlyForItsDwarvenCardAndAnotherDwarvenConditionBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var loyalty2 = scn.GetFreepsCard("loyalty2");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var sleep = scn.GetFreepsCard("sleep");
		var luthien = scn.GetFreepsCard("luthien");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(loyalty1);
		scn.StackCardsOn(loyalty1, axe);
		scn.MoveCardsToHand(loyalty2, strike, sleep, luthien, stairs);

		scn.StartGame();

		assertEquals(1, scn.GetStackedCards(loyalty1).size());
		assertTrue(scn.GetStackedCards(loyalty1).contains(axe));
		assertInHand(loyalty2, strike, sleep, luthien, stairs);
		int deck = scn.GetFreepsDeckCount();

		// Only the copy just played answers its own play.
		scn.FreepsPlayCard(loyalty2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(loyalty1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(loyalty2));
		scn.FreepsChooseOptionalTrigger(loyalty2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, sleep, luthien, stairs));
		scn.FreepsChooseCards(strike, sleep);

		// One [dwarven] card of the two: one card drawn. The [gandalf] event counts for nothing.
		assertEquals(2, scn.GetStackedCards(loyalty2).size());
		assertTrue(scn.GetStackedCards(loyalty2).contains(strike));
		assertTrue(scn.GetStackedCards(loyalty2).contains(sleep));
		assertEquals(1, scn.GetStackedCards(loyalty1).size());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(luthien, stairs);

		// Another [dwarven] condition played after it: neither copy answers.
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetStackedCards(loyalty1).size());
		assertEquals(2, scn.GetStackedCards(loyalty2).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesADwarfStrengthPlus2AndDamagePlus2ForEachDwarvenCardStackedHereForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var loyalty2 = scn.GetFreepsCard("loyalty2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(loyalty1, loyalty2);
		// Two [dwarven] cards and a [gandalf] one here; a [dwarven] card on the other copy.
		scn.StackCardsOn(loyalty1, axe, strike, sleep);
		scn.StackCardsOn(loyalty2, strike2);

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");

		scn.StartGame();
		assertEquals(3, scn.GetStackedCards(loyalty1).size());
		assertTrue(scn.GetStackedCards(loyalty1).contains(axe));
		assertTrue(scn.GetStackedCards(loyalty1).contains(strike));
		assertTrue(scn.GetStackedCards(loyalty1).contains(sleep));
		assertEquals(1, scn.GetStackedCards(loyalty2).size());
		assertInZone(Zone.STACKED, axe, strike, sleep, strike2);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		assertTrue(scn.FreepsActionAvailable(loyalty1));
		scn.FreepsUseCardAction(loyalty1);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally; not a companion who is no
		// Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// 4 + 2; damage +2 for each of the two [dwarven] cards stacked on this copy, read before it went.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// This copy is discarded, and what was stacked on it goes too. The other copy keeps its own.
		assertInDiscard(loyalty1, axe, strike, sleep);
		assertInZone(Zone.SUPPORT, loyalty2);
		assertEquals(1, scn.GetStackedCards(loyalty2).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: the Guard is still strengthened.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// 6 against 7: the Guard loses and lives with one wound.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsResolveSkirmish(gimli);

		// A new skirmish phase: the bonuses went with the last one.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
	}

	@Test
	public void WithOneDwarvenCardStackedTheDwarfIsDamagePlus2AndTheAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(loyalty1);
		scn.StackCardsOn(loyalty1, axe);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertTrue(scn.GetStackedCards(loyalty1).contains(axe));
		assertInZone(Zone.STACKED, axe);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(loyalty1));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Gimli is the one Dwarf, and is chosen unasked.
		assertTrue(scn.FreepsActionAvailable(loyalty1));
		scn.FreepsUseCardAction(loyalty1);

		// 6 + 2; his own damage +1 and +2 for the one [dwarven] card stacked.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertInDiscard(loyalty1, axe);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarfInPlayTheAbilityIsStillOfferedAndOnlyDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(loyalty1);
		scn.StackCardsOn(loyalty1, axe);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertTrue(scn.GetStackedCards(loyalty1).contains(axe));
		assertInZone(Zone.STACKED, axe);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		int frodoStrength = scn.GetStrength(frodo);

		// The ability's own words discard the card, so it is offered with no Dwarf to strengthen.
		assertTrue(scn.FreepsActionAvailable(loyalty1));
		scn.FreepsUseCardAction(loyalty1);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertInDiscard(loyalty1, axe);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
