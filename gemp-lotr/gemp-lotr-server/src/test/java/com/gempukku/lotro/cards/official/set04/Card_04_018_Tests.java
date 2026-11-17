package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "4_18");
					put("warrior2", "4_18");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, valiant only
					// Guma, Plains Farmer: [rohan] Man ally, strength 4, vitality 2, home 4T (never at home on this path).
					// His fellowship ability is used only on purpose.
					put("guma", "4_277");
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally, strength 4, vitality 2, home 3F.  His maneuver ability
					// is used only on purpose.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: <b>Assignment:</b> Spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Dunlending Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilitySpotsAnAllyMakesHimTakePartForThePhaseAndAssignsThisMinionToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var guma = scn.GetFreepsCard("guma");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(guma, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Ettenmoors, site 2: neither ally is at home.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowCanAssign(guma));
		assertFalse(scn.ShadowCanAssign(thrarin));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior2));
		scn.ShadowUseCardAction(warrior);

		// Allies of two cultures and races; the Ring-bearer is a companion and no ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guma, thrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		// The text says neither "may" nor "up to": the ally must be chosen.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guma);

		assertTrue(scn.IsCharAssignedAgainst(guma, warrior));
		// Guma now takes part in skirmishes; Thrarin, not chosen, does not, and the other Warrior is not assigned.
		assertTrue(scn.ShadowCanAssign(guma));
		assertFalse(scn.ShadowCanAssign(thrarin));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertFalse(scn.IsCharAssigned(warrior2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// This Warrior is assigned already and could be assigned to nobody, though Thrarin is free; the other
		// Warrior's ability is still its own.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior2));

		// Guma is assigned already: Thrarin is the one ally left to take, without a choice.
		scn.ShadowUseCardAction(warrior2);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior2));
		assertTrue(scn.IsCharAssignedAgainst(guma, warrior));
		assertFalse(scn.IsCharAssignedAgainst(guma, warrior2));

		// Every minion is assigned: the Free Peoples player has nobody to assign and is asked for the first skirmish.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The participation had no stated duration and ended with the assignment phase; the skirmish it was
		// assigned to is fought all the same.
		scn.FreepsResolveSkirmish(guma);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(guma, warrior));
		assertFalse(scn.ShadowCanAssign(guma));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsOfferedOnEitherAllyAloneAsWellAsOnTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var guma = scn.GetFreepsCard("guma");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(guma, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(guma));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		// The other way round from the first test: the Dwarf is taken first, so that the second Warrior's offer
		// rests on the [rohan] Man alone.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guma, thrarin));
		scn.ShadowChooseCard(thrarin);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior));
		assertFalse(scn.IsCharAssigned(guma));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior2));
		scn.ShadowUseCardAction(warrior2);
		assertTrue(scn.IsCharAssignedAgainst(guma, warrior2));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Companions to fight, and no ally to spot.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertFalse(scn.IsCharAssigned(warrior));
	}
}
