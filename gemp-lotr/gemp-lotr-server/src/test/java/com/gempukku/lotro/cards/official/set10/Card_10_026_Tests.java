package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cursed", "10_26");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("oathbreaker", "8_41"); // Oathbreaker: a [gondor] Wraith companion; to play, add a threat
					put("sleepless", "8_45");   // Sleepless Dead: a [gondor] Wraith companion of another title
					put("corpse", "6_99");      // Corpse Lights: a [sauron] Wraith minion, a Wraith who is not [gondor]
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CursedofErechStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cursed of Erech
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Enduring</b>. To play, add a threat.<br>When you play Cursed of Erech, exert him twice.<br>At the start of the maneuver phase, you may exert another [gondor] Wraith to make Cursed of Erech <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");

		assertEquals("Cursed of Erech", cursed.getBlueprint().getTitle());
		assertNull(cursed.getBlueprint().getSubtitle());
		assertTrue(cursed.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, cursed.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, cursed.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, cursed.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, cursed.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(cursed, Keyword.ENDURING));
		assertEquals(3, cursed.getBlueprint().getTwilightCost());
		assertEquals(7, cursed.getBlueprint().getStrength());
		assertEquals(3, cursed.getBlueprint().getVitality());
		assertEquals(6, cursed.getBlueprint().getResistance());
	}

	@Test
	public void IsPlayedByAddingAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(cursed);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsPlayAvailable(cursed));
		scn.FreepsPlayCard(cursed);

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, cursed);
		// "When you play Cursed of Erech, exert him twice": on him, and on no other companion.
		assertEquals(2, scn.GetWoundsOn(cursed));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAThreatCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		scn.MoveCardsToHand(cursed);

		scn.StartGame();

		// The Ring-bearer is the only companion in play, so one threat is as many as there may be: the board
		// nearest to one on which the threat can be added.  He is not counted as a companion before he is played.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, cursed);
		assertFalse(scn.FreepsPlayAvailable(cursed));

		// With the threat gone he is offered again.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(cursed));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherGondorWraithPlayedAfterHimDoesNotExertHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		scn.MoveCardsToHand(cursed, oathbreaker);

		scn.StartGame();

		scn.FreepsPlayCard(cursed);
		assertEquals(2, scn.GetWoundsOn(cursed));
		// His wounds taken off, so that he could exert again if another play set his trigger off.
		scn.RemoveWoundsFromChar(cursed, 2);
		assertEquals(0, scn.GetWoundsOn(cursed));
		assertEquals(1, scn.GetThreats());

		scn.FreepsPlayCard(oathbreaker);
		assertInZone(Zone.FREE_CHARACTERS, oathbreaker);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(cursed));
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedIntoTheStartingFellowshipHeAddsAThreatAndExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");

		// CR 4.2 "starting fellowship": "When you play" text is used on a starting companion (Q-0305).
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(cursed));

		scn.FreepsChooseCard(cursed);

		assertInZone(Zone.FREE_CHARACTERS, cursed);
		assertEquals(2, scn.GetWoundsOn(cursed));
		// No other companion of the deck fits beside his 3 under the total of 4: player 2 is choosing now, and
		// GetThreats reads the current player's threats, so the threat is read once turn 1 has begun.
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		scn.ShadowDeclineChoosing();
		scn.StartGame(true, false);

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(cursed));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseExertingAnotherGondorWraithMakesHimDefenderUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var knight = scn.GetFreepsCard("knight");
		var corpse = scn.GetShadowCard("corpse");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(cursed, oathbreaker, sleepless, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse, troop);
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertEquals(0, scn.GetWoundsOn(sleepless));
		assertFalse(scn.HasKeyword(cursed, Keyword.DEFENDER));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cursed));
		scn.FreepsAcceptOptionalTrigger();

		// Another [gondor] Wraith: both of the other two, and not himself, a [gondor] Man or a Wraith minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oathbreaker, sleepless));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cursed, knight, corpse));
		scn.FreepsChooseCard(oathbreaker);

		assertEquals(1, scn.GetWoundsOn(oathbreaker));
		assertEquals(0, scn.GetWoundsOn(sleepless));
		assertEquals(1, scn.GetKeywordCount(cursed, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(oathbreaker, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Still defender +1 in the assignment phase, and in a skirmish that follows one already resolved.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetKeywordCount(cursed, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{oathbreaker, troop}, new PhysicalCardImpl[]{sleepless, corpse});
		scn.FreepsResolveSkirmish(oathbreaker);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sleepless);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(cursed, Keyword.DEFENDER));

		// And gone once the regroup phase begins.
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.HasKeyword(cursed, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndItIsNotOfferedAgainAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(cursed, oathbreaker);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cursed));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertFalse(scn.HasKeyword(cursed, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase, with the cost still payable: no offer.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cursed));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOtherGondorWraithToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var knight = scn.GetFreepsCard("knight");
		var corpse = scn.GetShadowCard("corpse");
		scn.MoveCompanionsToTable(cursed, knight);

		scn.StartGame();
		// A [gondor] Man and a Wraith minion beside him, and he is a [gondor] Wraith himself: none is another [gondor] Wraith.
		scn.MoveMinionsToTable(corpse);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cursed));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.HasKeyword(cursed, Keyword.DEFENDER));
	}

	@Test
	public void IsNotOfferedWhenTheOnlyOtherGondorWraithIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("cursed");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(cursed, oathbreaker);
		// Vitality 3 with 2 wounds: exhausted, so he cannot exert.
		scn.AddWoundsToChar(oathbreaker, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertEquals(2, scn.GetWoundsOn(oathbreaker));
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cursed));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(oathbreaker));
		assertFalse(scn.HasKeyword(cursed, Keyword.DEFENDER));
	}
}
