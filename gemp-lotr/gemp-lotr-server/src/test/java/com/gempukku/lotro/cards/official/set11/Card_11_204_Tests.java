package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_204_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tyrannical", "11_204");
					// Two [uruk-hai] characters of the Shadow player's, of two races, to exert for toil.
					// Murderous Uruk: [uruk-hai] Uruk-hai, vitality 2; damage +1 and muster (a regroup trigger) only.
					put("murderous", "11_195");
					// Saruman, Master of the White Hand: [uruk-hai] Wizard, vitality 4; each companion is resistance -1,
					// and a skirmish ability.
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai with damage +1 alone: an Uruk-hai of another culture.
					put("guard", "10_81");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TyrannicalUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Tyrannical Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Toil 1</b>. <helper>(For each [uruk-hai] character you exert when playing this, its twilight cost is -1.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tyrannical");

		assertEquals("Tyrannical Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesOneOffItsCostForEachUrukHaiCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tyrannical = scn.GetShadowCard("tyrannical");
		var murderous = scn.GetShadowCard("murderous");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(tyrannical);
		scn.MoveMinionsToTable(murderous, saruman, guard);
		scn.FreepsPass();

		// Site 5 is its site number: it is not roaming.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(tyrannical);

		// 6 - 1 - 1 is the least it can cost, with both [uruk-hai] characters exerted; the [sauron] Guard does not
		// count. Toil 0 would want 6, and toil 2 (or the Guard counted) would let 3 do.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(tyrannical));
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(tyrannical));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowPlayCard(tyrannical);

		// The pool is 1 short of its 6: one exertion is owed, and the second is the player's to make or not.
		// Toil 0 would leave it unplayable; toil 2 would owe none.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(murderous, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		scn.ShadowChooseCard(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, tyrannical);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertEquals(0, scn.GetWoundsOn(guard));
		// 5 - (6 - 1)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
