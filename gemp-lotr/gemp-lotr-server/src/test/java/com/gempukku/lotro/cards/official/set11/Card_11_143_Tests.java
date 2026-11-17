package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_143_Tests
{

	// The card's own block: the Shadow player lays each next site, and holds the sites not laid to replace one with.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watchful", "11_143");
					put("watchful2", "11_143");
					// Denizen of Khazad-dûm: [orc] Orc minion; strength +2 at an underground site, and nothing more
					put("denizen", "11_115");
					put("denizen2", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is never reached
					put("halftroll", "14_12");
					// The Beckoning Shadow: an [orc] condition (its play cost is passed over by the cheat); its text needs 5 burdens
					put("beckoning", "12_83");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, and no text
					// Abandoned Mine Shaft: Underground; adds a burden only when left in the regroup phase outside region 1
					put("shaft", "13_185");
					put("bridge", "12_186");    // The Bridge of Khazad-dûm: Battleground, Underground; The Balrog's cost only
					put("stairway", "11_248");  // Moria Stairway: Underground; a hand weapon's bearer is damage +1, and none is laid
					put("road", "11_236");      // East Road: Forest; each companion is twilight cost +2, and none is played
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
	public void WatchfulOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Watchful Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot another [orc] minion to replace the fellowship's current site with an underground site from your adventure deck.
		*/

		var scn = GetScenario();

		var watchful = scn.GetFreepsCard("watchful");

		assertEquals("Watchful Orc", watchful.getBlueprint().getTitle());
		assertNull(watchful.getBlueprint().getSubtitle());
		assertFalse(watchful.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, watchful.getBlueprint().getSide());
		assertEquals(Culture.ORC, watchful.getBlueprint().getCulture());
		assertEquals(CardType.MINION, watchful.getBlueprint().getCardType());
		assertEquals(Race.ORC, watchful.getBlueprint().getRace());
		assertEquals(4, watchful.getBlueprint().getTwilightCost());
		assertEquals(8, watchful.getBlueprint().getStrength());
		assertEquals(3, watchful.getBlueprint().getVitality());
		assertEquals(4, watchful.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingAnotherOrcMinionItReplacesTheCurrentSiteWithAnUndergroundSiteOfHisOwnAndAnotherMinionPlayedLaterDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchful = scn.GetShadowCard("watchful");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");

		var shaft = scn.GetShadowSite("shaft");
		var bridge = scn.GetShadowSite("bridge");
		var stairway = scn.GetShadowSite("stairway");
		var road = scn.GetShadowSite("road");
		var ruins = scn.GetShadowSite("site1");
		var freepsShaft = scn.GetFreepsSite("shaft");

		// The fellowship moves from the Free Peoples player's Sirannon Ruins to the Shadow player's Mine Shaft.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(denizen);
		scn.MoveCardsToHand(watchful, denizen2);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, ruins, bridge, stairway, road, freepsShaft);

		scn.ShadowPlayCard(watchful);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watchful));
		scn.ShadowAcceptOptionalTrigger();

		// One underground site of his own adventure deck: the Free Peoples player's Mine Shaft is underground, but
		// his own lies under the fellowship and is in his deck no longer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bridge, stairway));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ruins, road, freepsShaft));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(bridge);

		// The Bridge takes the Mine Shaft's place as site 2, and the Mine Shaft goes back to its owner's adventure deck.
		assertEquals(bridge, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, shaft, stairway);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] minion played once this one is in play is not "this minion".
		scn.ShadowPlayCard(denizen2);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(bridge, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, shaft, stairway);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsEnoughToSpotAndDecliningLeavesTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchful = scn.GetShadowCard("watchful");
		var halftroll = scn.GetShadowCard("halftroll");

		var shaft = scn.GetShadowSite("shaft");
		var bridge = scn.GetShadowSite("bridge");
		var stairway = scn.GetShadowSite("stairway");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(halftroll);
		scn.MoveCardsToHand(watchful);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, bridge, stairway);

		scn.ShadowPlayCard(watchful);
		// The Half-troll alone is the other [orc] minion.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watchful));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, bridge, stairway);
		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondWatchfulOrcIsAnotherOrcMinionAndOnlyThePlayedCopyOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchful = scn.GetShadowCard("watchful");
		var watchful2 = scn.GetShadowCard("watchful2");

		var shaft = scn.GetShadowSite("shaft");
		var stairway = scn.GetShadowSite("stairway");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(watchful2);
		scn.MoveCardsToHand(watchful);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());

		scn.ShadowPlayCard(watchful);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watchful));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(watchful2));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCardBPFromSelection(stairway);

		assertEquals(stairway, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, shaft);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyAnOrcConditionAndAMoriaOrcBesideItNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchful = scn.GetShadowCard("watchful");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");

		var shaft = scn.GetShadowSite("shaft");
		var bridge = scn.GetShadowSite("bridge");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(watchful);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SUPPORT, beckoning);
		assertEquals(shaft, scn.GetCurrentSite());

		// The Beckoning Shadow is an [orc] card that is no minion, the Troop a minion of [moria]; Watchful Orc
		// itself is no "other" [orc] minion.
		scn.ShadowPlayCard(watchful);
		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, bridge);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUndergroundSiteInHisAdventureDeckNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchful = scn.GetShadowCard("watchful");
		var denizen = scn.GetShadowCard("denizen");

		var shaft = scn.GetShadowSite("shaft");
		var ruins = scn.GetShadowSite("site1");
		var road = scn.GetShadowSite("road");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		// His adventure deck left with a river and a forest.
		scn.SetShadowAdventureDeck(ruins, road);
		scn.MoveMinionsToTable(denizen);
		scn.MoveCardsToHand(watchful);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, ruins, road);

		scn.ShadowPlayCard(watchful);
		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, ruins, road);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
