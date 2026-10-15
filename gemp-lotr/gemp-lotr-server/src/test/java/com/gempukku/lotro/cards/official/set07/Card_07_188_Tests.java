package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_188_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brute", "7_188");
					put("brute2", "7_188");
					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul; text only touches [ringwraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: a [raider] Nazgul; text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: [ringwraith] Orc and no Nazgul; text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulBruteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Brute
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot a Nazgûl to add a burden. The Free Peoples player
		 * may wound the Ring-bearer to prevent this.
		 * Skirmish: Remove a burden to make this minion strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brute");

		assertEquals("Morgul Brute", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithANazgulInPlayItMayAddABurdenWhichTheFreePeoplesPlayerMayLetStand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(brute));
		scn.ShadowPlayCard(brute);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayWoundTheRingBearerToPreventTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(brute);
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The Ring-bearer takes the wound, with nobody asked to choose: Sam, Ring-bound but not the Ring-bearer,
		// and Aragorn are not wounded.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToAddTheBurdenAndNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(brute);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnyCultureIsSpottedButARingwraithOrcIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var brute2 = scn.GetShadowCard("brute2");
		var hound = scn.GetShadowCard("hound");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(brute, brute2);

		scn.StartGame();
		// A [ringwraith] minion who is an Orc and no Nazgul.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(brute);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Nazgul who is not [ringwraith].
		scn.MoveMinionsToTable(attea);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(brute2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute2));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedDoesNotSetItOffAndASecondCopyAddsOneBurdenForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var brute2 = scn.GetShadowCard("brute2");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(brute2, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(brute);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A Nazgul is played with a Morgul Brute in play: that Brute was not the minion played.
		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(brute2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();

		// One burden, for the copy played; the copy already in play adds nothing.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesABurdenToMakeThisMinionStrengthPlus3UntilItsSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var brute = scn.GetShadowCard("brute");
		var brute2 = scn.GetShadowCard("brute2");

		scn.StartGame();
		scn.MoveMinionsToTable(brute, brute2);
		scn.AddBurdens(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, brute}, new PhysicalCardImpl[]{frodo, brute2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(brute));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(brute));
		scn.ShadowUseCardAction(brute);
		assertEquals(9, scn.GetStrength(brute));
		assertEquals(1, scn.GetBurdens());
		// The other copy, in the other skirmish, is untouched.
		assertEquals(6, scn.GetStrength(brute2));

		scn.FreepsPass();
		scn.ShadowUseCardAction(brute);
		assertEquals(12, scn.GetStrength(brute));
		assertEquals(0, scn.GetBurdens());

		// No burden left to remove.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(brute));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(brute));
		scn.ShadowPass();

		// 12 against Aragorn's 8: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is another skirmish phase: the bonus has gone.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(6, scn.GetStrength(brute));
		assertEquals(6, scn.GetStrength(brute2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
