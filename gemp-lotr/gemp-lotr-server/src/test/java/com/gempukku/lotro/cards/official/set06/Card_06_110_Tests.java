package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burns", "6_110");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound
					// Sméagol, Poor Creature: [gollum] companion; to play, add a burden (cheated in); a skirmish
					// ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; to play, add a burden (cheated in); an assignment
					// ability used only on purpose
					put("smeagolshire", "103_116");
					// Gollum, Nasty Treacherous Creature: [gollum] minion with no race, vitality 4; a regroup
					// ability used only on purpose
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum; a trigger on playing a Nazgûl and an
					// assignment ability, neither reached here
					put("gollumwraith", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum
					put("shelob", "8_26");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion who is no Sam
					// Siege Troop: [sauron] Orc, vitality 4, so that three wounds leave him standing; a regroup
					// ability used only on purpose
					put("troop", "8_106");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc of vitality 1, killed by one wound
					// Cards for the Shadow hand, chosen by culture: never played
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria]
					put("marksman", "1_176");   // Goblin Marksman: [moria]
					put("assassin", "2_93");    // Tower Assassin: [sauron]
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], a Free Peoples card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItBurnsUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: It Burns Us
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot Sam and Sméagol (or Gollum) to choose an opponent who must reveal his or her hand. Wound a minion X times, where X is the number of different cultures revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("burns");

		assertEquals("It Burns Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingSamAndSmeagolRevealsTheShadowHandAndWoundsAMinionOnceForEachCultureAmongItsCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var patrol = scn.GetShadowCard("patrol");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToHand(burns);
		// Two [moria] cards and a [gondor] Free Peoples card: two different cultures.
		scn.MoveCardsToHand(patrol, marksman, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(burns));
		scn.FreepsPlayCard(burns);

		// The whole Shadow hand is shown to the Free Peoples player, and nothing to its owner.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksman, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Any minion: the exhausted Goblin Runner as well, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// [moria] and [gondor]: two wounds, both on the one minion chosen.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInPlay(runner);
		assertInDiscard(burns);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithThreeCulturesRevealedBesideTheShireSmeagolTheMinionTakesThreeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagolshire");
		var troop = scn.GetShadowCard("troop");
		var patrol = scn.GetShadowCard("patrol");
		var assassin = scn.GetShadowCard("assassin");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToHand(burns);
		// [moria], [sauron] and [gondor].
		scn.MoveCardsToHand(patrol, assassin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(burns));
		scn.FreepsPlayCard(burns);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Siege Troop is the only minion, and is wounded unasked.
		assertEquals(3, scn.GetWoundsOn(troop));
		assertInPlay(troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SamAndTheShadowPlayersGollumAreSpottedInPlaceOfSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(burns);
		scn.MoveCardsToHand(patrol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(burns));
		scn.FreepsPlayCard(burns);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Minions of two cultures, one with a race and one with none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(gollum);

		// One culture revealed: one wound.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(burns);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheShadowHandEmptyItIsPlayedBesideTheWraithGollumAndWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollumwraith");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(burns);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsPlayAvailable(burns));
		scn.MarkGameLog();
		scn.FreepsPlayCard(burns);

		// Nothing to show and no culture to count: X is 0.
		assertFalse(scn.FreepsDecisionAvailable("Hand of"));
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertInDiscard(burns);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithSamBesideAGollumMinionOfAnotherTitleUntilSmeagolIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(burns);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(burns));

		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(burns));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithSmeagolBesideAShireCompanionWhoIsNoSamUntilSamIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burns = scn.GetFreepsCard("burns");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, merry);
		scn.MoveCardsToHand(burns);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(burns));

		scn.MoveCompanionsToTable(sam);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(burns));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
