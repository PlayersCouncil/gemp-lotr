package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_205_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mob1", "4_205");
					put("mob2", "4_205");

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
	public void UrukhaiMobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk-hai Mob
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>The twilight cost of this minion is -1 for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mob1");

		assertEquals("Uruk-hai Mob", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsOneLessOnceItsOwnerTakesControlOfASiteAndEachCopyCountsForItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob1 = scn.GetShadowCard("mob1");
		var mob2 = scn.GetShadowCard("mob2");
		var raiders = scn.GetShadowCard("raiders");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		scn.MoveCardsToHand(mob1, mob2, raiders);
		assertInHand(mob1, mob2, raiders);
		// The fellowship moves to site 3; both markers have passed site 1.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetTwilightCost(mob1));
		assertEquals(5, scn.GetTwilightCost(mob2));
		assertEquals(4, scn.GetTwilightCost(raiders));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// 5 - 1 for the one site: a copy is lowered by its own text only, and the other minion not at all.
		assertEquals(4, scn.GetTwilightCost(mob1));
		assertEquals(4, scn.GetTwilightCost(mob2));
		assertEquals(4, scn.GetTwilightCost(raiders));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledItIsPlayedForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(mob);
		assertInHand(mob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 3 for Balin's Tomb, site 4
		assertEquals(14, scn.GetTwilight());
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// 5 - 2 for the two sites
		assertEquals(3, scn.GetTwilightCost(mob));

		scn.ShadowPlayCard(mob);

		// 14 - (5 - 2 for the two sites) - 2 for roaming at site 4
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, mob);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteOnlyTheFreePeoplesPlayerControlsDoesNotLowerTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The Free Peoples player took site 1 on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(mob);
		assertInHand(mob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 0 for Rivendell Valley, site 3
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetTwilightCost(mob));

		scn.ShadowPlayCard(mob);

		// 11 - 5 - 2 for roaming at site 3
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, mob);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
