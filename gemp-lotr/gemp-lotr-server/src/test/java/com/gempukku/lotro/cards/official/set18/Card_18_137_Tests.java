package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_137_Tests
{
	// A Shadows-block path, with Morannon Plains as the site the fellowship first moves to.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Heirs of Gondor: [gondor] support-area condition. "When you play this, add a [gondor] token
					// here." Its skirmish ability is not used.
					put("heirs", "13_69");
					// Noble Leaders: [gondor] support-area condition whose tokens come as it is played, one for each of
					// four named Men: none here, so it has no token.
					put("leaders", "7_112");
					// Ships of Great Draught: [raider] possession in the Shadow player's support area. "Shadow: Remove
					// a threat or discard a [raider] card from hand to add a [raider] token here." Not used.
					put("ships", "8_65");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site2", "18_137");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void MorannonPlainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Morannon Plains
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 1
		 * Type: Site
		 * Subtype:
		 * Site Number: *
		 * Block: Shadows
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. <b>Plains</b>. When the fellowship moves to this site, each player may reinforce a culture token.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site2");

		assertEquals("Morannon Plains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenTheFellowshipMovesHereEachPlayerMayReinforceACultureToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToSupportArea(heirs, leaders);

		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		// The Free Peoples player first: a [gondor] token or a [raider] one; not the card with no token.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce a culture token"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(heirs, ships));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		// Then the Shadow player, who reinforces his own.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to reinforce a culture token"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(heirs, ships));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(leaders));
		scn.ShadowChooseCard(ships);

		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals("Morannon Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheTokensAndMovingOnFromHereAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCardsToSupportArea(heirs);

		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(ships, 1);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce a culture token"));
		scn.FreepsChooseNo();
		assertTrue(scn.ShadowDecisionAvailable("Would you like to reinforce a culture token"));
		scn.ShadowChooseNo();

		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second move of the turn, from Morannon Plains to the next site: neither player is asked.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));

		assertFalse(scn.FreepsDecisionAvailable("Would you like to reinforce a culture token"));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to reinforce a culture token"));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals("Crags of Emyn Muil", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCultureTokenInPlayNeitherPlayerIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToSupportArea(leaders);

		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(0, scn.GetCultureTokensOn(ships));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		assertFalse(scn.FreepsDecisionAvailable("Would you like to reinforce a culture token"));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to reinforce a culture token"));
		assertEquals("Morannon Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
