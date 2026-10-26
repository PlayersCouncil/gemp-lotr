package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_10_021_Tests
{

	// The Great Ring adds no strength and, while it is not worn, answers no wound: the Ring-bearer may fight and lose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "10_21");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("sword", "1_112");      // Ranger's Sword: on Aragorn, strength +2 and damage +1
					// Hobbit Sword: on Frodo, strength +2 and no other text, so that Gollum at 8 does not overwhelm him
					put("hobbitsword", "1_299");
					// Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that
					// minion lose all game text keywords ..." (Gollum has none); her other text is about her own wounds
					put("eowyn", "17_96");
					// Pippin, Friend to Frodo, bearing Slow-kindled Courage: each minion gains "Assignment: Assign
					// this minion to bearer of Slow-kindled Courage", which the Shadow player uses
					put("pippin", "1_306");
					put("courage", "7_328");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gollum, Mad Thing
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype:
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: To assign Gollum to a skirmish, the Free Peoples player must make Gollum strength +3 until the regroup phase or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Mad Thing", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	// Éowyn's action is the Free Peoples player assigning: he pays once Gollum is chosen for it.
	@Test
	public void AssignedByTheFreePeoplesPlayersCardGollumIsMadeStrengthPlusThreeUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(frodo, hobbitsword);

		var gollum = scn.GetShadowCard("gollum");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, brigand));
		scn.FreepsChooseCard(gollum);

		// The price: one of the two, his choice.
		assertEquals(List.of("Make Gollum strength +3", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Make Gollum strength +3");

		assertTrue(scn.IsCharAssignedAgainst(frodo, gollum));
		assertEquals(8, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.PassAssignmentActions();

		// The Brigand beside him costs nothing to assign.
		scn.FreepsAssignToMinions(aragorn, brigand);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertEquals(0, scn.GetBurdens());
		assertEquals(8, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(brigand));

		// Aragorn, 8 + 2, beats the Brigand first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// In the next skirmish, his own, Gollum is still 8; he beats Frodo, 3 + 2, and takes no wound.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The regroup phase: the bonus is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersCardGollumMayBePaidForWithABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn);

		var gollum = scn.GetShadowCard("gollum");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(eowyn);
		scn.FreepsChooseCard(gollum);
		assertEquals(List.of("Make Gollum strength +3", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		assertTrue(scn.IsCharAssignedAgainst(frodo, gollum));
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	// The CR 4.2 ruling on this card: "Gollum's game text does not apply if a Shadow player assigns him to a
	// skirmish."
	@Test
	public void AssignedByTheShadowPlayerGollumCostsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, courage);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		// Nothing is asked of the Free Peoples player, and nothing is paid.
		assertTrue(scn.IsCharAssignedAgainst(pippin, gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	// At his own assigning the price is paid ahead, offered on Gollum as he starts (Q-0221, Q-0283).
	@Test
	public void AsHeStartsAssigningHeIsOfferedToPayForGollumAndPaidForGollumIsHisToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		var gollum = scn.GetShadowCard("gollum");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, brigand);
		scn.SkipToAssignments();

		// Gollum is offered to be paid for; the Brigand is not.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gollum));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brigand));
		scn.FreepsChooseOptionalTrigger(gollum);
		assertEquals(List.of("Make Gollum strength +3", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gollum));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{frodo, brigand});

		// One burden was the whole price: the Brigand, assigned in the same answer, cost nothing.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, gollum));
		assertTrue(scn.IsCharAssignedAgainst(frodo, brigand));
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void PassingOnGollumsPriceLeavesHimOffHisListAndTheShadowPlayerAssignsHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		var gollum = scn.GetShadowCard("gollum");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, brigand);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gollum));
		scn.FreepsDeclineOptionalTrigger();

		// Nothing paid, and Gollum is not his to assign.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gollum));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		scn.FreepsAssignToMinions(aragorn, brigand);

		// The Shadow player assigns him, left over, for nothing.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(gollum));
		scn.ShadowAssignToMinions(frodo, gollum);
		assertTrue(scn.IsCharAssignedAgainst(frodo, gollum));
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
