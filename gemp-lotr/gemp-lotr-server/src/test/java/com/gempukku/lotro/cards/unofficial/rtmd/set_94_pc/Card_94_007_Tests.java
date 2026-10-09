package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_007_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a non-Ring-bearer companion, for the "chose someone else" case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a non-Ring-bearer companion, for the "chose someone else" case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_7
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: You may remove burdens instead of wounds during Sanctuary healing.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_7", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void RingBearerWoundedWithBurdenOffersPromptAndCanHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Both a wound and a burden are available on the chosen Ring-bearer, so the choice is offered.
		assertTrue(scn.FreepsChoiceAvailable("remove a burden"));
		assertTrue(scn.FreepsChoiceAvailable("heal a wound"));
		scn.FreepsChooseOption("Heal a wound");

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		// The burden is still there to be taken off, so the next of the five heals is asked about.
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 4"));
	}

	@Test
	public void RingBearerWoundedWithBurdenOffersPromptAndCanRemoveBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsChoiceAvailable("remove a burden"));
		scn.FreepsChooseOption("Remove a burden");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		// The burden was removed in place of one of the five heals: four are left for the wound still on Frodo.
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 4"));
	}

	@Test
	public void RingBearerUnwoundedWithBurdenRemovesBurdenWithoutPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		// No wound to heal, so the Ring-bearer is only selectable because of the burden.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Only the burden-removal option exists, so it happens directly, with no follow-up prompt.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Neither a wound nor a burden is left, so the other four heals are not asked about.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RingBearerWoundedWithNoBurdenHealsWithoutPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Only the heal option exists (no burdens to remove), so it happens directly.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OtherCompanionChosenHealsNormallyWithNoPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		// Both the Ring-bearer (burden path) and Aragorn (wound) are selectable this heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);

		// The burden option only ever applies to the Ring-bearer, so choosing someone else just heals.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		// The burden is still there to be taken off, so the next of the five heals is asked about.
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 4"));
	}

	@Test
	public void BurdensRemovedAndWoundsHealedShareTheFiveHeals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		// Six things to take off, one more than the five heals of a sanctuary.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddBurdens(4);
		scn.SkipToSite(3);

		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));

		// Frodo has no wound, so choosing him takes a burden off.
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));

		scn.FreepsChooseCard(aragorn);

		// 3 burdens removed + 2 wounds healed = the 5 heals: the last burden stays, and no sixth choice is offered.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoRemovesABurdenAtHisOwnSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var frodo = scn.GetShadowRingBearer();

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 2's second turn, his fellowship at site 2: the burden is put on a turn ahead of the sanctuary.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(1);
		// His fellowship moves on to site 3, player 1's turn is played out, and player 2's third turn begins there.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		// Player 2 is the Free Peoples player and the modifier is his: his unwounded Ring-bearer may be chosen.
		assertTrue(scn.ShadowDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo));
		scn.ShadowChooseCard(frodo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void NotOfferedToNonOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		// The mod's default "player" scope is "owner" (Shadow here); the Free Peoples player, who is
		// doing the healing at this Sanctuary site, is not offered the burden-removal option, so the
		// unwounded Ring-bearer is not made selectable by the burden alone.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		// With nobody wounded and no burden he may take off, no sanctuary healing is asked at all.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
