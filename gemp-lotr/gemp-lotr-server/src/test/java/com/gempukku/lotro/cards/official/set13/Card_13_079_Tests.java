package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pallando", "13_79");
					put("throne", "17_39");
					put("saruman", "4_173");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PallandoDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Pallando Deceived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of the maneuver phase, you may exert a Wizard to make this condition a <b>fierce</b> Wizard minion until the start of the regroup phase that has 10 strength and 1 vitality, and cannot take wounds or bear other cards. This card is still a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pallando");

		assertEquals("Pallando Deceived", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PallandoDeceivedIsPumpedByThroneOfIsengardAfterConversion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var throne = scn.GetShadowCard("throne");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(pallando, throne);
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		//10 base +3 from throne making Wizards stronger
		assertEquals(13, scn.GetStrength(pallando));
	}
}
