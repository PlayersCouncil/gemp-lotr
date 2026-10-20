package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("words", "19_9");
					put("words2", "19_9");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; his text is about his strength
					put("whitesaruman", "32_24"); // Saruman, The White: a [gandalf] Wizard ally; a Gandalf is defender +1 beside him, abilities used only on purpose
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is no Wizard
					put("barliman", "1_70");      // Barliman Butterbur: a [gandalf] ally who is no Wizard; a fellowship ability never used
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8; a maneuver ability never used
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, damage +1; a skirmish ability never used

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("seeker", "4_195");       // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a bearer of a search card only
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SternWordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Stern Words
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Strength: +2
		 * Game Text: To play, spot a [gandalf] Wizard.
		 * 	Bearer must be a companion.
		 * 	Each time bearer wins a skirmish, you may transfer this condition to another companion.
		 * 	At the end of each turn, return this condition to hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("words");

		assertEquals("Stern Words", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpottedAndThenGoesOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(words, gandalf);
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		// A [gandalf] companion and a [gandalf] ally who are no Wizards, and no Wizard.
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInZone(Zone.SUPPORT, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(words);
		// Every companion may bear it; the ally may not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf, avenger, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(words, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyGandalfWizardIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var whiteSaruman = scn.GetFreepsCard("whitesaruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(words);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(whiteSaruman);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, whiteSaruman);
		assertTrue(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(words);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(words, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItMayBeTransferredToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var words2 = scn.GetFreepsCard("words2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(aragorn, gandalf, gimli);
		scn.MoveAlliesToTable(barliman);
		// A second copy on a companion who fights no skirmish.
		scn.AttachCardsTo(aragorn, words);
		scn.AttachCardsTo(gandalf, words2);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, seeker);

		assertAttachedTo(words, aragorn);
		assertAttachedTo(words2, gandalf);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gimli, savage }, new PhysicalCardImpl[] { aragorn, seeker });

		// Gimli (6), who bears no Stern Words, beats Uruk Savage (5): nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		// One wound for the win and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Aragorn (8 + 2) beats Uruk Seeker (6), who lives through it: only the copy he bears is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(words));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(words2));

		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(words, aragorn);
		assertAttachedTo(words2, gandalf);

		// The Seeker is fierce: Aragorn beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, seeker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(words));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(words2));

		scn.FreepsAcceptOptionalTrigger();
		// Another companion: not the bearer, not the ally, not a minion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, barliman, savage));

		scn.FreepsChooseCard(gimli);
		assertAttachedTo(words, gimli);
		assertAttachedTo(words2, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerIsTheOnlyCompanionTheTransferIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var barliman = scn.GetFreepsCard("barliman");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		// The ally is a character who may not bear it.
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(frodo, words);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		assertAttachedTo(words, frodo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// Frodo (3 + 1 Ring + 2) beats Uruk Savage (5), and no other companion could bear the condition.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(words, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, words);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		assertAttachedTo(words, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Goblin Patrol Troop (13) beats Aragorn (8 + 2) without overwhelming him.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(words, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheEndOfTheTurnEachCopyReturnsToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");
		var words2 = scn.GetFreepsCard("words2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, words);
		scn.AttachCardsTo(gimli, words2);

		scn.StartGame();

		assertAttachedTo(words, aragorn);
		assertAttachedTo(words2, gimli);

		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The regroup phase is over, and the turn is not: the conditions are where they were.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(words, aragorn);
		assertAttachedTo(words2, gimli);

		scn.SkipToOtherPlayersTurn();

		assertInZone(Zone.HAND, words, words2);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
