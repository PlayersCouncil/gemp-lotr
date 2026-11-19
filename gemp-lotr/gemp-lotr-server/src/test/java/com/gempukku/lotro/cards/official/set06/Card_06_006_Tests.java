package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("clan1", "6_6");
					put("clan2", "6_6");

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
	public void HillClanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Hill Clan
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 17
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may play a [dunland] Man from hand. That Man is <b>fierce</b> and <b>damage +2</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var clan = scn.GetFreepsCard("clan1");

		assertEquals("Hill Clan", clan.getBlueprint().getTitle());
		assertNull(clan.getBlueprint().getSubtitle());
		assertFalse(clan.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, clan.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, clan.getBlueprint().getCulture());
		assertEquals(CardType.MINION, clan.getBlueprint().getCardType());
		assertEquals(Race.MAN, clan.getBlueprint().getRace());
		assertEquals(8, clan.getBlueprint().getTwilightCost());
		assertEquals(17, clan.getBlueprint().getStrength());
		assertEquals(2, clan.getBlueprint().getVitality());
		assertEquals(3, clan.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningPlaysADunlandManFromHandWhoIsFierceAndDamagePlusTwoUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clan1 = scn.GetShadowCard("clan1");
		var clan2 = scn.GetShadowCard("clan2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Strength enough that 17 is not double theirs: each loses and is wounded, not overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 3));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(gimli));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan1, clan2);
		scn.SetShadowHand(savage, warrior, saruman, axeman);
		assertInHand(savage, warrior, saruman, axeman);

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion here is roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, clan1}, new PhysicalCardImpl[]{gimli, clan2});
		scn.FreepsResolveSkirmish(aragorn);
		// Enough for the Savage at full cost, and for the Warrior after him.
		scn.SetTwilight(7);
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// The first Hill Clan beats Aragorn, 17 to 9.  The second, whose skirmish is still to come, makes no offer.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(clan2));
		scn.ShadowChooseOptionalTrigger(clan1);

		// A [dunland] Man from hand, of either title; not the [dunland] minion who is no Man, or the [raider] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInHand(warrior, saruman, axeman);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		// The Man played, and no other [dunland] Man.
		assertFalse(scn.HasKeyword(clan1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(clan2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(clan1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(clan2, Keyword.DAMAGE));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Still so in the next skirmish.  The second Hill Clan beats Gimli, 17 to 9, and his own offer is declined.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan2));
		scn.ShadowDeclineOptionalTrigger();

		// And in the fierce round the Savage brings about; nobody is assigned in it.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
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

		var clan = scn.GetShadowCard("clan1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Strength enough that 17 is not double his: he loses and is wounded, not overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		assertEquals(9, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// The Hill Clan beats Aragorn, 17 to 9.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, to site 5, and a second skirmish for the same Hill Clan.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInPlay(clan);
		// The reconcile on the way made up the Shadow hand: it is laid again.
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// 17 to 9 again.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clan = scn.GetShadowCard("clan1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn made strong enough to win, and not to overwhelm: 18 against 17.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		assertEquals(18, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(clan));

		// Aragorn beats the Hill Clan, who takes the wound and lives on his vitality of 2.
		scn.PassSkirmishActions();
		assertInPlay(clan);
		assertEquals(1, scn.GetWoundsOn(clan));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(clan));
		assertInHand(savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheManInHandCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clan = scn.GetShadowCard("clan1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		assertEquals(9, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		scn.SetShadowHand(savage);
		assertInHand(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);
		// One short of the Savage's 4: the play is the cost of the trigger's effect.
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		// 17 to 9: the Hill Clan wins, and nothing is offered.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(clan));
		assertInHand(savage);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandManInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clan = scn.GetShadowCard("clan1");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		assertEquals(9, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		// A [dunland] minion who is no Man and a [raider] Man, each payable in full.
		scn.SetShadowHand(saruman, axeman);
		assertInHand(saruman, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// 17 to 9: the Hill Clan wins, and nothing is offered.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(clan));
		assertInHand(saruman, axeman);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
