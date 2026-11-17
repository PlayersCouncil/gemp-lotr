package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("might", "3_43");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; his text is about his play and a skirmish
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: [elven] companion; strength +3 only against a Nazgul
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion; skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally, no companion; maneuver ability needs initiative
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; his archery ability is used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
					put("runner", "1_178");     // Goblin Runner: minion, twilight cost 1
					put("wallcrawler", "1_184");// Goblin Wallcrawler: minion, twilight cost 2
					put("wreathed", "1_238");   // Wreathed in Shadow: Shadow condition, twilight cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Weatherhills as site 2: "Each Nazgul's twilight cost is -1."
	protected VirtualTableScenario GetWeatherhillsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("might", "3_43");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("sam", "1_311");        // Sam, Son of Hamfast: fellowship ability and a response to Frodo's death only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul, cost 4; her text is about [wraith] conditions
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_335");      // Weatherhills
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MightofNumenorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Might of Númenor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Tale</b>.
		 * <b>Maneuver:</b> Spot a [gondor] companion to reveal a card at random from an opponent's hand. Heal X companions, where X is the twilight cost of the card revealed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("might");

		assertEquals("Might of Númenor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(might);
		// A [gondor] ally and a companion of another culture: no [gondor] companion.
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(might));
		assertInZone(Zone.HAND, runner);
	}

	@Test
	public void RevealsACardAtRandomToItsPlayerAndHealsAsManyCompanionsAsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCardsToHand(might, gimli);
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(wallcrawler);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(orophin, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(might));

		scn.FreepsPlayCard(might);
		// The Shadow player's one card is shown to the Free Peoples player, and nothing to the hand's owner.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(wallcrawler, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// The Wallcrawler costs 2: two companions, and only companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, legolas);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(troop));
		// The card revealed stays in the hand it was shown from.
		assertInZone(Zone.HAND, wallcrawler);
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, might);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingAGondorHobbitACardOfCostOneHealsOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var pippin = scn.GetFreepsCard("pippin");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(might);
		// Pippin is the only [gondor] companion.
		scn.MoveCompanionsToTable(pippin, legolas);
		scn.MoveCardsToHand(runner);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(might));
		scn.FreepsPlayCard(might);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		scn.FreepsDismissRevealedCards();

		// The Runner costs 1.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(pippin);

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACardOfCostZeroHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToHand(might);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wreathed);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(might);
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		scn.FreepsDismissRevealedCards();

		// X is 0: nobody is chosen and nobody is healed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, might);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheNumberHealedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCardsToHand(might);
		scn.MoveCompanionsToTable(aragorn, legolas);
		// Two cards of different costs, 1 and 2, and three wounded companions, so that either is asked as a choice.
		scn.MoveCardsToHand(runner, wallcrawler);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPlayCard(might);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var runnerShown = scn.FreepsHasCardChoiceAvailable(runner, false);
		var wallcrawlerShown = scn.FreepsHasCardChoiceAvailable(wallcrawler, false);
		assertNotEquals(runnerShown, wallcrawlerShown);
		scn.FreepsDismissRevealedCards();

		var expected = runnerShown ? 1 : 2;
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas, frodo));
		assertEquals(expected, scn.FreepsGetChoiceMin());
		assertEquals(expected, scn.FreepsGetChoiceMax());
		// The game rests on the choice of whom to heal.
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to heal"));
	}

	@Test
	public void WithAnEmptyHandNothingIsRevealedAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(might);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetShadowHandCount());
		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(might));

		scn.MarkGameLog();
		scn.FreepsPlayCard(might);

		// Nobody is shown anything, the log says why, and nobody is healed.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, might);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughASiteMakesItCheaperToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeatherhillsScenario();

		var might = scn.GetFreepsCard("might");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(might);
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, sam);
		scn.MoveCardsToHand(lemenya);
		// Five wounded companions, so that 3 and 4 are told apart.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertEquals(4, scn.GetTwilightCost(lemenya));
		scn.SkipToPhase(Phase.MANEUVER);

		// At Weatherhills a Nazgul costs 1 less to play.
		assertEquals(3, scn.GetTwilightCost(lemenya));

		scn.FreepsPlayCard(might);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, false));
		scn.FreepsDismissRevealedCards();

		// X is 4: a card in play changes another card's cost only as that card is played.
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, legolas, gimli, sam);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
