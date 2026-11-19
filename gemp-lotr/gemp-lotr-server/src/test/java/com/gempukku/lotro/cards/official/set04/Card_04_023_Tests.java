package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mob1", "4_23");
					put("mob2", "4_23");

					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, cost 4, site 3; no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man, cost 3, site 3; an assignment ability that needs an ally, and there is none
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man, cost 4, site 4
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, cost 2, site 4

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3, so the Mob (12) does not overwhelm him; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillmanMobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Mob
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may play a [dunland] Man from hand; his twilight cost is -2. That Man is <b>fierce</b> and <b>damage +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var mob = scn.GetFreepsCard("mob1");

		assertEquals("Hillman Mob", mob.getBlueprint().getTitle());
		assertNull(mob.getBlueprint().getSubtitle());
		assertFalse(mob.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mob.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, mob.getBlueprint().getCulture());
		assertEquals(CardType.MINION, mob.getBlueprint().getCardType());
		assertEquals(Race.MAN, mob.getBlueprint().getRace());
		assertEquals(5, mob.getBlueprint().getTwilightCost());
		assertEquals(12, mob.getBlueprint().getStrength());
		assertEquals(1, mob.getBlueprint().getVitality());
		assertEquals(3, mob.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningPlaysADunlandManFromHandForTwoLessWhoIsFierceAndDamagePlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob1 = scn.GetShadowCard("mob1");
		var mob2 = scn.GetShadowCard("mob2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(mob1, mob2);
		scn.SetShadowHand(savage, warrior, saruman, axeman);
		assertInHand(savage, warrior, saruman, axeman);

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion here is roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mob1}, new PhysicalCardImpl[]{boromir, mob2});
		scn.FreepsResolveSkirmish(aragorn);
		// Enough for the Savage or Saruman at full cost.
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// The first Mob beats Aragorn, 12 to 8.  The second, whose skirmish is still to come, makes no offer.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mob1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mob2));
		scn.ShadowChooseOptionalTrigger(mob1);

		// A [dunland] Man from hand, of either title; not the [dunland] minion who is no Man, or the [raider] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInHand(warrior, saruman, axeman);
		// 4 - (4 to play - 2)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		// The Man played, and no other [dunland] Man.
		assertFalse(scn.HasKeyword(mob1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(mob2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(mob1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(mob2, Keyword.DAMAGE));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Still so in the next skirmish.  The second Mob beats Boromir, 12 to 7, and his own offer is declined.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mob2));
		scn.ShadowDeclineOptionalTrigger();

		// And in the fierce round the Savage brings about; nobody is assigned in it.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Gone at the start of the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));
	}

	@Test
	public void DecliningLeavesTheManInHandAndASecondWinOffersAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(mob);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// The Mob beats Aragorn, 12 to 8.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mob));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, to site 5, and a second skirmish for the same Mob.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInPlay(mob);
		// The reconcile on the way made up the Shadow hand: it is laid again.
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// 12 to 8 again.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mob));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// 4 - (4 to play - 2)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(mob);
		scn.SetShadowHand(savage);
		assertInHand(savage);
		// Aragorn made strong enough to win, the Mob given the vitality to live through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.ApplyAdHocModifier(new VitalityModifier(null, mob, null, 1));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetVitality(mob));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// Aragorn beats the Mob, 13 to 12, who takes the wound and lives.
		scn.PassSkirmishActions();
		assertInPlay(mob);
		assertEquals(1, scn.GetWoundsOn(mob));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mob));
		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheManInHandCannotBePaidForEvenAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(mob);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mob);
		scn.FreepsResolveSkirmish(aragorn);
		// One short of the Savage's 4 - 2.
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mob));
		assertInHand(savage);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandManInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mob = scn.GetShadowCard("mob1");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(mob);
		// A [dunland] minion who is no Man and a [raider] Man, each payable in full.
		scn.SetShadowHand(saruman, axeman);
		assertInHand(saruman, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mob));
		assertInHand(saruman, axeman);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
