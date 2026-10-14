package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_035_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("calenhad", "103_86");   // Northern Signal-fire, Flame of Calenhad: a [rohan] beacon, cost 2; to play, hinder 2 beacons; a maneuver ability only
					put("amondin", "103_28");    // Northern Signal-fire, Flame of Amon Din: a [gondor] beacon; a response to an unbound Man's wound only
					put("eilenach", "103_29");   // Northern Signal-fire, Flame of Eilenach: a [gondor] beacon; a skirmish ability only
					put("halifirien", "103_87"); // Northern Signal-fire, Flame of Halifirien: a [rohan] beacon; its text is about mounts
					put("level", "7_92");        // First Level: a [gondor] condition in the support area and no beacon, cost 1; a maneuver ability only

					put("knight1", "5_35");      // Gondorian Knight: an unbound [gondor] Man with no text, cost 2, vitality 3
					put("knight2", "5_35");
					put("guard", "5_83");        // Household Guard: an unbound [rohan] Man, cost 2, vitality 3; valiant is his whole text
					put("ithilien", "4_130");    // Ranger of Ithilien: a [gondor] Man who is Ring-bound, vitality 3; his text needs a skirmish
					put("barliman", "1_70");     // Barliman Butterbur: a [gandalf] Man who is an ally, vitality 2; his own fellowship ability is never used here
					put("gimli", "1_13");        // Gimli, Son of Glóin: an unbound companion who is no Man

					put("athelas", "1_94");      // Athelas: a [gondor] possession
					put("blade", "1_95");        // Blade of Gondor: a [gondor] possession
					put("helm", "4_279");        // Helm! Helm!: a [rohan] event
					put("curse", "1_36");        // Curse Their Foul Feet!: an [elven] event
					put("runner", "1_178");      // Goblin Runner: a [moria] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarbeaconStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: War-beacon
		 * Unique: 3
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, spot an unbound Man.
		 * 	Fellowship: Add (1) per beacon you can spot to play a beacon from the discard pile.
		 * 	Fellowship: Exert a Man and hinder a beacon to reveal the top 4 cards of your draw deck. You may take
		 * 	any [gondor] or [rohan] cards revealed into hand. Replace the rest in any order.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beacon1");

		assertEquals("War-beacon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(3, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneToTheSupportAreaWithAnUnboundRohanManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(beacon);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(beacon));
		scn.FreepsPlayCard(beacon);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon);
		assertNotAttachedTo(beacon, guard);
		assertFalse(scn.IsHindered(beacon));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAManWhoIsAnUnboundCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var gimli = scn.GetFreepsCard("gimli");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		// An unbound companion who is no Man, a Man who is Ring-bound, and a Man who is an ally (only a companion is unbound).
		scn.MoveCompanionsToTable(gimli, ithilien);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(beacon, knight);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(beacon));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(beacon));

		scn.FreepsPlayCard(beacon);
		// 2 for the Knight + 1 for the beacon
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneBeaconInPlayAddsOneAndPlaysTheBeaconInTheDiscardPileAtItsOwnCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var knight = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon1);
		scn.MoveCardsToDiscard(beacon2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(beacon1, "Play a beacon"));
		scn.FreepsUseCardAction(beacon1, "Play a beacon");

		// 1 for the one beacon spotted + 1, the cost of the beacon played
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon2);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsOneForEachBeaconAndOffersTheBeaconsInTheDiscardPileAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var amondin = scn.GetFreepsCard("amondin");
		var eilenach = scn.GetFreepsCard("eilenach");
		var halifirien = scn.GetFreepsCard("halifirien");
		var level = scn.GetFreepsCard("level");
		var knight = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(knight);
		// In play: two [gondor] beacons and a [rohan] one.
		scn.MoveCardsToSupportArea(beacon1, beacon2, halifirien);
		// In the discard pile: a [gondor] beacon, a [rohan] beacon, and a [gondor] card that is no beacon.
		scn.MoveCardsToDiscard(beacon3, calenhad, level);
		// A beacon in hand and a beacon in the draw deck.
		scn.MoveCardsToHand(amondin);
		scn.MoveCardsToTopOfDeck(eilenach);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(beacon1, "Play a beacon");

		// 1 for each of the 3 beacons in play, added before anything is played
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(beacon3, calenhad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(level, amondin, eilenach));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(beacon3);

		// + 1, the cost of the War-beacon played
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon3);
		assertInDiscard(calenhad, level);
		assertInHand(amondin);
		assertInZone(Zone.DECK, eilenach);
		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));
		assertFalse(scn.IsHindered(halifirien));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedBeaconIsNotCountedForTheTwilightAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var knight = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon1, beacon2);
		scn.MoveCardsToDiscard(beacon3);
		scn.HinderCard(beacon2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsHindered(beacon2));
		scn.FreepsUseCardAction(beacon1, "Play a beacon");

		// 1 for the one beacon that can be spotted + 1, the cost of the beacon played
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilABeaconInTheDiscardPileCanBePlayedAndThatBeaconsOwnCostsArePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var level = scn.GetFreepsCard("level");
		var knight = scn.GetFreepsCard("knight1");
		var theirBeacon = scn.GetShadowCard("beacon3");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon1);
		// A card that is no beacon, and a beacon that cannot be played: it needs 2 beacons to hinder and there is 1.
		scn.MoveCardsToDiscard(level, calenhad);
		// A War-beacon in the other player's discard pile, and one in hand.
		scn.MoveCardsToDiscard(theirBeacon);
		scn.MoveCardsToHand(beacon2);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(beacon1, "Play a beacon"));
		// Its other ability is on offer, so the beacon's text is in force.
		assertTrue(scn.FreepsActionAvailable(beacon1, "Reveal"));

		scn.FreepsPlayCard(beacon2);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(beacon1, "Play a beacon"));

		scn.FreepsUseCardAction(beacon1, "Play a beacon");
		// 1 already + 2 for the 2 beacons spotted + 2, the cost of the Signal-fire
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, calenhad);
		// The Signal-fire's own "to play, hinder 2 beacons" took the only two there were.
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
		assertFalse(scn.IsHindered(calenhad));
		assertInDiscard(level, theirBeacon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsAManAndHindersABeaconToRevealTheTopFourCardsToBothPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var athelas = scn.GetFreepsCard("athelas");
		var helm = scn.GetFreepsCard("helm");
		var knight2 = scn.GetFreepsCard("knight2");
		var guard = scn.GetFreepsCard("guard");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon);

		scn.StartGame();
		// The last one listed ends up on top: Athelas, Helm! Helm!, a Gondorian Knight, a Household Guard, and fifth the Blade.
		scn.MoveCardsToTopOfDeck(blade, guard, knight2, helm, athelas);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsHindered(beacon));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(beacon, "Reveal"));
		scn.FreepsUseCardAction(beacon, "Reveal");

		// One Man and one beacon, this one: both are taken without asking, and the beacon still does what it says.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(beacon));

		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(helm, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight2, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		assertFalse(scn.FreepsHasBPChoice(blade));
		assertTrue(scn.ShadowHasCardChoiceAvailable(athelas, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(helm, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight2, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		assertFalse(scn.ShadowHasBPChoice(blade));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// A [gondor] possession, a [rohan] event, a [gondor] companion and a [rohan] companion: all four may be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, helm, knight2, guard));
		assertFalse(scn.FreepsHasBPChoice(blade));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(athelas, helm, knight2, guard);

		assertInHand(athelas, helm, knight2, guard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(blade, scn.GetFreepsTopOfDeck());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnyManMayBeExertedAndAnyBeaconNotYetHinderedMayBeHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var level = scn.GetFreepsCard("level");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var guard = scn.GetFreepsCard("guard");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight1, knight2, guard, ithilien, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(beacon1, beacon2, beacon3, calenhad, level);
		// A Man with 1 vitality left cannot exert.
		scn.AddWoundsToChar(knight2, 2);
		scn.HinderCard(beacon3);

		scn.StartGame();

		scn.FreepsUseCardAction(beacon1, "Reveal");

		// A [gondor] Man, a [rohan] Man, a Ring-bound Man and a Man who is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, guard, ithilien, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo, knight2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(barliman));
		scn.FreepsChooseCard(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(ithilien));

		// The beacon itself, another War-beacon and a [rohan] beacon; not the beacon already hindered, nor a [gondor] card that is no beacon.
		assertTrue(scn.FreepsHasCardChoiceAvailable(beacon1, beacon2, calenhad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(beacon3, level));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsHindered(calenhad));
		scn.FreepsChooseCard(calenhad);
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));
		assertFalse(scn.IsHindered(level));

		// Both players are now being shown the top four cards.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsDecisionAvailable("revealed card(s) from top of deck"));
		assertTrue(scn.ShadowDecisionAvailable("revealed card(s) from top of deck"));
	}

	@Test
	public void IsNotOfferedWhileNoManCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		// The only Man has 1 vitality left; a Dwarf beside him could exert.
		scn.MoveCompanionsToTable(knight, gimli);
		scn.AddWoundsToChar(knight, 2);
		scn.MoveCardsToSupportArea(beacon);
		scn.MoveCardsToHand(guard);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(beacon, "Reveal"));

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsActionAvailable(beacon, "Reveal"));

		scn.FreepsUseCardAction(beacon, "Reveal");
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsHindered(beacon));
		// Both players are now being shown the top four cards.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsDecisionAvailable("revealed card(s) from top of deck"));
		assertTrue(scn.ShadowDecisionAvailable("revealed card(s) from top of deck"));
	}

	@Test
	public void OnlyTheGondorAndRohanCardsRevealedMayBeTakenAndTheRestGoBackOnTopInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var athelas = scn.GetFreepsCard("athelas");
		var helm = scn.GetFreepsCard("helm");
		var curse = scn.GetFreepsCard("curse");
		var runner = scn.GetFreepsCard("runner");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon);

		scn.StartGame();
		// From the top: Athelas, Helm! Helm!, Curse Their Foul Feet!, a Goblin Runner, and fifth the Blade of Gondor.
		scn.MoveCardsToTopOfDeck(blade, runner, curse, helm, athelas);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(beacon, "Reveal");
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// The [elven] event and the [moria] minion are shown and cannot be taken; the fifth card, a [gondor] one, is not there at all.
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, helm));
		assertTrue(scn.FreepsHasCardChoiceAvailable(curse, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertFalse(scn.FreepsHasBPChoice(blade));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// One of the two that may be taken is left.
		scn.FreepsChooseCards(athelas);

		assertInHand(athelas);
		assertEquals(1, scn.GetFreepsHandCount());

		// The other three go back one at a time, each on top of the one before; the last needs no asking.
		assertTrue(scn.FreepsHasCardChoiceAvailable(helm, curse, runner));
		assertFalse(scn.FreepsHasBPChoice(athelas));
		scn.FreepsChooseCard(runner);
		scn.FreepsChooseCard(helm);

		assertEquals(curse, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(helm, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(runner, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(blade, scn.GetFromTopOfFreepsDeck(4));
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingNoneLeavesTheHandEmptyAndAllFourGoBackOnTop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var athelas = scn.GetFreepsCard("athelas");
		var helm = scn.GetFreepsCard("helm");
		var curse = scn.GetFreepsCard("curse");
		var runner = scn.GetFreepsCard("runner");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon);

		scn.StartGame();
		// From the top: Athelas, Helm! Helm!, Curse Their Foul Feet!, a Goblin Runner, and fifth the Blade of Gondor.
		scn.MoveCardsToTopOfDeck(blade, runner, curse, helm, athelas);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(beacon, "Reveal");
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, helm));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsPass();

		// All four go back, here in the reverse of the order they came off in.
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, helm, curse, runner));
		scn.FreepsChooseCard(athelas);
		scn.FreepsChooseCard(helm);
		scn.FreepsChooseCard(curse);

		assertEquals(runner, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(curse, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(helm, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(athelas, scn.GetFromTopOfFreepsDeck(4));
		assertEquals(blade, scn.GetFromTopOfFreepsDeck(5));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheRevealIsOfferedFlaggedNoEffectAndPaidForAndRevealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon = scn.GetFreepsCard("beacon1");
		var knight = scn.GetFreepsCard("knight1");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(beacon);

		scn.StartGame();
		// One card is left in the draw deck, fewer than the 4 to be revealed: there is something to reveal.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(athelas);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(beacon, "Reveal"));
		assertEquals("false", scn.FreepsActionNoEffect(beacon, "Reveal"));

		// That card gone, the costs can still be paid, so the ability is offered (Q-0163); it is marked as doing
		// nothing.  The Shadow player's deck is not empty.
		scn.MoveCardsToDiscard(athelas);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.GetShadowDeckCount() > 0);
		assertTrue(scn.FreepsActionAvailable(beacon, "Reveal"));
		assertEquals("effect", scn.FreepsActionNoEffect(beacon, "Reveal"));

		// Used all the same, the Man exerts and the beacon is hindered; nobody is shown anything, no choice of
		// cards to take is put, and nothing comes into hand.
		scn.FreepsUseCardAction(beacon, "Reveal");
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(beacon));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
