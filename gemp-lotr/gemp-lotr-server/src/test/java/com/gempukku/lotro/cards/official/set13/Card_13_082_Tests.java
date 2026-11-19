package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "13_82");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] companion, strength 7; his ability is never used
					put("barliman", "1_70");      // Barliman Butterbur: an ally, vitality 2; his ability is never used
					// Easterling Runner: [men] Man, strength 8; a Shadow ability used only on purpose
					put("runner", "13_90");
					// Saruman, Coldly Still: a [men] minion who is a Wizard, strength 8.  "Each [men] minion bearing a
					// possession is strength +2" (none bears one); his skirmish ability is never used here.
					put("saruman", "17_48");
					put("brigand", "4_10");       // Dunlending Brigand: a Man of the [dunland] culture, strength 8, no text
					// Stragglers: a [men] support-area condition, a [men] card that is no minion; its skirmish
					// ability is never used here.
					put("stragglers", "13_99");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BringDowntheWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Bring Down the Wall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a companion to make a [men] minion strength +1 for each wound on that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall");

		assertEquals("Bring Down the Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingACompanionWithTwoWoundsMakesAMenMinionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		var wall = scn.GetShadowCard("wall");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, saruman, brigand);
		scn.MoveCardsToSupportArea(stragglers);
		scn.MoveCardsToHand(wall);
		// Two wounds on Aragorn, one on Frodo, and one on the ally, who is no companion.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(barliman, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInHand(wall);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// Saruman and the Brigand are left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.ShadowPlayAvailable(wall));
		scn.ShadowPlayCard(wall);

		// Either companion may be spotted; the ally is no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// A [men] minion in the skirmish or out of it; not the [dunland] Man, nor the [men] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, stragglers));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		// 8 + 1 for each of the two wounds on Aragorn.
		assertEquals(10, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(wall);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpottingACompanionWithOneWoundGivesPlus1UntilThisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wall = scn.GetShadowCard("wall");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, saruman);
		scn.MoveCardsToHand(wall);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{aragorn, saruman});
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowPlayCard(wall);
		scn.ShadowChooseCard(aragorn);
		scn.ShadowChooseCard(saruman);

		// 8 + 1 for the one wound on Aragorn, given to Saruman, whose skirmish is still to come.
		assertEquals(9, scn.GetStrength(saruman));

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(saruman));

		// The Runner's 8 beats Boromir's 7; then Saruman's own skirmish, a phase of its own.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wall = scn.GetShadowCard("wall");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToHand(wall);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInHand(wall);

		// The Shadow player's assignment actions, next door to the skirmish.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wall));
		assertInHand(wall);
		assertEquals(8, scn.GetStrength(runner));
	}
}
