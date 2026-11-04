package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A bar on using special abilities stops each kind of special ability it reaches, wherever the ability's card lies.
 *
 * Ketura on Q-0039, what a special ability is: "It would count, if it's got the bold text and a colon."  The
 * definition language writes them with several words: `activated` (a phase ability of a card in play, a site's
 * included), `response` (a Response ability of a card in play: mechanics/actions/ResponseAbilityIsASpecialAbilityTests),
 * `addActivated` (an ability one card gives others), `activatedFromStacked`, `activatedInDiscard` and
 * `activatedInDrawDeck`.  Each is an `ActivateCardAction`, and the bar, the definition word
 * `cantUseSpecialAbilities` (and the special-ability half of `cantPlayPhaseEventsOrPhaseSpecialAbilities`), is asked
 * of every one of them as it is offered (`DefaultActionsEnvironment`, phase actions and the optional actions before
 * and after an effect).  This class holds one printed card of each word that a printed bar can reach, stopped
 * under the bar, and its control.
 *
 * <ul>
 * <li>`activated`: Gorgoroth Breaker 8_97's archery ability, under Perspective 18_25 ("Maneuver: Add 2 threats and
 * spot a Wizard to spot a minion in play.  That minion cannot use special abilities until the regroup phase.").</li>
 * <li>`response`: Gorgoroth Soldier 7_278's Response ability, under Perspective.</li>
 * <li>`addActivated`: the assignment ability Reckless Pride 7_13 gives each minion, under Perspective.</li>
 * <li>`activatedFromStacked`: Gorgoroth Ransacker 7_276's skirmish ability, from a site its owner controls, under
 * Concealment 12_44 ("If he or she is skirmishing a roaming minion, your opponents cannot use special
 * abilities").</li>
 * </ul>
 *
 * Not here, and why.  No printed bar reaches an ability used from a discard pile or a draw deck: those abilities
 * are all used in the Shadow phase (Uruk Occupier 102_34, Ordnance Grunt 13_117, Gollum 13_50; the PC events of
 * set 102 from the draw deck), and no printed card bars a Shadow player's special abilities in his Shadow phase
 * without naming a card in play.  An event played from a discard pile by its own text (Dwarf-lords 13_4 and its
 * fellows, Hobbitses Are Dead 7_61) is written `activatedInDiscard` with `specialAbility: false`: it is a play,
 * not a special ability, and no bar on special abilities reaches it.
 *
 * Ketura on Q-0264 (2026-10-12): mechanics tests "should exist that do such a thing", "which will then ensure that
 * future cards using the bar only need to act as A": a card that lays this bar is tested with one ability it stops
 * and its control.
 */
public class ABarOnUsingSpecialAbilitiesTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Perspective: a [gandalf] condition of the support area.  "Maneuver: Add 2 threats and spot a
					// Wizard to spot a minion in play.  That minion cannot use special abilities until the regroup
					// phase."
					put("perspective", "18_25");
					// Gandalf, Friend of the Shirefolk: the Wizard to spot; a strength bonus of his own.
					put("gandalf", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability never used
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if that
					// minion is an Uruk-hai)."
					put("defendit", "4_115");
					// Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability never used.
					put("gimli", "1_13");
					// Reckless Pride: a [dwarven] condition on a [dwarven] companion.  "Each minion gains this
					// ability: 'Assignment: Assign this minion to bearer of Reckless Pride.'"  Its regroup ability is
					// never used.
					put("recklesspride", "7_13");
					// Concealment: a [gondor] Skirmish event, cost 0.  "Make a [gondor] Man strength +1.  If he or she
					// is skirmishing a roaming minion, your opponents cannot use special abilities."
					put("concealment", "12_44");

					// Gorgoroth Breaker: a [sauron] Orc of site 5, vitality 2.  "Archery: Exert this minion or stack
					// it on a site you control to make the fellowship archery total -2."
					put("breaker1", "8_97");
					put("breaker2", "8_97");
					// Gorgoroth Soldier: a [sauron] Orc of site 5, vitality 2.  "Response: If a besieger is about to
					// take a wound, discard 2 cards from hand to prevent it."
					put("soldier1", "7_278");
					put("soldier2", "7_278");
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
					// Gorgoroth Ransacker: a [sauron] Orc.  "Skirmish: If this minion is stacked on a site you
					// control, discard him to make a [sauron] Orc strength +5 (or +10 if you have initiative)."
					put("ransacker", "7_276");
					// Orc Soldier: a [sauron] Orc of site 6, strength 7, vitality 2.  "Skirmish: Exert this minion to
					// wound a character he is skirmishing."
					put("orcsoldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APhaseAbilityOfACardInPlayIsNotOfferedUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// activated: Gorgoroth Breaker 8_97, under Perspective 18_25.
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(perspective);

		var breaker1 = scn.GetShadowCard("breaker1");
		var breaker2 = scn.GetShadowCard("breaker2");

		scn.StartGame();
		scn.MoveMinionsToTable(breaker1, breaker2);
		scn.SkipToPhase(Phase.MANEUVER);

		// The first Breaker is spotted.
		scn.FreepsUseCardAction(perspective);
		scn.FreepsChooseCard(breaker1);
		assertEquals(2, scn.GetThreats());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// His archery ability is not offered; the other Breaker's is.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(breaker1));
		assertTrue(scn.ShadowActionAvailable(breaker2));
	}

	@Test
	public void AResponseAbilityOfACardInPlayIsNotOfferedUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// response: Gorgoroth Soldier 7_278, under Perspective 18_25.
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(perspective);
		scn.MoveCardsToHand(defendit);

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		// Two cards, to pay for either Soldier's prevention.
		scn.MoveCardsToHand(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToPhase(Phase.MANEUVER);

		// The first Soldier is spotted.
		scn.FreepsUseCardAction(perspective);
		scn.FreepsChooseCard(soldier1);
		scn.ShadowPass();

		// A wound about to be placed on him, a besieger, is answered by the other Soldier's Response ability and
		// not by his own.
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(soldier1);
		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier1));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(soldier1));
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void AnAbilityGivenByAnotherCardIsNotOfferedUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// addActivated: the ability Reckless Pride 7_13 gives each minion, under Perspective 18_25.
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var recklesspride = scn.GetFreepsCard("recklesspride");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.AttachCardsTo(gimli, recklesspride);
		scn.MoveCardsToSupportArea(perspective);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		// The first Troop is spotted.
		scn.FreepsUseCardAction(perspective);
		scn.FreepsChooseCard(troop1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ability the Troops have from Reckless Pride is offered for the other, and not for him.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop1));
		assertTrue(scn.ShadowActionAvailable(troop2));

		scn.ShadowUseCardAction(troop2);
		assertTrue(scn.IsCharAssigned(gimli));
		assertTrue(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(troop1));
	}

	@Test
	public void AnAbilityOfAStackedCardIsNotOfferedUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		// activatedFromStacked: Gorgoroth Ransacker 7_276, under Concealment 12_44.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var concealment = scn.GetFreepsCard("concealment");
		scn.MoveCompanionsToTable(aragorn);

		var ransacker = scn.GetShadowCard("ransacker");
		var orcsoldier = scn.GetShadowCard("orcsoldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.StackCardsOn(scn.GetSite(1), ransacker);
		scn.MoveCardsToHand(concealment);
		scn.FreepsPass();

		// At site 3 the Orc Soldier, of site 6, is roaming.
		scn.MoveMinionsToTable(orcsoldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orcsoldier);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Without the bar, the Ransacker's ability is offered from the site he is stacked on.  The Shadow player
		// uses the Orc Soldier's instead, so that the skirmish goes on.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ransacker));
		scn.ShadowUseCardAction(orcsoldier);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Aragorn, the one [gondor] Man, is skirmishing a roaming minion: the Shadow player may use no special
		// ability.
		scn.FreepsPlayCard(concealment);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ransacker));
		assertInZone(Zone.STACKED, ransacker);
	}
}
