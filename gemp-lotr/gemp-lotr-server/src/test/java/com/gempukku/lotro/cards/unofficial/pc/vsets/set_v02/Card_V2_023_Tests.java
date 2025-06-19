package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "102_23");
					put("saruman", "3_69");
					put("orc1", "3_58");
					put("orc2", "5_50");

					put("gandalf", "1_72");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresofIndustryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Fires of Industry
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, spot 2 [Isengard] Orcs or a Wizard to add 2 [isengard] tokens here.
		* 	At the start of the regroup phase, you may discard this condition (or remove an [isengard] token here), to heal an [Isengard] Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fires");

		assertEquals("Fires of Industry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FiresofIndustryCanSpot2IsenorcsToAdd2TokensToItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var saruman = scn.GetShadowCard("saruman");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToHand(fires);
		scn.MoveMinionsToTable(orc1, orc2);

		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.SHADOW_CHARACTERS, orc1.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, orc1.getZone());
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
	}

	@Test
	public void FiresofIndustryCanSpotSarumanToAdd2TokensToItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var saruman = scn.GetShadowCard("saruman");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToHand(fires);
		scn.MoveMinionsToTable(saruman);

		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.SHADOW_CHARACTERS, saruman.getZone());
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
	}

	@Test
	public void FiresofIndustryCanSpotGandalfToAdd2TokensToItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var saruman = scn.GetShadowCard("saruman");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToHand(fires);

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.FREE_CHARACTERS, gandalf.getZone());
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
	}

	@Test
	public void FiresofIndustryRemovesOneTokenToHealAnIsenorcAtStartOfRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var isenorc = scn.GetShadowCard("orc1");
		scn.MoveCardsToSupportArea(fires);
		scn.MoveMinionsToTable(isenorc);

		scn.AddWoundsToChar(isenorc,2);
		scn.AddTokensToCard(fires, 2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(isenorc));
		assertEquals(2, scn.GetCultureTokensOn(fires));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(isenorc));
		assertEquals(1, scn.GetCultureTokensOn(fires));
	}

	@Test
	public void FiresofIndustrySelfDiscardToHealAnIsenorcAtStartOfRegroupPhaseIfNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var isenorc = scn.GetShadowCard("orc1");
		scn.MoveCardsToSupportArea(fires);
		scn.MoveMinionsToTable(isenorc);

		scn.AddWoundsToChar(isenorc,2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(isenorc));
		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertEquals(Zone.SUPPORT, fires.getZone());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(isenorc));
		assertEquals(Zone.DISCARD, fires.getZone());
	}
}
