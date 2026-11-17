package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_318_Tests
{

	// The Fellowship-block path: site 2 Ettenmoors, site 3 Rivendell Valley, site 4 Balin's Tomb.  On a path of
	// numbered sites the player has one site of each number, so the next site played is his own of that number.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("map", "1_318");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit, the Shadow player's
					put("ted", "18_75");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is a Man
					// Smeagol, Once Like Us: a [shire] companion with no race
					put("smeagol", "103_116");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrorsMapStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Thrór's Map
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype:
		 * Game Text: Plays to your support area.
		 * 	<b>Fellowship</b> <i>or</i> <b>Regroup:</b> Exert 2 Hobbits and discard Thrór's Map to play the
		 * 	fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("map");

		assertEquals("Thrór's Map", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionExertsTwoHobbitsAndDiscardsTheMapToPlayHisOwnNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(map);
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.SUPPORT, map);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(map));

		scn.FreepsUseCardAction(map);

		// Hobbits of any culture, companions and allies, the Ring-bearer among them; not a Man, nor a [shire]
		// companion who is no Hobbit.  Exactly two.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(pippin, bounder);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.DISCARD, map);
		// His own site 2 is played, where a move would have had the Shadow player lay his.  Nothing is asked:
		// he has one site 2.  The fellowship has not moved, and site 3 is still unlaid.
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionMayExertTheShadowPlayersHobbitAndPlaysTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var ted = scn.GetShadowCard("ted");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(map);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		// Ted Sandyman stands in the regroup phase as he would after the skirmishes.
		scn.MoveMinionsToTable(ted);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(map));

		scn.FreepsUseCardAction(map);

		// "Hobbits" names no side (Q-0058): the Shadow player's Hobbit minion may pay.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCards(frodo, ted);

		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, map);
		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsSiteThatLiesNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin);

		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(map);

		// The other player's Balin's Tomb lies at site 4, as it does where he has played his next site himself.
		// Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.FreepsRefreshDecision();

		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Frodo and Pippin are the only Hobbits, and exert with nothing to choose.
		scn.FreepsUseCardAction(map);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));

		// His own site 4 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, map);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithHisOwnSiteAlreadyNextTheCostIsPaidAndNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(map);
		scn.MoveCompanionsToTable(pippin);

		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		// His own Ettenmoors lies next, as it does where he has played it already this phase.
		scn.MoveCardToAdventurePath(site2);
		scn.FreepsRefreshDecision();

		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		// The action has a cost, and is offered though it will play nothing.
		assertTrue(scn.FreepsActionAvailable(map));

		// Frodo and Pippin are the only Hobbits, and exert with nothing to choose.
		scn.FreepsUseCardAction(map);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertInZone(Zone.DISCARD, map);
		// Only an opponent's site is replaced: his own stays, and he has no other site 2.
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyOneHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(map);
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Vitality 4 with 3 wounds, and vitality 2 with 1: both exhausted.  Frodo alone of the Hobbits can exert;
		// Aragorn and Smeagol, who are no Hobbits, can too.
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(bounder, 1);
		scn.FreepsRefreshDecision();

		assertTrue(scn.IsExhausted(pippin));
		assertTrue(scn.IsExhausted(bounder));
		assertFalse(scn.IsExhausted(frodo));
		assertFalse(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(smeagol));
		assertInZone(Zone.SUPPORT, map);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(map));
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var pippin = scn.GetFreepsCard("pippin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(map);
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInZone(Zone.SUPPORT, map);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(map));
	}
}
