package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_055_Tests
{

// ----------------------------------------
// RAIDING SCHOONER TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("schooner1", "103_55");   // Raiding Schooner
					put("schooner2", "103_55");   // Second copy
					put("helmsman", "103_46");    // Corsair Helmsman - Corsair minion
					put("ships", "8_65");         // Ships of Great Draught - token holder
					put("runner", "1_178");

					put("aragorn", "1_89");
					put("anduril", "7_79");       // FP artifact
					put("athelas", "1_94");       // FP possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RaidingSchoonerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Raiding Schooner
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: When you play this, spot a Corsair to add a [raider] token here.
		* 	Shadow: Remove 2 [raider] tokens from your cards to hinder a Free Peoples possession.
		* 	Maneuver: Spot a Corsair, hinder another Raiding Schooner, and remove 3 [raider] tokens from your cards to hinder a Free Peoples artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("schooner1");

		assertEquals("Raiding Schooner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void RaidingSchoonerAddsTokenWhenPlayedSpottingCorsair() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner = scn.GetShadowCard("schooner1");
		var helmsman = scn.GetShadowCard("helmsman");
		scn.MoveCardsToHand(schooner);
		scn.MoveMinionsToTable(helmsman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(schooner);

		// Should have 1 token from play trigger
		assertEquals(1, scn.GetCultureTokensOn(schooner));
	}

	@Test
	public void RaidingSchoonerDoesNotAddTokenWithoutCorsair() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner = scn.GetShadowCard("schooner1");
		// No Corsair on table
		scn.MoveCardsToHand(schooner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(schooner);

		// No Corsair to spot - no token
		assertEquals(0, scn.GetCultureTokensOn(schooner));
	}

	@Test
	public void RaidingSchoonerShadowAbilityHindersFPPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner = scn.GetShadowCard("schooner1");
		var ships = scn.GetShadowCard("ships");
		var helmsman = scn.GetShadowCard("helmsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCardsToSupportArea(schooner, ships);
		scn.AddTokensToCard(ships, 5); // Tokens to spend
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas, anduril);

		scn.StartGame();
		scn.SetTwilight(10);

		assertFalse(scn.IsHindered(athelas));

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(schooner));
		scn.ShadowUseCardAction(schooner);

		// Should only target possessions, not artifacts
		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(anduril));
		assertEquals(3, scn.GetCultureTokensOn(ships)); // 5 - 2 = 3
	}

	@Test
	public void RaidingSchoonerManeuverAbilityHindersFPArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var ships = scn.GetShadowCard("ships");
		var helmsman = scn.GetShadowCard("helmsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCardsToSupportArea(schooner1, schooner2, ships);
		scn.AddTokensToCard(ships, 5);
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas, anduril);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(schooner2));

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(schooner1));
		scn.ShadowUseCardAction(schooner1);

		// Hinder another Schooner as cost
		// schooner2 auto-selected as only other Schooner

		scn.ShadowDeclineOptionalTrigger(); //helmsman
		// Should only target artifacts, not possessions

		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(schooner2));
		assertEquals(2, scn.GetCultureTokensOn(ships)); // 5 - 3 = 2
	}

	@Test
	public void RaidingSchoonerManeuverAbilityRequiresAnotherSchooner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner = scn.GetShadowCard("schooner1");
		var ships = scn.GetShadowCard("ships");
		var helmsman = scn.GetShadowCard("helmsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCardsToSupportArea(schooner, ships);
		scn.AddTokensToCard(ships, 5);
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPassCurrentPhaseAction();

		// Only one Schooner - Maneuver ability not available
		// (Shadow ability would be available, but wrong phase)
		assertFalse(scn.ShadowActionAvailable(schooner));
	}

	@Test
	public void RaidingSchoonerManeuverAbilityRequiresCorsair() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var ships = scn.GetShadowCard("ships");
		// No Corsair
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCardsToSupportArea(schooner1, schooner2, ships);
		scn.AddTokensToCard(ships, 5);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveMinionsToTable("runner");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPassCurrentPhaseAction();

		// No Corsair to spot - Maneuver ability not available
		assertFalse(scn.ShadowActionAvailable(schooner1));
	}
}
