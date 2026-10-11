package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: Dwarf companion with no text but "To play, spot a Dwarf", so cheated in.
					// With Frodo they are three companions: room for exactly three threats.
					put("guard1", "1_7");
					put("guard2", "1_7");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.KingSites);
					put("site5", "8_119");
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
		 * Direction: Left
		 * Game Text: At the start of the regroup phase, the Free Peoples player must add 3 threats or choose an opponent who may take control of a site.
		*/

		var scn = GetScenario();
		var card = scn.GetFreepsSite(5);

		assertEquals("Crashed Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseTheFreePeoplesPlayerMayAddThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		// The fellowship has moved to Crashed Gate, and both markers have left sites 1 to 3 behind.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("8_119", scn.GetCurrentSite().getBlueprintId());
		var site1 = scn.GetSite(1);

		// Nothing is asked before the regroup phase.
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Add 3 threats"));
		assertTrue(scn.FreepsChoiceAvailable("opponent"));

		scn.FreepsChooseOption("Add 3 threats");

		assertEquals(3, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OrChooseTheOpponentWhoMayThenTakeControlOfTheLowestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToPhase(Phase.REGROUP);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals("8_119", scn.GetCurrentSite().getBlueprintId());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));

		scn.FreepsChooseOption("opponent");

		// The opponent "may": he is asked.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		assertFalse(scn.IsSiteControlled(site1));
		scn.ShadowChooseYes();

		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOpponentChosenMayLeaveTheSiteAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToPhase(Phase.REGROUP);

		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChooseOption("opponent");

		assertTrue(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		scn.ShadowChooseNo();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithRoomForOnlyTwoThreatsTheOpponentIsChosenWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		// Three companions and one threat already: only two more can be added, so "add 3 threats"
		// cannot be done in full and is not there to be chosen.
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.REGROUP);

		var site1 = scn.GetSite(1);
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));

		scn.ShadowChooseYes();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteToTakeTheOpponentMayStillBeChosenAndNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		// Sites 1 to 3, every site behind both markers, are controlled already.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Add 3 threats"));
		assertTrue(scn.FreepsChoiceAvailable("opponent"));

		scn.FreepsChooseOption("opponent");

		// There is no site for the opponent to take, and he is not asked.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(scn.GetSite(4)));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithRoomForOnlyTwoThreatsAndNoSiteToTakeNobodyIsAskedAndNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		scn.AddThreats(1);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SkipToPhase(Phase.REGROUP);

		// The opponent is chosen, since three threats do not fit, and has no site to take.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.IsSiteControlled(scn.GetSite(4)));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	@Ignore("Q-0257: with no site that can be taken, may the Free Peoples player still choose the opponent and add no threats (as built), or must the 3 threats be added?")
	public void WithNoSiteToTakeTheThreeThreatsAreAddedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SkipToPhase(Phase.REGROUP);

		// An opponent who has no site to take cannot be chosen, so the threats are all that is left.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.ShadowDecisionAvailable("Would you like to take control of a site?"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
