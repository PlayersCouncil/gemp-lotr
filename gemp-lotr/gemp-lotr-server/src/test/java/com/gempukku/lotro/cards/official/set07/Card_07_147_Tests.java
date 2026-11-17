package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "7_147");
					put("veteran2", "7_147");
					// Easterling Ransacker: a [raider] Easterling Man; his only text is a skirmish ability
					put("ransacker", "7_144");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl who is an Easterling; at 3 burdens each
					// Easterling is strength +3, which no reading here turns on
					put("khamul", "103_61");
					// Desert Soldier: a [raider] Man who is a Southron and no Easterling; his trigger is for a
					// skirmish he is in, and he is in none
					put("soldier", "4_220");

					// Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br><b>Skirmish:</b> Remove 2 burdens to heal another Easterling.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Easterling Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesTwoBurdensToHealAnotherEasterlingUntilTwoCannotBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
		var ransacker = scn.GetShadowCard("ransacker");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(veteran, veteran2, ransacker, khamul, soldier);
		scn.AddWoundsToChar(veteran, 1);
		scn.AddWoundsToChar(veteran2, 1);
		scn.AddWoundsToChar(ransacker, 1);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(veteran2));
		assertEquals(1, scn.GetWoundsOn(ransacker));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, veteran);
		// The Ransacker joins the Veteran's skirmish; the others stay out of every skirmish.
		scn.ShadowAssignToMinions(aragorn, ransacker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);
		assertEquals(1, scn.GetBurdens());

		// Another Easterling: one in this skirmish, a Nazgûl in none, and the other copy of the Veteran.
		// Not the Veteran himself, nor a [raider] Man who is no Easterling.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ransacker, khamul, veteran2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(veteran, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(veteran2));
		assertEquals(1, scn.GetWoundsOn(ransacker));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetBurdens());

		// One burden is not two: neither copy is offered, with wounded Easterlings left to heal.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertFalse(scn.ShadowActionAvailable(veteran2));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WithNoOtherEasterlingWoundedTheBurdensAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var veteran = scn.GetShadowCard("veteran");
		var ransacker = scn.GetShadowCard("ransacker");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(veteran, ransacker, soldier);
		// Only the Veteran himself and the Southron are wounded.
		scn.AddWoundsToChar(veteran, 1);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
