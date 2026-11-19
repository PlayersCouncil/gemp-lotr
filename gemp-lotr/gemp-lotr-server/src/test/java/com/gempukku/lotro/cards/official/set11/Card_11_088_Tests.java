package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "11_88");

					// Axeman of Harad: a [men] Man minion, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, vitality 4, site 2.  His
					// maneuver and regroup abilities are never used.
					put("ted", "18_75");
					// Goblin Patrol Troop: a [moria] minion with no text, vitality 3: one who could exert, of another culture.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegionofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Legion of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legion");

		assertEquals("Legion of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(axeman, ted, troop);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();
		// Site 4 is its site number: it is not roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 6 - 2 - 2 is the least it can cost, with both [men] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 4, and toil 3 (or the Troop counted) would let 1 do.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(legion));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(legion);
		// The pool is 2 short of its 6: one exertion is owed, and the second is the Shadow player's to make or
		// not.  Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 4 - (6 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
