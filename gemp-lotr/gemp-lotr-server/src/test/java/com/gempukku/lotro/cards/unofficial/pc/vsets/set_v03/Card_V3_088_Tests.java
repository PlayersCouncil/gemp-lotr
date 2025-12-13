package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_088_Tests
{

// ----------------------------------------
// SNOWMANE, FAITHFUL SERVANT TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snowmane", "103_88");    // Snowmane, Faithful Servant
					put("theoden", "4_365");      // Theoden, Lord of the Mark
					put("eowyn", "5_122");        // Eowyn - Rohan companion
					put("horse", "4_283");        // Horse of Rohan - mount

					put("witchking", "103_80");   // Witch-king, Empowered - 15 strength
					put("orc", "1_271");          // Orc Soldier - weak minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SnowmaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Snowmane, Faithful Servant
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Vitality: 1
		 * Game Text: Bearer must be Theoden.
		* 	[Rohan] companions are strength +1 (or +2 if mounted) for each wound on Theoden.
		* 	At the start of the assignment phase, the Shadow player may exhaust a minion of strength 15 or more to hinder Snowmane.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("snowmane");

		assertEquals("Snowmane", card.getBlueprint().getTitle());
		assertEquals("Faithful Servant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}



	@Test
	public void SnowmaneGrantsStrengthPerWoundOnTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCompanionsToTable(theoden, eowyn);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();

		int eowynnBaseStrength = scn.GetStrength(eowyn);

		// No wounds on Theoden - no bonus yet
		assertEquals(0, scn.GetWoundsOn(theoden));

		// Add 1 wound to Theoden
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(eowynnBaseStrength + 1, scn.GetStrength(eowyn));

		// Add another wound
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(eowynnBaseStrength + 2, scn.GetStrength(eowyn));
	}

	@Test
	public void SnowmaneGrantsDoubleStrengthToMountedRohanPerWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var horse = scn.GetFreepsCard("horse");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCompanionsToTable(theoden, eowyn);
		scn.AttachCardsTo(theoden, snowmane);
		scn.AttachCardsTo(eowyn, horse);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();

		int eowynnBaseStrength = scn.GetStrength(eowyn); // Includes horse bonus

		// Add 1 wound to Theoden - mounted Eowyn gets +2
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(eowynnBaseStrength + 2, scn.GetStrength(eowyn));

		// Add another wound - +4 total
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(eowynnBaseStrength + 4, scn.GetStrength(eowyn));
	}

	@Test
	public void SnowmaneShadowCanExhaustStrong15MinionToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.IsHindered(snowmane));

		// Shadow should be offered the optional trigger
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exhaust"));
		scn.ShadowChooseYes();
		// Witch-king auto-selected as only valid minion

		// Witch-king exhausted (wounds = vitality - 1)
		assertTrue(scn.IsExhausted(witchking));
		assertTrue(scn.IsHindered(snowmane));
	}

	@Test
	public void SnowmaneShadowCannotHinderWithoutStrength15Minion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var orc = scn.GetShadowCard("orc"); // Weak minion, not 15 strength
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// No 15+ strength minion - trigger shouldn't be available
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exhaust"));
		assertFalse(scn.IsHindered(snowmane));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SnowmaneHinderKillsExhaustedTheoden_MastersBane() throws DecisionResultInvalidException, CardNotFoundException {
		// "Faithful servant, yet master's bane..."
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();

		// Snowmane grants +1 vitality
		// Exhaust Theoden (wounds = vitality - 1)
		int theodenVitality = scn.GetVitality(theoden);
		scn.AddWoundsToChar(theoden, theodenVitality - 1);
		assertTrue(scn.IsExhausted(theoden));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Shadow hinders Snowmane
		scn.ShadowChooseYes();

		// Snowmane hindered - loses its game text including +1 vitality, but since this kills Theoden it gets discarded
		assertInZone(Zone.DISCARD, snowmane);

		// Theoden now has wounds = (original vitality), but vitality dropped by 1
		// wounds >= vitality = death
		assertInZone(Zone.DEAD, theoden);
	}

	@Test
	public void SnowmaneHinderDoesNotKillHealthyTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();

		// Only 1 wound on Theoden - not exhausted
		scn.AddWoundsToChar(theoden, 1);
		assertFalse(scn.IsExhausted(theoden));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(snowmane));
		// Theoden survives - losing 1 vitality with only 1 wound is fine
		assertNotInZone(Zone.DEAD, theoden);
	}
}
