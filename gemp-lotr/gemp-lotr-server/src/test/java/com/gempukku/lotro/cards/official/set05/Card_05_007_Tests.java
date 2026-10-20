package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishWinScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "5_7");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8, vitality 4
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; Gimli beats and kills one in each round
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gimli, Skilled Defender
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Damage +1</b>.<br>Each time Gimli wins a skirmish, you may wound a minion assigned to skirmish an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Skilled Defender", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	// The second half is Ketura's Q-0034: an optional trigger with no cost whose whole effect would do nothing is
	// not offered (F-0347).  The old at/ test (TriggersAtTest.fpCharWinsSkirmish) had the offer made with the only
	// minion already killed.
	@Test
	public void WinningMayWoundAMinionAssignedToAnUnboundCompanionAndIsNotOfferedWhenThereIsNoSuchMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishWinScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, troop);
		// A second round of skirmishes for Gimli alone: the second Runner sits the first round out and fights
		// him in the fierce round, where the Troop, which is not fierce, is assigned to nobody.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, runner2, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowDeclineAssignments();

		// Gimli 6 against the Runner's 5: he wins and kills it.  The Troop is waiting on Aragorn.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));

		// The Troop is the one minion assigned to skirmish an unbound companion; the Runner assigned to nobody
		// is not wounded.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Aragorn 8 against the Troop's 13: he loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce round: only the second Runner fights.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, runner2);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli has won again, the Runner he beat is dead, and the Troop is assigned to nobody: there is no
		// minion his text could wound, so nothing is offered and the game goes on to the regroup phase.
		assertInZone(Zone.DISCARD, runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
