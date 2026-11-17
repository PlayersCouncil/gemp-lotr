package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_91_011_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("condition", "1_130");  // No Ordinary Storm: [isengard] condition, support area, cost 1; answers only the play of a weather condition
					put("condition2", "1_209"); // Blade Tip: [wraith] condition, support area, cost 0; a response when a Nazgul wins a skirmish
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition, support area, cost 3; nothing without a [sauron] Orc
					put("possession", "1_216"); // Morgul Blade: Shadow possession, cost 1; bearer must be a Nazgul
					put("lemenya", "1_232");    // Ulaire Lemenya: a Nazgul to bear the Blade; his text reaches only bearers of [wraith] conditions
					put("runner", "1_178");     // Goblin Runner: minion, cost 1, site 4 (roaming at site 2); adds (2) when played
					put("ally", "1_69");        // Albert Dreary: a Free Peoples ally, no card the Shadow player may play
					put("swarms", "1_183");     // Goblin Swarms: [moria] condition, support area, cost 1; a condition for the hand, of a blueprint in no pile
					// Axe Strike, a Free Peoples event: cards to discard from hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_11"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("condition", "1_130");  // No Ordinary Storm: [isengard] condition, support area, cost 1; answers only the play of a weather condition
					put("condition2", "1_209"); // Blade Tip: [wraith] condition, support area, cost 0; a response when a Nazgul wins a skirmish
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition, support area, cost 3; nothing without a [sauron] Orc
					put("possession", "1_216"); // Morgul Blade: Shadow possession, cost 1; bearer must be a Nazgul
					put("lemenya", "1_232");    // Ulaire Lemenya: a Nazgul to bear the Blade; his text reaches only bearers of [wraith] conditions
					put("runner", "1_178");     // Goblin Runner: minion, cost 1, site 4 (roaming at site 2); adds (2) when played
					put("ally", "1_69");        // Albert Dreary: a Free Peoples ally, no card the Shadow player may play
					// Axe Strike, a Free Peoples event: cards to discard from hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_11", null
		);
	}

	@Test
	public void PlayConditionFromDiscardStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_11
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Shadow: Discard 3 cards from hand to play a condition from your discard pile.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 91_11", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowCanPlayConditionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: "Shadow: Discard 3 cards from hand to play a condition from your discard pile."
		// Shadow should be able to use the ability to play a condition from discard.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var condition = scn.GetShadowCard("condition");
		var condition2 = scn.GetShadowCard("condition2");
		var possession = scn.GetShadowCard("possession");
		var runner = scn.GetShadowCard("runner");
		var ally = scn.GetShadowCard("ally");
		// A condition the pool can pay for, but in the Free Peoples player's discard pile, and of a title that is in no pile of the Shadow player's
		var freepsPileBowmen = scn.GetFreepsCard("bowmen");

		scn.MoveCardsToDiscard(condition, condition2, possession, runner, ally);
		scn.MoveCardsToDiscard(freepsPileBowmen);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		// With a Nazgul on the table the Morgul Blade in the discard pile is a card that could be played
		scn.MoveMinionsToTable("lemenya");
		scn.FreepsPass(); // move to site 2

		// 2 for the site + 1 companion: enough for any of the Shadow cards in the pile, the roaming Runner included
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDiscardCount());

		// Shadow phase — should be able to use the mod's ability
		assertTrue(scn.ShadowActionAvailable(shadowMod));
		scn.ShadowUseCardAction(shadowMod);

		// Should have discarded 3 cards from hand
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDiscardCount());

		// Should only be able to choose a condition, not a possession, a minion or an ally
		assertTrue(scn.ShadowHasCardChoiceAvailable(condition));
		assertTrue(scn.ShadowHasCardChoiceAvailable(condition2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(possession));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(ally));
		// Nor a condition from the other player's pile
		assertFalse(scn.ShadowHasCardChoiceAvailable(freepsPileBowmen));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(condition);

		// Condition should now be in play, and paid for: No Ordinary Storm costs 1
		assertInPlay(condition);
		assertInZone(Zone.SUPPORT, condition);
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(condition2, possession, runner, ally);
		assertEquals(7, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotUseWithoutThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: Should not be usable without 3 cards in hand.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var condition = scn.GetShadowCard("condition");

		scn.MoveCardsToDiscard(condition);
		scn.MoveCardsToShadowHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move

		// Should NOT be able to use the ability with only 2 cards in hand
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(shadowMod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsCopyDoesNotWorkForShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: "your discard pile" — Freeps' copy should not let Shadow use it.

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var condition = scn.GetShadowCard("condition");

		scn.MoveCardsToDiscard(condition);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		scn.FreepsPass(); // move

		// Shadow should NOT be able to use Freeps' copy
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(freepsMod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoConditionInTheDiscardPile()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var possession = scn.GetShadowCard("possession");
		var runner = scn.GetShadowCard("runner");

		// Shadow cards that could be played, a possession and a minion, and no condition
		scn.MoveCardsToDiscard(possession, runner);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		scn.MoveMinionsToTable("lemenya");
		scn.FreepsPass(); // move

		// The play is part of the price: with nothing to play, the three cards may not be discarded for nothing
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(shadowMod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedOnceThePoolCannotPayForTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var bowmen = scn.GetShadowCard("bowmen");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToDiscard(bowmen);
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		scn.FreepsPass(); // move

		// 2 for the site + 1 companion: just enough for Orc Bowmen, which costs 3
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(shadowMod));

		// 3 - 1 to play - 2 roaming + 2 that the Runner adds when played
		scn.ShadowPlayCard(runner);
		assertEquals(2, scn.GetTwilight());

		// One short of the condition's cost, with three cards still in hand
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(shadowMod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerOnePlaysAConditionInPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var condition = scn.GetFreepsCard("condition");
		var condition2 = scn.GetFreepsCard("condition2");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player
		scn.SkipToOtherPlayersTurn();
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		scn.MoveCardsToDiscard(condition, condition2);
		scn.ShadowPass(); // player 2's fellowship moves to site 2

		// 2 for the site + 1 companion
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(freepsMod));
		scn.FreepsUseCardAction(freepsMod);

		// Three of the four cards in hand, of his choosing
		assertEquals(3, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(filler1, filler2, filler4);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, filler3);
		assertInDiscard(filler1, filler2, filler4);

		assertTrue(scn.FreepsHasCardChoiceAvailable(condition));
		assertTrue(scn.FreepsHasCardChoiceAvailable(condition2));
		scn.FreepsChooseCard(condition2);

		// Blade Tip costs nothing
		assertInZone(Zone.SUPPORT, condition2);
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(condition);
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AConditionDiscardedToPayForItIsNotTheConditionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 4.2 "effect" (the They Are Coming example, the same text) and Q-0320: a card discarded from hand to pay
		// the cost cannot be the card then played from the discard pile (F-0544).
		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var condition = scn.GetShadowCard("condition");
		var condition2 = scn.GetShadowCard("condition2");
		var swarms = scn.GetShadowCard("swarms");

		scn.MoveCardsToDiscard(condition, condition2);
		scn.MoveCardsToShadowHand("swarms", "filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// 2 for the site + 1 companion: enough for Goblin Swarms, which costs 1
		assertEquals(3, scn.GetTwilight());
		assertInHand(swarms);
		assertInDiscard(condition, condition2);
		assertTrue(scn.ShadowActionAvailable(shadowMod));
		scn.ShadowUseCardAction(shadowMod);

		// The whole hand of three pays the cost, Goblin Swarms among them
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(swarms);

		// The two conditions that lay in the pile before the cost, and not the one that lies there now because it paid
		assertTrue(scn.ShadowHasCardChoiceAvailable(condition, condition2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(swarms));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(condition);

		assertInZone(Zone.SUPPORT, condition);
		assertInDiscard(condition2, swarms);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
