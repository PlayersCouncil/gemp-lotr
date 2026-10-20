package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_346_Tests
{

	// The Lake is site 4.  Site 3 is Ford of Bruinen (a sanctuary: nobody is wounded at the start of the turn
	// there) and site 5 The Bridge of Khazad-dûm, whose Shadow action plays The Balrog only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a [shire] Hobbit companion who is no Frodo, vitality 4; a fellowship ability used only on purpose
					put("sam", "1_311");
					// Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Grimir, Dwarven Elder: a Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("grimir", "1_17");
					// Dwarf Guard: a [dwarven] Dwarf companion with vitality 2 and no text beyond its play requirement (cheated in)
					put("guard", "1_7");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_346");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer, so that the one Frodo in play is not.
	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Frodo, Frenzied Fighter: the Frodo who cannot become the Ring-bearer, [shire] Hobbit, vitality 4;
					// a skirmish ability used only on purpose
					put("frodo", "13_149");
					// Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: a [shire] Hobbit companion, vitality 4; a fellowship ability used only on purpose
					put("sam", "1_311");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_346");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaLakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Moria Lake
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Marsh</b>. When the fellowship moves to Moria Lake, Frodo or 2 other companions must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Moria Lake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void MovingToTheLakeFrodoMayExertAloneAndMovingOnFromItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each branch can be done in full: both are offered.
		assertTrue(scn.FreepsChoiceAvailable("Exert Frodo"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert Frodo");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from the Lake to site 5: nobody is asked to exert.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoOtherCompanionsAreChosenFromEveryCompanionButFrodoAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 2 with 1 wound: the Guard, a Dwarf like Gimli, cannot exert.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsChoiceAvailable("Exert Frodo"));
		scn.FreepsChoose("Exert 2 other companions");

		// Companions of three cultures and races; Frodo, who could exert, is not "other", Grimir is no companion
		// and the exhausted Guard cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, grimir, guard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(sam, gimli);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFrodoExhaustedTwoOtherCompanionsExertWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 4 with 3 wounds: Frodo cannot exert.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		// Only the second branch can be done in full, so it is carried out unasked.  Sam, a [shire] Hobbit who
		// could exert, is no Frodo.
		assertFalse(scn.FreepsChoiceAvailable("Exert Frodo"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCards(sam, aragorn);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherCompanionFrodoExertsWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		// Sam is the only companion beside Frodo, and Grimir, an ally, is no companion: two other companions
		// cannot be exerted in full, so only the Frodo branch is open and it is carried out unasked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNeitherBranchPossibleInFullBothAreOfferedAndTheOneOtherCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		// Frodo is exhausted and Sam is the only other companion: no branch can be done in full, so both are
		// offered and what can be done of the one chosen is done (F-0310).
		assertTrue(scn.FreepsChoiceAvailable("Exert Frodo"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert 2 other companions");

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnotherRingBearerItIsFrodoWhoExertsAndNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(frodo, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		// The text names Frodo, not the Ring-bearer.
		assertTrue(scn.FreepsChoiceAvailable("Exert Frodo"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert Frodo");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnotherRingBearerTheTwoOtherCompanionsMayIncludeHimButNotFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(frodo, aragorn, sam);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();

		assertEquals("Moria Lake", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsChoiceAvailable("Exert Frodo"));
		scn.FreepsChoose("Exert 2 other companions");

		// The text excepts Frodo, not the Ring-bearer: Gimli, who bears the Ring, is "other" and Frodo, who could
		// exert, is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(gimli, aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
