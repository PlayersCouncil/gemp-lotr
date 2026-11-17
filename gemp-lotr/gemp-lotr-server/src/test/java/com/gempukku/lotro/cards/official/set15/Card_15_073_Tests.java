package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bold", "15_73");
					put("bold2", "15_73");       // a second copy: the Easterling is not unique

					// Elder of Dunland: [men] Man, cost 4, site 4 (so +2 roaming at site 2), strength 10, vitality 3;
					// damage +1 at a plains site.
					put("elder", "11_77");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, cost 1, site 2; a maneuver and
					// a regroup ability, each used only on purpose.
					put("ted", "18_75");
					// Countless Companies: a [men] condition, cost 0; its texts answer only [men] possessions.
					put("companies", "12_58");
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man, cost 2, site 3, no text
					// Harad Standard-bearer: [men] Man, cost 4, site 4; a skirmish ability used only on purpose.  Only the
					// Free Peoples player's copy is laid, in his own discard pile.
					put("standard", "11_84");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoldEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Bold Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> If this minion is skirmishing, discard this minion to play a [men] minion from your discard pile. That minion is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");

		assertEquals("Bold Easterling", bold.getBlueprint().getTitle());
		assertNull(bold.getBlueprint().getSubtitle());
		assertFalse(bold.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, bold.getBlueprint().getSide());
		assertEquals(Culture.MEN, bold.getBlueprint().getCulture());
		assertEquals(CardType.MINION, bold.getBlueprint().getCardType());
		assertEquals(Race.MAN, bold.getBlueprint().getRace());
		assertEquals(2, bold.getBlueprint().getTwilightCost());
		assertEquals(8, bold.getBlueprint().getStrength());
		assertEquals(1, bold.getBlueprint().getVitality());
		assertEquals(4, bold.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishingItDiscardsItselfToPlayAMenMinionFromYourDiscardPileWhichIsFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var bold = scn.GetShadowCard("bold");
		var bold2 = scn.GetShadowCard("bold2");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var companies = scn.GetShadowCard("companies");
		var brigand = scn.GetShadowCard("brigand");
		var standard = scn.GetFreepsCard("standard");

		scn.StartGame();
		scn.MoveCardsToDiscard(elder, ted, companies, brigand, standard);
		assertInDiscard(elder, ted, companies, brigand, standard);
		// To Ettenmoors, site 2.
		scn.FreepsPass();
		scn.MoveMinionsToTable(bold, bold2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, bold},
				new PhysicalCardImpl[]{gimli, bold2});
		scn.SetTwilight(20);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, bold));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// The copy skirmishing is offered its ability; the copy assigned to a skirmish still to come is not.
		assertTrue(scn.ShadowActionAvailable(bold));
		assertFalse(scn.ShadowActionAvailable(bold2));

		scn.ShadowUseCardAction(bold);
		assertInDiscard(bold);
		// [men] minions of two races are offered; a [men] condition, a [dunland] Man and the other player's [men]
		// Man in his own discard pile are not, though the pool could pay for each.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elder, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(companies, brigand, standard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(elder);

		assertInZone(Zone.SHADOW_CHARACTERS, elder);
		assertInDiscard(ted, companies, brigand, standard);
		assertFalse(scn.IsCharAssigned(elder));
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		// The other [men] minion in play is not made fierce.
		assertFalse(scn.HasKeyword(bold2, Keyword.FIERCE));

		// Aragorn's skirmish is over, its only minion gone.  The second skirmish: the Elder is fierce still.
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, bold2));
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		scn.PassSkirmishActions();

		// The fierce round: the Elder is fierce at its assignment and in its skirmish.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, elder);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, elder));
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		scn.PassSkirmishActions();

		// At the regroup phase it is fierce no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, elder);
		assertFalse(scn.HasKeyword(elder, Keyword.FIERCE));
	}

	@Test
	public void NotOfferedWithNoMenMinionInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bold = scn.GetShadowCard("bold");
		var companies = scn.GetShadowCard("companies");
		var brigand = scn.GetShadowCard("brigand");
		var standard = scn.GetFreepsCard("standard");

		scn.StartGame();
		scn.MoveCardsToDiscard(companies, brigand, standard);
		assertInDiscard(companies, brigand, standard);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bold);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, bold);
		scn.SetTwilight(20);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, bold));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bold));
		assertInZone(Zone.SHADOW_CHARACTERS, bold);
	}

	@Test
	public void ThePoolMustPayForTheMenMinionWithItsRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bold = scn.GetShadowCard("bold");
		var elder = scn.GetShadowCard("elder");

		scn.StartGame();
		scn.MoveCardsToDiscard(elder);
		assertInDiscard(elder);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bold);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, bold);
		// One short of 4 to play the Elder + 2 for roaming at site 2.
		scn.SetTwilight(5);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, bold));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(bold));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bold));

		assertEquals(6, scn.GetTwilight());
		assertInDiscard(elder);
		assertInZone(Zone.SHADOW_CHARACTERS, bold);
	}

	@Test
	public void TheEasterlingDiscardedForTheCostIsNotTheMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bold = scn.GetShadowCard("bold");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveCardsToDiscard(elder, ted);
		assertInDiscard(elder, ted);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bold);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, bold);
		scn.SetTwilight(20);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(bold));
		scn.ShadowUseCardAction(bold);
		assertInDiscard(bold);
		// No other Bold Easterling is in any pile, so a choice listing his blueprint lists him.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elder, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bold));
		scn.ShadowChooseCardBPFromSelection(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertInDiscard(bold, elder);
		// Aragorn's skirmish, the only one, is over; Ted, fierce, comes to the fierce round.
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
