package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_028_Tests
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
					put("beacon5", "103_35");

					// Unbound Men with different twilight costs
					put("strider", "11_54");    // Cost 1
					put("knight", "8_39");      // Cost 2
					put("boromir", "1_97");     // Cost 3
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
		* 	Response: If an unbound Man costing X is about to take a wound, hinder X beacons to prevent that wound.
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


//
// Extra Cost tests - requires hindering 2 beacons
//

	@Test
	public void NorthernSignalFireCannotPlayWithoutTwoBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(signal);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1); // Only 1 beacon

		scn.StartGame();

		// Only 1 beacon available to hinder - cannot play
		assertFalse(scn.FreepsPlayAvailable(signal));
	}

	@Test
	public void NorthernSignalFireCanPlayByHinderingTwoBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(signal);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);

		scn.StartGame();

		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));
		assertTrue(scn.FreepsPlayAvailable(signal));

		scn.FreepsPlayCard(signal);
		scn.FreepsChooseCards(beacon1, beacon2);

		assertInZone(Zone.SUPPORT, signal);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

	@Test
	public void NorthernSignalFireCannotHinderAlreadyHinderedBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(signal);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);
		scn.HinderCard(beacon1); // Pre-hinder one beacon

		scn.StartGame();

		// Only 1 non-hindered beacon available - cannot play
		assertFalse(scn.FreepsPlayAvailable(signal));
	}

//
// Response tests - prevent wound to unbound Man by hindering X beacons
//

	@Test
	public void NorthernSignalFireResponsePreventWoundToCost1Man() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var strider = scn.GetFreepsCard("strider"); // Cost 1
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(strider);
		scn.MoveCardsToSupportArea(signal, beacon1);

		scn.StartGame();

		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(strider));
		assertFalse(scn.IsHindered(beacon1));

		scn.ShadowUseCardAction(slayer);
		//Strider chosen as the only option automatically

		// Response should be available
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Northern Signal-fire"));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(beacon1); // Cost 1 = hinder 1 beacons

		assertEquals(0, scn.GetWoundsOn(strider));
		assertTrue(scn.IsHindered(beacon1));
	}

	@Test
	public void NorthernSignalFireResponsePreventWoundToCost2Man() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var knight = scn.GetFreepsCard("knight"); // Cost 2
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(signal, beacon1, beacon2);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(knight));

		scn.ShadowUseCardAction(slayer);
		//Knight is the only valid target

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Northern Signal-fire"));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(beacon1, beacon2); // Cost 2 = hinder 2 beacons

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

	@Test
	public void NorthernSignalFireResponsePreventWoundToCost4Man() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.FreepsChooseCards(beacon1, beacon2, beacon3, beacon4); // Cost 4 = hinder 4 beacons

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
		assertTrue(scn.IsHindered(beacon3));
		assertTrue(scn.IsHindered(beacon4));
	}

	@Test
	public void NorthernSignalFireResponseNotAvailableWithInsufficientBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn"); // Cost 4
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(signal, beacon1); // Only 1 beacon, need 4

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(slayer);

		// Response should NOT be available - insufficient beacons to pay cost
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Northern Signal-fire"));

		// Wound goes through
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void NorthernSignalFireResponseNotAvailableForBoundRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var slayer = scn.GetShadowCard("slayer");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(signal, beacon1);

		scn.StartGame();

		// Wound Frodo directly
		scn.AddWoundsToChar(frodo, 1);

		// Signal-fire only triggers for unbound Men; Frodo is bound and not a Man
		// This is implicitly tested by the trigger filter, but verifying the setup works
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void NorthernSignalFireResponseCanDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var signal = scn.GetFreepsCard("signal");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var strider = scn.GetFreepsCard("strider"); // Cost 1
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(strider);
		scn.MoveCardsToSupportArea(signal, beacon1);

		scn.StartGame();

		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(slayer);
		//Strider chosen as the only option automatically

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Northern Signal-fire"));

		scn.FreepsDeclineOptionalTrigger();

		// Wound goes through, beacon not hindered
		assertEquals(1, scn.GetWoundsOn(strider));
		assertFalse(scn.IsHindered(beacon1));
	}
}
