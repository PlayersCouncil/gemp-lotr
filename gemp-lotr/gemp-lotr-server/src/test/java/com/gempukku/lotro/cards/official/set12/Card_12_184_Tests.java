package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_12_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beast", "12_184");
					put("twk", "1_237");

					put("fodder1", "1_302");
					put("fodder2", "1_307");
					put("fodder3", "1_310");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingsBeastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Witch-king's Beast, Fell Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Game Text: Bearer must be a Nazgûl.
		 * If bearer is The Witch-king, after all skirmishes and fierce skirmishes have been resolved,
		 * you may exert him twice to make him participate in one additional assignment and skirmish phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beast");

		assertEquals("The Witch-king's Beast", card.getBlueprint().getTitle());
		assertEquals("Fell Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void TheWitchkingsBeastTriggersAThirdRoundOfSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);

		var frodo = scn.GetRingBearer();
		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");
		var fodder3 = scn.GetFreepsCard("fodder3");
		scn.MoveCompanionsToTable(fodder1, fodder2, fodder3);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		//First Regular Skirmish
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(fodder1, twk);
		scn.FreepsResolveSkirmish(fodder1);
		scn.PassCurrentPhaseActions();

		//Fierce Skirmish
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(fodder2, twk);
		scn.FreepsResolveSkirmish(fodder2);
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(twk));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(fodder3, twk);
		scn.FreepsResolveSkirmish(fodder3);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
