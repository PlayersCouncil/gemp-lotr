package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_008_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("haldir", "102_8");
					put("troop", "56_22");
					put("veowyn", "4_270");
					put("bow1", "1_41");
					put("bow2", "1_41");

					put("smith", "3_60");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Haldir, Naith Commander
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant. 
		* 	While Haldir bears a ranged weapon, each valiant Elf is strength +1 and each valiant Elf does not add to the fellowship archery total.
		* 	Each time an [elven] possession is about to be discarded by a card effect, you may exert Haldir to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Naith Commander", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void HaldirMakesValiantElvesStrengthPlus1AndPreventsAllFromAddingToArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var veowyn = scn.GetFreepsCard("veowyn");
		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		scn.FreepsMoveCardToHand(bow1);
		scn.FreepsMoveCharToTable(haldir, troop, veowyn);
		scn.FreepsAttachCardsTo(troop, bow2);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		assertEquals(6, scn.getStrength(veowyn));
		assertEquals(6, scn.getStrength(haldir));
		assertEquals(8, scn.getStrength(troop));

		scn.FreepsPlayCard(bow1);
		assertEquals(haldir, bow1.getAttachedTo());
		assertEquals(6, scn.getStrength(veowyn));
		assertEquals(7, scn.getStrength(haldir));
		assertEquals(9, scn.getStrength(troop));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void HaldirCanExertToPreventForcedDiscardOfBow() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var bow1 = scn.GetFreepsCard("bow1");
		scn.FreepsMoveCardToHand(bow1);
		scn.FreepsMoveCharToTable(haldir, troop);
		scn.FreepsAttachCardsTo(troop, bow1);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.ATTACHED, bow1.getZone());
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(smith)); //arrow shot
		scn.ShadowUseCardAction(smith);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(Zone.ATTACHED, bow1.getZone());
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetWoundsOn(smith));
	}

	@Test
	public void HaldirCannotExertToPreventDiscardFromDeath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var bow1 = scn.GetFreepsCard("bow1");
		scn.FreepsMoveCardToHand(bow1);
		scn.FreepsMoveCharToTable(haldir, troop);
		scn.FreepsAttachCardsTo(troop, bow1);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		scn.AddWoundsToChar(troop, 3);
		scn.PassCurrentPhaseActions(); //to get the death detected
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}


}
