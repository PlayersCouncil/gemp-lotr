package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_299_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cliffs", "4_299");

					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is not [shire]; his skirmish ability is never used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit companion; his bonus needs a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit; a maneuver ability
					// Bounder: a [shire] Hobbit ally, home site 2 of this path, where he may be assigned; his skirmish
					// ability is never used.
					put("bounder", "1_286");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; his text needs 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cliffs", "4_299");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CliffsofEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Cliffs of Emyn Muil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	When the fellowship moves from site 1[T], remove up to 2 burdens.
		 * 	<b>Skirmish:</b> Discard this condition to wound a minion a Hobbit companion is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cliffs");

		assertEquals("Cliffs of Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MovingFromSite1OfTheTowersPathRemovesUpToTwoBurdensAndMovingFromSite2RemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPass();

		// None, or up to 2 of the 3 there are; not asked whether to remove any first.
		assertTrue(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(1, scn.GetBurdens());

		// The second move of the turn is from site 2.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChooseToMove();

		assertFalse(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromSite1OfAnotherBlockRemovesNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetCurrentSiteNumber());

		// Site 1 here is Bag End 1_319, of the Fellowship block.
		scn.FreepsPass();

		assertFalse(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToWoundAMinionTheHobbitCompanionIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, merry);
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, axeman);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cliffs));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{pippin, savage},
				new PhysicalCardImpl[]{merry, axeman});
		// The Shadow player puts the Troop on Pippin as well: two minions in his skirmish.
		scn.ShadowAssignToMinions(pippin, troop);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.FreepsActionAvailable(cliffs));
		scn.FreepsUseCardAction(cliffs);

		// Both minions Pippin is skirmishing; not the Axeman, assigned to Merry, whose skirmish has not begun.
		assertInZone(Zone.DISCARD, cliffs);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axeman, pippin, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInTheSkirmishOfTheRingBearerItWoundsAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, savage},
				new PhysicalCardImpl[]{pippin, axeman});
		// The Shadow player puts the Troop on the Ring-bearer as well: two minions in his skirmish.
		scn.ShadowAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		// The Ring-bearer is a Hobbit companion who is Ring-bound and [shire]; Pippin, unbound and not [shire],
		// is the Hobbit of the other test.
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.FreepsActionAvailable(cliffs));
		scn.FreepsUseCardAction(cliffs);

		// Both minions the Ring-bearer is skirmishing; not the Axeman, assigned to Pippin, whose skirmish has not begun.
		assertInZone(Zone.DISCARD, cliffs);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axeman, pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInTheSkirmishOfACompanionWhoIsNoHobbitItWoundsNobody()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{sam, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		// The discard can be paid, so it is offered, and it is paid for nothing.
		assertTrue(scn.FreepsActionAvailable(cliffs));
		scn.FreepsUseCardAction(cliffs);

		assertInZone(Zone.DISCARD, cliffs);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInTheSkirmishOfAHobbitAllyItWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(cliffs);
		assertInZone(Zone.SUPPORT, cliffs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// The move to site 2, Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(bounder, troop);
		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(cliffs));
		scn.FreepsUseCardAction(cliffs);

		assertInZone(Zone.DISCARD, cliffs);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
