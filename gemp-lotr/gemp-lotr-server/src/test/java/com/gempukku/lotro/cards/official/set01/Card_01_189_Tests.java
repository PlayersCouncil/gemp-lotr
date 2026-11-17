package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_189_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lost", "1_189");

					// [moria] minions, counted by the spot
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play trigger never fires, as it is cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is no Orc
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, Archer and nothing else

					// Not [moria] minions
					put("gollum", "9_28");      // Gollum, Dark as Darkness: a [gollum] minion; "Shadow: Exert Gollum twice to exert the Ring-bearer"
					put("longdark", "1_188");   // The Long Dark: a [moria] condition; its trigger answers moves to sites 4 and 5 only

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; "Fellowship: Exert Sam to remove a burden"

					// Cards laid on top of the Shadow player's draw deck.
					put("decktroop", "1_177");  // a Shadow minion
					put("sword", "1_160");      // Uruk-hai Sword: a Shadow possession
					put("hunt", "1_126");       // Hunt Them Down!: a Shadow event
					put("underdeeps", "1_200"); // The Underdeeps of Moria: a Shadow condition
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card
					put("hunt2", "1_126");      // a Shadow card already in the discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LosttotheGoblinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lost to the Goblins
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the Ring-bearer exerts or takes a wound, discard a card from the top of your draw deck for each [moria] minion you spot. For each Shadow card discarded in this way, add a burden (limit 3 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lost");

		assertEquals("Lost to the Goblins", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenTheRingBearerExertsItDiscardsOneCardForEachMoriaMinionSpottedAndAddsABurdenForEachShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lost = scn.GetShadowCard("lost");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var gollum = scn.GetShadowCard("gollum");
		var longdark = scn.GetShadowCard("longdark");
		var hunt = scn.GetShadowCard("hunt");
		var aragorn = scn.GetShadowCard("aragorn");
		var sword = scn.GetShadowCard("sword");
		var decktroop = scn.GetShadowCard("decktroop");
		var hunt2 = scn.GetShadowCard("hunt2");
		scn.MoveCardsToHand(lost);

		scn.StartGame();
		// Three [moria] minions (two Orcs and a Troll), beside a [gollum] minion and a [moria] condition, neither of
		// which is a [moria] minion.
		scn.MoveMinionsToTable(runner, troop, troll, gollum);
		scn.MoveCardsToSupportArea(longdark);
		scn.SetShadowDrawDeck(hunt, aragorn, sword, decktroop);
		scn.MoveCardsToDiscard(hunt2);
		assertEquals(4, scn.GetShadowDeckCount());
		assertInDiscard(hunt2);
		assertInHand(lost);
		int freepsDeck = scn.GetFreepsDeckCount();
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.ShadowUseCardAction(gollum, "Exert the Ring-bearer");
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(lost));
		scn.ShadowPlayCard(lost);

		// The Shadow player names how many [moria] minions he spots, from none to the three there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		// The top three cards of his own deck are discarded and the fourth stays; the Free Peoples deck is untouched.
		assertInDiscard(hunt, aragorn, sword);
		assertInZone(Zone.DECK, decktroop);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		// An event and a possession are Shadow cards; Aragorn is not, nor is the Hunt Them Down! that was already in
		// the discard pile, nor Lost to the Goblins itself.
		assertEquals(2, scn.FreepsGetBurdens());
		assertInDiscard(lost);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheRingBearerTakesAnArcheryWoundTheShadowPlayerMaySpotFewerMoriaMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var lost = scn.GetShadowCard("lost");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		var decktroop = scn.GetShadowCard("decktroop");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(lost);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop);
		scn.SetShadowDrawDeck(decktroop, hunt);
		assertEquals(2, scn.GetShadowDeckCount());
		assertInHand(lost);
		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());

		// The Marksman's archery wound, placed by the Free Peoples player on Frodo.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(lost));
		scn.ShadowPlayCard(lost);

		// Two [moria] minions; he spots one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		// One card discarded, a Shadow minion: one burden.
		assertInDiscard(decktroop);
		assertInZone(Zone.DECK, hunt);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertInDiscard(lost);
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotPlayableWhenAnotherRingBoundCompanionTakesTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var lost = scn.GetShadowCard("lost");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(lost);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop);
		int shadowDeck = scn.GetShadowDeckCount();
		assertInHand(lost);
		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Sam is not the Ring-bearer: the Shadow player is asked nothing and the game goes on to the assignments.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(lost);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
	}

	@Test
	public void NotPlayableWhenAnotherRingBoundCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var lost = scn.GetShadowCard("lost");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(lost);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troll);
		scn.AddBurdens(1);
		int shadowDeck = scn.GetShadowDeckCount();
		assertInHand(lost);
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());

		// Sam is not the Ring-bearer: the Shadow player is asked nothing (an offer would have stopped the game at his
		// optional responses) and the Free Peoples player takes his next fellowship action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(lost);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void AddsNoMoreThanThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lost = scn.GetShadowCard("lost");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var marksman = scn.GetShadowCard("marksman");
		var gollum = scn.GetShadowCard("gollum");
		var decktroop = scn.GetShadowCard("decktroop");
		var underdeeps = scn.GetShadowCard("underdeeps");
		var hunt = scn.GetShadowCard("hunt");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(lost);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, troll, marksman, gollum);
		scn.SetShadowDrawDeck(decktroop, underdeeps, hunt, sword, aragorn);
		assertEquals(5, scn.GetShadowDeckCount());
		assertInHand(lost);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowUseCardAction(gollum, "Exert the Ring-bearer");
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard(lost);
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("4");

		// Four Shadow cards (a minion, a condition, an event and a possession) discarded: three burdens.
		assertInDiscard(decktroop, underdeeps, hunt, sword);
		assertInZone(Zone.DECK, aragorn);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(3, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
