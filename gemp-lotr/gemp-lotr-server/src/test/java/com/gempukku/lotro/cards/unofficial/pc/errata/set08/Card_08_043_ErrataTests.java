package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_043_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("host", "58_43");
					put("wraith1", "8_41");
					put("wraith2", "8_41");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ShadowHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 8
		* Title: Shadow Host
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Gondor
		* Twilight Cost: 5
		* Type: companion
		* Subtype: Wraith
		* Strength: 9
		* Vitality: 3
		* Signet: Aragorn
		* Game Text: <b>Enduring.</b> To play, spot 2 [gondor] Wraiths and add 2 threats. 
		* 	While Shadow Host is exhausted, it is defender +1.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");

		assertTrue(host.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, host.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, host.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, host.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, host.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(host, Keyword.ENDURING));
		assertEquals(5, host.getBlueprint().getTwilightCost());
		assertEquals(9, host.getBlueprint().getStrength());
		assertEquals(3, host.getBlueprint().getVitality());
		assertEquals(6, host.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, host.getBlueprint().getSignet());
	}

	@Test
	public void HostRequires2GondorWraithsAnd2ThreatsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var wraith1 = scn.GetFreepsCard("wraith1");
		var wraith2 = scn.GetFreepsCard("wraith2");
		scn.FreepsMoveCardToHand(host, wraith1, wraith2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(host));
		scn.FreepsPlayCard(wraith1);
		scn.RemoveThreats(1);
		assertFalse(scn.FreepsPlayAvailable(host));
		scn.FreepsPlayCard(wraith2);
		scn.RemoveThreats(1);
		assertTrue(scn.FreepsPlayAvailable(host));

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(wraith1));
		assertEquals(0, scn.GetWoundsOn(wraith2));
		scn.FreepsPlayCard(host);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(wraith1));
		assertEquals(0, scn.GetWoundsOn(wraith2));
	}

	@Test
	public void HostIsDefenderPlus1IfExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		scn.FreepsMoveCharToTable(host);

		scn.StartGame();

		assertFalse(scn.hasKeyword(host, Keyword.DEFENDER));
		scn.AddWoundsToChar(host, 1);
		assertFalse(scn.hasKeyword(host, Keyword.DEFENDER));
		scn.AddWoundsToChar(host, 1);
		assertTrue(scn.hasKeyword(host, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(host, Keyword.DEFENDER));
	}
}
