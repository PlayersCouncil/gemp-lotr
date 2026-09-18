package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_41");
					put("gimli", "1_13");
					put("lordOfMoria", "1_21");   // Dwarven support condition (each player gets a copy)
					put("toby", "1_305");         // filler to stack (each player gets a copy)
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Axe of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Gimli.<br>He is <b>damage +1</b>.<br><b>Skirmish:</b> Discard a [dwarven] condition or a card stacked on a [dwarven] condition to make Gimli strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Axe of Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Ignore
	@Test
	public void SkirmishAbilityCanDiscardACardStackedOnEitherPlayersDwarvenCondition() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1001: cards stacked on the opponent's [dwarven] condition were never offered, because the
		// stacked-card chooser only looked at hosts that are currently active. Stacked cards are inactive regardless
		// of their host, so the host's activity is not the right gate.
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var myCondition = scn.GetFreepsCard("lordOfMoria");
		var myStacked = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(myCondition);
		scn.StackCardsOn(myCondition, myStacked);

		var theirCondition = scn.GetShadowCard("lordOfMoria");
		var theirStacked = scn.GetShadowCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(theirCondition);
		scn.StackCardsOn(theirCondition, theirStacked);
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(Zone.STACKED, myStacked.getZone());
		assertEquals(Zone.STACKED, theirStacked.getZone());
		assertEquals(8, scn.GetStrength(gimli));   // 6 printed + 2 from the axe
		assertTrue(scn.FreepsActionAvailable(axe));

		scn.FreepsUseCardAction(axe);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("stacked");

		assertTrue(scn.FreepsHasCardChoiceAvailable(myStacked, theirStacked));
		scn.FreepsChooseCard(theirStacked);

		assertEquals(Zone.DISCARD, theirStacked.getZone());
		assertEquals(Zone.STACKED, myStacked.getZone());
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityCanDiscardADwarvenCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var myCondition = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(myCondition);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(axe));

		// Nothing is stacked anywhere, so only the "discard a condition" branch is playable and is taken directly
		scn.FreepsUseCardAction(axe);

		assertEquals(Zone.DISCARD, myCondition.getZone());
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
