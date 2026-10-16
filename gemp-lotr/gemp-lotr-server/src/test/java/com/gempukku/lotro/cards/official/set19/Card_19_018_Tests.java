package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_19_018_Tests
{

	// The Fellowship sites: site 2 is Ettenmoors (plains), site 4 Balin's Tomb (underground).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "19_18");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, vitality 1; Archer is its whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally, strength 4, vitality 2, home 3F; his maneuver
					// ability is not used.
					put("thrarin", "1_27");
					// Dunlending Warrior: [dunland] Man minion, strength 9, vitality 1. "Assignment: Spot an ally to
					// make that ally participate in skirmishes and assign this minion to skirmish that ally."
					put("warrior", "4_18");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: The Balrog, Demon of Might
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: <b>Damage +2</b>. <b>Fierce</b>.<br>While at an underground site, skip the archery phase, the maneuver phase, and The Balrog cannot be assigned to skirmish companions of strength 6 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Demon of Might", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		// The sheet's culture is moria_balrog, the Balrog's own [moria] icon.
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteTheManeuverAndArcheryPhasesAreSkipped() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var balrog = scn.GetShadowCard("balrog");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		// The fellowship moves from site 3 to Balin's Tomb.
		scn.FreepsPass();
		scn.MoveMinionsToTable(balrog, marksman);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();

		// From the Shadow phase straight to the assignment phase: the archer's wound is never placed.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AwayFromAnUndergroundSiteBothPhasesArePlayedAndTheBalrogMayBeAssignedToACompanionOfStrength6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var balrog = scn.GetShadowCard("balrog");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		// The fellowship moves from site 1 to Ettenmoors, a plains.
		scn.FreepsPass();
		scn.MoveMinionsToTable(balrog, marksman);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassCurrentPhaseActions();

		// Minion archery total 1 (the Marksman), placed on Frodo.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		scn.PassAssignmentActions();

		// Gimli is strength 6, and away from an underground site the Balrog may be assigned to him.
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, balrog));

		scn.FreepsAssignToMinions(gimli, balrog);

		assertTrue(scn.IsCharAssignedAgainst(gimli, balrog));
		// The Marksman was left unassigned, and the Shadow player assigns him.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void AtAnUndergroundSiteTheBalrogCannotBeAssignedToACompanionOfStrength6OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		var balrog = scn.GetShadowCard("balrog");
		// The Warrior has no ally to use his assignment ability on; he is a second minion the bar must not reach.
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.SkipToSite(3);
		// The fellowship moves from site 3 to Balin's Tomb.
		scn.FreepsPass();
		scn.MoveMinionsToTable(balrog, warrior);
		scn.ShadowPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();

		// Frodo 3 + 1 for The Ruling Ring; Gimli 6, the bound; Boromir 7.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(balrog));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, balrog));
		assertFalse(scn.FreepsCanAssignAgainst(gimli, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(boromir, balrog));
		// The same companions of strength 6 or less may be assigned to the other minion.
		assertTrue(scn.FreepsCanAssignAgainst(frodo, warrior));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, warrior));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, balrog}, new PhysicalCardImpl[]{gimli, warrior});

		assertTrue(scn.IsCharAssignedAgainst(boromir, balrog));
		assertTrue(scn.IsCharAssignedAgainst(gimli, warrior));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AtAnUndergroundSiteTheBalrogMayBeAssignedToAnAllyOfStrength6OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);

		var balrog = scn.GetShadowCard("balrog");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.SkipToSite(3);
		// The fellowship moves from site 3 to Balin's Tomb.
		scn.FreepsPass();
		scn.MoveMinionsToTable(balrog, warrior);
		scn.ShadowPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		// The Warrior makes Thrarin, the one ally, take part in skirmishes and is assigned to him.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior));
		scn.PassAssignmentActions();

		// The Free Peoples player leaves the Balrog unassigned, and the Shadow player assigns it.
		scn.FreepsDeclineAssignments();

		// Thrarin, strength 4, is an ally and no companion: the Balrog may be assigned to him.  Gimli, a
		// companion of strength 6, is still refused.
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowCanAssignAgainst(gimli, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(thrarin, balrog));

		scn.ShadowAssignToMinions(thrarin, balrog);

		assertTrue(scn.IsCharAssignedAgainst(thrarin, balrog));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
