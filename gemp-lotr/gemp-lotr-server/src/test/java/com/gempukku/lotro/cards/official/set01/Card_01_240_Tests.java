package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_240_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "1_240");
					put("band2", "1_240");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, strength 9, vitality 3; strength +2 only while bearing a weapon
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only ever cards in a hand
					put("filler2", "1_177");
					put("filler3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Band of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Response:</b> If this minion wins a skirmish, remove (2) to make the Free Peoples player discard a card at random from hand.
		*/

		var scn = GetScenario();

		var band = scn.GetFreepsCard("band");

		assertEquals("Band of the Eye", band.getBlueprint().getTitle());
		assertNull(band.getBlueprint().getSubtitle());
		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.SAURON, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.ORC, band.getBlueprint().getRace());
		assertEquals(4, band.getBlueprint().getTwilightCost());
		assertEquals(12, band.getBlueprint().getStrength());
		assertEquals(3, band.getBlueprint().getVitality());
		assertEquals(6, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersToRemoveTwoToDiscardACardAtRandomFromTheFreePeoplesHandAsOftenAsItCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(3, scn.GetFreepsHandCount());

		var band = scn.GetShadowCard("band");
		scn.MoveCardsToHand(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"));
		assertEquals(2, scn.GetShadowHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Strength 12 against 8: the minion wins.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);

		// At random: the Free Peoples player is asked nothing, and one card of his hand goes; the Shadow hand stays.
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// CR 4.2 "response": a response may be used again for the same win while its cost can be paid.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThanTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		assertEquals(2, scn.GetFreepsHandCount());

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		assertEquals(2, scn.GetFreepsHandCount());

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		// Strength 8 + 5 = 13 against 12: the minion is in the skirmish and loses it, and lives (vitality 3).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.FreepsPass();
		scn.SetTwilight(4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		assertEquals(0, scn.GetWoundsOn(band));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheCopyThatWonOffersAndAnotherOrcWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(3, scn.GetFreepsHandCount());

		var band = scn.GetShadowCard("band");
		var band2 = scn.GetShadowCard("band2");
		var swordsman = scn.GetShadowCard("swordsman");

		scn.StartGame();
		scn.MoveMinionsToTable(band, band2, swordsman);
		scn.FreepsPass();
		scn.SetTwilight(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, band}, new PhysicalCardImpl[]{guard, swordsman});
		// The second copy is left out of the skirmishes.
		scn.ShadowDeclineAssignments();

		// Orc Swordsman, strength 9, overwhelms Dwarf Guard, strength 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band2));
		scn.ShadowChooseOptionalTrigger(band);

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyFreePeoplesHandTheResponseIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var band = scn.GetShadowCard("band");
		scn.MoveCardsToHand(scn.GetShadowCard("filler1"));
		assertEquals(1, scn.GetShadowHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();
		scn.SetTwilight(3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetFreepsHandCount());
		// The response has a cost, so it is offered though there is nothing to discard.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowChooseOptionalTrigger(band);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
