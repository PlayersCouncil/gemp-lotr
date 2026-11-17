package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_313_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "1_313");
					// Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo; his abilities are never used
					put("sam", "1_311");

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc
					put("hunters", "1_267");    // Orc Hunters: [sauron] Orc
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, not an Orc
					put("scimitar", "1_180");   // Goblin Scimitar: a [moria] possession
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition
					// No Business of Ours: [isengard] support-area condition, "The Free Peoples player may not look
					// at or reveal cards in any Shadow player's hand."
					put("business", "2_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "1_313");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play beside
					// another Ring-bearer (Q-0159); cost 2, strength 3, vitality 4, and his texts want more minions than
					// companions, or a skirmish he is not in
					put("frodo", "13_149");
					// Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo; his abilities are never used
					put("sam", "1_311");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sting
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Frodo.
		 * Fellowship or Regroup: Exert Frodo to reveal an opponent's hand. Remove (1) for each Orc revealed
		 * (limit (4)).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnFrodoTheRingBearerAndNotOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sting));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAFrodoWhoIsNotTheRingBearerAndNotOnTheRingBearerOrSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(frodo, sting);

		scn.StartGame();

		// Galadriel the Ring-bearer and Sam, a Ring-bound Hobbit, and no Frodo: nobody may bear it.
		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertFalse(scn.FreepsPlayAvailable(sting));

		scn.FreepsPlayCard(frodo);
		assertTrue(scn.FreepsPlayAvailable(sting));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityExertsTheFrodoWhoBearsItAndNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(frodo, sam);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityRevealsTheWholeShadowHandAndRemovesOneForEachOrcOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var mytroop = scn.GetFreepsCard("troop");
		// Three Orcs of three cultures; a minion who is no Orc; a [moria] card and a [sauron] card that are no Orcs.
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		var creeper = scn.GetShadowCard("creeper");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		// An Orc left in the Shadow player's draw deck
		var hunters = scn.GetShadowCard("hunters");
		scn.AttachCardsTo(frodo, sting);
		// An Orc in the Free Peoples player's own hand, of a title the Shadow hand does not hold.
		scn.MoveCardsToHand(mytroop);
		scn.MoveCardsToHand(runner, soldier, creeper, savage, scimitar, fear);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.MarkGameLog();
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The whole Shadow hand is shown, and only to the Free Peoples player.
		assertEquals(6, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(creeper, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(mytroop, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(hunters, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner, soldier, creeper, savage, scimitar, fear));
		scn.FreepsDismissRevealedCards();

		// Three Orcs of three cultures; the Uruk-hai, the possession, the condition, the Orc in the deck and the
		// Free Peoples player's own Orc do not count: 5 - 3.
		assertEquals(2, scn.GetTwilight());
		assertEquals(6, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, mytroop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FiveOrcsRevealedRemoveNoMoreThan4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var hunters = scn.GetShadowCard("hunters");
		var creeper = scn.GetShadowCard("creeper");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner, troop, soldier, hunters, creeper);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsUseCardAction(sting);
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(frodo));
		// 5 Orcs revealed, but no more than (4) removed: 10 - 4.
		assertEquals(6, scn.GetTwilight());
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LimitOf4HoldsForTheWholePhaseAndRenewsInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner1, runner2, runner3);

		scn.StartGame();
		scn.SetTwilight(10);

		// Three Orcs: 10 - 3.
		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();
		assertEquals(7, scn.GetTwilight());

		// The same three again, and only one is left to the limit of 4 for the phase: 7 - 1.
		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();
		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(frodo));

		// At the limit: still offered, Frodo still exerts and the hand is still revealed, but nothing more is removed.
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(6, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// No minions, so the turn goes straight on to the regroup phase, where the limit is fresh.
		scn.RemoveWoundsFromChar(frodo, 3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(frodo));
		// 10 - 3
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WhileTheHandMayNotBeRevealedFrodoStillExertsAndNothingIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var business = scn.GetShadowCard("business");
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(runner, soldier, savage);

		scn.StartGame();
		scn.SetTwilight(5);

		// The ability may be used whatever it will do; the reveal is its effect, and that is what is barred.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Two Orcs are in that hand, and neither was revealed.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(5, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWhenFrodoCannotExertNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: exhausted.  Sam, who could exert, is not Frodo.
		assertFalse(scn.FreepsActionAvailable(sting));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RemoveWoundsFromChar(frodo, 3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsActionAvailable(sting));
	}
}
