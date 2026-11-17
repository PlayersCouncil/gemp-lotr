package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_204_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder1", "4_204");
					put("marauder2", "4_204");

					// Uruk-hai Raiding Party: [isengard] Uruk-hai, twilight 4, damage +1 and no other text: another
					// minion in the same hand, whose cost nothing changes.
					put("raiders", "4_207");
					// Southron Explorer: strength 5, vitality 3. "Regroup: Exert this minion and remove (5) to take
					// control of a site."
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk-hai Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>The twilight cost of this minion is -1 for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder1");

		assertEquals("Uruk-hai Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
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
	public void CostsOneLessOnceItsOwnerTakesControlOfASiteAndEachCopyCountsForItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder1 = scn.GetShadowCard("marauder1");
		var marauder2 = scn.GetShadowCard("marauder2");
		var raiders = scn.GetShadowCard("raiders");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		scn.MoveCardsToHand(marauder1, marauder2, raiders);
		assertInHand(marauder1, marauder2, raiders);
		// The fellowship moves to site 3; both markers have passed site 1.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetTwilightCost(marauder1));
		assertEquals(3, scn.GetTwilightCost(marauder2));
		assertEquals(4, scn.GetTwilightCost(raiders));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// 3 - 1 for the one site: a copy is lowered by its own text only, and the other minion not at all.
		assertEquals(2, scn.GetTwilightCost(marauder1));
		assertEquals(2, scn.GetTwilightCost(marauder2));
		assertEquals(4, scn.GetTwilightCost(raiders));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledItIsPlayedForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(marauder);
		assertInHand(marauder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 3 for Balin's Tomb, site 4
		assertEquals(14, scn.GetTwilight());
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// 3 - 2 for the two sites
		assertEquals(1, scn.GetTwilightCost(marauder));

		scn.ShadowPlayCard(marauder);

		// 14 - (3 - 2 for the two sites) - 2 for roaming at site 4
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteOnlyTheFreePeoplesPlayerControlsDoesNotLowerTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder1");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The Free Peoples player took site 1 on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(marauder);
		assertInHand(marauder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 0 for Rivendell Valley, site 3
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetTwilightCost(marauder));

		scn.ShadowPlayCard(marauder);

		// 11 - 3 - 2 for roaming at site 3
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
