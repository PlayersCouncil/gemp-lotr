package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_006_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: board filler, wound target for sanctuary healing
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_6"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_6
		 * Type: MetaSite
		 * Game Text: Site 8 is a sanctuary.
		 */

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
	}
}
