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

public class Card_04_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madman", "4_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMadmanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madman", "4_12");
					put("madman2", "4_12");
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
	public void DunlendingMadmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Madman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may make him strength +2 and <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madman");

		assertEquals("Dunlending Madman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersStrengthPlus2AndFierceThroughTheFierceSkirmishAndBothEndAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMadmanScenario();

		var madman = scn.GetShadowCard("madman");
		var madman2 = scn.GetShadowCard("madman2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(madman, madman2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, madman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(7, scn.GetStrength(madman));
		assertFalse(scn.HasKeyword(madman, Keyword.FIERCE));

		// 7 against 6: the Madman wins.
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
		// "This minion": the other Madman won nothing and offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(madman2));
		assertFalse(scn.InFierceRound());

		scn.ShadowAcceptOptionalTrigger();
		// 7 + 2
		assertEquals(9, scn.GetStrength(madman));
		assertTrue(scn.HasKeyword(madman, Keyword.FIERCE));
		// "Him": the other Madman is as printed.
		assertEquals(7, scn.GetStrength(madman2));
		assertFalse(scn.HasKeyword(madman2, Keyword.FIERCE));

		// Fierce, he is defended against a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(madman));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(madman2));
		scn.FreepsAssignToMinions(gimli, madman);

		// The fierce skirmish is the last thing before the regroup phase: both halves hold through it.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(9, scn.GetStrength(madman));
		assertTrue(scn.HasKeyword(madman, Keyword.FIERCE));
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(gimli));

		// He has won a second time, fierce already, and is asked again.  Declined, so that what is read in the
		// regroup phase is the end of the first win's bonus.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(madman));
		assertFalse(scn.HasKeyword(madman, Keyword.FIERCE));
	}

	@Test
	public void DecliningLeavesHimAsPrintedAndNoFierceRoundFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMadmanScenario();

		var madman = scn.GetShadowCard("madman");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(madman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, madman);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(madman));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(7, scn.GetStrength(madman));
		assertFalse(scn.HasKeyword(madman, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertInZone(Zone.SHADOW_CHARACTERS, madman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMadmanScenario();

		var madman = scn.GetShadowCard("madman");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(madman);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 8 beats the Madman's 7 without overwhelming him (14).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, madman);
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(madman));
		assertInZone(Zone.SUPPORT, hides);

		scn.PassCurrentPhaseActions();
		// Hides saves the Madman from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(madman));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, madman);
		assertEquals(0, scn.GetWoundsOn(madman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(madman));
		assertEquals(7, scn.GetStrength(madman));
		assertFalse(scn.HasKeyword(madman, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
