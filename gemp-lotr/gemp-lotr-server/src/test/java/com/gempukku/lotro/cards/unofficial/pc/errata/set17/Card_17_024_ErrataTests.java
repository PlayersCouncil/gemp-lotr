package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_17_024_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "67_24");
					put("glamdring", "1_75");
					put("glamdring2", "6_31");
					put("pipe", "1_74");
					put("staff", "2_29");
					put("gandalf", "1_364");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Shadowfax, Greatest of the Mearas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 1
		 * Vitality: 1
		 * Resistance: 1
		 * Game Text: Bearer must be a [gandalf] Wizard.
		 *  Discard all other possessions and weapons on bearer.
		 *  Each time the fellowship moves, you may exert bearer and add a threat to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Greatest of the Mearas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void ShadowfaxPlaysOnaGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(shadowfax);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.RemoveBurdens(1);

		assertEquals(Zone.HAND, shadowfax.getZone());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetVitality(gandalf));
		assertEquals(6, scn.GetResistance(gandalf));

		scn.FreepsPlayCard(shadowfax);
		assertEquals(Zone.ATTACHED, shadowfax.getZone());
		assertEquals(gandalf, shadowfax.getAttachedTo());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetVitality(gandalf));
		assertEquals(7, scn.GetResistance(gandalf));
	}

	@Test
	public void ShadowfaxDiscardsPossessionsAndWeaponsOnBearerBothBeforeAndAfterPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring2 = scn.GetFreepsCard("glamdring2");
		var pipe = scn.GetFreepsCard("pipe");
		var staff = scn.GetFreepsCard("staff");

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(shadowfax, staff, glamdring2);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, pipe, glamdring);

		scn.StartGame();

		assertEquals(Zone.HAND, shadowfax.getZone());
		assertEquals(Zone.ATTACHED, glamdring.getZone());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		scn.FreepsPlayCard(shadowfax);
		assertEquals(Zone.ATTACHED, shadowfax.getZone());
		assertEquals(Zone.DISCARD, glamdring.getZone());
		assertEquals(Zone.DISCARD, pipe.getZone());

		assertEquals(Zone.HAND, staff.getZone());
		scn.FreepsPlayCard(staff);
		assertEquals(Zone.DISCARD, staff.getZone());

		assertEquals(Zone.HAND, glamdring2.getZone());
		scn.FreepsPlayCard(glamdring2);
		assertEquals(Zone.DISCARD, glamdring2.getZone());
	}

	@Test
	public void ShadowfaxExertsBearerAndAddsThreatToRemoveBurdenWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(gandalf));
	}
}
