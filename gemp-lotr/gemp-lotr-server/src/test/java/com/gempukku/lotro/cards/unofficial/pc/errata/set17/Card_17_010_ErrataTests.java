package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_17_010_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("namarie", "67_10");
					put("legolas", "15_19");
					put("gimli", "18_1");
					put("aragorn", "15_55");

					put("mauhur", "15_164");
					put("bladetip", "1_209");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NamarieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 17
		* Title: Namarie
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: When you play this, add an [elven] token here for each hunter you can spot (limit 3).
		* 	<b>Maneuver:</b> Discard this condition (or remove a token from here) and discard a card from hand to discard a condition.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");

		assertTrue(namarie.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, namarie.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, namarie.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, namarie.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(namarie, Keyword.SUPPORT_AREA));
		assertEquals(2, namarie.getBlueprint().getTwilightCost());
	}

	@Test
	public void NamarieLimitedTo3TokensOnPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		scn.FreepsMoveCardToHand(namarie);
		scn.FreepsMoveCharToTable("legolas", "gimli", "aragorn");

		scn.ShadowMoveCharToTable("mauhur");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(namarie));
		scn.FreepsPlayCard(namarie);

		//Capped at 3
		assertEquals(3, scn.GetCultureTokensOn(namarie));
	}

	@Test
	public void NamarieSpotsShadowHunter() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		scn.FreepsMoveCardToHand(namarie);
		scn.FreepsMoveCharToTable("legolas");

		scn.ShadowMoveCharToTable("mauhur");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(namarie));
		scn.FreepsPlayCard(namarie);

		//Capped at 3
		assertEquals(2, scn.GetCultureTokensOn(namarie));
	}

	@Test
	public void ManeuverActionRemovesTokenIfAvailableToDiscardCondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.FreepsMoveCardToHand(namarie, gimli, legolas);

		var bladetip = scn.GetShadowCard("bladetip");
		scn.ShadowMoveCardToSupportArea(bladetip);
		scn.ShadowMoveCharToTable("mauhur");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(namarie));
		scn.FreepsPlayCard(namarie);

		assertEquals(1, scn.GetCultureTokensOn(namarie));
		assertEquals(Zone.SUPPORT, namarie.getZone());
		assertEquals(Zone.SUPPORT, bladetip.getZone());
		assertEquals(Zone.HAND, gimli.getZone());
		assertEquals(Zone.HAND, legolas.getZone());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		//Card to discard from hand
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(gimli);
		assertEquals(Zone.DISCARD, gimli.getZone());
		assertEquals(Zone.HAND, legolas.getZone());
		assertEquals(Zone.SUPPORT, namarie.getZone());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());

		//Condition to discard from play
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(bladetip);
		assertEquals(Zone.SUPPORT, namarie.getZone());
		assertEquals(Zone.DISCARD, bladetip.getZone());

	}

	@Test
	public void ManeuverActionSelfDiscardsIfNoTokensAvailableToDiscardCondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.FreepsMoveCardToHand(gimli, legolas);
		scn.FreepsMoveCardToSupportArea(namarie);

		var bladetip = scn.GetShadowCard("bladetip");
		scn.ShadowMoveCardToSupportArea(bladetip);
		scn.ShadowMoveCharToTable("mauhur");

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertEquals(Zone.SUPPORT, namarie.getZone());
		assertEquals(Zone.SUPPORT, bladetip.getZone());
		assertEquals(Zone.HAND, gimli.getZone());
		assertEquals(Zone.HAND, legolas.getZone());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		//Card to discard from hand
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(gimli);
		assertEquals(Zone.DISCARD, gimli.getZone());
		assertEquals(Zone.HAND, legolas.getZone());
		assertEquals(Zone.DISCARD, namarie.getZone());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());

		//Condition to discard from play; only blade tip remains
		assertEquals(Zone.DISCARD, namarie.getZone());
		assertEquals(Zone.DISCARD, bladetip.getZone());

	}
}
