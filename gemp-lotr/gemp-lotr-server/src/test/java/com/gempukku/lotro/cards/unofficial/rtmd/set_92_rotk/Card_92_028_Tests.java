package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

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

public class Card_92_028_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Mithril-coat (2_105): Shire artifact, twilight 2, bearer must be Frodo
					put("mithril", "2_105");
					// Library of Orthanc: Isengard Shadow artifact, twilight 2, plays to the support area
					put("library", "9_39");
					// Gimli, Son of Gloin (1_13): Dwarven companion
					put("gimli", "1_13");
					// The Shards of Narsil: Gondor artifact, twilight 1, plays to the support area
					put("shards", "3_44");
					// Hobbit Sword: Shire possession, twilight 1, bearer must be a Hobbit. No artifact
					put("sword", "1_299");
					// Phial of Galadriel, Star-glass: Elven artifact, twilight 0, bearer must be a Hobbit.
					// The artifact that lies in the discard pile
					put("phial", "10_13");
					// Throne of Isengard: Isengard Shadow artifact, twilight 2, plays to the support area
					put("throne", "17_39");
					// Ring of Rancor: Wraith Shadow artifact, twilight 0, bearer must be a Nazgul
					put("rancor", "9_44");
					// Black Steed: Wraith Shadow possession, twilight 1, bearer must be a Nazgul. No artifact
					put("steed", "1_208");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul to bear them (site 3)
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_28", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Mithril-coat (2_105): Shire artifact, twilight 2, bearer must be Frodo
					put("mithril", "2_105");
					// Library of Orthanc: Isengard Shadow artifact, twilight 2, plays to the support area
					put("library", "9_39");
					// Gimli, Son of Gloin (1_13): Dwarven companion
					put("gimli", "1_13");
					// The Shards of Narsil: Gondor artifact, twilight 1, plays to the support area
					put("shards", "3_44");
					// Hobbit Sword: Shire possession, twilight 1, bearer must be a Hobbit. No artifact
					put("sword", "1_299");
					// Phial of Galadriel, Star-glass: Elven artifact, twilight 0, bearer must be a Hobbit.
					// The artifact that lies in the discard pile
					put("phial", "10_13");
					// Throne of Isengard: Isengard Shadow artifact, twilight 2, plays to the support area
					put("throne", "17_39");
					// Ring of Rancor: Wraith Shadow artifact, twilight 0, bearer must be a Nazgul
					put("rancor", "9_44");
					// Black Steed: Wraith Shadow possession, twilight 1, bearer must be a Nazgul. No artifact
					put("steed", "1_208");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul to bear them (site 3)
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_28"
		);
	}

	protected VirtualTableScenario GetDualScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Mithril-coat (2_105): Shire artifact, twilight 2, bearer must be Frodo
					put("mithril", "2_105");
					// Library of Orthanc: Isengard Shadow artifact, twilight 2, plays to the support area
					put("library", "9_39");
					// Gimli, Son of Gloin (1_13): Dwarven companion
					put("gimli", "1_13");
					// The Shards of Narsil: Gondor artifact, twilight 1, plays to the support area
					put("shards", "3_44");
					// Hobbit Sword: Shire possession, twilight 1, bearer must be a Hobbit. No artifact
					put("sword", "1_299");
					// Phial of Galadriel, Star-glass: Elven artifact, twilight 0, bearer must be a Hobbit.
					// The artifact that lies in the discard pile
					put("phial", "10_13");
					// Throne of Isengard: Isengard Shadow artifact, twilight 2, plays to the support area
					put("throne", "17_39");
					// Ring of Rancor: Wraith Shadow artifact, twilight 0, bearer must be a Nazgul
					put("rancor", "9_44");
					// Black Steed: Wraith Shadow possession, twilight 1, bearer must be a Nazgul. No artifact
					put("steed", "1_208");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul to bear them (site 3)
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_28", "92_28"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_28
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Fellowship or Shadow: Play an artifact from your draw deck (limit once per turn).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_28", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsPlaysArtifactFromDeckInFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mithril = scn.GetFreepsCard("mithril");
		var mod = scn.GetFreepsCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(mithril);

		assertInZone(Zone.ATTACHED, mithril);
		// Played, and so paid for: Mithril-coat costs 2
		assertEquals(2, scn.GetTwilight());

		//Limit once per turn
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void ShadowPlaysArtifactFromDeckInShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Is not owner-gated
		var scn = GetShadowScenario();

		var library = scn.GetShadowCard("library");
		var mod = scn.GetShadowCard("mod");

		// Leave library in draw deck
		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCards(library);

		assertInZone(Zone.SUPPORT, library);
		// Played, and so paid for: Library of Orthanc costs 2
		assertEquals(11, scn.GetTwilight());

		//Limit once per turn
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OpponentCannotUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mithril = scn.GetFreepsCard("mithril");
		var mod = scn.GetShadowCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertInZone(Zone.DECK, mithril);
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerCannotUseTheOtherPlayersModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var library = scn.GetShadowCard("library");

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Shadow player has an artifact in his draw deck and the pool to play it, and the modifier is not his
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.DECK, library);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void FreepsCannotUseOpponentsDuringFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDualScenario();

		var mod = scn.GetFreepsCard("mod");
		var shadowMod = scn.GetShadowCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsActionAvailable(shadowMod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NeitherOwnerIsOfferedTheActionInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDualScenario();

		var mod = scn.GetFreepsCard("mod");
		var shadowMod = scn.GetShadowCard("mod");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		// Each owner is offered it in his own phase of this turn, and neither uses it
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shadowMod));

		// A minion, so that the turn has a maneuver phase
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shadowMod));
	}

	@Test
	public void FellowshipUseOffersTheFreePeoplesArtifactsOfTheDrawDeckAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var mithril = scn.GetFreepsCard("mithril");
		var shards = scn.GetFreepsCard("shards");
		var sword = scn.GetFreepsCard("sword");
		var library = scn.GetFreepsCard("library");
		var rancor = scn.GetFreepsCard("rancor");
		var phial = scn.GetFreepsCard("phial");

		// An artifact Frodo could bear, in the discard pile and not the draw deck
		scn.MoveCardsToDiscard(phial);

		scn.StartGame();

		assertEquals(9, scn.GetFreepsDeckCount());
		int shadowDeck = scn.GetShadowDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(mod);
		// His own draw deck is what he looks through, all nine cards of it
		assertEquals(9, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Artifacts of two cultures, one for Frodo to bear and one for the support area. Not the possession
		// Frodo could bear, not his own Shadow artifacts, not the artifact in his discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mithril, shards));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, library, rancor, phial));
		// There is no penalty for choosing not to play: none need be chosen, and one may be
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(shards);

		assertInZone(Zone.SUPPORT, shards);
		assertInZone(Zone.DECK, mithril);
		assertInZone(Zone.DISCARD, phial);
		// The Shards of Narsil cost 1
		assertEquals(1, scn.GetTwilight());
		assertEquals(8, scn.GetFreepsDeckCount());
		// The deck that was looked through is shuffled, and the other player's is left alone
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// Once per turn: Mithril-coat is still in the draw deck and Frodo could bear it
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowUseOffersTheShadowArtifactsOfTheDrawDeckAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var library = scn.GetShadowCard("library");
		var throne = scn.GetShadowCard("throne");
		var rancor = scn.GetShadowCard("rancor");
		var steed = scn.GetShadowCard("steed");
		var mithril = scn.GetShadowCard("mithril");
		var shards = scn.GetShadowCard("shards");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(13, scn.GetTwilight());
		assertEquals(9, scn.GetShadowDeckCount());

		scn.ShadowUseCardAction(mod);
		assertEquals(9, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Artifacts of two cultures, for the support area and for the Nazgul to bear. Not the possession the
		// Nazgul could bear, and not his own Free Peoples artifacts.
		assertTrue(scn.ShadowHasCardChoiceAvailable(library, throne, rancor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, mithril, shards));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(rancor);

		assertAttachedTo(rancor, lemenya);
		assertInZone(Zone.DECK, library, throne, steed);
		// Ring of Rancor costs nothing
		assertEquals(13, scn.GetTwilight());
		assertEquals(8, scn.GetShadowDeckCount());
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HavingLookedThePlayerMayPlayNoArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var mithril = scn.GetFreepsCard("mithril");
		var shards = scn.GetFreepsCard("shards");

		scn.StartGame();

		assertEquals(10, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(mod);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(mithril, shards));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, mithril, shards);
		assertEquals(10, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		// The deck was looked through all the same, and that was the turn's use
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoArtifactInTheDrawDeckTheDeckIsLookedThroughAndNothingIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var mithril = scn.GetFreepsCard("mithril");
		var shards = scn.GetFreepsCard("shards");
		var phial = scn.GetFreepsCard("phial");

		// Every Free Peoples artifact is in the discard pile; the Shadow artifacts left in the deck are not his to play
		scn.MoveCardsToDiscard(mithril, shards, phial);

		scn.StartGame();

		assertEquals(7, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// There is no penalty for not finding one: the action is offered, the deck is shown, and that is all
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		assertEquals(7, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, mithril, shards, phial);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheLimitIsForTheTurnAndNotThePhaseAndTheNextTurnHasItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var library = scn.GetShadowCard("library");
		var throne = scn.GetShadowCard("throne");
		var shards = scn.GetShadowCard("shards");

		// Kept out of the deck the reconciles draw from, and brought in when they are wanted
		scn.MoveCardsToDiscard(throne, shards);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The first Shadow phase of player 1's turn
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(library);
		assertInZone(Zone.SUPPORT, library);
		assertFalse(scn.ShadowActionAvailable(mod));

		// The fellowship moves again: a second Shadow phase in the same turn
		scn.SkipToMovementDecision();
		scn.MoveCardsToTopOfDeck(throne);
		scn.SetTwilight(10);
		scn.FreepsChooseToMove();

		// 10 set + 1 companion + 0 for Rivendell Valley: Throne of Isengard, cost 2, is in his deck to be played
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.DECK, throne);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));

		// Player 2's own turn: a new turn, and he is now the Free Peoples player
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToBottomOfDeck(shards);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.ShadowUseCardAction(mod);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shards);

		assertInZone(Zone.SUPPORT, shards);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
