package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_311_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "51_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] companion, strength 3, vitality 4
					// Bounder: [shire] Hobbit ally, home 2; a skirmish ability that is never used here
					put("bounder", "1_286");
					// The Gaffer: [shire] Hobbit ally, home 1; a fellowship ability that is never used here
					put("gaffer", "1_291");
					// Silinde: [elven] ally, home 3; no text while site 3 is not in play
					put("silinde", "1_60");
					// Rosie Cotton, Hobbiton Lass: [shire] ally. Sam is strength +1; a fellowship ability never used here
					put("rosieally", "1_309");
					// Rosie Cotton, Barmaid: [shire] companion. Sam can only be overwhelmed at triple strength,
					// and her burdens come only when she is assigned to a skirmish
					put("rosiecomp", "12_129");
					put("stealth", "1_298");    // Hobbit Stealth: Skirmish event, at sites 1 to 5 cancels a skirmish involving a Hobbit

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("twk", "1_237");        // The Witch-king, Lord of Angmar: fierce, strength 14
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "51_311");
					put("frodo", "1_290");      // Frodo, Son of Drogo, here only a companion: strength 3
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sam, Son of Hamfast
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Aragorn
		 * Game Text: Fellowship: Exert Sam and another companion to remove a burden. Then, exert Sam again unless
		 * you can spot 2 [shire] allies (or Rosie Cotton).
		 * Response: If Frodo dies, make Sam the Ring-bearer (resistance 5).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Son of Hamfast", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
		// Not printed: every version of Sam is Ring-bound by rule.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}

	@Test
	public void PlaysFromHandAsACompanionForTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sam));

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsSamAndAnotherCompanionToRemoveABurdenThenExertsSamAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(sam, aragorn, merry);
		scn.MoveAlliesToTable(bounder, silinde);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		// Any other companion, the Ring-bearer included; not Sam himself, and not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bounder, silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// Exerted once to pay and once again: Bounder is the only [shire] ally; Silinde is an ally of
		// another culture and Merry a [shire] companion, and neither counts towards the 2.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoShireAlliesSpareSamTheSecondExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bounder, gaffer);

		scn.StartGame();
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(sam);
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RosieCottonTheAllySparesSamTheSecondExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosieally");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(rosie);

		scn.StartGame();
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Rosie is one [shire] ally, a short count on its own, but she is Rosie Cotton.
		scn.FreepsUseCardAction(sam);
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RosieCottonTheCompanionSparesSamTheSecondExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosiecomp");
		scn.MoveCompanionsToTable(sam, rosie);

		scn.StartGame();
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(sam);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, rosie));
		scn.FreepsChooseCard(frodo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SamWithOneExertionLeftCanStillUseTheAbilityAndIsNotExertedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(sam, 2);

		// Vitality 4 with 2 wounds: one exertion left, which is all the cost asks of Sam.
		assertTrue(scn.FreepsActionAvailable(sam));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Exhausted by the cost, so the second exertion cannot happen.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedUnlessSamAndAnotherCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(sam, 3);
		scn.FreepsRefreshDecision();

		// Sam exhausted, Frodo and Aragorn fresh.
		assertFalse(scn.FreepsActionAvailable(sam));

		// Sam fresh, every other companion exhausted; a fresh ally does not stand in.
		scn.RemoveWoundsFromChar(sam, 3);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(sam));

		scn.RemoveWoundsFromChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sam));

		// With no burden to remove it is still offered: it has a cost (Q-0034).
		scn.RemoveBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		// The burdens went with Frodo (Q-0008).
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoDiesOfWoundsSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertSame(frodo, scn.GetRingBearer());
		// Vitality 4: he dies when the game next checks.
		scn.AddWoundsToChar(frodo, 4);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenFrodoIsOverwhelmedInAFierceSkirmishSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var stealth = scn.GetFreepsCard("stealth");
		var twk = scn.GetShadowCard("twk");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(stealth);

		scn.StartGame();

		scn.MoveMinionsToTable(twk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, twk);
		scn.FreepsResolveSkirmish(sam);
		// At site 1: Sam's skirmish is cancelled, and The Witch-king is fierce.
		scn.FreepsPlayCard(stealth);
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(frodo, twk);
		scn.FreepsResolveSkirmish(frodo);
		// 3 + 1 for the Ring against 14: Frodo is overwhelmed.
		scn.PassCurrentPhaseActions();

		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoDiesLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
