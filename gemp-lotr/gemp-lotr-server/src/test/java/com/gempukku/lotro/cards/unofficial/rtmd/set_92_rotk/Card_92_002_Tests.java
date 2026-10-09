package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_002_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Grima, Wormtongue: Isengard Man minion, twilight 2, str 4, vit 3; a maneuver ability only
					put("grima", "4_154");
					// Rohirrim Traitor: a second Isengard Man minion, not unique, vit 2
					put("traitor", "6_72");
					// Uruk Savage: an Isengard minion who is no Man, twilight 2, vit 3; damage +1 and nothing else
					put("savage", "1_151");
					put("decksavage", "1_151");
					// Easterling Axeman: a Man minion who is not Isengard (Raider), vit 2; nothing below 2 burdens
					put("axeman", "4_224");
					// Saruman's Ambition: Isengard condition, support area, twilight 0
					put("condition1", "1_133");
					// No Ordinary Storm: Isengard condition, support area, twilight 1; answers only a weather condition
					put("storm", "1_130");
					// Uruk-hai Armory and Uruk Bloodlust: two more Isengard support-area conditions, twilight 1 and 3,
					// for the hand and the discard pile; neither does anything without an Uruk-hai
					put("armory", "1_157");
					put("bloodlust", "1_144");
					// Strength Born of Fear: a support-area condition that is not Isengard (Sauron), twilight 1
					put("fear", "1_278");
					// Goblin Runner for a second minion (needed to survive to next turn)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_2"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Grima, Wormtongue: Isengard Man minion, twilight 2, str 4, vit 3; a maneuver ability only
					put("grima", "4_154");
					// Rohirrim Traitor: a second Isengard Man minion, not unique, vit 2
					put("traitor", "6_72");
					// Uruk Savage: an Isengard minion who is no Man, twilight 2, vit 3; damage +1 and nothing else
					put("savage", "1_151");
					put("decksavage", "1_151");
					// Easterling Axeman: a Man minion who is not Isengard (Raider), vit 2; nothing below 2 burdens
					put("axeman", "4_224");
					// Saruman's Ambition: Isengard condition, support area, twilight 0
					put("condition1", "1_133");
					// No Ordinary Storm: Isengard condition, support area, twilight 1; answers only a weather condition
					put("storm", "1_130");
					// Uruk-hai Armory and Uruk Bloodlust: two more Isengard support-area conditions, twilight 1 and 3,
					// for the hand and the discard pile; neither does anything without an Uruk-hai
					put("armory", "1_157");
					put("bloodlust", "1_144");
					// Strength Born of Fear: a support-area condition that is not Isengard (Sauron), twilight 1
					put("fear", "1_278");
					// Goblin Runner for a second minion (needed to survive to next turn)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_2", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_2
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Shadow: Once per turn, exert your [Isengard] Man to play an [Isengard] condition from your draw deck.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowCanExertIsengardManToPlayConditionFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: Exert Isengard Man to play Isengard condition from draw deck.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveMinionsToTable(grima);
		// Leave condition in draw deck

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// Shadow phase: the ability should be available
		assertEquals(0, scn.GetWoundsOn(grima));
		assertInZone(Zone.DECK, condition1);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int otherShuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));

		// Should exert Grima (cost)
		assertEquals(1, scn.GetWoundsOn(grima));

		// Should be choosing an Isengard condition from deck — dismiss the revealed cards
		scn.ShadowDismissRevealedCards();

		scn.ShadowChooseCard(condition1);

		// Condition should now be in support area
		assertInZone(Zone.SUPPORT, condition1);
		// The deck that was looked through is one card shorter and was shuffled once; the other player's was not
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(otherShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsOneOfYourIsengardMenAndPlaysOnlyAnIsengardConditionFromYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var ambition = scn.GetShadowCard("condition1");
		var storm = scn.GetShadowCard("storm");
		var deckSavage = scn.GetShadowCard("decksavage");
		var fear = scn.GetShadowCard("fear");
		var armory = scn.GetShadowCard("armory");
		var bloodlust = scn.GetShadowCard("bloodlust");

		// Isengard conditions that are not in the draw deck
		scn.MoveCardsToHand(armory);
		scn.MoveCardsToDiscard(bloodlust);

		scn.StartGame();
		// Two Isengard Men, an Isengard minion who is no Man, and a Man who is not Isengard: all four could exert
		scn.MoveMinionsToTable(grima, traitor, savage, axeman);
		scn.SetTwilight(7);
		scn.FreepsPass(); // move to site 2

		// 7 + 2 for the site + 1 companion: enough for anything in the deck, the roaming Uruk Savage included
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(grima, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, axeman));
		scn.ShadowChooseCard(traitor);

		assertEquals(1, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// The deck is shown, and then the Isengard conditions in it are offered: one may be played, none must be
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambition, storm));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(deckSavage, fear, armory, bloodlust));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(storm);

		assertInZone(Zone.SUPPORT, storm);
		// No Ordinary Storm costs 1
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.DECK, ambition);
		assertInZone(Zone.DECK, fear);
		assertInZone(Zone.DECK, deckSavage);
		assertInZone(Zone.HAND, armory);
		assertInZone(Zone.DISCARD, bloodlust);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoIsengardConditionInTheDeckTheManStillExertsAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var grima = scn.GetShadowCard("grima");
		var ambition = scn.GetShadowCard("condition1");
		var storm = scn.GetShadowCard("storm");
		var armory = scn.GetShadowCard("armory");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var fear = scn.GetShadowCard("fear");

		// Every Isengard condition is out of the draw deck; a Sauron condition and an Uruk Savage are still in it
		scn.MoveCardsToHand(ambition, storm);
		scn.MoveCardsToDiscard(armory, bloodlust);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SetTwilight(7);
		scn.FreepsPass(); // move to site 2

		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(grima));
		int shuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetShadowDeckCount();

		// What is in the deck is hidden, and there is no penalty for not finding a card there: still offered
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		assertEquals(1, scn.GetWoundsOn(grima));

		// The deck is shown, with nothing in it that may be chosen
		scn.ShadowDismissRevealedCards();

		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, fear);
		assertInZone(Zone.HAND, ambition);
		assertInZone(Zone.HAND, storm);
		assertInZone(Zone.DISCARD, armory);
		assertInZone(Zone.DISCARD, bloodlust);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotUseWithoutIsengardMan() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: No Isengard Man on table = ability not available.

		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		// Don't put Grima on table, only runner (Orc, not Man), and beside it an Isengard Uruk-hai and a
		// Raider Man who could each exert
		scn.MoveMinionsToTable(runner, savage, axeman);

		scn.StartGame();
		scn.FreepsPass(); // move

		// Ability should NOT be available without an Isengard Man
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotUseWhileTheOnlyIsengardManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: the cost is an exertion, which an exhausted Man cannot pay.

		var scn = GetShadowScenario();

		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		// Rohirrim Traitor has 2 vitality, so one wound exhausts him. Beside him an Isengard Uruk-hai and a
		// Raider Man who are not exhausted: the Isengard Man is the only one of them the text names
		scn.MoveMinionsToTable(traitor, savage, axeman);
		scn.AddWoundsToChar(traitor, 1);

		scn.StartGame();
		scn.FreepsPass(); // move

		assertEquals(1, scn.GetWoundsOn(traitor));
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotUseTwiceInSameShadowPhase()throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — cannot use twice in the same shadow phase.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move

		// First use: should succeed
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCard(condition1);

		// Second use in same phase: should NOT be available, though Grima (1 wound of 3) could exert again
		// and No Ordinary Storm is still in the deck
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.DECK, scn.GetShadowCard("storm"));
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotUseInDoubleMoveShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — after using in the first shadow phase,
		// if FP moves again during regroup, the second shadow phase should NOT allow use.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(grima, runner);

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// First shadow phase: use the ability
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();

		// Skip to regroup and choose to move again
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();

		// Second shadow phase (after double move): should NOT be available
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void CanUseAgainOnNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — after a full turn cycle, should be usable again.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// Shadow phase: use the ability
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();

		// Skip the rest of this turn and start next turn
		scn.SkipToSite(3);

		// We should be in fellowship phase at site 3
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		//scn.MoveCardsToTopOfDeck(condition1);
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass(); // move to site 4

		// Shadow phase of new turn: ability should be available again
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void FreepsCopyDoesNotWorkForShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "your [Isengard] Man" — Freeps copy should not give Shadow the ability.

		var scn = GetFreepsScenario();

		var grima = scn.GetShadowCard("grima");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move

		// Shadow should NOT have the ability from Freeps' copy, though his Grima could exert and his own
		// Isengard conditions are in his draw deck
		assertEquals(0, scn.GetWoundsOn(grima));
		assertInZone(Zone.DECK, scn.GetShadowCard("condition1"));
		assertFalse(scn.ShadowActionAvailable(scn.GetFreepsCard("mod")));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerOneUsesItInPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var grima = scn.GetFreepsCard("grima");
		var ambition = scn.GetFreepsCard("condition1");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player. He drew at the end of his
		// own turn, so the condition is put back on his deck.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToTopOfDeck(ambition);
		scn.MoveMinionsToTable(grima);
		scn.ShadowPass(); // player 2's fellowship moves to site 2

		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertEquals(0, scn.GetWoundsOn(grima));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int otherShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		assertEquals(1, scn.GetWoundsOn(grima));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(ambition);

		assertInZone(Zone.SUPPORT, ambition);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(otherShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
