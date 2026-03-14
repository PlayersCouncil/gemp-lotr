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
					put("runner", "1_178");

					put("guard", "1_7"); // Dwarf Guard
					put("condition1", "1_21"); // Lord of Moria, FP condition
					put("condition2", "1_21"); // Lord of Moria, FP condition
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
	public void ThePaleBladeGrantsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCardsToHand(blade);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		scn.AttachCardsTo(witchking, blade);

		assertEquals(1, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
	}

	@Test
	public void ThePaleBladeOptionalTriggerExertsToDiscardOnlyOneConditionOnWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var condition1 = scn.GetFreepsCard("condition1");
		var condition2 = scn.GetFreepsCard("condition2");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(condition1, condition2);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);

		scn.StartGame();

		// Dwarf Guard vs Witch-king -- WK wins easily
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, witchking);
		scn.PassSkirmishActions();

		// WK wins.
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));

		assertInZone(Zone.SUPPORT, condition1);
		assertInZone(Zone.SUPPORT, condition2);
		assertTrue(scn.ShadowHasCardChoicesAvailable(condition1, condition2));

		scn.ShadowChooseCard(condition1);
		assertInDiscard(condition1);

		// Optional trigger should fire, but only once -- this is a Trigger, NOT a Response.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void ThePaleBladeTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, witchking);
		scn.PassSkirmishActions();

		// WK wins. Decline the optional trigger.
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(witchking));
	}
}
