package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machinery", "102_28");
					put("saruman", "3_69");
					put("isenorc", "3_62");

					put("gandalf", "1_72");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MachineryofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Machinery of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, spot a Wizard to add 2 [isengard] tokens here.
		* 	Each time an [isengard] Orc loses a skirmish, add an [isengard] token here.
		* 	Skirmish: Discard this condition (or remove 2 [isengard] tokens here) to cancel a skirmish involving an [isengard] Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("machinery");

		assertEquals("Machinery of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MachineryofWarStartsWithNoTokensByDefault() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		scn.MoveCardsToHand(machinery);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(machinery));

		scn.ShadowPlayCard(machinery);
		assertEquals(0, scn.GetCultureTokensOn(machinery));
	}

	@Test
	public void MachineryofWarCanSpotSarumanToAdd2TokensToItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCardsToHand(machinery);
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.SHADOW_CHARACTERS, saruman.getZone());
		assertTrue(scn.ShadowPlayAvailable(machinery));

		scn.ShadowPlayCard(machinery);
		assertEquals(2, scn.GetCultureTokensOn(machinery));
	}

	@Test
	public void MachineryofWarCanSpotGandalfToAdd2TokensToItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		scn.MoveCardsToHand(machinery);

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(gandalf);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.FREE_CHARACTERS, gandalf.getZone());
		assertTrue(scn.ShadowPlayAvailable(machinery));

		scn.ShadowPlayCard(machinery);
		assertEquals(2, scn.GetCultureTokensOn(machinery));
	}

	@Test
	public void MachineryofWarAddsTokenToItselfWhenIsenorcLosesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		var isenorc = scn.GetShadowCard("isenorc");
		scn.MoveCardsToSupportArea(machinery);
		scn.MoveMinionsToTable(isenorc);

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(gandalf);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, isenorc);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishing(isenorc));
		assertEquals(0, scn.GetWoundsOn(isenorc));
		assertEquals(0, scn.GetCultureTokensOn(machinery));
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		assertFalse(scn.IsCharSkirmishing(isenorc));
		assertEquals(1, scn.GetWoundsOn(isenorc));
		assertEquals(1, scn.GetCultureTokensOn(machinery));
	}

	@Test
	public void MachineryofWarRemoves2TokensToCancelIsenorcSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		var isenorc = scn.GetShadowCard("isenorc");
		scn.MoveCardsToSupportArea(machinery);
		scn.MoveMinionsToTable(isenorc);

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(gandalf);

		scn.AddTokensToCard(machinery, 2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, isenorc);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishing(isenorc));
		assertEquals(0, scn.GetWoundsOn(isenorc));
		assertEquals(2, scn.GetCultureTokensOn(machinery));

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(machinery));
		scn.ShadowUseCardAction(machinery);

		assertFalse(scn.IsCharSkirmishing(isenorc));
		assertEquals(0, scn.GetWoundsOn(isenorc));
		assertEquals(0, scn.GetCultureTokensOn(machinery));
	}

	@Test
	public void MachineryofWarSelfDiscardsToCancelIsenorcSkirmishIfNotEnoughCultureTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery");
		var isenorc = scn.GetShadowCard("isenorc");
		scn.MoveCardsToSupportArea(machinery);
		scn.MoveMinionsToTable(isenorc);

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionToTable(gandalf);

		scn.AddTokensToCard(machinery, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, isenorc);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishing(isenorc));
		assertEquals(0, scn.GetWoundsOn(isenorc));
		assertEquals(1, scn.GetCultureTokensOn(machinery));
		assertEquals(Zone.SUPPORT, machinery.getZone());

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(machinery));
		scn.ShadowUseCardAction(machinery);

		assertFalse(scn.IsCharSkirmishing(isenorc));
		assertEquals(0, scn.GetWoundsOn(isenorc));
		assertEquals(Zone.DISCARD, machinery.getZone());
	}
}
