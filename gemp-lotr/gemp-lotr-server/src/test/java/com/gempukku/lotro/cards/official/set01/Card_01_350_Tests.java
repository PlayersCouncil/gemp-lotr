package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_350_Tests
{

	// The Dale is site 6, after The Bridge of Khazad-dûm at site 5 (its Shadow action plays The Balrog only).
	// Site 7 is Anduin Wilderland (Shadow number 6; the archery phase is skipped there) and not Anduin
	// Confluence, which discards every ally.  At sites 6 to 8 neither a [moria] minion (site number 4) nor a
	// [sauron] Orc (site number 6) is roaming.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					// Goblin Flankers: [moria] Orc, cost 5; may add (3) when played after a second move of the turn
					put("flankers", "2_61");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3; Archer is its whole text
					// Orc Chieftain: a [sauron] Orc, cost 2; its text answers a weapon played on it
					put("redeye", "1_266");
					// Vile Tentacle: a [moria] Creature, cost 2; its Shadow ability is used only on purpose
					put("tentacle", "101_36");
					put("host", "1_187");       // Host of Thousands: "Shadow: Play a [moria] Orc from your discard pile", cost 0
					// Uruviel, Maid of Lórien: ally, "While you can spot your site 6, Uruviel has the game text of that site"
					put("uruviel", "1_67");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path, with what plays a [moria] Orc outside a Shadow phase.
	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					// Orc Chieftain: a [sauron] Orc, vitality 2; its text answers a weapon played on it
					put("redeye", "1_266");
					// Peril: [sauron] support-area condition, "Regroup: Exert a [sauron] Orc to play a Nazgûl or
					// [sauron] minion. Its twilight cost is -4 and it comes into play exhausted."
					put("peril", "6_105");
					// Úlairë Nertëa, Messenger of Dol Guldur: Nazgûl, cost 4, site number 3; "When you play Úlairë
					// Nertëa, for each companion over 4, you may play 1 minion from your discard pile."
					put("nertea", "1_234");

					// Dwarf Guard: a companion with no text in play; four of them and Frodo are five companions
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DimrillDaleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dimrill Dale
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. The twilight cost of the first [moria] Orc played each Shadow phase is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Dimrill Dale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void DimrillDaleOnlyDiscountsFirstMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dale = scn.GetShadowSite(6);

		var troop = scn.GetShadowCard("troop");
		var flankers = scn.GetShadowCard("flankers");
		var redeye = scn.GetShadowCard("redeye");

		scn.StartGame();
		// The turn begins at site 5, so that the move to the Dale is its first.
		scn.SkipToSite(5);
		scn.SetShadowHand(redeye, troop, flankers);

		scn.SetTwilight(11);
		scn.FreepsPass();

		assertSame(dale, scn.GetCurrentSite());
		// 11 set + 1 companion + 3 for the Dale
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The [sauron] Orc its printed 2; either [moria] Orc 2 less while none has been played: 6 - 2 and 5 - 2
		assertEquals(2, scn.GetTwilightCost(redeye));
		assertEquals(4, scn.GetTwilightCost(troop));
		assertEquals(3, scn.GetTwilightCost(flankers));

		scn.ShadowPlayCard(redeye);
		// 2 to play
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, redeye);
		// An Orc that is not [moria] was not the first [moria] Orc of the phase.
		assertEquals(4, scn.GetTwilightCost(troop));
		assertEquals(3, scn.GetTwilightCost(flankers));

		scn.ShadowPlayCard(troop);
		// 6 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// The second [moria] Orc of the phase costs its printed 5.
		assertEquals(5, scn.GetTwilightCost(flankers));
		scn.ShadowPlayCard(flankers);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, flankers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaMinionThatIsNoOrcIsNeitherDiscountedNorCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tentacle = scn.GetShadowCard("tentacle");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(tentacle, marksman, troop);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		// 6 set + 1 companion + 3 for the Dale
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The tentacle its printed 2 (2 less would read 0); the Orcs 3 - 2 and 6 - 2
		assertEquals(2, scn.GetTwilightCost(tentacle));
		assertEquals(1, scn.GetTwilightCost(marksman));
		assertEquals(4, scn.GetTwilightCost(troop));

		scn.ShadowPlayCard(tentacle);
		// 2 to play
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, tentacle);
		// No [moria] Orc has been played yet.
		assertEquals(1, scn.GetTwilightCost(marksman));
		assertEquals(4, scn.GetTwilightCost(troop));

		scn.ShadowPlayCard(marksman);
		// 3 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertEquals(6, scn.GetTwilightCost(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaOrcPlayedFromTheDiscardPileByAnotherCardIsTheFirstOfThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var flankers = scn.GetShadowCard("flankers");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(host, flankers);
		scn.MoveCardsToDiscard(troop);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		// 6 set + 1 companion + 3 for the Dale
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, troop);
		assertEquals(3, scn.GetTwilightCost(flankers));

		// Host of Thousands costs nothing and plays the one [moria] Orc in the discard pile.
		scn.ShadowPlayCard(host);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, host);
		// 6 - 2 for the Troop
		assertEquals(6, scn.GetTwilight());
		// It was the first [moria] Orc played this phase.
		assertEquals(5, scn.GetTwilightCost(flankers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstMoriaOrcPlayedInTheNextShadowPhaseAtTheDaleIsTwoCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Each player's own Orcs, for the turn on which that player is the Shadow player
		var firstPlayersTroop = scn.GetFreepsCard("troop");
		var firstPlayersFlankers = scn.GetFreepsCard("flankers");
		var secondPlayersTroop = scn.GetShadowCard("troop");
		var secondPlayersFlankers = scn.GetShadowCard("flankers");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(secondPlayersTroop, secondPlayersFlankers);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		// 6 set + 1 companion + 3 for the Dale
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilightCost(secondPlayersTroop));

		scn.ShadowPlayCard(secondPlayersTroop);
		assertEquals(6, scn.GetTwilight());
		assertEquals(5, scn.GetTwilightCost(secondPlayersFlankers));

		// The second player's turn.  His fellowship moves from site 5 to the same Dale, and the first player
		// has the Shadow phase there.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetFreepsHand(firstPlayersTroop, firstPlayersFlankers);
		scn.SetTwilight(6);
		scn.ShadowPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		// 6 set + 1 companion + 3 for the Dale
		assertEquals(10, scn.GetTwilight());
		// A new Shadow phase: 6 - 2 and 5 - 2 once more.
		assertEquals(4, scn.GetTwilightCost(firstPlayersTroop));
		assertEquals(3, scn.GetTwilightCost(firstPlayersFlankers));

		scn.FreepsPlayCard(firstPlayersTroop);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, firstPlayersTroop);
		assertEquals(5, scn.GetTwilightCost(firstPlayersFlankers));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasLeftTheDaleTheFirstMoriaOrcCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dale = scn.GetShadowSite(6);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(troop);

		scn.SetTwilight(6);
		scn.FreepsPass();

		assertSame(dale, scn.GetCurrentSite());
		// 6 set + 1 companion + 3 for the Dale
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// At the Dale, with no [moria] Orc played yet: 6 - 2
		assertEquals(4, scn.GetTwilightCost(troop));

		// Nothing is played there.  The fellowship moves on to Anduin Wilderland and the Dale lies behind it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Anduin Wilderland", scn.GetCurrentSite().getBlueprint().getTitle());
		assertSame(dale, scn.GetSite(6));
		// 10 left + 1 companion + 6 for Anduin Wilderland
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The text of a site binds only while the fellowship is at it: the Troop costs its printed 6.
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.ShadowPlayCard(troop);
		// 6 to play
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaOrcPlayedAtTheDaleInTheRegroupPhaseCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var troop = scn.GetShadowCard("troop");
		var redeye = scn.GetShadowCard("redeye");
		var peril = scn.GetShadowCard("peril");
		var nertea = scn.GetShadowCard("nertea");

		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(peril);
		scn.SetShadowHand(nertea);
		scn.MoveCardsToDiscard(troop);

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 5 companions + 3 for the Dale
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// In the Shadow phase the Troop would be the first [moria] Orc played: 6 - 2.
		assertEquals(4, scn.GetTwilightCost(troop));

		// A [sauron] Orc for Peril to exert.  No [moria] Orc is played in the Shadow phase.
		scn.MoveMinionsToTable(redeye);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals("Dimrill Dale", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(18, scn.GetTwilight());
		// The regroup phase is no Shadow phase: the Troop costs its printed 6.
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);
		// The Chieftain exerts and Nertëa is played from hand for 4 - 4.
		assertEquals(1, scn.GetWoundsOn(redeye));
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(18, scn.GetTwilight());

		// Five companions: her own text offers one minion from the discard pile, and the Troop is the one there.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 6 to play
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// The site's own text is in force in one Shadow phase of a turn at most: a second move leaves the Dale.
	// Uruviel has the text of her owner's site 6 wherever the fellowship stands, so with her the same text meets
	// two Shadow phases of one turn.
	@Test
	public void WithUruvielsCopyOfTheTextTheFirstMoriaOrcOfASecondShadowPhaseInTheTurnIsTwoCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPlayersDale = scn.GetFreepsSite(6);
		var uruviel = scn.GetFreepsCard("uruviel");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(5);
		// Site 6 is the first player's own Dale, as it is where he played his next site himself.
		scn.MoveCardToAdventurePath(firstPlayersDale);
		scn.SkipToSite(6);
		assertSame(firstPlayersDale, scn.GetCurrentSite());

		scn.MoveAlliesToTable(uruviel);
		scn.SetShadowHand(troop, marksman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Anduin Wilderland", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 6 for Anduin Wilderland
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Away from the Dale, by her copy of its text: 6 - 2 and 3 - 2
		assertEquals(4, scn.GetTwilightCost(troop));
		assertEquals(1, scn.GetTwilightCost(marksman));

		scn.ShadowPlayCard(troop);
		assertEquals(13, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(marksman));

		// The fellowship moves again: a second Shadow phase in the same turn, at Anduin Banks.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 13 left + 1 companion + 6 for Anduin Banks
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// No [moria] Orc has been played in this Shadow phase, though one was played this turn.
		assertEquals(1, scn.GetTwilightCost(marksman));

		scn.ShadowPlayCard(marksman);
		// 3 - 2 to play
		assertEquals(19, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
