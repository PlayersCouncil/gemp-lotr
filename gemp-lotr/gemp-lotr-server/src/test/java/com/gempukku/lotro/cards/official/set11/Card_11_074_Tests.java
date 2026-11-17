package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("detachment", "11_74");
					put("detachment2", "11_74");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, resistance 5; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, resistance 6
					put("barliman", "1_70");    // Barliman Butterbur: a Man ally, resistance 0 as every ally has
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DetachmentofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Detachment of Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Exert this minion to add (1) for each companion who has resistance 5 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("detachment");

		assertEquals("Detachment of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsThisCopyToAddOneForTheCompanionAtResistanceFiveAndNotForOneAtSixOrAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var detachment2 = scn.GetShadowCard("detachment2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment, detachment2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(detachment));
		assertEquals(0, scn.GetWoundsOn(detachment2));
		assertTrue(scn.ShadowActionAvailable(detachment));
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(detachment);
		assertEquals(1, scn.GetWoundsOn(detachment));
		// The other copy did not pay.
		assertEquals(0, scn.GetWoundsOn(detachment2));
		// Sam alone: Aragorn is one over, and Barliman, of resistance 0, is no companion.
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ResistanceIsReadAsBurdensLeaveItSoEachCompanionTheyBringToFiveOrLessCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(detachment);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// One burden: Frodo 10 - 1, Sam 5 - 1, Aragorn and Gimli 6 - 1.
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(0, scn.GetWoundsOn(detachment));
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(detachment);
		assertEquals(1, scn.GetWoundsOn(detachment));
		// Sam, Aragorn and Gimli.
		assertEquals(twilight + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCompanionAtResistanceFiveOrLessItIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(detachment));
		assertTrue(scn.ShadowActionAvailable(detachment));
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(detachment);
		assertEquals(1, scn.GetWoundsOn(detachment));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExhaustedItCannotUseTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(detachment, 2);
		assertEquals(2, scn.GetWoundsOn(detachment));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(detachment));
	}
}
