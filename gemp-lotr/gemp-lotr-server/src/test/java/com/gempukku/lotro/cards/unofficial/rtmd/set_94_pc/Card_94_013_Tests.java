package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_013_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");       // Legolas, Greenleaf: an [elven] archer companion, strength 6, with an Archery ability of his own
					put("orophin", "1_56");       // Orophin, Lorien Bowman: an [elven] Elf ally archer, strength 3, home site 6 (so he takes no part in archery at site 1); his Archery ability (exert him to wound an Uruk-hai) is offered all the same
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no archer, strength 8
					put("bow", "1_90");           // Aragorn's Bow: makes Aragorn an archer, and has an Archery ability; no strength bonus
					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text
					put("runner", "1_178");       // Goblin Runner: a minion who is no archer, strength 5, vitality 1
					put("marksman", "1_176");     // Goblin Marksman: a [moria] Orc, strength 7, vitality 1; Archer is its whole text
					put("southron", "4_245");     // Southron Archer: a [raider] Man archer, strength 6, vitality 2; its text needs another Southron
					put("crossbowman", "4_183");  // Uruk Crossbowman: an [isengard] Uruk-hai archer, strength 6, vitality 2, with an Archery ability
					put("cloud", "4_145");        // Cloud of Arrows: a Shadow Archery event, cost 0; exert an [isengard] archer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_13", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");       // Legolas, Greenleaf: an [elven] archer companion, strength 6, with an Archery ability of his own
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no archer, strength 8
					put("bow", "1_90");           // Aragorn's Bow: makes Aragorn an archer, and has an Archery ability; no strength bonus
					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text
					put("runner", "1_178");       // Goblin Runner: a minion who is no archer, strength 5, vitality 1
					put("marksman", "1_176");     // Goblin Marksman: a [moria] Orc, strength 7, vitality 1; Archer is its whole text
					put("southron", "4_245");     // Southron Archer: a [raider] Man archer, strength 6, vitality 2; its text needs another Southron
					put("crossbowman", "4_183");  // Uruk Crossbowman: an [isengard] Uruk-hai archer, strength 6, vitality 2, with an Archery ability
					put("cloud", "4_145");        // Cloud of Arrows: a Shadow Archery event, cost 0; exert an [isengard] archer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_13"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_13
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Archery: Make each of your archers strength +3 until the regroup phase. Your
		 * archers do not contribute to your archery total and you cannot make any more Archery
		 * actions.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_13", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ArcherGetsStrengthAndStopsContributingAndLocksOutFurtherArcheryActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var orophin = scn.GetFreepsCard("orophin");

		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// Pre-check: base strengths and archery total (Legolas is the only archer who takes part: Orophin is an ally away from his home)
		int legolasBase = scn.GetStrength(legolas);
		int aragornBase = scn.GetStrength(aragorn);
		assertEquals(6, legolasBase);
		assertEquals(8, aragornBase);
		assertEquals(3, scn.GetStrength(orophin));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsActionAvailable(mod));
		// Legolas's own Archery ability is on offer until the modifier is used.
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(mod);

		// Effect: Legolas +3 strength and Orophin, an archer who is an ally, +3 as well; Aragorn untouched, Legolas no longer counted in archery total
		assertEquals(legolasBase + 3, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(orophin));
		assertEquals(aragornBase, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Self-lockout: no more archery actions this phase (including Legolas's own archery ability).
		// The Shadow player is asked next; once he passes, the Free Peoples player is asked again.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertFalse(scn.FreepsAnyActionsAvailable());

		// Duration: bonus gone at regroup
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(legolasBase, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ActionAvailableWithoutAnArcherButEffectFizzlesAndCostIsStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// No archer in play, but there is no CanSpot requirement: costs/requirements gate,
		// effects merely fizzle. The action is still available and still locks out the phase.
		int aragornBase = scn.GetStrength(aragorn);
		assertEquals(8, aragornBase);
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		assertEquals(aragornBase, scn.GetStrength(aragorn));
		// The Shadow player is asked next; once he passes, the Free Peoples player is asked again.
		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsAnyActionsAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotOfferedTheFreepsOwnersActionAndTheOwnersArcherIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		// "Your archers": the action is the modifier's owner's alone.  A MetaSite's Activated ability
		// in a back-and-forth phase (archery) would otherwise be offered to whichever player is acting.
		// Here the Free Peoples player owns it, and the Shadow player, whose turn to act it is, is not
		// offered it; the owner's archer is as he was.
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(legolas);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		int legolasBase = scn.GetStrength(legolas);
		assertEquals(6, legolasBase);

		// Freeps passes their archery action first (back-and-forth), then Shadow is asked.
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));

		assertEquals(legolasBase, scn.GetStrength(legolas));
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void TheOtherPlayerIsNotOfferedTheOwnersArcheryAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var marksman = scn.GetShadowCard("marksman");
		var southron = scn.GetShadowCard("southron");

		scn.MoveCompanionsToTable(legolas);
		scn.StartGame();
		scn.MoveMinionsToTable(marksman, southron);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		// The Shadow player has two archers of his own that the action would strengthen.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void TheShadowOwnersArchersAreStrengthPlus3UntilTheRegroupPhaseAndAddNothingToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		var southron = scn.GetShadowCard("southron");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(marksman, southron, runner);

		// An Archery action: with the same archers on the table it is not offered in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(6, scn.GetStrength(southron));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(legolas));
		// The Marksman and the Southron Archer; Legolas for the fellowship.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Each of the owner's archers, of whatever culture and race; not his minion who is no archer,
		// and not the other player's archer, who still counts for the fellowship's total.
		assertEquals(10, scn.GetStrength(marksman));
		assertEquals(9, scn.GetStrength(southron));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Archery fire: Legolas's one wound is placed on a minion, and the minions' total of 0 wounds nobody.
		scn.PassArcheryActions();
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, marksman},
				new PhysicalCardImpl[]{legolas, southron});
		scn.FreepsResolveSkirmish(aragorn);
		// 10 against Aragorn's 8
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Still +3 in a second skirmish, after the first has been resolved.
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(9, scn.GetStrength(southron));
		assertEquals(10, scn.GetStrength(marksman));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(legolas));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(6, scn.GetStrength(southron));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnersOtherArcheryActionsAreWithheldForTheRestOfThePhaseAndTheOtherPlayersAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var crossbowman = scn.GetShadowCard("crossbowman");
		var cloud = scn.GetShadowCard("cloud");

		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(cloud);
		scn.StartGame();
		scn.MoveMinionsToTable(crossbowman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Before the modifier is used the owner has a card's Archery ability and an Archery event to hand.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mod));
		assertTrue(scn.ShadowActionAvailable(crossbowman));
		assertTrue(scn.ShadowPlayAvailable(cloud));
		assertEquals(6, scn.GetStrength(crossbowman));

		scn.ShadowUseCardAction(mod);
		assertEquals(9, scn.GetStrength(crossbowman));

		// "You": the other player's Archery ability is still his to use.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsPass();

		// The Crossbowman could still exert, for his own ability or for the event.
		assertEquals(0, scn.GetWoundsOn(crossbowman));
		assertFalse(scn.ShadowActionAvailable(mod));
		assertFalse(scn.ShadowActionAvailable(crossbowman));
		assertFalse(scn.ShadowPlayAvailable(cloud));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ACompanionWhoBecomesAnArcherAfterTheActionIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));

		scn.FreepsUseCardAction(mod);
		assertEquals(9, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));

		// Aragorn is given his bow only now: the action reached the archers there were when it was used.
		scn.AttachCardsTo(aragorn, bow);
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void InASecondArcheryPhaseOfTheTurnTheArchersCountAgainAndArcheryActionsAreOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, bow);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas is an archer in print and Aragorn by his bow.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(mod);
		assertEquals(9, scn.GetStrength(legolas));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(bow));

		// Nobody is assigned; the fellowship moves again and comes to a second archery phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetMoveCount());
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(mod));
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsActionAvailable(bow));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
