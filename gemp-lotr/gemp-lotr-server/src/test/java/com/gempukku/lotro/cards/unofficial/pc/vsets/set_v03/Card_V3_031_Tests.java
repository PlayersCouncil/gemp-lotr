package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_031_Tests
{

// ----------------------------------------
// PIPPIN, TROLLSLAYER TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");      // Pippin, Trollslayer
					put("aragorn", "1_89");       // Aragorn - [Gondor] companion for play requirement

					put("cavetroll", "1_165");    // Cave Troll of Moria - Damage +1, Fierce
					put("blademaster", "7_142");  // Easterling Blademaster - Easterling keyword, can add Damage +1
					put("runner", "1_178");       // Goblin Runner - no keywords (for negative test)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pippin, Trollslayer
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Knight. Enduring. To play, spot a [gondor] companion.
		* 	Skirmish: Exert Pippin twice to make a minion he is skirmishing lose all keywords until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Trollslayer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}



	@Test
	public void PippinTrollslayerRequiresGondorCompanionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(pippin, aragorn);

		scn.StartGame();

		// No [Gondor] companion in play - can't play Pippin
		assertFalse(scn.FreepsPlayAvailable(pippin));

		// Play Aragorn (Gondor companion)
		scn.FreepsPlayCard(aragorn);

		// Now Pippin is playable
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
	}

	@Test
	public void PippinTrollslayerSkirmishAbilityRemovesAllKeywordsFromMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cavetroll = scn.GetShadowCard("cavetroll");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveMinionsToTable(cavetroll);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, cavetroll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);

		// Verify Cave Troll has keywords before ability
		assertTrue(scn.HasKeyword(cavetroll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cavetroll, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(cavetroll, Keyword.FIERCE));

		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		// Cave Troll auto-selected as only minion in skirmish

		// Verify cost paid
		assertEquals(2, scn.GetWoundsOn(pippin));

		// Verify keywords removed
		assertFalse(scn.HasKeyword(cavetroll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(cavetroll, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(cavetroll, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		// Keywords should be restored
		assertTrue(scn.HasKeyword(cavetroll, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(cavetroll, Keyword.DAMAGE));
	}

	@Test
	public void PippinTrollslayerAbilityPreventsKeywordsFromBeingAddedUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blademaster = scn.GetShadowCard("blademaster");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveMinionsToTable(blademaster);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, blademaster);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);

		// Verify Blademaster has Easterling keyword initially
		assertTrue(scn.HasKeyword(blademaster, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(blademaster, Keyword.DAMAGE));

		// Pippin uses ability to remove all keywords
		scn.FreepsUseCardAction(pippin);

		// Easterling keyword should be gone
		assertFalse(scn.HasKeyword(blademaster, Keyword.EASTERLING));

		// Blademaster tries to add Damage +1 to himself
		assertTrue(scn.ShadowActionAvailable(blademaster));
		scn.ShadowUseCardAction(blademaster);

		// The continuous modifier should prevent the keyword from sticking
		assertFalse(scn.HasKeyword(blademaster, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
	}

	@Test
	public void PippinTrollslayerAbilityRequires2VitalityToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cavetroll = scn.GetShadowCard("cavetroll");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveMinionsToTable(cavetroll);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, cavetroll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);

		// Wound Pippin until he has only 1 vitality (can't exert twice)
		int pippinVit = scn.GetVitality(pippin);
		scn.AddWoundsToChar(pippin, pippinVit - 1);
		assertEquals(1, scn.GetVitality(pippin));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// Pippin can't exert twice with only 1 vitality
		assertFalse(scn.FreepsActionAvailable(pippin));
	}

}
