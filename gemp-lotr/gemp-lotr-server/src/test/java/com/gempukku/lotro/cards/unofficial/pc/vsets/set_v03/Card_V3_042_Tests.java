package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_042_Tests
{

// ----------------------------------------
// BLADETUSK MATRIARCH TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("matriarch", "103_42");   // Bladetusk Matriarch
					put("southron1", "4_222");    // Desert Warrior - Southron Man
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("southron4", "4_222");
					put("southron5", "4_222");
					put("southron6", "4_222");
					put("southron7", "4_222");    // 7th for testing archery limit
					put("orc", "1_271");          // Orc Soldier - not a Southron

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskMatriarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Matriarch
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 2
		 * Vitality: 6
		 * Game Text: Each time a Southron is played, you may stack a Southron from play or hand here.
		 * 		Maneuver: Remove (6) to make this artifact a <b>fierce</b> mounted Southron minion until the end of
		 * 		the turn that is strength +3 and <b>ambush (1)</b> for each Southron stacked on her. She adds 1 to
		 * 		the Shadow archery total for each Southron stacked on her (limit 6).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("matriarch");

		assertEquals("Bladetusk Matriarch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}



// ========================================
// STACKING TRIGGER TESTS
// ========================================

	@Test
	public void BladetuskMatriarchTriggersWhenSouthronPlayedOfferingStackFromPlayOrHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var southron1 = scn.GetShadowCard("southron1"); // To play
		var southron2 = scn.GetShadowCard("southron2"); // In play, can stack
		var southron3 = scn.GetShadowCard("southron3"); // In hand, can stack
		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCardsToHand(southron1, southron3);
		scn.MoveMinionsToTable(southron2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetStackedCards(matriarch).size());

		// Play a Southron
		scn.ShadowPlayCard(southron1);

		// Should trigger optional stacking
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Should be offered choice between play and hand
		assertTrue(scn.ShadowChoiceAvailable("Stack a Southron"));
		scn.ShadowChoose("hand");

		// southron3 should be auto-selected as only Southron in hand
		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertInZone(Zone.STACKED, southron3);
	}

	@Test
	public void BladetuskMatriarchCanStackFromPlayWhenTriggered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCardsToHand(southron1);
		scn.MoveMinionsToTable(southron2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(southron1);
		scn.ShadowAcceptOptionalTrigger();

		// Can choose southron1 (just played) or southron2 (already in play)
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(southron2);

		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertInZone(Zone.STACKED, southron2);
		assertInZone(Zone.SHADOW_CHARACTERS, southron1); // The played one stays
	}

	@Test
	public void BladetuskMatriarchDoesNotTriggerWhenNonSouthronPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCardsToHand(orc);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(orc);

		// Should not trigger - Orc is not a Southron
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ========================================
// TRANSFORMATION TESTS
// ========================================


	@Test
	public void BladetuskMatriarchTransformsWithStackedSouthronBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(matriarch);
		scn.StackCardsOn(matriarch, southron1, southron2, southron3);
		scn.MoveMinionsToTable(southron4);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowUseCardAction(matriarch);

		// Verify cost paid
		assertEquals(twilightBefore - 6, scn.GetTwilight());

		// Verify transformation
		assertInZone(Zone.SHADOW_CHARACTERS, matriarch);
		assertTrue(scn.HasKeyword(matriarch, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(matriarch, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(matriarch, Keyword.SOUTHRON));

		// Base strength 6 + (3 Southrons * 3) = 15
		assertEquals(15, scn.GetStrength(matriarch));

		// Ambush (3) for 3 stacked Southrons
		assertTrue(scn.HasKeyword(matriarch, Keyword.AMBUSH));
		assertEquals(3, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
	}

	@Test
	public void BladetuskMatriarchAddsToShadowArcheryTotalWithLimit6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var southron5 = scn.GetShadowCard("southron5");
		var southron6 = scn.GetShadowCard("southron6");
		var southron7 = scn.GetShadowCard("southron7");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveMinionsToTable("orc");
		scn.StackCardsOn(matriarch, southron1, southron2, southron3, southron4, southron5, southron6, southron7);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		int shadowArcheryBefore = scn.GetShadowArcheryTotal();

		scn.ShadowUseCardAction(matriarch);

		// 7 Southrons stacked, but archery bonus is limited to 6
		assertEquals(shadowArcheryBefore + 6, scn.GetShadowArcheryTotal());

		// Strength and Ambush are NOT limited
		// Base 6 + (7 * 3) = 27 strength
		assertEquals(27, scn.GetStrength(matriarch));
		assertEquals(7, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
	}

	@Test
	public void BladetuskMatriarchIsDiscardedAsMinionDuringRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var southron1 = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveMinionsToTable("orc");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(matriarch);

		assertInZone(Zone.SHADOW_CHARACTERS, matriarch);

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToStay();

		// Matriarch and stacked cards should be discarded
		assertInDiscard(matriarch);
	}
}
