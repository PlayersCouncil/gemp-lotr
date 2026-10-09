package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_015_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "67_15");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, cost 4; a strength bonus and nothing else
					put("radagast", "9_26");    // Radagast, The Brown: the [gandalf] Wizard who is not Gandalf; move limit +1, and a trigger on a regroup move
					put("saruman", "32_24");    // Saruman, The White: the [gandalf] Wizard who is an ally, cost 4; abilities used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, made a Wizard by the test

					// Minions of four cultures and four races.
					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl
					put("troop", "1_177");      // Goblin Patrol Troop: the minion in the Free Peoples player's own discard pile
					// Cards of the Shadow player's that are no minions.
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition
					put("wreathed", "1_238");   // Wreathed in Shadow: [wraith] condition
					put("elf", "1_53");         // Lórien Elf: a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ANewLightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: A New Light
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spell.
		 * Spot a [gandalf] Wizard to search a Shadow player's discard pile and choose 2 minions, then return those minions to their owner's hand. If you do, reveal that hand and discard a revealed minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("light");

		assertEquals("A New Light", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReturnsTwoMinionsFromTheShadowPlayersDiscardPileThenRevealsHisHandAndDiscardsAMinionFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var ownTroop = scn.GetFreepsCard("troop");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var fear = scn.GetShadowCard("fear");
		var elf = scn.GetShadowCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		// Still in the Shadow player's draw deck.
		var shadowTroop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(ownTroop);
		scn.MoveCardsToDiscard(runner, savage, brigand, fear, elf);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.MarkGameLog();
		scn.FreepsPlayCard(light);

		assertEquals(1, scn.GetTwilight());
		// The three minions in the Shadow player's discard pile, and nothing else there or in the other pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fear, elf, ownTroop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(runner, savage);

		assertInHand(runner, savage);
		assertInDiscard(brigand, fear, elf);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());

		// The whole hand is shown to the player of the event; the owner of the hand is told by the game log.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(shadowTroop, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkMentions(runner, savage, lemenya, wreathed));
		assertFalse(scn.GameLogSinceMarkMentions(shadowTroop));
		scn.FreepsDismissRevealedCards();

		// A minion of that hand, whether it was returned or was there already. Not the condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wreathed));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(lemenya);

		assertInDiscard(lemenya);
		assertInHand(runner, savage, wreathed);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable("avenger");
		scn.MoveCompanionsToTable(aragorn);
		// A Wizard who is not a [gandalf] card: every Free Peoples Wizard there is, is one.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.WIZARD));
		scn.MoveCardsToHand(light, gandalf);
		scn.MoveCardsToDiscard(runner, savage);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion and no Wizard; Aragorn is a Wizard here and no [gandalf] card.
		assertFalse(scn.FreepsPlayAvailable(light));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(light));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyWhoIsAGandalfWizardIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var saruman = scn.GetFreepsCard("saruman");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(light, saruman);
		scn.MoveCardsToDiscard(runner, savage);

		scn.StartGame();

		// No Wizard of any kind is in play.
		assertFalse(scn.FreepsPlayAvailable(light));

		scn.FreepsPlayCard(saruman);
		assertInZone(Zone.SUPPORT, saruman);
		assertTrue(scn.FreepsPlayAvailable(light));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RadagastIsAGandalfWizardAndBothOfTwoMinionsInThePileAreReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable("radagast");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(runner, savage, fear);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		// Exactly the two there are: no choice to make.
		assertInHand(runner, savage);
		assertInDiscard(fear);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(savage);

		assertInDiscard(savage);
		assertInHand(runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMinionInTheShadowPlayersDiscardPileNothingIsReturnedAndHisHandIsNotRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var fear = scn.GetShadowCard("fear");
		var elf = scn.GetShadowCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(fear, elf);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		// An event may be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertEquals(1, scn.GetTwilight());
		assertInDiscard(light, fear, elf);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInHand(lemenya, wreathed);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Ignore("Q-0169: with one minion in the discard pile, is \"If you do\" met by returning that one?")
	@Test
	public void WithOneMinionInTheShadowPlayersDiscardPileItIsReturnedAndHisHandIsNotRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(runner, fear);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		// As much as can be done is done: the one minion goes back to its owner's hand.
		assertInHand(runner);
		assertInDiscard(fear);
		// Two minions were not returned, so nothing is revealed and nothing discarded.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInHand(lemenya, wreathed);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
