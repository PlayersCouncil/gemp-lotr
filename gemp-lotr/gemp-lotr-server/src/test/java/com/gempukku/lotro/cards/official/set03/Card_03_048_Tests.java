package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warily", "3_48");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion, vitality 3; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally, vitality 1; a maneuver ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: a Free Peoples card for the Free Peoples hand, cost 2

					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, cost 1
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2, site 5; damage +1 only
					put("wreathed", "1_238");   // Wreathed in Shadow: a Shadow condition, cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeMustGoWarilyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: We Must Go Warily
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the fellowship moves in the regroup phase, exert a [gondor] companion twice to make each minion's twilight cost +1 until the next regroup phase.
		*/

		var scn = GetScenario();

		var warily = scn.GetFreepsCard("warily");

		assertEquals("We Must Go Warily", warily.getBlueprint().getTitle());
		assertNull(warily.getBlueprint().getSubtitle());
		assertFalse(warily.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, warily.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, warily.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, warily.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(warily, Timeword.RESPONSE));
		assertEquals(0, warily.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheFellowshipPhaseMoveIsNotAnsweredAndARegroupMoveRaisesEachMinionsCostByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warily = scn.GetFreepsCard("warily");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(warily, legolas);
		scn.MoveCardsToHand(runner, savage, wreathed);

		scn.StartGame();
		scn.SetShadowDrawDeck();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInHand(warily, legolas, runner, savage, wreathed);

		// The move at the end of the fellowship phase is no move in the regroup phase: nothing is asked of the
		// Free Peoples player, and the Shadow phase follows at once with every cost as printed.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(warily));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(2, scn.GetTwilightCost(savage));
		assertEquals(0, scn.GetTwilightCost(wreathed));
		assertEquals(2, scn.GetTwilightCost(legolas));

		// No minion is played, so the Shadow phase is followed by the regroup phase.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warily));
		scn.FreepsChooseOptionalTrigger(warily);

		// Boromir, the only [gondor] companion, exerts twice.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInDiscard(warily);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Each minion is +1; a Shadow card that is no minion and a companion are not.
		assertEquals(2, scn.GetTwilightCost(runner));
		assertEquals(3, scn.GetTwilightCost(savage));
		assertEquals(0, scn.GetTwilightCost(wreathed));
		assertEquals(2, scn.GetTwilightCost(legolas));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(savage);
		// 10 - (2 printed + 1 + 2 roaming at site 3)
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheExertionsArePaidByAGondorCompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warily = scn.GetFreepsCard("warily");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(warily);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		// The Merchant's vitality raised by hand, so that he too could exert twice: only his type keeps him out.
		scn.ApplyAdHocModifier(new VitalityModifier(null, merchant, null, 2));
		assertEquals(3, scn.GetVitality(merchant));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warily));
		scn.FreepsChooseOptionalTrigger(warily);

		// The [gondor] companions of two races are offered; the companion of another culture and the [gondor]
		// ally are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(pippin);

		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(2, scn.GetTwilightCost(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyGondorCompanionCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warily = scn.GetFreepsCard("warily");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveCardsToHand(warily);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		// Vitality 3 with 1 wound: Boromir can exert once, not twice.  Gimli could exert twice, but is no [gondor] companion.
		scn.AddWoundsToChar(boromir, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(warily);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Nothing is asked of the Free Peoples player: the Shadow phase follows with costs as printed.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(warily));
		assertInHand(warily);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetTwilightCost(runner));
	}

	@Test
	public void TheCostIsStillRaisedInTheSkirmishPhaseAndPrintedAgainAtTheNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warily = scn.GetFreepsCard("warily");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warily);
		scn.MoveCardsToHand(runner, savage);

		scn.StartGame();
		scn.SetShadowDrawDeck();

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.FreepsChooseOptionalTrigger(warily);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// In the skirmish phase after the regroup move the Runner would still cost 1 more.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetTwilightCost(runner));

		// Aragorn (8) beats the Savage (5); the regroup phase comes next, and with it the Runner's printed cost.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetTwilightCost(runner));
	}
}
