package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Card_03_014_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erestor", "53_14");
					put("arwen", "3_7");

					put("enquea", "1_231");
					put("hate", "1_250");
					put("orc", "1_271");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErestorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Erestor, Chief Advisor to Elrond
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Response:</b> If an Elf is about to take a wound from a [sauron] or [ringwraith] card, exert Erestor to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erestor");

		assertEquals("Erestor", card.getBlueprint().getTitle());
		assertEquals("Chief Advisor to Elrond", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void ErestorSpotsAnElfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(arwen, erestor);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(erestor));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(erestor));
	}

	@Test
	public void ErestorCanBlockEnquea() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(erestor);

		var enquea = scn.GetShadowCard("enquea");
		scn.MoveMinionsToTable(enquea);

		scn.StartGame();
		scn.AddBurdens(5);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(enquea));
		scn.ShadowUseCardAction(enquea);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(enquea));
	}

	@Test
	public void ErestorCanBlockEnqueaSkirmishWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(erestor);

		var enquea = scn.GetShadowCard("enquea");
		scn.MoveMinionsToTable(enquea);

		scn.StartGame();
		scn.AddBurdens(5);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, enquea);

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(enquea));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(enquea));
	}

	@Test
	public void ErestorCanBlockHate() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(erestor);

		var hate = scn.GetShadowCard("hate");
		var orc = scn.GetShadowCard("orc");

		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToHand(hate);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orc));
		scn.ShadowPlayCard(hate);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(orc));
	}

	@Test
	public void ErestorCanBlockSauronOrcSkirmishWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(erestor);

		var orc = scn.GetShadowCard("orc");
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.AddBurdens(5);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, orc);

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orc));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orc));
	}


}
