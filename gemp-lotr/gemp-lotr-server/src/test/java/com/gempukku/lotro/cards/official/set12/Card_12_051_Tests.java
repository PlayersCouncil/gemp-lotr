package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invigorated", "12_51");
					put("invigorated2", "12_51");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("cloak", "15_68");      // Ranger's Cloak: vitality +1 for the Merchant (no ranger, no companion: its site-number text stays off)
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("kotd", "8_38");        // King of the Dead, Oathbreaker: [gondor] Wraith companion, vitality 4; his Response answers a wound on Aragorn only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; "Valiant." is his whole text

					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, site 2, so not roaming at site 2; fierce only at 2 burdens or 2 wounds on Frodo
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5, so roaming at site 2; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, site 4, so roaming at site 2; its play trigger is not reached (cheated in)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InvigoratedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Invigorated
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to heal a [gondor] Man (or to heal 2 [gondor] Men if you can spot a roaming minion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("invigorated");

		assertEquals("Invigorated", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoRoamingMinionItHealsOneGondorManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invigorated = scn.GetFreepsCard("invigorated");
		var invigorated2 = scn.GetFreepsCard("invigorated2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");
		var pippin = scn.GetFreepsCard("pippin");
		var kotd = scn.GetFreepsCard("kotd");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, kotd, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, cloak);
		scn.MoveCardsToSupportArea(invigorated, invigorated2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(kotd, 1);
		scn.AddWoundsToChar(guard, 1);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(kotd));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, invigorated, invigorated2);

		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		assertTrue(scn.FreepsActionAvailable(invigorated));
		scn.FreepsUseCardAction(invigorated);
		// The copy used goes; the other stays where it was.
		assertInZone(Zone.DISCARD, invigorated);
		assertInZone(Zone.SUPPORT, invigorated2);

		// A [gondor] Man companion and the [gondor] Man ally; not the [gondor] Hobbit, the [gondor] Wraith or the [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, kotd, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(kotd));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithARoamingMinionInAnotherSkirmishItHealsExactlyTwoGondorMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invigorated = scn.GetFreepsCard("invigorated");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");
		var pippin = scn.GetFreepsCard("pippin");
		var kotd = scn.GetFreepsCard("kotd");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, kotd, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, cloak);
		scn.MoveCardsToSupportArea(invigorated);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(kotd, 1);
		scn.AddWoundsToChar(guard, 1);

		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(kotd));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, invigorated);

		scn.MoveMinionsToTable(nelya, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, runner});
		scn.FreepsResolveSkirmish(aragorn);
		// The roaming minion is spotted where it stands, waiting for Boromir; this skirmish's minion is not roaming.
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		scn.FreepsUseCardAction(invigorated);
		assertInZone(Zone.DISCARD, invigorated);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, kotd, guard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, merchant);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(kotd));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithARoamingMinionAndExactlyTwoWoundedGondorMenBothAreHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invigorated = scn.GetFreepsCard("invigorated");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, boromir, pippin);
		scn.MoveCardsToSupportArea(invigorated);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(pippin, 1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertInZone(Zone.SUPPORT, invigorated);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.FreepsUseCardAction(invigorated);

		// Both [gondor] Men, with nothing asked; the [gondor] Hobbit keeps his wound.
		assertInZone(Zone.DISCARD, invigorated);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedInTheAssignmentPhaseAndDiscardedForNothingWithNoWoundedGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invigorated = scn.GetFreepsCard("invigorated");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToSupportArea(invigorated);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(guard, 1);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, invigorated);

		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(invigorated));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(invigorated));
		scn.FreepsUseCardAction(invigorated);

		assertInZone(Zone.DISCARD, invigorated);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
