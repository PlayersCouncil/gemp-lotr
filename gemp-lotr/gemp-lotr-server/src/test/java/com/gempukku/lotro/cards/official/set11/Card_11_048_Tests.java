package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquished", "11_48");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4; his texts are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; fierce
					put("pursuer1", "4_190");   // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer2", "4_190");
					put("pursuer3", "4_190");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his texts are used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2; no text in play
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; skirmishes by his own maneuver ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("pursuer4", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotYetVanquishedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Not Yet Vanquished
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a companion loses a fierce skirmish, you may exert Gollum to add a burden.
		*/

		var scn = GetScenario();

		var vanquished = scn.GetFreepsCard("vanquished");

		assertEquals("Not Yet Vanquished", vanquished.getBlueprint().getTitle());
		assertNull(vanquished.getBlueprint().getSubtitle());
		assertTrue(vanquished.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, vanquished.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, vanquished.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, vanquished.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(vanquished, Keyword.SUPPORT_AREA));
		assertEquals(0, vanquished.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotOfferedForANormalSkirmishLostAndForAFierceOneGollumExertsToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var vanquished = scn.GetShadowCard("vanquished");
		var gollum = scn.GetShadowCard("gollum");
		var pursuer = scn.GetShadowCard("pursuer1");
		scn.MoveCardsToSupportArea(vanquished);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Sam (3) loses the normal skirmish to the Uruk Pursuer (5): no offer.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// He loses the fierce one too.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedNothingChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var vanquished = scn.GetShadowCard("vanquished");
		var gollum = scn.GetShadowCard("gollum");
		var pursuer = scn.GetShadowCard("pursuer1");
		scn.MoveCardsToSupportArea(vanquished);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, pursuer);

		// Nobody assigned in the normal round: the fierce round follows.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCompanionLosingAFierceSkirmishOffersItAgainAndAnAllyLosingOneDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, guard, aragorn);
		scn.MoveAlliesToTable(thrarin);

		var vanquished = scn.GetShadowCard("vanquished");
		var gollum = scn.GetShadowCard("wraithgollum");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var pursuer3 = scn.GetShadowCard("pursuer3");
		var pursuer4 = scn.GetShadowCard("pursuer4");
		scn.MoveCardsToSupportArea(vanquished);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, pursuer1, pursuer2, pursuer3, pursuer4);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, pursuer1}, new PhysicalCardImpl[]{guard, pursuer2},
				new PhysicalCardImpl[]{thrarin, pursuer3},
				new PhysicalCardImpl[]{aragorn, pursuer4});

		// Aragorn (8) beats a Pursuer (5): a minion loses the fierce skirmish and a companion wins it: no offer.
		assertEquals(0, scn.GetWoundsOn(pursuer4));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer4));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thrarin (4), an ally, loses to a Pursuer (5) and is killed by the wound: no offer.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Sam (3) loses: the [wraith] Gollum exerts to add a burden.
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetBurdens());

		// Dwarf Guard (4) loses: offered again.
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileGollumIsExhaustedThoughShelobCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var vanquished = scn.GetShadowCard("vanquished");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var pursuer = scn.GetShadowCard("pursuer1");
		scn.MoveCardsToSupportArea(vanquished);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, pursuer);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(vanquished));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
