package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_240_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "51_240");
					put("band2", "51_240");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, strength 9, vitality 3; strength +2 only while bearing a weapon
					put("seal", "0_8004");      // Test Seal: Free Peoples condition, "Burdens cannot be added."
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only ever cards in the Free Peoples player's hand
					put("filler2", "1_177");
					put("filler3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Band of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each time this minion wins a skirmish, you may remove (2) to add 2 burdens.
		 * The Free Peoples player may discard 2 cards at random from hand to prevent this.
		 */

		var scn = GetScenario();

		var band = scn.GetFreepsCard("band");

		assertEquals("Band of the Eye", band.getBlueprint().getTitle());
		assertNull(band.getBlueprint().getSubtitle());
		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.SAURON, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.ORC, band.getBlueprint().getRace());
		assertEquals(4, band.getBlueprint().getTwilightCost());
		assertEquals(12, band.getBlueprint().getStrength());
		assertEquals(3, band.getBlueprint().getVitality());
		assertEquals(6, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForFourPlusTwoRoamingAtSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		scn.MoveCardsToHand(band);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion (Frodo) + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(band));

		scn.ShadowPlayCard(band);
		// 4 to play + 2 roaming: site 6 is ahead of site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WinningASkirmishOffersToRemoveTwoToAddTwoBurdensAndTheFreePeoplesPlayerMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Strength 12 against 8: the minion wins.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTwoCardsAtRandomToPreventTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.ShadowChooseOptionalTrigger(band);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The 2 cards go at random: nothing is put to the Free Peoples player, and the game goes on to regroup.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetBurdens());
		// The (2) was the Shadow player's cost and stays paid.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheFreePeoplesPlayerCannotPreventAndTheBurdensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowChooseOptionalTrigger(band);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineAndNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThanTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		// Strength 8 + 5 = 13 against 12: the minion is in the skirmish and loses it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		assertEquals(0, scn.GetWoundsOn(band));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheCopyThatWonOffersAndAnotherOrcWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var band = scn.GetShadowCard("band");
		var band2 = scn.GetShadowCard("band2");
		var swordsman = scn.GetShadowCard("swordsman");

		scn.StartGame();
		scn.MoveMinionsToTable(band, band2, swordsman);
		scn.FreepsPass();
		scn.SetTwilight(4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, band}, new PhysicalCardImpl[]{guard, swordsman});
		// The second copy is left out of the skirmishes.
		scn.ShadowDeclineAssignments();

		// Orc Swordsman, strength 9, overwhelms Dwarf Guard, strength 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band2));
		scn.ShadowChooseOptionalTrigger(band);

		// The Free Peoples player has no cards in hand, so is not asked.
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeItWinsItOffersAgainInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		// Fierce, so that the same minion wins two skirmishes in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, band, null, Keyword.FIERCE));
		scn.FreepsPass();
		scn.SetTwilight(5);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBurdensCannotBeAddedTheFreePeoplesPlayerIsNotAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("seal"));
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The trigger has a cost, so it is offered though its effect will do nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
