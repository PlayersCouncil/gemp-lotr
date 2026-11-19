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

public class Card_04_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pool", "4_120");
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3, keywords only
					put("damrod", "4_114");     // Damrod, Ranger of Ithilien: Ring-bound Man, strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8, vitality 4
					put("sam", "4_315");        // Sam, Frodo's Gardener: a Ring-bound Hobbit who is not the Ring-bearer, strength 3, vitality 4
					put("raiders1", "1_158");   // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("raiders2", "1_158");
					put("raiders3", "1_158");
					put("savage", "1_151");     // Uruk Savage: strength 5, damage +1 and nothing else
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pool", "4_120");
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3
					// Lurtz, Servant of Isengard: archer, damage +1, strength 13 (under twice Boromir's 7); his
					// maneuver ability needs another Uruk-hai
					put("lurtz", "1_127");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pool", "4_120");
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForbiddenPoolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Forbidden Pool
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While the fellowship is at site 6[T], each Ring-bound Man takes no more than 1 wound during each skirmish phase.<br><b>Fellowship:</b> Add (2) and discard this condition to heal a Ring-bound Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pool");

		assertEquals("Forbidden Pool", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite6TEachRingBoundManTakesOneWoundLosingToADamageMinionAndOthersTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var raiders1 = scn.GetShadowCard("raiders1");
		var raiders2 = scn.GetShadowCard("raiders2");
		var raiders3 = scn.GetShadowCard("raiders3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, damrod, aragorn, sam);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(raiders1, raiders2, raiders3, savage);
		scn.SkipToAssignments();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { boromir, raiders1 },
				new PhysicalCardImpl[] { damrod, raiders2 },
				new PhysicalCardImpl[] { aragorn, raiders3 },
				new PhysicalCardImpl[] { sam, savage }
		);

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// 7 against 9: the wound and the damage +1 would be 2; a Ring-bound Man takes 1.
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(damrod);
		scn.PassSkirmishActions();
		// 6 against 9: a Ring-bound Man of another title, 1 wound.
		assertEquals(1, scn.GetWoundsOn(damrod));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 8 against 9: an unbound Man takes both.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		// 3 against 5: a Ring-bound Hobbit takes both.
		assertEquals(2, scn.GetWoundsOn(sam));

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOneWoundAllowedRenewsInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var raiders = scn.GetShadowCard("raiders1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		// Caves of Aglarond, site 6T: "Maneuver: Remove (2) and spot your Uruk-hai to make it fierce until the
		// regroup phase", used by the Shadow player on his Raiding Party.
		var caves = scn.GetSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		scn.ShadowUseCardAction(caves);
		assertTrue(scn.HasKeyword(raiders, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, raiders);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		// 7 against 9: 2 wounds owed, 1 taken.
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, raiders);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		// A skirmish phase of its own: 1 wound again. One in all would leave him at 1; no limit, 4 and dead.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnArcheryWoundAtSite6TIsTakenAndLeavesTheSkirmishsOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(lurtz);

		scn.SkipToArcheryWounds();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		// The archery phase is no skirmish phase: the wound is taken.
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, lurtz);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// 7 against 13: of the 2 wounds owed he takes 1, the archery wound before it no part of that.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite5TARingBoundManTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var raiders = scn.GetShadowCard("raiders1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsAssignToMinions(boromir, raiders);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite6OfTheFellowshipBlockARingBoundManTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();

		// Galadriel's Glade, site 6 of the Fellowship block, which prints no block symbol.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsAssignToMinions(boromir, raiders);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipActionAddsTwoAndDiscardsItselfToHealARingBoundMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, damrod, aragorn);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(damrod, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pool);
		assertTrue(scn.FreepsActionAvailable(pool));

		scn.FreepsUseCardAction(pool);
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(pool);
		// Ring-bound Men of two titles are offered; an unbound Man and the Ring-bound Hobbit, all wounded, are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, damrod));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedRingBoundManTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(pool);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertInZone(Zone.SUPPORT, pool);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(pool));

		scn.FreepsUseCardAction(pool);
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(pool);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pool = scn.GetFreepsCard("pool");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(pool);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pool));
		assertInZone(Zone.SUPPORT, pool);
		assertEquals(1, scn.GetWoundsOn(boromir));
	}
}
