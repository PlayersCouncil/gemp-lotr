package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_061_Tests
{

	// The Hobbit table, with Wooded Steep Cliff as site 4 in place of Goblin Town: the turn begins at Rivendell
	// (site 3) and the fellowship phase's move goes to the Cliff.  Site 5 is Mirkwood, which discards Gandalf as
	// the fellowship moves there, so no Gandalf is laid in a test that moves on.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4.  Strength +1 for each [dwarven]
					// follower he bears (none here); a fellowship ability used only on purpose.
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability used
					// only on purpose.
					put("balin", "30_2");
					// Gandalf, The Grey: [gandalf] Wizard companion, vitality 4.  A trigger on his player's
					// [gandalf] events and a fellowship ability, neither reached.
					put("gandalf", "30_27");
					// Thorin III, Stonehelm: a [dwarven] Dwarf companion whose title is not Thorin, vitality 3.
					// A trigger on a minion losing a skirmish he is in, never reached.
					put("thorin3", "17_6");
					// Dain Ironfoot: [dwarven] Dwarf ally, vitality 3.  Thorin is strength +1; a start-of-regroup
					// offer that needs a [dwarven] follower, which no test lays.
					put("dain", "32_2");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site4", "30_61");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WoodedSteepCliffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Wooded Steep Cliff
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Forest</b>. When the fellowship moves to Wooded Steep Cliff, Thorin or 2 other companions must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Wooded Steep Cliff", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void MovingToTheCliffThorinMayExertAloneAndMovingOnFromItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin, balin, thorin3);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin3));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsPass();

		assertEquals("Wooded Steep Cliff", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each branch can be done in full: both are offered.
		assertTrue(scn.FreepsChoiceAvailable("Exert Thorin"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert Thorin");

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin3));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from the Cliff to Mirkwood (site 5): nobody is asked to exert.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoOtherCompanionsAreChosenFromEveryCompanionButThorinAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin, balin, gandalf, thorin3);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 3 with 2 wounds: Thorin III, a Dwarf like Balin, cannot exert.
		scn.AddWoundsToChar(thorin3, 2);
		assertEquals(2, scn.GetWoundsOn(thorin3));
		scn.FreepsPass();

		assertEquals("Wooded Steep Cliff", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsChoiceAvailable("Exert Thorin"));
		scn.FreepsChoose("Exert 2 other companions");

		// Companions of three cultures and races; Thorin, who could exert, is not "other", Dain is no companion
		// and the exhausted Thorin III cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thorin, dain, thorin3));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(bilbo, gandalf);

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(2, scn.GetWoundsOn(thorin3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThorinExhaustedTwoOtherCompanionsExertWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		scn.MoveCompanionsToTable(thorin, balin, thorin3);

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 4 with 3 wounds: Thorin cannot exert.
		scn.AddWoundsToChar(thorin, 3);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(thorin3));

		scn.FreepsPass();

		assertEquals("Wooded Steep Cliff", scn.GetCurrentSite().getBlueprint().getTitle());
		// Only the second branch can be done in full, so it is carried out unasked.  Thorin III, a [dwarven]
		// Dwarf companion who could exert, is no Thorin.
		assertFalse(scn.FreepsChoiceAvailable("Exert Thorin"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, thorin3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thorin));
		scn.FreepsChooseCards(balin, thorin3);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(thorin3));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherCompanionThorinExertsWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsPass();

		assertEquals("Wooded Steep Cliff", scn.GetCurrentSite().getBlueprint().getTitle());
		// Bilbo is the only companion beside Thorin, and Dain, an ally, is no companion: two other companions
		// cannot be exerted in full, so only the Thorin branch is open and it is carried out unasked.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNeitherBranchPossibleInFullBothAreOfferedAndTheOneOtherCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(thorin, 3);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.FreepsPass();

		assertEquals("Wooded Steep Cliff", scn.GetCurrentSite().getBlueprint().getTitle());
		// Thorin is exhausted and Bilbo is the only other companion: no branch can be done in full, so both are
		// offered and what can be done of the one chosen is done (F-0310).
		assertTrue(scn.FreepsChoiceAvailable("Exert Thorin"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 other companions"));
		scn.FreepsChoose("Exert 2 other companions");

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
