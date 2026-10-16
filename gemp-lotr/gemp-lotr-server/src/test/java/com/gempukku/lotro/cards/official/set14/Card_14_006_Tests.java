package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimbeorn", "14_6");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("runner", "1_178");         // Goblin Runner: [moria] Orc of strength 5, vitality 1
					put("lemenya", "1_232");        // Úlairë Lemenya: [wraith] Nazgûl of strength 9
					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("guard", "1_7");            // Dwarf Guard: a companion, and no minion
					put("beckoning", "12_83");      // The Beckoning Shadow: a Shadow condition, and no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStrengthScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimbeorn", "14_6");
					// The Witch-king, Lord of Angmar: a minion with 14 printed on him, and "For each other Nazgûl
					// you can spot, The Witch-king is strength +2": in play beside Lemenya he would be 16
					put("witchking", "1_237");
					put("runner", "1_178");         // Goblin Runner: a minion of strength 5
					put("scimitar", "1_180");       // Goblin Scimitar: a Shadow possession, and no minion
					// Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl of strength 9 and vitality 2, not fierce; her
					// text needs a [wraith] condition on a companion
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimbeornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Grimbeorn, Beorning Chieftain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 5
		 * Resistance: 6
		 * Game Text: At the start of each turn, heal Grimbeorn.<br>At the start of the maneuver phase, you may exert Grimbeorn twice and discard a minion from hand to make Grimbeorn strength +X until the regroup phase, where X is that minion's strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimbeorn");

		assertEquals("Grimbeorn", card.getBlueprint().getTitle());
		assertEquals("Beorning Chieftain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfManeuverHeExertsTwiceAndDiscardsAMinionFromHandToGainThatMinionsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStrengthScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var witchking = scn.GetFreepsCard("witchking");
		var runner = scn.GetFreepsCard("runner");
		var scimitar = scn.GetFreepsCard("scimitar");
		scn.MoveCompanionsToTable(grimbeorn);
		scn.MoveCardsToHand(witchking, runner, scimitar);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertEquals(4, scn.GetStrength(grimbeorn));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));

		scn.FreepsAcceptOptionalTrigger();
		// The two minions are the choice, and the Shadow possession is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(witchking, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(witchking);

		assertEquals(2, scn.GetWoundsOn(grimbeorn));
		assertInDiscard(witchking);
		assertInHand(runner, scimitar);
		// 4 + 14, the number printed on The Witch-king: 5 for the Runner would be 9, and a Witch-king
		// counted as he would stand beside Lemenya, 20.
		assertEquals(18, scn.GetStrength(grimbeorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Until the regroup phase: still so in his skirmish, and gone at the first regroup decision.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(grimbeorn, lemenya);
		assertEquals(18, scn.GetStrength(grimbeorn));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(grimbeorn));
	}

	@Test
	public void HealsGrimbeornAtTheStartOfEachOfHisOwnersTurnsAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(grimbeorn, aragorn);
		scn.AddWoundsToChar(grimbeorn, 3);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(3, scn.GetWoundsOn(grimbeorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Player 2's turn: Grimbeorn is not active on it.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(3, scn.GetWoundsOn(grimbeorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(grimbeorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetWoundsOn(grimbeorn));

		// His next turn begins at site 3, a sanctuary: Grimbeorn's heal is taken first, and the sanctuary's declined.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Required trigger from");
		scn.FreepsDeclineSanctuaryHealing();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(grimbeorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAMinionOfStrength5DiscardedHeIsStrength9ThroughTheLaterSkirmishAndBackTo4AtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var lemenya = scn.GetFreepsCard("lemenya");
		var guard = scn.GetFreepsCard("guard");
		var beckoning = scn.GetFreepsCard("beckoning");

		var troop = scn.GetShadowCard("troop");
		var theirRunner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCompanionsToTable(grimbeorn, aragorn);
		scn.MoveCardsToHand(runner, lemenya, guard, beckoning);
		scn.MoveMinionsToTable(troop, theirRunner);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertEquals(4, scn.GetStrength(grimbeorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInHand(runner, lemenya, guard, beckoning);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));
		scn.FreepsAcceptOptionalTrigger();

		// A minion of each of two cultures is offered; a companion and a Shadow condition are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, beckoning));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertEquals(2, scn.GetWoundsOn(grimbeorn));
		assertInDiscard(runner);
		assertInHand(lemenya, guard, beckoning);
		// 4 + 5: the Runner's strength. Lemenya would have made it 13.
		assertEquals(9, scn.GetStrength(grimbeorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{grimbeorn, theirRunner});

		// Aragorn's skirmish first: the Troop (13) beats him (8) and does not overwhelm him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Still in force in the skirmish that follows.
		scn.FreepsResolveSkirmish(grimbeorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(grimbeorn));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(grimbeorn));
	}

	@Test
	public void DecliningTheOfferChangesNothingAndTheStartOfTheArcheryPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCompanionsToTable(grimbeorn);
		scn.MoveCardsToHand(runner);
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertEquals(4, scn.GetStrength(grimbeorn));
		assertInHand(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertInHand(runner);
		assertEquals(4, scn.GetStrength(grimbeorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));
		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertInHand(runner);
	}

	@Test
	public void NotOfferedWhenGrimbeornCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCompanionsToTable(grimbeorn);
		scn.MoveCardsToHand(runner);
		scn.MoveMinionsToTable(troop);
		// Vitality 5 with 3 wounds: he can exert once, and not twice.
		scn.AddWoundsToChar(grimbeorn, 3);
		assertEquals(3, scn.GetWoundsOn(grimbeorn));
		assertInHand(runner);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));
		assertEquals(3, scn.GetWoundsOn(grimbeorn));
		assertInHand(runner);
	}

	@Test
	public void NotOfferedWithNoMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var guard = scn.GetFreepsCard("guard");
		var beckoning = scn.GetFreepsCard("beckoning");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCompanionsToTable(grimbeorn);
		scn.MoveCardsToHand(guard, beckoning);
		scn.MoveMinionsToTable(troop);
		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertInHand(guard, beckoning);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimbeorn));
		assertEquals(0, scn.GetWoundsOn(grimbeorn));
		assertInHand(guard, beckoning);
	}
}
