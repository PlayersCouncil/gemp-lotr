package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("challenging", "12_109");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, resistance 6; "Valiant." is his whole text
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion, resistance 6; his abilities are used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; "Knight." is his whole text
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a regroup ability used only on purpose (cheated in)

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 is its whole text
					put("banner", "6_59");      // Banner of Isengard: a Shadow possession, no minion; its win trigger is not reached
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its play trigger is not reached (cheated in)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChallengingtheOrchostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Challenging the Orc-host
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] companion strength +2 (or +1 for each minion you can spot if that companion has resistance 5 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("challenging");

		assertEquals("Challenging the Orc-host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BelowResistance5ARohanCompanionIsStrengthPlus2WhateverTheMinionsUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var challenging = scn.GetFreepsCard("challenging");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, merry, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(challenging);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(challenging);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{knight, troop});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// 6, less 2 burdens
		assertEquals(4, scn.GetResistance(guard));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(challenging);
		// [rohan] companions, the Hobbit not in a skirmish among them; not the [gondor] Knight or the [rohan] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, challenging);
		// +2, not +1 for each of the 3 minions in play
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(knight);

		// The next skirmish is a phase of its own: the bonus is gone.
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtResistance5ItIsPlus1ForEachMinionInPlayWhereverItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var challenging = scn.GetFreepsCard("challenging");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveCardsToHand(challenging);

		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(challenging);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());

		scn.MoveMinionsToTable(savage, troop, runner);
		scn.AttachCardsTo(savage, banner);
		assertEquals(savage, banner.getAttachedTo());
		assertInZone(Zone.ATTACHED, banner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{knight, troop});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// 6, less 1 burden
		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetStrength(guard));

		// The Guard is the one [rohan] companion, and is chosen unasked.
		scn.FreepsPlayCard(challenging);

		// 6, and 1 for each minion: the Savage he skirmishes, the Troop waiting for the Knight and the Runner
		// assigned to nobody; the Banner is no minion.
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithResistance5OrMoreAndOneMinionItIsPlus1NotPlus2AndItIsNotPlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var challenging = scn.GetFreepsCard("challenging");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(challenging);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(challenging);

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(challenging));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsPlayAvailable(challenging));

		scn.FreepsPlayCard(challenging);
		// The parenthesis is taken whenever it is available (F-0047, Q-0035): 6 and 1 for the one minion.
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
