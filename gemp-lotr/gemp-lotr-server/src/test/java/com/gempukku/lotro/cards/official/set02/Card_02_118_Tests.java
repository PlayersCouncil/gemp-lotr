package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_118_Tests
{

	// The Chasm is site 4, after Rivendell Valley at site 3; site 5 is The Bridge of Khazad-dûm, whose Shadow action
	// plays The Balrog only.  [moria] minions have site number 4: none is roaming at the Chasm or at site 5.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "1_176");     // Goblin Marksman: [moria] Orc archer, cost 3; Archer is its whole text
					// Moria Archer Troop: [moria] Orc archer, cost 6; its text adds to the minion archery total only
					put("archertroop", "2_67");
					put("patrol", "1_177");       // Goblin Patrol Troop: a [moria] Orc who is no archer, cost 6, no text
					// Orthanc Assassin: an [isengard] Uruk-hai archer, cost 5, site 5 (roaming at the Chasm); no other text
					put("assassin", "1_131");
					put("host", "1_187");         // Host of Thousands: "Shadow: Play a [moria] Orc from your discard pile", cost 0
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "2_118");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path of the rig with Great Chasm as its site 4.
	protected VirtualTableScenario GetSite4Scenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer, (3), site 4; Archer is its whole text
					put("marksman2", "1_176");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; killed in a skirmish here
					// Now for Ruin: "Each time a minion is killed, spot 2 valiant companions to add a [rohan] token
					// here. Each time there are 3 [rohan] tokens here, remove all tokens here to play a [rohan]
					// condition or event from your discard pile. The Shadow player may then play a minion from
					// their discard pile."  A way for a minion to be played in a skirmish phase.
					put("ruin", "102_52");
					put("doom", "8_86");        // Doom Drove Them: a [rohan] condition for Now for Ruin to play, (1), with nothing to spot
					put("guard1", "5_83");      // Household Guard: a valiant companion, strength 6, with no other text
					put("guard2", "5_83");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "2_118");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatChasmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Great Chasm
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 4
		 * Direction: Right
		 * Game Text: <b>Underground</b>. The twilight cost of the first [moria] archer played each Shadow phase is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Great Chasm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void OnlyTheFirstMoriaArcherOfTheShadowPhaseIsTwoCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chasm = scn.GetShadowSite(4);
		var assassin = scn.GetShadowCard("assassin");
		var archertroop = scn.GetShadowCard("archertroop");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(assassin, archertroop, marksman);

		scn.SetTwilight(20);
		scn.FreepsPass();

		assertSame(chasm, scn.GetCurrentSite());
		// 20 set + 1 companion + 4 for the Chasm
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The [isengard] archer its printed 5; either [moria] archer 2 less while none has been played: 6 - 2 and 3 - 2
		assertEquals(5, scn.GetTwilightCost(assassin));
		assertEquals(4, scn.GetTwilightCost(archertroop));
		assertEquals(1, scn.GetTwilightCost(marksman));

		scn.ShadowPlayCard(assassin);
		// 5 to play + 2 roaming
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		// An archer who is not [moria] was not the first [moria] archer of the phase.
		assertEquals(4, scn.GetTwilightCost(archertroop));
		assertEquals(1, scn.GetTwilightCost(marksman));

		scn.ShadowPlayCard(archertroop);
		// 6 - 2 to play
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, archertroop);

		// The second [moria] archer of the phase costs its printed 3.
		assertEquals(3, scn.GetTwilightCost(marksman));
		scn.ShadowPlayCard(marksman);
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaOrcWhoIsNoArcherIsNeitherDiscountedNorCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var marksman = scn.GetShadowCard("marksman");
		var archertroop = scn.GetShadowCard("archertroop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(patrol, marksman, archertroop);

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Great Chasm", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 4 for the Chasm
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The Orc who is no archer its printed 6; the archers 3 - 2 and 6 - 2
		assertEquals(6, scn.GetTwilightCost(patrol));
		assertEquals(1, scn.GetTwilightCost(marksman));
		assertEquals(4, scn.GetTwilightCost(archertroop));

		scn.ShadowPlayCard(patrol);
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		// No [moria] archer has been played yet.
		assertEquals(1, scn.GetTwilightCost(marksman));
		assertEquals(4, scn.GetTwilightCost(archertroop));

		scn.ShadowPlayCard(marksman);
		// 3 - 2 to play
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertEquals(6, scn.GetTwilightCost(archertroop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaArcherPlayedFromTheDiscardPileByAnotherCardIsTheFirstOfThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var marksman = scn.GetShadowCard("marksman");
		var archertroop = scn.GetShadowCard("archertroop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(host, archertroop);
		scn.MoveCardsToDiscard(marksman);

		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals("Great Chasm", scn.GetCurrentSite().getBlueprint().getTitle());
		// 5 set + 1 companion + 4 for the Chasm
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, marksman);
		assertEquals(4, scn.GetTwilightCost(archertroop));

		// Host of Thousands costs nothing and plays the one [moria] Orc in the discard pile.
		scn.ShadowPlayCard(host);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertInZone(Zone.DISCARD, host);
		// 3 - 2 for the Marksman
		assertEquals(9, scn.GetTwilight());
		// It was the first [moria] archer played this phase.
		assertEquals(6, scn.GetTwilightCost(archertroop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstMoriaArcherPlayedInTheNextShadowPhaseAtTheChasmIsTwoCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Each player's own archers, for the turn on which that player is the Shadow player
		var firstPlayersArcherTroop = scn.GetFreepsCard("archertroop");
		var firstPlayersMarksman = scn.GetFreepsCard("marksman");
		var secondPlayersArcherTroop = scn.GetShadowCard("archertroop");
		var secondPlayersMarksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(secondPlayersArcherTroop, secondPlayersMarksman);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("Great Chasm", scn.GetCurrentSite().getBlueprint().getTitle());
		// 6 set + 1 companion + 4 for the Chasm
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilightCost(secondPlayersArcherTroop));

		scn.ShadowPlayCard(secondPlayersArcherTroop);
		assertEquals(7, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(secondPlayersMarksman));

		// The second player's turn.  His fellowship moves from site 3 to the same Chasm, and the first player
		// has the Shadow phase there.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetFreepsHand(firstPlayersArcherTroop, firstPlayersMarksman);
		scn.SetTwilight(6);
		scn.ShadowPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals("Great Chasm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		// 6 set + 1 companion + 4 for the Chasm
		assertEquals(11, scn.GetTwilight());
		// A new Shadow phase: 6 - 2 and 3 - 2 once more.
		assertEquals(4, scn.GetTwilightCost(firstPlayersArcherTroop));
		assertEquals(1, scn.GetTwilightCost(firstPlayersMarksman));

		scn.FreepsPlayCard(firstPlayersArcherTroop);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, firstPlayersArcherTroop);
		assertEquals(3, scn.GetTwilightCost(firstPlayersMarksman));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasLeftTheChasmTheFirstMoriaArcherCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chasm = scn.GetShadowSite(4);
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(marksman);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertSame(chasm, scn.GetCurrentSite());
		// 6 set + 1 companion + 4 for the Chasm
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// At the Chasm, with no [moria] archer played yet: 3 - 2
		assertEquals(1, scn.GetTwilightCost(marksman));

		// Nothing is played there.  The fellowship moves on to The Bridge of Khazad-dûm and the Chasm lies behind it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("The Bridge of Khazad-dûm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertSame(chasm, scn.GetSite(4));
		// 11 left + 1 companion + 6 for the Bridge
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The text of a site binds only while the fellowship is at it: the Marksman costs its printed 3.
		assertEquals(3, scn.GetTwilightCost(marksman));

		scn.ShadowPlayCard(marksman);
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaArcherPlayedInAnotherPhaseDoesNotTakeTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSite4Scenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(ruin);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCardsToDiscard(doom);
		scn.MoveCardsToHand(marksman1);
		scn.MoveCardsToDiscard(marksman2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());

		// The Shadow phase: the first [moria] archer costs 3 - 2.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(marksman1);
		assertEquals(twilight - 1, scn.GetTwilight());

		scn.MoveMinionsToTable(runner);
		// The Marksman's arrow goes to Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard1, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		// The Guard (6) kills the Runner (5): a third token, Doom Drove Them is played from the discard pile, and
		// the Shadow player may play a minion from his, in the skirmish phase.
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.SUPPORT, doom);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman2, runner));
		twilight = scn.GetTwilight();
		scn.ShadowChooseCardBPFromSelection(marksman2);

		assertInZone(Zone.SHADOW_CHARACTERS, marksman2);
		// 3 to play, and no discount: no [moria] archer was played before it in this phase, which is not a
		// Shadow phase.
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
