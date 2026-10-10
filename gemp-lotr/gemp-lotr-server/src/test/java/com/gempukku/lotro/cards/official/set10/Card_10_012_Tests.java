package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_12");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8, vitality 4
					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					put("valor", "1_103");
					// Dwarven Armor: three cards to hold in hand and nothing else.
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlimpseofFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Glimpse of Fate
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Elves.<br>Each time you lose initiative, make a minion strength -4 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Glimpse of Fate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void LosingInitiativeMakesAMinionStrengthMinusFourUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		// Four cards to three: initiative is lost with the play of the event.  The text has no "may": the choice
		// of a minion is put at once, and one must be chosen.
		scn.FreepsPlayCard(valor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// 13 - 4
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// It lasts through the phases that follow: read in the assignment phase and in a skirmish the minion is
		// no part of.
		scn.SkipToAssignments();
		assertEquals(9, scn.GetStrength(troop));
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// And it is over at the first decision of the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
	}
}
