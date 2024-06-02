package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.HasInitiativeModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("driven", "102_1");
					put("madman", "4_12");
					put("freca", "9_2");
					put("hillman", "15_90");

					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DrivenintotheHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Driven into the Hills
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [Dunland] minion.
		* 	While the shadow has initiative, skip the archery phase.
		* 	If the Free-Peoples player liberates a site, discard this condition. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven into the Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrivenRequiresDunlandMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var runner = scn.GetShadowCard("runner");
		var madman = scn.GetShadowCard("madman");
		scn.ShadowMoveCardToHand(driven, runner, madman);

		scn.StartGame();

		scn.SetTwilight(50);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(driven));
		scn.ShadowPlayCard(runner);
		assertFalse(scn.ShadowPlayAvailable(driven));
		scn.ShadowPlayCard(madman);
		assertTrue(scn.ShadowPlayAvailable(driven));
	}

	@Test
	public void DrivenSkipsArcheryPhaseIfShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var madman = scn.GetShadowCard("madman");
		scn.ShadowMoveCardToSupportArea(driven);
		scn.ShadowMoveCharToTable(madman);

		scn.StartGame();

		//cheating to give Shadow initiative
		scn.ApplyAdHocModifier(new HasInitiativeModifier(null, null, Side.SHADOW));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());

		scn.PassCurrentPhaseActions();

		//Skipped archery entirely
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void DrivenDoesNotSkipsArcheryPhaseIfFreepsHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var madman = scn.GetShadowCard("madman");
		scn.ShadowMoveCardToSupportArea(driven);
		scn.ShadowMoveCharToTable(madman);

		scn.StartGame();

		//cheating to give Freeps initiative, since there are too few cards in hand
		scn.ApplyAdHocModifier(new HasInitiativeModifier(null, null, Side.FREE_PEOPLE));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowHasInitiative());
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());

		scn.PassCurrentPhaseActions();

		//Did not skip archery
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
	}

	@Test
	public void DrivenSelfDiscardsWhenASiteIsLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var madman = scn.GetShadowCard("madman");
		var hillman = scn.GetShadowCard("hillman");
		var freca = scn.GetShadowCard("freca");
		scn.ShadowMoveCardToSupportArea(driven);

		var site1 = scn.GetFreepsSite(1);

		scn.StartGame();

		//Need to have sites controlled to liberate
		scn.SkipToSite(3);

		scn.ShadowMoveCharToTable(hillman, madman);
		scn.ShadowMoveCardToHand(freca);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(freca);
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.IsSiteControlled(site1));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(Zone.SUPPORT, driven.getZone());

		scn.SkipToPhase(Phase.REGROUP);

		//Rapt Hillman gives Freeps the opportunity to liberate a site,
		// and shadow can stop it (but we choose not to)
		scn.FreepsAcceptOptionalTrigger();
		scn.ShadowChooseNo();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(Zone.DISCARD, driven.getZone());
	}
}
