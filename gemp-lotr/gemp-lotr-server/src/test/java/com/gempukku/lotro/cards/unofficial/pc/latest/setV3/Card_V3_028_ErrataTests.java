package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_028_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("signal", "103_28");
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("beacon4", "103_35");

					// Unbound Men with different twilight costs
					put("strider", "11_54");    // Cost 1
					put("aragorn", "1_89");     // Cost 4

					put("slayer", "3_93");      // For wounding companions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Amon Din
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	Response: If an unbound Man costing X is about to take a wound, spot X beacons to prevent that wound. Hinder a beacon.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("signal");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Amon Din", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NorthernSignalFireResponseSpotsXBeaconsAndHinders1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		// Errata: cost changed from "hinder X beacons" to "spot X beacons + hinder 1 beacon"
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var beacon4 = scn.GetFreepsCard("beacon4");
		var aragorn = scn.GetFreepsCard("aragorn"); // Cost 4
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(signal, beacon1, beacon2, beacon3, beacon4);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowUseCardAction(slayer);
		// Only 1 valid target, auto-selected

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Northern Signal-fire"));

		scn.FreepsAcceptOptionalTrigger();
		// Errata: spot 4 beacons (Aragorn's cost), then hinder just 1 beacon
		// Only 1 beacon to hinder, so auto-chosen or we pick one
		scn.FreepsChooseCard(beacon1);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Only 1 beacon should be hindered (not 4 like in original)
		assertTrue(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));
		assertFalse(scn.IsHindered(beacon3));
		assertFalse(scn.IsHindered(beacon4));
	}
}
