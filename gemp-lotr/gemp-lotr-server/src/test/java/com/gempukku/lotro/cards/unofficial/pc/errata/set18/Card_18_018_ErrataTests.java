package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_018_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "68_18");
					put("axe2", "68_18");
					// Grimbeorn, Beorning Chieftain: the only Grimbeorn; [gandalf] Man, strength 4, vitality 5.
					// He heals at the start of each turn; his maneuver offer needs a minion in his owner's hand.
					put("grimbeorn", "14_6");
					// Dasron, Merchant From Dorwinion: [gandalf] Man companion, strength 6, vitality 3;
					// his fellowship offer needs 2 culture tokens, and there are none.
					put("dasron", "13_29");
					put("erland", "2_21");        // Erland, Advisor to Brand: [gandalf] Man ally; nothing that matters here
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is no Man
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man who is not [gandalf]

					put("secret", "1_71");        // Durin's Secret: [gandalf] Fellowship event
					put("servant", "1_83");       // Servant of the Secret Fire: [gandalf] Skirmish event (needs Gandalf)
					put("treachery", "1_86");     // Treachery Deeper Than You Know: [gandalf] event, never in this player's discard pile
					put("creation", "1_44");      // Foul Creation: an [elven] event

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("party", "1_158");        // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeorningAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Beorning Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be a [gandalf] Man.
		 * 	While bearer is Grimbeorn, each time he wins a skirmish you may take a [gandalf] event into hand
		 * 	from your discard pile (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Beorning Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForTwoOnAGandalfManWhoIsThenStrengthPlus2AndVitalityPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(grimbeorn, dasron, avenger, aragorn);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(dasron));
		assertEquals(3, scn.GetVitality(dasron));
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);

		// Two companions and an ally, all [gandalf] Men. Not the [gandalf] Ent, and not the [gondor] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimbeorn, dasron, erland));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dasron);

		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(axe, dasron);
		assertEquals(8, scn.GetStrength(dasron));
		assertEquals(4, scn.GetVitality(dasron));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenGrimbeornWinsASkirmishBearingTheAxeAGandalfEventMayBeTakenFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var secret = scn.GetFreepsCard("secret");
		var servant = scn.GetFreepsCard("servant");
		var creation = scn.GetFreepsCard("creation");
		var treachery = scn.GetFreepsCard("treachery");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn, dasron);
		scn.AttachCardsTo(grimbeorn, axe);
		// A second Axe on a [gandalf] Man who is not Grimbeorn: it makes no offer of its own.
		scn.AttachCardsTo(dasron, axe2);
		// Erland is a [gandalf] card that is no event, Foul Creation an event that is not [gandalf].
		scn.MoveCardsToDiscard(secret, servant, erland, creation);
		// The third [gandalf] event is in the opponent's discard pile, and this player's copy is in his draw deck.
		scn.MoveCardsToDiscard(scn.GetShadowCard("treachery"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);

		// 4 + 2 for the Axe against 5.
		assertEquals(6, scn.GetStrength(grimbeorn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(axe2));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(secret, servant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(erland, creation, treachery));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(servant);

		assertInHand(servant);
		assertInDiscard(secret, erland, creation);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADeclinedOfferTakesNothingAndIsMadeAgainWhenGrimbeornWinsHisSecondSkirmishOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var secret = scn.GetFreepsCard("secret");
		var servant = scn.GetFreepsCard("servant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		scn.MoveCardsToDiscard(secret, servant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// A fierce minion that loses and lives gives Grimbeorn two skirmishes to win in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInDiscard(secret, servant);

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		// Nothing was taken at the first win, so the once has not been used.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(secret);

		assertInHand(secret);
		assertInDiscard(servant);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HavingTakenAnEventASecondWinInTheSameTurnBringsNoOfferAndAWinOnTheNextTurnDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var secret = scn.GetFreepsCard("secret");
		var servant = scn.GetFreepsCard("servant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		scn.MoveCardsToDiscard(secret, servant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(servant);
		assertInHand(servant);

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		// He has won again (the Savage's second wound), and a [gandalf] event is still there to be taken.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(secret);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The opponent's turn, and then this player's next.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertInDiscard(secret);
		assertAttachedTo(axe, grimbeorn);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		// Durin's Secret is the one [gandalf] event left in the discard pile: taken without a choice.
		scn.FreepsAcceptOptionalTrigger();
		assertInHand(secret);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenGrimbeornLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var secret = scn.GetFreepsCard("secret");
		var servant = scn.GetFreepsCard("servant");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		scn.MoveCardsToDiscard(secret, servant);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, party);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		// 6 against 9: he loses, and takes 2 wounds from a minion with damage +1.
		assertEquals(2, scn.GetWoundsOn(grimbeorn));
		assertEquals(0, scn.GetWoundsOn(party));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(secret, servant);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGandalfEventInTheDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var erland = scn.GetFreepsCard("erland");
		var creation = scn.GetFreepsCard("creation");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		// A [gandalf] ally and an [elven] event in his own discard pile; a [gandalf] event in the opponent's.
		scn.MoveCardsToDiscard(erland, creation);
		scn.MoveCardsToDiscard(scn.GetShadowCard("treachery"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileAnotherGandalfManBearsTheAxeNeitherHisWinNorGrimbeornsBringsAnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var dasron = scn.GetFreepsCard("dasron");
		var secret = scn.GetFreepsCard("secret");
		var servant = scn.GetFreepsCard("servant");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(grimbeorn, dasron);
		scn.AttachCardsTo(dasron, axe);
		scn.MoveCardsToDiscard(secret, servant);

		scn.StartGame();
		// Grimbeorn's own 4 would lose to the Savage's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, grimbeorn, null, 3));
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dasron, savage}, new PhysicalCardImpl[]{grimbeorn, savage2});

		// The bearer, a [gandalf] Man who is not Grimbeorn, wins: 6 + 2 against 5.
		scn.FreepsResolveSkirmish(dasron);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Grimbeorn, who bears nothing, wins: 7 against 5.
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertInDiscard(secret, servant);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
