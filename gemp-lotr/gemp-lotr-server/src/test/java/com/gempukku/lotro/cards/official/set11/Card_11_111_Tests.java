package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "11_111");

					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, vitality 2; +2 strength at an underground site, which no reading here takes
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, vitality 2; its assignment trigger is never reached
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChampionOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Champion Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 13
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Toil 2</b>. <helper>(For each [orc] character you exert when playing this, its twilight cost is -2.)</helper>
		*/

		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");

		assertEquals("Champion Orc", champion.getBlueprint().getTitle());
		assertNull(champion.getBlueprint().getSubtitle());
		assertFalse(champion.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, champion.getBlueprint().getSide());
		assertEquals(Culture.ORC, champion.getBlueprint().getCulture());
		assertEquals(CardType.MINION, champion.getBlueprint().getCardType());
		assertEquals(Race.ORC, champion.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(champion, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(champion, Keyword.TOIL));
		assertEquals(5, champion.getBlueprint().getTwilightCost());
		assertEquals(13, champion.getBlueprint().getStrength());
		assertEquals(2, champion.getBlueprint().getVitality());
		assertEquals(4, champion.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachOrcCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetShadowCard("champion");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(champion);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertInHand(champion);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();
		// Site 4 is its site number: it is not roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 5 - 2 - 2 is the least it can cost, with both [orc] characters exerted; the [moria] Troop does not count.
		// Toil 1 would want 3, and toil 3 (or the Troop counted) would let 0 do.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(champion));
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(champion));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(champion);
		// The pool is 2 short of its 5: one exertion is owed, and the second is the Shadow player's to make or
		// not.  Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, champion);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 3 - (5 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
