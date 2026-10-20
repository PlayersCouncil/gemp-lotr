package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_344_Tests
{

	// The Chamber is site 4.  Site 3 is Ford of Bruinen (a sanctuary: nobody is wounded at the start of the turn
	// there) and site 5 The Bridge of Khazad-dûm, whose Shadow action plays The Balrog only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Farin, Dwarven Emissary: a [dwarven] Dwarf companion who is no Gimli, vitality 3
					put("farin", "1_11");
					// Grimir, Dwarven Elder: a Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("grimir", "1_17");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_344");
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

	@Test
	public void DwarrowdelfChamberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarrowdelf Chamber
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Underground</b>. When the fellowship moves to Dwarrowdelf Chamber, Gimli or 2 other companions must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Dwarrowdelf Chamber", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void MovingToTheChamberGimliMayExertAloneAndMovingOnFromItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, aragorn, farin);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPass();

		assertEquals("Dwarrowdelf Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each branch can be done in full: both are offered.
		assertTrue(scn.FreepsChoiceAvailable("Exert Gimli"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert Gimli");

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from the Chamber to site 5: nobody is asked to exert.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoOtherCompanionsAreChosenFromEveryCompanionButGimliAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, aragorn, farin);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		assertEquals("Dwarrowdelf Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsChoiceAvailable("Exert Gimli"));
		scn.FreepsChoose("Exert 2 other companions");

		// Gimli, who could exert, is not "other"; Grimir is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(aragorn, farin);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGimliExhaustedTwoOtherCompanionsExertWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, aragorn, farin);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 3 with 2 wounds: Gimli cannot exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		scn.FreepsPass();

		assertEquals("Dwarrowdelf Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		// Only the second branch can be done in full, so it is carried out unasked.  Farin, a Dwarf who could
		// exert, is no Gimli.
		assertFalse(scn.FreepsChoiceAvailable("Exert Gimli"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCards(frodo, farin);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherCompanionGimliExertsWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPass();

		assertEquals("Dwarrowdelf Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		// Frodo is the only companion beside Gimli, and Grimir, a Dwarf ally, is no companion: two other companions
		// cannot be exerted in full, so only the Gimli branch is open and it is carried out unasked.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNeitherBranchPossibleInFullBothAreOfferedAndTheOneOtherCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();

		assertEquals("Dwarrowdelf Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		// Gimli is exhausted and Frodo is the only other companion: no branch can be done in full, so both are
		// offered and what can be done of the one chosen is done (F-0310).
		assertTrue(scn.FreepsChoiceAvailable("Exert Gimli"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert 2 other companions");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
