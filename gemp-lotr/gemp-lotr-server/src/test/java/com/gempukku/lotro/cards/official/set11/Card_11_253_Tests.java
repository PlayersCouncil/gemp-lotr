package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_253_Tests
{

	// A Shadows-block path with Pelennor Fields as its first site, so that the game starts there with its text in
	// force; site 2 is Abandoned Mine Shaft 13_185, whose only text is a burden for a regroup move from it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bree", "11_90");       // Man of Bree: [men] Man minion; strength +1 for each card in the Free Peoples hand
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion; abilities used only on purpose
					put("dunlending", "11_79"); // Fearsome Dunlending: [men] Man minion printed Ambush (1); its play trigger is not reached by a cheat
					put("poleaxe", "11_95");    // Poleaxe: a [men] possession, borne by a [men] minion
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion; "Damage +1." is its whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "11_253");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorFieldsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pelennor Fields
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Plains</b>. Each [men] minion gains <b>ambush</b> (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Pelennor Fields", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachMenMinionGainsAmbush1AndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fields = scn.GetFreepsSite("site1");
		var bree = scn.GetShadowCard("bree");
		var ted = scn.GetShadowCard("ted");
		var dunlending = scn.GetShadowCard("dunlending");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame(fields);
		assertSame(fields, scn.GetCurrentSite());
		scn.MoveMinionsToTable(bree, ted, dunlending, savage);
		scn.AttachCardsTo(bree, poleaxe);

		// A [men] Man and a [men] Hobbit: one instance each.
		assertEquals(1, scn.GetKeywordCount(bree, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.AMBUSH));
		// A second instance beside the printed one.
		assertEquals(2, scn.GetKeywordCount(dunlending, Keyword.AMBUSH));
		// A minion of another culture, and a [men] card that is no minion.
		assertFalse(scn.HasKeyword(savage, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(poleaxe, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnTheMenMinionsLoseTheAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fields = scn.GetFreepsSite("site1");
		var bree = scn.GetShadowCard("bree");
		var ted = scn.GetShadowCard("ted");
		var dunlending = scn.GetShadowCard("dunlending");

		scn.StartGame(fields);
		scn.MoveMinionsToTable(bree, ted, dunlending);
		assertEquals(1, scn.GetKeywordCount(bree, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(dunlending, Keyword.AMBUSH));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		// The Fields are behind the fellowship, and a site's text holds only where the fellowship is.
		assertSame(scn.GetShadowSite("site2"), scn.GetCurrentSite());
		assertFalse(scn.HasKeyword(bree, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(ted, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(dunlending, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
