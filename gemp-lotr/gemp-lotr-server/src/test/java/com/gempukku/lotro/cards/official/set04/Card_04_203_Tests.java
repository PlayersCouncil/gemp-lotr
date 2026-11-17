package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_203_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "4_203");
					put("horde2", "4_203");       // a second copy in hand: each copy's discount is its own, and they do not add up
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk-hai Horde
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>The twilight cost of this minion is -1 for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde");

		assertEquals("Uruk-hai Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsMinus1ForEachSiteItsOwnerControlsAndNotForTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var horde2 = scn.GetShadowCard("horde2");
		scn.MoveCardsToHand(horde, horde2);
		assertInHand(horde, horde2);

		scn.StartGame();
		// Both markers on site 4: sites 1, 2 and 3 are behind every marker.
		scn.SkipToSite(4);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(6, scn.GetTwilightCost(horde));
		assertEquals(6, scn.GetTwilightCost(horde2));

		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetTwilightCost(horde));

		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// 6 - 1 for each of two sites, for each copy (not 6 - 4: a copy's text is about that copy alone).
		assertEquals(4, scn.GetTwilightCost(horde));
		assertEquals(4, scn.GetTwilightCost(horde2));

		// A site the Free Peoples player controls is not counted.
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(4, scn.GetTwilightCost(horde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledItIsPlayedForFourAndNotForThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		scn.MoveCardsToHand(horde);
		assertInHand(horde);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());

		// The fellowship moves to The Bridge of Khazad-dum, site 5: this minion does not roam there.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		// 6 - 2 = 4: three is one short.
		assertFalse(scn.ShadowPlayAvailable(horde));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(horde));
		scn.ShadowPlayCard(horde);
		assertInZone(Zone.SHADOW_CHARACTERS, horde);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
