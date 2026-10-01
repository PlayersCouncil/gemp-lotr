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

public class Card_94_008_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: unique, vitality 4 - qualifies
		put("gandalf", "1_72"); // Gandalf, Friend of the Shirefolk: unique, vitality 4 - 2nd qualifying target
		put("gimli", "1_13"); // Gimli, Son of Gloin: unique, vitality 3 - does not qualify
		put("backstabber", "1_174"); // Goblin Backstabber: minion, needed so maneuver phase isn't auto-skipped
		put("maneuverEvent", "1_103"); // Elendil's Valor: Freeps Maneuver event, no requirements
		put("shadowManeuverEvent", "1_126"); // Hunt Them Down!: Shadow Maneuver event, no requirements
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_8"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_8
		 * Type: MetaSite
		 * Game Text: Maneuver: If the fellowship is in region 2, place your unique companion with
		 * 4 or more vitality (except the Ring-bearer) in the dead pile to skip to the regroup phase.
		 */

		assertEquals("Race Text 94_8", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void KillingAQualifyingCompanionInRegionTwoSkipsToRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer(); // vitality 4, but excluded for being the Ring-bearer
		var backstabber = scn.GetShadowCard("backstabber");
		var maneuverEvent = scn.GetFreepsCard("maneuverEvent");
		var shadowManeuverEvent = scn.GetShadowCard("shadowManeuverEvent");

		scn.MoveCompanionsToTable(aragorn, gandalf, gimli);
		scn.MoveCardsToHand(maneuverEvent, shadowManeuverEvent);
		scn.StartGame();
		scn.SkipToSite(4); // region 2
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Filter specificity: only the two vitality-4 uniques qualify; Gimli (vitality 3) and Frodo
		// (vitality 4, but the Ring-bearer) do not
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertInPlay(gandalf, gimli);

		// The rest of this maneuver phase is locked out for both sides - neither eligible
		// Maneuver event can be used once the ability has been activated
		assertFalse(scn.FreepsPlayAvailable(maneuverEvent));
		assertFalse(scn.ShadowPlayAvailable(shadowManeuverEvent));

		// Both players pass out of the (now action-less) maneuver phase; skipped straight
		// through archery/assignment/skirmish to regroup
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UnavailableOutsideRegionTwo() throws DecisionResultInvalidException, CardNotFoundException {
		// Region 1 (site 1, default start)
		var scn1 = GetFreepsScenario();
		var mod1 = scn1.GetFreepsCard("mod");
		var aragorn1 = scn1.GetFreepsCard("aragorn");
		var backstabber1 = scn1.GetShadowCard("backstabber");
		scn1.MoveCompanionsToTable(aragorn1);
		scn1.StartGame();
		scn1.MoveMinionsToTable(backstabber1);
		scn1.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn1.FreepsActionAvailable(mod1));

		// Region 3 (site 7)
		var scn2 = GetFreepsScenario();
		var mod2 = scn2.GetFreepsCard("mod");
		var aragorn2 = scn2.GetFreepsCard("aragorn");
		var backstabber2 = scn2.GetShadowCard("backstabber");
		scn2.MoveCompanionsToTable(aragorn2);
		scn2.StartGame();
		scn2.SkipToSite(7);
		scn2.MoveMinionsToTable(backstabber2);
		scn2.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn2.FreepsActionAvailable(mod2));
	}

	@Test
	public void UnavailableWithoutAQualifyingCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(gimli);
		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void OwnerGatingShadowCantUseEvenInRegionTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
