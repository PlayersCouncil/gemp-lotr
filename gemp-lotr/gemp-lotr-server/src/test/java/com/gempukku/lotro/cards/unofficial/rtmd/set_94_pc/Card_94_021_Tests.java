package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_021_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");     // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");     // second copy
					put("endurance1", "2_4");  // Endurance of Dwarves: [dwarven] condition, not unique, cost 1, borne by a Dwarf (vitality +1); a skirmish ability used only on purpose
					put("endurance2", "2_4");  // second copy
					put("ambition1", "1_133"); // Saruman's Ambition: [isengard] Shadow condition, not unique, cost 0, plays to the support area
					put("ambition2", "1_133"); // second copy
					put("gimli", "1_13");      // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Endurance borne
					put("guard1", "1_7");      // Dwarf Guard: Free Peoples companion, not unique; to play, spot a Dwarf
					put("guard2", "1_7");      // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_21", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");     // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");     // second copy
					put("endurance1", "2_4");  // Endurance of Dwarves: [dwarven] condition, not unique, cost 1, borne by a Dwarf (vitality +1); a skirmish ability used only on purpose
					put("endurance2", "2_4");  // second copy
					put("ambition1", "1_133"); // Saruman's Ambition: [isengard] Shadow condition, not unique, cost 0, plays to the support area
					put("ambition2", "1_133"); // second copy
					put("gimli", "1_13");      // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Endurance borne
					put("guard1", "1_7");      // Dwarf Guard: Free Peoples companion, not unique; to play, spot a Dwarf
					put("guard2", "1_7");      // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_21"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");     // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");     // second copy
					put("endurance1", "2_4");  // Endurance of Dwarves: [dwarven] condition, not unique, cost 1, borne by a Dwarf (vitality +1); a skirmish ability used only on purpose
					put("endurance2", "2_4");  // second copy
					put("ambition1", "1_133"); // Saruman's Ambition: [isengard] Shadow condition, not unique, cost 0, plays to the support area
					put("ambition2", "1_133"); // second copy
					put("gimli", "1_13");      // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Endurance borne
					put("guard1", "1_7");      // Dwarf Guard: Free Peoples companion, not unique; to play, spot a Dwarf
					put("guard2", "1_7");      // second copy
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
		 * Name: Race Text 94_21
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your Free Peoples conditions are unique.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_21", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OwnerCannotPlayASecondCopyOfAFreepsCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(foes2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SecondCopyOfAFreepsConditionIsPlayableWithoutTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(foes2));
		scn.FreepsPlayCard(foes2);

		assertInZone(Zone.SUPPORT, foes1);
		assertInZone(Zone.SUPPORT, foes2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OwnersNonConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(guard2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NonOwnersFreepsConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// "your" scopes the modifier to the runner; the Shadow player's copy leaves the Freeps player alone
		var scn = GetShadowScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(foes2));
		scn.FreepsPlayCard(foes2);

		assertInZone(Zone.SUPPORT, foes1);
		assertInZone(Zone.SUPPORT, foes2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OwnersShadowConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// side(freeps) in the filter: the owner's own Shadow conditions stay non-unique
		var scn = GetShadowScenario();
		var ambition1 = scn.GetShadowCard("ambition1");
		var ambition2 = scn.GetShadowCard("ambition2");

		scn.MoveCardsToSupportArea(ambition1);
		scn.MoveCardsToHand(ambition2);
		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(ambition2));
		scn.ShadowPlayCard(ambition2);

		assertInZone(Zone.SUPPORT, ambition1);
		assertInZone(Zone.SUPPORT, ambition2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceOneCopyOfABorneConditionIsPlayedTheSecondCannotBe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var endurance1 = scn.GetFreepsCard("endurance1");
		var endurance2 = scn.GetFreepsCard("endurance2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(endurance1, endurance2);
		scn.StartGame();

		// With neither in play, each copy may be played.
		assertTrue(scn.FreepsPlayAvailable(endurance1));
		assertTrue(scn.FreepsPlayAvailable(endurance2));

		scn.FreepsPlayCard(endurance1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard1));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(endurance1, gimli);

		// Dwarf Guard could bear the second copy, but its title is now in play.
		assertFalse(scn.FreepsPlayAvailable(endurance2));
		assertInZone(Zone.HAND, endurance2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnerInPlayerTwosSeatCannotPlayASecondCopyOnHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetShadowCard("gimli");
		var foes1 = scn.GetShadowCard("foes1");
		var foes2 = scn.GetShadowCard("foes2");
		var endurance1 = scn.GetShadowCard("endurance1");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2, endurance1);
		scn.StartGame();

		// Player 2's turn: he owns the modifier and is now the Free Peoples player.
		scn.SkipToOtherPlayersTurn();

		assertInZone(Zone.SUPPORT, foes1);
		assertInZone(Zone.HAND, foes2, endurance1);
		assertFalse(scn.ShadowPlayAvailable(foes2));
		// A condition with no copy in play is offered in the same decision.
		assertTrue(scn.ShadowPlayAvailable(endurance1));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
