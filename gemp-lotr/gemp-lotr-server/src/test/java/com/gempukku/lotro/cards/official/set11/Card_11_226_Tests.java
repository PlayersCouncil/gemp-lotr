package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_226_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "11_226");
					// Two [wraith] characters of the Shadow player's, of two titles and races, to exert for toil.
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, vitality 3; fierce and damage +1 only
					// with 2 burdens or 2 wounds on the Ring-bearer, and there are none.
					put("nelya", "2_84");
					// Morgul Hound: [wraith] Orc, vitality 2; strength +4 and fierce only with 2 wounds on the
					// Ring-bearer.
					put("hound", "7_192");
					// Goblin Patrol Troop: a [moria] Orc with no text, vitality 3: a character of his of another culture.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Witch-king, Captain of the Nine Riders
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>. <b>Toil 2</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -2.)</helper> <b>Muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Captain of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesTwoOffHisCostForEachWraithCharacterExertedAsHeIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(witchking);
		scn.MoveMinionsToTable(nelya, hound, troop);
		scn.FreepsPass();
		// Site 3 is his site number: he is not roaming.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 8 - 2 - 2 is the least he can cost, with both [wraith] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 6, and toil 3 (or the Troop counted) would let 3 do.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(witchking));
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(witchking));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(witchking);
		// The pool is 2 short of his 8: one exertion is owed, and the second is his to make or not.
		// Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 6 - (8 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
