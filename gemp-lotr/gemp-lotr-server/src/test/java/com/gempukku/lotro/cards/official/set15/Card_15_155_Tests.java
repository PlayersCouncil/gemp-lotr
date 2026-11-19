package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "15_155");
					put("horde2", "15_155");        // a second copy: each copy's bonus is its own, and they do not add up
					put("savage", "1_151");       // Uruk Savage: another Uruk-hai, strength 5; damage +1 is its whole text
					// Southron Explorer: strength 5, vitality 3. "Regroup: Exert this minion and remove (5) to take
					// control of a site."
					put("explorer1", "4_250");
					put("explorer2", "4_250");
				}},
				// The Towers path, Westemnet Hills as site 4 (a plains that heals mounted characters; nobody is
				// mounted). Eastemnet Gullies (site 1) and Eastfold (site 2) are the sites taken; their texts are
				// not used.
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvancingHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Advancing Horde
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>This minion is strength +1 for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde");

		assertEquals("Advancing Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus1ForEachSiteItsOwnerTakesControlOf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var horde2 = scn.GetShadowCard("horde2");
		var savage = scn.GetShadowCard("savage");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(horde, horde2, savage, explorer1, explorer2);

		// The fellowship moves to Westemnet Hills, site 4; both markers have passed sites 1 and 2.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(14, scn.GetStrength(horde));
		assertEquals(14, scn.GetStrength(horde2));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);
		assertTrue(scn.ShadowControls(site1));
		// 14 + 1 for one site.
		assertEquals(15, scn.GetStrength(horde));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// 14 + 1 for each of two sites, for each copy (not 14 + 4: a copy's text is about that copy alone); the
		// other Uruk-hai is unchanged.
		assertEquals(16, scn.GetStrength(horde));
		assertEquals(16, scn.GetStrength(horde2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// The Free Peoples player took site 1 on a turn of his as the Shadow player; this minion's owner holds site 2.
		scn.FreepsTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveMinionsToTable(horde);
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));

		// 14 + 1 for its owner's one site.
		assertEquals(15, scn.GetStrength(horde));
		scn.FreepsPass();
		assertEquals(15, scn.GetStrength(horde));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
