package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_172_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "51_172");
					put("archer2", "51_172");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, no other text
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai archer, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, not an archer; its (2) only when played
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, not an archer, no text
					put("frenzy", "1_171");     // Frenzy: Assignment event, assigns an exhausted companion to skirmish a [moria] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.
		 * 	The fellowship archery total is -1 for each [moria] archer you can spot.
		 * 	Assignment: Exert this minion to prevent [moria] archers from being assigned to skirmishes.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Goblin Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FellowshipArcheryTotalIsMinus1ForEachMoriaArcherSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");
		var marksman = scn.GetShadowCard("marksman");
		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(archer, marksman);

		scn.StartGame();
		// A fellowship archery total high enough that no reading reaches zero.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 7));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertEquals(7, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(archer);
		// 5 to play + 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, archer);
		// Goblin Archer spots itself.
		assertEquals(6, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(marksman);
		assertEquals(5, scn.GetFreepsArcheryTotal());

		// An archer of another culture and a [moria] Orc who is no archer are not counted.
		scn.MoveMinionsToTable(assassin, runner);
		assertEquals(5, scn.GetFreepsArcheryTotal());

		// A second Goblin Archer: three [moria] archers, and each copy takes 3.
		scn.MoveMinionsToTable(archer2);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The minion archery total is untouched: one for each of the four archers.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentAbilityExertsToPreventMoriaArchersFromBeingAssignedThisAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");
		var marksman = scn.GetShadowCard("marksman");
		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The archers go down after the archery phase, so that it fires no arrows.
		scn.MoveMinionsToTable(archer, archer2, marksman, assassin);
		// Goblin Marksman is made fierce, to be assigned again in the fierce assignment phase.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, marksman, null, Keyword.FIERCE));
		scn.FreepsPass();

		assertTrue(scn.FreepsCanAssign(archer));
		assertTrue(scn.FreepsCanAssign(marksman));
		assertEquals(0, scn.GetWoundsOn(archer));
		assertEquals(0, scn.GetWoundsOn(archer2));
		assertTrue(scn.ShadowActionAvailable(archer));
		scn.ShadowUseCardAction(archer);
		assertEquals(1, scn.GetWoundsOn(archer));
		assertEquals(0, scn.GetWoundsOn(archer2));
		scn.PassAssignmentActions();

		// Every [moria] archer is out of reach of the Free Peoples player's assignments...
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(archer));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(archer2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(marksman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(assassin));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		scn.FreepsAssignToMinions(aragorn, runner);

		// ...and of the Shadow player's.
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(archer));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(archer2));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(marksman));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(assassin));
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(frodo));

		// Aragorn, 8, kills Goblin Runner, 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// The fierce assignment phase is another assignment phase: Goblin Marksman can be assigned again.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(marksman));
		scn.FreepsAssignToMinions(aragorn, marksman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, marksman));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DISCARD, marksman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMoriaArcherKeptFromBeingAssignedIsNotTheOneAnotherCardsEventAssigns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var marksman = scn.GetShadowCard("marksman");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var frenzy = scn.GetShadowCard("frenzy");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The archers go down after the archery phase, so that it fires no arrows.
		scn.MoveMinionsToTable(archer, marksman);
		// Vitality 4 with 3 wounds: the one exhausted companion who is not the Ring-bearer, for Frenzy.
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsPass();

		assertTrue(scn.FreepsCanAssign(archer));
		assertTrue(scn.FreepsCanAssign(marksman));
		scn.ShadowUseCardAction(archer);
		assertFalse(scn.FreepsCanAssign(archer));
		assertFalse(scn.FreepsCanAssign(marksman));
		assertTrue(scn.FreepsCanAssign(runner));
		scn.FreepsPass();

		// Frenzy would assign Aragorn against any [moria] Orc.  The two archers are [moria] Orcs too, and are
		// left out; the two who are no archers are offered.
		assertTrue(scn.ShadowPlayAvailable(frenzy));
		scn.ShadowPlayCard(frenzy);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(archer, marksman));
		scn.ShadowChooseCard(runner);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertFalse(scn.IsCharAssignedAgainst(aragorn, archer));
		assertFalse(scn.IsCharAssignedAgainst(aragorn, marksman));
		assertFalse(scn.IsCharAssignedAgainst(aragorn, troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhenThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(archer, archer2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(archer, 2);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(archer));
		// The other copy, fresh, may still use its own.
		assertTrue(scn.ShadowActionAvailable(archer2));
		assertTrue(scn.FreepsCanAssign(archer));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
