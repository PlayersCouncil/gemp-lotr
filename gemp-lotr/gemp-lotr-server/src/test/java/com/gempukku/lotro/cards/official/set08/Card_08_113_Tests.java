package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "8_113");
					put("shelob", "8_25");
					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sting, Bane of the Eight Legs
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Frodo or Sam.
		 * <b>Response:</b> If a fierce skirmish involving bearer is about to end, add a threat to discard a minion
		 * involved in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Bane of the Eight Legs", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void StingTriggersAfterFierceButNotRegularSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		scn.AttachCardsTo(frodo, sting);

		var shelob = scn.GetShadowCard("shelob");
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, shelob);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger(); //Ring converting burdens
		assertFalse(scn.FreepsDecisionAvailable("Sting"));

		//Fierce assignment
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, shelob);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, shelob.getZone());
		scn.FreepsDeclineOptionalTrigger(); //Ring converting burdens
		scn.FreepsAcceptOptionalTrigger(); //Sting
		assertEquals(1, scn.GetThreats());
		assertEquals(Zone.DISCARD, shelob.getZone());
	}

	@Test
	public void Sting_DOESNOT_TriggerOnOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		scn.AttachCardsTo(frodo, sting);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Fierce assignment
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, sauron);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());

	}
}
