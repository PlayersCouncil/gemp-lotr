package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortheshire", "103_108");
					put("merry", "1_302");
					put("sam", "1_311");
					put("pippin", "1_306");
					put("bilbo", "1_284");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For the Shire
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Spot a Hobbit companion (except the Ring-bearer).
		 * 	In region 1, hinder all other Hobbits.
		 * 	In region 2, make that Hobbit defender +2 until the regroup phase.
		 * 	In region 3, make that Hobbit <b>enduring</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortheshire");

		assertEquals("For the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ForTheShireRegion1HindersAllOtherHobbits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry, sam, pippin);
		scn.MoveCardsToSupportArea(bilbo);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();

		// Site 3 is still region 1
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Play assignment event, choose Merry as the spotted Hobbit
		scn.FreepsPlayCard(fortheshire);
		scn.FreepsChooseCard(merry);

		// All other Hobbits should be hindered — including Frodo
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(pippin));
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(bilbo));
	}

	@Test
	public void ForTheShireRegion2GrantsDefenderPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();

		// Get to region 2 (sites 4-6)
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 4 (region 2)

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Play assignment event — Merry is only valid Hobbit, auto-chosen
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DEFENDER));
	}

	@Test
	public void ForTheShireRegion3GrantsEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();

		// Get to region 3 (sites 7-9)
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 7 (region 3)

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Play assignment event — Merry is only valid Hobbit, auto-chosen
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.HasKeyword(merry, Keyword.ENDURING));
	}
}
