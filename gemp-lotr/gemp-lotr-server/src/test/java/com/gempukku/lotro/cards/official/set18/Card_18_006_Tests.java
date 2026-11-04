package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "18_6");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, and Arwen
					put("ladyarwen", "102_6");  // Arwen, Lady of Rivendell: the Arwen who is an ally (her text reads only Aragorn)
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an [elven] companion who is not Arwen
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion of another title
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] ally
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of another culture
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					// Six cards of six different blueprints for the top of the draw deck.
					put("troop", "1_177");      // Goblin Patrol Troop
					put("savage", "1_151");     // Uruk Savage
					put("grimir", "1_17");      // Grimir, Dwarven Elder
					put("elvensword", "4_64");  // Elven Sword
					put("blade", "11_16");      // Blade of Lindon
					put("axe", "1_9");          // Dwarven Axe
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A draw deck of seven known cards: the six named last and the Free Peoples player's own Goblin Runner.
	protected VirtualTableScenario GetDrawDeckOfSevenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "18_6");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, and Arwen
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("armor", "1_8");        // Dwarven Armor
					put("sleep", "1_84");       // Sleep, Caradhras
					put("sword", "1_299");      // Hobbit Sword
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BacktotheLightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Back to the Light
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: To play, exert an [elven] companion or spot Arwen. Examine the top 5 cards of your draw deck and place any number of them aside. Shuffle the remaining cards into your draw deck and place those cards set aside on top of your draw deck in any order.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("light");

		assertEquals("Back to the Light", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheCardsSetAsideGoBackOnTopOfTheDrawDeckInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawDeckOfSevenScenario();

		var light = scn.GetFreepsCard("light");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var sword = scn.GetFreepsCard("sword");
		var hisRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// From the top: Aragorn, Gimli, Legolas, Dwarven Armor, Sleep, Caradhras; then Hobbit Sword and the
		// Goblin Runner, which are not among the top five.
		scn.MoveCardsToTopOfDeck(hisRunner, sword, sleep, armor, legolas, gimli, aragorn);
		assertInZone(Zone.DECK, aragorn, gimli, legolas, armor, sleep, sword, hisRunner);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetFreepsDeckCount());
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(arwen));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(light));

		// Arwen is an [elven] companion and Arwen: the player says which way he pays, and spotting costs her
		// nothing.
		scn.FreepsPlayCard(light);
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Spot Arwen"));
		scn.FreepsChooseOption("Spot Arwen");
		assertEquals(0, scn.GetWoundsOn(arwen));

		// The top five are his to choose from, any number of them; the other player is shown nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, legolas, armor, sleep));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, hisRunner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(5, scn.FreepsGetChoiceMax());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// Two are set aside and the deck is shuffled once.
		scn.FreepsChooseCardBPFromSelection(legolas, sleep);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		// The game asks which card to put on top of the deck, one card at a time: the card named first is put
		// down first, and the last, which is not asked for, lies on it.
		assertTrue(scn.FreepsDecisionAvailable("Choose card to put on top of deck"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, sleep));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, armor));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(legolas);
		assertSame(sleep, scn.GetFreepsTopOfDeck());
		assertSame(legolas, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(7, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, aragorn, gimli, armor, sword, hisRunner);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(light);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	// Ketura on Q-0176: a card moved from a draw deck where the text reveals nothing is not named in the game
	// log.  The text has the player "examine" the five.  The log used to name each card set aside (F-0378).
	@Test
	public void TheCardsSetAsideAreNotNamedInTheGameLog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawDeckOfSevenScenario();

		var light = scn.GetFreepsCard("light");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var sword = scn.GetFreepsCard("sword");
		var hisRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(hisRunner, sword, sleep, armor, legolas, gimli, aragorn);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.MarkGameLog();
		scn.FreepsPlayCard(light);
		scn.FreepsChooseOption("Spot Arwen");
		scn.FreepsChooseCardBPFromSelection(legolas, sleep);
		scn.FreepsChooseCardBPFromSelection(legolas);

		// The event is named as it is played, and that is all the other player learns: not which cards were
		// set aside, any more than the three that were not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(light));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(legolas));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(sleep));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(aragorn));
		assertSame(sleep, scn.GetFreepsTopOfDeck());
		assertSame(legolas, scn.GetFromTopOfFreepsDeck(2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoElvenCompanionCanExertAndNoArwenIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(haldir, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 3 with 2 wounds: Haldir, the only [elven] companion, cannot exert. The [elven] ally and the Dwarf could.
		scn.AddWoundsToChar(haldir, 2);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(light);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(light));
	}

	@Test
	public void PlayedBySpottingAnExhaustedArwenItExertsNobodyAndSettingNoneAsideStillShufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		var grimir = scn.GetFreepsCard("grimir");
		var elvensword = scn.GetFreepsCard("elvensword");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(blade, elvensword, grimir, savage, troop);
		assertInZone(Zone.DECK, troop, savage, grimir, elvensword, blade);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		scn.MoveMinionsToTable(runner);
		// Both [elven] companions are exhausted: only the spot of Arwen can be met.
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(haldir, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(haldir));
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, grimir, elvensword, blade));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		// Nothing set aside: the five go back into the deck, which is shuffled all the same.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, troop, savage, grimir, elvensword, blade);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(light);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ArwenWhoIsAnAllyIsSpottedAndAllFiveSetAsideGoBackOnTopInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var ladyarwen = scn.GetFreepsCard("ladyarwen");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		var grimir = scn.GetFreepsCard("grimir");
		var elvensword = scn.GetFreepsCard("elvensword");
		var blade = scn.GetFreepsCard("blade");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveAlliesToTable(ladyarwen);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// From the top: the Troop, the Savage, Grimir, Elven Sword, Blade of Lindon; sixth the Dwarven Axe.
		scn.MoveCardsToTopOfDeck(axe, blade, elvensword, grimir, savage, troop);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertSame(axe, scn.GetFromTopOfFreepsDeck(6));
		scn.MoveMinionsToTable(runner);
		// Haldir, the only [elven] companion, is exhausted: the ally Arwen is what is spotted.
		scn.AddWoundsToChar(haldir, 2);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(ladyarwen));
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertEquals(5, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(troop, savage, grimir, elvensword, blade);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		// Put down one at a time, the first named deepest: Blade, Troop, Grimir, Savage, then Elven Sword on top.
		scn.FreepsChooseCardBPFromSelection(blade);
		scn.FreepsChooseCardBPFromSelection(troop);
		scn.FreepsChooseCardBPFromSelection(grimir);
		scn.FreepsChooseCardBPFromSelection(savage);

		assertSame(elvensword, scn.GetFreepsTopOfDeck());
		assertSame(savage, scn.GetFromTopOfFreepsDeck(2));
		assertSame(grimir, scn.GetFromTopOfFreepsDeck(3));
		assertSame(troop, scn.GetFromTopOfFreepsDeck(4));
		assertSame(blade, scn.GetFromTopOfFreepsDeck(5));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetWoundsOn(ladyarwen));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertInDiscard(light);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoArwenToSpotAnyElvenCompanionMayBeExertedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		var grimir = scn.GetFreepsCard("grimir");
		var elvensword = scn.GetFreepsCard("elvensword");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(haldir, lorienelf, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(light);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(blade, elvensword, grimir, savage, troop);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		// Either [elven] companion; not the [elven] ally, the Dwarf or the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, lorienelf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// One card set aside is the top of the deck, the only one put back on it, with nothing asked.
		scn.FreepsChooseCardBPFromSelection(grimir);
		assertSame(grimir, scn.GetFreepsTopOfDeck());
		assertInDiscard(light);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
