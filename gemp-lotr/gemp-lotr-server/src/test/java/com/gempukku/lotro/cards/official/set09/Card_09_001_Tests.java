package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ring of Fury: [dwarven] artifact, ring, cost 0.  "Bearer must be a Dwarf.  Bearer is damage +2."
					// Its regroup ability is never used here.
					put("fury", "9_7");
					// Narya: [gandalf] artifact, ring, cost 0.  "Bearer must be Gandalf."  Its text acts at the start
					// of a turn, which no test here reaches with it in play.
					put("narya", "3_34");
					// Ring of Accretion: a second [dwarven] ring a Dwarf may bear, for the discard pile.
					put("accretion", "9_6");
					// Vilya, Ring of Air: [elven] ring.  "Bearer must be Elrond or Gil-galad", and neither is here.
					put("vilya", "9_23");
					// Ring of Rancor: a [wraith] ring, a Shadow card.  "Bearer must be a Nazgul."
					put("rancor", "9_44");
					// The Shards of Narsil: [gondor] artifact, cost 1, played to the support area with nothing to
					// spot: an artifact that is no ring.
					put("shards", "3_44");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk

					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");

					// Gate Picket: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert this minion twice to make
					// the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer and nothing else
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities exert the Ring-bearer or remove a burden to add twilight.
					put("gollum", "9_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"9_1"
		);
	}

	@Test
	public void TheBindingRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: The One Ring, The Binding Ring
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: <b>Fellowship:</b> Add 2 burdens to play a ring from your draw deck.
		 * <b>Maneuver:</b> Exert bearer to wear The One Ring until the regroup phase.
		 * While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a burden instead.
		*/

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Binding Ring", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void TheBindingRingFellowshipAbilityAdds2BurdensToPlayARingFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fury = scn.GetFreepsCard("fury");
		var narya = scn.GetFreepsCard("narya");
		var accretion = scn.GetFreepsCard("accretion");
		var vilya = scn.GetFreepsCard("vilya");
		var rancor = scn.GetFreepsCard("rancor");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(gimli, gandalf);
		// A ring Gimli could bear, but not in the draw deck.
		scn.MoveCardsToDiscard(accretion);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, fury, narya);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.FreepsUseCardAction(ring);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsDismissRevealedCards();

		// Rings of two cultures, each with its bearer in play.  Not the artifact that is no ring, the Shadow ring,
		// the ring nobody here may bear, or the ring in the discard pile.  The player need not find one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, narya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shards, rancor, vilya, accretion));
		assertEquals(2, scn.FreepsGetSelectableCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(fury);

		assertAttachedTo(fury, gimli);
		assertInZone(Zone.DECK, narya);
		assertEquals(2, scn.GetBurdens());
		// Ring of Fury costs 0.
		assertEquals(0, scn.GetTwilight());
		// The deck that was looked through is shuffled; the other player's is not.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// There is no limit: it is offered again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
	}

	@Test
	public void WithNoRingThatCanBePlayedTheBurdensAreStillAddedAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var fury = scn.GetFreepsCard("fury");
		var narya = scn.GetFreepsCard("narya");
		var vilya = scn.GetFreepsCard("vilya");
		var rancor = scn.GetFreepsCard("rancor");

		// Neither a Dwarf nor Gandalf is in play: no ring in the draw deck has a bearer.
		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		// Looking through a draw deck for a card that is not there is allowed, and the cost is paid all the same.
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.FreepsUseCardAction(ring);
		assertEquals(2, scn.GetBurdens());
		// The deck is shown, and there is nothing to choose.
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DECK, fury, narya, vilya, rancor);
		assertEquals(2, scn.GetBurdens());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileNoBurdenCanBeAddedToAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gimli = scn.GetFreepsCard("gimli");
		var fury = scn.GetFreepsCard("fury");
		// With Gimli in play, Ring of Fury in the draw deck could be played.
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(ring));

		// No burden can be added to a hindered Ring-bearer.
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DECK, fury);
	}

	@Test
	public void TheBindingRingManeuverAbilityExertsRingBearerToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, gollum);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(ring));
		// One action and no more: the Ring's Fellowship ability, whose burdens could be added, is not on offer here.
		assertEquals(1, scn.FreepsGetActionCount());

		// The bearer exerts, and nobody else; an exertion is no wound, and adds no burden.
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		// Worn, the ability still has a cost that can be paid, and is still among the maneuver actions.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// The archery phase is the phase next door: the ability is not among its actions.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowAssignToMinions(sam, runner);

		// Sam, 3, loses to the Runner, 5.  He is Ring-bound and no Ring-bearer: his wound is a wound.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		// Frodo's skirmish follows one already resolved, and the Ring is still on.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());

		// 3 + 1 against 5; damage +1: each of the two wounds is a burden.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// Off, Gollum's wound is a wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBindingRingConvertsWoundsDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var picket = scn.GetShadowCard("picket");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(picket, marksman, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		// Worn, in the maneuver phase: the Free Peoples player names no culture, so Gate Picket wounds Frodo and
		// Sam.  The Ring-bearer's wound is a burden; Sam, Ring-bound and no Ring-bearer, takes his.
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertEquals(2, scn.GetWoundsOn(picket));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Worn, in the archery phase: the Marksman's wound, placed on Frodo, is a burden.
		scn.SkipToArcheryWounds();
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// Worn, in a skirmish, an exertion is no wound: Ettenmoors, the site the fellowship is at, has Frodo exert
		// to be strength +2, and the Ring has nothing to say.
		assertTrue(scn.RBWearingOneRing());
		var ettenmoors = scn.GetCurrentSite();
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(frodo));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Soldier's own wound is a burden ...
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and so is the wound for losing, 3 + 1 (Ring) + 2 (Ettenmoors) against 7.
		scn.PassSkirmishActions();
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void NotWornTheSameWoundsAreWoundsAndNoBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var picket = scn.GetShadowCard("picket");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(picket, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Ring could be put on, and is not.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertFalse(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// 3 + 1 against 5, and damage +1: two wounds more, of his vitality of 4 + 1.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		// Orc Soldier is there only so that the maneuver phase happens.
		scn.MoveMinionsToTable(soldier);
		// Frodo's vitality of 4 and the Ring's +1: with 3 wounds he has 2 left and can exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));

		// A fourth wound leaves him 1: exhausted, he cannot exert (CR 4.2 "exhausted"), and the cost cannot be paid.
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertFalse(scn.RBWearingOneRing());
	}
}
