package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "103_73");
					put("rider", "12_161");      // Another Nazgul

					put("aragorn", "1_89");
					put("boromir", "1_96");
					put("athelas1", "1_94");     // Possession - on Aragorn
					put("athelas2", "1_94");     // Possession - on Boromir
					put("lastalliance1", "1_49"); // Condition - on Aragorn
					put("lastalliance2", "1_49"); // Condition - on Boromir
					put("sword", "1_112");       // Ranger's Sword - on Aragorn, no match
					put("saga", "1_114");        // Saga of Elendil - on Aragorn, no match
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Cantea, Exalted with Decay
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Skirmish: Exert this minion to hinder a possession or condition borne by a character it is skirmishing.  If you can spot another Nazgul, hinder all other copies of that card you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Exalted with Decay", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}


// ======== BASIC HINDER TESTS ========

	@Test
	public void CanteaHindersAttachmentsOnSkirmishingCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var lastalliance2 = scn.GetFreepsCard("lastalliance2");

		scn.MoveMinionsToTable(cantea);
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, athelas2, lastalliance2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(lastalliance2));
		assertEquals(0, scn.GetWoundsOn(cantea));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(cantea);

		// Cantëa exerts
		assertEquals(1, scn.GetWoundsOn(cantea));

		// Both attachments on Boromir hindered
		assertTrue(scn.IsHindered(athelas2));
		assertTrue(scn.IsHindered(lastalliance2));
	}

	@Test
	public void CanteaDoesNotHinderAttachmentsOnOtherCharactersWithoutAnotherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveMinionsToTable(cantea);  // No other Nazgul
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas1);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(cantea);

		// Boromir's Athelas hindered
		assertTrue(scn.IsHindered(athelas2));

		// Aragorn's Athelas NOT hindered - no other Nazgul to enable bonus
		assertFalse(scn.IsHindered(athelas1));
	}

	@Test
	public void CanteaHindersAllCopiesWithAnotherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var lastalliance1 = scn.GetFreepsCard("lastalliance1");
		var lastalliance2 = scn.GetFreepsCard("lastalliance2");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");

		scn.MoveMinionsToTable(cantea, rider);  // Another Nazgul present
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas1, lastalliance1, sword, saga);
		scn.AttachCardsTo(boromir, athelas2, lastalliance2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(cantea);

		// Boromir's attachments hindered (direct effect)
		assertTrue(scn.IsHindered(athelas2));
		assertTrue(scn.IsHindered(lastalliance2));

		// Aragorn's matching copies also hindered (bonus effect)
		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(lastalliance1));

		// Aragorn's non-matching cards NOT hindered
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(saga));
	}

	@Test
	public void CanteaAbilityNotAvailableWhenExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveMinionsToTable(cantea);
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, athelas2);
		scn.AddWoundsToChar(cantea, 2);  // Vitality 3, exhausted

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsPassCurrentPhaseAction();

		// Can't exert when exhausted
		assertFalse(scn.ShadowActionAvailable(cantea));
	}

	@Test
	public void CanteaBonusDoesNotTriggerIfNoCardsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var rider = scn.GetShadowCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas1 = scn.GetFreepsCard("athelas1");
		// Boromir has no attachments

		scn.MoveMinionsToTable(cantea, rider);
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(aragorn, athelas1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(cantea);

		// No cards on Boromir to hinder, so bonus effect doesn't fire
		// Aragorn's Athelas should NOT be hindered even with another Nazgul
		assertFalse(scn.IsHindered(athelas1));
	}
}
