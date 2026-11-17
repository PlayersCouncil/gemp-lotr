package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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
					put("gimli", "5_7");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4
					put("guard", "1_7");        // Dwarf Guard: unbound Dwarf, strength 4, vitality 2, no text that matters here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule, not the Ring-bearer
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally whose home is site 3, so he skirmishes there

					put("savage", "1_151");     // Uruk Savage: [isengard], strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("runner", "1_178");     // Goblin Runner: [moria], strength 5, vitality 1; Gimli beats and kills it
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce in print; its other text answers
					// an unbound Hobbit, of whom there is none here
					put("pursuer", "4_190");
					put("pursuer2", "4_190");
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
		 * Game Text: <b>Damage +1</b>.
		 * Each time Gimli wins a skirmish, you may wound a minion assigned to skirmish an unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

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

	@Test
	public void WinningLetsGimliWoundOnlyAMinionAssignedToAnUnboundCompanionTheOneHeBeatIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli, aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var troop4 = scn.GetShadowCard("troop4");

		scn.StartGame();
		// Elrond can only be assigned at his home, site 3: the fellowship moves there from site 2.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, savage2, troop, troop2, troop3, troop4);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// troop4 is left unassigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage},
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{guard, savage2},
				new PhysicalCardImpl[]{sam, troop2},
				new PhysicalCardImpl[]{elrond, troop3});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
		assertEquals(0, scn.GetWoundsOn(troop4));

		// Gimli 6 against Uruk Savage 5.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// The minion Gimli has just beaten is still assigned until the triggers of the win have resolved.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, savage2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2, troop3, troop4));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// 1 wound for losing + 1 for Gimli's damage + 1 from his trigger: vitality 3, killed.
		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
		assertEquals(0, scn.GetWoundsOn(troop4));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void GimliWoundsAMinionEachTimeHeWinsASkirmishTheFierceSkirmishIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		// Both minions are fierce, so each skirmishes twice.
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{aragorn, pursuer2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		// Gimli 6 against the Pursuer's 5.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(pursuer2);
		assertEquals(1, scn.GetWoundsOn(pursuer2));
		// 1 for losing + 1 for Gimli's damage.
		assertEquals(2, scn.GetWoundsOn(pursuer));

		// Aragorn 8 against the second Pursuer's 5: he wins, and it takes its second wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer2));

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{aragorn, pursuer2});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		// The first Pursuer, already on 2 wounds, is killed by losing again, so the second is the one minion left
		// to choose: its third wound kills it.
		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(pursuer2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GimliMayDeclineToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(troop));
		// Only the wounds for losing to Gimli: 1 + 1 for his damage.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NothingIsOfferedWhenAnotherCompanionWinsOrWhenGimliLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, aragorn, guard);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, brigand);
		scn.SkipToAssignments();
		// The minion on Dwarf Guard waits throughout as something Gimli's trigger could wound.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, brigand},
				new PhysicalCardImpl[]{guard, savage2});

		// Aragorn 8 against Uruk Savage 5: Aragorn wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli 6 against Dunlending Brigand 8: Gimli loses, and is not overwhelmed.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ADwarfAloneIsAnUnboundCompanionWhoseMinionMayBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, guard, sam);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, troop2);
		scn.SkipToAssignments();
		// Every minion waiting is on a Dwarf or on Sam: no Man, no Elf, no [gondor] companion.  The Savage Gimli
		// beats is still assigned to him.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage},
				new PhysicalCardImpl[]{guard, troop},
				new PhysicalCardImpl[]{sam, troop2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// Gimli 6 against Uruk Savage 5.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
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
