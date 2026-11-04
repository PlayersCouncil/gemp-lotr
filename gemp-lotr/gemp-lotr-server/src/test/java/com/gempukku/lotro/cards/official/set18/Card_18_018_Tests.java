package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "18_18");
					put("axe2", "18_18");
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
					put("stern", "19_9");         // Stern Words: [gandalf] condition
					put("treachery", "1_86");     // Treachery Deeper Than You Know: [gandalf] event, never in this player's discard pile
					put("creation", "1_44");      // Foul Creation: an [elven] event

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("pursuer", "4_190");      // Uruk Pursuer: strength 5, vitality 3, fierce; its text is about unbound Hobbits
					put("whelp", "7_202");        // Morgul Whelp: strength 3 while the Ring-bearer has no wound
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
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be a [gandalf] Man.
		 * 	While Grimbeorn is bearing this possession, each time he wins a skirmish, you may take a [gandalf] card
		 * 	from your discard pile into hand.
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
	public void IsPlayedOnAGandalfMan() throws DecisionResultInvalidException, CardNotFoundException {
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

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);

		// Two companions and an ally, all [gandalf] Men. Not the [gandalf] Ent, and not the [gondor] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimbeorn, dasron, erland));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dasron);

		assertAttachedTo(axe, dasron);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenGrimbeornWinsASkirmishBearingTheAxeAGandalfCardMayBeTakenFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var secret = scn.GetFreepsCard("secret");
		var stern = scn.GetFreepsCard("stern");
		var creation = scn.GetFreepsCard("creation");
		var treachery = scn.GetFreepsCard("treachery");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn, dasron);
		scn.AttachCardsTo(grimbeorn, axe);
		// A second Axe on a [gandalf] Man who is not Grimbeorn: it makes no offer of its own.
		scn.AttachCardsTo(dasron, axe2);
		// [gandalf] cards of three types: an event, an ally and a condition.  Foul Creation is an event that is not [gandalf].
		scn.MoveCardsToDiscard(secret, erland, stern, creation);
		// A [gandalf] event in the opponent's discard pile; this player's copy is in his draw deck.
		scn.MoveCardsToDiscard(scn.GetShadowCard("treachery"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, savage);
		scn.FreepsResolveSkirmish(grimbeorn);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		scn.PassSkirmishActions();

		// 4 + 2 against 5: he wins.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(axe2));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(secret, erland, stern));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(creation, treachery));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(erland);

		assertInHand(erland);
		assertInDiscard(secret, stern, creation);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAtEachWinEvenAfterACardWasTakenAndADeclinedOfferTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var secret = scn.GetFreepsCard("secret");
		var stern = scn.GetFreepsCard("stern");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		scn.MoveCardsToDiscard(secret, stern);

		scn.StartGame();
		// A fierce minion that loses and lives gives Grimbeorn two skirmishes to win in one turn.
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbeorn, pursuer);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsAcceptOptionalTrigger();
		// The event is taken, so that the one [gandalf] card left in the pile is a condition: the second offer
		// does not depend on a [gandalf] event being there.
		scn.FreepsChooseCards(secret);
		assertInHand(secret);
		assertInDiscard(stern);

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(grimbeorn, pursuer);
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassFierceSkirmishActions();

		// He wins again, and the offer is made again: there is no limit on it.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: Stern Words stays in the discard pile, and the hand is as it was.
		assertInDiscard(stern);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(secret);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenGrimbeornLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var secret = scn.GetFreepsCard("secret");
		var stern = scn.GetFreepsCard("stern");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		scn.MoveCardsToDiscard(secret, stern);

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
		assertInDiscard(secret, stern);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGandalfCardInTheDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var creation = scn.GetFreepsCard("creation");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.AttachCardsTo(grimbeorn, axe);
		// A [gondor] companion and an [elven] event in his own discard pile; a [gandalf] event in the opponent's.
		scn.MoveCardsToDiscard(aragorn, creation);
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
		var stern = scn.GetFreepsCard("stern");
		var savage = scn.GetShadowCard("savage");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCompanionsToTable(grimbeorn, dasron);
		scn.AttachCardsTo(dasron, axe);
		scn.MoveCardsToDiscard(secret, stern);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, whelp);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dasron, savage}, new PhysicalCardImpl[]{grimbeorn, whelp});

		// The bearer, a [gandalf] Man who is not Grimbeorn, wins: 6 + 2 against 5.
		scn.FreepsResolveSkirmish(dasron);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Grimbeorn, who bears nothing, wins: 4 against 3, and the Whelp (vitality 1) dies.
		scn.FreepsResolveSkirmish(grimbeorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, whelp);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertInDiscard(secret, stern);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
