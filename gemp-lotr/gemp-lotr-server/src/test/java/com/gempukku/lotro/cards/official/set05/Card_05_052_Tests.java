package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flanker", "5_52");
					put("flanker2", "5_52");
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
	public void IsengardFlankerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Isengard Flanker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While this minion is not exhausted, he is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flanker");

		assertEquals("Isengard Flanker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExhaustedHeIsNotFierceUntilAWarWargPlayedOnHimLeavesHimMoreThanOneVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flanker = scn.GetShadowCard("flanker");
		var flanker2 = scn.GetShadowCard("flanker2");
		var warwarg = scn.GetShadowCard("warwarg");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(flanker, flanker2, troop);
		// Unwounded he has vitality 2, one above exhausted, and is fierce.
		assertEquals(0, scn.GetWoundsOn(flanker));
		assertEquals(2, scn.GetVitality(flanker));
		assertFalse(scn.IsExhausted(flanker));
		assertTrue(scn.HasKeyword(flanker, Keyword.FIERCE));
		scn.AddWoundsToChar(flanker, 1);
		assertEquals(1, scn.GetWoundsOn(flanker));
		assertEquals(0, scn.GetWoundsOn(flanker2));
		scn.FreepsPass();

		// Vitality 2 with 1 wound: exhausted, and not fierce.  The other Flanker goes by his own state, unwounded
		// and fierce; the Troop, not exhausted, is not fierce either.
		assertEquals(1, scn.GetWoundsOn(flanker));
		assertTrue(scn.IsExhausted(flanker));
		assertFalse(scn.HasKeyword(flanker, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(flanker2));
		assertTrue(scn.HasKeyword(flanker2, Keyword.FIERCE));
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// Both Flankers are warg-riders the War-warg could go on.
		scn.ShadowPlayCard(warwarg);
		scn.ShadowChooseCard(flanker);
		assertAttachedTo(warwarg, flanker);

		// 2 + 2 less 1 wound: 3 left, not exhausted, fierce.
		assertEquals(1, scn.GetWoundsOn(flanker));
		assertEquals(3, scn.GetVitality(flanker));
		assertFalse(scn.IsExhausted(flanker));
		assertTrue(scn.HasKeyword(flanker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(flanker2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
