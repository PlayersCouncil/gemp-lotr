package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noman", "103_69");
					put("witchking", "1_237");
					put("rider1", "12_161");
					put("rider2", "12_161");

					put("mount", "4_287");
					put("lance", "103_91");
					put("rider", "4_286");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoManMayHinderMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: No Man May Hinder Me
		 * Unique: false
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver/Skirmish
		 * Game Text: Maneuver or Skirmish: Restore all Nazgul. Then you may exert The Witch-king twice to hinder all Free Peoples possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("noman");

		assertEquals("No Man May Hinder Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}



// ======== PHASE AVAILABILITY TESTS ========

	@Test
	public void NoManMayHinderMePlayableInManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(noman));
	}

	@Test
	public void NoManMayHinderMePlayableInSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var runner = scn.GetShadowCard("runner");
		var rider = scn.GetFreepsCard("rider");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(runner);
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(noman));
	}

// ======== RESTORE EFFECT TESTS ========

	@Test
	public void NoManMayHinderMeRestoresAllNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(witchking, rider1, rider2, runner);
		// Hinder the Nazgul
		scn.HinderCard(witchking, rider1, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.IsHindered(witchking));
		assertTrue(scn.IsHindered(rider1));
		assertFalse(scn.IsHindered(rider2));
		assertTrue(scn.IsHindered(runner));

		scn.ShadowPlayCard(noman);

		// All Nazgul restored
		assertFalse(scn.IsHindered(witchking));
		assertFalse(scn.IsHindered(rider1));
		assertFalse(scn.IsHindered(rider2));
		assertTrue(scn.IsHindered(runner));
	}


// ======== WITCH-KING OPTIONAL EFFECT TESTS ========

	@Test
	public void NoManMayHinderMeCanExertWitchKingTwiceToHinderAllFPPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var mount = scn.GetFreepsCard("mount");
		var lance = scn.GetFreepsCard("lance");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(witchking, runner);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount, lance);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertFalse(scn.IsHindered(mount));
		assertFalse(scn.IsHindered(lance));

		scn.ShadowPlayCard(noman);

		// Accept optional exert
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert The Witch-king twice to hinder all Free Peoples possessions?"));
		scn.ShadowChooseYes();

		// WK exerted twice
		assertEquals(2, scn.GetWoundsOn(witchking));
		// All FP possessions hindered
		assertTrue(scn.IsHindered(mount));
		assertTrue(scn.IsHindered(lance));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NoManMayHinderMeOptionalEffectNotAvailableIfWitchKingHas2Wounds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(witchking, runner);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);
		scn.AddWoundsToChar(witchking, 2);  // Can only exert once more

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(noman);

		// Optional effect should NOT be available - WK can't exert twice
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exert The Witch-king twice to hinder all Free Peoples possessions?"));
		assertFalse(scn.IsHindered(mount));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NoManMayHinderMeOptionalEffectNotAvailableWithoutWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var rider1 = scn.GetShadowCard("rider1");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(rider1, runner);  // No Witch-king
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(noman);

		// Optional effect should NOT be available - no WK
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(mount));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NoManMayHinderMeOptionalEffectAvailableIfWitchKingHas1Wound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(noman);
		scn.MoveMinionsToTable(witchking, runner);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);
		scn.AddWoundsToChar(witchking, 1);  // Can still exert twice (1+2=3 wounds, vitality 4)

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(noman);

		// Optional effect should be available - WK can exert twice
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert The Witch-king twice to hinder all Free Peoples possessions?"));
		scn.ShadowChooseYes();

		assertEquals(3, scn.GetWoundsOn(witchking));
		assertTrue(scn.IsHindered(mount));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
