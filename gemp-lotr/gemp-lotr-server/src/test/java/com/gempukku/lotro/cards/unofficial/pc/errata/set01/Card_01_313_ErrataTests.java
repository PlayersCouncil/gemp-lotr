package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_313_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "51_313");
					// Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo; his abilities are never used
					put("sam", "1_311");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc
					put("hunters", "1_267");    // Orc Hunters: [sauron] Orc
					put("scout", "1_191");      // Moria Scout: [moria] Orc
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, not an Orc
					put("scimitar", "1_180");   // Goblin Scimitar: a [moria] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "51_313");
					put("frodo", "13_149");     // Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play beside another Ring-bearer (Q-0159); strength 3, vitality 4, and his texts want more minions than companions, or a skirmish he is not in
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
		 * Fellowship or Regroup: Exert Frodo to reveal 4 cards at random from an opponent's hand. Remove (1) for
		 * each Orc revealed (limit (2)).
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

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sting));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(sting);
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(sting, frodo);
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAFrodoWhoIsNotTheRingBearerAndNotOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(frodo, sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertTrue(scn.FreepsPlayAvailable(sting));

		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityExertsTheFrodoWhoBearsItAndNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(frodo);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsFrodoToRevealTheShadowHandAndRemovesOneForEachOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var mytroop = scn.GetFreepsCard("troop");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.AttachCardsTo(frodo, sting);
		// An Orc in the Free Peoples player's own hand, of a title the Shadow hand does not hold.
		scn.MoveCardsToHand(mytroop);
		scn.MoveCardsToHand(runner, soldier, savage, scimitar);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The Shadow hand has 4 cards: all are shown, and only to the Free Peoples player.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(mytroop, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Two Orcs of two cultures; the Uruk-hai, the possession and the Free Peoples player's own Orc do not count.
		assertEquals(3, scn.GetTwilight());
		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, mytroop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneOrcRevealedRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner, savage, scimitar);

		scn.StartGame();
		scn.SetTwilight(5);

		// One Orc beside an Uruk-hai and a [moria] possession: neither the race nor the culture counts for it.
		scn.FreepsUseCardAction(sting);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealsOnly4CardsAndRemovesNoMoreThan2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var hunters = scn.GetShadowCard("hunters");
		var scout = scn.GetShadowCard("scout");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner, troop, soldier, hunters, scout);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsUseCardAction(sting);
		// 5 Orcs in hand, 4 of them shown at random.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(frodo));
		// 4 Orcs revealed, but no more than (2) removed.
		assertEquals(8, scn.GetTwilight());
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LimitOf2HoldsForTheWholePhaseAndRenewsInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();
		assertEquals(9, scn.GetTwilight());

		scn.FreepsUseCardAction(sting);
		scn.FreepsDismissRevealedCards();
		assertEquals(8, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(frodo));

		// At the limit: still offered, Frodo still exerts and the hand is still revealed, but nothing more is removed.
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// No minions, so the turn goes straight on to the regroup phase, where the limit is fresh.
		scn.RemoveWoundsFromChar(frodo, 3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWhenFrodoCannotExertNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: exhausted.
		assertFalse(scn.FreepsActionAvailable(sting));

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
