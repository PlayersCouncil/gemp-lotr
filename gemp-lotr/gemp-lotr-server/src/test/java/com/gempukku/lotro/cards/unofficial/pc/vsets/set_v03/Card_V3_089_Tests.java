package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("takeitdown", "103_89");
					put("theoden", "7_255");       // Théoden, Rekindled King
					put("eomer", "4_267");         // Éomer, TMOR
					put("rider", "4_286");         // Rider of Rohan - Rohan companion
					put("aragorn", "1_89");        // Non-Rohan

					put("witchking", "1_237");
					put("fellbeast", "6_83");      // Mount for Nazgul
					put("rearguard", "103_43");    // Bladetusk Rearguard
					put("southron1", "4_222");     // Desert Warrior
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("swarms", "1_183");        // Goblin Swarms - for stacking test
					put("runner", "1_178");        // Goblin Runner - to stack
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TakeitDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Take it Down!
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Theoden or Eomer to wound a mounted minion twice.  You may then exert X [rohan] companions to discard X stacked Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("takeitdown");

		assertEquals("Take it Down!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ======== COST TESTS ========

	@Test
	public void TakeItDownExertsTheodenOrEomerAsCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var witchking = scn.GetShadowCard("witchking");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.MoveCompanionsToTable(theoden, eomer);
		scn.MoveCardsToHand(takeitdown);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, fellbeast);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(theoden, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));

		scn.FreepsPlayCard(takeitdown);

		// Choose which to exert
		assertTrue(scn.FreepsHasCardChoicesAvailable(theoden, eomer));
		scn.FreepsChooseCard(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
	}

	@Test
	public void TakeItDownNotPlayableWithoutTheodenOrEomer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var rider = scn.GetFreepsCard("rider");
		var witchking = scn.GetShadowCard("witchking");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(takeitdown);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, fellbeast);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		// No Théoden or Éomer, can't play
		assertFalse(scn.FreepsPlayAvailable(takeitdown));
	}

// ======== MOUNTED MINION WOUND TESTS ========

	@Test
	public void TakeItDownWoundsMountedMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var witchking = scn.GetShadowCard("witchking");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToHand(takeitdown);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, fellbeast);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(theoden, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(witchking));

		scn.FreepsPlayCard(takeitdown);
		// Théoden auto-selected as only valid cost target
		// Witch-king auto-selected as only mounted minion

		// Decline optional exert for stacked cards
		scn.FreepsDeclineChoosing();

		assertEquals(2, scn.GetWoundsOn(witchking));
	}

// ======== STACKED CARD DISCARD TESTS ========

	@Test
	public void TakeItDownDiscardsStackedCardsForEachRohanExerted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var eomer = scn.GetFreepsCard("eomer");
		var witchking = scn.GetShadowCard("witchking");
		var fellbeast = scn.GetShadowCard("fellbeast");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(theoden, rider, eomer);
		scn.MoveCardsToHand(takeitdown);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, fellbeast);
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(theoden, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		scn.FreepsPlayCard(takeitdown);
		scn.FreepsChooseCard(theoden);

		// Witch-king wounded twice
		assertEquals(2, scn.GetWoundsOn(witchking));

		// Choose to exert 1 Rohan companion to discard 1 stacked card
		// Rider and Éomer are valid Rohan companions (Théoden already exerted but still valid if not exhausted)
		scn.FreepsChooseCard(rider);

		assertEquals(1, scn.GetWoundsOn(rider));

		assertInDiscard(runner);
	}

// ======== MUMAK INTEGRATION TEST ========

	@Test
	public void TakeItDownWoundsAndDiscardsFromTransformedMumak() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var eomer = scn.GetFreepsCard("eomer");
		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(theoden, rider, eomer);
		scn.MoveCardsToHand(takeitdown);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, southron1, southron2, southron3);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		// Transform the Mumak
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(rearguard);

		// Verify transformation
		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertEquals(0, scn.GetWoundsOn(rearguard));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(theoden, rearguard);
		scn.ShadowAcceptOptionalTrigger(); //ambush
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		// Mumak has 3 Southrons stacked
		assertEquals(3, scn.GetStackedCards(rearguard).size());

		scn.FreepsPlayCard(takeitdown);
		scn.FreepsChooseCard(theoden);

		// Rearguard is only mounted minion - auto-selected, wounded twice
		assertEquals(2, scn.GetWoundsOn(rearguard));

		// Exert 2 Rohan companions to discard 2 stacked Southrons
		scn.FreepsChooseCards(rider, eomer);

		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(eomer));

		// Choose which stacked cards to discard
		scn.FreepsChooseCards(southron1, southron2);

		assertInDiscard(southron1);
		assertInDiscard(southron2);
		// One Southron remains
		assertEquals(1, scn.GetStackedCards(rearguard).size());
	}
}
