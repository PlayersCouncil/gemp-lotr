package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_325_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pressing", "7_325");
					put("pressing2", "7_325");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: a [shire] Hobbit ally, cost 1, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is a Man; a maneuver ability only
					// Smeagol, Once Like Us: a [shire] companion with no race
					put("smeagol", "103_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo and Sam to exert, and a companion who is no Hobbit.
	protected VirtualTableScenario GetHobbitCompanionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pressing", "7_325");
					put("sam", "1_311");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PressingOnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pressing On
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Hobbits.<br>Each time the fellowship moves, add a threat or discard this condition.<br><b>Regroup:</b> Discard this condition and exert a Hobbit companion to play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pressing");

		assertEquals("Pressing On", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Regroup: Discard this condition and exert a Hobbit companion to play the fellowship's next site": the
	// definition had no exertion.
	@Test
	public void TheRegroupAbilityExertsAHobbitCompanionAsWellAsDiscardingTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitCompanionsScenario();

		var frodo = scn.GetRingBearer();
		var pressing = scn.GetFreepsCard("pressing");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(pressing);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, pressing);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(pressing));
		scn.FreepsUseCardAction(pressing);

		// The condition goes first, and then a Hobbit companion is chosen: Frodo or Sam, and not Aragorn.
		assertInZone(Zone.DISCARD, pressing);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASecondHobbitIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(pressing, bounder);
		assertInHand(pressing, bounder);

		scn.StartGame();

		// Frodo is the one Hobbit, beside a Man and a [shire] companion who is no Hobbit.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pressing));

		// A Hobbit ally makes two.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.FreepsPlayAvailable(pressing));

		scn.FreepsPlayCard(pressing);
		assertInZone(Zone.SUPPORT, pressing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitCompanionOfAnotherCultureIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(pressing);
		assertInHand(pressing);

		scn.StartGame();

		// Frodo and a [gondor] Hobbit.
		assertTrue(scn.FreepsPlayAvailable(pressing));
		scn.FreepsPlayCard(pressing);
		assertInZone(Zone.SUPPORT, pressing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveAsksToAddAThreatOrDiscardItAndAThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(pressing);
		assertInZone(Zone.SUPPORT, pressing);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The fellowship moves from site 1.
		scn.FreepsPass();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, pressing);

		// No minion: on to the regroup phase, and a second move. Two companions leave room for a second threat.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, pressing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAMoveItMayBeDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(pressing);
		assertInZone(Zone.SUPPORT, pressing);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(pressing);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRoomForAThreatItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		scn.MoveCardsToSupportArea(pressing);
		assertInZone(Zone.SUPPORT, pressing);

		scn.StartGame();
		// Frodo is the only companion: one threat is all there may be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertInDiscard(pressing);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyAnswersAMoveForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pressing = scn.GetFreepsCard("pressing");
		var pressing2 = scn.GetFreepsCard("pressing2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(pressing, pressing2);
		assertInZone(Zone.SUPPORT, pressing, pressing2);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The copy laid first answers first: discarding it discards that copy alone.
		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(pressing);
		assertInZone(Zone.SUPPORT, pressing2);

		// The second copy asks for itself.
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, pressing2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItAndExertsAHobbitCompanionToPlayHisOwnNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pressing = scn.GetFreepsCard("pressing");
		var pressing2 = scn.GetFreepsCard("pressing2");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.  The two
		// copies are laid after that move, which would otherwise ask for a threat or their discard.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(pressing, pressing2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, pressing, pressing2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pressing));

		scn.FreepsUseCardAction(pressing);

		// Hobbit companions of two cultures, the Ring-bearer among them; not a Hobbit ally, a Man, nor a [shire]
		// companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		// The copy used is discarded, and the other stays.
		assertInDiscard(pressing);
		assertInZone(Zone.SUPPORT, pressing2);
		// His own site 3 is played, where a move would have had the Shadow player lay his.  The fellowship has not
		// moved, and site 4 is still unlaid.
		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsSiteThatLiesNextAndWithHisOwnSiteNextPlaysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pressing = scn.GetFreepsCard("pressing");
		var pressing2 = scn.GetFreepsCard("pressing2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		// The fellowship is at site 3, and the Shadow player's Balin's Tomb lies at site 4, as where he played his
		// own next site on his turn.  Both players hold the same nine sites here, so the site that replaces it is
		// its twin.
		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.MoveCardsToSupportArea(pressing, pressing2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, pressing, pressing2);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(pressing));

		// Frodo is the only Hobbit companion, and exerts with nothing to choose.
		scn.FreepsUseCardAction(pressing);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(pressing);
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With his own site 4 next, the second copy is still offered (it has a cost), is paid for, and plays
		// nothing on this numbered path.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(pressing2));
		scn.FreepsUseCardAction(pressing2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInDiscard(pressing2);
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertNull(scn.GetSite(5));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoHobbitCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pressing = scn.GetFreepsCard("pressing");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(pressing);
		// Vitality 4 with 3 wounds: both Hobbit companions are exhausted.  The Hobbit ally, the Man and the [shire]
		// companion who is no Hobbit can exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, pressing);
		assertTrue(scn.IsExhausted(frodo));
		assertTrue(scn.IsExhausted(pippin));
		assertFalse(scn.IsExhausted(bounder));
		assertFalse(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pressing));
	}
}
