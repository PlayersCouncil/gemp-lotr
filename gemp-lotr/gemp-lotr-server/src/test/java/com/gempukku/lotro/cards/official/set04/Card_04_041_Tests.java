package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_41");
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3, damage +1 of his own; a skirmish ability used only on purpose
					put("guard", "1_7");          // Dwarf Guard: another Dwarf companion, strength 4, vitality 2, no text in play
					put("armor", "1_8");          // Dwarven Armor: a [dwarven] possession, which is no condition
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; its text acts only as it is played

					// [dwarven] conditions: two in the support area, one a Dwarf bears, and two that play stacks cards on.
					put("lordOfMoria", "1_21");   // Lord of Moria: each Dwarf is damage +1 (each player gets a copy)
					put("endurance", "2_4");      // Endurance of Dwarves: vitality +1, on a Dwarf; a skirmish ability used only on purpose
					put("rock", "4_50");          // Here Is Good Rock: its skirmish ability stacks a Free Peoples card from hand here
					put("liking", "5_9");         // More to My Liking: its maneuver ability stacks the top card of the draw deck here, whatever it is
					// A condition of another culture that play stacks cards on; with no Aragorn in play it does nothing.
					put("end", "101_23");         // I Would Have Gone With You to the End: [gondor]
					put("toby", "1_305");         // Old Toby: filler to stack (each player gets a copy)
					put("toby2", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Axe of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Gimli.<br>He is <b>damage +1</b>.<br><b>Skirmish:</b> Discard a [dwarven] condition or a card stacked on a [dwarven] condition to make Gimli strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Axe of Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnGimliForTwoAndOnNoOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(gimli, axe);

		scn.StartGame();

		// A Dwarf companion who is not Gimli is no bearer for it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(axe));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(gimli));
		// Every Gimli has damage +1 of his own.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(axe));

		// Gimli is the only bearer there can be, so nothing is asked.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// The other Dwarf is as he was.
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsADwarvenConditionToMakeGimliStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		var endurance = scn.GetFreepsCard("endurance");
		var end = scn.GetFreepsCard("end");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.AttachCardsTo(guard, endurance);
		scn.MoveCardsToSupportArea(lordOfMoria, end);
		scn.StackCardsOn(end, toby);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		// The other player's own copy lies in his support area: not active on this turn.
		var theirLord = scn.GetShadowCard("lordOfMoria");
		scn.MoveCardsToSupportArea(theirLord);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{guard, runner});
		scn.FreepsResolveSkirmish(gimli);

		// 6 printed + 2 for the Axe
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertInZone(Zone.SUPPORT, lordOfMoria);
		assertAttachedTo(endurance, guard);
		assertTrue(scn.FreepsActionAvailable(axe));

		// Nothing is stacked on a [dwarven] condition, so only a condition can be discarded: one in the support
		// area or one a Dwarf bears.  Not a [dwarven] possession, a [gondor] condition or the card stacked on it,
		// nor the other player's [dwarven] condition.
		scn.FreepsUseCardAction(axe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lordOfMoria, endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, axe, end, toby, theirLord));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(endurance);
		assertInZone(Zone.DISCARD, endurance);
		assertInZone(Zone.SUPPORT, lordOfMoria, theirLord);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		// There is no limit: the one [dwarven] condition left is discarded for another +1, with nothing asked.
		scn.ShadowPass();
		scn.FreepsUseCardAction(axe);
		assertInZone(Zone.DISCARD, lordOfMoria);
		assertEquals(10, scn.GetStrength(gimli));

		// 13 against 10: Gimli loses, and the bonus ends with his skirmish.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(guard);
		assertEquals(8, scn.GetStrength(gimli));

		// With no [dwarven] condition of his own left there is nothing to discard: the other player's does not pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe));
	}

	@Test
	public void SkirmishAbilityMayDiscardACardStackedOnADwarvenConditionInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var liking = scn.GetFreepsCard("liking");
		var end = scn.GetFreepsCard("end");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		// The Free Peoples player's own copy of a Shadow card, as the top card of his draw deck may be.
		var stackedRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(rock, liking, end);
		scn.StackCardsOn(rock, toby);
		scn.StackCardsOn(liking, stackedRunner);
		scn.StackCardsOn(end, toby2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(8, scn.GetStrength(gimli));
		assertInZone(Zone.STACKED, toby, stackedRunner, toby2);
		assertTrue(scn.FreepsActionAvailable(axe));

		// Either can be done, so the player is asked which.
		scn.FreepsUseCardAction(axe);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("stacked");

		// A card of either side stacked on either [dwarven] condition; not the one stacked on a [gondor] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, stackedRunner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(toby2, rock, liking, end));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(stackedRunner);
		assertInZone(Zone.DISCARD, stackedRunner);
		assertInZone(Zone.STACKED, toby, toby2);
		assertInZone(Zone.SUPPORT, rock, liking, end);
		assertEquals(9, scn.GetStrength(gimli));

		// The other alternative on the same board: a condition, with what is stacked on it.
		scn.ShadowPass();
		scn.FreepsUseCardAction(axe);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Discard a DWARVEN condition");
		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, liking));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(end, toby, toby2));
		scn.FreepsChooseCard(liking);
		assertInZone(Zone.DISCARD, liking);
		assertInZone(Zone.SUPPORT, rock, end);
		assertEquals(10, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDwarvenConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var end = scn.GetFreepsCard("end");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli);
		// A [dwarven] possession, and a card stacked on a [gondor] condition: neither pays.
		scn.AttachCardsTo(gimli, axe, armor);
		scn.MoveCardsToSupportArea(end);
		scn.StackCardsOn(end, toby);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe));
		assertEquals(8, scn.GetStrength(gimli));
	}

	@Test
	public void IsNotOfferedWhenTheOnlyStackedCardIsOnTheOtherPlayersDwarvenCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		// The other player's own Free Peoples cards lie on the table and are not active on this turn.
		var theirCondition = scn.GetShadowCard("lordOfMoria");
		var theirStacked = scn.GetShadowCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(theirCondition);
		scn.StackCardsOn(theirCondition, theirStacked);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe));
		assertInZone(Zone.STACKED, theirStacked);
		assertInZone(Zone.SUPPORT, theirCondition);
		assertEquals(8, scn.GetStrength(gimli));
	}

	@Test
	public void WithADwarvenConditionOfHisOwnTheStackedAlternativeIsNotAskedWhenTheOnlyStackedCardIsOnTheOtherPlayersCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var myCondition = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(myCondition);

		// The only card stacked on a [dwarven] condition lies on the other player's, which is not active on this turn.
		var theirCondition = scn.GetShadowCard("lordOfMoria");
		var theirStacked = scn.GetShadowCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(theirCondition);
		scn.StackCardsOn(theirCondition, theirStacked);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(axe));

		// Only a condition can be discarded, so there is no choice between the alternatives and the one condition
		// is taken with nothing asked.
		scn.FreepsUseCardAction(axe);
		assertInZone(Zone.DISCARD, myCondition);
		assertInZone(Zone.STACKED, theirStacked);
		assertInZone(Zone.SUPPORT, theirCondition);
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
