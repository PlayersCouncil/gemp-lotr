package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_042_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver
	// phase of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrush", "31_42");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion who is not Bilbo, vitality 4; +2
					// only while he bears a weapon. The Hobbit sets print no [shire] companion but Bilbo.
					put("merry", "1_303");

					// Free Peoples cards that may be taken: a companion, a possession and an event
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] Dwarf companion
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon
					put("weapon", "32_18");     // Iron-Forged Weapon: [esgaroth] possession, hand weapon
					put("barrels", "31_40");    // Barrels: [shire] Regroup event
					// Cards that may not: [gandalf], [elven], and a Shadow card
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] Wizard companion
					put("moonletters", "33_15");// Moon-Letters: [elven] Fellowship event
					put("thranduil", "31_12");  // Thranduil, Elven King: [elven] Elf ally
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc minion

					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OldThrushStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Old Thrush, Messenger Bird
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Bird
		 * Strength: +1
		 * Aid: Exert Bilbo.
		 * Game Text: <b>Aid</b> – Exert Bilbo. <helper>(At the start of the maneuver phase, you may exert Bilbo to transfer this to a companion.)</helper>
		 * 	<b>Maneuver:</b> If Old Thrush is attached, discard him to take into hand a Free Peoples card (except [gandalf] or [elven]) from your draw deck or discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrush");

		assertEquals("Old Thrush", card.getBlueprint().getTitle());
		assertEquals("Messenger Bird", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.BIRD, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsBilboToTransferOldThrushToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// Bilbo is the one who can pay: he exerts unasked, and Balin, who could, does not.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin));
		scn.FreepsChooseCard(balin);
		assertAttachedTo(thrush, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileBilboIsExhaustedAndOldThrushUnattachedHasNoManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thrush = scn.GetFreepsCard("thrush");
		var merry = scn.GetFreepsCard("merry");
		var dwalin = scn.GetFreepsCard("dwalin");
		var weapon = scn.GetFreepsCard("weapon");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(thrush);
		// Vitality 4 with 3 wounds: Bilbo cannot exert.
		scn.AddWoundsToChar(bilbo, 3);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		// Something he could take from each pile, were he attached.
		scn.MoveCardsToDiscard(weapon);
		assertInZone(Zone.DECK, dwalin);
		assertInZone(Zone.DISCARD, weapon);

		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, thrush);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
		// In the support area he is not attached.
		assertFalse(scn.FreepsActionAvailable(thrush));
	}

	@Test
	public void DiscardedHeTakesAFreePeoplesCardThatIsNotGandalfOrElvenFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var weapon = scn.GetFreepsCard("weapon");
		var barrels = scn.GetFreepsCard("barrels");
		var gandalf = scn.GetFreepsCard("gandalf");
		var moonletters = scn.GetFreepsCard("moonletters");
		var thranduil = scn.GetFreepsCard("thranduil");
		var aggressor = scn.GetFreepsCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.MoveCardsToDiscard(weapon);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DECK, dwalin, axe, barrels, gandalf, moonletters, thranduil, aggressor);
		assertInZone(Zone.DISCARD, weapon);

		scn.SkipToPhase(Phase.MANEUVER);
		// The Aid is offered for a follower already borne as well.
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(thrush));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.MarkGameLog();
		scn.FreepsUseCardAction(thrush);
		assertInZone(Zone.DISCARD, thrush);

		// Each pile holds a card he may take, so the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard"));
		scn.FreepsChoose("draw deck");

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(dwalin, axe, barrels));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, moonletters, thranduil, aggressor));
		scn.FreepsChooseCardBPFromSelection(dwalin);

		assertInZone(Zone.HAND, dwalin);
		assertInZone(Zone.DECK, axe, barrels, gandalf, moonletters, thranduil, aggressor);
		assertInZone(Zone.DISCARD, weapon);
		// The card taken is named in the game log, one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(dwalin));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(axe));
		// The deck looked through is shuffled once (nothing was played out of it); the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardedHeTakesAFreePeoplesCardThatIsNotGandalfOrElvenFromTheDiscardPileWithNoShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var merry = scn.GetFreepsCard("merry");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var weapon = scn.GetFreepsCard("weapon");
		var barrels = scn.GetFreepsCard("barrels");
		var gandalf = scn.GetFreepsCard("gandalf");
		var moonletters = scn.GetFreepsCard("moonletters");
		var aggressor = scn.GetFreepsCard("aggressor");
		var shadowAxe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.MoveCardsToDiscard(dwalin, weapon, barrels, gandalf, moonletters, aggressor);
		// A Free Peoples card in the other player's discard pile, of a title the player's own piles do not hold.
		scn.MoveCardsToDiscard(shadowAxe);
		scn.MoveCardsToHand(axe);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DISCARD, dwalin, weapon, barrels, gandalf, moonletters, aggressor, shadowAxe);
		assertInZone(Zone.HAND, axe);
		// Merry is a card the draw deck still holds that he may take.
		assertInZone(Zone.DECK, merry);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(thrush);
		assertInZone(Zone.DISCARD, thrush);
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard"));
		scn.FreepsChoose("discard");

		assertTrue(scn.FreepsHasCardChoiceAvailable(dwalin, weapon, barrels));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, moonletters, aggressor, shadowAxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(weapon);

		assertInZone(Zone.HAND, weapon, axe);
		assertInZone(Zone.DISCARD, dwalin, barrels, gandalf, moonletters, aggressor, shadowAxe);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheThrushDiscardedForTheCostIsNotTakenBackFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var weapon = scn.GetFreepsCard("weapon");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.MoveCardsToDiscard(dwalin, weapon);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DISCARD, dwalin, weapon);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.FreepsUseCardAction(thrush);
		assertInZone(Zone.DISCARD, thrush);
		scn.FreepsChoose("discard");

		// Old Thrush is a [shire] Free Peoples card in that pile now, but he was the price of the take.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dwalin, weapon));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush));
		scn.FreepsChooseCardBPFromSelection(dwalin);
		assertInZone(Zone.HAND, dwalin);
		assertInZone(Zone.DISCARD, thrush, weapon);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNothingToTakeInTheDrawDeckTheDiscardPileIsTakenFromUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var merry = scn.GetFreepsCard("merry");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var weapon = scn.GetFreepsCard("weapon");
		var barrels = scn.GetFreepsCard("barrels");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		// The deck holds only cards he may not take.
		scn.SetFreepsDrawDeck(gandalf, scn.GetFreepsCard("moonletters"), scn.GetFreepsCard("thranduil"), scn.GetFreepsCard("aggressor"));
		scn.MoveCardsToHand(merry, axe, weapon, barrels);
		scn.MoveCardsToDiscard(dwalin);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DECK, gandalf);
		assertInZone(Zone.DISCARD, dwalin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(thrush);
		// Nothing asked: Dwalin, the one card he may take, comes from the discard pile.
		assertInZone(Zone.HAND, dwalin);
		assertInZone(Zone.DISCARD, thrush);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNothingToTakeInTheDiscardPileTheDrawDeckIsSearchedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		// Only a [gandalf] card in the discard pile.
		scn.MoveCardsToDiscard(gandalf);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DECK, dwalin);
		assertInZone(Zone.DISCARD, gandalf);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.FreepsUseCardAction(thrush);
		// No question of which pile: the draw deck is shown at once.
		assertFalse(scn.FreepsChoiceAvailable("discard"));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(dwalin);
		assertInZone(Zone.HAND, dwalin);
		assertInZone(Zone.DISCARD, gandalf, thrush);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNothingToTakeInEitherPileTheManeuverActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var moonletters = scn.GetFreepsCard("moonletters");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, thrush);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SetFreepsDrawDeck(gandalf, scn.GetFreepsCard("thranduil"), scn.GetFreepsCard("aggressor"));
		scn.MoveCardsToHand(scn.GetFreepsCard("merry"), scn.GetFreepsCard("dwalin"), scn.GetFreepsCard("axe"),
				scn.GetFreepsCard("weapon"), scn.GetFreepsCard("barrels"));
		scn.MoveCardsToDiscard(moonletters);
		assertAttachedTo(thrush, balin);
		assertInZone(Zone.DECK, gandalf);
		assertInZone(Zone.DISCARD, moonletters);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thrush));
		assertAttachedTo(thrush, balin);
	}
}
