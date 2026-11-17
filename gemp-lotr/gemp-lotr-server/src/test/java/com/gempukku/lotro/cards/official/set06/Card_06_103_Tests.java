package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gatetroll", "6_103");
					// Morgul Hunter: [sauron] Orc, cost 5, strength 9 +1 for each companion you can spot
					put("hunter", "1_256");
					put("hunter2", "1_256");
					// Troll of Cirith Gorgor: [sauron] Troll, cost 7, site 6; to play, spot a [sauron] minion; a strength
					// bonus for possessions only (none is in play)
					put("troll", "10_101");
					// Beastly Olog-hai: [orc] Troll; its ability is an assignment ability, used only on purpose
					put("olog", "11_108");
					// Goblin Patrol Troop: [moria] Orc, no text
					put("troop", "1_177");
					// The Number Must Be Few: [sauron] support-area condition; nothing below 7 companions
					put("few", "1_260");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gate Troll
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 13
		 * Vitality: 4
		 * Site Number: 6
		 * Game Text: <b>Fierce</b>.<br>While you can spot a Troll, Gate Troll's twilight cost is -4.<br>For each other [sauron] minion you can spot, this minion is strength +1.
		*/

		var scn = GetScenario();

		var gatetroll = scn.GetFreepsCard("gatetroll");

		assertEquals("Gate Troll", gatetroll.getBlueprint().getTitle());
		assertNull(gatetroll.getBlueprint().getSubtitle());
		assertTrue(gatetroll.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gatetroll.getBlueprint().getSide());
		assertEquals(Culture.SAURON, gatetroll.getBlueprint().getCulture());
		assertEquals(CardType.MINION, gatetroll.getBlueprint().getCardType());
		assertEquals(Race.TROLL, gatetroll.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(gatetroll, Keyword.FIERCE));
		assertEquals(8, gatetroll.getBlueprint().getTwilightCost());
		assertEquals(13, gatetroll.getBlueprint().getStrength());
		assertEquals(4, gatetroll.getBlueprint().getVitality());
		assertEquals(6, gatetroll.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsFourLessOnceATrollIsPlayedAndAnOrcDoesNotLowerIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gatetroll = scn.GetShadowCard("gatetroll");
		var hunter = scn.GetShadowCard("hunter");
		var hunter2 = scn.GetShadowCard("hunter2");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(gatetroll, hunter2, troll);
		assertInHand(gatetroll, hunter2, troll);

		scn.StartGame();
		scn.MoveMinionsToTable(hunter);
		// 7 + 2 roaming for Troll of Cirith Gorgor at site 2, and 8 - 4 + 2 roaming for Gate Troll after it: 15, of
		// which the move to Ettenmoors adds 3 (its Shadow number 2, and 1 for Frodo).
		scn.SetTwilight(12);
		scn.FreepsPass();
		assertEquals(15, scn.GetTwilight());

		// A [sauron] Orc, no Troll: the printed 8; the other minion in hand at its own 5.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(8, scn.GetTwilightCost(gatetroll));
		assertEquals(5, scn.GetTwilightCost(hunter2));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(6, scn.GetTwilight());

		// A Troll in play: 8 - 4; the other minion in hand is as it was.
		assertEquals(4, scn.GetTwilightCost(gatetroll));
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertTrue(scn.ShadowPlayAvailable(gatetroll));
		scn.ShadowPlayCard(gatetroll);

		// 6 - (8 - 4 + 2 roaming)
		assertInZone(Zone.SHADOW_CHARACTERS, gatetroll);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcTrollAloneMakesItsTwilightCostFourLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gatetroll = scn.GetShadowCard("gatetroll");
		var hunter2 = scn.GetShadowCard("hunter2");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCardsToHand(gatetroll, hunter2);
		assertInHand(gatetroll, hunter2);

		scn.StartGame();
		assertEquals(8, scn.GetTwilightCost(gatetroll));
		scn.MoveMinionsToTable(olog);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilightCost(gatetroll));
		assertEquals(5, scn.GetTwilightCost(hunter2));
	}

	@Test
	public void StrengthPlusOneForEachOtherSauronMinionYouCanSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gatetroll = scn.GetShadowCard("gatetroll");
		var hunter = scn.GetShadowCard("hunter");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var few = scn.GetShadowCard("few");
		scn.MoveCardsToHand(hunter);
		scn.MoveCardsToSupportArea(few);
		assertInHand(hunter);
		assertInZone(Zone.SUPPORT, few);

		scn.StartGame();
		scn.MoveMinionsToTable(gatetroll, troop);
		// Morgul Hunter: 5 + 2 roaming at site 2, with the 3 the move adds.
		scn.SetTwilight(4);
		scn.FreepsPass();

		// Itself, a [moria] Orc and a [sauron] condition: no other [sauron] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(13, scn.GetStrength(gatetroll));

		// A [sauron] Orc played: one.
		scn.ShadowPlayCard(hunter);
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertEquals(14, scn.GetStrength(gatetroll));
		// Morgul Hunter: 9 + 1 for Frodo, the one companion.
		assertEquals(10, scn.GetStrength(hunter));

		// A [sauron] Troll as well: two.
		scn.MoveMinionsToTable(troll);
		assertEquals(15, scn.GetStrength(gatetroll));
		assertEquals(10, scn.GetStrength(hunter));
		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
