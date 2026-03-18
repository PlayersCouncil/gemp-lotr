package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("guard", "1_7");
					put("bandit", "7_160");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "103_128");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrodruinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Orodruin
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain. Battleground. Each time a threat is added, the Free Peoples player must assign all threat wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Orodruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void ThreatsCascadeImmediatelyWhenAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bandit = scn.GetShadowCard("bandit");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(bandit);

		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Maneuver: Shadow uses Bandit's ability (exert to add 5 twilight)
		// FP may add a threat to prevent the twilight gain
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(bandit);

		// FP adds a threat to prevent the +5 twilight
		scn.FreepsChooseYes();

		// Orodruin triggers: all threats immediately cascade into wounds
		// FP must assign the 1 threat wound to a companion
		scn.FreepsChooseCard(aragorn);

		// Threat was converted to a wound on Aragorn
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AllExistingThreatsCascadeWhenNewOneAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var bandit = scn.GetShadowCard("bandit");

		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();

		// Pre-load 2 threats before arriving at Orodruin (cheating doesn't fire trigger)
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(bandit);

		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Maneuver: Shadow uses Bandit, FP adds threat to prevent
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(bandit);
		scn.FreepsChooseYes();

		// 2 pre-existing + 1 new = 3 threats total, all cascade into wounds
		// FP assigns 3 wounds to companions
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);

		// All threats converted to wounds
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(aragorn));
	}
}
