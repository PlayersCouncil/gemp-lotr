package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march1", "103_22");
					put("march2", "103_22"); // Second copy for Saruman's Power test
					put("kingdead", "8_38"); // Gondor Wraith
					put("deadman", "10_27"); // Gondor Wraith
					put("aragorn", "1_89");

					put("power", "51_136"); // Saruman's Power - discards 1 condition, hinders rest
					put("savage", "1_151"); // Uruk Savage - Isengard minion to exert for Power
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EtherealMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ethereal March
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 2 [gondor] Wraiths, the threat limit is +2.
		* 	While you can spot more threats than companions, each Wraith is strength +1.
		* 	Each time this condition is hindered or discarded, remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("march1");

		assertEquals("Ethereal March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void EtherealMarchIncreasesThreatLimitWith2Wraiths() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var kingdead = scn.GetFreepsCard("kingdead");
		var deadman = scn.GetFreepsCard("deadman");

		scn.MoveCompanionsToTable(kingdead, deadman);
		scn.MoveCardsToSupportArea(march);

		scn.StartGame();

		// 3 companions (Frodo + King + Dead Man), base limit = 3, with 2 Wraiths = 5
		assertEquals(5, scn.GetThreatLimit());
	}

	@Test
	public void EtherealMarchDoesNotIncreaseThreatLimitWithOnly1Wraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var kingdead = scn.GetFreepsCard("kingdead");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(kingdead, aragorn);
		scn.MoveCardsToSupportArea(march);

		scn.StartGame();

		// 3 companions (Frodo + King + Aragorn), only 1 Wraith, base limit = 3
		assertEquals(3, scn.GetThreatLimit());
	}

	@Test
	public void EtherealMarchGivesWraithsStrengthWhenThreatsGreaterThanCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var kingdead = scn.GetFreepsCard("kingdead");
		var deadman = scn.GetFreepsCard("deadman");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(kingdead, deadman);
		scn.MoveCardsToSupportArea(march);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		int kingBaseStrength = scn.GetStrength(kingdead);
		int deadmanBaseStrength = scn.GetStrength(deadman);

		// 3 companions, 0 threats - no bonus
		assertEquals(0, scn.GetThreats());
		assertEquals(kingBaseStrength, scn.GetStrength(kingdead));
		assertEquals(deadmanBaseStrength, scn.GetStrength(deadman));

		// Add 3 threats (equal to companions) - still no bonus
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertEquals(kingBaseStrength, scn.GetStrength(kingdead));
		assertEquals(deadmanBaseStrength, scn.GetStrength(deadman));

		// Add 1 more threat (4 threats > 3 companions) - now +1 strength
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(kingBaseStrength + 1, scn.GetStrength(kingdead));
		assertEquals(deadmanBaseStrength + 1, scn.GetStrength(deadman));
	}

	@Test
	public void EtherealMarchRemovesThreatWhenDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToSupportArea(march);
		scn.MoveCardsToHand(power);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.FreepsPass();

		// Play Saruman's Power (exert savage, discard a condition, hinder rest)
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		// March is discarded
		assertInZone(Zone.DISCARD, march);

		// Should have removed 1 threat
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void EtherealMarchRemovesThreatWhenHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var march1 = scn.GetFreepsCard("march1");
		var march2 = scn.GetFreepsCard("march2");
		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToSupportArea(march1, march2);
		scn.MoveCardsToHand(power);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());

		scn.FreepsPass();

		// Play Saruman's Power (exert savage, discard a condition, hinder rest)
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		// Choose march1 to discard
		scn.ShadowChooseCard(march1);

		assertInZone(Zone.DISCARD, march1);
		assertTrue(scn.IsHindered(march2));

		// Both triggers should fire: 1 for discard, 1 for hinder = -2 threats
		assertEquals(1, scn.GetThreats());
	}


}
