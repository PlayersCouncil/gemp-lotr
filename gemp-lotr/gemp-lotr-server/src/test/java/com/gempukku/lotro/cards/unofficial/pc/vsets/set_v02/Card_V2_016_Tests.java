package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "102_16");
					put("veowyn", "4_270");
					put("vgamling", "5_82");
					put("vrscout", "5_90");
					put("gandalf", "1_72");

					put("grima", "5_51");
					put("twk", "1_237");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Aragorn, Last Hope of Men
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Valiant.
		* 	At the start of each turn you may spot another valiant companion to draw a card.
		* 	Skirmish: Exert Aragorn to make a skirmishing valiant companion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Last Hope of Men", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void AragornDrawsCardAtStartOfTurnIfOtherValiantCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var veowyn = scn.GetFreepsCard("veowyn");
		scn.MoveCompanionToTable(aragorn, veowyn);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
	}

	@Test
	public void AragornCannotDrawCardAtStartOfTurnIfNoOtherValiantCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}


	@Test
	public void AragornExertsToBoostValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var veowyn = scn.GetFreepsCard("veowyn");
		scn.MoveCompanionToTable(aragorn, veowyn);

		var twk = scn.GetShadowCard("twk");
		scn.MoveMinionsToTable(twk);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(veowyn, twk);
		scn.FreepsResolveSkirmish(veowyn);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(veowyn));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(veowyn));
	}

	@Test
	public void AragornCantExertToBoostNonValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(aragorn, gandalf);

		var twk = scn.GetShadowCard("twk");
		scn.MoveMinionsToTable(twk);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, twk);
		scn.FreepsResolveSkirmish(gandalf);

		// Despite all evidence to the contrary, Gandalf isn't valiant so shouldn't be boostable
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(gandalf));
	}
}
