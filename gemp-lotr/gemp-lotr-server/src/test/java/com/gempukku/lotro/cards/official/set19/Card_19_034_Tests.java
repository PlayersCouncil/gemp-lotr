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

public class Card_19_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twilight", "19_34");
					put("twilight2", "19_34");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9, vitality 2; her text
					// reaches only a companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Morgul Hound: a [wraith] Orc, strength 5, vitality 2; strength +4 and fierce only at 2 wounds on
					// the Ring-bearer
					put("hound", "7_192");
					// Ulaire Nelya, Ringwraith in Twilight: a [wraith] Nazgul, strength 10, vitality 3; fierce and
					// damage +1 while 2 burdens can be spotted
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: a Nazgul who is not [wraith] ([raider]); his texts need 3 burdens
					put("khamul", "103_61");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; its text answers its own play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InTwilightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: In Twilight
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Strength: +2
		 * Game Text: Bearer must be a [wraith] minion.
		 * 	When you play this, you may draw a card.
		 * 	Each time bearer wins a skirmish, you may transfer this condition to another [wraith] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twilight");

		assertEquals("In Twilight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAWraithMinionAndMayThenDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twilight = scn.GetShadowCard("twilight");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(twilight);
		assertInZone(Zone.HAND, twilight);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, khamul, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.HAND, twilight);
		assertTrue(scn.ShadowPlayAvailable(twilight));
		scn.ShadowPlayCard(twilight);
		// A [wraith] Nazgul and a [wraith] Orc; not the [raider] Nazgul, the [moria] Orc or Frodo.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, runner, frodo));
		scn.ShadowChooseCard(lemenya);
		assertAttachedTo(twilight, lemenya);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDrawDeck().size();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilight));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDrawMayBeDeclinedAndAnotherCopyPlayedLaterOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twilight = scn.GetShadowCard("twilight");
		var twilight2 = scn.GetShadowCard("twilight2");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(twilight, twilight2);
		assertInZone(Zone.HAND, twilight);
		assertInZone(Zone.HAND, twilight2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.HAND, twilight);
		assertInZone(Zone.HAND, twilight2);
		scn.ShadowPlayCard(twilight);
		scn.ShadowChooseCard(lemenya);
		assertAttachedTo(twilight, lemenya);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDrawDeck().size();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilight));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDrawDeck().size());

		// The second copy is played: its own draw is offered, and the first copy's is not offered again.
		scn.ShadowPlayCard(twilight2);
		scn.ShadowChooseCard(hound);
		assertAttachedTo(twilight2, hound);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilight2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(twilight));
		scn.ShadowAcceptOptionalTrigger();
		// The second copy left the hand and one card was drawn.
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItMayBeTransferredToAnotherWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twilight = scn.GetShadowCard("twilight");
		var twilight2 = scn.GetShadowCard("twilight2");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		// Two burdens: Nelya is fierce and damage +1.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(nelya, lemenya, hound, runner);
		// A second copy on a [wraith] minion who fights no skirmish.
		scn.AttachCardsTo(nelya, twilight);
		scn.AttachCardsTo(hound, twilight2);
		assertAttachedTo(twilight, nelya);
		assertAttachedTo(twilight2, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();

		// Nelya (10 + 2) beats Aragorn (8): only the copy she bears is offered.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(twilight2));
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(twilight, nelya);
		assertAttachedTo(twilight2, hound);

		// Nelya is fierce: she beats Boromir (7) as well, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, nelya);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(twilight2));
		int hand = scn.GetShadowHandCount();
		scn.ShadowAcceptOptionalTrigger();

		// Another [wraith] minion, a Nazgul or an Orc: not Nelya, who bears it, the [moria] Orc or a companion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya, runner, aragorn, boromir, frodo));
		scn.ShadowChooseCard(lemenya);
		assertAttachedTo(twilight, lemenya);
		assertAttachedTo(twilight2, hound);

		// A transfer is no play: no draw is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(twilight));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesOrAnotherWraithMinionWinsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twilight = scn.GetShadowCard("twilight");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.AttachCardsTo(hound, twilight);
		assertAttachedTo(twilight, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, lemenya }, new PhysicalCardImpl[] { aragorn, hound });

		// Lemenya (9), who bears no copy, beats Boromir (7): nothing is offered.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The Hound (5 + 2) loses to Aragorn (8) and lives through it: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(twilight, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerIsTheOnlyWraithMinionTheTransferIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twilight = scn.GetShadowCard("twilight");
		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		// The Goblin Runner is a minion, but not [wraith].
		scn.MoveMinionsToTable(lemenya, runner);
		scn.AttachCardsTo(lemenya, twilight);
		assertAttachedTo(twilight, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, lemenya);
		scn.ShadowDeclineAssignments();

		// Lemenya (9 + 2) beats Boromir (7), and no other [wraith] minion could bear the condition.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(twilight, lemenya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
