package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "12_173");
					// The [wraith] cards for the hand, of two types: Nazgûl Sword, a possession (bearer must be a Nazgûl; damage +1
					// only at 3 burdens), and Not Easily Destroyed, a support-area condition (remove (3) to heal a Nazgûl)
					put("sword", "1_218");
					put("notdestroyed", "1_220");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] minion in the hand, the character among the [wraith] cards; her text is not reached from the hand
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, the card of another culture in the hand
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion of strength 13 with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Attëa, Black Predator
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Skirmish:</b> Remove (2) and discard a [wraith] card from hand to make Úlairë Attëa strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Black Predator", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesTwoAndDiscardsAWraithCardToMakeHerStrengthPlus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var sword = scn.GetShadowCard("sword");
		var notdestroyed = scn.GetShadowCard("notdestroyed");
		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(sword, notdestroyed, nelya, runner);
		assertInHand(sword, notdestroyed, nelya, runner);
		scn.MoveMinionsToTable(attea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);
		// Any [wraith] card may go, of whatever type: a possession, a condition, a minion; the [moria] Runner may not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, notdestroyed, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(sword);

		assertEquals(1, scn.GetTwilight());
		assertInDiscard(sword);
		assertInHand(notdestroyed, nelya, runner);
		assertEquals(15, scn.GetStrength(attea));

		// Still +3 at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(15, scn.GetStrength(attea));

		scn.PassSkirmishActions();
		// 15 against 8: Aragorn loses and lives.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Her fierce skirmish is a phase of its own: the bonus is gone.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedUntilBothTwoTwilightAndAWraithCardInHandCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var notdestroyed = scn.GetShadowCard("notdestroyed");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(notdestroyed);
		assertInHand(notdestroyed);
		scn.MoveMinionsToTable(attea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// A [wraith] card in hand and (1) in the pool.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(attea));

		// (2) in the pool and only a [moria] card in hand.
		scn.SetTwilight(2);
		scn.SetShadowHand(runner);
		assertInHand(runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(attea));

		// Both: offered.
		scn.SetShadowHand(runner, notdestroyed);
		assertInHand(runner, notdestroyed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(attea));
	}

	@Test
	public void SkirmishAbilityCanBeUsedWhileSheIsInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var notdestroyed = scn.GetShadowCard("notdestroyed");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SetShadowHand(notdestroyed);
		assertInHand(notdestroyed);
		scn.MoveMinionsToTable(attea, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		// The Shadow player leaves her unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(attea));

		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);

		assertEquals(0, scn.GetTwilight());
		assertInDiscard(notdestroyed);
		assertEquals(15, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
