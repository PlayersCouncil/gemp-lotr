package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_059_Tests
{

// ----------------------------------------
// STRENGTH OF MEN TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "103_59");    // Strength of Men
					put("legion", "4_218");       // Desert Legion - str 13 [Raider]
					put("southron1", "4_222");    // Desert Warrior [Raider]
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("southron4", "4_222");
					put("southron5", "4_222");
					put("helmsman1", "103_46");   // Corsair Helmsman [Raider]
					put("helmsman2", "103_46");
					put("ships", "8_65");         // Ships of Great Draught [Raider]
					put("battlelines", "103_58"); // Shifting Battle-lines [Raider]
					put("orc", "1_271");          // Orc Soldier - not Raider

					put("aragorn", "1_89");
					put("moria1", "1_21");        // Lord of Moria - FP condition
					put("moria2", "1_21");
					put("moria3", "1_21");
					put("moria4", "1_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Strength of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a [raider] minion wins a skirmish, hinder 2 Free Peoples conditions (or all Free Peoples conditions if you can spot 5 burdens or 10 [raider] cards).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("Strength of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void StrengthOfMenHindersTwoConditionsWhenRaiderWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var legion = scn.GetShadowCard("legion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var moria1 = scn.GetFreepsCard("moria1");
		var moria2 = scn.GetFreepsCard("moria2");
		var moria3 = scn.GetFreepsCard("moria3");
		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(legion);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(moria1, moria2, moria3);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(aragorn);

		// Legion (13) vs Aragorn (8) - Legion wins
		scn.PassCurrentPhaseActions();

		// Response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose 2 conditions to hinder
		scn.ShadowChooseCards(moria1, moria2);

		assertTrue(scn.IsHindered(moria1));
		assertTrue(scn.IsHindered(moria2));
		assertFalse(scn.IsHindered(moria3));
	}

	@Test
	public void StrengthOfMenDoesNotTriggerForNonRaiderMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var orc = scn.GetShadowCard("orc");
		var moria1 = scn.GetFreepsCard("moria1");
		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToSupportArea(moria1);
		// Pump orc to ensure it wins
		scn.AddWoundsToChar(scn.GetRingBearer(), 3); // Frodo at 4 vit, 3 wounds = overwhelmed

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(scn.GetRingBearer(), orc);
		scn.FreepsResolveSkirmish(scn.GetRingBearer());

		scn.PassCurrentPhaseActions();

		// Orc won but is not Raider - no response
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void StrengthOfMenHindersAllConditionsWithFiveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var legion = scn.GetShadowCard("legion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var moria1 = scn.GetFreepsCard("moria1");
		var moria2 = scn.GetFreepsCard("moria2");
		var moria3 = scn.GetFreepsCard("moria3");
		var moria4 = scn.GetFreepsCard("moria4");
		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(legion);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(moria1, moria2, moria3, moria4);
		scn.AddBurdens(5);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		scn.ShadowAcceptOptionalTrigger();

		// With 5 burdens, ALL conditions hindered automatically
		assertTrue(scn.IsHindered(moria1));
		assertTrue(scn.IsHindered(moria2));
		assertTrue(scn.IsHindered(moria3));
		assertTrue(scn.IsHindered(moria4));
	}

	@Test
	public void StrengthOfMenHindersAllConditionsWithTenRaiderCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var legion = scn.GetShadowCard("legion");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var southron5 = scn.GetShadowCard("southron5");
		var helmsman1 = scn.GetShadowCard("helmsman1");
		var helmsman2 = scn.GetShadowCard("helmsman2");
		var ships = scn.GetShadowCard("ships");
		var battlelines = scn.GetShadowCard("battlelines");
		var aragorn = scn.GetFreepsCard("aragorn");
		var moria1 = scn.GetFreepsCard("moria1");
		var moria2 = scn.GetFreepsCard("moria2");
		var moria3 = scn.GetFreepsCard("moria3");
		var moria4 = scn.GetFreepsCard("moria4");
		scn.MoveCardsToHand(strength);
		// 10 Raider cards: legion + 5 southrons + 2 helmsmen + ships + battlelines
		scn.MoveMinionsToTable(legion, southron1, southron2, southron3, southron4, southron5, helmsman1, helmsman2);
		scn.MoveCardsToSupportArea(ships, battlelines);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(moria1, moria2, moria3, moria4);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineAssignments();
		scn.ShadowDeclineOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(aragorn);

		scn.ShadowDeclineOptionalTrigger(); //shifting battle-lines
		scn.PassCurrentPhaseActions();

		scn.ShadowAcceptOptionalTrigger();

		// With 10 Raider cards, ALL conditions hindered automatically
		assertTrue(scn.IsHindered(moria1));
		assertTrue(scn.IsHindered(moria2));
		assertTrue(scn.IsHindered(moria3));
		assertTrue(scn.IsHindered(moria4));
	}
}
