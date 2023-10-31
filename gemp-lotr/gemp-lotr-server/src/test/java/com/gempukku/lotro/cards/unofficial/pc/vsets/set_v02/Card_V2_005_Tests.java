package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_005_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("haldir", "102_5");
					put("greenleaf", "1_50");
					put("bow", "1_41");

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
		* Title: Haldir, Naith Commander
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Elf
		* Strength: 6
		* Vitality: 3
		* Game Text: Valiant. 
		* 	While Haldir bears a ranged weapon, each valiant Elf is strength +1 and does not add to the fellowship archery total.
		* 	Each time an [elven] possession is about to be discarded by a card effect, you may exert Haldir to prevent that.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");

		assertTrue(haldir.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(haldir, Keyword.VALIANT)); // test for keywords as needed
		assertEquals(2, haldir.getBlueprint().getTwilightCost());
		assertEquals(6, haldir.getBlueprint().getStrength());
		assertEquals(3, haldir.getBlueprint().getVitality());
		assertEquals(6, haldir.getBlueprint().getResistance());
		assertEquals(CardType.COMPANION, haldir.getBlueprint().getCardType());
		assertEquals(Culture.ELVEN, haldir.getBlueprint().getCulture());
		assertEquals(Race.ELF, haldir.getBlueprint().getRace());
		assertEquals(Side.FREE_PEOPLE, haldir.getBlueprint().getSide());
	}

	@Test
	public void HaldirGrantsStrengthBonusToValiantElvesWhileBearingRangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCardToHand(bow);
		scn.FreepsMoveCharToTable(haldir, greenleaf);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(greenleaf));

		scn.FreepsPlayCard(bow);
		scn.FreepsChooseCard(haldir);

		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(greenleaf));
	}

	@Test
	public void HaldirDoesNotContributeToArcheryTotalWhileBearingRangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCharToTable(haldir);
		scn.FreepsAttachCardsTo(haldir, bow);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());

	}

	@Test
	public void HaldirTriggerOnElvenPossessionDiscardExertsToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCharToTable(haldir);
		scn.FreepsAttachCardsTo(haldir, bow);

		var smith = scn.GetShadowCard("smith");
		scn.ShadowMoveCharToTable(smith);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(smith));

		assertEquals(2, scn.GetVitality(smith));
		scn.ShadowUseCardAction(smith);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Discard Elven Bow"));

		assertEquals(3, scn.GetVitality(haldir));
		//Once Archery ability is fixed, update this value
		assertEquals(1, scn.GetVitality(smith));
		assertEquals(Zone.ATTACHED, bow.getZone());
		assertEquals(haldir, bow.getAttachedTo());

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetVitality(haldir));
		assertEquals(Zone.ATTACHED, bow.getZone());
		assertEquals(haldir, bow.getAttachedTo());

	}

}