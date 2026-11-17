package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("temptation", "12_163");
					put("lemenya", "1_232");     // Ulaire Lemenya: [wraith] Nazgul, cost 4; strength -2 to bearers of [wraith] conditions only
					put("khamul", "103_61");     // Ulaire Attea, Khamul of the East: the [raider] Nazgul; his text needs 3 burdens
					put("hound", "7_192");       // Morgul Hound: [wraith] Orc, no Nazgul; stronger and fierce at 2 wounds on the Ring-bearer
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkTemptationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dark Temptation
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play a Nazgûl, you may heal the Ring-bearer twice to add a burden.
		*/

		var scn = GetScenario();

		var temptation = scn.GetFreepsCard("temptation");

		assertEquals("Dark Temptation", temptation.getBlueprint().getTitle());
		assertNull(temptation.getBlueprint().getSubtitle());
		assertFalse(temptation.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, temptation.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, temptation.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, temptation.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(temptation, Keyword.SUPPORT_AREA));
		assertEquals(1, temptation.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachNazgulPlayedLetsYouHealTheRingBearerTwiceToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var temptation = scn.GetShadowCard("temptation");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(temptation);
		scn.MoveCardsToHand(lemenya, khamul);
		assertInZone(Zone.SUPPORT, temptation);
		assertInHand(lemenya, khamul);

		scn.StartGame();
		// Three wounds, so that healing twice leaves one, which neither one heal nor every wound healed would.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 2);
		scn.SetTwilight(20);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		// A [wraith] Nazgul.
		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(temptation));
		scn.ShadowChooseOptionalTrigger(temptation);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Sam, Ring-bound but not the Ring-bearer, is not healed.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The [raider] Nazgul, played with two wounds on Frodo again.
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(temptation));
		scn.ShadowChooseOptionalTrigger(temptation);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NeitherItsOwnPlayNorAWraithMinionWhoIsNoNazgulSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var temptation = scn.GetShadowCard("temptation");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(temptation, hound);
		assertInHand(temptation, hound);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.SetTwilight(20);
		assertEquals(2, scn.GetWoundsOn(frodo));
		scn.FreepsPass();

		scn.ShadowPlayCard(temptation);
		assertInZone(Zone.SUPPORT, temptation);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(temptation));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(hound);
		assertInPlay(hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(temptation));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheWoundsAndBurdensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var temptation = scn.GetShadowCard("temptation");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(temptation);
		scn.MoveCardsToHand(lemenya);
		assertInZone(Zone.SUPPORT, temptation);
		assertInHand(lemenya);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.SetTwilight(20);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(temptation));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneWoundOnTheRingBearerTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var temptation = scn.GetShadowCard("temptation");
		var lemenya = scn.GetShadowCard("lemenya");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(temptation);
		scn.MoveCardsToHand(lemenya);
		assertInZone(Zone.SUPPORT, temptation);
		assertInHand(lemenya);

		scn.StartGame();
		// One heal can be paid and not two (F-0425: a cost is judged payable in full); Sam's wounds pay nothing.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.SetTwilight(20);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		assertInPlay(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(temptation));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
