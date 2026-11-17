package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celebring", "18_7");
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ALLY, no companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of another culture, a Dwarf

					// Mirror of Galadriel, Dangerous Guide: [elven] artifact to the support area (to play, spot 3 Elves)
					put("mirror", "15_22");
					// Phial of Galadriel, Star-glass: [elven] artifact borne by a Hobbit (the Ring-bearer)
					put("phial", "10_13");
					// Elven Bow: an [elven] POSSESSION an Elf may bear, no artifact
					put("bow", "1_41");
					// Ring of Guile: a [dwarven] artifact, which Gimli may bear
					put("guile", "9_8");
					// Aiglos: an [elven] artifact an Elf may bear; the first player's copy is laid in his hand
					put("aiglos", "9_12");
					// Library of Rivendell: an [elven] support-area artifact; the first player's copy is laid in his
					// discard pile
					put("library", "101_11");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CelebringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Celebring, Elven-smith
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Follower
		 * Strength: +2
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper>
		 * To play, spot 2 [elven] companions.
		 * When you play Celebring, you may play an [elven] artifact from your deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("celebring");

		assertEquals("Celebring", card.getBlueprint().getTitle());
		assertEquals("Elven-smith", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, scn.GetAidTwilightCost(card));
	}

	@Test
	public void CannotBePlayedUntilASecondElvenCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celebring = scn.GetFreepsCard("celebring");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(celebring, arwen);

		scn.StartGame();

		// One [elven] companion, beside an [elven] Elf who is an ally and a companion of another culture.
		assertInZone(Zone.HAND, celebring);
		assertFalse(scn.FreepsPlayAvailable(celebring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);

		// Arwen is the second.
		assertTrue(scn.FreepsPlayAvailable(celebring));
		scn.FreepsPlayCard(celebring);
		assertInZone(Zone.SUPPORT, celebring);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celebring));
	}

	@Test
	public void PlayingCelebringMayPlayAnElvenArtifactFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celebring = scn.GetFreepsCard("celebring");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		// The third Elf, so that the Mirror can be played.
		scn.MoveAlliesToTable(orophin);

		var mirror = scn.GetFreepsCard("mirror");
		var phial = scn.GetFreepsCard("phial");
		var bow = scn.GetFreepsCard("bow");
		var guile = scn.GetFreepsCard("guile");
		var aiglos = scn.GetFreepsCard("aiglos");
		var library = scn.GetFreepsCard("library");
		scn.MoveCardsToHand(celebring, aiglos);
		scn.MoveCardsToDiscard(library);

		scn.StartGame();

		assertInZone(Zone.DECK, mirror, phial, bow, guile);
		assertInZone(Zone.HAND, aiglos);
		assertInZone(Zone.DISCARD, library);
		// The other player's Aiglos and Library lie in HIS draw deck.
		assertInZone(Zone.DECK, scn.GetShadowCard("aiglos"), scn.GetShadowCard("library"));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(celebring);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celebring));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// An [elven] artifact to the support area and one borne by a Hobbit are offered.  Not an [elven]
		// possession, an artifact of another culture, an [elven] artifact in hand or in the discard pile, nor
		// one in the other player's draw deck (a choice from a pile is read by blueprint, and the first player's
		// copies of those two are out of his deck).
		assertTrue(scn.FreepsHasCardChoiceAvailable(mirror, phial));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow, guile));
		assertFalse(scn.FreepsHasBPChoiceAvailable("9_12"));
		assertFalse(scn.FreepsHasBPChoiceAvailable("101_11"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(mirror);

		assertInZone(Zone.SUPPORT, mirror);
		assertInZone(Zone.DECK, phial, bow, guile);
		assertInZone(Zone.HAND, aiglos);
		assertInZone(Zone.DISCARD, library);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck is shuffled as the Mirror leaves it, and again when the look is over; the other player's is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndAnElvenArtifactPlayedLaterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celebring = scn.GetFreepsCard("celebring");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);

		var phial = scn.GetFreepsCard("phial");
		var aiglos = scn.GetFreepsCard("aiglos");
		scn.MoveCardsToHand(celebring, aiglos);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(celebring);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celebring));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, phial);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [elven] artifact played with Celebring in play is no play of Celebring.
		scn.FreepsPlayCard(aiglos);
		// Legolas and Arwen may each bear it.
		scn.FreepsChooseCard(legolas);
		assertTrue(scn.GetAttachedCards(legolas).contains(aiglos));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(celebring));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElvenArtifactInTheDrawDeckTheOfferIsStillMadeAndTheLookFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celebring = scn.GetFreepsCard("celebring");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		scn.MoveCardsToHand(celebring);
		// Every [elven] artifact out of the deck; the Elven Bow and the Ring of Guile are left in it.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("mirror"), scn.GetFreepsCard("phial"),
				scn.GetFreepsCard("aiglos"), scn.GetFreepsCard("library"));

		var bow = scn.GetFreepsCard("bow");
		var guile = scn.GetFreepsCard("guile");

		scn.StartGame();

		assertInZone(Zone.DECK, bow, guile);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(celebring);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celebring));
		scn.FreepsAcceptOptionalTrigger();
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, bow, guile);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AidAddsTwoToTransferCelebringToACompanionAndOffersNoArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celebring = scn.GetFreepsCard("celebring");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToSupportArea(celebring);

		var phial = scn.GetFreepsCard("phial");

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, celebring);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celebring, "Aid"));
		int twilight = scn.GetTwilight();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli));
		scn.FreepsChooseCard(legolas);

		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, celebring);
		assertTrue(scn.GetAttachedCards(legolas).contains(celebring));
		// A transfer is no play: the artifact is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(celebring));
		assertInZone(Zone.DECK, phial);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
