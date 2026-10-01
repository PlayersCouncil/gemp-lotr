package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_001_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("celeborn", "1_34"); // Celeborn, Lord of Lorien: Elven ally
		put("backstabber", "1_174"); // Goblin Backstabber: minion
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_1", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_1"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_1
		 * Type: MetaSite
		 * Game Text: Maneuver: Exert your ally to allow them to participate in archery fire and
		 * skirmishes. At the start of the regroup phase, kill that ally.
		 */

		assertEquals("Race Text 94_1", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ExertAllyEnablesParticipationThenKillsItAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// Pre-check: ally can't normally join skirmishes/archery, no cost paid yet
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.CanBeAssignedViaAction(celeborn));

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		// Celeborn is the only ally in play, so the choose(your,ally) cost auto-resolves to him.

		// Cost paid (exert = 1 wound) and effect applied (can now be assigned)
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.CanBeAssignedViaAction(celeborn));

		// Control alternates to Shadow (who has nothing to do) and back to Freeps
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// At the start of regroup, the ally is killed
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.DEAD, celeborn);
		assertEquals(1, scn.GetFreepsDeadCount());
	}

	@Test
	public void WithoutUsingTheActionAllyIsNeverAssignableAndSurvives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.CanBeAssignedViaAction(celeborn));
		assertInPlay(celeborn);
		assertEquals(0, scn.GetFreepsDeadCount());
	}

	@Test
	public void OwnerGatingFreepsCantUseWhenShadowOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_1's ability says "your ally" - when Shadow owns the mod, the ability must be absent
		// for the non-owner (Freeps), even though Freeps has an ally that would otherwise qualify.
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(mod));
	}
}
