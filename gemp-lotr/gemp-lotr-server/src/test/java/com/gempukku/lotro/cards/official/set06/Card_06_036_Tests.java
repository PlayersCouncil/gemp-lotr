package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threw", "6_36");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: strength 6, +1 beside a Hobbit companion; vitality 4
					put("merry", "1_302");        // Merry, Friend to Sam: a second companion to skirmish; his ability is used only on purpose

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage1", "1_151");      // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("savage2", "1_151");
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; adds (2) when played, never played here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrewDownMyEnemyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Threw Down My Enemy
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spell.
		 * Skirmish: Make a minion skirmishing Gandalf strength -1 for each wound on each character in the skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("threw");

		assertEquals("Threw Down My Enemy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMinionSkirmishingGandalfStrengthMinus1ForEachWoundOnEachCharacterInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threw = scn.GetFreepsCard("threw");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(threw);
		// One wound on each character of Gandalf's skirmish, and wounds on characters outside it.
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merry, 1);

		var troop = scn.GetShadowCard("troop");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(troop, savage1, savage2);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage1, 1);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{merry, savage2});
		// The Shadow player puts the minion left over on Gandalf as well.
		scn.ShadowAssignToMinions(gandalf, savage1);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsPlayAvailable(threw));

		scn.FreepsPlayCard(threw);
		// Either minion skirmishing Gandalf, of two cultures and races; not the one skirmishing Merry.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInDiscard(threw);
		// 13 - 3: the wounds on Gandalf, the Troop and the Savage, not those on Frodo, Merry or the other Savage.
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheCountIsTakenAsItIsPlayedAndTheMinusLastsTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threw = scn.GetFreepsCard("threw");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(threw);
		scn.AddWoundsToChar(gandalf, 1);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(troop, runner);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{merry, runner});
		scn.FreepsResolveSkirmish(gandalf);

		scn.FreepsPlayCard(threw);
		// The Troop is the one minion skirmishing Gandalf, so nobody is asked.  13 - 2.
		assertEquals(11, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A wound placed afterwards adds nothing: the count was taken as the event was played.
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(troop));

		// The Troop, 11 against Gandalf's 7, wins; in Merry's skirmish, the next, it is 13 again.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(merry);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndPlayedForNothingOutsideGandalfsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threw = scn.GetFreepsCard("threw");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(threw);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(merry, 1);

		var troop = scn.GetShadowCard("troop");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(troop, savage2);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(savage2));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(threw));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{merry, savage2});
		scn.FreepsResolveSkirmish(merry);

		// Merry's skirmish: the Savage skirmishes Merry, and the Troop waits for Gandalf's skirmish.
		assertTrue(scn.FreepsPlayAvailable(threw));
		scn.FreepsPlayCard(threw);
		assertInDiscard(threw);
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
