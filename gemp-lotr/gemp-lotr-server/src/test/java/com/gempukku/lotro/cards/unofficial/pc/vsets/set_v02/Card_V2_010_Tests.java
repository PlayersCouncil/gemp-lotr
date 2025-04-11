package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_010_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("armaments", "102_10");
					put("haldir", "102_8");
					put("troop", "56_22");
					put("arwen", "1_30");
					put("bow", "1_41");
					put("sword", "4_64");

					put("smith", "3_60");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NaithArmamentsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Naith Armaments
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand/ranged weapon
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be a valiant Elf.
		* 	Each time bearer wins a skirmish, you may exert bearer to heal another valiant Elf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armaments");

		assertEquals("Naith Armaments", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void NaithArmamentsIsBorneByValiantElf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(haldir, arwen);
		scn.FreepsMoveCardToHand(armaments);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();
		scn.FreepsPlayCard(armaments);

		assertEquals(haldir, armaments.getAttachedTo()); //automatically attached to the only valiant elf
	}

	@Test
	public void NaithArmamentsCountsAsBothHandAndRangedWeaponb() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		scn.FreepsMoveCharToTable(haldir, troop);
		scn.AttachCardsTo(haldir, sword);
		scn.AttachCardsTo(troop, bow);
		scn.FreepsMoveCardToHand(armaments);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		//Haldir bears a hand weapon, and Troop bears a ranged weapon, so neither can take Armaments
		assertFalse(scn.FreepsPlayAvailable(armaments));
	}

	@Test
	public void NaithArmamentsCanExertBearerToHealAnotherValiantElfAfterWinningSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(haldir, troop, arwen);
		scn.FreepsAttachCardsTo(haldir, armaments);

		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(arwen, 1);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haldir, smith);
		scn.FreepsResolveSkirmish(haldir);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsAcceptOptionalTrigger();
		//Haldir exerted, could not heal himself, and auto-selected the only other valiant elf on the field
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(arwen));
	}
}
