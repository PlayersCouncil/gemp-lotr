package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("party", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiRaidingPartyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Raiding Party
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
		*/

		var scn = GetScenario();

		var party = scn.GetFreepsCard("party");

		assertEquals("Uruk-hai Raiding Party", party.getBlueprint().getTitle());
		assertNull(party.getBlueprint().getSubtitle());
		assertFalse(party.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, party.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, party.getBlueprint().getCulture());
		assertEquals(CardType.MINION, party.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, party.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(4, party.getBlueprint().getTwilightCost());
		assertEquals(9, party.getBlueprint().getStrength());
		assertEquals(3, party.getBlueprint().getVitality());
		assertEquals(5, party.getBlueprint().getSiteNumber());
	}
}
