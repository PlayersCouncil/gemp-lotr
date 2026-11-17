package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_104_Tests
{

	// The Towers path: site 2 is Eastfold 4_331, a plains and no battleground; site 4 is White Rocks 4_346, a
	// battleground whose Shadow number counts mounted companions (there are none).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("taunt", "12_104");

					// On the table.
					// Denizen of Khazad-dum: an [orc] Orc minion, strength 9; +2 only at an underground site.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; its start-of-assignment
					// trigger needs more companions than minions.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc with no text, strength 13.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area); cheated in, its text needs 5 burdens.
					put("beckoning", "12_83");

					// In the draw decks, where their texts do nothing.  Orc Spear: an [orc] possession.
					put("deckdenizen", "11_115");
					put("deckdenizen2", "11_115");
					put("deckhalftroll", "14_12");
					put("decktroop", "1_177");
					put("decktroop2", "1_177");
					put("deckspear", "12_96");
					put("deckspear2", "12_96");
					put("deckbeckoning", "12_83");

					// Aragorn, Ranger of the North: a companion to skirmish.  His maneuver ability is never used.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains) as site 2; it exerts each Uruk-hai PLAYED, and none is.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("taunt", "12_104");

					// On the table.
					// Denizen of Khazad-dum: an [orc] Orc minion, strength 9; +2 only at an underground site.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; its start-of-assignment
					// trigger needs more companions than minions.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc with no text, strength 13.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area); cheated in, its text needs 5 burdens.
					put("beckoning", "12_83");

					// In the draw decks, where their texts do nothing.  Orc Spear: an [orc] possession.
					put("deckdenizen", "11_115");
					put("deckdenizen2", "11_115");
					put("deckhalftroll", "14_12");
					put("decktroop", "1_177");
					put("decktroop2", "1_177");
					put("deckspear", "12_96");
					put("deckspear2", "12_96");
					put("deckbeckoning", "12_83");

					// Aragorn, Ranger of the North: a companion to skirmish.  His maneuver ability is never used.
					put("aragorn", "1_89");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TauntStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Taunt
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Reveal the top 5 cards of your draw deck (or, if the fellowship is at a battleground site, the top 7 cards) to make an [orc] minion strength +1 for each [orc] card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("taunt");

		assertEquals("Taunt", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundItRevealsTheTop5AndMakesAnOrcMinionStrengthPlus1ForEachOrcCardAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		// The top 5: three [orc] cards (a minion, a possession, a condition) and two [moria] Orcs.  Below them two
		// more [orc] cards, which only the top 7 would take in, and an eighth.
		var deck = new PhysicalCardImpl[]{
				scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("decktroop"), scn.GetShadowCard("deckspear"),
				scn.GetShadowCard("deckbeckoning"), scn.GetShadowCard("decktroop2"),
				scn.GetShadowCard("deckhalftroll"), scn.GetShadowCard("deckspear2"), scn.GetShadowCard("deckdenizen2")};
		scn.SetShadowDrawDeck(deck);
		// The other player's deck has five [orc] cards on top: "your draw deck" is not his.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("deckdenizen"), scn.GetFreepsCard("deckspear"),
				scn.GetFreepsCard("deckbeckoning"), scn.GetFreepsCard("deckhalftroll"), scn.GetFreepsCard("deckspear2"));
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertInZone(Zone.SUPPORT, beckoning);
		assertEquals(deck[0], scn.GetShadowTopOfDeck());
		assertEquals(8, scn.GetShadowDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{frodo, troop});
		// The Half-troll stays out of any skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowPlayAvailable(taunt));
		scn.ShadowPlayCard(taunt);

		// The top 5 are shown to both players; the sixth is not.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(5, scn.ShadowGetTotalRevealedCardCount());
		for (int i = 0; i < 5; i++) {
			assertTrue(scn.FreepsHasCardChoiceAvailable(deck[i], false));
			assertTrue(scn.ShadowHasCardChoiceAvailable(deck[i], false));
		}
		scn.DismissRevealedCards();

		// An [orc] minion: the Orc in the skirmish and the Half-troll in none.  The [moria] Orc and the [orc]
		// condition are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		// Three [orc] cards in the top 5: 8 + 3.
		assertEquals(11, scn.GetStrength(halftroll));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(taunt);
		// A reveal moves nothing.
		assertEquals(deck[0], scn.GetShadowTopOfDeck());
		assertEquals(8, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtUrukCampABattlegroundItRevealsTheTop7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		var deck = new PhysicalCardImpl[]{
				scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("decktroop"), scn.GetShadowCard("deckspear"),
				scn.GetShadowCard("deckbeckoning"), scn.GetShadowCard("decktroop2"),
				scn.GetShadowCard("deckhalftroll"), scn.GetShadowCard("deckspear2"), scn.GetShadowCard("deckdenizen2")};
		scn.SetShadowDrawDeck(deck);
		scn.MoveMinionsToTable(denizen);
		assertEquals(deck[0], scn.GetShadowTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(9, scn.GetStrength(denizen));
		scn.ShadowPlayCard(taunt);
		// The top 7 are shown; the eighth is not.
		assertEquals(7, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(7, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(deck[5], false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(deck[6], false));
		scn.DismissRevealedCards();

		// Five [orc] cards in the top 7, the only [orc] minion chosen unasked: 9 + 5.
		assertEquals(14, scn.GetStrength(denizen));
		assertInDiscard(taunt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtWhiteRocksABattlegroundItRevealsTheTop7AndTheBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(taunt);
		var deck = new PhysicalCardImpl[]{
				scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("decktroop"), scn.GetShadowCard("deckspear"),
				scn.GetShadowCard("deckbeckoning"), scn.GetShadowCard("decktroop2"),
				scn.GetShadowCard("deckhalftroll"), scn.GetShadowCard("deckspear2"), scn.GetShadowCard("deckdenizen2")};
		scn.SetShadowDrawDeck(deck);
		scn.MoveMinionsToTable(denizen, halftroll);
		assertEquals(deck[0], scn.GetShadowTopOfDeck());

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{frodo, halftroll});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(8, scn.GetStrength(halftroll));
		scn.ShadowPlayCard(taunt);
		assertEquals(7, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		// The Half-troll, whose own skirmish is still to come (F-0527: any [orc] minion).
		scn.ShadowChooseCard(halftroll);
		// Five [orc] cards in the top 7: 8 + 5.
		assertEquals(13, scn.GetStrength(halftroll));
		assertEquals(9, scn.GetStrength(denizen));

		// Each skirmish is a phase of its own: in Frodo's, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		scn.MoveMinionsToTable(denizen);
		assertInHand(taunt);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowDecisionAvailable("Assignment"));
		assertFalse(scn.ShadowPlayAvailable(taunt));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(taunt));
	}

	@Test
	public void CannotBePlayedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		scn.SetShadowDrawDeck();
		scn.MoveMinionsToTable(denizen);
		assertEquals(0, scn.GetShadowDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(taunt));

		// Five cards put in the deck: it can be played.
		scn.SetShadowDrawDeck(scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("decktroop"),
				scn.GetShadowCard("deckspear"), scn.GetShadowCard("deckbeckoning"), scn.GetShadowCard("decktroop2"));
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(taunt));
	}

	@Test
	public void WithFewerThanFiveCardsInTheDrawDeckItRevealsThoseThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		// Four cards: three [orc] cards and a [moria] Orc.
		var deck = new PhysicalCardImpl[]{
				scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("decktroop"), scn.GetShadowCard("deckspear"),
				scn.GetShadowCard("deckbeckoning")};
		scn.SetShadowDrawDeck(deck);
		scn.MoveMinionsToTable(denizen);
		assertEquals(4, scn.GetShadowDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(denizen));

		// The reveal is done only to count: four cards pay "the top 5" with what there is (Ketura's ruling, Q-0378: A).
		assertTrue(scn.ShadowPlayAvailable(taunt));
		scn.ShadowPlayCard(taunt);
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		for (var card : deck) {
			assertTrue(scn.FreepsHasCardChoiceAvailable(card, false));
		}
		scn.DismissRevealedCards();

		// Three [orc] cards among the four, the only [orc] minion chosen unasked: 9 + 3.
		assertEquals(12, scn.GetStrength(denizen));
		assertInDiscard(taunt);
		assertEquals(4, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOrcMinionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taunt = scn.GetShadowCard("taunt");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(taunt);
		var deck = new PhysicalCardImpl[]{
				scn.GetShadowCard("deckdenizen"), scn.GetShadowCard("deckspear"), scn.GetShadowCard("deckbeckoning"),
				scn.GetShadowCard("deckspear2"), scn.GetShadowCard("deckdenizen2")};
		scn.SetShadowDrawDeck(deck);
		scn.MoveMinionsToTable(troop);
		assertEquals(5, scn.GetShadowDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowPlayAvailable(taunt));
		scn.ShadowPlayCard(taunt);
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Five [orc] cards revealed, and no [orc] minion to make stronger: the [moria] Orc is as it was.
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(taunt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
