package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hour", "102_50");
					put("eowyn", "4_270");

					put("runner1", "1_178");
					put("runner2", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetThisBetheHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Let This Be the Hour
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Exert a valiant companion to make that companion defender +1. That companion is strength +1 for each minion skirmishing them until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hour");

		assertEquals("Let This Be the Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void LetThisBetheHourAppliesDefenderAndMultiOpponentStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetFreepsCard("hour");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(hour);
		scn.MoveCompanionsToTable(eowyn);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsPlayAvailable(hour));
		assertEquals(3, scn.GetVitality(eowyn));
		assertEquals(6, scn.GetStrength(eowyn));
		assertFalse(scn.HasKeyword(eowyn, Keyword.DEFENDER));

		scn.FreepsPlayCard(hour);

		assertEquals(2, scn.GetVitality(eowyn));
		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.HasKeyword(eowyn, Keyword.DEFENDER));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsAssignToMinions(eowyn, runner1, runner2);

		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(8, scn.GetStrength(eowyn));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		assertEquals(6, scn.GetStrength(eowyn));
		assertFalse(scn.HasKeyword(eowyn, Keyword.DEFENDER));
	}
}
