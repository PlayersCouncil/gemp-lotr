package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "17_24");
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
		 * Game Text: Bearer must be a [gandalf] Wizard. Discard all other possessions on bearer and bearer may not bear a hand weapon.<br><b>Fellowship:</b> Add a threat to remove a burden.
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
		scn.MoveCompanionToTable(gandalf);

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

	/**
	 * Legacy Ruling #1 decided that GOTM is to be read as having a perpetual discard effect on
	 * all possessions.  Weapons remain unable to be borne by bearer.
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_1">Legacy Ruling #1 on the wiki</a>
	 */
	@Test
	public void ShadowfaxDiscardsPossessionsOnBearerBothBeforeAndAfterPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring2 = scn.GetFreepsCard("glamdring2");
		var pipe = scn.GetFreepsCard("pipe");
		var staff = scn.GetFreepsCard("staff");

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(shadowfax, staff, glamdring2);
		scn.MoveCompanionToTable(gandalf);
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
		assertFalse(scn.FreepsPlayAvailable(glamdring2));
	}

	@Test
	public void ShadowfaxFellowshipAbilityAddsAThreatToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}
}
