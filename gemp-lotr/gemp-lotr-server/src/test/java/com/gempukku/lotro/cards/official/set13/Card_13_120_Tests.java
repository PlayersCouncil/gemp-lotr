package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_120_Tests
{

	// The card's own block: the Shadow player lays each next site, and holds the sites not laid to replace one with.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depths1", "13_120");
					put("depths2", "13_120");

					put("denizen", "11_115");    // Denizen of Khazad-dûm: an [orc] Orc; strength +2 at an underground site, and nothing more
					put("moria", "11_116");      // Denizen of Moria: an [orc] Orc of another title; strength +3 at an underground site
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is never reached
					put("halftroll", "14_12");
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion with no text
					// The Beckoning Shadow: an [orc] condition (its play cost is passed over by the cheat); its text needs 5 burdens
					put("beckoning", "12_83");
				}},
				new HashMap<>()
				{{
					// Abandoned Mine Shaft: Underground; adds a burden only when left in the regroup phase outside region 1
					put("shaft", "13_185");
					put("ruins", "18_138");      // Sirannon Ruins: River, and no text
					// Moria Stairway: Underground; a hand weapon's bearer is damage +1, and none is laid
					put("stairway", "11_248");
					put("bridge", "12_186");     // The Bridge of Khazad-dûm: Battleground, Underground; The Balrog's cost only
					put("road", "11_236");       // East Road: Forest; each companion is twilight cost +2, and none is played
					// Never laid in these tests, and never named: four copies of Expanding Marshland.
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void UnforgivingDepthsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Unforgiving Depths
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 3 underground sites, each [orc] Orc gains <b>muster</b>.<br><b>Shadow:</b> Discard this condition from play and spot an [orc] minion to replace the fellowship's current site with an underground site from your adventure deck.
		*/

		var scn = GetScenario();

		var depths1 = scn.GetFreepsCard("depths1");

		assertEquals("Unforgiving Depths", depths1.getBlueprint().getTitle());
		assertNull(depths1.getBlueprint().getSubtitle());
		assertFalse(depths1.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, depths1.getBlueprint().getSide());
		assertEquals(Culture.ORC, depths1.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, depths1.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(depths1, Keyword.SUPPORT_AREA));
		assertEquals(0, depths1.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReplacingTheCurrentSiteWithAnUndergroundOneMakesThreeAndEachOrcCultureOrcGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths1 = scn.GetShadowCard("depths1");
		var depths2 = scn.GetShadowCard("depths2");
		var denizen = scn.GetShadowCard("denizen");
		var moria = scn.GetShadowCard("moria");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");

		var freepsShaft = scn.GetFreepsSite("shaft");
		var ruins = scn.GetShadowSite("ruins");
		var stairway = scn.GetShadowSite("stairway");
		var bridge = scn.GetShadowSite("bridge");
		var shaft = scn.GetShadowSite("shaft");
		var road = scn.GetShadowSite("road");
		var freepsStairway = scn.GetFreepsSite("stairway");

		// The Free Peoples player's Mine Shaft is site 1; the fellowship moves to the Shadow player's Ruins, and his
		// Stairway lies ahead as site 3. Two underground sites, and a river.
		scn.StartGame(freepsShaft);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.MoveCardToAdventurePath(stairway);
		scn.MoveCardsToSupportArea(depths1, depths2);
		scn.MoveMinionsToTable(denizen, moria, halftroll, troop);
		scn.ShadowRefreshDecision();

		assertEquals(ruins, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, bridge, shaft, road, freepsStairway);
		assertInZone(Zone.SUPPORT, depths1, depths2);
		assertFalse(scn.HasKeyword(denizen, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(moria, Keyword.MUSTER));

		assertTrue(scn.ShadowActionAvailable(depths2));
		scn.ShadowUseCardAction(depths2);
		assertInDiscard(depths2);
		assertInZone(Zone.SUPPORT, depths1);

		// One underground site of his own adventure deck: not a forest, and not the Free Peoples player's Stairway
		// (underground, in the other player's deck: a choice from a pile is read by blueprint, and his own copy
		// lies ahead as site 3).
		assertTrue(scn.ShadowHasCardChoiceAvailable(bridge, shaft));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(road, freepsStairway));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(bridge);

		// The Bridge takes the Ruins' place as site 2, and the Ruins go back to their owner's adventure deck.
		assertEquals(bridge, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, ruins, shaft);

		// Three underground sites now: each [orc] Orc gains muster, and nothing else does.
		assertTrue(scn.HasKeyword(denizen, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(moria, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(halftroll, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(troop, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcCultureMinionWhoIsNoOrcIsEnoughToSpotAndWithNoUndergroundSiteNothingIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths1 = scn.GetShadowCard("depths1");
		var halftroll = scn.GetShadowCard("halftroll");

		var freepsShaft = scn.GetFreepsSite("shaft");
		var ruins = scn.GetShadowSite("ruins");
		var road = scn.GetShadowSite("road");
		var freepsBridge = scn.GetFreepsSite("bridge");

		scn.StartGame(freepsShaft);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		// His adventure deck holds a forest and nothing more.
		scn.SetShadowAdventureDeck(road);
		scn.MoveCardsToSupportArea(depths1);
		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();

		assertEquals(ruins, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, road, freepsBridge);
		assertInZone(Zone.SUPPORT, depths1);

		// The Half-troll is an [orc] minion: offered, paid, and nothing to lay.
		assertTrue(scn.ShadowActionAvailable(depths1));
		scn.ShadowUseCardAction(depths1);
		assertInDiscard(depths1);

		assertEquals(ruins, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, road, freepsBridge);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAMoriaOrcAndAnOrcConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths1 = scn.GetShadowCard("depths1");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");

		var ruins = scn.GetShadowSite("ruins");
		var bridge = scn.GetShadowSite("bridge");

		scn.StartGame(scn.GetFreepsSite("shaft"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.MoveCardsToSupportArea(depths1, beckoning);
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.ADVENTURE_DECK, bridge);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(depths1));
		assertEquals(ruins, scn.GetCurrentSite());
	}
}
