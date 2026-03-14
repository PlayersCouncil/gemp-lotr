package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_221_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "51_221");
					put("witchking", "1_237"); // The Witch-king, Lord of Angmar
					put("condition", "1_108"); // Albert Dreary, FP Gandalf condition
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePaleBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Pale Blade
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be The Witch-king.<br>He is <b>damage +1</b>.<br>Each time
		 * The Witch-king wins a skirmish, you may exert him to discard a Free Peoples condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("The Pale Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void ThePaleBladeGrantsDamagePlusOneAndOptionallyDiscardsConditionOnWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var condition = scn.GetFreepsCard("condition");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToSupportArea(condition);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);

		scn.StartGame();

		// Witch-king should have damage +1 from the blade
		assertTrue(scn.HasKeyword(witchking, Keyword.DAMAGE));

		// Frodo vs Witch-king -- WK wins easily
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, witchking);
		scn.PassSkirmishActions();

		// WK wins. Optional trigger should fire -- this is a Trigger, NOT a Response.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Condition should be auto-selected (only FP condition in play) and discarded
		assertInDiscard(condition);
	}

	@Test
	public void ThePaleBladeTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var condition = scn.GetFreepsCard("condition");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToSupportArea(condition);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, witchking);
		scn.PassSkirmishActions();

		// WK wins. Decline the optional trigger.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Condition should still be in support area
		assertInZone(Zone.SUPPORT, condition);
	}
}
