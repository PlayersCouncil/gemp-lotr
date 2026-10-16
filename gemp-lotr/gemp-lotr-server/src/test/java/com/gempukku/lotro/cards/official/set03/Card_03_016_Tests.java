package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "3_16");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] ally, home 6, vitality 3; an archery ability only
					put("rumil", "1_57");       // Rumil, Elven Protector: [elven] ally, home 6, vitality 3; an archery ability only
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally, home 3, vitality 3; a skirmish ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, no ally; +3 only against a Nazgul
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a card for the Free Peoples player's own hand

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("runner", "1_178");     // Goblin Runner: minion, twilight cost 1
					put("wallcrawler", "1_184");// Goblin Wallcrawler: minion, twilight cost 2
					put("wreathed", "1_238");   // Wreathed in Shadow: Shadow condition, twilight cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path, whose site 1 is Edoras Hall: "The twilight cost of each [rohan] possession is -1."
	protected VirtualTableScenario GetEdorasHallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "3_16");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] ally, vitality 3
					put("rumil", "1_57");       // Rumil, Elven Protector: [elven] ally, vitality 3
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally, vitality 3
					// Sturdy Shield: a [rohan] possession of twilight cost 2, in the Shadow player's hand.  Its text
					// is about a bearer it never has here.
					put("shield", "15_141");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FriendsofOldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Friends of Old
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Reveal a card at random from an opponent's hand. Heal X [elven] allies, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friends");

		assertEquals("Friends of Old", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.AddWoundsToChar(orophin, 1);
		assertEquals(1, scn.GetWoundsOn(orophin));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// A card in the Shadow hand and a wounded [elven] ally: only the phase is wrong.
		assertInZone(Zone.HAND, friends);
		assertInZone(Zone.HAND, runner);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(friends));
	}

	@Test
	public void RevealsACardAtRandomToItsPlayerAndHealsAsManyElvenAlliesAsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCardsToHand(friends, gimli);
		scn.MoveAlliesToTable(orophin, rumil, golradir, grimir);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(wallcrawler);

		scn.StartGame();
		// Two wounds on each [elven] ally, so that one healed reads 1; the others are wounded too.
		scn.AddWoundsToChar(orophin, 2);
		scn.AddWoundsToChar(rumil, 2);
		scn.AddWoundsToChar(golradir, 2);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.HAND, wallcrawler);
		assertInZone(Zone.HAND, gimli);
		assertTrue(scn.FreepsPlayAvailable(friends));

		scn.FreepsPlayCard(friends);
		// The Shadow player's one card is shown to the Free Peoples player, his own hand is not, and the hand's
		// owner is shown nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(wallcrawler, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// The Wallcrawler costs 2: two [elven] allies, and only [elven] allies.
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, rumil, golradir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, grimir, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(orophin, golradir);

		// One wound off each ally chosen.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The card revealed stays in the hand it was shown from.
		assertInZone(Zone.HAND, wallcrawler);
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfCostOneHealsOneElvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var golradir = scn.GetFreepsCard("golradir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin, golradir);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.AddWoundsToChar(orophin, 2);
		scn.AddWoundsToChar(golradir, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(golradir));

		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		scn.FreepsDismissRevealedCards();

		// The Runner costs 1.
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, golradir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(golradir);

		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfCostZeroHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var golradir = scn.GetFreepsCard("golradir");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin, golradir);
		scn.MoveCardsToHand(wreathed);

		scn.StartGame();
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(golradir));

		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		scn.FreepsDismissRevealedCards();

		// X is 0: nobody is chosen and nobody is healed.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertInZone(Zone.DISCARD, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheNumberHealedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		var runner = scn.GetShadowCard("runner");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin, rumil, golradir);
		// Two cards of different costs, 1 and 2, and three wounded [elven] allies, so that either is asked as a choice.
		scn.MoveCardsToHand(runner, wallcrawler);

		scn.StartGame();
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPlayCard(friends);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var runnerShown = scn.FreepsHasCardChoiceAvailable(runner, false);
		var wallcrawlerShown = scn.FreepsHasCardChoiceAvailable(wallcrawler, false);
		assertNotEquals(runnerShown, wallcrawlerShown);
		scn.FreepsDismissRevealedCards();

		var expected = runnerShown ? 1 : 2;
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, rumil, golradir));
		assertEquals(expected, scn.FreepsGetChoiceMin());
		assertEquals(expected, scn.FreepsGetChoiceMax());
		// The game rests on the choice of whom to heal.
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to heal"));
	}

	@Test
	public void IsNotPlayableWithTheShadowHandEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var golradir = scn.GetFreepsCard("golradir");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin, golradir);

		scn.StartGame();
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		// Wounded [elven] allies, and no card in the Shadow hand to reveal: the reveal must be able to happen for
		// the heal to read it, so the event is not playable (Q-0336: B).
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsPlayAvailable(friends));

		// One card there, of cost 0, and it is playable.
		scn.MoveCardsToHand(wreathed);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(friends));
		assertInZone(Zone.HAND, friends);
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughASiteMakesItCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEdorasHallScenario();

		var friends = scn.GetFreepsCard("friends");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		var shield = scn.GetShadowCard("shield");
		scn.MoveCardsToHand(friends);
		scn.MoveAlliesToTable(orophin, rumil, golradir);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		// Three wounded [elven] allies, so that 1 and 2 are told apart.
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(golradir));
		// At Edoras Hall the Shield, printed 2, costs 1 to play.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(shield));

		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(shield, false));
		scn.FreepsDismissRevealedCards();

		// X is 2: a card in play changes another card's cost only as that card is played.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(orophin, rumil);

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
