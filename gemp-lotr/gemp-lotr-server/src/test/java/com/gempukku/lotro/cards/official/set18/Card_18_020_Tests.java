package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marching", "18_20");
					put("quickbeam", "6_33");   // Quickbeam, Bregalad: Ent companion, strength 8, vitality 3; a cost text only
					put("avenger", "6_27");     // Ent Avenger: Ent companion, strength 6, vitality 4; +2 only with 3 Ents
					// Treebeard, Oldest Living Thing: Ent ally, vitality 4; a fellowship ability used only on purpose
					put("treebeard", "4_104");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; his ability is used only on purpose

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; a trigger on its own play only
					put("runner2", "1_178");
					put("filler", "1_177");     // Goblin Patrol Troop: a card with no text, to be drawn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntsMarchingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ents Marching
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: If the fellowship is in region 1 or region 2, exert 2 Ents to make an Ent <b>defender</b> <b>+1</b> until the regroup phase.<br>If the fellowship is in region 3, spot a [gandalf] companion to draw a card.
		*/

		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");

		assertEquals("Ents Marching", marching.getBlueprint().getTitle());
		assertNull(marching.getBlueprint().getSubtitle());
		assertFalse(marching.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, marching.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, marching.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, marching.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(marching, Timeword.MANEUVER));
		assertEquals(3, marching.getBlueprint().getTwilightCost());
	}

	@Test
	public void InRegion1ItExertsTwoEntsToMakeAnEntDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(quickbeam, avenger, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(marching);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// Site 2: region 1.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(marching));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(marching);
		// Exactly 2 Ents, companion or ally; Gandalf is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(quickbeam, avenger, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(avenger, treebeard);
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// "An Ent": any of them, one that did not exert as well.
		assertTrue(scn.FreepsHasCardChoiceAvailable(quickbeam, avenger, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(quickbeam);
		assertEquals(1, scn.GetKeywordCount(quickbeam, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		// Region 1: no card drawn.
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(marching);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegion2TheDefenderBonusLastsUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(quickbeam, avenger, gandalf);
		scn.MoveCardsToHand(marching);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Site 6: region 2, and Gandalf is a [gandalf] companion.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(marching));
		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(avenger));
		int deck = scn.GetFreepsDeckCount();

		// The only two Ents there exert unasked.
		scn.FreepsPlayCard(marching);
		assertEquals(1, scn.GetWoundsOn(quickbeam));
		assertEquals(1, scn.GetWoundsOn(avenger));
		scn.FreepsChooseCard(quickbeam);
		assertEquals(1, scn.GetKeywordCount(quickbeam, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DEFENDER));
		// Not region 3: no card drawn.
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{avenger, runner}, new PhysicalCardImpl[]{quickbeam, runner2});
		scn.FreepsResolveSkirmish(avenger);
		scn.PassCurrentPhaseActions();

		// The second skirmish, after one has resolved: still defender +1.
		scn.FreepsResolveSkirmish(quickbeam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(quickbeam, Keyword.DEFENDER));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(quickbeam, Keyword.DEFENDER));
	}

	@Test
	public void InRegion2WithOneEntAbleToExertItIsNotPlayableThoughAGandalfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(quickbeam, avenger, gandalf, aragorn);
		// Vitality 4: exhausted.  Gandalf and Aragorn could exert, but are no Ents.
		scn.AddWoundsToChar(avenger, 3);
		assertEquals(3, scn.GetWoundsOn(avenger));
		scn.MoveCardsToHand(marching);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertInHand(marching);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(marching));
	}

	@Test
	public void InRegion3ItNeedsAGandalfCompanionSpottedAndDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var filler = scn.GetFreepsCard("filler");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		// A [gandalf] Ent who is an ally, not a companion; a companion of another culture.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(marching);
		scn.MoveCardsToTopOfDeck(filler);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 8: region 3.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, treebeard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(marching));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(marching));
		assertInZone(Zone.DECK, filler);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(marching);
		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DEFENDER));
		assertInDiscard(marching);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegion3NoEntExertsAndAnEntCompanionIsSpottedToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marching = scn.GetFreepsCard("marching");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var filler = scn.GetFreepsCard("filler");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(6);
		// Two Ents able to exert, who are [gandalf] companions as well.
		scn.MoveCompanionsToTable(quickbeam, avenger);
		scn.MoveCardsToHand(marching);
		scn.MoveCardsToTopOfDeck(filler);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 7: region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DECK, filler);
		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(marching));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(marching);
		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetKeywordCount(quickbeam, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DEFENDER));
		assertInDiscard(marching);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
