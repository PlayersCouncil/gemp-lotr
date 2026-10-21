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

public class Card_04_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arsonist1", "4_9");
					put("arsonist2", "4_9");

					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, cost 4, site 3; no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man, cost 3, site 3; an assignment ability that needs an ally, and there is none
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man, cost 4, site 4
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, cost 2, site 4

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingArsonistStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Arsonist
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may play a [dunland] Man from hand; his twilight cost is -2. That Man is <b>fierce</b> and <b>damage +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arsonist1");

		assertEquals("Dunlending Arsonist", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningPlaysADunlandManFromHandForTwoLessWhoIsFierceAndDamagePlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arsonist1 = scn.GetShadowCard("arsonist1");
		var arsonist2 = scn.GetShadowCard("arsonist2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(arsonist1, arsonist2);
		scn.SetShadowHand(savage, warrior, saruman, axeman);
		assertInHand(savage, warrior, saruman, axeman);

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion here is roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, arsonist1}, new PhysicalCardImpl[]{gimli, arsonist2});
		scn.FreepsResolveSkirmish(aragorn);
		// Enough for the Savage or Saruman at full cost.
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// The first Arsonist beats Aragorn, 10 to 8.  The second, whose skirmish is still to come, makes no offer.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arsonist1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arsonist2));
		scn.ShadowChooseOptionalTrigger(arsonist1);

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
		assertFalse(scn.HasKeyword(arsonist1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(arsonist2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(arsonist1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(arsonist2, Keyword.DAMAGE));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Still so in the next skirmish.  The second Arsonist beats Gimli, 10 to 6, and his own offer is declined.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arsonist2));
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

		var arsonist = scn.GetShadowCard("arsonist1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(arsonist);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, arsonist);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// The Arsonist beats Aragorn, 10 to 8.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arsonist));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, to site 5, and a second skirmish for the same Arsonist.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInPlay(arsonist);
		// The reconcile on the way made up the Shadow hand: it is laid again.
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, arsonist);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// 10 to 8 again.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arsonist));
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

		var arsonist = scn.GetShadowCard("arsonist1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(arsonist);
		scn.SetShadowHand(savage);
		assertInHand(savage);
		// Aragorn made strong enough to win, the Arsonist given the vitality to live through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.ApplyAdHocModifier(new VitalityModifier(null, arsonist, null, 1));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetVitality(arsonist));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, arsonist);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// Aragorn beats the Arsonist, 13 to 10, who takes the wound and lives.
		scn.PassSkirmishActions();
		assertInPlay(arsonist);
		assertEquals(1, scn.GetWoundsOn(arsonist));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arsonist));
		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheManInHandCannotBePaidForEvenAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arsonist = scn.GetShadowCard("arsonist1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(arsonist);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, arsonist);
		scn.FreepsResolveSkirmish(aragorn);
		// One short of the Savage's 4 - 2.
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arsonist));
		assertInHand(savage);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandManInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arsonist = scn.GetShadowCard("arsonist1");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(arsonist);
		// A [dunland] minion who is no Man and a [raider] Man, each payable in full.
		scn.SetShadowHand(saruman, axeman);
		assertInHand(saruman, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, arsonist);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arsonist));
		assertInHand(saruman, axeman);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
