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
					put("card", "14_6");
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

		var card = scn.GetFreepsCard("card");

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
}
