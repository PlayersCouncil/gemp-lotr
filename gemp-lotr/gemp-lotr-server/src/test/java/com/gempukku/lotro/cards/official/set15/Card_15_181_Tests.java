package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("later", "15_181");
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul, cost 4, strength 9, site 3; strength -2 for bearers of [wraith] conditions, and none is laid
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, cost 6, site 3; his bonuses want 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgul, cost 2; strength +4 and fierce at 2 wounds on the Ring-bearer
					put("nelya", "1_233");      // Ulaire Nelya, Lieutenant of Morgul: a [wraith] Nazgul, cost 5; her ability is used only on purpose
					put("rider", "12_161");     // Black Rider: a [wraith] Nazgul, fierce, strength +1 for each companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, no hunter; his ability is not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LaterThanYouThinkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Later Than You Think
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Remove 2 threats to play a Nazgûl. That Nazgûl is twilight cost -2 and gains <b>hunter 2</b> <helper>(While skirmishing a non-hunter character, this character is strength +2.)</helper> until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("later");

		assertEquals("Later Than You Think", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void Removes2ThreatsToPlayANazgulFromHandAt2LessWithHunter2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var later = scn.GetShadowCard("later");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var nelya = scn.GetShadowCard("nelya");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame();
		scn.SkipToSite(2);
		// The hand is made exactly these cards: the reconciles of the first two turns have drawn others.
		scn.SetShadowHand(later, lemenya, khamul, hound);
		// Two companions: room for two threats.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, nelya);
		scn.FreepsPass();

		// 10 + 0 for Rivendell Valley + 2 companions.
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(later));
		scn.ShadowPlayCard(later);
		assertEquals(0, scn.GetThreats());

		// A Nazgul of each culture from hand; not a [wraith] Orc, and not a Nazgul in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, nelya));
		scn.ShadowChooseCard(lemenya);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertInZone(Zone.DISCARD, later);
		// 12 - (4 - 2).
		assertEquals(10, scn.GetTwilight());
		assertEquals(2, scn.GetKeywordCount(lemenya, Keyword.HUNTER));
		// The Nazgul already in play gains nothing.
		assertFalse(scn.HasKeyword(rider, Keyword.HUNTER));

		// The discount was that Nazgul's alone: the next one played costs its full 6.
		scn.ShadowPlayCard(khamul);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.HasKeyword(khamul, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var later = scn.GetShadowCard("later");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// The hand is made exactly these cards: the reconciles of the first two turns have drawn others.
		scn.SetShadowHand(later, lemenya);
		scn.AddThreats(1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(later));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(later));
	}

	@Test
	public void CannotBePlayedWithNoNazgulInHandThatTheDiscountedCostCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var later = scn.GetShadowCard("later");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// The hand is made exactly these cards: the reconciles of the first two turns have drawn others.
		scn.SetShadowHand(later, hound);
		scn.AddThreats(2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		// No Nazgul in hand, only a [wraith] Orc the pool could pay for.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(later));

		// A Nazgul of cost 4, at 4 - 2: a pool of 1 cannot pay for it, a pool of 2 can.
		scn.MoveCardsToHand(lemenya);
		assertInHand(lemenya);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(later));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(later));
		scn.ShadowPlayCard(later);
		// The one Nazgul in hand is played with nothing asked.
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheNazgulIsAHunterThroughItsSkirmishAndNoLongerAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var later = scn.GetShadowCard("later");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// The hand is made exactly these cards: the reconciles of the first two turns have drawn others.
		scn.SetShadowHand(later, lemenya);
		scn.AddThreats(2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		scn.ShadowPlayCard(later);
		assertEquals(2, scn.GetKeywordCount(lemenya, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		// 9 + 2 for hunter, against Aragorn, who is no hunter.
		assertEquals(2, scn.GetKeywordCount(lemenya, Keyword.HUNTER));
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// 11 against 8: Aragorn loses, and the Nazgul goes on into the regroup phase.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.HasKeyword(lemenya, Keyword.HUNTER));
		assertEquals(9, scn.GetStrength(lemenya));
	}
}
