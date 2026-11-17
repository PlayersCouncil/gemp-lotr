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

public class Card_11_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wickedness", "11_47");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4; his texts are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; fierce
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an [isengard]
	// minion and a weather card in the draw deck, and none is used).
	protected VirtualTableScenario GetMountainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wickedness", "11_47");
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4; his texts are used only on purpose
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoEndofWickednessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: No End of Wickedness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Gollum to make another minion strength +2 (or +3 if Gollum is at a mountain site).
		*/

		var scn = GetScenario();

		var wickedness = scn.GetFreepsCard("wickedness");

		assertEquals("No End of Wickedness", wickedness.getBlueprint().getTitle());
		assertNull(wickedness.getBlueprint().getSubtitle());
		assertFalse(wickedness.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wickedness.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, wickedness.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, wickedness.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(wickedness, Timeword.SKIRMISH));
		assertEquals(1, wickedness.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromAMountainGollumExertsToMakeAnotherMinionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var wickedness = scn.GetShadowCard("wickedness");
		var gollum = scn.GetShadowCard("gollum");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(wickedness);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, pursuer, runner1, runner2);

		// Site 2, Ettenmoors, is a plains.
		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gimli, runner2});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(pursuer));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(pursuer));
		assertEquals(5, scn.GetStrength(runner1));
		assertTrue(scn.ShadowPlayAvailable(wickedness));
		scn.ShadowPlayCard(wickedness);

		assertEquals(1, scn.GetWoundsOn(gollum));
		// The Runner in this skirmish, the one waiting for Gimli's and the Pursuer assigned to nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, pursuer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gollum, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pursuer);

		assertEquals(7, scn.GetStrength(pursuer));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(gollum));
		assertInDiscard(wickedness);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Gimli's, the next, the Pursuer is 5 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(pursuer));
	}

	@Test
	public void AtAMountainTheWraithGollumExertsToMakeAnotherMinionStrengthPlus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountainScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var wickedness = scn.GetShadowCard("wickedness");
		var gollum = scn.GetShadowCard("wraithgollum");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(wickedness);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, pursuer, runner1, runner2);

		// The fellowship moves to site 4, Pass of Caradhras.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gimli, runner2});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(pursuer));
		assertTrue(scn.ShadowPlayAvailable(wickedness));
		scn.ShadowPlayCard(wickedness);

		assertEquals(1, scn.GetWoundsOn(gollum));
		scn.ShadowChooseCard(pursuer);
		assertEquals(8, scn.GetStrength(pursuer));
		assertEquals(5, scn.GetStrength(runner1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(pursuer));
	}

	@Test
	public void NotPlayableWhileGollumIsExhaustedThoughShelobCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wickedness = scn.GetShadowCard("wickedness");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(wickedness);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum, shelob, runner1);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertInHand(wickedness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wickedness));
		assertInHand(wickedness);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
	}

	@Test
	public void WithNoOtherMinionItIsPlayedAndGollumExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wickedness = scn.GetShadowCard("wickedness");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(wickedness);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(wickedness));
		scn.ShadowPlayCard(wickedness);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(wickedness);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
