package com.gempukku.lotro.mechanics.wounds;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Who kills a character, for a text that names its killer ("if bearer kills a companion", "if a [sauron] Orc kills a
 * companion", the definition word `killer` of a `killed` trigger).
 *
 * Comprehensive Rules 4.2, Section Three, The Ring's Oppression 1_273: "A [Sauron] Orc kills a companion when the
 * game text that kills that companion comes from a [Sauron] Orc minion card, or that companion is killed by losing a
 * skirmish involving a [Sauron] Orc minion."  Ketura read it for every such text (Q-0328: B).  So the killers of a
 * companion who loses a skirmish, whether the wound for losing kills him or he is overwhelmed, are every minion in
 * it; a companion killed by one minion's own text is that minion's kill alone, whoever else is in his skirmish.
 *
 * A wound that kills is answered as a kill by the wound's source.  A required trigger is gathered as the wound's
 * results are, before the rule's death has carried the character off; a response is asked after it.  Both answer
 * the same kill (TriggerConditions.forEachMortallyWoundedBy).
 *
 * Goblin Spear 1_182 is the one printed card whose required trigger names its killer ("If bearer kills a companion in
 * a skirmish, add 1 burden (or 2 burdens if that companion was a Hobbit)"); The Ring's Oppression 1_273 is a
 * response that does ("If a [sauron] Orc kills a companion, add 1 burden").  The Spear goes on a [moria] minion, so
 * a skirmish with a minion of each is a skirmish both read.
 */
public class WhoKillsACharacterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "1_182");      // Goblin Spear: strength +2; bearer must be a [moria] minion
					put("oppression", "1_273"); // The Ring's Oppression: Response event, twilight 3

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its (2) only when played
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; adds (2) only when a weapon is played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; "Skirmish: Exert this minion to wound a character he is skirmishing"

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: Hobbit companion, strength 3 (+2 only with a weapon), vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWoundForLosingThatKillsIsAKillByEveryMinionInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var oppression = scn.GetShadowCard("oppression");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Aragorn has one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(runner, chieftain);
		scn.AttachCardsTo(runner, spear);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(spear, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowAssignToMinions(aragorn, chieftain);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());

		// 7 + 7 against 8: no overwhelm, and the wound for losing kills him.  The Spear's bearer killed him, though
		// another minion was in the skirmish: its required trigger is ordered with the rule's death.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());

		// And so did the [sauron] Orc, though the Spear's bearer was in it: the response is offered.
		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnOverwhelmIsAKillByEveryMinionInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var oppression = scn.GetShadowCard("oppression");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, chieftain);
		scn.AttachCardsTo(runner, spear);
		assertAttachedTo(spear, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, chieftain);
		scn.ShadowAssignToMinions(merry, runner);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetBurdens());

		// 7 + 7 against 3: Merry is overwhelmed, by both.  A Hobbit: the Spear adds 2.
		scn.PassSkirmishActions();
		// The kill of an overwhelm names both as its killers; the Spear's trigger is ordered with the rule's own answer.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, merry);
		assertEquals(2, scn.GetBurdens());

		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AKillByAMinionsOwnTextIsThatMinionsAloneThoughAnotherIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var oppression = scn.GetShadowCard("oppression");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Gimli has one vitality left.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(runner, soldier);
		scn.AttachCardsTo(runner, spear);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(spear, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowAssignToMinions(gimli, soldier);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsPass();
		// The Orc Soldier's own wound kills Gimli before the skirmish resolves: the Soldier killed him, and the
		// Spear's bearer did not (no order to make, no burden).
		scn.ShadowUseCardAction(soldier);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
