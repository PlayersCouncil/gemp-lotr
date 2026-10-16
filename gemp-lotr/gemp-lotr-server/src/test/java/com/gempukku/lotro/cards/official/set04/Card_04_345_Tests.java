package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_345_Tests
{

	// The Towers path with White Mountains as its site 4.  Site 3 is Barrows of Edoras (a sanctuary; its Fellowship
	// action is used only on purpose) and site 5 Deeping Wall, whose Shadow action plays Saruman only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion, vitality 2; a Regroup ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a Maneuver ability only
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion who is no Théoden, vitality 3; valiant only
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, vitality 2, home 4T; a Fellowship ability only
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_345");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with White Mountains as its site 4; Théoden and two other companions.
	protected VirtualTableScenario GetWhiteMountainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "4_292");
					put("aragorn", "1_89");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_345");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: White Mountains
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 4T
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. When the fellowship moves to White Mountains, Théoden or 2 other companions must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("White Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	// "Théoden or 2 other companions must exert": the definition let Théoden be one of the two.
	@Test
	public void TheTwoCompanionsWhoExertAreOtherThanTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWhiteMountainsScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsChoiceAvailable("Exert Théoden"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert 2 other companions");

		// Frodo and Aragorn are the only two companions other than Théoden: they exert with nothing more asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToWhiteMountainsTheodenMayExertAloneAndMovingOnFromItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden, aragorn, guard);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));

		scn.FreepsPass();

		assertEquals("White Mountains", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each branch can be done in full: both are offered.
		assertTrue(scn.FreepsChoiceAvailable("Exert Théoden"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert Théoden");

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from White Mountains to site 5: nobody is asked to exert.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoOtherCompanionsAreChosenFromEveryCompanionButTheodenAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden, aragorn, guard);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPass();

		assertEquals("White Mountains", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsChoiceAvailable("Exert Théoden"));
		scn.FreepsChoose("Exert 2 other companions");

		// Théoden, who could exert, is not "other"; Guma, a [rohan] Man, is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, guma));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(aragorn, guard);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheodenExhaustedTwoOtherCompanionsExertWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, aragorn, guard);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 2 with 1 wound: Théoden cannot exert.
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();

		assertEquals("White Mountains", scn.GetCurrentSite().getBlueprint().getTitle());
		// Only the second branch can be done in full, so it is carried out unasked.  The Guard, a [rohan] Man
		// companion who could exert, is no Théoden.
		assertFalse(scn.FreepsChoiceAvailable("Exert Théoden"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden));
		scn.FreepsChooseCards(frodo, guard);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherCompanionTheodenExertsWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));

		scn.FreepsPass();

		assertEquals("White Mountains", scn.GetCurrentSite().getBlueprint().getTitle());
		// Frodo is the only companion beside Théoden, and Guma, a [rohan] Man ally, is no companion: two other
		// companions cannot be exerted in full, so only the Théoden branch is open and it is carried out unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNeitherBranchPossibleInFullBothAreOfferedAndTheOneOtherCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();

		assertEquals("White Mountains", scn.GetCurrentSite().getBlueprint().getTitle());
		// Théoden is exhausted and Frodo is the only other companion: no branch can be done in full, so both are
		// offered and what can be done of the one chosen is done (F-0310).
		assertTrue(scn.FreepsChoiceAvailable("Exert Théoden"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert 2 other companions");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
