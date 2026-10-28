package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_299_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "7_299");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc of another title; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]); his text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcDestroyerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Destroyer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Remove (3) and spot 3 [sauron] Orcs to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("destroyer");

		assertEquals("Orc Destroyer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityRemoves3AndSpotsThreeSauronOrcsItselfAmongThemToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, chieftain, soldier);
		scn.FreepsPass();

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		// The Destroyer and two other [sauron] Orcs: no "other" in the text, so he is the third.
		assertTrue(scn.ShadowActionAvailable(destroyer));

		scn.ShadowUseCardAction(destroyer);

		assertEquals(0, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoSauronOrcsBesideASauronUrukHaiAndAWraithOrcItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, chieftain, sentry, hound);
		scn.FreepsPass();

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(destroyer));
	}

	@Test
	public void WithTwoTwilightTokensItIsNotOfferedAndWithThreeItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, chieftain, soldier);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(destroyer));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(destroyer));
	}
}
