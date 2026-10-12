package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A bar on looking at or revealing the cards in a hand stops every definition word that does either to another
 * player's hand.
 *
 * Two printed cards lay the bar, the definition word `cantLookOrRevealHand`: No Business of Ours 2_44 (and its
 * erratum 52_44), "The Free Peoples player may not look at or reveal cards in any Shadow player's hand", and
 * Erland 2_21, "Shadow players may not look at or reveal cards in your hand".  The bar is asked by each word that
 * looks at or reveals a hand for itself, and four words do it to another player's hand.  Each is here once, with
 * the one printed card that shows it: the act done without the bar, then the bar put into play between two uses
 * and the same act stopped.  Under the bar the action is still offered and its cost paid; nothing is shown and the
 * hand keeps its count.
 *
 * <ul>
 * <li>`revealHand`, the whole hand: Curse Their Foul Feet! 1_36, under No Business of Ours.</li>
 * <li>`lookAtHand`, the whole hand: Goblin Man 2_42, the Shadow player looking at the Free Peoples hand, under
 * Erland.  (Galadriel's Glade 1_351 looks at the Shadow hand with the same word; it is No Business of Ours's own
 * stopped card, in Card_02_044_Tests.)</li>
 * <li>`lookAtRandomCardsFromHand`: The Mirror of Galadriel 1_55, its one user, under No Business of Ours.  The
 * Mirror's "discard one" is a `discardFromHand` of the cards it has looked at, so it discards nothing either.</li>
 * <li>`revealRandomCardsFromHand`: Storm of Argument 3_6, under No Business of Ours.  The card on which F-0391 was
 * found: it was written with `revealCardsFromHand`, which asks nothing of the bar, until engine pass 26.</li>
 * </ul>
 *
 * Ketura on Q-0264 (2026-10-12): "I don't think that a card should necessarily take upon itself the burden of
 * checking that every possible engine route follows its own bar, but I *do* think that mechanic tests should exist
 * that do such a thing. ... one of each is a great idea, but they should be in mechanics tests, which will then
 * ensure that future cards using the bar only need to act as A."  So a card that lays this bar is tested with one
 * card that is stopped and its control, and this class holds the rest.
 *
 * Not here, and why.  `revealCardsFromHand` is the word for cards a player reveals from his OWN hand and asks
 * nothing of the bar; no definition gives it another player's hand (pitfalls, "A bar on looking at or revealing a
 * hand"), and No Business of Ours's own tests show the Free Peoples player still revealing from his own hand
 * under it.  `discardFromHand` asks the bar of a player who chooses from a hand that is not his, but only of what
 * it forecasts; every printed card that has one player discard from another's hand discards cards a look or a
 * reveal of the same card has already shown (the Mirror above) or the whole hand (Southern Spies 2_91), and no
 * printed card has a player choose from a hand he has not been shown.  Race Text 92_7's "You must play with your
 * hand revealed" is the owner's own showing, which neither bar reaches.
 */
public class ABarOnLookingAtOrRevealingAHandTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// No Business of Ours: "To play, spot an [isengard] minion.  Plays to your support area.  The Free
					// Peoples player may not look at or reveal cards in any Shadow player's hand."
					put("business", "2_44");
					// Erland, Advisor to Brand: an ally, strength 5, vitality 2.  "To play, spot Gandalf.  Shadow
					// players may not look at or reveal cards in your hand."
					put("erland", "2_21");

					// Curse Their Foul Feet!: "Fellowship: Exert an Elf to reveal an opponent's hand.  That player
					// discards a card from hand for each Orc revealed."
					put("curse1", "1_36");
					put("curse2", "1_36");
					// Goblin Man: an [isengard] Orc, vitality 2.  "Discard this minion if underground.  Shadow: Exert
					// this minion to look at the Free Peoples player's hand."
					put("goblinman1", "2_42");
					put("goblinman2", "2_42");
					// The Mirror of Galadriel: "Maneuver: If an opponent has at least 7 cards in hand, exert Galadriel
					// to look at 2 of those cards at random.  Discard one and replace the other."
					put("mirror", "1_55");
					// Storm of Argument: a [dwarven] Fellowship event, cost 1.  "Reveal a card at random from an
					// opponent's hand.  Shuffle up to X [dwarven] cards from your discard pile into your draw deck,
					// where X is the twilight cost of the card revealed."
					put("storm1", "3_6");
					put("storm2", "3_6");

					// Galadriel, Keeper of Nenya: an Elf ally, vitality 3; heals herself at the start of each turn;
					// a skirmish ability.
					put("galadriel", "6_18");
					put("uruk", "1_151");       // Uruk Savage: an Uruk-hai with damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, for a hand
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] condition, for a hand
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text, for a hand
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWholeHandRevealedIsNotShownUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// revealHand: Curse Their Foul Feet! 1_36, the Free Peoples player revealing the Shadow hand.
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		// The bar waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// No Orc in the Shadow player's hand, so the curse makes nobody discard.
		scn.MoveCardsToHand(uruk, armory);

		var galadriel = scn.GetFreepsCard("galadriel");
		var curse1 = scn.GetFreepsCard("curse1");
		var curse2 = scn.GetFreepsCard("curse2");
		scn.MoveCardsToHand(curse1, curse2);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();

		// Without the bar, the curse reveals the hand.
		scn.FreepsPlayCard(curse1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Hand"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// Under it, the curse is still played and its cost paid, and nothing is shown.
		assertTrue(scn.FreepsPlayAvailable(curse2));
		scn.FreepsPlayCard(curse2);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertInDiscard(curse2);
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheWholeHandLookedAtIsNotShownUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// lookAtHand: Goblin Man 2_42, the Shadow player looking at the Free Peoples hand, under Erland 2_21.
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var curse1 = scn.GetFreepsCard("curse1");
		var storm1 = scn.GetFreepsCard("storm1");
		// Erland waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(erland);
		scn.MoveCardsToHand(curse1, storm1);

		// Two copies, one for each look: vitality 2 lets each exert once.
		var goblinman1 = scn.GetShadowCard("goblinman1");
		var goblinman2 = scn.GetShadowCard("goblinman2");

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman1, goblinman2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());

		// Without the bar, the Goblin Man is shown the whole hand.
		scn.ShadowUseCardAction(goblinman1);
		assertEquals(1, scn.GetWoundsOn(goblinman1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(curse1, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(storm1, false));
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveAlliesToTable(erland);
		assertInZone(Zone.SUPPORT, erland);

		// Under it, the other Goblin Man is still offered and exerts, and nothing is shown.
		assertTrue(scn.ShadowActionAvailable(goblinman2));
		scn.ShadowUseCardAction(goblinman2);
		assertEquals(1, scn.GetWoundsOn(goblinman2));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CardsAtRandomLookedAtAreNotShownUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// lookAtRandomCardsFromHand: The Mirror of Galadriel 1_55, its one user.
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		// The bar waits in the discard pile, where the Mirror's random look and discard cannot reach it.
		scn.MoveCardsToDiscard(business);
		// Eight cards in hand, so that after one is discarded the Mirror's 7 are still there.
		scn.MoveCardsToHand(scn.GetShadowCard("goblinman1"), scn.GetShadowCard("runner"), scn.GetShadowCard("armory"),
				scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("mirror"),
				scn.GetShadowCard("curse1"), scn.GetShadowCard("curse2"));

		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(8, scn.GetShadowHandCount());

		// Without the bar, the Mirror looks at 2 cards and discards one.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseAny();
		assertEquals(7, scn.GetShadowHandCount());

		scn.ShadowPass();
		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// Under it, the Mirror is still used and its cost paid, and nothing is looked at or discarded.
		assertTrue(scn.FreepsActionAvailable(mirror));
		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertFalse(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(7, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACardAtRandomRevealedIsNotShownUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// revealRandomCardsFromHand: Storm of Argument 3_6.
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		// The bar waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// One card in the Shadow hand, so that the card revealed at random is not left to chance.
		scn.MoveCardsToHand(uruk);

		var storm1 = scn.GetFreepsCard("storm1");
		var storm2 = scn.GetFreepsCard("storm2");
		// No [dwarven] card in the Free Peoples discard pile for the event's second sentence to shuffle in, until
		// the first event is itself played.
		scn.MoveCardsToHand(storm1, storm2);

		scn.StartGame();

		// Without the bar, the event shows the one card.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(storm1);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInDiscard(storm1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// Under it, the event is still played and paid for, and nothing is shown.
		assertTrue(scn.FreepsPlayAvailable(storm2));
		scn.FreepsPlayCard(storm2);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInDiscard(storm2);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
