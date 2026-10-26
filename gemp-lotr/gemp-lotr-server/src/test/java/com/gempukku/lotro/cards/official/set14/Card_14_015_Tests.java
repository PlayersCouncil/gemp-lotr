package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "14_15");
					// Two more copies, one for each of the keywords the first does not take.
					put("scout2", "14_15");
					put("scout3", "14_15");
					// Brawling Uruk: another [uruk-hai] Uruk-hai; a response to a companion's special ability, never used here.
					put("brawling", "11_179");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Uruk-hai Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this, choose one of the following keywords: battleground, mountain, plains, or underground. The fellowship's current site gains that keyword until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Uruk-hai Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedTheFellowshipsSiteGainsTheChosenKeywordUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var brawling = scn.GetShadowCard("brawling");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(scout, brawling);
		scn.FreepsPass();
		// Rivendell Valley, site 3: a sanctuary with none of the four keywords.
		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(scout);
		// The choice of the four keywords, and no way to decline it.
		assertEquals(4, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Battleground"));
		assertTrue(scn.ShadowChoiceAvailable("Mountain"));
		assertTrue(scn.ShadowChoiceAvailable("Plains"));
		assertTrue(scn.ShadowChoiceAvailable("Underground"));
		scn.ShadowChoose("Battleground");

		// The fellowship's site, and that keyword alone.
		assertTrue(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Uruk-hai played after it asks nothing.
		scn.ShadowPlayCard(brawling);
		assertInZone(Zone.SHADOW_CHARACTERS, brawling);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The minions taken down so that the turn goes on with no skirmish; the fellowship moves on to site 4.
		scn.MoveCardsToDiscard(scout, brawling);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		var site4 = scn.GetCurrentSite();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Still the same turn: site 3 keeps the keyword, and it did not go with the fellowship.
		assertTrue(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site4, Keyword.BATTLEGROUND));

		scn.SkipToOtherPlayersTurn();
		// The next turn: gone.
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void EachOfTheOtherKeywordsMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");
		var scout3 = scn.GetShadowCard("scout3");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(scout, scout2, scout3);
		scn.FreepsPass();
		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(scout);
		scn.ShadowChoose("Mountain");
		assertTrue(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));

		scn.ShadowPlayCard(scout2);
		scn.ShadowChoose("Plains");
		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));

		scn.ShadowPlayCard(scout3);
		scn.ShadowChoose("Underground");
		assertTrue(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Each choice wrote its own "until the end of the turn": the minions taken down, the fellowship moves on
		// (the second move of the turn) and site 3 keeps all three in the Shadow phase there.
		scn.MoveCardsToDiscard(scout, scout2, scout3);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		var site4 = scn.GetCurrentSite();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		// They stayed on the site the fellowship was at; Balin's Tomb is underground by its own print.
		assertFalse(scn.HasKeyword(site4, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site4, Keyword.PLAINS));

		scn.SkipToOtherPlayersTurn();
		// The next turn: all gone.
		assertFalse(scn.HasKeyword(site3, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.UNDERGROUND));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
