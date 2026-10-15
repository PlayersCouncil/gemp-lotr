package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174");
					put("backstabber2", "1_174");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its text answers only its own play
					put("tentacle", "101_36");  // Vile Tentacle: [moria] minion who is no Orc (a Creature)
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinBackstabberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Backstabber
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: This minion is <b>damage +2</b> while in the same skirmish as another [moria] Orc.
		*/

		var scn = GetScenario();

		var backstabber = scn.GetFreepsCard("backstabber");

		assertEquals("Goblin Backstabber", backstabber.getBlueprint().getTitle());
		assertNull(backstabber.getBlueprint().getSubtitle());
		assertFalse(backstabber.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, backstabber.getBlueprint().getSide());
		assertEquals(Culture.MORIA, backstabber.getBlueprint().getCulture());
		assertEquals(CardType.MINION, backstabber.getBlueprint().getCardType());
		assertEquals(Race.ORC, backstabber.getBlueprint().getRace());
		assertEquals(1, backstabber.getBlueprint().getTwilightCost());
		assertEquals(5, backstabber.getBlueprint().getStrength());
		assertEquals(1, backstabber.getBlueprint().getVitality());
		assertEquals(4, backstabber.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus2InASkirmishWithAnotherMoriaOrcWhoGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, runner);

		// On the table together, in no skirmish.
		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, backstabber);
		// The Runner left over goes to the same companion.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, backstabber));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(2, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoBackstabbersInOneSkirmishAreEachDamagePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");
		var backstabber2 = scn.GetShadowCard("backstabber2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, backstabber2);

		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(backstabber2, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, backstabber);
		scn.ShadowAssignToMinions(aragorn, backstabber2);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, backstabber));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, backstabber2));
		assertEquals(2, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(backstabber2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotDamagePlus2WhileTheOtherMoriaOrcWaitsInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, runner);

		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, backstabber},
				new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, backstabber));
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner));
		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotDamagePlus2BesideAMoriaMinionWhoIsNoOrcAndAnOrcOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");
		var tentacle = scn.GetShadowCard("tentacle");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, tentacle, chieftain);

		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, backstabber);
		scn.ShadowAssignToMinions(aragorn, tentacle, chieftain);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, backstabber));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, tentacle));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
