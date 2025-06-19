package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_240_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");

					put("band", "51_240");
					put("card1", "1_241");
					put("card2", "1_242");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Band of the Eye
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 4
		* Type: minion
		* Subtype: Orc
		* Strength: 12
		* Vitality: 3
		* Site Number: 6
		* Game Text: Each time this minion wins a skirmish, you may remove (2) to add 2 burdens.
		 * The Free Peoples player may discard 2 cards at random from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var band = scn.GetFreepsCard("band");

		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.SAURON, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.ORC, band.getBlueprint().getRace());
		assertEquals(4, band.getBlueprint().getTwilightCost());
		assertEquals(12, band.getBlueprint().getStrength());
		assertEquals(3, band.getBlueprint().getVitality());
		assertEquals(6, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void BandWinningSkirmishCanRemove2ToAdd2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void BandWinningSkirmishCanBePreventedByDiscarding2CardsInHandAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsDrawCards(3);

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
	}

	@Test
	public void BandWinningSkirmishNeeds2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsDrawCards(3);

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
