package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_271_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "4_271");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose

					// Cards for the hand, one of each kind; none is played
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion
					put("axe", "1_9");          // Dwarven Axe: a possession
					put("defiance", "1_37");    // Defiance: an event
					put("kingdom", "1_16");     // Greatest Kingdom of My People: a condition

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 only: two wounds on a loser
					put("skirmisher", "18_90"); // Orkish Skirmisher: strength 9; each time he wins, he may exert twice to wound a companion
					put("marksman", "1_176");   // Goblin Marksman: an archer and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éowyn, Sister-daughter of Théoden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Valiant</b>.<br><b>Response:</b> If Éowyn is exhausted and about to take a wound in a skirmish, discard 2 cards from hand to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Sister-daughter of Théoden", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void ExhaustedEowynDiscards2CardsToPreventEachWoundOfALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var axe = scn.GetFreepsCard("axe");
		var defiance = scn.GetFreepsCard("defiance");
		var kingdom = scn.GetFreepsCard("kingdom");
		scn.MoveCompanionsToTable(eowyn);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.SetFreepsHand(boromir, axe, defiance, kingdom);
		scn.MoveMinionsToTable(party);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, party);
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();

		// 9 beats 6: the first of two wounds (damage +1) is about to be placed on an exhausted Éowyn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		// Any 2 cards of the hand, whatever their kind.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, axe, defiance, kingdom));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(boromir, defiance);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, boromir, defiance);

		// The second wound is a window of its own, and is prevented by the last 2 cards.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, axe, kingdom);
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWoundThatExhaustsHerIsNotAnsweredAndTheNextIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(eowyn);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 1);
		scn.SetFreepsHand(boromir, axe);
		scn.MoveMinionsToTable(party);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, party);
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();

		// With 2 vitality left she is not exhausted: the first wound is placed unanswered.  It exhausts her, and the
		// second wound is answered.
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInHandSheIsKilledByTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(eowyn);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.SetFreepsHand(boromir);
		scn.MoveMinionsToTable(party);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, party);
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();

		// One card cannot pay for 2: no offer, and the first wound kills her.
		assertInZone(Zone.DEAD, eowyn);
		assertInZone(Zone.HAND, boromir);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersAWoundSheTakesInNoSkirmishDuringAnotherCompanionsSkirmishButNotThatCompanionsWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetFreepsHand(boromir, axe);
		scn.MoveMinionsToTable(skirmisher);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 9 beats 8: exhausted Aragorn's wound kills him, and Éowyn's response is not offered for it (the next
		// decision is the Skirmisher's offer, and her hand is untouched).
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		// Frodo and Éowyn are the companions left; Éowyn, assigned to no skirmish, is wounded during this one.
		scn.ShadowChooseCard(eowyn);

		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, boromir, axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DoesNotAnswerAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(eowyn);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.SetFreepsHand(boromir, axe);
		scn.MoveMinionsToTable(marksman);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.SkipToArcheryWounds();
		// The Marksman's archery wound, placed by the Free Peoples player on Éowyn.
		scn.FreepsChooseCard(eowyn);

		assertInZone(Zone.DEAD, eowyn);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
