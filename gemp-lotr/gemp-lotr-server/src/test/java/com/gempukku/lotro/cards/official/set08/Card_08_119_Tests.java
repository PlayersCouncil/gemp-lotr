package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_310");
					put("pippin", "4_314");
					put("sam", "1_311");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "8_119"); //
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrashedGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Crashed Gate
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 7
		 * Type: Site
		 * Subtype: 
		 * Site Number: 5K
		 * Game Text: At the start of the regroup phase, the Free Peoples player must add 3 threats or choose an opponent who may take control of a site.
		*/

		var scn = GetScenario();
		var card = scn.GetFreepsSite(5);

		assertEquals("Crashed Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CrashedGateOffers3ThreatsChoiceWhenBothAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		//Threat limit is now 4
		scn.MoveCompanionsToTable("sam", "merry", "pippin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(2, scn.FreepsGetChoiceCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));

		scn.FreepsChooseOption("threat");
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void CrashedGateOffersSiteControlChoiceWhenBothAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		//Threat limit is now 4
		scn.MoveCompanionsToTable("sam", "merry", "pippin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(2, scn.FreepsGetChoiceCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));

		scn.FreepsChooseOption("opponent");
		assertTrue(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsSiteControlled(site1));
	}

	@Test
	public void CrashedGateOffersOpponentChoiceWhenSiteControlNotAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		//Threat limit is now 4
		scn.MoveCompanionsToTable("sam", "merry", "pippin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.FreepsGetChoiceCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));

		scn.FreepsChooseOption("opponent");
		assertFalse(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));

		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void CrashedGateChoosesOpponentChoiceAutomaticallyWhen3ThreatsCantBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		//Threat limit is now 4
		scn.MoveCompanionsToTable("sam", "merry", "pippin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPassCurrentPhaseAction();

		//Only 2 threat slots left
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));

	}

	@Test
	public void CrashedGateChoosesOpponentChoiceAutomaticallyWhen3ThreatsCantBeAddedAndNoSiteAvailableToControl() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		//Threat limit is now 4
		scn.MoveCompanionsToTable("sam", "merry", "pippin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPassCurrentPhaseAction();

		//Only 2 threat slots left
		scn.AddThreats(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));

		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}
}
