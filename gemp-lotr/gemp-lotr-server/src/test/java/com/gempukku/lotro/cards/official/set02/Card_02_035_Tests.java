package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "2_35");

					put("ranger", "4_122");     // Gondorian Ranger: [gondor] Man ranger, cost 2; a site number modifier on minions only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; +3 only against a Nazgul
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man who is no ranger; Knight is his whole text
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf who is no ranger; his archery ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, whose site 1 (Eastemnet Gullies) is no forest, with Derndingle, a forest that is no
	// sanctuary, at site 2.
	protected VirtualTableScenario GetDerndingleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "2_35");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: the one Free Peoples archer; his archery ability is not used

					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc archer and nothing else
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: Forest. Move limit +1 while an unbound Hobbit is there; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path, with Rivendell Waterfall, a forest that is also a sanctuary, at site 3; site 2
	// (Ettenmoors) is plains.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "2_35");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: the one Free Peoples archer; his archery ability is not used

					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc archer and nothing else
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Rivendell Waterfall: Forest, Sanctuary; move limit +1 for the turn while a ranger is spotted there
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaturalCoverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Natural Cover
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a ranger. Plays to your support area.<br>While the fellowship is at a forest, the minion archery total is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cover");

		assertEquals("Natural Cover", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGondorRangerIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetFreepsCard("cover");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(knight, legolas);
		scn.MoveCardsToHand(cover, ranger);

		scn.StartGame();

		// Frodo, a [gondor] Man who is no ranger and an Elf who is no ranger: nothing to spot.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(cover);
		assertFalse(scn.FreepsPlayAvailable(cover));

		scn.FreepsPlayCard(ranger);
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertTrue(scn.FreepsPlayAvailable(cover));

		scn.FreepsPlayCard(cover);
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhileAnElvenRangerIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetFreepsCard("cover");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(cover);

		scn.StartGame();

		assertInHand(cover);
		assertTrue(scn.FreepsPlayAvailable(cover));

		scn.FreepsPlayCard(cover);
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtDerndingleTheMinionArcheryTotalIsTwoLowerAndAtASiteThatIsNoForestItIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDerndingleScenario();

		var cover = scn.GetFreepsCard("cover");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(cover);
		assertInZone(Zone.SUPPORT, cover);

		scn.StartGame();
		// Laid in the fellowship phase of the turn whose archery phase they stand in.
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");
		scn.FreepsRefreshDecision();

		// Eastemnet Gullies is no forest: three minion archers count 3, and Legolas 1 for the fellowship.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves to Derndingle.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		// 3 - 2: a -1 would read 2 and a -3 would read 0; the fellowship archery total is left alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtRivendellWaterfallAForestThatIsASanctuaryTheMinionArcheryTotalIsTwoLower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var cover = scn.GetFreepsCard("cover");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(cover);
		assertInZone(Zone.SUPPORT, cover);

		scn.StartGame();
		scn.SkipToSite(2);
		// Laid in the fellowship phase of the turn whose archery phase they stand in.
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");
		scn.FreepsRefreshDecision();

		// Ettenmoors is plains and no forest.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves to Rivendell Waterfall.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
