package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_003_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("brc", "58_3");
					put("gimli", "8_5");
					put("ring", "9_7");
					put("guard1", "1_7");
					put("guard2", "1_7");

					put("runner", "1_178");
					put("troll", "1_165");
					put("swarms", "1_183");
					put("grond", "8_103");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a Dwarf who is damage +X and exert that Dwarf twice to make an opponent hinder or exert X Shadow cards. X is limited to the number of [dwarven] companions you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brc");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BloodRunsChillLoopsThroughChoicesProperly() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		scn.FreepsMoveCardToHand(brc);
		scn.FreepsMoveCharToTable(gimli);
		scn.AttachCardsTo(gimli, ring);
		scn.FreepsMoveCharToTable("guard1", "guard2");

		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var swarms = scn.GetShadowCard("swarms");
		var grond = scn.GetShadowCard("grond");
		scn.ShadowMoveCharToTable(runner, troll);
		scn.ShadowMoveCardToSupportArea(swarms, grond);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, troll.getZone());
		assertEquals(Zone.SUPPORT, swarms.getZone());
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertFalse(scn.IsHindered(runner));
		assertFalse(scn.IsHindered(troll));
		assertFalse(scn.IsHindered(swarms));
		assertFalse(scn.IsHindered(grond));

		assertTrue(scn.FreepsPlayAvailable(brc));
		scn.FreepsPlayCard(brc);
		assertEquals(2, scn.GetWoundsOn(gimli));

 		assertTrue(scn.ShadowDecisionAvailable("Choose Shadow card to hinder or exert"));
		//The following are all valid shadow cards to target
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, troll, swarms, grond));

		scn.ShadowChooseCard(grond);
		assertTrue(scn.IsHindered(grond));

		assertTrue(scn.ShadowDecisionAvailable("Choose Shadow card to hinder or exert"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, troll, swarms));
		//Grond is already hindered and should not be hinderable again
		assertFalse(scn.ShadowHasCardChoicesAvailable(grond));

		scn.ShadowChooseCard(troll);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowDecisionAvailable(""));
		scn.ShadowChoose("exert");
		assertEquals(1, scn.GetWoundsOn(troll));

		assertTrue(scn.ShadowDecisionAvailable("Choose Shadow card to hinder or exert"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, troll, swarms));

		scn.ShadowChooseCard(runner);
		assertTrue(scn.IsHindered(runner));
	}
}
