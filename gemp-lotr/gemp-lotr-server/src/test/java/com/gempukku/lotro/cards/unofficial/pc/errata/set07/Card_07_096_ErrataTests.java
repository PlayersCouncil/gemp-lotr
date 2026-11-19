package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_096_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "57_96");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a third companion, so that 3 threats fit; a maneuver ability used only on purpose

					// City Wall: [gondor] fortification condition for the support area; bearer strength -3; its skirmish
					// transfer is used only on purpose
					put("wall1", "5_33");
					put("wall2", "5_33");
					put("wall3", "5_33");
					// Stronghold of My Fathers: [rohan] fortification possession for the support area; a response when a
					// [rohan] companion is about to take a wound
					put("stronghold", "102_48");
					put("walls", "7_111");          // Man the Walls: [gondor] support condition that is no fortification

					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondorian Captain
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Knight.
		 * Maneuver: Discard a fortification to remove a threat.
		 */

		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");

		assertEquals("Gondorian Captain", captain.getBlueprint().getTitle());
		assertNull(captain.getBlueprint().getSubtitle());
		assertFalse(captain.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, captain.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, captain.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, captain.getBlueprint().getCardType());
		assertEquals(Race.MAN, captain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(captain, Keyword.KNIGHT));
		assertEquals(2, captain.getBlueprint().getTwilightCost());
		assertEquals(6, captain.getBlueprint().getStrength());
		assertEquals(3, captain.getBlueprint().getVitality());
		assertEquals(6, captain.getBlueprint().getResistance());
		assertNull(captain.getBlueprint().getSignet());
	}

	@Test
	public void ManeuverDiscardsAFortificationToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var wall1 = scn.GetFreepsCard("wall1");
		var wall2 = scn.GetFreepsCard("wall2");
		var wall3 = scn.GetFreepsCard("wall3");
		var stronghold = scn.GetFreepsCard("stronghold");
		var walls = scn.GetFreepsCard("walls");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(captain, scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToSupportArea(wall1, stronghold, walls);
		scn.MoveCardsToHand(wall3);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		// A fortification of his own borne by a minion.
		scn.AttachCardsTo(troop, wall2);
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(captain));

		scn.FreepsUseCardAction(captain);
		// Fortifications of either culture and type, in the support area or on a minion; not a [gondor] condition
		// that is no fortification, and not a fortification in hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall1, wall2, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walls, wall3));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(wall2);
		assertInDiscard(wall2);
		assertInZone(Zone.SUPPORT, wall1, stronghold, walls);
		assertInHand(wall3);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAFortificationInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var wall1 = scn.GetFreepsCard("wall1");
		scn.MoveCompanionsToTable(captain);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("walls"));
		scn.MoveCardsToHand(wall1);

		scn.StartGame();

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(captain));

		scn.MoveCardsToSupportArea(wall1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(captain));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OfferedInTheManeuverPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var wall1 = scn.GetFreepsCard("wall1");
		scn.MoveCompanionsToTable(captain);
		scn.MoveCardsToSupportArea(wall1);

		scn.StartGame();

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		// The original's phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(captain));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(captain));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(captain));
		assertInZone(Zone.SUPPORT, wall1);
		assertEquals(1, scn.GetThreats());
	}
}
