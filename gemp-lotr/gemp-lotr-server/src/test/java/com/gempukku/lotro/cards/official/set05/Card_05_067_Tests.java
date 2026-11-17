package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "5_67");
					put("rider2", "5_67");
					put("warwarg", "5_64");     // War-warg: [isengard] mount on a warg-rider, strength +4, vitality +2; a skirmish Response
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WargriderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Warg-rider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While this minion is not exhausted, he is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider");

		assertEquals("Warg-rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExhaustedHeIsNotFierceUntilAWarWargPlayedOnHimLeavesHimMoreThanOneVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var warwarg = scn.GetShadowCard("warwarg");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, rider2, troop);
		// Vitality 2 and no wound: 2 left, one above exhausted, and fierce.
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(2, scn.GetVitality(rider));
		assertFalse(scn.IsExhausted(rider));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		scn.AddWoundsToChar(rider, 1);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));
		scn.FreepsPass();

		// Vitality 2 with 1 wound: exhausted, and not fierce.  The other Warg-rider goes by his own state, no wound
		// and fierce; the Troop, not exhausted, is not fierce either.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertTrue(scn.IsExhausted(rider));
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertFalse(scn.IsExhausted(rider2));
		assertTrue(scn.HasKeyword(rider2, Keyword.FIERCE));
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// Both Warg-riders are warg-riders the War-warg could go on.
		scn.ShadowPlayCard(warwarg);
		scn.ShadowChooseCard(rider);
		assertAttachedTo(warwarg, rider);

		// 2 + 2 less 1 wound: 3 left, not exhausted, fierce.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(3, scn.GetVitality(rider));
		assertFalse(scn.IsExhausted(rider));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
