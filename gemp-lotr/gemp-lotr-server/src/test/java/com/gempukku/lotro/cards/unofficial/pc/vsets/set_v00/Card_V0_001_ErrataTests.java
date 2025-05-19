package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V0_001_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("dummy", "1_311");
				}},
				new HashMap<>() {{
					put("East Road", "11_236");
					put("Ettenmoors", "11_237");
					put("Rath Dínen", "100_1");
					put("site4", "11_238");
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing,
				"open"
		);
	}

	@Test
	public void RathDinenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V0
		* Title: Rath Dinen
		* Side: Site
		* Culture: 
		* Twilight Cost: 0
		* Type: site
		* Subtype: Standard
		* Game Text: When sanctuary healing at this site, the Free Peoples player heals only 3 wounds instead of 5.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl rath = scn.GetFreepsSite("Rath Dínen");

		assertFalse(rath.getBlueprint().isUnique());
		assertEquals(CardType.SITE, rath.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, rath.getBlueprint().getSiteBlock());
		assertEquals(0, rath.getBlueprint().getTwilightCost());
	}

	@Test
	public void SantuaryHealingOnlyHeals3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl site1 = scn.GetFreepsSite("East Road");
		PhysicalCardImpl site2 = scn.GetShadowSite("Ettenmoors");
		PhysicalCardImpl rath = scn.GetShadowSite("Rath Dínen");

		scn.AddWoundsToChar(frodo, 5);

		scn.StartGame(site1);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(site2);
		scn.SkipCurrentSite();
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(rath);
		scn.SkipCurrentSite();

		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 3"));

	}
}
