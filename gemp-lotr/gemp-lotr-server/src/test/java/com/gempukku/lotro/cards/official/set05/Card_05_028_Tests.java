package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_28");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					// The minions, laid as the Shadow phase would lay them and left unassigned, so that they stand in the regroup phase.
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, vitality 3; its texts are about unbound Hobbits, none in play
					put("gollum", "5_25");      // Gollum, Stinker: the [gollum] Gollum, vitality 4; strength +1 for each burden, a trigger on his own win
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; a Nazgûl-play trigger and an assignment ability
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, vitality 8
					// Cards for the top of the draw deck, whose texts do nothing there.
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession
					put("drums", "1_168");      // Drums in the Deep: a Shadow event
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Free Peoples companion
					put("sword", "1_112");      // Ranger's Sword: a Free Peoples possession
					put("axestrike", "1_3");    // Axe Strike: a Free Peoples event
					put("sam", "1_311");        // Sam, Son of Hamfast: a Free Peoples companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sméagol, Old Noser
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.
		 * 		<b>Regroup:</b> Exert Sméagol (or Gollum) twice to reveal the top 4 cards of your draw deck. Wound a minion
		 * 		for each Shadow card revealed. Place those 4 cards beneath your draw deck in any order.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Old Noser", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		// CR 4.2 "Gollum/Sméagol": he has no race.
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, smeagol);
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityRevealsFourAndWoundsAMinionForEachShadowCardThenPutsThemBeneathTheDeckInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetFreepsCard("runner");
		var scimitar = scn.GetFreepsCard("scimitar");
		var drums = scn.GetFreepsCard("drums");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, pursuer);
		// Vitality 3 with 2 wounds: one more wound kills this Troop.
		scn.AddWoundsToChar(troop2, 2);
		// Three Shadow cards of three types, and a Free Peoples card; the Runner on top.
		scn.MoveCardsToTopOfDeck(sword, drums, scimitar, runner);
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertInZone(Zone.DECK, runner, scimitar, drums, sword);
		assertSame(runner, scn.GetFreepsTopOfDeck());
		assertSame(scimitar, scn.GetFromTopOfFreepsDeck(2));
		assertSame(drums, scn.GetFromTopOfFreepsDeck(3));
		assertSame(sword, scn.GetFromTopOfFreepsDeck(4));

		scn.SkipToPhase(Phase.REGROUP);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		// Sméagol is the only one who can pay, so nobody is asked.
		assertEquals(2, scn.GetWoundsOn(smeagol));

		// The four are shown to both players, and none can be chosen.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(drums, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, false));
		scn.DismissRevealedCards();

		// Three Shadow cards: three wounds, each on a minion of his choice, the same one more than once if he likes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, pursuer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pursuer);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, pursuer));
		scn.FreepsChooseCard(pursuer);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, pursuer));
		scn.FreepsChooseCard(troop2);
		// Exhausted, the second Troop is killed by the wound.
		assertInDiscard(troop2);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The order beneath the deck is his: the first chosen lies highest, the last, unasked, at the very bottom.
		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseCardBPFromSelection(scimitar);
		scn.FreepsChooseCardBPFromSelection(drums);
		assertSame(sword, scn.GetFreepsBottomOfDeck());
		assertSame(drums, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(scimitar, scn.GetFromBottomOfFreepsDeck(3));
		assertSame(runner, scn.GetFromBottomOfFreepsDeck(4));
		assertInZone(Zone.DECK, runner, scimitar, drums, sword);
		assertNotSame(runner, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithSmeagolUnableTheShadowPlayersGollumExertsTwiceAndOneShadowCardWoundsOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		var troop1 = scn.GetShadowCard("troop1");
		var runner = scn.GetFreepsCard("runner");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop1);
		// Vitality 4 with 2 wounds: Sméagol has one exertion left, not two.
		scn.AddWoundsToChar(smeagol, 2);
		// One Shadow card among three Free Peoples cards: a companion, a possession and an event.
		scn.MoveCardsToTopOfDeck(gimli, sword, axestrike, runner);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(runner, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		// The Gollum is the only one who can pay, so nobody is asked.
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetWoundsOn(smeagol));
		scn.DismissRevealedCards();

		// One wound, on a minion of his choice: the Gollum is a minion too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, troop1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(gollum));

		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseCardBPFromSelection(gimli);
		scn.FreepsChooseCardBPFromSelection(sword);
		assertSame(axestrike, scn.GetFreepsBottomOfDeck());
		assertSame(runner, scn.GetFromBottomOfFreepsDeck(4));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithSmeagolUnableTheWraithGollumExertsTwiceToPayToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetFreepsCard("runner");
		var scimitar = scn.GetFreepsCard("scimitar");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(wraithgollum, pursuer);
		scn.AddWoundsToChar(smeagol, 2);
		scn.MoveCardsToTopOfDeck(gimli, sword, scimitar, runner);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(runner, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(2, scn.GetWoundsOn(wraithgollum));
		assertEquals(2, scn.GetWoundsOn(smeagol));
		scn.DismissRevealedCards();

		// Two Shadow cards: two wounds, both on the Pursuer.
		scn.FreepsChooseCard(pursuer);
		scn.FreepsChooseCard(pursuer);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(2, scn.GetWoundsOn(wraithgollum));

		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseCardBPFromSelection(scimitar);
		scn.FreepsChooseCardBPFromSelection(sword);
		assertSame(gimli, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithSmeagolAndGollumBothAbleTheFreePeoplesPlayerChoosesWhoExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var axestrike = scn.GetFreepsCard("axestrike");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		// Four Free Peoples cards: nothing to wound for.
		scn.MoveCardsToTopOfDeck(gimli, sword, axestrike, sam);
		assertSame(sam, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		// Sméagol and the Gollum can each pay; Shelob, a [gollum] minion, and the other companions cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, gollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shelob, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gollum);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(shelob));
		scn.DismissRevealedCards();

		// No Shadow card revealed: no wound is asked, and the four go beneath the deck.
		scn.FreepsChooseCardBPFromSelection(sam);
		scn.FreepsChooseCardBPFromSelection(axestrike);
		scn.FreepsChooseCardBPFromSelection(sword);
		assertSame(gimli, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheShadowCardsRevealedWoundNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var scimitar = scn.GetFreepsCard("scimitar");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		// Vitality 4 with 1 wound: just enough to exert twice.
		scn.AddWoundsToChar(smeagol, 1);
		scn.MoveCardsToTopOfDeck(gimli, sword, scimitar, runner);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(runner, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(3, scn.GetWoundsOn(smeagol));
		scn.DismissRevealedCards();

		// Two Shadow cards and no minion: no wound is placed, on a companion or anyone.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseCardBPFromSelection(scimitar);
		scn.FreepsChooseCardBPFromSelection(sword);
		assertSame(gimli, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNeitherSmeagolNorAGollumCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		// Shelob, a [gollum] minion who is no Gollum, could exert twice; Sméagol, vitality 4 with 2 wounds, cannot.
		scn.MoveMinionsToTable(shelob);
		scn.AddWoundsToChar(smeagol, 2);
		assertEquals(2, scn.GetWoundsOn(smeagol));

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}
}
