package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapper", "13_89");
					put("trapper2", "13_89");
					put("column", "11_72");     // Column of Easterlings: [men] Man minion, lurker; a skirmish ability used only on purpose
					put("fearsome", "11_79");   // Fearsome Dunlending: [men] Man minion, ambush; a play trigger that is not set off when cheated in
					put("soldier", "4_220");    // Desert Soldier: [raider] Man minion; a start-of-skirmish trigger only
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; a play trigger and a skirmish ability
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support-area possession; a play trigger and a regroup ability
					put("overrun", "11_92");    // Overrun: [men] skirmish event
					put("goblinrunner", "1_178"); // Goblin Runner: [moria] Orc minion; "When you play" only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingTrapperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Dunlending Trapper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>To play, spot a [men] minion.<br><b>Assignment:</b> Discard a [men] card from your hand to assign this minion to an unbound companion.
		 */

		var scn = GetScenario();

		var trapper = scn.GetFreepsCard("trapper");

		assertEquals("Dunlending Trapper", trapper.getBlueprint().getTitle());
		assertNull(trapper.getBlueprint().getSubtitle());
		assertFalse(trapper.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, trapper.getBlueprint().getSide());
		assertEquals(Culture.MEN, trapper.getBlueprint().getCulture());
		assertEquals(CardType.MINION, trapper.getBlueprint().getCardType());
		assertEquals(Race.MAN, trapper.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(trapper, Keyword.LURKER));
		assertEquals(3, trapper.getBlueprint().getTwilightCost());
		assertEquals(7, trapper.getBlueprint().getStrength());
		assertEquals(2, trapper.getBlueprint().getVitality());
		assertEquals(4, trapper.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAMenMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapper = scn.GetShadowCard("trapper");
		var column = scn.GetShadowCard("column");
		var soldier = scn.GetShadowCard("soldier");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(trapper, column);
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		// A Man minion of another culture, and a [men] card that is no minion.
		scn.MoveMinionsToTable(soldier);
		assertInZone(Zone.SUPPORT, stragglers);
		assertInHand(trapper, column);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trapper));

		scn.ShadowPlayCard(column);
		assertInZone(Zone.SHADOW_CHARACTERS, column);
		assertTrue(scn.ShadowPlayAvailable(trapper));
		scn.ShadowPlayCard(trapper);

		assertInZone(Zone.SHADOW_CHARACTERS, trapper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMenMinionOfAnotherTitleAlsoLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapper = scn.GetShadowCard("trapper");
		var fearsome = scn.GetShadowCard("fearsome");
		scn.MoveCardsToHand(trapper);

		scn.StartGame();
		scn.MoveMinionsToTable(fearsome);
		assertInHand(trapper);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(trapper));
		scn.ShadowPlayCard(trapper);

		assertInZone(Zone.SHADOW_CHARACTERS, trapper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardingAMenCardFromHandAssignsItselfToTheChosenUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);

		var trapper = scn.GetShadowCard("trapper");
		var trapper2 = scn.GetShadowCard("trapper2");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var overrun = scn.GetShadowCard("overrun");
		var goblinrunner = scn.GetShadowCard("goblinrunner");
		scn.MoveCardsToHand(column, stragglers, fleet, overrun, goblinrunner);

		scn.StartGame();
		scn.MoveMinionsToTable(trapper, trapper2);
		assertInHand(column, stragglers, fleet, overrun, goblinrunner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(trapper));
		assertFalse(scn.IsCharAssigned(trapper));
		scn.ShadowUseCardAction(trapper);

		// A [men] card of each kind; not the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(column, stragglers, fleet, overrun));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblinrunner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stragglers);
		assertInDiscard(stragglers);
		assertInHand(column, fleet, overrun, goblinrunner);

		// Two unbound companions of other cultures and races; not the Ring-bearer, nor Sam, who is Ring-bound, nor the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// This minion, and not the other copy.
		assertTrue(scn.IsCharAssignedAgainst(gimli, trapper));
		assertFalse(scn.IsCharAssigned(trapper2));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithNoMenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var trapper = scn.GetShadowCard("trapper");
		var column = scn.GetShadowCard("column");
		var goblinrunner = scn.GetShadowCard("goblinrunner");
		scn.MoveCardsToHand(goblinrunner);

		scn.StartGame();
		scn.MoveMinionsToTable(trapper);
		assertInHand(goblinrunner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trapper));

		// A [men] card in hand, and it is offered.
		scn.MoveCardsToHand(column);
		assertInHand(column);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(trapper));
	}

	@Test
	public void WithOnlyRingBoundCompanionsTheCardIsStillDiscardedAndNobodyIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var trapper = scn.GetShadowCard("trapper");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToHand(column);

		scn.StartGame();
		scn.MoveMinionsToTable(trapper);
		assertInHand(column);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The cost can be paid, so the ability is offered (Q-0034); the lone [men] card goes unasked.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(trapper));
		scn.ShadowUseCardAction(trapper);

		assertInDiscard(column);
		assertFalse(scn.IsCharAssigned(trapper));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
