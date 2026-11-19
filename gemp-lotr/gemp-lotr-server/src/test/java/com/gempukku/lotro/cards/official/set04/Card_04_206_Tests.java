package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_206_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol1", "4_206");
					put("patrol2", "4_206");

					// Uruk-hai Raiding Party: [isengard] Uruk-hai, damage +1 and no other text: another minion with
					// the same printed bonus, which no site changes.
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
	public void UrukhaiPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk-hai Patrol
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you control a site, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol1");

		assertEquals("Uruk-hai Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1MoreOnceItsOwnerTakesControlOfASiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var raiders = scn.GetShadowCard("raiders");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		// The fellowship moves to site 3; both markers have passed site 1.
		scn.SkipToPhase(Phase.REGROUP);
		// Laid at the start of the regroup phase, where minions that lived through the skirmishes stand.
		scn.MoveMinionsToTable(patrol1, patrol2, raiders);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetKeywordCount(patrol1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(patrol2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// 1 printed + 1 while a site is controlled: each copy gives itself the bonus once, and the other minion
		// gets none.
		assertEquals(2, scn.GetKeywordCount(patrol1, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(patrol2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASiteOnlyTheFreePeoplesPlayerControlsGivesNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol1");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The Free Peoples player took site 1 on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.MoveMinionsToTable(patrol);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// The printed 1 alone.
		assertEquals(1, scn.GetKeywordCount(patrol, Keyword.DAMAGE));

		scn.FreepsPass();
		assertEquals(1, scn.GetKeywordCount(patrol, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
