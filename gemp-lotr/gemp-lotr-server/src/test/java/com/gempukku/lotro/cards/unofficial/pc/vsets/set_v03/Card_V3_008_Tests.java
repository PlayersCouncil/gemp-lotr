package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_008_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("words", "103_8");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: vitality 4; a fellowship ability never used here
					put("radagast", "9_26");        // Radagast, The Brown: the other Free Peoples Wizard, vitality 4
					put("quickbeam", "6_33");       // Quickbeam, Bregalad: a [gandalf] companion who is an Ent and no Wizard
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man; Maneuver: exert him to make him defender +1
					put("whitesaruman", "32_24");   // Saruman, The White (a Hobbit-set card): a [gandalf] ally who is a Wizard, vitality 4; makes Gandalf defender +1, fellowship and skirmish abilities only
					put("palantir", "13_36");       // The Palantir of Orthanc: [gandalf] artifact borne by Gandalf, no vitality bonus; its trigger is at the start of the fellowship phase
					put("staff", "0_57");           // Radagast's Staff: [gandalf] artifact, strength +1, vitality +1; its trigger is on a move in the regroup phase
					put("gandalfspipe", "1_74");    // Gandalf's Pipe: a [gandalf] possession
					put("shards", "3_44");          // The Shards of Narsil: a [gondor] artifact in the support area, fellowship abilities only
					put("saruman", "3_69");         // Saruman, Servant of the Eye: the opponent's Wizard, an [isengard] minion, vitality 4
					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text
					// Axe Strike, a skirmish event: cards to hold and to draw
					put("fodder1", "1_3");
					put("fodder2", "1_3");
					put("fodder3", "1_3");
					put("fodder4", "1_3");
					put("fodder5", "1_3");
					put("fodder6", "1_3");
					put("fodder7", "1_3");
					put("fodder8", "1_3");
					put("fodder9", "1_3");
					put("fodder10", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A draw deck of three cards once Gandalf, his artifact and a hand of two have been taken out of it.
	protected VirtualTableScenario GetSmallDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("words", "103_8");
					put("gandalf", "1_364");
					put("palantir", "13_36");
					put("troop", "1_177");
					put("fodder1", "1_3");
					put("fodder2", "1_3");
					put("fodder3", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WordsofPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Words of Power
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spell.
		 * 	Exhaust your Wizard to reconcile your hand. You may spot a [gandalf] artifact to shuffle this into
		 * 	your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("words");

		assertEquals("Words of Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExhaustsTheWizardAndDrawsTheHandUpToEightAndWithNoGandalfArtifactIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var shards = scn.GetFreepsCard("shards");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		// A [gandalf] possession and an artifact of another culture: neither is a [gandalf] artifact.
		scn.AttachCardsTo(gandalf, gandalfspipe);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(words, fodder1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(words);

		assertEquals(twilight + 4, scn.GetTwilight());
		// Vitality 4: three exertions leave him exhausted.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsExhausted(gandalf));
		// The event has left the hand, and the reconciling begins with the discard a player may make.
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(fodder1));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineReconciliation();

		// 1 card held + 7 drawn
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 7, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, fodder1);
		// Nothing is asked about shuffling, and the event is discarded like any other.
		assertInZone(Zone.DISCARD, words);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReconcilingAHandOfMoreThanEightDiscardsDownToEightAndDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words);
		scn.MoveCardsToFreepsHand("fodder1", "fodder2", "fodder3", "fodder4", "fodder5",
				"fodder6", "fodder7", "fodder8", "fodder9", "fodder10");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(gandalf, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(words);

		// Already wounded once, he exerts twice: as many times as he can.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(10, scn.GetFreepsHandCount());

		// Ten held: whether or not the discard a player may make first is taken, two cards have to go, and
		// the game asks for the two together.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(fodder1, fodder2);

		assertInZone(Zone.DISCARD, fodder1, fodder2);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, words);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnlyAWizardOfYourOwnCanPayAndTheShuffleMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whitesaruman = scn.GetFreepsCard("whitesaruman");
		var staff = scn.GetFreepsCard("staff");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, radagast, quickbeam, aragorn);
		// A Wizard of yours who is an ally: the text does not say "companion".
		scn.MoveAlliesToTable(whitesaruman);
		scn.AttachCardsTo(radagast, staff);
		scn.MoveCardsToHand(words);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// Radagast's Staff: vitality 4 + 1
		assertEquals(5, scn.GetVitality(radagast));

		scn.FreepsPlayCard(words);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, whitesaruman));
		// Quickbeam is [gandalf] and no Wizard; Aragorn is neither; the minion Saruman is a Wizard and not yours.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(quickbeam, aragorn, saruman));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(radagast);

		// Vitality 5: four exertions leave him exhausted.
		assertEquals(4, scn.GetWoundsOn(radagast));
		assertTrue(scn.IsExhausted(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(whitesaruman));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// The hand is empty, so there is no discard to offer and eight cards are drawn.
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 8, scn.GetFreepsDeckCount());
		// A [gandalf] artifact on a Wizard other than Gandalf.
		assertTrue(scn.FreepsDecisionAvailable("shuffle"));

		scn.FreepsChooseNo();

		assertInZone(Zone.DISCARD, words);
		assertEquals(deck - 8, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyWizardOfYoursIsExhaustedAndPlayableOnceHeCanExertOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, quickbeam, aragorn);
		scn.MoveCardsToHand(words);

		scn.StartGame();
		// A Wizard who could exert, but the opponent's.
		scn.MoveMinionsToTable(saruman, troop);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(gandalf, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(words));

		// One wound fewer, and another action taken so that the game asks afresh.
		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(words);

		// One exertion is all he has: that is exhausting him.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(8, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, words);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAGandalfArtifactTheEventMayBeShuffledIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var palantir = scn.GetFreepsCard("palantir");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words, fodder1);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, palantir);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(words);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		// The discard a player may make first, taken this time.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fodder1));
		scn.FreepsChooseCard(fodder1);

		// Nothing held, 8 drawn, and only then the question
		assertInZone(Zone.DISCARD, fodder1);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 8, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.FreepsDecisionAvailable("shuffle"));

		scn.FreepsChooseYes();

		assertInZone(Zone.DECK, words);
		assertNotInZone(Zone.DISCARD, words);
		// 8 drawn, 1 shuffled in
		assertEquals(deck - 7, scn.GetFreepsDeckCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheHandIsReconciledBeforeTheEventIsShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDeckScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var palantir = scn.GetFreepsCard("palantir");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words, fodder1);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, palantir);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The draw deck is two more Axe Strikes and this player's own copy of the Troop.
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(words);
		scn.FreepsDeclineReconciliation();

		// The whole deck is drawn while the event is still being played: 1 card held + 3 drawn.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("shuffle"));

		scn.FreepsChooseYes();

		// Shuffled in afterwards, the event is the only card of the deck and was not drawn back.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertSame(words, scn.GetFreepsTopOfDeck());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReconcilingRestoresYourHinderedCardsSoAHinderedGandalfArtifactIsSpottedAfterwards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var palantir = scn.GetFreepsCard("palantir");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words, fodder1);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, palantir);
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// The only [gandalf] artifact is hindered, and so is a card the other player owns.
		scn.HinderCard(palantir, saruman);

		assertTrue(scn.IsHindered(palantir));
		assertTrue(scn.IsHindered(saruman));

		scn.FreepsPlayCard(words);
		scn.FreepsDeclineReconciliation();

		// Reconciling has restored the Palantir, which this player owns, and not the opponent's minion.
		assertEquals(8, scn.GetFreepsHandCount());
		assertFalse(scn.IsHindered(palantir));
		assertTrue(scn.IsHindered(saruman));
		assertTrue(scn.FreepsDecisionAvailable("shuffle"));

		scn.FreepsChooseYes();

		assertInZone(Zone.DECK, words);
		assertFalse(scn.IsHindered(palantir));
		assertTrue(scn.IsHindered(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void YourHinderedCardsAreRestoredBeforeTheDiscardThatBeginsTheReconciling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var palantir = scn.GetFreepsCard("palantir");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words, fodder1);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, palantir);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(palantir);

		assertTrue(scn.IsHindered(palantir));

		scn.FreepsPlayCard(words);

		// The discard a player may make is on offer and nothing has been discarded or drawn yet.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fodder1));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.IsHindered(palantir));
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
