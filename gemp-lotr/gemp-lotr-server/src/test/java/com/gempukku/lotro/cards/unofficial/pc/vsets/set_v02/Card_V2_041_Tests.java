package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_041_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two copies: the second is read once the first has emptied the pool.
					put("peak1", "102_41");
					put("peak2", "102_41");

					// [moria] minions.
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc, strength 13, no text
					put("runner", "1_178");     // Goblin Runner: an Orc; its text is about being played
					// Cave Troll of Moria: a unique [moria] Troll, damage +1 and fierce.  Nothing is ever
					// assigned to it here.
					put("troll", "1_165");
					// Orc Soldier: an Orc that is not [moria] but [sauron].  Its ability is a skirmish one.
					put("soldier", "1_271");
					// Pinned Down: a [moria] card in play that is no minion.  It needs a [moria] archer to do
					// anything.
					put("pinned", "1_192");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven], vitality 3
					put("guard", "1_7");        // Dwarf Guard: vitality 2, so exhausted with one wound
					// Bounder: an ally, vitality 2, who could exert.  His ability is a skirmish one.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ToHighestPeakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: To Highest Peak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard X [Moria] minions to add X threats. The Free Peoples player may exert X companions
		 * to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("peak1");

		assertEquals("To Highest Peak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsOneMoriaMinionToAddOneThreatAndCostsOneTwilightToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak1 = scn.GetShadowCard("peak1");
		var peak2 = scn.GetShadowCard("peak2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var pinned = scn.GetShadowCard("pinned");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(peak1, peak2);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troll, soldier);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(peak1));
		assertTrue(scn.ShadowPlayAvailable(peak2));

		scn.ShadowPlayCard(peak1);
		assertEquals(0, scn.GetTwilight());
		// A [moria] Orc and a [moria] Troll; not the [sauron] Orc, nor the [moria] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, pinned));
		// X may be anything from none to all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(troll);
		assertInZone(Zone.DISCARD, troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, soldier);
		assertInZone(Zone.SUPPORT, pinned);

		// One minion, so one companion to exert, and the Free Peoples player says no.
		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		assertEquals(0, scn.GetThreats());
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, peak1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The pool is empty and a [moria] minion is still in play: the second copy cannot be paid for.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.HAND, peak2);
		assertFalse(scn.ShadowPlayAvailable(peak2));
	}

	@Test
	public void DiscardsTwoMoriaMinionsToAddTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, troll);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(peak));

		scn.ShadowPlayCard(peak);
		assertEquals(3 - 1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner, troll));
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(runner, troll);
		assertInZone(Zone.DISCARD, runner, troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		scn.FreepsChooseNo();
		// Three companions, so there is room for both threats.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, peak);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertTwoCompanionsToPreventTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		// Vitality 2 with one wound: the Guard is a companion who cannot exert.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(troop, runner, troll);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(peak);
		assertEquals(3 - 1, scn.GetTwilight());
		scn.ShadowChooseCards(troop, runner);
		assertInZone(Zone.DISCARD, troop, runner);

		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		scn.FreepsChooseYes();

		// The Ring-bearer and two other companions of two cultures; not the exhausted companion, and not
		// the ally, who could exert.  Exactly two of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, bounder));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsChooseCards(frodo, gimli);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// No threat; the minions stay discarded and the event stays paid for.
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DISCARD, troop, runner, peak);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertOneCompanionToPreventOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(peak);
		scn.ShadowChooseCards(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DISCARD, runner, peak);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionMustExertForEachMinionDiscardedThoughFewerThreatsCouldBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Two companions and one threat already: by the rules only one more threat can be added.
		scn.AddThreats(1);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());

		// X is the minions discarded, whatever the dead pile has room for.
		scn.ShadowPlayCard(peak);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, runner);
		assertInZone(Zone.DISCARD, troop, runner);

		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseYes();
		// Two minions were discarded, so both companions exert, to keep one threat off.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFewerCompanionsAbleToExertThanMinionsDiscardedNobodyIsAskedAndTheThreatsAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		// Vitality 4 with three wounds: Aragorn is exhausted.  Frodo can exert, and so could the ally.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(peak);
		scn.ShadowChooseCards(troop, runner);
		assertInZone(Zone.DISCARD, troop, runner);

		// Two companions would have to exert and only one can: there is nothing to ask.
		assertFalse(scn.FreepsDecisionAvailable("to prevent"));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMoriaMinionToDiscardItMayStillBePlayedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var pinned = scn.GetShadowCard("pinned");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// A [moria] minion in hand is not one to discard.
		scn.MoveCardsToHand(peak, troop);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(1);
		scn.FreepsPass();

		// An event may be played for nothing, to be rid of it: X is 0.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(peak));

		scn.ShadowPlayCard(peak);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, peak);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.SUPPORT, pinned);
		assertInZone(Zone.HAND, troop);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Nothing to prevent, and nobody is asked.
		assertFalse(scn.FreepsDecisionAvailable("to prevent"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsPlayedInTheRegroupPhaseAndNotInTheAssignmentPhaseNorASkirmishBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak1");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(peak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// 3 set + 2 companions + 2 for the site: enough for the event in every phase that follows.
		assertEquals(7, scn.GetTwilight());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInZone(Zone.HAND, peak);
		assertFalse(scn.ShadowPlayAvailable(peak));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(peak));

		// The Troop (13) beats Aragorn (8) and lives to the regroup phase.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(peak));
	}
}
