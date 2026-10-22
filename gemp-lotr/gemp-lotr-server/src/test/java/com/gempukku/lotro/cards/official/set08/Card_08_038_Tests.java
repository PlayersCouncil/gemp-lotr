package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("king", "8_38");
					// Sleepless Dead: [gondor] Wraith companion; its win trigger is used only on purpose
					put("sleepless", "8_45");
					// Dead Man of Dunharrow: [gondor] Wraith companion; to play, add a threat; exerts himself twice as he is played
					put("deadman", "10_27");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					// Aragorn, Ranger of the North: [gondor] Aragorn, strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Aragorn, Defender of Rohan: the [rohan] Aragorn, strength 8, vitality 4; his text needs the
					// Ring-bearer assigned, who is not
					put("rohanaragorn", "17_93");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish, he may exert
					// twice to wound a companion
					put("skirmisher", "18_90");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingoftheDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: King of the Dead, Oathbreaker
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Enduring</b>. To play, spot a [gondor] Wraith and add 2 threats.<br><b>Response:</b> If Aragorn is about to take a wound in a skirmish, exert King of the Dead to prevent that wound.
		*/

		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");

		assertEquals("King of the Dead", king.getBlueprint().getTitle());
		assertEquals("Oathbreaker", king.getBlueprint().getSubtitle());
		assertTrue(king.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, king.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, king.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, king.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, king.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(king, Keyword.ENDURING));
		assertEquals(4, king.getBlueprint().getTwilightCost());
		assertEquals(7, king.getBlueprint().getStrength());
		assertEquals(4, king.getBlueprint().getVitality());
		assertEquals(6, king.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, king.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedBesideAGondorManUntilAGondorWraithIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var gknight = scn.GetFreepsCard("gknight");
		// A [gondor] Man who is no Wraith, beside Frodo: two companions, so two threats could be added.
		scn.MoveCompanionsToTable(gknight);
		scn.MoveCardsToHand(king, deadman);

		scn.StartGame();
		assertInZone(Zone.HAND, king, deadman);
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsPlayAvailable(king));

		// Dead Man of Dunharrow adds his own threat; three companions leave room for two more.
		scn.FreepsPlayCard(deadman);
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(king);
		assertInZone(Zone.FREE_CHARACTERS, king);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileOnlyOneMoreThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var sleepless = scn.GetFreepsCard("sleepless");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(sleepless);
		scn.MoveCardsToHand(king, gknight);

		scn.StartGame();
		// Frodo and Sleepless Dead: two companions, so with 1 threat only one more can be added (the King is not in
		// play while his cost is paid).
		scn.AddThreats(1);
		// The choice of fellowship actions was sent before the threat was added.
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, king, gknight);
		assertFalse(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(king);
		assertInZone(Zone.FREE_CHARACTERS, king);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToPreventTheWoundAragornTakesForLosingASkirmishAndNotAnotherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(king, aragorn, gknight);

		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gknight, brigand});

		// The Gondorian Knight, 6, loses to the Brigand, 8: a [gondor] companion who is no Aragorn, not protected.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(king));
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn, 8, loses to the Troop, 13 (not double his strength).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(king));
		scn.FreepsChooseOptionalTrigger(king);

		assertEquals(1, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void PreventsAWoundOnTheRohanAragornToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(king, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(king));
		scn.FreepsChooseOptionalTrigger(king);

		assertEquals(1, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void PreventsAWoundAragornTakesInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(king, aragorn, gknight);

		var skirmisher = scn.GetShadowCard("skirmisher");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gknight, skirmisher});

		// The Skirmisher, 9, beats the Gondorian Knight, 6, and exerts twice to wound Aragorn, whose own skirmish is
		// still to come: a wound taken during a skirmish.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(king));
		scn.FreepsChooseOptionalTrigger(king);

		assertEquals(1, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForAnArcheryWoundOnAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(king, aragorn);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Marksman's archery wound, placed by the Free Peoples player on Aragorn.
		scn.FreepsChooseCard(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(king));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileKingOfTheDeadIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(king, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(king, 3);
		assertEquals(3, scn.GetWoundsOn(king));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(king));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(king));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
