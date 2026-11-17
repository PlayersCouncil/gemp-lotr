package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "17_129");
					// Companions beside Frodo, so that the threats can be there: no more threats than companions.
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability and a response to Frodo's death only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandLegionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Legion
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 17
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>To play, remove 2 threats.
		*/

		var scn = GetScenario();

		var legion = scn.GetFreepsCard("legion");

		assertEquals("White Hand Legion", legion.getBlueprint().getTitle());
		assertNull(legion.getBlueprint().getSubtitle());
		assertFalse(legion.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, legion.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, legion.getBlueprint().getCulture());
		assertEquals(CardType.MINION, legion.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, legion.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(legion, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertEquals(6, legion.getBlueprint().getTwilightCost());
		assertEquals(17, legion.getBlueprint().getStrength());
		assertEquals(3, legion.getBlueprint().getVitality());
		assertEquals(5, legion.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeThreatsItIsPlayedByRemovingTwoOfThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToHand(legion);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.HAND, legion);
		// Enough for its cost and the roaming penalty at site 2; the pool is not what this test reads.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.HAND, legion);
		assertTrue(scn.ShadowPlayAvailable(legion));

		scn.ShadowPlayCard(legion);

		// Two removed, not every threat there was.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneThreatUntilASecondIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToHand(legion);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, legion);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, legion);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(legion));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(legion));

		scn.ShadowPlayCard(legion);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
