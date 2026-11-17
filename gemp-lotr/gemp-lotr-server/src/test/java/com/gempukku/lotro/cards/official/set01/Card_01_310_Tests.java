package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_310_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_310");
					// Bill the Pony 3_106: unique [shire] possession, no item class, bearer must be a Hobbit companion;
					// each site's Shadow number -1, discarded at an underground site
					put("bill", "3_106");
					// Bill the Pony, Dearly-loved 13_143: unique [shire] mount, bearer must be a Ring-bound Hobbit; its
					// "you may reinforce a [shire] token" is withheld with no token in play, and its regroup ability
					// needs [shire] tokens
					put("bill2", "13_143");
					put("sword", "1_299");      // Hobbit Sword: [shire] possession of another title, bearer must be a Hobbit, no other text
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; his text needs a weapon
					put("merry", "1_303");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
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
					put("sam", "1_310");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer; strength 3, and his texts want more minions than companions, or a
					// skirmish he is not in
					put("frodo", "13_149");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sam, Faithful Companion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Frodo
		 * Game Text: <b>Fellowship:</b> Play Bill the Pony from your draw deck.<br><b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5)</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Faithful Companion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
		// Not printed: every version of Sam is Ring-bound by rule.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}

	@Test
	public void FellowshipAbilityPlaysEitherBillThePonyFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var bill2 = scn.GetFreepsCard("bill2");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertInZone(Zone.DECK, bill, bill2, sword);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();

		// Either card titled Bill the Pony, with an item class and without; not a [shire] possession of another title.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bill, bill2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		scn.FreepsChooseCardBPFromSelection(bill);
		// Bill's own text: a Hobbit companion bears him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(bill, sam);
		assertInZone(Zone.DECK, bill2, sword);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck has been looked through, so it is shuffled: once as Bill leaves it and once more as the look ends
		// (pitfalls, "Whether a deck was shuffled"). The other player's is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsUsedAgainInTheSamePhaseAndWithNoBillInTheDeckPlaysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var bill2 = scn.GetFreepsCard("bill2");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		// One Bill in the deck: once he is played, none is left there.
		scn.MoveCardsToDiscard(bill2);

		scn.StartGame();

		assertInZone(Zone.DECK, bill, sword);
		assertInZone(Zone.DISCARD, bill2);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bill);
		scn.FreepsChooseCard(sam);
		assertAttachedTo(bill, sam);
		// Once as Bill leaves the deck, once as the look ends.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		// No limit: the action is on offer again in the same phase, with no Bill left in the deck.
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();

		// Nothing is played, and the deck is looked through and shuffled once more.
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, sword);
		assertInZone(Zone.DISCARD, bill2);
		assertEquals(shuffles + 3, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(sam));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DECK, bill);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		// The burdens went with Frodo.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoDiesOfWoundsSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertSame(frodo, scn.GetRingBearer());
		// Vitality 4: he dies when the game next checks.
		scn.AddWoundsToChar(frodo, 4);
		assertEquals(4, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoDiesLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
