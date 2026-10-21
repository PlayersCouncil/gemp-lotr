package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dead", "8_45");
					put("dead2", "8_45");       // a second copy, to show that only the winner's own copy asks
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3; damage +1 is its whole text
					// Black Rider: fierce [wraith] Nazgul, strength 8, vitality 3; strength +1 for each companion
					put("rider", "12_161");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SleeplessDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sleepless Dead
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Enduring</b>. To play, add a threat.<br>Each time this companion wins a skirmish, you may exert him to discard a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dead");

		assertEquals("Sleepless Dead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedWhileNoThreatCanBeAddedUntilAnotherCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCardsToHand(dead, gknight);

		scn.StartGame();
		// The Ring-bearer is the only companion in play, so one threat is as many as there may be. Sleepless Dead is
		// not counted while he is being paid for.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, dead, gknight);
		assertFalse(scn.FreepsPlayAvailable(dead));

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(dead));

		scn.FreepsPlayCard(dead);
		assertInZone(Zone.FREE_CHARACTERS, dead);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WinningASkirmishHeMayExertToDiscardAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		var dead2 = scn.GetFreepsCard("dead2");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(dead, dead2, gknight);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		// A strength that only sets the board: 7 + 12 = 19 beats the Savage and the Troop together, 5 + 13 = 18,
		// without overwhelming either.
		scn.ApplyAdHocModifier(new StrengthModifier(null, dead, null, 12));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dead, savage}, new PhysicalCardImpl[]{gknight, runner});
		// The Troop is left over, and the Shadow player puts it on Sleepless Dead too.
		scn.ShadowAssignToMinions(dead, troop);

		scn.FreepsResolveSkirmish(dead);
		assertEquals(0, scn.GetWoundsOn(dead));
		assertEquals(0, scn.GetWoundsOn(dead2));
		scn.PassSkirmishActions();

		// He wins: each minion he beat takes its wound first. Only his own copy asks.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dead));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dead2));
		scn.FreepsChooseOptionalTrigger(dead);

		assertEquals(1, scn.GetWoundsOn(dead));
		assertEquals(0, scn.GetWoundsOn(dead2));
		// Either minion he is skirmishing, of two cultures and races; not the Runner, waiting for the Knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void LosingASkirmishHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		scn.MoveCompanionsToTable(dead);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dead, troop);
		scn.FreepsResolveSkirmish(dead);
		assertEquals(0, scn.GetWoundsOn(dead));
		scn.PassSkirmishActions();

		// 7 against 13 (under double): he loses, takes a wound and lives, and could still exert.
		assertEquals(1, scn.GetWoundsOn(dead));
		assertInZone(Zone.FREE_CHARACTERS, dead);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dead));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningWhileExhaustedHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		scn.MoveCompanionsToTable(dead);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted, and strength 7 + 4 (enduring).
		scn.AddWoundsToChar(dead, 2);
		assertEquals(2, scn.GetWoundsOn(dead));
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dead, raiders);
		scn.FreepsResolveSkirmish(dead);
		scn.PassSkirmishActions();

		// 11 against 9: he wins, and the Raiding Party takes its wound, but he cannot exert.
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dead));
		assertEquals(2, scn.GetWoundsOn(dead));
		assertInZone(Zone.SHADOW_CHARACTERS, raiders);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtOneWinHeIsAskedAgainAtTheNextInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		scn.MoveCompanionsToTable(dead);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		// A strength that only sets the board: 7 + 4 = 11 against the Rider's 8 + 2 (Frodo and Sleepless Dead).
		scn.ApplyAdHocModifier(new StrengthModifier(null, dead, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dead, rider);
		scn.FreepsResolveSkirmish(dead);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(dead));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dead));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: no exertion, and the Rider is still there for the fierce round.
		assertEquals(0, scn.GetWoundsOn(dead));
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(dead, rider);
		scn.FreepsResolveSkirmish(dead);
		scn.PassFierceSkirmishActions();

		// He wins again, and is asked again.
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dead));
		scn.FreepsChooseOptionalTrigger(dead);

		// The Rider is the only minion he is skirmishing and is discarded unasked.
		assertEquals(1, scn.GetWoundsOn(dead));
		assertInZone(Zone.DISCARD, rider);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainstAMinionHisWinKillsHeIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetFreepsCard("dead");
		scn.MoveCompanionsToTable(dead);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dead, runner);
		scn.FreepsResolveSkirmish(dead);
		assertEquals(0, scn.GetWoundsOn(dead));
		scn.PassSkirmishActions();

		// 7 against 5 (under double): the Runner, vitality 1, is killed by its wound before the trigger is offered.
		assertInZone(Zone.DISCARD, runner);
		// The exertion is a cost, so the trigger is still offered with no minion left to discard.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dead));
		scn.FreepsChooseOptionalTrigger(dead);

		assertEquals(1, scn.GetWoundsOn(dead));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
