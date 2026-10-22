package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "10_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolSentryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Sentry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>.
		*/

		var scn = GetScenario();

		var sentry = scn.GetFreepsCard("sentry");

		assertEquals("Cirith Ungol Sentry", sentry.getBlueprint().getTitle());
		assertNull(sentry.getBlueprint().getSubtitle());
		assertFalse(sentry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentry.getBlueprint().getSide());
		assertEquals(Culture.SAURON, sentry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentry.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, sentry.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(2, sentry.getBlueprint().getTwilightCost());
		assertEquals(8, sentry.getBlueprint().getStrength());
		assertEquals(2, sentry.getBlueprint().getVitality());
		assertEquals(6, sentry.getBlueprint().getSiteNumber());
	}
}
