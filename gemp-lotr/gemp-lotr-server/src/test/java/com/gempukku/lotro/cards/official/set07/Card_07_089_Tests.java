package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duty", "7_89");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man companion, vitality 3; text only on being overwhelmed
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("horseman", "7_226");   // Enraged Horseman: [rohan] Man companion, vitality 3; text only in a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DutyofTwoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Duty of Two
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Add 3 threats to heal all [gondor] companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duty");

		assertEquals("Duty of Two", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsThreeThreatsToHealEveryGondorCompanionOnceAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetFreepsCard("duty");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var horseman = scn.GetFreepsCard("horseman");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, pippin, horseman);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(duty);
		// The Merchant's vitality of 1 holds no wound; raised by hand so that he can carry one (no printed card is needed for it).
		scn.ApplyAdHocModifier(new VitalityModifier(null, merchant, null, 1));
		// Boromir exhausted (2 wounds, vitality 3), Pippin on 1; a [rohan] companion, the Ring-bearer and the
		// [gondor] ally wounded beside them.
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(horseman, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertInHand(duty);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(horseman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));

		scn.StartGame();

		// Four companions and no threat: room for three.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(duty));

		scn.FreepsPlayCard(duty);
		assertEquals(3, scn.GetThreats());
		// One heal each: Boromir's second wound stays.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(horseman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertInDiscard(duty);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUnlessAllThreeThreatsCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetFreepsCard("duty");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var horseman = scn.GetFreepsCard("horseman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, pippin, horseman);
		scn.MoveCardsToHand(duty);
		scn.AddWoundsToChar(boromir, 1);
		assertInHand(duty);
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.StartGame();

		// Four companions and two threats: two more could be added, not three.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsPlayAvailable(duty));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A fifth companion makes room for the third.
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(duty));

		scn.FreepsPlayCard(duty);
		assertEquals(5, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(duty);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorCompanionWoundedItIsStillPlayedAndTheThreatsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetFreepsCard("duty");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var horseman = scn.GetFreepsCard("horseman");
		scn.MoveCompanionsToTable(boromir, pippin, horseman);
		scn.MoveCardsToHand(duty);
		scn.AddWoundsToChar(horseman, 1);
		assertInHand(duty);
		assertEquals(1, scn.GetWoundsOn(horseman));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsPlayAvailable(duty));

		scn.FreepsPlayCard(duty);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(horseman));
		assertInDiscard(duty);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
