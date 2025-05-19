package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "102_37");
					put("runner", "1_178");
					put("balrog", "102_38");

					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Ancient Evil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [moria] minion wins a skirmish, you may stack it here.
		* 	Shadow: Play a unique minion stacked here, it is twilight cost -1 for each other card stacked here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil");

		assertEquals("Ancient Evil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AncientEvilStacksWinningMoriaCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var runner = scn.GetShadowCard("runner");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(evil);
		scn.MoveMinionsToTable(runner, balrog);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, balrog.getZone());
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.STACKED, runner.getZone());
		assertEquals(evil, runner.getStackedOn());
		assertEquals(Zone.SHADOW_CHARACTERS, balrog.getZone());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(sam, balrog);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.STACKED, balrog.getZone());
		assertEquals(evil, balrog.getStackedOn());
	}

	@Test
	public void AncientEvilPlaysStackedUniqueMoriaMinionsWithDiscountThenSelfDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var runner = scn.GetShadowCard("runner");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(evil);
		scn.StackCardsOn(evil, runner, balrog);

		scn.StartGame();
		scn.SetTwilight(17);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.STACKED, runner.getZone());
		assertEquals(evil, runner.getStackedOn());
		assertEquals(Zone.STACKED, balrog.getZone());
		assertEquals(evil, balrog.getStackedOn());
		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(evil));

		scn.ShadowUseCardAction(evil);
		assertEquals(Zone.SHADOW_CHARACTERS, balrog.getZone());
		assertEquals(Zone.DISCARD, evil.getZone());
		assertEquals(Zone.DISCARD, runner.getZone());
		//20 to start, -14 for balrog, -2 for roaming, +1 for discount of 1 stacked card besides balrog
		assertEquals(5, scn.GetTwilight());
	}
}
