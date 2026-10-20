package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("robber", "4_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRobberScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("robber", "4_16");
					put("robber2", "4_16");
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingRobberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Robber
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may make him strength +2 and <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("robber");

		assertEquals("Dunlending Robber", card.getBlueprint().getTitle());
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
	public void WinningASkirmishOffersStrengthPlus2AndFierceThroughTheFierceSkirmishAndBothEndAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRobberScenario();

		var robber = scn.GetShadowCard("robber");
		var robber2 = scn.GetShadowCard("robber2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(robber, robber2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, robber);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(robber));
		assertFalse(scn.HasKeyword(robber, Keyword.FIERCE));

		// 9 against 6: the Robber wins, short of overwhelming him (12).
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(robber));
		// "This minion": the other Robber won nothing and offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(robber2));
		assertFalse(scn.InFierceRound());

		scn.ShadowAcceptOptionalTrigger();
		// 9 + 2
		assertEquals(11, scn.GetStrength(robber));
		assertTrue(scn.HasKeyword(robber, Keyword.FIERCE));
		// "Him": the other Robber is as printed.
		assertEquals(9, scn.GetStrength(robber2));
		assertFalse(scn.HasKeyword(robber2, Keyword.FIERCE));

		// Fierce, he is defended against a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(robber));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(robber2));
		scn.FreepsAssignToMinions(gimli, robber);

		// The fierce skirmish is the last thing before the regroup phase: both halves hold through it.  11 against 6
		// still falls short of overwhelming him, and Gimli lives with two wounds.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(robber));
		assertTrue(scn.HasKeyword(robber, Keyword.FIERCE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// He has won a second time, fierce already, and is asked again.  Declined, so that what is read in the
		// regroup phase is the end of the first win's bonus.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(robber));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(robber));
		assertFalse(scn.HasKeyword(robber, Keyword.FIERCE));
	}

	@Test
	public void DecliningLeavesHimAsPrintedAndNoFierceRoundFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRobberScenario();

		var robber = scn.GetShadowCard("robber");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(robber);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, robber);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(robber));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(9, scn.GetStrength(robber));
		assertFalse(scn.HasKeyword(robber, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertInZone(Zone.SHADOW_CHARACTERS, robber);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRobberScenario();

		var robber = scn.GetShadowCard("robber");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(robber);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 10 beats the Robber's 9 without overwhelming him (18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, robber);
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(robber));
		assertInZone(Zone.SUPPORT, hides);

		scn.PassCurrentPhaseActions();
		// Hides saves the Robber from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(robber));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, robber);
		assertEquals(0, scn.GetWoundsOn(robber));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(robber));
		assertEquals(9, scn.GetStrength(robber));
		assertFalse(scn.HasKeyword(robber, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
