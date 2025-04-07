package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_026_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("grunt", "102_26");
					put("worker", "3_62");

					put("mithrandir", "6_30");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void IsengardGruntStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Isengard Grunt
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Skirmish: Exert this minion to prevent an [isengard] Orc from being overwhelmed unless its strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grunt");

		assertEquals("Isengard Grunt", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsengardGruntCanExertToPreventIsenorcOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var worker = scn.GetShadowCard("worker");
		scn.ShadowMoveCharToTable(grunt, worker);

		var mithrandir = scn.GetFreepsCard("mithrandir");
		scn.FreepsMoveCharToTable(mithrandir);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(mithrandir, worker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(mithrandir);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(grunt));
		assertTrue(scn.ShadowActionAvailable(grunt));
		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(Zone.SHADOW_CHARACTERS, worker.getZone());

		scn.PassCurrentPhaseActions();
		assertEquals(Zone.SHADOW_CHARACTERS, worker.getZone());
		assertEquals(2, scn.GetWoundsOn(worker)); //Mithrandir is damage +1
	}
}
