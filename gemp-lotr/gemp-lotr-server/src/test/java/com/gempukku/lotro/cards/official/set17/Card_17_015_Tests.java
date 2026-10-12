package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "17_15");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, cost 4; a strength bonus and nothing else
					put("radagast", "9_26");    // Radagast, The Brown: the [gandalf] Wizard who is not Gandalf; move limit +1, and a trigger on a regroup move
					put("saruman", "32_24");    // Saruman, The White: the [gandalf] Wizard who is an ally, cost 4; abilities used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard

					// Minions of three cultures and three races.
					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai
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
		 * Game Text: <b>Spell</b>. Spot a [gandalf] Wizard to search a Shadow player's discard pile and choose a
		 * 	minion, then return that minion to its owner's hand. If you do, you may reveal that Shadow player's hand
		 * 	and discard a minion found there.
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
	public void ReturnsAMinionFromTheShadowPlayersDiscardPileAndMayThenRevealHisHandAndDiscardAMinionFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var ownTroop = scn.GetFreepsCard("troop");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		var elf = scn.GetShadowCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		// Still in the Shadow player's draw deck.
		var shadowTroop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(ownTroop);
		scn.MoveCardsToDiscard(runner, savage, fear, elf);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.MarkGameLog();
		scn.FreepsPlayCard(light);

		assertEquals(1, scn.GetTwilight());
		// The two minions in the Shadow player's discard pile, and nothing else there or in the other pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fear, elf, ownTroop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(savage);

		// To its owner's hand, which is the Shadow player's.
		assertInZone(Zone.HAND, savage);
		assertInDiscard(runner, fear, elf);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());

		// "You may": nothing is shown until the player has said yes.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reveal"));
		scn.FreepsChooseYes();

		// The whole hand is shown to the player of the event; the owner of the hand is told by the game log.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(shadowTroop, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkMentions(savage, lemenya, wreathed));
		assertFalse(scn.GameLogSinceMarkMentions(shadowTroop));
		scn.FreepsDismissRevealedCards();

		// A minion of that hand, whether it was returned or was there already, or none (Q-0278). Not the condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wreathed));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(lemenya);

		assertInDiscard(lemenya);
		assertInZone(Zone.HAND, savage, wreathed);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningToRevealLeavesTheReturnedMinionInHandAndShowsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(runner, savage);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		scn.FreepsPlayCard(light);
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInZone(Zone.HAND, runner);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reveal"));
		scn.FreepsChooseNo();

		// Nothing is shown and nothing is discarded.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInZone(Zone.HAND, runner, lemenya, wreathed);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AfterTheRevealTheFreePeoplesPlayerMayDiscardNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(runner, savage);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		scn.FreepsPlayCard(light);
		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseYes();
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// "You may reveal ... and discard a minion found there": the hand is shown, and the discard is a choice
		// of no minion or one (Q-0278).  None is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, lemenya));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.HAND, runner, lemenya, wreathed);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RadagastIsAGandalfWizardAndTheMinionJustReturnedMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("radagast");
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(runner, fear);
		scn.MoveCardsToHand(wreathed);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		// The one minion there is: no choice to make.
		assertInZone(Zone.HAND, runner);
		assertInDiscard(fear);
		assertEquals(2, scn.GetShadowHandCount());

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reveal"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Runner is the only minion found in that hand: the choice is still asked, none being an answer
		// (Q-0278), and the Runner goes back to the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wreathed));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner, fear);
		assertInZone(Zone.HAND, wreathed);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable("avenger");
		scn.MoveCardsToHand(light, gandalf);
		scn.MoveCardsToDiscard(runner, savage);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion and no Wizard.  Every printed Free Peoples Wizard is a
		// [gandalf] card, so no Wizard of another culture stands beside him.
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
	public void WithNoMinionInTheShadowPlayersDiscardPileNothingIsReturnedAndNothingIsAskedOrShown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var ownTroop = scn.GetFreepsCard("troop");
		var fear = scn.GetShadowCard("fear");
		var elf = scn.GetShadowCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToHand(light);
		// A minion in the Free Peoples player's own discard pile is not in "a Shadow player's discard pile".
		scn.MoveCardsToDiscard(ownTroop);
		scn.MoveCardsToDiscard(fear, elf);
		scn.MoveCardsToHand(lemenya, wreathed);

		scn.StartGame();

		// An event may be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		// "If you do": no minion was returned, so the player is not asked and the hand is not shown.
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(light, fear, elf, ownTroop);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInZone(Zone.HAND, lemenya, wreathed);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
