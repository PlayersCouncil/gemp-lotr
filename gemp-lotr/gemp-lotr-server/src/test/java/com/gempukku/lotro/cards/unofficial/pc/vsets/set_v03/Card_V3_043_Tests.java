package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_043_Tests
{

// ----------------------------------------
// BLADETUSK REARGUARD TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rearguard", "103_43");   // Bladetusk Rearguard
					put("southron1", "4_222");    // Desert Warrior - Southron Man
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("orc", "1_271");          // Orc Soldier - not a Southron

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskRearguardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Rearguard
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 5
		 * Vitality: 5
		 * Game Text: Regroup: Stack a Southron Man here.
		 * 	Maneuver: Remove (5) to make this artifact a <b>fierce</b> mounted Southron minion until the end of the turn
		 * 	that is strength +3 and <b>ambush (1)</b> for each Southron stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rearguard");

		assertEquals("Bladetusk Rearguard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void BladetuskRearguardRegroupActionStacksSouthronManFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(rearguard);
		scn.MoveMinionsToTable(southron1, southron2, orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetStackedCards(rearguard).size());

		assertTrue(scn.ShadowActionAvailable(rearguard));
		scn.ShadowUseCardAction(rearguard);

		// Should only be able to stack Southron Men, not the Orc
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(southron1, southron2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(orc));

		scn.ShadowChooseCard(southron1);

		assertEquals(1, scn.GetStackedCards(rearguard).size());
		assertInZone(Zone.STACKED, southron1);

		// Can stack again (not once per phase)
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(rearguard));
		scn.ShadowUseCardAction(rearguard);

		assertEquals(2, scn.GetStackedCards(rearguard).size());
	}

	@Test
	public void BladetuskRearguardRegroupActionRequiresSouthronManInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(rearguard);
		scn.MoveMinionsToTable(orc); // Only non-Southron minion

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		// No Southron Men in play - action not available
		assertFalse(scn.ShadowActionAvailable(rearguard));
	}

	@Test
	public void BladetuskRearguardTransformationRequires5Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, southron1, southron2);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(4); // Not enough
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(rearguard));
	}

	@Test
	public void BladetuskRearguardTransformationGrantsBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, southron1, southron2);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(rearguard));
		scn.ShadowUseCardAction(rearguard);

		// Verify cost paid
		assertEquals(twilightBefore - 5, scn.GetTwilight());

		// Verify transformation
		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));

		// Base strength 5 + (2 Southrons * 3) = 11
		assertEquals(11, scn.GetStrength(rearguard));

		// Ambush (2) for 2 stacked Southrons
		assertTrue(scn.HasKeyword(rearguard, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));
	}


	@Test
	public void BladetuskRearguardIsDiscardedAsMinionDuringRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, southron1);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(rearguard);

		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToStay();

		// Rearguard and stacked cards should be discarded
		assertInDiscard(rearguard);
		assertInDiscard(southron1);
	}
}
