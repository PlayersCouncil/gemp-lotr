package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_081_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("winged1", "103_81");
					put("winged2", "103_81");
					put("winged3", "103_81");
					put("lemenya", "1_232");    // Ulaire Lemenya: a Nazgul of another title; weakens bearers of [ringwraith] conditions

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("bounder", "1_286");    // Bounder: ally whose home is site 2, where these tests are fought
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WingedNazgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Winged Nazgul
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Enduring. Fierce.
		 * 	Assignment: Return another Winged Nazgul to hand to assign this minion to an unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("winged1");

		assertEquals("Winged Nazgul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForFourPlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged1 = scn.GetShadowCard("winged1");
		scn.MoveCardsToHand(winged1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, winged1);
		assertTrue(scn.ShadowPlayAvailable(winged1));

		scn.ShadowPlayCard(winged1);

		assertInZone(Zone.SHADOW_CHARACTERS, winged1);
		// 13 - 4 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReturnsTheOtherWingedNazgulToHandAndAssignsThisOneToTheChosenUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged1 = scn.GetShadowCard("winged1");
		var winged2 = scn.GetShadowCard("winged2");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(winged1, winged2, lemenya);

		// Not in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(winged1));
		assertFalse(scn.ShadowActionAvailable(winged2));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// Each copy can send the other home; the other Nazgul has no such ability.
		assertTrue(scn.ShadowActionAvailable(winged1));
		assertTrue(scn.ShadowActionAvailable(winged2));
		assertFalse(scn.ShadowActionAvailable(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, winged2);
		assertFalse(scn.IsCharAssigned(winged1));
		assertFalse(scn.IsCharAssigned(gimli));

		scn.ShadowUseCardAction(winged1);

		// The other copy is the only card that can be returned (Ulaire Lemenya cannot), so it goes without
		// a choice, and the next thing asked is the companion. The ally is at its home site.
		assertInZone(Zone.HAND, winged2);
		assertInZone(Zone.SHADOW_CHARACTERS, winged1);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(gimli);

		assertTrue(scn.IsCharAssignedAgainst(winged1, gimli));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The assignment stands: with nobody else assigned, Gimli's skirmish is against the Winged Nazgul.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, winged1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EitherCopyMayBeTheOneUsedAndItIsTheOneAssignedWhileTheOtherGoesHome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged1 = scn.GetShadowCard("winged1");
		var winged2 = scn.GetShadowCard("winged2");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Two dots: two copies are as many as a game can have in play (CR 5.0 "non-unique"), so the copy used
		// never has two others to choose between. Here it is the second copy that is used.
		scn.MoveMinionsToTable(winged1, winged2, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, winged1);
		assertFalse(scn.IsCharAssigned(winged2));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.ShadowActionAvailable(winged2));

		scn.ShadowUseCardAction(winged2);

		// The other copy goes home with nothing asked: never the copy used, never the Nazgul of another title.
		assertInZone(Zone.HAND, winged1);
		assertInZone(Zone.SHADOW_CHARACTERS, winged2);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);

		// It is the copy whose ability was used that is assigned.
		assertTrue(scn.IsCharAssignedAgainst(winged2, aragorn));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithNoOtherWingedNazgulInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged1 = scn.GetShadowCard("winged1");
		var winged2 = scn.GetShadowCard("winged2");
		var winged3 = scn.GetShadowCard("winged3");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Copies that are not in play, and a Nazgul of another title that is.
		scn.MoveCardsToHand(winged2);
		scn.MoveCardsToDiscard(winged3);

		scn.StartGame();
		scn.MoveMinionsToTable(winged1, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(winged1));
	}

	@Test
	public void WithOnlyRingBoundCompanionsTheOtherCopyIsStillReturnedAndNobodyIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged1 = scn.GetShadowCard("winged1");
		var winged2 = scn.GetShadowCard("winged2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(winged1, winged2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The cost can be paid, so the ability is on offer though it will assign nobody.
		assertTrue(scn.ShadowActionAvailable(winged1));
		assertInZone(Zone.SHADOW_CHARACTERS, winged2);

		scn.ShadowUseCardAction(winged1);

		assertInZone(Zone.HAND, winged2);
		assertInZone(Zone.SHADOW_CHARACTERS, winged1);
		assertFalse(scn.IsCharAssigned(winged1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
