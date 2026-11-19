package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_026_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow and
	// assignment phases of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twarg1", "31_26");
					put("twarg2", "31_26");

					put("thorin", "30_19");     // Thorin, Oakenshield: the one card titled Thorin, [dwarven] companion
					put("thorin3", "17_6");     // Thorin III, Stonehelm: a [dwarven] companion of another title; a loss trigger
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used

					// [gundabad] Orcs, vitality 2.  The Aggressor's assignment ability and Yazneg's maneuver ability are not used.
					put("aggressor", "30_39");  // Orkish Aggressor
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; texts on assigning him, a regroup ability
					put("malevolent", "33_47"); // Malevolent Orc: a [sauron] Orc; an end-of-Shadow-phase offer with nothing to take
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThreateningWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Threatening Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 4
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 4
		 * Vitality: 2
		 * Game Text: Bearer must be a [gundabad] Orc.
		 * Bearer is <b>fierce</b>.
		 * <b>Assignment:</b> Exert bearer twice or remove 2 doubts to assign bearer to Thorin.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("twarg1");

		assertEquals("Threatening Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAGundabadOrcOnlyAndMakesThatBearerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg = scn.GetShadowCard("twarg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToHand(twarg);

		scn.StartGame();
		assertInZone(Zone.HAND, twarg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(aggressor, yazneg, ferny, malevolent);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		scn.ShadowPlayCard(twarg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, malevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(twarg, aggressor);
		assertTrue(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ferny, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothCostsPayableTheChoiceIsAskedAndExertingBearerTwiceAssignsHimToThorin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		scn.AddBurdens(2);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(2, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(twarg1));
		scn.ShadowUseCardAction(twarg1);
		assertEquals(List.of("Exert bearer twice", "Remove 2 doubts"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert bearer twice");
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(thorin, aggressor));
		assertFalse(scn.IsCharAssigned(yazneg));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertFalse(scn.IsCharAssigned(thorin3));
		assertFalse(scn.IsCharAssigned(balin));
		// After the Shadow player's action the Free Peoples player is asked again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithBothCostsPayableRemovingTwoDoubtsAssignsBearerToThorin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		scn.AddBurdens(2);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(2, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowUseCardAction(twarg1);
		assertEquals(List.of("Exert bearer twice", "Remove 2 doubts"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove 2 doubts");
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.IsCharAssignedAgainst(thorin, aggressor));
		assertFalse(scn.IsCharAssigned(yazneg));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertFalse(scn.IsCharAssigned(thorin3));
		assertFalse(scn.IsCharAssigned(balin));
		// After the Shadow player's action the Free Peoples player is asked again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithBearerUnableToExertTwiceTwoDoubtsAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		// Vitality 2 + 2 with 2 wounds: the Aggressor can exert once more, not twice.
		scn.AddWoundsToChar(aggressor, 2);
		scn.AddBurdens(2);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(2, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowUseCardAction(twarg1);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertTrue(scn.IsCharAssignedAgainst(thorin, aggressor));
		assertFalse(scn.IsCharAssigned(yazneg));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertFalse(scn.IsCharAssigned(thorin3));
		assertFalse(scn.IsCharAssigned(balin));
		// After the Shadow player's action the Free Peoples player is asked again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithOneDoubtBearerIsExertedTwiceUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		scn.AddBurdens(1);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(1, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowUseCardAction(twarg1);
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(thorin, aggressor));
		assertFalse(scn.IsCharAssigned(yazneg));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertFalse(scn.IsCharAssigned(thorin3));
		assertFalse(scn.IsCharAssigned(balin));
		// After the Shadow player's action the Free Peoples player is asked again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerCannotExertTwiceAndThereAreFewerThanTwoDoubts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		// Vitality 2 + 2 with 2 wounds, and one doubt: neither cost can be paid.
		scn.AddWoundsToChar(aggressor, 2);
		scn.AddBurdens(1);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(1, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(twarg1));
		// The other copy, whose bearer can exert, is offered.
		assertTrue(scn.ShadowActionAvailable(twarg2));
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WithNoThorinTheCostIsPaidAndNobodyIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twarg1 = scn.GetShadowCard("twarg1");
		var twarg2 = scn.GetShadowCard("twarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin3, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg);
		// The second copy, on Yazneg, is the other bearer: he is neither exerted nor assigned by the first.
		scn.AttachCardsTo(aggressor, twarg1);
		scn.AttachCardsTo(yazneg, twarg2);
		assertAttachedTo(twarg1, aggressor);
		assertAttachedTo(twarg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(0, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(twarg1));
		scn.ShadowUseCardAction(twarg1);
		// No doubts: the exertion is the one cost that can be paid.
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertFalse(scn.IsCharAssigned(aggressor));
		assertFalse(scn.IsCharAssigned(thorin3));
		assertFalse(scn.IsCharAssigned(balin));
		// After the Shadow player's action the Free Peoples player is asked again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
