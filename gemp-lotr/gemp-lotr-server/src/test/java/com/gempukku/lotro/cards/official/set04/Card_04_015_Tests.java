package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ravager", "4_15");
					put("ravager2", "4_15");
					put("ravager3", "4_15");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5; its text answers only its own play

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, valiant only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, knight only
					// Merry, Master Holbytla: [rohan] Hobbit companion, strength 3, vitality 4.  A Man only while 3 [rohan]
					// companions or Eowyn can be spotted: beside the Guard alone he is none.  His other texts answer an
					// exertion or are used on purpose.
					put("merry", "103_85");
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
	public void DunlendingRavagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Ravager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While skirmishing a [rohan] Man, this minion is strength +2.<br><b>Assignment:</b> Spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ravager");

		assertEquals("Dunlending Ravager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSkirmishingARohanManCompanionHeIsStrength7AndNotAgainstAnotherManOrAnotherRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var ravager2 = scn.GetShadowCard("ravager2");
		var ravager3 = scn.GetShadowCard("ravager3");
		var runner = scn.GetShadowCard("runner");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(guard, knight, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager, ravager2, ravager3, runner);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetStrength(ravager));
		assertEquals(5, scn.GetStrength(ravager2));
		assertEquals(5, scn.GetStrength(ravager3));
		assertEquals(5, scn.GetStrength(runner));
		// Two [rohan] companions and no Eowyn: Merry is a Hobbit and no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, ravager},
				new PhysicalCardImpl[]{knight, ravager2},
				new PhysicalCardImpl[]{merry, ravager3});
		// The Goblin Runner is left over, and the Shadow player puts it beside the Ravager against the Guard.
		scn.ShadowAssignToMinions(guard, runner);

		// A [gondor] Man: no bonus.  And the Ravager assigned to the Guard is not skirmishing him yet.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, ravager2));
		assertEquals(5, scn.GetStrength(ravager2));
		assertTrue(scn.IsCharAssignedAgainst(guard, ravager));
		assertEquals(5, scn.GetStrength(ravager));
		scn.PassCurrentPhaseActions();

		// A [rohan] companion who is no Man: no bonus.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, ravager3));
		assertEquals(5, scn.GetStrength(ravager3));
		scn.PassCurrentPhaseActions();

		// A [rohan] Man: 5 + 2.  The Runner skirmishing the same Man is as printed.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, ravager));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, runner));
		assertEquals(7, scn.GetStrength(ravager));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignmentAbilitySpotsAnAllyMakesHimTakePartForThePhaseAndAssignsThisMinionToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var ravager2 = scn.GetShadowCard("ravager2");
		var guma = scn.GetFreepsCard("guma");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(guma, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager, ravager2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Ettenmoors, site 2: neither ally is at home.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowCanAssign(guma));
		assertFalse(scn.ShadowCanAssign(thrarin));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ravager));
		assertTrue(scn.ShadowActionAvailable(ravager2));
		scn.ShadowUseCardAction(ravager);

		// Allies of two cultures and races; the Ring-bearer is a companion and no ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guma, thrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		// The text says neither "may" nor "up to": the ally must be chosen.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guma);

		assertTrue(scn.IsCharAssignedAgainst(guma, ravager));
		// Guma now takes part in skirmishes; Thrarin, not chosen, does not, and the other Ravager is not assigned.
		assertTrue(scn.ShadowCanAssign(guma));
		assertFalse(scn.ShadowCanAssign(thrarin));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertFalse(scn.IsCharAssigned(ravager2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// This Ravager is assigned already and could be assigned to nobody, though Thrarin is free; the other
		// Ravager's ability is still its own.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravager));
		assertTrue(scn.ShadowActionAvailable(ravager2));

		// Guma is assigned already: Thrarin is the one ally left to take, without a choice.
		scn.ShadowUseCardAction(ravager2);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, ravager2));
		assertTrue(scn.IsCharAssignedAgainst(guma, ravager));
		assertFalse(scn.IsCharAssignedAgainst(guma, ravager2));

		// Every minion is assigned: the Free Peoples player has nobody to assign and is asked for the first skirmish.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A [rohan] Man ally: 5 + 2.  The participation had no stated duration and ended with the assignment
		// phase; the skirmish it was assigned to is fought all the same.
		scn.FreepsResolveSkirmish(guma);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(guma, ravager));
		assertEquals(7, scn.GetStrength(ravager));
		assertFalse(scn.ShadowCanAssign(guma));
		scn.PassCurrentPhaseActions();

		// A [dwarven] Dwarf ally: no bonus.
		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, ravager2));
		assertEquals(5, scn.GetStrength(ravager2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsOfferedOnEitherAllyAloneAsWellAsOnTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var ravager2 = scn.GetShadowCard("ravager2");
		var guma = scn.GetFreepsCard("guma");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(guma, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager, ravager2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(guma));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertTrue(scn.ShadowActionAvailable(ravager));
		scn.ShadowUseCardAction(ravager);
		// The other way round from the first test: the Dwarf is taken first, so that the second Ravager's offer
		// rests on the [rohan] Man alone.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guma, thrarin));
		scn.ShadowChooseCard(thrarin);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, ravager));
		assertFalse(scn.IsCharAssigned(guma));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ravager2));
		scn.ShadowUseCardAction(ravager2);
		assertTrue(scn.IsCharAssignedAgainst(guma, ravager2));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, ravager));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Companions to fight, and no ally to spot.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravager));
		assertFalse(scn.IsCharAssigned(ravager));
	}
}
