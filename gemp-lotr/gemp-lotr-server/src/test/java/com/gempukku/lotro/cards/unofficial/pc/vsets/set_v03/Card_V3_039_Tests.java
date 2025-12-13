package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury1", "103_39");
					put("fury2", "103_39");
					put("fury3", "103_39");
					put("uruk1", "1_143"); // Troop - Strength 9
					put("uruk2", "1_143");
					put("runner", "1_178");

					put("aragorn", "1_89"); // Strength 8
					put("blade", "1_94"); // Blade of Gondor - FP possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavageFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Savage Fury
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time your [isengard] minion wins a skirmish, you may discard another Savage Fury to hinder each Free Peoples card on a companion it was skirmishing.
		* 	Maneuver: Discard a minion to make your [isengard] minion <b>fierce</b> or <b>relentless</b> until the regroup phase <i>(it participates in 1 extra round of skirmishes after fierce)</i>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury1");

		assertEquals("Savage Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void SavageFuryTwoDotUniquenessAllowsTwoCopiesButNotThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var fury3 = scn.GetShadowCard("fury3");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(fury1, fury2, fury3);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(fury1));
		scn.ShadowPlayCard(fury1);

		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fury2));
		scn.ShadowPlayCard(fury2);

		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(fury3));
	}

	@Test
	public void SavageFuryWinTriggerDiscardsAnotherFuryToHinderAttachedCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");

		scn.MoveCardsToSupportArea(fury1, fury2);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, blade);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(blade));

		// Uruk Savage (9) vs Aragorn (8) - Uruk wins
		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		//Since responding to actions is in a random order, we're not quite sure which Fury we responded to
		assertTrue((fury1.getZone() == Zone.SUPPORT && fury2.getZone() == Zone.DISCARD)
				|| (fury1.getZone() == Zone.DISCARD && fury2.getZone() == Zone.SUPPORT));

		assertTrue(scn.IsHindered(blade));
	}

	@Test
	public void SavageFuryWinTriggerNotAvailableWithoutAnotherFury() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");

		scn.MoveCardsToSupportArea(fury1);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, blade);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(blade));
	}

	@Test
	public void SavageFuryWinTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");

		scn.MoveCardsToSupportArea(fury1, fury2);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, blade);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, fury2);
		assertFalse(scn.IsHindered(blade));
	}

	@Test
	public void SavageFuryManeuverAbilityMakesMinionFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(fury1);
		scn.MoveMinionsToTable(uruk1, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(uruk1, Keyword.FIERCE));

		scn.FreepsPass();

		scn.ShadowUseCardAction(fury1);
		scn.ShadowChooseCard(runner);

		scn.ShadowChoose("Fierce");

		assertTrue(scn.HasKeyword(uruk1, Keyword.FIERCE));
	}

	@Test
	public void SavageFuryManeuverAbilityMakesMinionRelentless() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(fury1);
		scn.MoveMinionsToTable(uruk1, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(uruk1, Keyword.RELENTLESS));

		scn.FreepsPass();

		scn.ShadowUseCardAction(fury1);
		scn.ShadowChooseCard(runner);

		scn.ShadowChoose("Relentless");

		assertTrue(scn.HasKeyword(uruk1, Keyword.RELENTLESS));
	}

	@Test
	public void SavageFuryKeywordsWearOffAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(fury1);
		scn.MoveMinionsToTable(uruk1, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(fury1);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Fierce");

		assertTrue(scn.HasKeyword(uruk1, Keyword.FIERCE));

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.HasKeyword(uruk1, Keyword.FIERCE));
	}
}
