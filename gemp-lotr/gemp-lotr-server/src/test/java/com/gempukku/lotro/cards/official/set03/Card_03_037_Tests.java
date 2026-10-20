package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cries", "3_37");
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] tale, a condition on a [gondor] companion; its skirmish ability used only on purpose
					put("resisted", "3_45");    // Some Who Resisted: a [gondor] tale, a Maneuver event
					put("again", "1_317");      // There and Back Again: a [shire] tale, a tale of another culture
					put("armor", "1_92");       // Armor: a [gondor] possession that is no tale
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7; no ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, strength 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnsweringtheCriesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Answering the Cries
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Skirmish:</b> Discard a [gondor] tale from hand to make a [gondor] companion strength +2.
		*/

		var scn = GetScenario();

		var cries = scn.GetFreepsCard("cries");

		assertEquals("Answering the Cries", cries.getBlueprint().getTitle());
		assertNull(cries.getBlueprint().getSubtitle());
		assertFalse(cries.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, cries.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, cries.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, cries.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(cries, Keyword.SUPPORT_AREA));
		assertEquals(0, cries.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityDiscardsAGondorTaleToMakeAGondorCompanionStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cries = scn.GetFreepsCard("cries");
		var saga = scn.GetFreepsCard("saga");
		var resisted = scn.GetFreepsCard("resisted");
		var again = scn.GetFreepsCard("again");
		var armor = scn.GetFreepsCard("armor");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(cries);
		scn.MoveCardsToHand(saga, resisted, again, armor);
		assertInHand(saga, resisted, again, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, runner1 }, new PhysicalCardImpl[] { aragorn, runner2 });
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(cries));

		scn.FreepsUseCardAction(cries);
		// The tale to discard: a [gondor] condition and a [gondor] event; not the [shire] tale, nor the [gondor]
		// possession that is no tale.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(saga, resisted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(again, armor));
		scn.FreepsChooseCard(saga);
		assertInDiscard(saga);
		assertInHand(resisted, again, armor);
		assertEquals(3, scn.GetFreepsHandCount());

		// The companion: Boromir in this skirmish, Aragorn assigned to the next one and Pippin, a [gondor] Hobbit
		// assigned to none; not the [gondor] ally, nor Frodo.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		scn.FreepsChooseCard(boromir);
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// No duration is printed, so the bonus lasts this skirmish phase: in the next skirmish it is gone.
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
	}

	@Test
	public void IsNotOfferedWithNoGondorTaleInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cries = scn.GetFreepsCard("cries");
		var saga = scn.GetFreepsCard("saga");
		var resisted = scn.GetFreepsCard("resisted");
		var again = scn.GetFreepsCard("again");
		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(cries);
		// A [gondor] tale in play and one in the discard pile; in hand, only a [shire] tale and a [gondor] card that is
		// no tale.
		scn.AttachCardsTo(boromir, saga);
		scn.MoveCardsToDiscard(resisted);
		scn.MoveCardsToHand(again, armor);
		assertAttachedTo(saga, boromir);
		assertInDiscard(resisted);
		assertInHand(again, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, runner1);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		// The Saga's own skirmish ability is on offer; the Cries is not.
		assertTrue(scn.FreepsActionAvailable(saga));
		assertFalse(scn.FreepsActionAvailable(cries));
	}

	@Test
	public void IsOfferedAndTheTaleDiscardedWithNoGondorCompanionToMakeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cries = scn.GetFreepsCard("cries");
		var saga = scn.GetFreepsCard("saga");
		var frodo = scn.GetRingBearer();
		var merchant = scn.GetFreepsCard("merchant");
		var runner1 = scn.GetShadowCard("runner1");
		// No [gondor] companion in play: Frodo is a [shire] companion and the Merchant a [gondor] ally.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(cries);
		scn.MoveCardsToHand(saga);
		assertInZone(Zone.SUPPORT, merchant);
		assertInHand(saga);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner1);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsActionAvailable(cries));

		// The cost is paid for nothing: the only tale is discarded and nobody is made stronger.
		scn.FreepsUseCardAction(cries);
		assertInDiscard(saga);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
