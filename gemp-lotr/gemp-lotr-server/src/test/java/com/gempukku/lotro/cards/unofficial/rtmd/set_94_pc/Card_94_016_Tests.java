package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_016_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "1_186"); // Guard Commander: unique [moria] Orc minion, twilight 3; a skirmish ability used only on purpose
					put("commander2", "1_186"); // Guard Commander: second copy, to test uniqueness override
					put("commander3", "1_186"); // Guard Commander: third copy
					put("commander4", "1_186"); // Guard Commander: fourth copy
					put("lemenya1", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: unique [wraith] Nazgul minion, twilight 4; his text needs a [wraith] condition on a companion
					put("lemenya2", "1_232");   // second copy
					put("refuge1", "4_29");     // No Refuge: unique [dunland] condition, twilight 0, plays to the support area with nothing needed; its text needs a [dunland] Man stacked on a site
					put("refuge2", "4_29");     // second copy: a unique Shadow card that is no minion
					put("aragorn1", "1_89");    // Aragorn, Ranger of the North: unique FP companion
					put("aragorn2", "1_89");    // second copy, proves the filter is minion-only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_16"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "1_186"); // Guard Commander: unique [moria] Orc minion, twilight 3; a skirmish ability used only on purpose
					put("commander2", "1_186"); // Guard Commander: second copy, to test uniqueness override
					put("lemenya1", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: unique [wraith] Nazgul minion, twilight 4; his text needs a [wraith] condition on a companion
					put("lemenya2", "1_232");   // second copy
					put("refuge1", "4_29");     // No Refuge: unique [dunland] condition, twilight 0, plays to the support area with nothing needed; its text needs a [dunland] Man stacked on a site
					put("refuge2", "4_29");     // second copy: a unique Shadow card that is no minion
					put("aragorn1", "1_89");    // Aragorn, Ranger of the North: unique FP companion
					put("aragorn2", "1_89");    // second copy, proves the filter is minion-only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_16", null
		);
	}

	// No meta-site at all - the true "without the card" control, since the filter is global
	// (no "your"), so even a Freeps-owned copy would still loosen every minion's uniqueness.
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "1_186"); // Guard Commander: unique [moria] Orc minion, twilight 3; a skirmish ability used only on purpose
					put("commander2", "1_186"); // Guard Commander: second copy, to test uniqueness override
					put("lemenya1", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: unique [wraith] Nazgul minion, twilight 4; his text needs a [wraith] condition on a companion
					put("lemenya2", "1_232");   // second copy
					put("refuge1", "4_29");     // No Refuge: unique [dunland] condition, twilight 0, plays to the support area with nothing needed; its text needs a [dunland] Man stacked on a site
					put("refuge2", "4_29");     // second copy: a unique Shadow card that is no minion
					put("aragorn1", "1_89");    // Aragorn, Ranger of the North: unique FP companion
					put("aragorn2", "1_89");    // second copy, proves the filter is minion-only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_16
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: Minions are not unique.
		 */

		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		assertEquals("Race Text 94_16", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void CanPlayDuplicateMinionWhenModifierActive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var commander3 = scn.GetShadowCard("commander3");
		var commander4 = scn.GetShadowCard("commander4");

		// Three copies of one title are in play, and a fourth is played beside them:
		// "not unique" is no cap of two or three.
		scn.MoveMinionsToTable(commander1, commander2, commander3);
		scn.MoveCardsToHand(commander4);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(commander4));
		scn.ShadowPlayCard(commander4);

		assertInZone(Zone.SHADOW_CHARACTERS, commander1);
		assertInZone(Zone.SHADOW_CHARACTERS, commander2);
		assertInZone(Zone.SHADOW_CHARACTERS, commander3);
		assertInZone(Zone.SHADOW_CHARACTERS, commander4);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotPlayDuplicateMinionWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.MoveMinionsToTable(commander1);
		scn.MoveCardsToHand(commander2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(commander2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UniqueCompanionsAreStillBlockedFilterIsMinionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn1 = scn.GetFreepsCard("aragorn1");
		var aragorn2 = scn.GetFreepsCard("aragorn2");

		scn.MoveCompanionsToTable(aragorn1);
		scn.MoveCardsToHand(aragorn2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreepsOwnedCopyStillLoosensUniquenessForShadowsMinionsSinceThereIsNoYour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.MoveMinionsToTable(commander1);
		scn.MoveCardsToHand(commander2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// No "your" in the text - a Freeps-owned copy still loosens uniqueness for the Shadow
		// player's minions.
		assertTrue(scn.ShadowPlayAvailable(commander2));
		scn.ShadowPlayCard(commander2);

		assertInZone(Zone.SHADOW_CHARACTERS, commander1);
		assertInZone(Zone.SHADOW_CHARACTERS, commander2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondNazgulOfOneTitleMayBePlayedAndASecondCopyOfAUniqueShadowConditionMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var lemenya1 = scn.GetShadowCard("lemenya1");
		var lemenya2 = scn.GetShadowCard("lemenya2");
		var refuge1 = scn.GetShadowCard("refuge1");
		var refuge2 = scn.GetShadowCard("refuge2");

		scn.MoveCardsToSupportArea(refuge1);
		scn.MoveCardsToHand(lemenya2, refuge2);
		scn.StartGame();
		scn.MoveMinionsToTable(lemenya1);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// A minion of another culture and race than the Guard Commander is no more unique than he is;
		// No Refuge costs nothing and needs nothing, and is refused for the copy in the support area.
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya1);
		assertInZone(Zone.SUPPORT, refuge1);
		assertTrue(scn.ShadowPlayAvailable(lemenya2));
		assertFalse(scn.ShadowPlayAvailable(refuge2));

		scn.ShadowPlayCard(lemenya2);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya1);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya2);
		assertFalse(scn.ShadowPlayAvailable(refuge2));
		assertInZone(Zone.HAND, refuge2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
