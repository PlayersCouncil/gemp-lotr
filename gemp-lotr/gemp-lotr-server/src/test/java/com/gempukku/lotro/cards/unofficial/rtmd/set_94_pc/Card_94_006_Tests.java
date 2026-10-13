package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_006_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: board filler, wound target for sanctuary healing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: board filler, wound target for sanctuary healing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_6"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: board filler, wound target for sanctuary healing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_6
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Site 8 is a sanctuary.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_6", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void SiteEightBecomesSanctuaryWhileOtherSitesAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.StartGame();

		scn.SkipToSite(7);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));

		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));

		// The fellowship phase is passed and the fellowship moves on to site 9, the Shadow phase there.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutTheModSiteEightIsNotASanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		scn.StartGame();
		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
	}

	@Test
	public void SiteEightIsStillASanctuaryWhenTheOpponentOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// "Site 8 is a sanctuary" has no "you/your" - it affects the board regardless of who owns
		// this copy of the metasite, so it must also work when Shadow owns it.
		var scn = GetShadowScenario();
		scn.StartGame();
		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
	}

	@Test
	public void SanctuaryHealingIsOfferedToAWoundedCompanionAtSiteEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 1);
		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));

		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Nobody is left to heal, so the four heals that remain are not asked about.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
