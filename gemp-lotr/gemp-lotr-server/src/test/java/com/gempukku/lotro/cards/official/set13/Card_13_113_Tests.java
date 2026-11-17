package com.gempukku.lotro.cards.official.set13;

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

public class Card_13_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breaker", "13_113");
					put("breaker2", "13_113");
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, strength 5, vitality 1; its played trigger is at an underground site only
					put("garrison", "6_52");    // Garrison of Osgiliath: [gondor] condition that holds [gondor] tokens; its skirmish ability is used only on purpose
					put("stout", "4_57");       // Stout and Strong: [dwarven] condition that holds [dwarven] tokens; its skirmish ability is used only on purpose
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession, support area, that holds [raider] tokens; its abilities are used only on purpose
					put("stealth", "1_298");    // Hobbit Stealth: "At sites 1 to 5, cancel a skirmish involving a Hobbit"
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his maneuver ability is used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; his skirmish ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit, strength 3; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcLinebreakerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Orc Line-breaker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: At the end of each skirmish involving this minion, if you can spot a Free Peoples culture token, this minion is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breaker");

		assertEquals("Orc Line-breaker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAGondorTokenEachCopyIsFierceFromTheEndOfItsOwnSkirmishUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);

		var breaker = scn.GetShadowCard("breaker");
		var breaker2 = scn.GetShadowCard("breaker2");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(breaker, breaker2);
		// Only so that a Line-breaker (14) does not overwhelm Boromir: 7 + 1 = 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, breaker}, new PhysicalCardImpl[]{boromir, breaker2});
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(breaker2, Keyword.FIERCE));

		// Aragorn (8) loses to the first.  Only the copy whose skirmish ended is fierce.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(breaker2, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(breaker2, Keyword.FIERCE));

		// Still fierce in the fierce skirmishes, and no longer once the regroup phase begins.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, breaker}, new PhysicalCardImpl[]{boromir, breaker2});
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(breaker2, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(breaker2, Keyword.FIERCE));
	}

	@Test
	public void WithADwarvenTokenItIsFierceThoughTheCompanionItSkirmishedWasKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(stout);
		scn.AddTokensToCard(stout, 1);

		var breaker = scn.GetShadowCard("breaker");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(stout));
		scn.MoveMinionsToTable(breaker, goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, breaker}, new PhysicalCardImpl[]{aragorn, goblin});
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));

		// 14 is more than double Gimli's 6: he is overwhelmed and killed.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
	}

	@Test
	public void WithACanceledSkirmishItIsFierceAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var garrison = scn.GetFreepsCard("garrison");
		var stealth = scn.GetFreepsCard("stealth");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);
		scn.MoveCardsToHand(stealth);

		var breaker = scn.GetShadowCard("breaker");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertInHand(stealth);
		scn.MoveMinionsToTable(breaker, goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, breaker}, new PhysicalCardImpl[]{aragorn, goblin});
		scn.FreepsResolveSkirmish(sam);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(stealth);

		// Canceled at site 2: no wound, and the skirmish is over.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(breaker));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
	}

	@Test
	public void WithOnlyAShadowCultureTokenItIsNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var breaker = scn.GetShadowCard("breaker");
		var goblin = scn.GetShadowCard("goblin");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.MoveMinionsToTable(breaker, goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, breaker}, new PhysicalCardImpl[]{boromir, goblin});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));
		assertEquals(2, scn.GetCultureTokensOn(ships));
	}

	@Test
	public void ItIsFierceAgainAfterItsSkirmishAtTheSecondSiteOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);

		var breaker = scn.GetShadowCard("breaker");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(breaker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, breaker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, breaker);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));

		// The fellowship moves on to site 3, and the Line-breaker, still in play, skirmishes Aragorn again.
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, breaker);
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(breaker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(breaker, Keyword.FIERCE));
	}
}
